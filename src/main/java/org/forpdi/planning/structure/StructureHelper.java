package org.forpdi.planning.structure;

import java.util.Date;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Disjunction;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyUser;
import org.forpdi.core.notification.NotificationBS;
import org.forpdi.core.notification.NotificationSetting;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.planning.attribute.AggregateIndicator;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.enums.CalculationType;
import org.forpdi.planning.bean.PerformanceBean;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanDetailed;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Classe com implementações de métodos para auxílios nas classes de negócio
 * e para as jobs assíncronas. Contempla regras relacionadas às estruturas e
 * aos níveis de informação (StructureLevel).
 * 
 * @author Renato R. R. de Oliveira
 *
 */
@Service
public class StructureHelper {
	@Autowired
	private HibernateDAO dao;
	
	@Autowired
	private UserBS userBS;
	
	@Autowired
	private NotificationBS notificationBS;

	
	/**
	 * Buscar uma instância de um level pelo id.
	 */
	public StructureLevelInstance retrieveLevelInstance(Long id) {
		StructureLevelInstance levelInstance = this.dao.exists(id, StructureLevelInstance.class);
		this.fillIndicators(levelInstance);
		return levelInstance;
	}
	
	/**
	 * Busca detalhada de uma instância de um level.
	 */
	public StructureLevelInstanceDetailed getLevelInstanceDetailed(StructureLevelInstance levelInstance, AttributeInstance finishDate) {
		int month = finishDate.getValueAsDate().getMonth()+1;
		int year = finishDate.getValueAsDate().getYear()+1900;
		Criteria criteria = this.dao.newCriteria(StructureLevelInstanceDetailed.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("levelInstance", levelInstance));
		criteria.add(Restrictions.eq("month", month));
		criteria.add(Restrictions.eq("year", year));
		StructureLevelInstanceDetailed levelInstanceDetailed = (StructureLevelInstanceDetailed) criteria.uniqueResult();
		
		if (levelInstanceDetailed == null)
			levelInstanceDetailed = new StructureLevelInstanceDetailed();
		levelInstanceDetailed.setLevelInstance(levelInstance);
		levelInstanceDetailed.setMonth(month);
		levelInstanceDetailed.setYear(year);
		levelInstanceDetailed.setLevelValue(levelInstance.getLevelValue());
		levelInstanceDetailed.setLevelMinimum(levelInstance.getLevelMinimum());
		levelInstanceDetailed.setLevelMaximum(levelInstance.getLevelMaximum());
		return levelInstanceDetailed;
	}
	
	/**
	 * Busca detalhada de um plano.
	 */
	public PlanDetailed getPlanDetailed(Plan plan, AttributeInstance finishDate) {
		int month = finishDate.getValueAsDate().getMonth()+1;
		int year = finishDate.getValueAsDate().getYear()+1900;
		Criteria criteria = this.dao.newCriteria(PlanDetailed.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("plan", plan));
		criteria.add(Restrictions.eq("month", month));
		criteria.add(Restrictions.eq("year", year));
		PlanDetailed planDetailed = (PlanDetailed) criteria.uniqueResult();
		
		if (planDetailed == null)
			planDetailed = new PlanDetailed();
		planDetailed.setPlan(plan);
		planDetailed.setMonth(month);
		planDetailed.setYear(year);
		planDetailed.setPerformance(plan.getPerformance());
		planDetailed.setMinimumAverage(plan.getMinimumAverage());
		planDetailed.setMaximumAverage(plan.getMaximumAverage());
		return planDetailed;
	}
	
