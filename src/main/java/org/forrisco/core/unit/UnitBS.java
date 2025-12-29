package org.forrisco.core.unit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.ProjectionList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.user.User;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.policy.Policy;
import org.forrisco.core.process.Process;
import org.forrisco.core.process.ProcessBS;
import org.forrisco.core.unit.permissions.EditUnitPermission;
import org.forrisco.core.unit.permissions.ManageUnitPermission;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskHistory;
import org.forrisco.risk.RiskLevel;
import org.forrisco.risk.monitor.Monitor;
import org.jsoup.Jsoup;
import org.forpdi.core.common.Order;
import org.forpdi.core.common.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * @author Matheus Nascimento
 */
@Service
public class UnitBS extends HibernateBusiness {

	@Autowired
	private RiskBS riskBS;
	@Autowired
	private ProcessBS processBS;
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private UserSession userSession;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private UnitHistoryBS unitHistoryBS;

	/**
	 * Salvar uma nova unidade
	 *
	 * @param Unit,
	 *              instância da unidade
	 *
	 */
	public void save(Unit unit) {
		unit.setDeleted(false);
		this.persist(unit);
	}

	public PaginatedList<Unit> listSubunitByUnit(Unit unit) {
		DefaultParams params = DefaultParams.createWithMaxPageSize();
		return this.listSubunitByUnit(unit, params);
	}

	public PaginatedList<Unit> listSubunitByUnit(Unit unit, DefaultParams params) {
		PaginatedList<Unit> results = new PaginatedList<Unit>();

		Criteria criteria = this.dao.newCriteria(Unit.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("parent", unit))
				.createAlias("user", "user", JoinType.INNER_JOIN);

		Criteria count = this.dao.newCriteria(Unit.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("parent", unit))
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.setProjection(Projections.countDistinct("id"));

		if (params.hasTerm()) {
			String term = params.getTerm();
			Criterion name = Restrictions.like("name", "%" + term + "%").ignoreCase();
			Criterion abbreviation = Restrictions.like("abbreviation", "%" + term + "%").ignoreCase();
			Criterion responsible = Restrictions.like("user.name", "%" + term + "%").ignoreCase();

			criteria.add(Restrictions.or(name, abbreviation, responsible));
			count.add(Restrictions.or(name, abbreviation, responsible));
		}

		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		} else {
			criteria.addOrder(Order.asc("name"));
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setMaxResults(pageSize).setFirstResult(page * pageSize);
		criteria.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize);

		results.setList(this.dao.findByCriteria(criteria, Unit.class));
		results.setTotal((Long) count.uniqueResult());

		return results;
	}

	/**
	 * Deleta uma unidade
	 *
	 * @param Unit,
	 *              instância da unidade
	 *
	 */
	public void delete(Unit unit) {
		unit.setDeleted(true);
		this.persist(unit);
	}

	public PaginatedList<Monitor> listMonitorbyUnit(Unit unit) {
		PaginatedList<Monitor> monitors = new PaginatedList<Monitor>();
		List<Monitor> list = new ArrayList<Monitor>();
		Long total = (long) 0;

		Criteria criteria = this.dao.newCriteria(Risk.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("unit", unit));

		for (Risk risk : this.dao.findByCriteria(criteria, Risk.class)) {

			Criteria crit = this.dao.newCriteria(Monitor.class)
					.add(Restrictions.eq("deleted", false))
					.add(Restrictions.eq("risk", risk));

			Criteria count = this.dao.newCriteria(Monitor.class)
					.add(Restrictions.eq("deleted", false))
					.add(Restrictions.eq("risk", risk))
					.setProjection(Projections.countDistinct("id"));

			list.addAll(this.dao.findByCriteria(crit, Monitor.class));

			total += (Long) count.uniqueResult();

		}

		for (Monitor monitor : list) {
			monitor.setRiskId(monitor.getRisk().getId());
		}

		monitors.setList(list);
		monitors.setTotal(total);

		return monitors;
	}



