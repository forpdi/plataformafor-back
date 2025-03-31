package org.forrisco.risk.incident;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.ProjectionList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.utils.DateUtil;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.Risk;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IncidentBS extends HibernateBusiness {

	@Autowired
	private EntityManager entityManager;

	/**
	 * Salva no banco de dados incidente
	 * 
	 * @param Incident,
	 *            instância de um incident a ser salvo
	 */
	public void saveIncident(Incident incident) {
		incident.setDeleted(false);
		this.persist(incident);
	}

	/**
	 * Deleta do banco de dados um incidente
	 * 
	 * @param Incident,
	 *            instância do incidente a ser deletado
	 */
	public void delete(Incident incident) {
		incident.setDeleted(true);
		this.persist(incident);
	}
	
	public PaginatedList<Incident> pagitaneIncidents(List<Long> incidentsId, Integer page, Integer pageSize) {
		if (page == null || page < 1) {
			page = 1;
		}

		if (pageSize == null) {
			pageSize = 5;
		}

		PaginatedList<Incident> results = new PaginatedList<Incident>();

		Criteria criteria = this.dao.newCriteria(Incident.class);
		criteria.setFirstResult((page - 1) * pageSize);
		criteria.setMaxResults(pageSize).addOrder(Order.asc("description"));
		criteria.add(Restrictions.in("id", incidentsId));

		Criteria counting = this.dao.newCriteria(Incident.class);
		counting.add(Restrictions.in("id", incidentsId));
		counting.setProjection(Projections.countDistinct("id"));

		List<Incident> findCriteria = this.dao.findByCriteria(criteria, Incident.class);

		results.setList(findCriteria);
		results.setTotal((Long) counting.uniqueResult());

		return results;
	}

	/**
	 * Retorna os incidentes a partir de vários riscos
	 * 
	 * @param PaginatedList<Risk>
	 *            instância de um risco
	 * 
	 * @return PaginatedList<Incident>
	 */
	public PaginatedList<Incident> listIncidentsByRisk(PaginatedList<Risk> risks) {

		List<Incident> inc = new ArrayList<>();

		for (Risk risk : risks.getList()) {
			PaginatedList<Incident> incident = listIncidentsByRisk(risk);
			inc.addAll(incident.getList());
		}

		PaginatedList<Incident> incident = new PaginatedList<Incident>();

		incident.setList(inc);
		incident.setTotal((long) inc.size());

		return incident;
	}

	
	/**
	 * Retorna os incidentes a partir de um risco
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<Incident>
	 */
	public PaginatedList<Incident> listIncidentsByRisk(Risk risk) {
		return listIncidentsByRisk(risk, -1);
	}
	
	/**
	 * Retorna os incidentes a partir de um risco
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<Incident>
	 */
	public PaginatedList<Incident> listIncidentsByRisk(Risk risk, Integer selectedYear) {

		PaginatedList<Incident> results = new PaginatedList<Incident>();

		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Incident> cq = builder.createQuery(Incident.class);
		Root<Incident> root = cq.from(Incident.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get("deleted"), false));
		predicates.add(builder.equal(root.get("risk"), risk));
		
		if (selectedYear != -1) {
			Expression<Integer> yearExpression = builder.function("year", Integer.class, root.get("begin"));
			predicates.add(builder.equal(yearExpression, selectedYear));
		}
		
		cq.where(predicates.toArray(new Predicate[] {}));

		List<Incident> list = entityManager.createQuery(cq).getResultList();
		
		results.setList(list);	
		results.setTotal((long) list.size());

		return results;		
	}
	
	/**
	 * Retorna os incidentes a partir de várias unidades e uma data limite
	 * 
	 * @param PaginatedList<Unit> lista de unidades
	 * @param Date data limite
	 * 
	 * @return PaginatedList<Incident>
	 */
	public List<Incident> listIncidentsByUnitsAndDateLimit(List<Unit> units, Date dateLimit) {
		Criteria criteria = this.dao.newCriteria(Incident.class)
				.createAlias("risk", "risk")
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.ge("begin", dateLimit))
				.add(Restrictions.in("risk.unit", units))
				.add(Restrictions.eq("risk.archived", false));

		List<Incident> incidents = this.dao.findByCriteria(criteria, Incident.class);
		return incidents;
	}
	
	public List<IncidentBean> listIncidentsByPlanRisk(PlanRisk planRisk) {
		Criteria criteria = this.dao.newCriteria(Incident.class)
				.createAlias("risk", "risk")
				.createAlias("risk.unit", "unit")
				.createAlias("unit.planRisk", "planRisk")
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRisk.id", planRisk.getId()))
				.add(Restrictions.eq("risk.archived", false));

		ProjectionList projList = Projections.projectionList()
				.add(Projections.property("id"), "id")
				.add(Projections.property("risk.id"), "riskId")
				.add(Projections.property("unit.id"), "unitId")
				.add(Projections.property("description"), "description")
				.add(Projections.property("type"), "type")
				.add(Projections.property("begin"), "begin");
		
		criteria.setProjection(projList)
				.setResultTransformer(IncidentBean.class);
		
		return this.dao.findByCriteria(criteria, IncidentBean.class);
	}
	
	/**
	 * Retorna os incidentes a partir de um risco com paginacao
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<Incident>
	 */
	public PaginatedList<Incident> listIncidentsByRisk(Risk risk, DefaultParams params) {

		PaginatedList<Incident> results = new PaginatedList<Incident>();

		Criteria criteria = this.dao.newCriteria(Incident.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("manager", "manager", JoinType.LEFT_OUTER_JOIN)
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.add(Restrictions.eq("risk", risk));

		Criteria count = this.dao.newCriteria(Incident.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("manager", "manager", JoinType.LEFT_OUTER_JOIN)
				.add(Restrictions.eq("risk", risk))
				.setProjection(Projections.countDistinct("id"));
		
		if (params.hasTerm()) {
			String term = params.getTerm();
			Criterion description = Restrictions.like("description", "%" + term + "%").ignoreCase();
			Criterion action = Restrictions.like("action", "%" + term + "%").ignoreCase();
			Criterion manager = Restrictions.like("manager.name", "%" + term + "%").ignoreCase();
			Criterion responsible = Restrictions.like("user.name", "%" + term + "%").ignoreCase();
			Criterion begin = Restrictions.like("begin", "%" + DateUtil.formatSearchDate(term) + "%").ignoreCase();

			criteria.add(Restrictions.or(description, action, manager, responsible, begin));
			count.add(Restrictions.or(description, action, manager, responsible, begin));
		}
		
		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((page-1) * pageSize)
			.setMaxResults(pageSize);
		
		results.setList(this.dao.findByCriteria(criteria, Incident.class));
		results.setTotal((Long) count.uniqueResult());

		for (Incident incident : results.getList()) {

			incident.setUnitId(incident.getRisk().getUnit().getId());
		}

		return results;
	}
}
