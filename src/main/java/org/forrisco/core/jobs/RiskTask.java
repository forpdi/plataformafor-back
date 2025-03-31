package org.forrisco.core.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.mail.EmailException;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.jobs.EmailSenderTask;
import org.forpdi.core.notification.Notification;
import org.forpdi.core.notification.NotificationEmail;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.user.User;
import org.forpdi.core.utils.NotificationUtil;
import org.forpdi.system.EmailUtilsPlugin.EmailBuilder;
import org.forpdi.system.jobsetup.JobsSetup;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.risk.CloseToMaturityPeriod;
import org.forrisco.risk.Risk;
import org.forrisco.risk.monitor.Monitor;
import org.hibernate.criterion.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.ibm.icu.util.Calendar;

/**
 * Tarefa que é executada a cada Hora para verificar o estado dos riscos
 * 
 * @author Matheus Nascimento
 * 
 */
@Configuration
public class RiskTask {
	private static Logger LOG = LoggerFactory.getLogger(RiskTask.class);
	private final int risksPageSize = 400;

	@Autowired
	private HibernateDAO dao;
	
	@Autowired
	private EmailSenderTask emailSenderTask;

	/**
	 * Método que é executado a cada hora,
	 * 
	 */
	@Scheduled(fixedRate = JobsSetup.RISK_TASK_FIXED_RATE)
	public void execute() {
		checkCloseToExpireRisks();
	}

	/**
	 *
	 * verificando se algum risco mudou de "Em dia" para "Próximo a vencer" na
	 * ultima hora
	 * 
	 */
	private void checkCloseToExpireRisks() {
		
		LOG.info("Verificando monitoramento dos Riscos...");
				
		try {

			List<Risk> risks = listRisksToSendNotification();
			
			for (Risk risk : risks) {
				String texto = risk.getName();
				CompanyDomain companyDomain = companyDomainByRisk(risk);

				String url = companyDomain.getBaseUrl() + "/#/forrisco/risk/" + risk.getId() + "/details/monitors";
				
				for (User user : listUsersToNotification(risk)) {
					sendNotification(NotificationType.FORRISCO_RISK_CLOSE_TO_MATURITY, texto, user, url, companyDomain);
				}
			}
		} catch (Throwable ex) {
			LOG.error("Unexpected error occurred while verifying monitors.", ex);
		}
	}
	
	private List<Risk> listRisksToSendNotification() {
		Date now = new Date();
		
		Calendar calendar = Calendar.getInstance();
		
		long total = countRisks();
		List<Risk> selectedRisks = new ArrayList<>();
		long numOfPages = (long) Math.ceil(total / (double) risksPageSize);
		
		for (int page = 1; page <= numOfPages; page++) {
			List<Risk> risks = listRisks(page);
			
			for (Risk risk : risks) {
				Monitor monitor = lastMonitorbyRisk(risk);
				Date lastMonitorDate = risk.getBegin();

				if (monitor != null) {
					lastMonitorDate = monitor.getBegin();
				}
				
				calendar.setTime(lastMonitorDate);

				Date closeToMaturityDate = getCloseToMaturityDate(risk, lastMonitorDate);
				
				long diffTime = now.getTime() - closeToMaturityDate.getTime();
				
				if (diffTime < JobsSetup.RISK_TASK_FIXED_RATE && diffTime > 0) {
					selectedRisks.add(risk);
				}
			}
		}

		return selectedRisks;
	}

	private List<Risk> listRisks(int page) {
		Criteria criteria = this.dao.newCriteria(Risk.class)
				.add(Restrictions.eq("deleted", false));

		criteria.setFirstResult((page - 1) * risksPageSize)
			.setMaxResults(risksPageSize);

		return this.dao.findByCriteria(criteria, Risk.class);
	}

	private long countRisks() {
		Criteria count = this.dao.newCriteria(Risk.class)
				.add(Restrictions.eq("deleted", false))
				.setProjection(Projections.countDistinct("id"));

		return (long) count.uniqueResult();
	}

	private Monitor lastMonitorbyRisk(Risk risk) {

		Criteria criteria = this.dao.newCriteria(Monitor.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk)).addOrder(Order.desc("begin"));

		criteria.setMaxResults(1);
		Monitor monitor = (Monitor) criteria.uniqueResult();

		return monitor;
	}

	private Date getCloseToMaturityDate(Risk risk, Date lastMonitorDate) {
		long time = lastMonitorDate.getTime();
		long hour = 1000 * 60 * 60L;
		long day = hour * 24;

		switch (risk.getPeriodicity().toLowerCase()) {
		case "diária":
			time += hour * (24 - CloseToMaturityPeriod.DIARIO.getValue());
			break;

		case "semanal":
			time += day * (7 - CloseToMaturityPeriod.SEMANAL.getValue());
			break;

		case "quinzenal":
			time += day * (15 - CloseToMaturityPeriod.QUINZENAL.getValue());
			break;

		case "mensal":
			time += day * (30 - CloseToMaturityPeriod.MENSAL.getValue());
			break;

		case "bimestral":
			time += day * (60 - CloseToMaturityPeriod.BIMESTRAL.getValue());
			break;

		case "trimestral":
			time += day * (90 - CloseToMaturityPeriod.TRIMESTRAL.getValue());
			break;

		case "semestral":
			time += day * (180 - CloseToMaturityPeriod.SEMESTRAL.getValue());
			break;

		case "anual":
			time += day * (360 - CloseToMaturityPeriod.ANUAL.getValue());
			break;
		}

		return new Date(time);
	}

	private CompanyDomain companyDomainByRisk(Risk risk) {

		PlanRisk planRisk = this.dao.exists(risk.getUnit().getPlanRisk().getId(), PlanRisk.class);

		Company company = planRisk.getPolicy().getCompany();

		Criteria criteria = this.dao.newCriteria(CompanyDomain.class)
				.createAlias("company", "company")
				.add(Restrictions.eq("company.id", company.getId()));

		return (CompanyDomain) criteria.uniqueResult();
	}

	private Set<User> listUsersToNotification(Risk risk) {
		Set<User> users = new HashSet<>();
		
		users.add(risk.getUser());
		users.add(risk.getManager());
		users.add(risk.getUnit().getUser());
		
		return users;
	}
	
	private void sendNotification(NotificationType type, String text, User user, String url,
			CompanyDomain companyDomain) throws EmailException {
		if (url == null) {
			url = companyDomain.getBaseUrl();
		}
		url = url.replace("//#", "/#");
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setCompany(companyDomain.getCompany());
		notification.setCreation(new Date());
		notification.setOnlyEmail(false);
		notification.setPicture(type.getImageUrl());
		notification.setVizualized(false);
		notification.setType(type.getValue());
		notification.setUrl(url);
		NotificationUtil.setDescriptionForNotification(notification, type, text, null);
		EmailBuilder builder = new EmailBuilder(type, notification.getDescription(), url);

		this.dao.persist(notification);
		
		NotificationEmail notificationEmail = new NotificationEmail(user.getEmail(), user.getName(),
				builder.getSubject(), builder.getBody(), null, NotificationType.FORRISCO_RISK_CLOSE_TO_MATURITY);
		this.emailSenderTask.add(notificationEmail);
	}
}
