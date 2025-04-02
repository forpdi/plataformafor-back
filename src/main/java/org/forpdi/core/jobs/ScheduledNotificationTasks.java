package org.forpdi.core.jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyUser;
import org.forpdi.core.notification.NotificationBS;
import org.forpdi.core.notification.NotificationSetting;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureHelper;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Controller para eventos agendados de notificações.
 * 
 * @author Pedro Mutter
 * 
 * 
 *
 */
@Configuration
public class ScheduledNotificationTasks {

	@Autowired
	private StructureBS sbs;
	@Autowired
	private StructureHelper structureHelper;
	@Autowired
	private PlanBS pbs;
	@Autowired
	private NotificationBS bs;
	@Autowired
	private UserBS ubs;
	@Autowired
	private FieldsBS fbs;
//	@Autowired Não faz sentido utilizar o current domain em uma task
//	private CompanyDomain domain;

	/**
	 * Tarefa que verifica se as metas do sistema estão vencidas ou próximas a
	 * vencer, se sim é enviado uma notificação ao usuário, atraves do sistema e
	 * por e-mail.
	 * 
	 * @throws SchedulerException
	 */
	// (second, minute, hour, day of month, month, day(s) of week)
	
//	@Scheduled(cron = JobsSetup.SCHEDULED_NOTIFICATION_CRON)
public void inspectGoalsMaturity() {
	PaginatedList<StructureLevelInstance> goals = this.sbs.listGoals();

	for (StructureLevelInstance goal : goals.getList()) {
		if (goal.isClosed()) {
			continue;
		}

		long idMacro = goal.getPlan().getParent().getId();
		List<AttributeInstance> attrList = this.sbs.listAttributeInstanceByLevel(goal, false);

		for (AttributeInstance attr : attrList) {
			if (attr.getAttribute().isFinishDate()) {
				processGoalMaturity(goal, attr, idMacro);
			}
		}
	}
}

private void processGoalMaturity(StructureLevelInstance goal, AttributeInstance attr, long idMacro) {
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(new Date());
	calendar.add(Calendar.DAY_OF_MONTH, 5);

	String url = generateGoalUrl(idMacro, goal);
	User manager = this.ubs.retrieveManager(goal);

	if (DateUtils.isSameDay(calendar.getTime(), attr.getValueAsDate())) {
		notifyGoalCloseToMaturity(goal, manager, url);
	} else if (new Date().after(attr.getValueAsDate())) {
		notifyLateGoal(goal, manager, url);
	}
}

private String generateGoalUrl(long idMacro, StructureLevelInstance goal) {
	return "/#/plan/" + idMacro + "/details/subplan/level/" + goal.getId();
}

private void notifyGoalCloseToMaturity(StructureLevelInstance goal, User manager, String url) {
	StructureLevelInstance parent = this.structureHelper.retrieveLevelInstance(goal.getParent());
	User parentResp = this.ubs.retrieveResponsible(parent);

	sendGoalNotification(manager, goal, parent, url, NotificationType.GOAL_CLOSE_TO_MATURITY);

	if (manager != null && parentResp != null && !manager.getId().equals(parentResp.getId())) {
		sendGoalNotification(parentResp, goal, parent, url, NotificationType.GOAL_CLOSE_TO_MATURITY);
	}
}

private void notifyLateGoal(StructureLevelInstance goal, User manager, String url) {
	StructureLevelInstance parent = this.structureHelper.retrieveLevelInstance(goal.getParent());

	sendGoalNotification(manager, goal, parent, url, NotificationType.LATE_GOAL);

	User parentResp = this.ubs.retrieveResponsible(parent);
	if (manager != null && parentResp != null && !manager.getId().equals(parentResp.getId())) {
		sendGoalNotification(parentResp, goal, parent, url, NotificationType.LATE_GOAL);
	}
}

private void sendGoalNotification(User recipient, StructureLevelInstance goal, StructureLevelInstance parent,
		String url, NotificationType notificationType) {
	if (recipient != null) {
	}
}

