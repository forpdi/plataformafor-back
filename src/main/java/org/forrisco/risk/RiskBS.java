package org.forrisco.risk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.mail.EmailException;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.ProjectionList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.notification.NotificationBS;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.user.User;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.policy.Policy;
import org.forrisco.core.process.Process;
import org.forrisco.core.process.ProcessBS;
import org.forrisco.core.process.ProcessObjective;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.links.RiskActivity;
import org.forrisco.risk.links.RiskAxis;
import org.forrisco.risk.links.RiskGoal;
import org.forrisco.risk.links.RiskIndicator;
import org.forrisco.risk.links.RiskProcessObjective;
import org.forrisco.risk.links.RiskStrategy;
import org.forrisco.risk.links.RiskStrategyBean;
import org.forrisco.risk.monitor.Monitor;
import org.forrisco.risk.monitor.MonitorBS;
import org.forrisco.risk.permissions.ManageRiskItemsPermission;
import org.forrisco.risk.permissions.ManageRiskPermission;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author Matheus Nascimento
 */
@Service
public class RiskBS extends HibernateBusiness {

	@Autowired
	private CompanyDomainContext domain;
	
	@Autowired
	private NotificationBS notificationBS;

	@Autowired
	private UserSession userSession;
	
	@Autowired
	@Lazy
	private UnitBS unitBS;
	
	@Autowired
	private MonitorBS monitorBS;
	
	@Autowired
	private ProcessBS processBS;

	@Autowired
	private RiskFilterBS filterService;

	
	private static final int PAGESIZE = 10;

	
	public void createNewRisk(Risk risk) {
		RiskLevel riskLevel = this.getRiskLevelByRisk(risk, null);
		
		this.validateRisk(risk, riskLevel);
		
		risk.setRiskLevel(riskLevel);
		
		risk.setId(null);
		risk.setBegin(new Date());
		risk.setReason(SanitizeUtil.sanitize(risk.getReason()));
		risk.setResult(SanitizeUtil.sanitize(risk.getResult()));
		this.saveRisk(risk);
		
		this.saveActivities(risk);
		this.saveRiskProcessObjective(risk);
		this.saveStrategies(risk);
		this.saveAxes(risk);
		this.saveIndicators(risk);
		this.saveGoals(risk);
		this.saveResponse(risk);
	}
	
	/**
	 * Salva no banco de dados um novo risco
	 * 
	 * @param Risk,
	 *            instância de um risco a ser salvo
	 */
	public void saveRisk(Risk risk) {
		risk.setDeleted(false);
		this.persist(risk);
	}

	public void validateRisk(Risk risk, RiskLevel riskLevel) {
		Unit unit = this.exists(risk.getUnit().getId(), Unit.class);
		if (unit == null) {
			throw new IllegalArgumentException("A unidade solicitada não foi encontrada.");
		}
		
		User user = this.exists(risk.getUser().getId(), User.class);
		if (user == null) {
			throw new IllegalArgumentException("Responsável técnico solicitado não foi encontrado.");
		}

		User manager = this.exists(risk.getManager().getId(), User.class);
		try {
			this.validateManager(manager);
		} catch (IllegalStateException ex) {
			throw ex;
		}

		if (riskLevel == null) {
			throw new IllegalArgumentException("Grau de Risco solicitado não foi encontrado.");
		}
	}
	
	public void validateManager(User user) {
		if (user == null) {
			throw new IllegalStateException("Gestor solicitado não foi encontrado.");
		}
	}

	/**
	 * Salva no banco de dados graus de risco
	 * 
	 * @param policy,
	 *            instância da política a ser salva
	 */
	public void saveRiskLevel(Policy policy) {

		String[][] str = policy.getRisk_level();

		for (int i = 0; i < str[0].length; i++) {
			if (str[1][i] != null) {
				RiskLevel rk = new RiskLevel();
				rk.setId(null);
				rk.setColor(Integer.parseInt(str[1][i]));
				rk.setLevel(str[0][i]);
				rk.setPolicy(policy);
				this.persist(rk);
			}
		}
	}

	/**
	 * Salva no banco de dados graus de risco
	 * 
	 * @param RiskLevel,
	 *            instância de um grau de risco a ser salvo
	 */
	public void saveRiskLevel(RiskLevel risklevel) {
		risklevel.setDeleted(false);
		this.persist(risklevel);
	}

	/**
	 * Salvar uma lista de atividades
	 * 
	 * @param PaginatedList<RiskActivity>
	 *            instância de uma lista de atividades de risco
	 */
	public void saveActivities(Risk risk) {

		if (risk.getActivities().getList() == null) {
			return;
		}

		for (RiskActivity activity : risk.getActivities().getList()) {
			activity.setId(null);
			activity.setRisk(risk);
			activity.setDeleted(false);

			Process process = this.dao.exists(activity.getProcess().getId(), Process.class);

			activity.setProcess(process);

			if (activity.getName() == null) {
				activity.setName("");
			}

			// pegar link correto da unidade que contem o processo
			if (process != null && !process.isDeleted() && process.getUnitCreator() != null) {
				activity.setLinkFPDI("#/forrisco/plan-risk/" + process.getUnitCreator().getPlanRisk().getId() + "/unit/"
						+ process.getUnitCreator().getId() + "/info");
			}
			this.dao.persist(activity);
		}
	}

	
	/**
	 * Salvar um novo riskProcessObjective
	 * 
	 * @param Risk,
	 *			instância do risk
	 *            
	 */
	public void saveRiskProcessObjective(Risk risk) {
		
		if (risk.getProcessObjectives() == null) {
			return;
		}
		
		for (RiskProcessObjective processObj : risk.getProcessObjectives()) {
			ProcessObjective existentObj = processBS.retrieveProcessObjectiveById(processObj.getId());
			RiskProcessObjective riskProcessObj = new RiskProcessObjective();
			riskProcessObj.setRisk(risk);
			riskProcessObj.setProcessObjective(existentObj);
			riskProcessObj.setLinkFPDI("#/forrisco/plan-risk/" + existentObj.getProcess().getUnitCreator().getPlanRisk().getId() + "/unit/"
						+ existentObj.getProcess().getUnitCreator().getId() + "/info");
			riskProcessObj.setProcessId(processObj.getId());
			this.persist(riskProcessObj);
		}
	}