	/** Calcula a média do valor no nível abaixo. */
	public PerformanceBean calculateLevelValue(StructureLevelInstance levelInstance) {
		Criteria criteria =
			this.dao.newCriteria(StructureLevelInstance.class)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.eq("parent", levelInstance.getId()))
			.add(Restrictions.isNotNull("levelValue"))
			.setProjection(
				Projections.projectionList()
				.add(Projections.avg("levelValue"), "performance")
				.add(Projections.avg("levelMinimum"), "minimumAverage")
				.add(Projections.avg("levelMaximum"), "maximumAverage")
			)
			.setResultTransformer(PerformanceBean.class)
		;
		return (PerformanceBean) criteria.uniqueResult();
	}
	
	/** Calcula a média do valor no nível indicador abaixo. */
	public PerformanceBean calculateIndicatorLevelValue(StructureLevelInstance levelInstance) {
		Date date = new Date();
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.createAlias("attribute", "attribute", JoinType.INNER_JOIN);
		criteria.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN);
		criteria.add(Restrictions.isNotNull("valueAsDate"));
		criteria.add(Restrictions.isNotNull("levelInstance.levelValue"));
		criteria.add(Restrictions.eq("attribute.finishDate", true));
		criteria.add(Restrictions.eq("levelInstance.deleted", false));
		criteria.add(Restrictions.eq("levelInstance.parent", levelInstance.getId()));
		Disjunction or = Restrictions.disjunction();
		or.add(Restrictions.le("valueAsDate", date));
		or.add(Restrictions.isNotNull("levelInstance.levelValue"));
		criteria.add(or);
		
		List<AttributeInstance> list = this.dao.findByCriteria(criteria, AttributeInstance.class);
		Double performance = 0.0;
		Double minimumAverage = 0.0;
		Double maximumAverage = 0.0;
		for (AttributeInstance attrInstance : list) {
			if (attrInstance.getLevelInstance().getLevelValue() != null)
				performance = performance + attrInstance.getLevelInstance().getLevelValue();
			if (attrInstance.getLevelInstance().getLevelMinimum() != null)
				minimumAverage = minimumAverage + attrInstance.getLevelInstance().getLevelMinimum();
			if (attrInstance.getLevelInstance().getLevelMaximum() != null)
				maximumAverage = maximumAverage + attrInstance.getLevelInstance().getLevelMaximum();
		}
		PerformanceBean performanceBean = new PerformanceBean();
		if(list.size() > 0) {
			performanceBean.setPerformance(performance/list.size());
			performanceBean.setMinimumAverage(minimumAverage/list.size());
			performanceBean.setMaximumAverage(maximumAverage/list.size());
		} else {
			performanceBean.setPerformance(performance);
			performanceBean.setMinimumAverage(minimumAverage);
			performanceBean.setMaximumAverage(maximumAverage);
		}
		
		return performanceBean;
	}
	
	/** Calcula a média do valor no nível abaixo detalhado. */
	public PerformanceBean calculateLevelValueDetailed(StructureLevelInstance levelInstance, AttributeInstance finishDate) {
		int month = finishDate.getValueAsDate().getMonth()+1;
		int year = finishDate.getValueAsDate().getYear()+1900;
		Criteria criteria =
			this.dao.newCriteria(StructureLevelInstanceDetailed.class)
			.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.isNotNull("levelValue"))
			.add(Restrictions.eq("month", month))
			.add(Restrictions.eq("year", year))
			.add(Restrictions.eq("levelInstance.deleted", false))
			.add(Restrictions.eq("levelInstance.parent", levelInstance.getId()))
			.setProjection(
				Projections.projectionList()
				.add(Projections.avg("levelValue"), "performance")
				.add(Projections.avg("levelMinimum"), "minimumAverage")
				.add(Projections.avg("levelMaximum"), "maximumAverage")
			)
			.setResultTransformer(PerformanceBean.class)
		;
		return (PerformanceBean) criteria.uniqueResult();
	}
	
	/** Calcula a média do valor do primeiro nível do plano. */
	public PerformanceBean calculatePlanPerformance(Plan plan) {
		Criteria criteria =
			this.dao.newCriteria(StructureLevelInstance.class)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.eq("plan", plan))
			.add(Restrictions.isNull("parent"))
			.add(Restrictions.isNotNull("levelValue"))
			.setProjection(
				Projections.projectionList()
				.add(Projections.avg("levelValue"), "performance")
				.add(Projections.avg("levelMinimum"), "minimumAverage")
				.add(Projections.avg("levelMaximum"), "maximumAverage")
			)
			.setResultTransformer(PerformanceBean.class)
		;
		return (PerformanceBean) criteria.uniqueResult();
	}
	
	/** Calcula a média do valor do primeiro nível do plano. */
	public PerformanceBean calculatePlanPerformanceDetailed(Plan plan, AttributeInstance finishDate) {
		int month = finishDate.getValueAsDate().getMonth()+1;
		int year = finishDate.getValueAsDate().getYear()+1900;
		Criteria criteria =
			this.dao.newCriteria(StructureLevelInstanceDetailed.class)
			.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.isNotNull("levelValue"))
			.add(Restrictions.eq("month", month))
			.add(Restrictions.eq("year", year))
			.add(Restrictions.eq("levelInstance.deleted", false))
			.add(Restrictions.eq("levelInstance.plan", plan))
			.add(Restrictions.isNull("levelInstance.parent"))
			.setProjection(
				Projections.projectionList()
				.add(Projections.avg("levelValue"), "performance")
				.add(Projections.avg("levelMinimum"), "minimumAverage")
				.add(Projections.avg("levelMaximum"), "maximumAverage")
			)
			.setResultTransformer(PerformanceBean.class)
		;
		return (PerformanceBean) criteria.uniqueResult();
	}


	/** Preenche a lista de indicadores de um nível indicador. */
	public void fillIndicators(StructureLevelInstance levelInstance) {
		if (levelInstance != null && levelInstance.isAggregate()) {
			levelInstance.setIndicatorList(this.listIndicators(levelInstance));
		}
	}
	
	/** Listar instâncias dos indicadores agregados pela instância de um
	 *  nível de indicador. */
	public List<AggregateIndicator> listIndicators(StructureLevelInstance indicator) {
		Criteria criteria = this.dao.newCriteria(AggregateIndicator.class);
		criteria.createAlias("aggregate", "aggregate", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("aggregate.deleted", false));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("indicator", indicator));

		return this.dao.findByCriteria(criteria, AggregateIndicator.class);
	}
	
	public List<AggregateIndicator> getAggregatedToIndicators(StructureLevelInstance aggregated) {
		Criteria criteria = this.dao.newCriteria(AggregateIndicator.class);
		criteria.createAlias("indicator", "indicator", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("aggregate", aggregated));
		criteria.add(Restrictions.eq("indicator.deleted", false));
		criteria.add(Restrictions.eq("deleted", false));

		return this.dao.findByCriteria(criteria, AggregateIndicator.class);
	}
	
	/** Realiza o cálculo e atualiza o valor de nível (valor agregado)
	 * de acordo com o tipo de cálculo definido para o nível. */
	public void updateAggregatedLevelValue(StructureLevelInstance levelInstance) {
		Double total = 0.0;
		Double totalMinimum = 0.0;
		Double totalMaximum = 0.0;
		if (levelInstance.getCalculation() == CalculationType.NORMAL_AVG) {
			for (AggregateIndicator aggregates : levelInstance.getIndicatorList()) {
				if (aggregates.getAggregate().getLevelValue() != null) {
					total += aggregates.getAggregate().getLevelValue();
					totalMinimum += aggregates.getAggregate().getLevelMinimum();
					totalMaximum += aggregates.getAggregate().getLevelMaximum();
				}
			}
			if (levelInstance.getIndicatorList().size() > 0) {
				levelInstance.setLevelValue(total / levelInstance.getIndicatorList().size());
				levelInstance.setLevelMinimum(totalMinimum / levelInstance.getIndicatorList().size());
				levelInstance.setLevelMaximum(totalMaximum / levelInstance.getIndicatorList().size());
			} else {
				levelInstance.setLevelValue(total);
				levelInstance.setLevelMinimum(totalMinimum);
				levelInstance.setLevelMaximum(totalMaximum);
			}
		} else if (levelInstance.getCalculation() == CalculationType.WEIGHTED_AVG) {
			for (AggregateIndicator aggregates : levelInstance.getIndicatorList()) {
				if (aggregates.getAggregate().getLevelValue() != null) {
					total += aggregates.getAggregate().getLevelValue() * aggregates.getPercentage();
					totalMinimum += aggregates.getAggregate().getLevelMinimum() * aggregates.getPercentage();
					totalMaximum += aggregates.getAggregate().getLevelMaximum() * aggregates.getPercentage();
				}
			}
			if (levelInstance.getIndicatorList().size() > 0) {
				levelInstance.setLevelValue(total / 100);
				levelInstance.setLevelMinimum(totalMinimum / 100);
				levelInstance.setLevelMaximum(totalMaximum / 100);
			} else {
				levelInstance.setLevelValue(total);
				levelInstance.setLevelMinimum(totalMinimum);
				levelInstance.setLevelMaximum(totalMaximum);
			}
		} else if (levelInstance.getCalculation() == CalculationType.SUM) {
			for (AggregateIndicator aggregates : levelInstance.getIndicatorList()) {
				if (aggregates.getAggregate().getLevelValue() != null) {
					total += aggregates.getAggregate().getLevelValue();
					totalMinimum += aggregates.getAggregate().getLevelMinimum();
					totalMaximum += aggregates.getAggregate().getLevelMaximum();
				}
			}
			levelInstance.setLevelValue(total);
			levelInstance.setLevelMinimum(totalMinimum);
			levelInstance.setLevelMaximum(totalMaximum);
		}
		this.dao.persist(levelInstance);
	}
	
	/**
	 * Envia notificação e email relacionado a meta
	 * @param user
	 * @param domain
	 * @param goal
	 * @param baseUrl
	 * @param notificationType
	 * @throws EmailException
	*/

	public void sendGoalNotification(User user, CompanyDomain domain, StructureLevelInstance goal,
		StructureLevelInstance parent, String url, NotificationType notificationType) throws EmailException {

		if (user == null || domain == null || goal == null) {
			return;
		}
			
		CompanyUser companyUser = this.userBS.retrieveCompanyUser(user, domain.getCompany());
		String urlAux = domain.getBaseUrl() + "/" + url;
		
		if (notificationType == NotificationType.ATTRIBUTED_RESPONSIBLE 
			&& companyUser.getNotificationSetting() != NotificationSetting.DO_NOT_RECEIVE_EMAIL.getSetting()) {
			this.notificationBS.sendNotificationEmail(
				notificationType,
				goal.getName(),
				goal.getLevel().getName(),
				user,
				urlAux
			);
		} 
		
		if (notificationType != NotificationType.ATTRIBUTED_RESPONSIBLE) {
			if (companyUser.getNotificationSetting() != NotificationSetting.DO_NOT_RECEIVE_EMAIL.getSetting()) {
				this.notificationBS.sendNotificationEmail(
					notificationType,
					goal.getName(),
					parent.getName(),
					user,
					urlAux
				);
			}
			
			this.notificationBS.sendNotification(
				notificationType,
				goal.getName(),
				parent.getName(),
				user.getId(),
				urlAux
			);	
		}
	}
}
