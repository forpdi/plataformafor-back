package org.forpdi.planning.jobs;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.HibernateDAO.TransactionalOperation;
import org.forpdi.planning.attribute.AggregateIndicator;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.bean.PerformanceBean;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanDetailed;
import org.forpdi.planning.structure.Polarity;
import org.forpdi.planning.structure.StructureHelper;
import org.forpdi.planning.structure.StructureLevel;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.planning.structure.StructureLevelInstanceDetailed;
import org.forpdi.system.jobsetup.JobsSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Realiza cálculos e atualizações necessárias quando uma
 * instância de nível tem algum atributo atualizado.
 * 
 * @author Renato R. R. de Oliveira
 *
 */
@Configuration
public class OnLevelInstanceUpdateTask {

	private ConcurrentLinkedQueue<StructureLevelInstance> queue;
	private static final Logger LOG = LoggerFactory.getLogger(OnLevelInstanceUpdateTask.class);

	@Autowired
	private HibernateDAO dao;
	
	@Autowired
	private StructureHelper structHelper;
	
	@Autowired
	AttributeHelper attrHelper;
	
	public OnLevelInstanceUpdateTask() {
		queue = new ConcurrentLinkedQueue<>();
	}

	/**
	 * Adiciona uma instância de nível para ser processada pela task.
	 * 
	 * @param goal
	 */
	public void add(StructureLevelInstance level) {
		queue.add(level);
	}
	public void add(List<StructureLevelInstance> levels) {
		queue.addAll(levels);
	}
	
	/**
	 * Método que é chamado toda vez em que a job é executada.
	 */
	@Scheduled(fixedRate = JobsSetup.ON_LEVEL_INSTANCE_UPDATE_FIXED_RATE)
	public void execute() {
		if (this.queue.isEmpty()) {
			return;
		}
		LOG.info("Processing level instance queue with {} level instances...", this.queue.size());
		int count = 0;
		try {
			while (!this.queue.isEmpty()) {
				var updater = new LevelInstanceUpdater(this, this.queue.poll());
				dao.execute(updater);
				count++;
			}
		} catch (Throwable ex) {
			LOG.error("Exceção ao executar a task.", ex);
		}
		LOG.info("Finished updating {} level instances.", count);
	}
	
	private class LevelInstanceUpdater implements TransactionalOperation {
		private OnLevelInstanceUpdateTask task;
		private StructureLevelInstance levelInstance;
		
		public LevelInstanceUpdater(OnLevelInstanceUpdateTask task, StructureLevelInstance levelInstance) {
			this.task = task;
			this.levelInstance = levelInstance;
		}