	/**
	 * Salvar uma lista de objetivos estratégicos
	 * 
	 * @param PaginatedList<RiskStrategy>
	 *            instância de uma lista de objetivos estratégicos
	 * @throws Exception
	 */
	public void saveStrategies(Risk risk) {

		if (risk.getStrategies().getList() == null) {
			return;
		}

		for (RiskStrategy strategy : risk.getStrategies().getList()) {
			strategy.setId(null);
			strategy.setRisk(risk);
			strategy.setDeleted(false);

			StructureLevelInstance structure = this.dao.exists(strategy.getStructure().getId(),
					StructureLevelInstance.class);
			if (structure == null) {
				throw new IllegalArgumentException("Objetivo do PDI não foi encontrado");
			}

			Plan plan = this.dao.exists(structure.getPlan().getId(), Plan.class);

			if (plan == null) {
				throw new IllegalArgumentException("Plano do PDI não foi encontrado");
			}

			// pegar link correto do Objetivo na Plataforma ForPDI.
			strategy.setLinkFPDI(
					"#/plan/" + plan.getParent().getId() + "/details/subplan/level/" + strategy.getStructure().getId());
			strategy.setName(structure.getName());
			strategy.setStructure(structure);
			this.dao.persist(strategy);
		}
	}
	
	/**
	 * Salvar uma lista de eixos tematicos
	 * 
	 * @param PaginatedList<RiskAxis>
	 *            instância de uma lista de eixos tematicoss
	 * @throws Exception
	 */
	public void saveAxes(Risk risk) {

		if (risk.getAxes().getList() == null) {
			return;
		}

		for (RiskAxis axis : risk.getAxes().getList()) {
			axis.setId(null);
			axis.setRisk(risk);
			axis.setDeleted(false);

			StructureLevelInstance structure = this.dao.exists(axis.getStructure().getId(),
					StructureLevelInstance.class);
			if (structure == null) {
				throw new IllegalArgumentException("Eixo do PDI não foi encontrado");
			}

			Plan plan = this.dao.exists(structure.getPlan().getId(), Plan.class);

			if (plan == null) {
				throw new IllegalArgumentException("Plano do PDI não foi encontrado");
			}

			axis.setLinkFPDI(
					"#/plan/" + plan.getParent().getId() + "/details/subplan/level/" + axis.getStructure().getId());
			axis.setName(structure.getName());
			axis.setStructure(structure);
			this.dao.persist(axis);
		}
	}
	
	/**
	 * Salvar uma lista de indicadores
	 * 
	 * @param PaginatedList<RiskIndicator>
	 *            instância de uma lista de indicadores
	 * @throws Exception
	 */
	public void saveIndicators(Risk risk) {

		if (risk.getIndicators().getList() == null) {
			return;
		}

		for (RiskIndicator indicator : risk.getIndicators().getList()) {
			indicator.setId(null);
			indicator.setRisk(risk);
			indicator.setDeleted(false);

			StructureLevelInstance structure = this.dao.exists(indicator.getStructure().getId(),
					StructureLevelInstance.class);
			if (structure == null) {
				throw new IllegalArgumentException("Eixo do PDI não foi encontrado");
			}

			Plan plan = this.dao.exists(structure.getPlan().getId(), Plan.class);

			if (plan == null) {
				throw new IllegalArgumentException("Plano do PDI não foi encontrado");
			}

			indicator.setLinkFPDI(
					"#/plan/" + plan.getParent().getId() + "/details/subplan/level/" + indicator.getStructure().getId());
			indicator.setName(structure.getName());
			indicator.setStructure(structure);
			this.dao.persist(indicator);
		}
	}

	/**
	 * Salvar uma lista de metas
	 * 
	 * @param PaginatedList<RiskGoal>
	 *            instância de uma lista de indicadores
	 * @throws Exception
	 */
	public void saveGoals(Risk risk) {

		if (risk.getGoals().getList() == null) {
			return;
		}

		for (RiskGoal goal : risk.getGoals().getList()) {
			goal.setId(null);
			goal.setRisk(risk);
			goal.setDeleted(false);

			StructureLevelInstance structure = this.dao.exists(goal.getStructure().getId(),
					StructureLevelInstance.class);
			if (structure == null) {
				throw new IllegalArgumentException("Eixo do PDI não foi encontrado");
			}

			Plan plan = this.dao.exists(structure.getPlan().getId(), Plan.class);

			if (plan == null) {
				throw new IllegalArgumentException("Plano do PDI não foi encontrado");
			}

			goal.setLinkFPDI(
					"#/plan/" + plan.getParent().getId() + "/details/subplan/level/" + goal.getStructure().getId());
			goal.setName(structure.getName());
			goal.setStructure(structure);
			this.dao.persist(goal);
		}
	}
	