//	public PaginatedList<Incident> listIncidentsbyUnit(Unit unit) {
//		PaginatedList<Incident> incidents = new PaginatedList<Incident>();
//		List<Incident> list = new ArrayList<Incident>();
//		Long total = (long) 0;
//
//		Criteria criteria = this.dao.newCriteria(Risk.class)
//				.add(Restrictions.eq("deleted", false))
//				.add(Restrictions.eq("unit", unit));
//
//		for (Risk risk : this.dao.findByCriteria(criteria, Risk.class)) {
//
//			Criteria crit = this.dao.newCriteria(Incident.class)
//					.add(Restrictions.eq("deleted", false))
//					.add(Restrictions.eq("risk", risk));
//
//			Criteria count = this.dao.newCriteria(Incident.class)
//					.add(Restrictions.eq("deleted", false))
//					.add(Restrictions.eq("risk", risk))
//					.setProjection(Projections.countDistinct("id"));
//
//			list.addAll(this.dao.findByCriteria(crit, Incident.class));
//
//			total += (Long) count.uniqueResult();
//
//		}
//
//		for (Incident incident : list) {
//			incident.setUnitId(unit.getId());
//		}
//
//		incidents.setList(list);
//		incidents.setTotal(total);
//
//		return incidents;
//	}



	/**
	 * Retorna uma unidade
	 *
	 * @param id
	 *           Id da unidade
	 */
	public Unit retrieveUnitById(Long id) {
		Criteria criteria = this.dao.newCriteria(Unit.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("id", id));

		return (Unit) criteria.uniqueResult();
	}

	public List<Unit> retrieveUnitsByIds(List<Long> idList) {
		Criteria criteria = this.dao.newCriteria(Unit.class)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.in("id", idList));

		return this.dao.findByCriteria(criteria, Unit.class);
	}

	/**
	 * Lista as unidades não deletadas.
	 * 
	 * @param page
	 *                   Número da página da lista a ser acessada.
	 * @param pageSize
	 *                   Número de resultados por página.
	 * @param planRiskId
	 * @return PaginatedList<Unit>
	 *         Lista de unidades.
	 */
	public PaginatedList<Unit> listUnits(Company company, Long planRiskId, DefaultParams params) {
		PaginatedList<Unit> results = new PaginatedList<Unit>();
		Criteria criteria = this.dao.newCriteria(Unit.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.isNull("parent"))
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("planRisk", "planRisk", JoinType.INNER_JOIN)
				.createAlias("planRisk.policy", "policy", JoinType.INNER_JOIN)
				.add(Restrictions.eq("policy.company", company));

		Criteria count = this.dao.newCriteria(Unit.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.isNull("parent"))
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("planRisk", "planRisk", JoinType.INNER_JOIN)
				.createAlias("planRisk.policy", "policy", JoinType.INNER_JOIN)
				.add(Restrictions.eq("policy.company", company))
				.setProjection(Projections.countDistinct("id"));

		if (params.hasTerm()) {
			String term = params.getTerm();
			Criterion name = Restrictions.like("name", "%" + term + "%").ignoreCase();
			Criterion abbreviation = Restrictions.like("abbreviation", "%" + term + "%").ignoreCase();
			Criterion responsible = Restrictions.like("user.name", "%" + term + "%").ignoreCase();
			Criterion planRisk = Restrictions.like("planRisk.name", "%" + term + "%").ignoreCase();

			List<Unit> units = this.dao.findByCriteria(criteria, Unit.class);

			List<Long> unitsWithSubunits = this.listUnitIdsWithSubunits(units, term);

			Criterion subunits = Restrictions.in("id", unitsWithSubunits);

			criteria.add(Restrictions.or(name, abbreviation, responsible, planRisk, subunits));
			count.add(Restrictions.or(name, abbreviation, responsible, planRisk, subunits));
		}

		if (planRiskId != null) {
			criteria.add(Restrictions.eq("planRisk.id", planRiskId));
			count.add(Restrictions.eq("planRisk.id", planRiskId));
		}

		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		} else {
			criteria.addOrder(Order.asc("name"));
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize);

		results.setList(this.dao.findByCriteria(criteria, Unit.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}

	public PaginatedList<Unit> listUnitsbyPlanRisk(PlanRisk planrisk, DefaultParams params) {
		PaginatedList<Unit> results = new PaginatedList<>();
		CriteriaBuilder cb = em.getCriteriaBuilder();

		List<Unit> baseUnits = Collections.emptyList();
		if (params.hasTerm()) {
			CriteriaQuery<Unit> baseCQ = cb.createQuery(Unit.class);
			Root<Unit> baseRoot = baseCQ.from(Unit.class);
			baseCQ.select(baseRoot).where(this.getPredicates(cb, baseRoot, planrisk, params, null));
			baseUnits = em.createQuery(baseCQ).getResultList();
		}

		CriteriaQuery<Unit> listCQ = cb.createQuery(Unit.class);
		Root<Unit> listRoot = listCQ.from(Unit.class);
		listCQ.select(listRoot)
				.where(this.getPredicates(cb, listRoot, planrisk, params, baseUnits));

		this.applySorting(cb, listCQ, listRoot, params);

		TypedQuery<Unit> listQuery = em.createQuery(listCQ);
		listQuery.setFirstResult((params.getPage() - 1) * params.getPageSize());
		listQuery.setMaxResults(params.getPageSize());
		List<Unit> unitList = listQuery.getResultList();

		CriteriaQuery<Long> countCQ = cb.createQuery(Long.class);
		Root<Unit> countRoot = countCQ.from(Unit.class);
		countCQ.select(cb.countDistinct(countRoot.get("id")))
				.where(this.getPredicates(cb, countRoot, planrisk, params, baseUnits));
		Long total = em.createQuery(countCQ).getSingleResult();

		results.setList(unitList);
		results.setTotal(total);
		return results;
	}

	/**
	 * Constrói e retorna a lista de predicados (filtros) para a consulta.
	 * Centraliza a lógica de filtragem para evitar duplicação.
	 */
	private jakarta.persistence.criteria.Predicate[] getPredicates(CriteriaBuilder cb, Root<Unit> root,
			PlanRisk planrisk, DefaultParams params, List<Unit> baseUnits) {
		Join<Unit, User> userJoin = root.join("user", jakarta.persistence.criteria.JoinType.INNER);
		List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

		predicates.add(cb.equal(root.get("deleted"), false));
		predicates.add(cb.isNull(root.get("parent")));
		predicates.add(cb.equal(root.get("planRisk"), planrisk));

		if (params.hasTerm()) {
			String termWildcard = "%" + params.getTerm().toLowerCase() + "%";
			List<Long> unitsWithSubunits = (baseUnits != null)
					? this.listUnitIdsWithSubunits(baseUnits, params.getTerm())
					: Collections.emptyList();

			predicates.add(cb.or(
					cb.like(cb.lower(root.get("name")), termWildcard),
					cb.like(cb.lower(root.get("abbreviation")), termWildcard),
					cb.like(cb.lower(userJoin.get("name")), termWildcard),
					root.get("id").in(unitsWithSubunits)));
		}
		return predicates.toArray(new jakarta.persistence.criteria.Predicate[0]);
	}

	/**
	 * Aplica a lógica de ordenação na consulta.
	 * Reduz a complexidade eliminando a aninhamento de ifs.
	 */
	private void applySorting(CriteriaBuilder cb, CriteriaQuery<Unit> query, Root<Unit> root, DefaultParams params) {
		if (params.isSorting()) {
			Order sortOrder = params.getSortOrder();
			String property = sortOrder.getPropertyName();

			Expression<?> path = sortOrder.isIgnoreCase() ? cb.lower(root.get(property)) : root.get(property);
			jakarta.persistence.criteria.Order order = sortOrder.isAscending() ? cb.asc(path) : cb.desc(path);
			query.orderBy(order);
		} else {
			query.orderBy(cb.asc(root.get("name")));
		}
	}

	/**
	 * Recuperar unidades e subunidades de um plano
	 *
	 * @param PlanRisk,
	 *                  instância da plano de risco
	 *
	 */
	public PaginatedList<Unit> listUnitsbyPlanRisk(PlanRisk planrisk) {
		PaginatedList<Unit> results = new PaginatedList<>();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Unit> cq = cb.createQuery(Unit.class);
		Root<Unit> root = cq.from(Unit.class);

		cq.select(root)
				.where(
						cb.equal(root.get("deleted"), false),
						cb.equal(root.get("planRisk"), planrisk));

		List<Unit> list = em.createQuery(cq).getResultList();

		CriteriaQuery<Long> countCQ = cb.createQuery(Long.class);
		Root<Unit> countRoot = countCQ.from(Unit.class);

		countCQ.select(cb.countDistinct(countRoot.get("id")))
				.where(
						cb.equal(countRoot.get("deleted"), false),
						cb.equal(countRoot.get("planRisk"), planrisk));
		Long total = em.createQuery(countCQ).getSingleResult();

		results.setList(list);
		results.setTotal(total);
		return results;
	}

	/**
	 * Recuperar unidades de um plano
	 *
	 * @param PlanRisk,
	 *                  instância da plano de risco
	 *
	 */
	public PaginatedList<Unit> listOnlyUnitsbyPlanRisk(PlanRisk planrisk) {
		PaginatedList<Unit> results = new PaginatedList<>();
	
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Unit> cq = cb.createQuery(Unit.class);
		Root<Unit> root = cq.from(Unit.class);
	
		Predicate notDeleted = cb.equal(root.get("deleted"), false);
		Predicate parentIsNull = cb.isNull(root.get("parent"));
		Predicate byPlanRisk = cb.equal(root.get("planRisk"), planrisk);
	
		cq.select(root)
		  .where(cb.and(notDeleted, parentIsNull, byPlanRisk))
		  .orderBy(cb.asc(root.get("id")));
		List<Unit> units = em.createQuery(cq).getResultList();
	
		results.setList(units);
		results.setTotal((long) units.size());
		return results;
	}

	/**
	 * Atualiza o historico de riscos
	 *
	 * @param plan
	 *             Plano de Risco
	 *
	 */
	public void updateHistory(PlanRisk plan, boolean threat) {
		if (plan == null) {
			return;
		}

		Map<RiskLevel, Integer> map = new HashMap<RiskLevel, Integer>();
		int year = new Date().getYear() + 1900;
		int month = new Date().getMonth() + 1;

		PaginatedList<Unit> units = this.listUnitsbyPlanRisk(plan);
		for (Unit unit : units.getList()) {
			map.clear();

			Criteria criteria = this.dao.newCriteria(Risk.class)
					.add(Restrictions.eq("deleted", false))
					.add(Restrictions.eq("unit", unit))
					.add(Restrictions.eq("type", threat ? "Ameaça" : "Oportunidade"));

			List<Risk> risks = this.dao.findByCriteria(criteria, Risk.class);

			for (Risk risk : risks) {
				RiskLevel level = risk.getRiskLevel();

				if (!map.containsKey(level)) {
					map.put(level, 0);
				}
				map.put(level, map.get(level) + 1);
			}

			for (RiskLevel level : map.keySet()) {
				criteria = this.dao.newCriteria(RiskHistory.class)
						.add(Restrictions.eq("deleted", false))
						.add(Restrictions.eq("month", month))
						.add(Restrictions.eq("year", year))
						.add(Restrictions.eq("riskLevel", level))
						.add(Restrictions.eq("unit", unit))
						.add(Restrictions.eq("threat", threat));

				criteria.setMaxResults(1);
				RiskHistory riskhistory = (RiskHistory) criteria.uniqueResult();

				if (riskhistory == null) {
					riskhistory = new RiskHistory();
					riskhistory.setUnit(unit);
					riskhistory.setMonth(month);
					riskhistory.setYear(year);
					riskhistory.setRiskLevel(level);
					riskhistory.setThreat(threat);
				}

				riskhistory.setQuantity(map.get(level));

				this.persist(riskhistory);
			}
		}
	}

	/**
	 * Verifica se uma unidade é deletável.
	 *
	 * @param Unit
	 *             unidade a ser verificada.
	 */
	public boolean deletableUnit(Unit unit) {

		// verifica se possui subunidades vinculadas
		if (unit.getParent() == null) {
			PaginatedList<Unit> subunits = this.listSubunitByUnit(unit);
			if (subunits.getTotal() > 0) {
				return false;
			}
		}

		// verifica se possui riscos vinculados
		PaginatedList<Risk> risks = this.riskBS.listRiskByUnit(unit);
		if (risks.getTotal() > 0) {
			return false;
		}

		// verifica se possui processos vinculados com algum risco de outra unidade?
		// um processo pode estar vinculado a um risco de outra unidade? aparentemente
		// sim
		PaginatedList<Process> processes = this.processBS.listProcessByUnit(unit);
		for (Process process : processes.getList()) {

			if (this.riskBS.hasLinkedRiskActivity(process) && process.getUnitCreator().getId().equals(unit.getId())) {
				return false;
			}
			if (this.riskBS.hasLinkedRiskObjectiveProcess(process)
					&& process.getUnitCreator().getId().equals(unit.getId())) {
				return false;
			}
		}

		return true;
	}

	public List<UnitToSelect> listToSelect(Long planRiskId) {

		ProjectionList projList = Projections.projectionList()
				.add(Projections.property("id"), "id")
				.add(Projections.property("name"), "name")
				.add(Projections.property("abbreviation"), "abbreviation")
				.add(Projections.property("parent.id"), "parentId");

		Criteria criteria = this.dao.newCriteria(Unit.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("planRisk", "planRisk", JoinType.INNER_JOIN)
				.createAlias("planRisk.policy", "policy", JoinType.INNER_JOIN)
				.createAlias("policy.company", "company", JoinType.INNER_JOIN)
				.add(Restrictions.eq("company.id", domain.get().getCompany().getId()));

		if (planRiskId != null) {
			criteria.add(Restrictions.eq("planRisk.id", planRiskId));
		}

		criteria.setProjection(projList)
				.setResultTransformer(UnitToSelect.class);

		return dao.findByCriteria(criteria, UnitToSelect.class);
	}

	public Unit updateUnit(Unit existentUnit, Unit unitWithNewData) {
			User newUser = this.exists(unitWithNewData.getUser().getId(), User.class);
			if (newUser == null) {
				throw new IllegalArgumentException("O novo usuário responsável não foi encontrado.");
			}
			
			PlanRisk newPlanRisk = this.exists(unitWithNewData.getPlanRisk().getId(), PlanRisk.class);
			if (newPlanRisk == null) {
				throw new IllegalArgumentException("O novo Plano de Risco não foi encontrado.");
			}

			boolean hasPermission = this.hasPermissionToEdit(userSession);
			boolean isResponsible = existentUnit.getUser().getId().equals(userSession.getUser().getId());
			if (!hasPermission && !isResponsible) {
				throw new IllegalStateException("O usuário não tem permissão para realizar esta edição.");
			}

			unitHistoryBS.saveUnitChanges(existentUnit, unitWithNewData, newUser, newPlanRisk, userSession.getUser());

			existentUnit.setName(unitWithNewData.getName());
			existentUnit.setAbbreviation(unitWithNewData.getAbbreviation());
			existentUnit.setDescription(unitWithNewData.getDescription());
			if (hasPermission) {
				existentUnit.setUser(newUser);
			}
		
			List<Unit> subunitsToUpdate = existentUnit.isSubunit()
					? Collections.emptyList()
					: updateUnitAndSubunitsPlanRisk(existentUnit, newPlanRisk);
			this.dao.execute(dao -> {
				dao.persist(existentUnit);
				for (Unit subunit : subunitsToUpdate) {
					dao.persist(subunit);
				}
			});

			return existentUnit;
		}


    private List<Unit> updateUnitAndSubunitsPlanRisk(Unit existent, PlanRisk newPlanRisk) {
		if (existent.isSubunit()) {
			throw new IllegalStateException("Não é possível alterar o plano de risco de uma subunidade.");
		}

		List<Unit> subunitsToUpdate;
		if (!existent.getPlanRisk().getId().equals(newPlanRisk.getId())) {
			Policy existentPolicy = existent.getPlanRisk().getPolicy();
			Policy newPolicy = newPlanRisk.getPolicy();
			if (!existentPolicy.getId().equals(newPolicy.getId())) {
				throw new IllegalStateException("O novo plano de risco deve ter a mesma política.");
			}
			existent.setPlanRisk(newPlanRisk);
			subunitsToUpdate = this.listSubunitByUnit(existent).getList();
			for (Unit subunit : subunitsToUpdate) {
				subunit.setPlanRisk(newPlanRisk);
			}
		} else {
			subunitsToUpdate = Collections.emptyList();
		}

		return subunitsToUpdate;
	}

	/**
	 * Retorna se o usuário tem ou não a permissao de editar uma unidade
	 * 
	 * @param userSession
	 *                    sessão do usuario
	 *
	 * @return boolean se o usuário tem permissão de editar a unidade
	 */
	public boolean hasPermissionToEdit(UserSession userSession) {
		return userSession.getAccessLevel() >= AccessLevels.COMPANY_ADMIN.getLevel() ||
				userSession.getPermissions().contains(ManageUnitPermission.class.getCanonicalName()) ||
				userSession.getPermissions().contains(EditUnitPermission.class.getCanonicalName());
	}

	public List<Long> listUnitIdsWithSubunits(List<Unit> units, String term) {
		List<Long> unitsWithSubunits = new ArrayList<>();

		for (Unit unit : units) {
			DefaultParams params = DefaultParams.createWithMaxPageSize();
			params.setTerm(term);
			List<Unit> curSubunits = this.listSubunitByUnit(unit, params).getList();
			curSubunits.forEach((subunit) -> unitsWithSubunits.add(subunit.getParent().getId()));
		}

		return unitsWithSubunits;
	}
}