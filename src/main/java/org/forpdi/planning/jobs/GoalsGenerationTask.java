package org.forpdi.planning.jobs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.HibernateDAO.TransactionalOperation;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.ManagerField;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.attribute.types.enums.Periodicity;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.system.jobsetup.JobsSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 *
 * Tarefa para gerar metas em segundo plano.
 *
 * @author Pedro Mutter
 * @author Renato Oliveira
 *
 */
@Configuration
public class GoalsGenerationTask {

	private ConcurrentLinkedQueue<GoalDTO> queue;
	private static final Logger LOG = LoggerFactory.getLogger(GoalsGenerationTask.class);

	@Autowired
	private HibernateDAO dao;
	
	public GoalsGenerationTask() {
		this.queue = new ConcurrentLinkedQueue<>();
	}

	/**
	 * Adiciona uma configuração de meta à fila e executa a tarefa
	 *
	 * @param goal
	 */
	public void add(GoalDTO goal) {
		this.queue.add(goal);
	}

	/**
	 * Execução da tarefa, que instancia os objetos nescessários e chama a
	 * função para gerar metas.
	 */
	@Scheduled(fixedRate=JobsSetup.GOALS_GENERATION_FIXED_RATE)
	public void execute() {
		if (this.queue.isEmpty()) {
			return;
		}

		try {
			while (!this.queue.isEmpty()) {
				Generator gen = new Generator(this.queue.poll());
				dao.execute(gen);
			}
		} catch (Throwable ex) {
			LOG.error("Unexpected error at goal generate task.", ex);
		}
	}

	private class Generator implements TransactionalOperation {
		
		private GoalDTO goal;
		
		public Generator(GoalDTO goal) {
			this.goal = goal;
		}

		@Override
		public void execute(HibernateDAO dao) {
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			Periodicity goalPeriodicity = Periodicity.getPeriodicityByName(goal.getPeriodicity());
			long periodicityTimeInMillis = calculatePeriodicityTimeInMillis(goalPeriodicity);

			List<Date> goalDates = calculateGoalDates(goal.getBeginDate(), goal.getEndDate(), periodicityTimeInMillis);
			for (Date currentDate : goalDates) {
				StructureLevelInstance levelInstance = createStructureLevelInstance(currentDate, df);
				dao.persist(levelInstance);

				persistGoalAttributes(dao, levelInstance, currentDate, df);
			}
		}

		private long calculatePeriodicityTimeInMillis(Periodicity goalPeriodicity) {
			long timeOffset = new GregorianCalendar(0, 0, 0).getTimeInMillis();
			return goalPeriodicity.getTimeInMilliseconds() - timeOffset;
		}

		private List<Date> calculateGoalDates(Date begin, Date end, long periodicityTimeInMillis) {
			List<Date> dates = new ArrayList<>();
			Date currentDate = new Date(begin.getTime());
			dates.add(currentDate);

			while (true) {
				currentDate = new Date(currentDate.getTime() + periodicityTimeInMillis);
				if (currentDate.after(end)) {
					currentDate = end;
				}
				dates.add(currentDate);
				if (currentDate.equals(end)) {
					break;
				}
			}

			return dates;
		}

		private StructureLevelInstance createStructureLevelInstance(Date currentDate, DateFormat df) {
			StructureLevelInstance levelInstance = new StructureLevelInstance();
			levelInstance.setName(goal.getName() + " - " + df.format(currentDate));
			levelInstance.setParent(goal.getParent());
			levelInstance.setLevel(goal.getLevel());
			levelInstance.setPlan(goal.getPlan());
			levelInstance.setCreation(new Date());
			levelInstance.setModification(new Date());
			levelInstance.setVisualized(false);
			return levelInstance;
		}

		private void persistGoalAttributes(HibernateDAO dao, StructureLevelInstance levelInstance, Date currentDate,
				DateFormat df) {
			AttributeInstance descriptionAttribute = createAttributeInstance(
					goal.getLevel().getAttributes().get(1), levelInstance, goal.getDescription());
			dao.persist(descriptionAttribute);

			for (Attribute attribute : goal.getLevel().getAttributes()) {
				AttributeInstance attributeInstance = createAttributeInstance(attribute, levelInstance, currentDate, df);
				if (attributeInstance != null) {
					dao.persist(attributeInstance);
					if (attribute.getAttributeInstances() == null) {
						attribute.setAttributeInstances(new ArrayList<>());
					}
					attribute.getAttributeInstances().add(attributeInstance);
				}
			}
		}

		private AttributeInstance createAttributeInstance(Attribute attribute, StructureLevelInstance levelInstance,
				Date currentDate, DateFormat df) {
			AttributeInstance attributeInstance = new AttributeInstance();
			attributeInstance.setAttribute(attribute);
			attributeInstance.setLevelInstance(levelInstance);

			if (attribute.getType().equals(ResponsibleField.class.getCanonicalName())) {
				attributeInstance.setValue(goal.getResponsible());
			} else if (attribute.getType().equals(ManagerField.class.getCanonicalName())) {
				attributeInstance.setValue(goal.getManager());
			} else if (attribute.isFinishDate()) {
				attributeInstance.setValue(df.format(currentDate));
				attributeInstance.setValueAsDate(currentDate);
			} else if (attribute.isExpectedField()) {
				attributeInstance.setValueAsNumber(goal.getExpected());
				attributeInstance.setValue(String.valueOf(goal.getExpected()));
			} else if (attribute.isMinimumField()) {
				attributeInstance.setValueAsNumber(goal.getMinimum());
				attributeInstance.setValue(String.valueOf(goal.getMinimum()));
			} else if (attribute.isMaximumField()) {
				attributeInstance.setValueAsNumber(goal.getMaximum());
				attributeInstance.setValue(String.valueOf(goal.getMaximum()));
			} else if (attribute.isReachedField()) {
				attributeInstance.setValueAsNumber(goal.getReached());
				if (goal.getReached() != null) {
					attributeInstance.setValue(String.valueOf(goal.getReached()));
				}
			}

			return attributeInstance.getValue() != null ? attributeInstance : null;
		}

		private AttributeInstance createAttributeInstance(Attribute attribute, StructureLevelInstance levelInstance,
				String value) {
			AttributeInstance attributeInstance = new AttributeInstance();
			attributeInstance.setAttribute(attribute);
			attributeInstance.setLevelInstance(levelInstance);
			attributeInstance.setValue(value);
			return attributeInstance;
		}
	}
}
