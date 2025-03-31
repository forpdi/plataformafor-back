package org.forpdi.dashboard.goalsinfo;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.Restrictions;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.system.jobsetup.JobLockService;
import org.forpdi.system.jobsetup.JobsSetup;
import org.hibernate.sql.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class GoalsInfoCalculatorTask {

	private static final Logger LOG = LoggerFactory.getLogger(GoalsInfoCalculatorTask.class);
	
	private static final long LOCK_SECONDS = 2 * 60 * 60;

	@Autowired
	private HibernateDAO dao;
	@Autowired
	private GoalsInfoBS bs;
	@Autowired
	private JobLockService jobLockService;

	@Scheduled(cron = JobsSetup.GOALS_INFO_CALCULATOR_TASK_CRON)
	public void execute() {
		var lockExpiryAt = LocalDateTime.now().plusSeconds(LOCK_SECONDS);
		if (jobLockService.lockJob(GoalsInfoCalculatorTask.class, lockExpiryAt)) {
			LOG.info("Atualizando informações das metas...");
			long totalGoals = 0;
			List<PlanMacro> planMacros = listPlanMacros();
			for (PlanMacro planMacro : planMacros) {
				List<StructureLevelInstance> goals = listGoalsByPlanMacro(planMacro);
				GoalsInfo goalsInfo = bs.calculateAdminGoalsInfo(goals);
				GoalsInfo existent = bs.retrieveGoalsInfoByPlanMacro(planMacro.getId());
				Date now = new Date();
				if (existent != null) {
					existent.updateInfos(goalsInfo);
					existent.setUpdatedAt(now);
					dao.merge(existent);
				} else {
					goalsInfo.setPlanMacro(planMacro);
					goalsInfo.setUpdatedAt(now);
					dao.merge(goalsInfo);
				}
				
				totalGoals += goals.size();
			}
			
			LOG.info("Atualização de metas concluída: " + totalGoals + " processadas");
		}
	}

	public List<StructureLevelInstance> listGoalsByPlanMacro(PlanMacro macro) {
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.createAlias("level", "level", JoinType.INNER_JOIN);
		criteria.createAlias("plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("level.goal", true));
		criteria.add(Restrictions.eq("macro.archived", false));
		criteria.add(Restrictions.eq("plan.parent", macro));

		return dao.findByCriteria(criteria, StructureLevelInstance.class);
	}
	
	public List<PlanMacro> listPlanMacros() {
		Criteria criteria = this.dao.newCriteria(PlanMacro.class)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.eq("archived", false));
		return dao.findByCriteria(criteria, PlanMacro.class);
	}
}