	public void saveResponse(Risk risk) {
		if (risk.getResponse() != null) {
			RiskResponse riskResponse = RiskResponse.GetById(risk.getResponse()); 
			
			if (riskResponse.equals(RiskResponse.SHARE)) {
				for (Unit sharedUnit : risk.getSharedUnits()) {
					Unit existentUnit = unitBS.retrieveUnitById(sharedUnit.getId());
					RiskSharedUnit riskSharedUnit = new RiskSharedUnit();
					riskSharedUnit.setRisk(risk);
					riskSharedUnit.setUnit(existentUnit);
					this.persist(riskSharedUnit);
				}
			}
		}
	}

	/**
	 * Deleta do banco de dados graus de risco
	 * 
	 * @param Item,
	 *            instância do item a ser deletado
	 */
	public void delete(RiskLevel risklevel) {
		risklevel.setDeleted(true);
		this.persist(risklevel);
	}

	/**
	 * Deleta do banco de dados uma atividade do processo
	 * 
	 * @param RiskActivity,
	 *            instância da atividade do processo
	 */
	private void delete(RiskActivity act) {
		act.setDeleted(true);
		this.persist(act);
	}

	/**
	 * Deleta do banco de dados um objetivo estratégico
	 * 
	 * @param RiskStrategy,
	 *            instância do objetivo
	 */
	private void delete(RiskStrategy str) {
		str.setDeleted(true);
		this.persist(str);
	}
	
	/**
	 * Deleta do banco de dados um eixo temático
	 * 
	 * @param RiskAxis,
	 *            instância do eixo
	 */
	private void delete(RiskAxis axi) {
		axi.setDeleted(true);
		this.persist(axi);
	}

	/**
	 * Deleta do banco de dados as atividades, os processos, os objetivos e os eixos
	 * estratégicos de um risco
	 * 
	 * @param Risk,
	 *            instância do risco
	 */
	public void deleteAPS(Risk oldrisk) {

		Risk risk = this.dao.exists(oldrisk.getId(), Risk.class);
		if (risk == null) {
			return;
		}

		PaginatedList<RiskActivity> activity = this.listRiskActivity(risk);
		PaginatedList<RiskStrategy> strategy = this.listRiskStrategy(risk);
		PaginatedList<RiskAxis> axis = this.listRiskAxis(risk);
		PaginatedList<RiskIndicator> indicators = this.listRiskIndicators(risk);
		PaginatedList<RiskGoal> goals = this.listRiskGoals(risk);
		List<RiskProcessObjective> riskProcessObjectives = this.listRiskProcessObjectives(risk);
		List<RiskSharedUnit> riskSharedUnits = listRiskSharedUnits(risk, true);
		
		for (RiskActivity act : activity.getList()) {
			this.delete(act);
		}

		for (RiskStrategy str : strategy.getList()) {
			this.delete(str);
		}
		
		for (RiskAxis axi : axis.getList()) {
			this.delete(axi);
		}
		
		for (RiskIndicator ind : indicators.getList()) {
			dao.delete(ind);
		}
		
		for (RiskGoal goal : goals.getList()) {
			dao.delete(goal);
		}
		
		for (RiskSharedUnit rsu : riskSharedUnits) {
			dao.delete(rsu);
		}
		
		for (RiskProcessObjective rpo : riskProcessObjectives) {
			dao.delete(rpo);
		}
	}

	/**
	 * Deleta do banco de dados um risco,
	 * 
	 * @param Risk,
	 *            instância do risco
	 */
	public void delete(Risk risk) {
		risk.setDeleted(true);
		this.persist(risk);
	}

	/**
	 * Retorna um risco
	 * 
	 * @param id
	 *            Id do risco
	 */
	public Risk listRiskById(Long id) {
		Criteria criteria = this.dao.newCriteria(Risk.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("id", id));
		criteria.setMaxResults(1);

		return (Risk) criteria.uniqueResult();
	}

	/**
	 * Retorna uma lista de grau de risco a partir da política
	 * 
	 * política não salva no banco (acho que da para usar a outra função)
	 * 
	 * @param policy
	 * 
	 */
	public PaginatedList<RiskLevel> listRiskLevel(Policy policy) {
		List<RiskLevel> array = new ArrayList<RiskLevel>();
		PaginatedList<RiskLevel> list = new PaginatedList<RiskLevel>();
		String[][] str = policy.getRisk_level();

		for (int i = 0; i < str[0].length; i++) {
			if (str[1][i] != null) {
				RiskLevel rk = new RiskLevel();
				rk.setId(null);
				rk.setColor(Integer.parseInt(str[1][i]));
				rk.setLevel(str[0][i]);
				rk.setPolicy(policy);
				array.add(rk);
			}
		}

		list.setList(array);
		list.setTotal(Long.valueOf(array.size()));
		return list;
	}