		@Override
		public void execute(HibernateDAO contextDao) {
			levelInstance = structHelper.retrieveLevelInstance(levelInstance.getId());
			StructureLevel level = levelInstance.getLevel();
			
			if (!level.isGoal()) {
				if (level.isIndicator() && levelInstance.isAggregate()) {
					structHelper.updateAggregatedLevelValue(levelInstance);
				} else if (level.isIndicator()) {
					PerformanceBean performance = structHelper.calculateIndicatorLevelValue(levelInstance);
					levelInstance.setLevelValue(performance.getPerformance());
					levelInstance.setLevelMinimum(performance.getMinimumAverage());
					levelInstance.setLevelMaximum(performance.getMaximumAverage());
					contextDao.persist(levelInstance);
				} else {
					PerformanceBean performance = structHelper.calculateLevelValue(levelInstance);
					levelInstance.setLevelValue(performance.getPerformance());
					levelInstance.setLevelMinimum(performance.getMinimumAverage());
					levelInstance.setLevelMaximum(performance.getMaximumAverage());
					contextDao.persist(levelInstance);
				}
			} else {
				AttributeInstance reachedAttribute = attrHelper.retrieveReachedFieldAttribute(levelInstance);
				Double reached = null;
				Double expected = null;
				Double minimum = null;
				Double maximum = null;
				if (reachedAttribute == null) {
					LOG.error("Goal level instance without reached attribute? level instance id: {}", levelInstance.getId());
				} else {
					expected = attrHelper.retrieveExpectedFieldAttribute(levelInstance).getValueAsNumber();
					minimum = attrHelper.retrieveMinimumFieldAttribute(levelInstance).getValueAsNumber();
					maximum = attrHelper.retrieveMaximumFieldAttribute(levelInstance).getValueAsNumber();
					reached = reachedAttribute.getValueAsNumber();
				}
				
				if (expected != null && reached != null) {
					AttributeInstance polarity = attrHelper.retrievePolarityAttributeInstance(levelInstance.getParent());
					if (polarity == null || polarity.getValue().equals(Polarity.BIGGER_BETTER.getValue())) {
						if (expected == 0) {
							levelInstance.setLevelValue(100.0);
							levelInstance.setLevelMinimum(100.0);
							levelInstance.setLevelMaximum(100.0);
						} else {
							levelInstance.setLevelValue(reached * 100.0 / expected);
							levelInstance.setLevelMinimum(minimum * 100.0 / expected);
							levelInstance.setLevelMaximum(maximum * 100.0 / expected);
						}
					} else {
						if (expected == 0) {
							levelInstance.setLevelValue(100.0 - reached >= 0.0 ? 100.0 - reached : 0.0);
							levelInstance.setLevelMinimum(100.0 + minimum - reached >= 0.0 ? (100.0 + minimum - reached) : 0.0);
							levelInstance.setLevelMaximum(100.0 + maximum - reached >= 0.0 ? (100.0 + maximum - reached) : 0.0);
						} else {
							levelInstance.setLevelValue(100.0 * ((2.0 * expected) - reached) / expected);
							levelInstance.setLevelMinimum(100.0 * ((2.0 * expected) - minimum) / expected);
							levelInstance.setLevelMaximum(100.0 * ((2.0 * expected) - maximum) / expected);
						}
					}
					this.saveLevelInstanceValues(levelInstance, contextDao);
					
				} else {
					levelInstance.setLevelValue(null);
					this.saveLevelInstanceValues(levelInstance, contextDao);
				}
				contextDao.persist(levelInstance);
			}
			
			if (levelInstance.getParent() != null) {
				StructureLevelInstance parent = contextDao.exists(levelInstance.getParent(), StructureLevelInstance.class);
				if (parent != null)
					task.add(parent);
				else
					LOG.error("An inconsistency on level instance with id {}, it refers to an unexistent parent id {}",
							levelInstance.getId(), levelInstance.getParent());
			} else {
				Plan plan = levelInstance.getPlan();
				PerformanceBean performance = structHelper.calculatePlanPerformance(plan);
				plan.setPerformance(performance.getPerformance());
				plan.setMinimumAverage(performance.getMinimumAverage());
				plan.setMaximumAverage(performance.getMaximumAverage());
				contextDao.persist(plan);
			}
			
			if (level.isIndicator()) {
				List<AggregateIndicator> indicators = structHelper.getAggregatedToIndicators(levelInstance);
				if (!GeneralUtils.isEmpty(indicators)) {
					for (AggregateIndicator indicator : indicators) {
						task.add(indicator.getIndicator());
					}
				}
			}
			
			/*
			StructureLevelInstance indicatorLevelInstance = null;
			if (levelInstance.getParent() != null) {
				boolean haveParent = true;
				while (haveParent) {
					levelInstance = this.retrieveLevelInstance(levelInstance.getParent());
					if (levelInstance.getParent() == null)
						haveParent = false;
					List<StructureLevelInstance> structureLevelInstances = this
							.retrieveLevelInstanceSons(levelInstance.getId());
					if (structureLevelInstances != null) {
						Double total = 0.0;
						for (StructureLevelInstance structureLevelInstance : structureLevelInstances) {
							if (structureLevelInstance.getLevelValue() != null)
								total += structureLevelInstance.getLevelValue();
						}
						if (structureLevelInstances.size() > 0) {
							int size = structureLevelInstances.size();
							if (levelInstance.getLevel().isIndicator()) {
								for (StructureLevelInstance structureLevelInstance : structureLevelInstances) {
									structureLevelInstance.getLevel()
											.setAttributes(this.retrieveLevelAttributes(structureLevelInstance.getLevel()));
									for (Attribute attribute : structureLevelInstance.getLevel().getAttributes()) {
										if (attribute.isFinishDate()) {
											AttributeInstance attributeInstance = this
													.retrieveAttributeInstance(structureLevelInstance, attribute);
											if (structureLevelInstance.getLevelValue() == null && (attributeInstance == null
													|| attributeInstance.getValueAsDate().after(new Date())))
												size--;
										}
									}
								}
								if (size != 0)
									levelInstance.setLevelValue(total / size);
							} else {
								for (StructureLevelInstance structureLevelInstance : structureLevelInstances) {
									if (structureLevelInstance.getLevelValue() == null)
										size--;
									else
										levelInstance.setLevelValue(null);
								}
								if (size != 0)
									levelInstance.setLevelValue(total / size);
								else
									levelInstance.setLevelValue(null);
							}
						} else {
							levelInstance.setLevelValue(null);
						}
						this.persist(levelInstance);
					}
	
					if (levelInstance.isAggregate()) {
						List<BigInteger> indicators = this.listAggregateIndicatorsByAggregate(levelInstance);
						for (BigInteger indicatorId : indicators) {
							StructureLevelInstance indicator = this.retrieveLevelInstance(indicatorId.longValue());
							this.setAggregateIndicatorValue(indicator);
						}
					}
					
					if (levelInstance.getLevel().isIndicator() && !levelInstance.isAggregate()) {
						indicatorLevelInstance = levelInstance;
					}
				}
				
				if (indicatorLevelInstance != null) {
					List<BigInteger> listAggregatesIndicatorsId = this.listAggregateIndicatorsByAggregate(indicatorLevelInstance);
					if (listAggregatesIndicatorsId.size() > 0) {
						for (BigInteger indAggId : listAggregatesIndicatorsId) {
							this.setAggregateIndicatorValue(this.retrieveLevelInstance(indAggId.longValue()));
						}
					}
				}
			}*/
		}

		
	/*Salva as atualizações da instância de nível atual */
		private void saveLevelInstanceValues(StructureLevelInstance levelInstance, HibernateDAO contextDao) {
			
			AttributeInstance finishDate = attrHelper.retrieveFinishDateFieldAttribute(levelInstance);
			if (finishDate != null) {
				StructureLevelInstanceDetailed levelInstanceDetailed = structHelper.getLevelInstanceDetailed(levelInstance, finishDate);
				contextDao.persist(levelInstanceDetailed);
				
				StructureLevelInstance parentLevelInstance = levelInstance;
				while (parentLevelInstance.getParent() != null) {
					parentLevelInstance = structHelper.retrieveLevelInstance(parentLevelInstance.getParent());
					PerformanceBean performance = structHelper.calculateLevelValueDetailed(parentLevelInstance, finishDate);
					parentLevelInstance.setLevelValue(performance.getPerformance());
					parentLevelInstance.setLevelMinimum(performance.getMinimumAverage());
					parentLevelInstance.setLevelMaximum(performance.getMaximumAverage());
					levelInstanceDetailed = structHelper.getLevelInstanceDetailed(parentLevelInstance, finishDate);
					contextDao.persist(levelInstanceDetailed);
				}
				
				Plan plan = levelInstance.getPlan();
				PerformanceBean performance = structHelper.calculatePlanPerformanceDetailed(plan, finishDate);
				plan.setPerformance(performance.getPerformance());
				plan.setMinimumAverage(performance.getMinimumAverage());
				plan.setMaximumAverage(performance.getMaximumAverage());
				PlanDetailed planDetailed = structHelper.getPlanDetailed(plan, finishDate);
				contextDao.persist(planDetailed);
			}
		}
		
		
		/** Realiza cálculos e atualizações para a instância de nível atual e
		 * enfileira a instância de nível pai para ser atualizada por esta própria
		 * task, a grosso modo, chama "recursivamente" para os pais. */
	}
}
