package org.forpdi.planning.jobs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.forpdi.core.common.PaginatedList;
import org.forpdi.dashboard.DashboardBS;
import org.forpdi.planning.attribute.types.enums.Periodicity;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Controller para eventos agendados do painel de bordo.
 * 
 * @author Pedro Mutter
 *
 * 
 */
@Configuration
public class ScheduledDasboardTasks {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledDasboardTasks.class);
	
	@Autowired
	private StructureBS sbs;
	@Autowired
	private DashboardBS bs;

	/**
	 * Tarefa para salvar os valores atuais dos indicadores, executada todo dia
	 * às 13:00
	 * 
	 * @throws SchedulerException
	 */
//	@Scheduled(cron = JobsSettup.SCHEDULE_DASHBOARD_CRON)
	public void saveIndicatorHistory() {
		LOGGER.info("Atualizando os indicadores...");
		PaginatedList<StructureLevelInstance> instances = this.sbs.listAllIndicators();
		for (StructureLevelInstance instance : instances.getList()) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			if (instance.getNextSave() == null || calendar.getTime().after(instance.getNextSave())
					|| calendar.getTime().equals(instance.getNextSave())) {
				Periodicity peri = this.sbs.getPeriodicityByInstance(instance);
				if (peri == null) {
					LOGGER.info("Histórico do indicador " + instance.getName()
							+ " não pode ser salvo, periodicidade nula.");
				} else {
					this.bs.saveIndicatorHistory(instance, peri);
					LOGGER.info("Histórico do indicador " + instance.getName()
							+ " salvo com sucesso, próximo registro em "
							+ new SimpleDateFormat("dd/MM/yyyy").format(instance.getNextSave()));
				}
			}
		}
	}
}