	/**
	 * Retorna os graus de risco a partir da política
	 * 
	 * @param policy
	 *            instância da política
	 * @return
	 */
	public PaginatedList<RiskLevel> listRiskLevelByPolicy(Policy policy) {
		PaginatedList<RiskLevel> results = new PaginatedList<RiskLevel>();

		Criteria criteria = this.dao.newCriteria(RiskLevel.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("policy", policy));

		Criteria count = this.dao.newCriteria(RiskLevel.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("policy", policy)).setProjection(Projections.countDistinct("id"));

		results.setList(this.dao.findByCriteria(criteria, RiskLevel.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
		
	/**
	 * Retorna riscos de uma unidade
	 * 
	 * @param Unit,
	 *            instância da unidade
	 * 
	 */
	public PaginatedList<Risk> listRiskByUnit(Unit unit) {
		PaginatedList<Risk> results = new PaginatedList<Risk>();

		Criteria criteria = this.dao.newCriteria(Risk.class)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.eq("archived", false))
			.add(Restrictions.eq("unit", unit));

		// Criteria count = this.dao.newCriteria(Risk.class)
		// .add(Restrictions.eq("deleted", false))
		// .add(Restrictions.eq("unit", unit))
		// .setProjection(Projections.countDistinct("id"));

		List<Risk> risks = this.dao.findByCriteria(criteria, Risk.class);
		
		this.setRisksMonitoringState(risks);
		
		results.setList(risks);
		results.setTotal((long) risks.size());

		return results;
	}

		/**
	 * Retorna todos os riscos
	 * 
	 */
	public PaginatedList<Risk> listAllRisks() {
    PaginatedList<Risk> results = new PaginatedList<Risk>();

    Criteria criteria = this.dao.newCriteria(Risk.class)
        .add(Restrictions.eq("deleted", false));

    List<Risk> risks = this.dao.findByCriteria(criteria, Risk.class);

    this.setRisksMonitoringState(risks);

    results.setList(risks);
    results.setTotal((long) risks.size());
    
    return results;
}	
	public List<RiskBean> listRiskReducedByUnits(List<Unit> units) {
		if (GeneralUtils.isEmpty(units)) {
			return Collections.emptyList();
		}
		
		List<Long> unitIds = Util.mapEntityIds(units);
		
		Criteria criteria = this.dao.newCriteria(Risk.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("archived", false))
				.add(Restrictions.in("unit.id", unitIds));
		
		ProjectionList projList = Projections.projectionList()
				.add(Projections.property("id"), "id")
				.add(Projections.property("user.id"), "userId")
				.add(Projections.property("manager.id"), "managerId")
				.add(Projections.property("unit.id"), "unitId")
				.add(Projections.property("riskLevel"), "riskLevel")
				.add(Projections.property("name"), "name")
				.add(Projections.property("probability"), "probability")
				.add(Projections.property("impact"), "impact")
				.add(Projections.property("periodicity"), "periodicity")
				.add(Projections.property("tipology"), "tipology")
				.add(Projections.property("otherTipologies"), "otherTipologies")
				.add(Projections.property("type"), "type")
				.add(Projections.property("begin"), "begin")
				.add(Projections.property("response"), "response");

		criteria.setProjection(projList)
			.setResultTransformer(RiskBean.class);

		List<RiskBean> risks = this.dao.findByCriteria(criteria, RiskBean.class);
		
		for (RiskBean risk : risks) {
			Monitor monitor = monitorBS.lastMonitorbyRisk(risk.getId());
			Date beginDate = monitor != null ? monitor.getBegin() : risk.getBegin();
			int monitoringState = RiskStateChecker.riskState(risk.getPeriodicity(), beginDate);
			risk.setMonitoringState(monitoringState);
			PaginatedList<RiskStrategyBean> strategy = this.listReducedRiskStrategy(this.listRiskById(risk.getId()));
			risk.setStrategies(strategy);		}
		
		return risks;
	}

	public List<Risk> filterRisks(Long planRiskId, RiskFilterParams filters) {
		DefaultParams params = DefaultParams.createWithMaxPageSize();
		return filterRisks(planRiskId, filters, params).getList();
	}

/**
     * Método para filtrar riscos com detalhamento completo necessário para relatórios.
     * 
     * @param planRiskId Identificador do plano de riscos.
     * @param filters Parâmetros de filtragem para busca de riscos.
     * @return Lista de objetos Risk com todas as informações detalhadas necessárias.
     */
	public List<Risk> filterFullRisks(Long planRiskId, RiskFilterParams filters) {
		DefaultParams params = DefaultParams.createWithMaxPageSize();
		List<Risk> risks = filterRisks(planRiskId, filters, params).getList();

		for (Risk risk : risks) {
				risk.setStrategies(this.listRiskStrategy(risk));
				risk.setAxes(this.listRiskAxis(risk));
				risk.setIndicators(this.listRiskIndicators(risk));
				risk.setGoals(this.listRiskGoals(risk));
				risk.setActivities(this.listRiskActivity(risk));
		}

		return risks;
	}
	/**
	 * Recupera riscos de uma unidade. Faz a busca dos riscos em uma unidade
	 * 
	 * @param Unit,
	 *            instância da unidade
	 * 
	 */
	public PaginatedList<Risk> filterRisks(Long planRiskId, RiskFilterParams filters, DefaultParams params) {
		PaginatedList<Risk> results = new PaginatedList<Risk>();

		Criteria criteria = this.dao.newCriteria(Risk.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("unit", "unit", JoinType.INNER_JOIN)
				.createAlias("unit.planRisk", "planRisk", JoinType.INNER_JOIN)
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("manager", "manager", JoinType.LEFT_OUTER_JOIN)
				.add(Restrictions.eq("planRisk.id", planRiskId));
		
		Criteria count = this.dao.newCriteria(Risk.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("unit", "unit", JoinType.INNER_JOIN)
				.createAlias("unit.planRisk", "planRisk", JoinType.INNER_JOIN)
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("manager", "manager", JoinType.LEFT_OUTER_JOIN)
				.add(Restrictions.eq("planRisk.id", planRiskId))
				.setProjection(Projections.countDistinct("id"));
		
		applyFiltersToCriteria(filters, criteria, count);

		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((page-1) * pageSize)
			.setMaxResults(pageSize);

		results.setList(this.dao.findByCriteria(criteria, Risk.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}

	private void applyFiltersToCriteria(RiskFilterParams filters, Criteria criteria, Criteria count) {
		filterService.applyFilters(filters, criteria, count);

	}

	/**
	 * Retorna riscos de uma lista de unidades
	 * 
	 * @param List<Unit>,
	 *            lista da unidades
	 * 
	 */
	public PaginatedList<Risk> listRiskByUnitList(List<Unit> units) {
		PaginatedList<Risk> results = new PaginatedList<Risk>();

		Criteria criteria = this.dao.newCriteria(Risk.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.in("unit", units));

		List<Risk> risks = this.dao.findByCriteria(criteria, Risk.class);
		results.setList(risks);
		results.setTotal((long) risks.size());

		return results;
	}


	// recuperar o risklevel do risco
	// risco -> unidade -> plano -> politica -> risco level
	/**
	 * Retorna o grau de risco a partir da probabilidade e impacto
	 * 
	 * @param ProcessUnit,
	 *            instância processunit
	 * 
	 * @return List<Risk>
	 * @throws Exception
	 */
	public RiskLevel getRiskLevelByRisk(Risk risk, Policy policy) {

		policy = resolvePolicy(risk, policy);
		
		int total = policy.getNline() * policy.getNcolumn() + policy.getNline() + policy.getNcolumn();
		String[][] matrix = getMatrixVector(policy);

		int linha = 0;
		int coluna = 0;
		for (int i = 0; i < total; i++) {
			if (i % (policy.getNline() + 1) == 0) {

				if (risk.getProbability().equals(matrix[i][0])) {
					i = total;
				}
				linha += 1;
			}
		}

		for (int i = total - policy.getNcolumn(); i < total; i++) {
			if (risk.getImpact().equals(matrix[i][0])) {
				i = total;
			}
			coluna += 1;
		}

		int position = ((linha - 1) * (policy.getNcolumn() + 1)) + coluna;

		if (position >= total) {
			throw new IllegalArgumentException("falha ao carregar grau de risco");
		}

		String result = matrix[position][0];

		Criteria criteria = this.dao.newCriteria(RiskLevel.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("policy", policy)).add(Restrictions.eq("level", result));

		return (RiskLevel) criteria.uniqueResult();

	}

	/**
	 * Resolve a política associada ao risco.
	 * 
	 * @param risk instância do risco
	 * 
	 * @param policy instância da política
	 * 
	 * @return Policy política resolvida
	 * 
	 * @throws IllegalStateException se a política não for encontrada
	 */
	private Policy resolvePolicy(Risk risk, Policy policy) {
		
		if (policy != null) {
			return policy;
		}

		Unit unit = this.exists(risk.getUnit().getId(), Unit.class);

		if (unit == null) {
			return null;
		}

		PlanRisk planRisk = this.exists(unit.getPlanRisk().getId(), PlanRisk.class);

		if (planRisk == null) {
			return null;
		}

		policy = this.exists(planRisk.getPolicy().getId(), Policy.class);

		if (policy == null) {
			throw new IllegalStateException("Policy not found");
		}

		return policy;
	}

	/**
	 * Retorna os riscos a partir de um nível (level)
	 * 
	 * @param level
	 * @param page
	 * @param pageSize
	 * @return Riscos paginados
	 */

	public PaginatedList<Risk> listRiskByLevel(PlanRisk planRisk, String level, Integer page,
			Integer pageSize) {

		if (page == null || page < 1) {
			page = 1;
		}
		if (pageSize == null) {
			pageSize = PAGESIZE;
		}

		PaginatedList<Risk> results = new PaginatedList<Risk>();

		Criteria criteria = this.dao.newCriteria(Risk.class).setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize).addOrder(Order.asc("name")).createAlias("unit", "unit").createAlias("riskLevel", "riskLevel")
				.add(Restrictions.eq("unit.planRisk", planRisk)).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("riskLevel.level", level));

		Criteria counting = this.dao.newCriteria(Risk.class).createAlias("unit", "unit").createAlias("riskLevel", "riskLevel")
				.add(Restrictions.eq("unit.planRisk", planRisk)).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("riskLevel.level", level))
				.setProjection(Projections.countDistinct("id"));

		List<Risk> risks = this.dao.findByCriteria(criteria, Risk.class);
		results.setList(risks);
		results.setTotal((Long) counting.uniqueResult());

		return results;
	}

	/**
	 * Retorna os riscos a partir de um id de usuário
	 * 
	 * @param user
	 * @param page
	 * @param pageSize
	 * @return Riscos paginados
	 */

	public PaginatedList<Risk> listRiskByUser(Long userId, DefaultParams params) {
		PaginatedList<Risk> results = new PaginatedList<Risk>();
		int page = params.getPage();
		int pageSize = params.getPageSize();
		Criteria criteria = this.dao.newCriteria(Risk.class).setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.add(Restrictions.eq("user.id", userId)).add(Restrictions.eq("deleted", false));
			
		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		} else {
			criteria.addOrder(Order.asc("name"));
		}
		Criteria counting = this.dao.newCriteria(Risk.class)
				.add(Restrictions.eq("user.id", userId)).add(Restrictions.eq("deleted", false))
				.setProjection(Projections.countDistinct("id"));

		List<Risk> risks = this.dao.findByCriteria(criteria, Risk.class);
		results.setList(risks);
		results.setTotal((Long) counting.uniqueResult());

		return results;
	}

	/**
	 * Retorna as ativiades do processo de um risco
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<RiskActivity>
	 */
	public PaginatedList<RiskActivity> listActivityByRisk(Risk risk) {

		PaginatedList<RiskActivity> results = new PaginatedList<RiskActivity>();

		Criteria criteria = this.dao.newCriteria(RiskActivity.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk));

		Criteria count = this.dao.newCriteria(RiskActivity.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk)).setProjection(Projections.countDistinct("id"));

		results.setList(this.dao.findByCriteria(criteria, RiskActivity.class));
		results.setTotal((Long) count.uniqueResult());
		return results;

	}

