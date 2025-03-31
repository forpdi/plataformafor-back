package org.forrisco.risk.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.ProjectionList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.utils.DateUtil;
import org.forpdi.core.utils.Util;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskState;
import org.forrisco.risk.RiskStateChecker;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class MonitorBS extends HibernateBusiness {

	@Autowired
	@Lazy
	private UnitBS unitBS;
	@Autowired
	private EntityManager entityManager;
	
	/**
	 * Salva no banco de dados um novo monitoramento
	 * 
	 * @param Monitor,
	 *            instância de um monitoramento a ser salva
	 */
	public void saveMonitor(Monitor monitor) {
		monitor.setDeleted(false);
		this.persist(monitor);
	}

	/**
	 * Deleta do banco de dados um monitoramento
	 * 
	 * @param Monitor,
	 *            instância do monitoramento ser deletado
	 */
	public void delete(Monitor monitor) {
		monitor.setDeleted(true);
		this.persist(monitor);
	}

	/**
	 * Retorna os monitoramentos a partir de um risco
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<Monitor>
	 */
	public PaginatedList<Monitor> listMonitorByRisk(Risk risk) {
		return listMonitorByRisk(risk, -1);
	}
	
	/**
	 * Retorna os monitoramentos a partir de um risco
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<Monitor>
	 */
	public PaginatedList<Monitor> listMonitorByRisk(Risk risk, Integer selectedYear) {

		PaginatedList<Monitor> results = new PaginatedList<Monitor>();

		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Monitor> cq = builder.createQuery(Monitor.class);
		Root<Monitor> root = cq.from(Monitor.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get("deleted"), false));
		predicates.add(builder.equal(root.get("risk"), risk));
		
		if (selectedYear != -1) {
			Expression<Integer> yearExpression = builder.function("year", Integer.class, root.get("begin"));
			predicates.add(builder.equal(yearExpression, selectedYear));
		}
		
		cq.where(predicates.toArray(new Predicate[] {}));

		List<Monitor> list = entityManager.createQuery(cq).getResultList();
		
		results.setList(list);	
		results.setTotal((long) list.size());
		return results;		
	}

	/**
	 * Retorna os monitoramentos a partir de um risco com paginacao
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<Monitor>
	 */
	public PaginatedList<Monitor> listMonitorByRisk(Risk risk, DefaultParams params) {

		PaginatedList<Monitor> results = new PaginatedList<Monitor>();

		Criteria criteria = this.dao.newCriteria(Monitor.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("manager", "manager", JoinType.LEFT_OUTER_JOIN)
				.add(Restrictions.eq("risk", risk));

		Criteria count = this.dao.newCriteria(Monitor.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("manager", "manager", JoinType.LEFT_OUTER_JOIN)
				.add(Restrictions.eq("risk", risk))
				.setProjection(Projections.countDistinct("id"));
		
		if (params.hasTerm()) {
			String term = params.getTerm();
			Criterion report = Restrictions.like("report", "%" + term + "%").ignoreCase();
			Criterion probability = Restrictions.like("probability", "%" + term + "%").ignoreCase();
			Criterion impact = Restrictions.like("impact", "%" + term + "%").ignoreCase();
			Criterion manager = Restrictions.like("manager.name", "%" + term + "%").ignoreCase();
			Criterion responsible = Restrictions.like("user.name", "%" + term + "%").ignoreCase();
			Criterion begin = Restrictions.like("begin", "%" + DateUtil.formatSearchDate(term) + "%").ignoreCase();

			criteria.add(Restrictions.or(report, probability, impact, manager, responsible, begin));
			count.add(Restrictions.or(report, probability, impact, manager, responsible, begin));
		}
		
		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((page-1) * pageSize)
			.setMaxResults(pageSize);

		results.setList(this.dao.findByCriteria(criteria, Monitor.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}

	/**
	 * Retorna o ultimo monitoramento de um risco
	 * 
	 * @param Risk
	 *            instância de um risco
	 *
	 * @return Monitor instância de um monitoramento
	 */
	public Monitor lastMonitorbyRisk(Long riskId) {

		Criteria criteria = this.dao.newCriteria(Monitor.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk.id", riskId)).addOrder(Order.desc("begin"));

		criteria.setMaxResults(1);
		Monitor monitor = (Monitor) criteria.uniqueResult();

		return monitor;
	}
	
	public List<MonitorHistoryBean> listMonitorHistoryByUnits(PaginatedList<Unit> units) {
		if (GeneralUtils.isEmpty(units.getList())) {
			return Collections.emptyList();
		}

		List<Long> unitIds = Util.mapEntityIds(units.getList());
		
		Criteria criteria = this.dao.newCriteria(MonitorHistory.class)
				.add(Restrictions.in("unit.id", unitIds))
				.add(Restrictions.eq("deleted", false))
				.addOrder(Order.asc("id"));

		ProjectionList projList = Projections.projectionList()
				.add(Projections.property("id"), "id")
				.add(Projections.property("unit.id"), "unitId")
				.add(Projections.property("estado"), "estado")
				.add(Projections.property("month"), "month")
				.add(Projections.property("year"), "year")
				.add(Projections.property("quantity"), "quantity");

		criteria.setProjection(projList)
			.setResultTransformer(MonitorHistoryBean.class);

		return this.dao.findByCriteria(criteria, MonitorHistoryBean.class);
	}

	public List<MonitorHistory> listLastMonitorHistoryByUnits(List<Unit> units) {
		List<MonitorHistory> list = new ArrayList<>();

		int numOfRiskStates = RiskState.values().length;
		for (Unit unit : units) {
			Criteria criteria = this.dao.newCriteria(MonitorHistory.class)
					.add(Restrictions.eq("unit", unit))
					.add(Restrictions.eq("deleted", false))
					.addOrder(Order.desc("year"))
					.addOrder(Order.desc("month"))
					.setMaxResults(numOfRiskStates);

			list.addAll(this.dao.findByCriteria(criteria, MonitorHistory.class));
		}

		return list;
	}
	
	/**
	 * Atualiza o historico de monitoramentos
	 *
	 * @param plan
	 *            Plano de Risco com os monitoramentos
	 *
	 */
	public void updateMonitorHistory(PlanRisk plan) {
		if (plan == null) {
			return;
		}

		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		int year = new Date().getYear() + 1900;
		int month = new Date().getMonth();

		for (Unit unit : unitBS.listUnitsbyPlanRisk(plan).getList()) {
			for (int i = 0; i < RiskState.values().length; i++) {
				map.put(i, 0);
			}
			Criteria criteria = this.dao.newCriteria(Risk.class)
					.add(Restrictions.eq("deleted", false))
					.add(Restrictions.eq("archived", false))
					.add(Restrictions.eq("unit", unit));


			for (Risk risk : this.dao.findByCriteria(criteria, Risk.class)) {
				Monitor monitor = this.lastMonitorbyRisk(risk.getId());
				int state = RiskStateChecker.riskState(risk.getPeriodicity(), monitor != null ? monitor.getBegin() : risk.getBegin());
				map.put(state, map.get(state) + 1);
			}

			for (int i = 0; i < map.size(); i++) {
				Integer quantity = map.get(i);
				RiskState riskState = RiskState.getById(i);
				String state = getRiskStatus(riskState);
				createOrUpdateMonitorHistory(unit, month, year, quantity, state);
			}
		}
	}

	/**
	 *
	 *
	 * Realiza a criação ou atualização do histórico de monitoramento.
	 *
	 */
	private void createOrUpdateMonitorHistory(Unit unit, int month, int year, int quantity, String state) {
		Criteria criteria = this.dao.newCriteria(MonitorHistory.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("month", month))
				.add(Restrictions.eq("year", year))
				.add(Restrictions.eq("estado", state))
				.add(Restrictions.eq("unit", unit));

		criteria.setMaxResults(1);
		MonitorHistory hmonitor = (MonitorHistory) criteria.uniqueResult();

		if (hmonitor == null) {
			hmonitor = new MonitorHistory();
		}

		hmonitor.setUnit(unit);
		hmonitor.setMonth(month);
		hmonitor.setYear(year);
		hmonitor.setQuantity(quantity);
		hmonitor.setEstado(state);

		this.persist(hmonitor);
	}

	/**
	 *
	 *
	 * Retorna o estado do risco.
	 *
	 */
	public String getRiskStatus(RiskState riskState) {
		switch (riskState) {
			case UP_TO_DATE:
				return "em dia";
			case CLOSE_TO_EXPIRE:
				return "próximo a vencer";
			case LATE:
				return "atrasado";
			default:
				throw new IllegalArgumentException("Unrecognized RiskState: " + riskState);
		}
	}

	/**
     * Retorna todos os monitoramentos não arquivados
     * 
     * @return PaginatedList<Monitor>
     */
    public PaginatedList<Monitor> listAllMonitors() {
			PaginatedList<Monitor> results = new PaginatedList<Monitor>();

			Criteria criteria = this.dao.newCriteria(Monitor.class)
							.add(Restrictions.eq("deleted", false));

			List<Monitor> monitors = this.dao.findByCriteria(criteria, Monitor.class);
			results.setList(monitors);
			results.setTotal((long) monitors.size());

			return results;
	}
}