	/**
	 * Tarefa que verifica se os planos de metas estão próximas a vencer, se
	 * sim, uma notificação é enviado aos usuários pertencentes à instituição do
	 * plano de meta.
	 * 
	 * @throws SchedulerException
	 */
//	@Scheduled(cron = JobsSetup.SCHEDULED_NOTIFICATION_CRON)
	public void inspectPlansMaturity() {
		// O domain precisa ser definido corretamente (Erick Alves)
//		PaginatedList<PlanMacro> macros = this.pbs.listMacros(this.domain.getCompany(), false, null);
//		for (PlanMacro macro : macros.getList()) {
//			PaginatedList<Plan> plans = this.pbs.listPlans(macro, false, 0, null, null, 1);
//			for (Plan plan : plans.getList()) {
//				Calendar calendar = Calendar.getInstance();
//				calendar.setTime(new Date());
//				calendar.add(Calendar.DAY_OF_MONTH, 5);
//				if (DateUtils.isSameDay(calendar.getTime(), plan.getEnd())) {
//						String url = this.domain.getBaseUrl() + "/#/plan/" + plan.getParent().getId()
//								+ "/details/subplan/" + plan.getId();
//						this.bs.sendNotification(NotificationType.PLAN_CLOSE_TO_MATURITY, plan.getName(), "", null,
//								url);
//				}
//			}
//		}
	}

	/**
	 * Tarefa que verifica se os planos de ação estão próximas a vencer ou
	 * vencidos, se sim, uma notificação é enviado aos usuários pertencentes à
	 * instituição do plano de meta.
	 * 
	 * @throws SchedulerException
	 */
//	@Scheduled(cron = JobsSetup.SCHEDULED_NOTIFICATION_CRON)
	public void inspectActionPlanMaturity() {
		PaginatedList<ActionPlan> acList = this.fbs.listActionPlans();
		for (ActionPlan ap : acList.getList()) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_MONTH, 5);
			Date today = new Date();
			if (DateUtils.isSameDay(calendar.getTime(), ap.getEnd())) {
				// O domain precisa ser definido corretamente (Erick Alves)
//				String url = domain.getBaseUrl() + "/#/plan/" + ap.getLevelInstance().getPlan().getParent().getId()
//						+ "/details/subplan/level/" + ap.getLevelInstance().getId();
//
//				CompanyUser companyUser = this.ubs.retrieveCompanyUser(this.ubs.retrieveResponsible(ap.getLevelInstance()),
//						this.domain.getCompany());
//				if (companyUser.getNotificationSetting() == NotificationSetting.DEFAULT.getSetting() || companyUser
//						.getNotificationSetting() == NotificationSetting.RECEIVE_ALL_BY_EMAIL.getSetting()) {
//					this.bs.sendNotification(NotificationType.ACTION_PLAN_CLOSE_TO_MATURITY, ap.getDescription(),
//							ap.getLevelInstance().getName(),
//							this.ubs.retrieveResponsible(ap.getLevelInstance()).getId(), url);
//					this.bs.sendNotificationEmail(NotificationType.ACTION_PLAN_CLOSE_TO_MATURITY, ap.getDescription(),
//							ap.getLevelInstance().getName(),
//							this.ubs.retrieveResponsible(ap.getLevelInstance()), url);
//				} else if (companyUser.getNotificationSetting() == NotificationSetting.DO_NOT_RECEIVE_EMAIL
//						.getSetting()) {
//					this.bs.sendNotification(NotificationType.ACTION_PLAN_CLOSE_TO_MATURITY, ap.getDescription(),
//							ap.getLevelInstance().getName(),
//							this.ubs.retrieveResponsible(ap.getLevelInstance()).getId(), url);
//				}
			} else if (today.after(ap.getEnd())) {
				// O domain precisa ser definido corretamente (Erick Alves)
//				String url = domain.getBaseUrl() + "/#/plan/" + ap.getLevelInstance().getPlan().getParent().getId()
//						+ "/details/subplan/level/" + ap.getLevelInstance().getId();
//				CompanyUser companyUser = this.ubs.retrieveCompanyUser(
//						this.ubs.retrieveResponsible(ap.getLevelInstance()), this.domain.getCompany());
//				if (companyUser.getNotificationSetting() == NotificationSetting.DEFAULT.getSetting() || companyUser
//						.getNotificationSetting() == NotificationSetting.RECEIVE_ALL_BY_EMAIL.getSetting()) {
//					this.bs.sendNotification(NotificationType.LATE_ACTION_PLAN, ap.getDescription(),
//							ap.getLevelInstance().getName(),
//							this.ubs.retrieveResponsible(ap.getLevelInstance()).getId(), url);
//					this.bs.sendNotificationEmail(NotificationType.LATE_ACTION_PLAN, ap.getDescription(),
//							ap.getLevelInstance().getName(), this.ubs.retrieveResponsible(ap.getLevelInstance()),
//							url);
//				} else if (companyUser.getNotificationSetting() == NotificationSetting.DO_NOT_RECEIVE_EMAIL
//						.getSetting()) {
//					this.bs.sendNotification(NotificationType.LATE_ACTION_PLAN, ap.getDescription(),
//							ap.getLevelInstance().getName(),
//							this.ubs.retrieveResponsible(ap.getLevelInstance()).getId(), url);
//				}
			}
		}
	}

}