	/**
	 * Retorna os objetivos estrategicos vinculados ao risco
	 * 
	 * @param risk
	 *            instância do Risk
	 * @return PaginatedList<RiskStrategy> lista de objetivos estrategicos
	 */
	public PaginatedList<RiskStrategy> listRiskStrategy(Risk risk) {
		PaginatedList<RiskStrategy> results = new PaginatedList<RiskStrategy>();

		Criteria criteria = this.dao.newCriteria(RiskStrategy.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk));

		Criteria count = this.dao.newCriteria(RiskStrategy.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk)).setProjection(Projections.countDistinct("id"));

		results.setList(this.dao.findByCriteria(criteria, RiskStrategy.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	/**
	 * Retorna os objetivos estrategicos vinculados ao risco
	 * 
	 * @param risk
	 *            instância do Risk
	 * @return PaginatedList<RiskStrategy> lista de objetivos estrategicos
	 */
	public PaginatedList<RiskStrategyBean> listReducedRiskStrategy(Risk risk) {
		PaginatedList<RiskStrategyBean> results = new PaginatedList<RiskStrategyBean>();

		Criteria criteria = this.dao.newCriteria(RiskStrategy.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk));
		
		ProjectionList projList = Projections.projectionList()
				.add(Projections.property("id"), "id")
				.add(Projections.property("name"), "name")
				.add(Projections.property("structure.id"), "structureId");

		criteria.setProjection(projList)
			.setResultTransformer(RiskStrategyBean.class);

		results.setList(this.dao.findByCriteria(criteria, RiskStrategyBean.class));
		return results;
	}
	
	/**
	 * Retorna os eixos tematicos vinculados ao risco
	 * 
	 * @param risk
	 *            instância do Risk
	 * @return PaginatedList<RiskAxis> lista de eixos tematicos
	 */
	public PaginatedList<RiskAxis> listRiskAxis(Risk risk) {
		PaginatedList<RiskAxis> results = new PaginatedList<RiskAxis>();

		Criteria criteria = this.dao.newCriteria(RiskAxis.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk));

		Criteria count = this.dao.newCriteria(RiskAxis.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk)).setProjection(Projections.countDistinct("id"));

		results.setList(this.dao.findByCriteria(criteria, RiskAxis.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	/**
	 * Retorna os indicadores vinculados ao risco
	 * 
	 * @param risk
	 *            instância do Risk
	 * @return PaginatedList<RiskIndicator> lista de indicadores
	 */
	public PaginatedList<RiskIndicator> listRiskIndicators(Risk risk) {
		PaginatedList<RiskIndicator> results = new PaginatedList<RiskIndicator>();

		Criteria criteria = this.dao.newCriteria(RiskIndicator.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk));

		Criteria count = this.dao.newCriteria(RiskIndicator.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk)).setProjection(Projections.countDistinct("id"));

		results.setList(this.dao.findByCriteria(criteria, RiskIndicator.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	/**
	 * Retorna as metas vinculadas ao risco
	 * 
	 * @param risk
	 *            instância do Risk
	 * @return PaginatedList<RiskGoal> lista de metas
	 */
	public PaginatedList<RiskGoal> listRiskGoals(Risk risk) {
		PaginatedList<RiskGoal> results = new PaginatedList<RiskGoal>();

		Criteria criteria = this.dao.newCriteria(RiskGoal.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk));

		Criteria count = this.dao.newCriteria(RiskGoal.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk)).setProjection(Projections.countDistinct("id"));

		results.setList(this.dao.findByCriteria(criteria, RiskGoal.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}

	/**
	 * Retorna as atividades do processo do risco
	 * 
	 * @param risk
	 *            instância do Risk
	 * @return PaginatedList<RiskActivity> lista de atividades do processo
	 */
	public PaginatedList<RiskActivity> listRiskActivity(Risk risk) {
		PaginatedList<RiskActivity> results = new PaginatedList<RiskActivity>();

		Criteria criteria = this.dao.newCriteria(RiskActivity.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk));

		Criteria count = this.dao.newCriteria(RiskActivity.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk)).setProjection(Projections.countDistinct("id"));

		results.setList(this.dao.findByCriteria(criteria, RiskActivity.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	public List<Unit> listSharedUnits(Risk risk) {
		List<RiskSharedUnit> riskSharedUnits = this.listRiskSharedUnits(risk, false);
		List<Unit> units = new ArrayList<Unit>(riskSharedUnits.size());
		for (RiskSharedUnit riskSharedUnit : riskSharedUnits) {
			units.add(riskSharedUnit.getUnit());
		}
		return units;
	}
	
	public List<RiskHistoryBean> listHistoryByUnits(PaginatedList<Unit> units) {
		if (GeneralUtils.isEmpty(units.getList())) {
			return Collections.emptyList();
		}

		List<Long> unitIds = Util.mapEntityIds(units.getList());

		Criteria criteria = this.dao.newCriteria(RiskHistory.class)
				.add(Restrictions.in("unit.id", unitIds))
				.add(Restrictions.eq("deleted", false))
				.addOrder(Order.asc("id"));

		ProjectionList projList = Projections.projectionList()
				.add(Projections.property("id"), "id")
				.add(Projections.property("riskLevel.id"), "riskLevelId")
				.add(Projections.property("unit.id"), "unitId")
				.add(Projections.property("month"), "month")
				.add(Projections.property("year"), "year")
				.add(Projections.property("threat"), "threat")
				.add(Projections.property("quantity"), "quantity");
		
		criteria.setProjection(projList)
			.setResultTransformer(RiskHistoryBean.class);
		
		return this.dao.findByCriteria(criteria, RiskHistoryBean.class);
	}

	/**
	 * Transforma a string matrix em vetor
	 * 
	 * 
	 * @param policy
	 * @return
	 */
	public String[][] getMatrixVector(Policy policy) {
		int total = policy.getNline() * policy.getNcolumn() + policy.getNline() + policy.getNcolumn();
		String[] aux = policy.getMatrix().split(";");
		String[][] matrix = new String[total][3];

		for (int i = 0; i < aux.length; i++) {
			matrix[i][0] = aux[i].split("\\[.*\\]")[1];
			Pattern pattern = Pattern.compile("\\[.*\\]");
			Matcher matcher = pattern.matcher(aux[i]);
			if (matcher.find()) {
				matrix[i][1] = matcher.group(0).split(",")[0].split("\\[")[1];
				matrix[i][2] = matcher.group(0).split(",")[1].split("\\]")[0];
			}
		}
		return matrix;
	}
	
	/**
	 * Verifica se um processo tem algum risco vinculado
	 * 
	 * @param process
	 *            instância de um processo
	 * @param boolean
	 *            booleano para verificação
	 */
	public boolean hasLinkedRiskObjectiveProcess(Process process) {		
		
		List<RiskProcessObjective> processesObj = new ArrayList<>();

		List<ProcessObjective> objectives = this.processBS.listProcessObjectives(process);
	
		for (ProcessObjective processObj : objectives) {
		    Criteria criteria = this.dao.newCriteria(RiskProcessObjective.class);

		    criteria.createAlias("processObjective", "processObjective", JoinType.INNER_JOIN)
		    		.add(Restrictions.eq("processObjective.id", processObj.getId()));
			
			processesObj.addAll(this.dao.findByCriteria(criteria, RiskProcessObjective.class));
		}

		return !processesObj.isEmpty();
	}

	/**
	 * Verifica se um processo tem alguma atividade vinculada
	 * 
	 * @param process
	 *            instância de um processo
	 * @param boolean
	 *            booleano para verificação
	 */
	public boolean hasLinkedRiskActivity(Process process) {
		Criteria criteria = this.dao.newCriteria(RiskActivity.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("process", process));
		criteria.setMaxResults(1);
		return criteria.uniqueResult() != null;
	}
	
    /**
	 * Retorna se o usuário tem ou não a permissao de editar um risco
     * 
	 * @param userSession
	 *            sessão do usuario     
     *
     * @return boolean se o usuário tem permissão de editar aquele risco
     */
	public boolean hasPermissionToEditRisk(UserSession userSession) {
	    return userSession.getAccessLevel() >= AccessLevels.MANAGER.getLevel() ||
				userSession.getPermissions().contains(ManageRiskPermission.class.getCanonicalName());
	}
	
	public RiskItemPermissionInfo validateRiskItemPermissions(Risk risk, User itemResponsible) {
        boolean hasPermission = hasPermissionToEditRiskItems(userSession);
        boolean isResponsible = isResposible(userSession, itemResponsible);
        boolean isRiskResponsible = isResposible(userSession, risk.getUser());
		
        if (!hasPermission && !isResponsible && !isRiskResponsible) {
            throw new IllegalStateException("Usuário não tem permissão de acesso.");
        }

        return new RiskItemPermissionInfo(hasPermission, isResponsible, isRiskResponsible);
	}
	
	public boolean isResposible(UserSession userSession, User userResponsible) {
		return userSession.getUser().getId().equals(userResponsible.getId());
	}
	
    /**
     * Retorna se o usuário tem ou não a permissao de editar os itens de um risco
     * 
     * @param userSession
     *            sessão do usuario     
     *
     * @return boolean se o usuário tem permissão de editar algum item do risco
     */
    public boolean hasPermissionToEditRiskItems(UserSession userSession) {
        return userSession.getAccessLevel() >= AccessLevels.MANAGER.getLevel() ||
		        userSession.getPermissions().contains(ManageRiskItemsPermission.class.getCanonicalName());
    }
	
	public void sendUserLinkedToRiskNotification(Risk risk, Unit unit, User user, String baseUrl) throws EmailException {
		String url = baseUrl + "/#/forrisco/" + "risk/" + risk.getId() + "/details" + "/info";

		String text = String.format("Você foi vinculado como responsável pelo risco %s no ForRisco", risk.getName());

		this.notificationBS.sendNotification(NotificationType.FORRISCO_USER_LINKED_TO_RISK, text, null,
				user.getId(), url);
		this.notificationBS.sendNotificationEmail(NotificationType.FORRISCO_USER_LINKED_TO_RISK, text, "",
				user, url);
	}
	
	public void sendRiskChangedNotification(Risk risk, User user, String baseUrl) throws EmailException {
		String url = baseUrl + "/#/forrisco/" + "risk/" + risk.getId() + "/details" + "/info";
		String text = String.format("O risco %s foi alterado.", risk.getName());
		this.notificationBS.sendNotification(NotificationType.FORRISCO_MANAGER_RISK_UPDATED, text, null,
				user.getId(), url);
		this.notificationBS.sendNotificationEmail(NotificationType.FORRISCO_MANAGER_RISK_UPDATED, text, "",
				user, url);
	}
	
	public void sendNotificationToRiskItemManager(
		Risk risk, User manager, String url, String text, NotificationType notificationType
	) throws EmailException {
		String aux = risk.getName();

		this.notificationBS
			.sendNotification(notificationType, text, aux, manager.getId(), url);
		this.notificationBS
			.sendNotificationEmail(notificationType, text, aux, manager, url);
	}
	
	public void validateVigency(Date validityBegin, Date validityEnd) {
		if ((validityBegin == null && validityEnd != null) ||
				(validityEnd == null && validityBegin != null)) {
			throw new IllegalArgumentException("Não é permitido preencher somente uma das datas do prazo de vigência");
		}
	
		if (validityBegin != null && validityEnd != null &&
				validityEnd.before(validityBegin)) {
			throw new IllegalArgumentException("A data de início do prazo de vigência não deve ser superior à data de término");
		}
	}
	
	public List<RiskSharedUnit> listRiskSharedUnits(Risk risk, boolean includesDeleted) {
	    Criteria criteria = this.dao.newCriteria(RiskSharedUnit.class);

	    criteria.createAlias("risk", "risk", JoinType.INNER_JOIN)
	    		.createAlias("unit", "unit", JoinType.INNER_JOIN)
	    		.add(Restrictions.eq("risk.id", risk.getId()));

	    if (!includesDeleted) {
		    criteria.add(Restrictions.eq("unit.deleted", false));
	    }
	    
	    return this.dao.findByCriteria(criteria, RiskSharedUnit.class);
	}
	
	public List<RiskProcessObjective> listRiskProcessObjectives(Risk risk) {
	    Criteria criteria = this.dao.newCriteria(RiskProcessObjective.class);

	    criteria.createAlias("risk", "risk", JoinType.INNER_JOIN)
	    		.createAlias("processObjective", "processObjective", JoinType.INNER_JOIN)
	    		.add(Restrictions.eq("risk.id", risk.getId()));
	    
	    return this.dao.findByCriteria(criteria, RiskProcessObjective.class);
	}
	
	public void updateRisk(Risk risk) throws EmailException {
		Risk existent = this.exists(risk.getId(), Risk.class);
		Unit unit = this.exists(risk.getUnit().getId(), Unit.class);
		User user = this.exists(risk.getUser().getId(), User.class);
		User manager = this.exists(risk.getManager().getId(), User.class);

		RiskLevel riskLevel = this.getRiskLevelByRisk(risk, null);

		this.validateRisk(risk, riskLevel);

		boolean userChanged = !existent.getUser().equals(user);
		boolean managerChanged = existent.getManager() == null || !existent.getManager().equals(manager);
		boolean hasPermission = this.hasPermissionToEditRisk(userSession);
        boolean isResponsible = userSession.getUser().getId().equals(existent.getUser().getId());

        if (!hasPermission && !isResponsible) {
            throw new IllegalStateException("Usuário não tem permissão para realizar esta edição.");
        }

        if (hasPermission) {
            existent.setUser(user);
            existent.setName(risk.getName());
            existent.setCode(risk.getCode());
			existent.setManager(manager);
        }

        if (unit == null) {
        	throw new IllegalStateException("Unidade não encontrada.");
        }

        Policy currentPolicy = existent.getUnit().getPlanRisk().getPolicy();
        Policy newPolicy = unit.getPlanRisk().getPolicy();

        if (!newPolicy.getId().equals(currentPolicy.getId())) {
        	throw new IllegalStateException("O risco não pode ser movido para uma política diferente.");
        }

		existent.setUnit(unit);
		existent.setImpact(risk.getImpact());
		existent.setProbability(risk.getProbability());
		existent.setPeriodicity(risk.getPeriodicity());
		existent.setLinkFPDI(risk.getLinkFPDI());
		existent.setReason(SanitizeUtil.sanitize(risk.getReason()));
		existent.setResult(SanitizeUtil.sanitize(risk.getResult()));
		existent.setTipology(risk.getTipology());
		existent.setType(risk.getType());
		existent.setRisk_act_process(risk.isRisk_act_process());
		existent.setRisk_obj_process(risk.isRisk_obj_process());
		existent.setRisk_pdi(risk.isRisk_pdi());
		existent.setRisk_pdi_axis(risk.isRisk_pdi_axis());
		existent.setOtherTipologies(risk.getOtherTipologies());
		existent.setResponse(risk.getResponse());
		existent.setLevel(risk.getLevel());
		existent.setProcessObjectives(risk.getProcessObjectives());
		existent.setArchived(risk.isArchived());
		existent.setRiskLevel(riskLevel);

		this.deleteAPS(existent);			
		this.saveActivities(risk);
		this.saveRiskProcessObjective(risk);
		this.saveStrategies(risk);
		this.saveAxes(risk);
		this.saveIndicators(risk);
		this.saveGoals(risk);
		this.saveResponse(risk);

		this.saveRisk(existent);

		if (domain != null) {
			if (userChanged) {
				this.sendUserLinkedToRiskNotification(risk, unit, user, domain.get().getBaseUrl());
			}

			if (managerChanged) {
				this.sendUserLinkedToRiskNotification(risk, unit, manager, domain.get().getBaseUrl());
			} else {
				this.sendRiskChangedNotification(risk, manager, domain.get().getBaseUrl());
			}
		}
	}

	/**
	 * Define o estado de monitoramento para uma lista de riscos com base na
	 * periodicidade e na data de início do monitoramento.
	 * 
	 * @param risks
	 *              Lista de instâncias de Risk.
	 * 
	 * @return void
	 */
	public void setRisksMonitoringState(List<Risk> risks) {
		for (Risk risk : risks) {
			Monitor monitor = monitorBS.lastMonitorbyRisk(risk.getId());
			Date beginDate = monitor != null ? monitor.getBegin() : risk.getBegin();
			int monitoringState = RiskStateChecker.riskState(risk.getPeriodicity(), beginDate);
			risk.setMonitoringState(monitoringState);
		}
	}
}