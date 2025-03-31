package org.forrisco.core.unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	/**
	 * Salvar uma nova unidade
	 *
	 * @param Unit,
	 *            instância da unidade
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
		criteria.setFirstResult((page-1) * pageSize)
			.setMaxResults(pageSize);

		results.setList(this.dao.findByCriteria(criteria, Unit.class));
		results.setTotal((Long) count.uniqueResult());

		return results;
	}


	/**
	 * Deleta uma unidade
	 *
	 * @param Unit,
	 *            instância da unidade
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
	 * 		Id da unidade
	 */
	public Unit retrieveUnitById(Long id) {
		Criteria criteria = this.dao.newCriteria(Unit.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("id", id));

		return (Unit) criteria.uniqueResult();
	}

	/**
	 * Lista as unidades não deletadas.
	 * @param page
	 * 			Número da página da lista a ser acessada.
	 * @param pageSize
	 * 			Número de resultados por página.
	 * @param planRiskId 
	 * @return PaginatedList<Unit>
	 * 			Lista de unidades.
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
		criteria.setFirstResult((page-1) * pageSize)
			.setMaxResults(pageSize);

		results.setList(this.dao.findByCriteria(criteria, Unit.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}

	/**
	 * Recuperar unidades e subunidades de um plano
	 *
	 * @param PlanRisk,
	 *            instância da plano de risco
	 *
	 */
	public PaginatedList<Unit> listUnitsbyPlanRisk(PlanRisk planrisk, DefaultParams params) {
		PaginatedList<Unit> results = new PaginatedList<Unit>();

		Criteria criteria = this.dao.newCriteria(Unit.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.add(Restrictions.isNull("parent"))
				.add(Restrictions.eq("planRisk", planrisk));

		Criteria count = this.dao.newCriteria(Unit.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.add(Restrictions.eq("planRisk", planrisk))
				.add(Restrictions.isNull("parent"))
				.addOrder(Order.asc("name"))
				.setProjection(Projections.countDistinct("id"));
		
		if (params.hasTerm()) {
			String term = params.getTerm();
			Criterion name = Restrictions.like("name", "%" + term + "%").ignoreCase();
			Criterion abbreviation = Restrictions.like("abbreviation", "%" + term + "%").ignoreCase();
			Criterion responsible = Restrictions.like("user.name", "%" + term + "%").ignoreCase();

			List<Unit> units = this.dao.findByCriteria(criteria, Unit.class);
			
			List<Long> unitsWithSubunits = this.listUnitIdsWithSubunits(units, term);
			
			Criterion subunits = Restrictions.in("id", unitsWithSubunits);

			criteria.add(Restrictions.or(name, abbreviation, responsible, subunits));
			count.add(Restrictions.or(name, abbreviation, responsible, subunits));
		}
		
		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		} else {
			criteria.addOrder(Order.asc("name"));
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((page-1) * pageSize)
			.setMaxResults(pageSize);

		results.setList(this.dao.findByCriteria(criteria, Unit.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	/**
	 * Recuperar unidades e subunidades de um plano
	 *
	 * @param PlanRisk,
	 *            instância da plano de risco
	 *
	 */
	public PaginatedList<Unit> listUnitsbyPlanRisk(PlanRisk planrisk) {
		PaginatedList<Unit> results = new PaginatedList<Unit>();

		Criteria criteria = this.dao.newCriteria(Unit.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRisk", planrisk));


		Criteria count = this.dao.newCriteria(Unit.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRisk", planrisk))
				.setProjection(Projections.countDistinct("id"));

		results.setList(this.dao.findByCriteria(criteria, Unit.class));
		results.setTotal((Long) count.uniqueResult());

		return results;
	}

	/**
	 * Recuperar unidades de um plano
	 *
	 * @param PlanRisk,
	 *            instância da plano de risco
	 *
	 */
	public PaginatedList<Unit> listOnlyUnitsbyPlanRisk(PlanRisk planrisk) {
		PaginatedList<Unit> results = new PaginatedList<Unit>();

		Criteria criteria = this.dao.newCriteria(Unit.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.isNull("parent"))
				.add(Restrictions.eq("planRisk", planrisk))
				.addOrder(Order.asc("id"));

		List<Unit> units = this.dao.findByCriteria(criteria, Unit.class);
		results.setList(units);
		results.setTotal((long) units.size());

		return results;
	}



	/**
	 * Atualiza o historico de riscos
	 *
	 * @param plan
	 *            Plano de Risco
	 *
	 */
	public void updateHistory(PlanRisk plan, boolean threat) {
		if (plan == null) {
			return;
		}

		Map<RiskLevel, Integer> map = new HashMap<RiskLevel, Integer>();
		int year = new Date().getYear() + 1900;
		int month = new Date().getMonth()+1;


		PaginatedList<Unit> units = this.listUnitsbyPlanRisk(plan);
		for (Unit unit : units.getList()) {
			map.clear();

			Criteria criteria = this.dao.newCriteria(Risk.class)
					.add(Restrictions.eq("deleted", false))
					.add(Restrictions.eq("unit", unit))
					.add(Restrictions.eq("type", threat?"Ameaça":"Oportunidade"));

			List<Risk> risks = this.dao.findByCriteria(criteria, Risk.class);

			for (Risk risk : risks) {
				RiskLevel level =  risk.getRiskLevel();

				if(!map.containsKey(level)) {
					map.put(level,0);
				}
				map.put(level, map.get(level) + 1);
			}

			for ( RiskLevel level : map.keySet() ) {
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
	 * 			unidade a ser verificada.
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

		//verifica se possui processos vinculados com algum risco de outra unidade?
		//um processo pode estar vinculado a um risco de outra unidade? aparentemente sim
		PaginatedList<Process> processes = this.processBS.listProcessByUnit(unit);
		for(Process process :processes.getList()) {

			if (this.riskBS.hasLinkedRiskActivity(process) && process.getUnitCreator().getId().equals(unit.getId())) {
				return false;
			}
			if (this.riskBS.hasLinkedRiskObjectiveProcess(process) && process.getUnitCreator().getId().equals(unit.getId())) {
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
 	
 	public Unit updateUnit(Unit unit) {
		Unit existent = this.exists(unit.getId(), Unit.class);
		if (existent == null) {
			throw new IllegalArgumentException("A unidade não foi encontrada.");
		}

		User user = this.riskBS.exists(unit.getUser().getId(), User.class);
		if (user == null) {
			throw new IllegalArgumentException("O usuário responsável não foi encontrado.");
		}
		
		boolean hasPermission = this.hasPermissionToEdit(userSession);
		boolean isResponsible = unit.getUser().getId().equals(userSession.getUser().getId());
		
		if (!hasPermission && !isResponsible) {
			throw new IllegalStateException("O usuário não tem permissão para realizar esta edição.");
		}
		
		if (hasPermission) {
		    existent.setUser(user);
		}

		existent.setName(unit.getName());
		existent.setAbbreviation(unit.getAbbreviation());
		existent.setDescription(unit.getDescription());

		List<Unit> subunitsToUpdate = existent.isSubunit()
				? Collections.emptyList()
				: updateUnitAndSubunitsPlanRisk(existent, unit.getPlanRisk().getId());
		
		this.dao.execute(dao -> {
			dao.persist(existent);
			for (Unit subunit : subunitsToUpdate) {
				dao.persist(subunit);
			}
		});

		this.persist(existent);
		
		return existent;
 	}

	private List<Unit> updateUnitAndSubunitsPlanRisk(Unit existent, Long newPlanRiskId) {
		if (existent.isSubunit()) {
			throw new IllegalStateException("Não é possível alterar o plano de risco de uma subunidade.");
		}

		PlanRisk newPlanRisk = this.exists(newPlanRiskId, PlanRisk.class);
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
     *            sessão do usuario     
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