package org.forrisco.risk;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.mail.EmailException;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.JoinType;
import org.forpdi.core.common.Order;
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
import org.forpdi.system.EmailUtilsPlugin;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.policy.Policy;
import org.forrisco.core.process.Process;
import org.forrisco.core.process.ProcessBS;
import org.forrisco.core.process.ProcessObjective;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.contingency.Contingency;
import org.forrisco.risk.history.RiskEditHistoryBS;
import org.forrisco.risk.incident.Incident;
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
import org.forrisco.risk.preventiveaction.PreventiveAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

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

	@Autowired
	private RiskEditHistoryBS riskEditHistoryBS;

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private EmailUtilsPlugin emailPlugin;

	private static final int PAGESIZE = 10;
	private static final String DELETED_PROPERTY = "deleted";
	private static final String POLICY = "policy";
	private static final String ARCHIVED = "archived";
	private static final String IMPORTED = "imported";
	private static final String MANAGER = "manager";
	private static final String RISK_LEVEL = "riskLevel";

	public void createNewRisk(Risk risk) throws EmailException {
		RiskLevel riskLevel = this.getRiskLevelByRisk(risk, null);

		this.validateRisk(risk, riskLevel);
		risk.setRiskLevel(riskLevel);

		risk.setId(null);
		risk.setBegin(new Date());
		risk.setReason(SanitizeUtil.sanitize(risk.getReason()));
		risk.setResult(SanitizeUtil.sanitize(risk.getResult()));
		this.saveRisk(risk);
		if (risk.getSharedUnits() != null) {
			for (Unit unit1 : risk.getSharedUnits()) {
				if (!risk.getUnit().getId().equals(unit1.getId())) {
					notifySharedRisk(risk, unit1, unit1.getUser());
				}
			}
		}
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
	 *              instância de um risco a ser salvo
	 */
	public void notifySharedRisk(Risk risk, Unit unit, User responsible) throws EmailException {
		if (risk == null || unit == null)
			return;

		unit = unitBS.retrieveUnitById(unit.getId());
		User target = (responsible != null) ? responsible : unit.getUser();
		if (target == null || target.getId() == null)
			return;

		String baseUrl = domain.get().getBaseUrl();
		String url = baseUrl + "/#/forrisco/risk/" + risk.getId() + "/details/info";
		;
		String nomeResposta;
		NotificationType notificationType;

		if (RiskResponse.SHARE.getId() == risk.getResponse()) {
			nomeResposta = "compartilhado";
			notificationType = NotificationType.FORRISCO_SHARED_RISK;
		} else {
			nomeResposta = "transferido";
			notificationType = NotificationType.FORRISCO_TRANSFERED_RISK;
		}

		String alertSubject = "ForRisco - Há um risco " + nomeResposta;

		String body = " O risco " + risk.getName()
				+ " foi " + nomeResposta + " com a unidade/subunidade " + unit.getName()
				+ ", pela qual você atua como responsável técnico, no ForRisco.";

		String subject = alertSubject;

		this.notificationBS.sendNotificationEmail(
				notificationType,
				body,
				subject,
				target,
				url);

		this.notificationBS.sendNotification(
				notificationType,
				body,
				subject,
				target.getId(),
				url);
	}

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

	public void isUserLinkedToRisk(Long userId) {

		Criteria criteria = this.dao.newCriteria(Unit.class);
		criteria.createAlias("user", "u", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("u.id", userId));
		criteria.setMaxResults(1);
		Boolean userIsLinkedToUnit = !criteria.list().isEmpty();

		if (userIsLinkedToUnit) {
			throw new IllegalStateException("Usuário está vinculado à unidade(s)");
		}

		Criteria criteria1 = this.dao.newCriteria(Risk.class);
		criteria1.createAlias("user", "u", JoinType.LEFT_OUTER_JOIN);
		criteria1.createAlias("manager", "m", JoinType.LEFT_OUTER_JOIN);
		criteria1.add(Restrictions.eq("deleted", false));
		criteria1.add(Restrictions.disjunction()
			.add(Restrictions.eq("u.id", userId))
			.add(Restrictions.eq("m.id", userId)));
		criteria1.setMaxResults(1);
		Boolean userIsLinkedToRisk = !criteria1.list().isEmpty();

		if(userIsLinkedToRisk) {
			throw new IllegalStateException("Usuário está vinculado à risco(s)");
		}

		Criteria criteria2 = this.dao.newCriteria(Monitor.class);
		criteria2.createAlias("user", "u", JoinType.LEFT_OUTER_JOIN);
		criteria2.createAlias("manager", "m", JoinType.LEFT_OUTER_JOIN);
		criteria2.add(Restrictions.eq("deleted", false));
		criteria2.add(Restrictions.disjunction()
			.add(Restrictions.eq("u.id", userId))
			.add(Restrictions.eq("m.id", userId)));
		criteria2.setMaxResults(1);
		Boolean userIsLinkedToMonitor = !criteria2.list().isEmpty();

		if(userIsLinkedToMonitor) {
			throw new IllegalStateException("Usuário está vinculado à Monitoria(s)");
		}

		Criteria criteria3 = this.dao.newCriteria(Incident.class);
		criteria3.createAlias("user", "u", JoinType.LEFT_OUTER_JOIN);
		criteria3.createAlias("manager", "m", JoinType.LEFT_OUTER_JOIN);
		criteria3.add(Restrictions.eq("deleted", false));
		criteria3.add(Restrictions.disjunction()
			.add(Restrictions.eq("u.id", userId))
			.add(Restrictions.eq("m.id", userId)));
		criteria3.setMaxResults(1);
		Boolean userIsLinkedToIncident = !criteria3.list().isEmpty();

		if (userIsLinkedToIncident) {
			throw new IllegalStateException("Usuário está vinculado à Incidente(s)");
		}

		Criteria criteria4 = this.dao.newCriteria(Contingency.class);
		criteria4.createAlias("user", "u", JoinType.LEFT_OUTER_JOIN);
		criteria4.createAlias("manager", "m", JoinType.LEFT_OUTER_JOIN);
		criteria4.add(Restrictions.eq("deleted", false));
		criteria4.add(Restrictions.disjunction()
			.add(Restrictions.eq("u.id", userId))
			.add(Restrictions.eq("m.id", userId)));
		criteria4.setMaxResults(1);
		Boolean userIsLinkedToContingency = !criteria4.list().isEmpty();

		if (userIsLinkedToContingency) {
			throw new IllegalStateException("Usuário está vinculado à Contingência(s)");
		}

		Criteria criteria5 = this.dao.newCriteria(PreventiveAction.class);
		criteria5.createAlias("user", "u", JoinType.LEFT_OUTER_JOIN);
		criteria5.createAlias("manager", "m", JoinType.LEFT_OUTER_JOIN);
		criteria5.add(Restrictions.eq("deleted", false));
		criteria5.add(Restrictions.disjunction()
			.add(Restrictions.eq("u.id", userId))
			.add(Restrictions.eq("m.id", userId)));
		criteria5.setMaxResults(1);
		Boolean userIsLinkedToPreventiveAction = !criteria5.list().isEmpty();

		if (userIsLinkedToPreventiveAction) {
			throw new IllegalStateException("Usuário está vinculado a uma ou mais Ações de Prevenção");
		}
	}

	/**
	 * Salva no banco de dados graus de risco
	 *
	 * @param policy,
	 *                instância da política a ser salva
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
	 *                   instância de um grau de risco a ser salvo
	 */
	public void saveRiskLevel(RiskLevel risklevel) {
		risklevel.setDeleted(false);
		this.persist(risklevel);
	}

	/**
	 * Salvar uma lista de atividades
	 *
	 * @param PaginatedList<RiskActivity>
	 *                                    instância de uma lista de atividades de
	 *                                    risco
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
	 *              instância do risk
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
			riskProcessObj.setLinkFPDI(
					"#/forrisco/plan-risk/" + existentObj.getProcess().getUnitCreator().getPlanRisk().getId() + "/unit/"
							+ existentObj.getProcess().getUnitCreator().getId() + "/info");
			riskProcessObj.setProcessId(processObj.getId());
			this.persist(riskProcessObj);
		}
	}

	/**
	 * Salvar uma lista de objetivos estratégicos
	 *
	 * @param PaginatedList<RiskStrategy>
	 *                                    instância de uma lista de objetivos
	 *                                    estratégicos
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
	 *                                instância de uma lista de eixos tematicoss
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
	 *                                     instância de uma lista de indicadores
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
					"#/plan/" + plan.getParent().getId() + "/details/subplan/level/"
							+ indicator.getStructure().getId());
			indicator.setName(structure.getName());
			indicator.setStructure(structure);
			this.dao.persist(indicator);
		}
	}

	/**
	 * Salvar uma lista de metas
	 *
	 * @param PaginatedList<RiskGoal>
	 *                                instância de uma lista de indicadores
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

			if (riskResponse.equals(RiskResponse.SHARE) || riskResponse.equals(RiskResponse.TRANSFER)) {
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
	 *              instância do item a ser deletado
	 */
	public void delete(RiskLevel risklevel) {
		risklevel.setDeleted(true);
		this.persist(risklevel);
	}

	/**
	 * Deleta do banco de dados uma atividade do processo
	 *
	 * @param RiskActivity,
	 *                      instância da atividade do processo
	 */
	private void delete(RiskActivity act) {
		act.setDeleted(true);
		this.persist(act);
	}

	/**
	 * Deleta do banco de dados um objetivo estratégico
	 *
	 * @param RiskStrategy,
	 *                      instância do objetivo
	 */
	private void delete(RiskStrategy str) {
		str.setDeleted(true);
		this.persist(str);
	}

	/**
	 * Deleta do banco de dados um eixo temático
	 *
	 * @param RiskAxis,
	 *                  instância do eixo
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
	 *              instância do risco
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
	 *              instância do risco
	 */
	public void delete(Risk risk) {
		risk.setDeleted(true);
		this.persist(risk);
	}

	/**
	 * Retorna um risco
	 *
	 * @param id
	 *           Id do risco
	 */
	public Risk listRiskById(Long id) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Risk> cq = cb.createQuery(Risk.class);
		Root<Risk> root = cq.from(Risk.class);

		cq.select(root)
				.where(
						cb.equal(root.get(DELETED_PROPERTY), false),
						cb.equal(root.get("id"), id));

		TypedQuery<Risk> query = em.createQuery(cq).setMaxResults(1);
		var list = query.getResultList();
		return list.isEmpty() ? null : list.get(0);
	}

	public List<Risk> listRisksByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) return java.util.Collections.emptyList();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Risk> cq = cb.createQuery(Risk.class);
		Root<Risk> root = cq.from(Risk.class);

		cq.select(root)
			.where(
				cb.equal(root.get(DELETED_PROPERTY), false),
				root.get("id").in(ids)
			);

		List<Risk> result = em.createQuery(cq).getResultList();

		return result;
	}


	/**
	 * Retorna uma lista de grau de risco a partir da política
	 *
	 * política não salva no banco (acho que da para usar a outra função)
	 * ç
	 *
	 * @param policy
	 *
	 */
	public PaginatedList<RiskLevel> listRiskLevel(Policy policy) {
		List<RiskLevel> array = new ArrayList<>();
		PaginatedList<RiskLevel> list = new PaginatedList<>();
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
	 * @param policy instância da política
	 * @return PaginatedList contendo lista e total de registros
	 */
	@Transactional(Transactional.TxType.SUPPORTS)
	public PaginatedList<RiskLevel> listRiskLevelByPolicy(Policy policy) {
		CriteriaBuilder cbCount = em.getCriteriaBuilder();
		CriteriaQuery<Long> countCQ = cbCount.createQuery(Long.class);
		Root<RiskLevel> countRoot = countCQ.from(RiskLevel.class);

		countCQ.select(cbCount.countDistinct(countRoot.get("id")));
		countCQ.where(
				cbCount.equal(countRoot.get(DELETED_PROPERTY), false),
				cbCount.equal(countRoot.get(POLICY), policy));

		Long total = em.createQuery(countCQ).getSingleResult();

		CriteriaBuilder cbList = em.getCriteriaBuilder();
		CriteriaQuery<RiskLevel> listCQ = cbList.createQuery(RiskLevel.class);
		Root<RiskLevel> listRoot = listCQ.from(RiskLevel.class);

		listCQ.select(listRoot);
		listCQ.where(
				cbList.equal(listRoot.get(DELETED_PROPERTY), false),
				cbList.equal(listRoot.get(POLICY), policy));

		TypedQuery<RiskLevel> typedQuery = em.createQuery(listCQ);
		List<RiskLevel> list = typedQuery.getResultList();

		PaginatedList<RiskLevel> results = new PaginatedList<>();
		results.setList(list);
		results.setTotal(total);

		return results;
	}

	/**
	 * Retorna riscos de uma unidade
	 *
	 * @param unit instância da unidade
	 */
	@Transactional(Transactional.TxType.SUPPORTS)
	public PaginatedList<Risk> listRiskByUnit(Unit unit) {
		CriteriaBuilder cbCount = em.getCriteriaBuilder();
		CriteriaQuery<Long> countCQ = cbCount.createQuery(Long.class);
		Root<Risk> countRoot = countCQ.from(Risk.class);

		countCQ.select(cbCount.countDistinct(countRoot.get("id")));
		countCQ.where(
				cbCount.equal(countRoot.get(DELETED_PROPERTY), false),
				cbCount.equal(countRoot.get(ARCHIVED), false),
				cbCount.equal(countRoot.get(IMPORTED), false),
				cbCount.equal(countRoot.get("unit"), unit));
		Long total = em.createQuery(countCQ).getSingleResult();

		CriteriaBuilder cbList = em.getCriteriaBuilder();
		CriteriaQuery<Risk> listCQ = cbList.createQuery(Risk.class);
		Root<Risk> listRoot = listCQ.from(Risk.class);

		listCQ.select(listRoot);
		listCQ.where(
				cbList.equal(listRoot.get(DELETED_PROPERTY), false),
				cbList.equal(listRoot.get(ARCHIVED), false),
				cbList.equal(listRoot.get(IMPORTED), false),
				cbList.equal(listRoot.get("unit"), unit));
		List<Risk> risks = em.createQuery(listCQ).getResultList();

		setRisksMonitoringState(risks);

		PaginatedList<Risk> results = new PaginatedList<>();
		results.setList(risks);
		results.setTotal(total);
		return results;
	}

	/**
	 * Retorna todos os riscos não deletados
	 */
	@Transactional(Transactional.TxType.SUPPORTS)
	public PaginatedList<Risk> listAllRisks() {
		CriteriaBuilder cbCount = em.getCriteriaBuilder();
		CriteriaQuery<Long> countCQ = cbCount.createQuery(Long.class);
		Root<Risk> countRoot = countCQ.from(Risk.class);

		countCQ.select(cbCount.countDistinct(countRoot.get("id")));
		countCQ.where(cbCount.equal(countRoot.get(DELETED_PROPERTY), false));

		Long total = em.createQuery(countCQ).getSingleResult();

		CriteriaBuilder cbList = em.getCriteriaBuilder();
		CriteriaQuery<Risk> listCQ = cbList.createQuery(Risk.class);
		Root<Risk> listRoot = listCQ.from(Risk.class);

		listCQ.select(listRoot);
		listCQ.where(cbList.equal(listRoot.get(DELETED_PROPERTY), false));

		List<Risk> risks = em.createQuery(listCQ).getResultList();

		setRisksMonitoringState(risks);

		PaginatedList<Risk> results = new PaginatedList<>();
		results.setList(risks);
		results.setTotal(total);
		return results;
	}

	/**
	 * Retorna uma lista reduzida de riscos por unidades, preenchendo somente os
	 * campos de RiskBean, sem registros deletados ou arquivados.
	 */
	@Transactional(Transactional.TxType.SUPPORTS)
	public List<RiskBean> listRiskReducedByUnits(List<Unit> units) {
		if (GeneralUtils.isEmpty(units)) {
			return Collections.emptyList();
		}

		Date now = new Date();
		List<Long> unitIds = units.stream()
				.map(Unit::getId)
				.collect(Collectors.toList());

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RiskBean> cq = cb.createQuery(RiskBean.class);
		Root<Risk> root = cq.from(Risk.class);

		cq.select(cb.construct(
				RiskBean.class,
				root.get("id"),
				root.get("user").get("id"),
				root.get(MANAGER).get("id"),
				root.get("unit").get("id"),
				root.get(RISK_LEVEL),
				root.get("name"),
				root.get("probability"),
				root.get("impact"),
				root.get("periodicity"),
				root.get("tipology"),
				root.get("otherTipologies"),
				root.get("type"),
				root.get("begin"),
				root.get("response")));

		cq.where(
				cb.equal(root.get(DELETED_PROPERTY), false),
				cb.equal(root.get(ARCHIVED), false),
				cb.equal(root.get(IMPORTED), false),
				root.get("unit").get("id").in(unitIds));

		TypedQuery<RiskBean> query = em.createQuery(cq);
		List<RiskBean> risks = query.getResultList();

		for (RiskBean risk : risks) {
			if (listRiskById(risk.getId()).getUnit().getPlanRisk().getValidityEnd().before(now)) {
				risk.setMonitoringState(RiskState.EXPIRED_PLAN.getId());
			} else {
				Monitor monitor = monitorBS.lastMonitorbyRisk(risk.getId());
				Date beginDate = monitor != null ? monitor.getBegin() : risk.getBegin();
				int monitoringState = RiskStateChecker.riskState(risk.getPeriodicity(), beginDate);
				risk.setMonitoringState(monitoringState);
			}
			PaginatedList<RiskStrategyBean> strategy = this.listReducedRiskStrategy(this.listRiskById(risk.getId()));
			risk.setStrategies(strategy);
		}

		return risks;
	}

	public List<Risk> filterRisks(Long planRiskId, RiskFilterParams filters) {
		DefaultParams params = DefaultParams.createWithMaxPageSize();
		return filterRisks(planRiskId, filters, params).getList();
	}

	/**
	 * Método para filtrar riscos com detalhamento completo necessário para
	 * relatórios.
	 *
	 * @param planRiskId Identificador do plano de riscos.
	 * @param filters    Parâmetros de filtragem para busca de riscos.
	 * @return Lista de objetos Risk com todas as informações detalhadas
	 *         necessárias.
	 */
	public List<Risk> filterFullRisks(Long planRiskId, RiskFilterParams filters) {
		DefaultParams params = DefaultParams.createWithMaxPageSize();
		List<Risk> risks = filterRisks(planRiskId, filters, params).getList();

		for (Risk risk : risks) {
			risk.setStrategies(this.listRiskStrategy(risk));
			risk.setAxes(this.listRiskAxis(risk));
			risk.setIndicators(this.listRiskIndicators(risk));
			risk.setGoals(this.listRiskGoals(risk));
            risk.setProcessObjectives(this.listRiskProcessObjectives(risk));
			risk.setActivities(this.listRiskActivity(risk));
		}

		return risks;
	}

	/**
	 * Recupera riscos de uma unidade. Faz a busca dos riscos em uma unidade
	 *
	 * @param Unit,
	 *              instância da unidade
	 *
	 */
	public PaginatedList<Risk> filterRisks(Long planRiskId, RiskFilterParams filters, DefaultParams params) {
		PaginatedList<Risk> results = new PaginatedList<>();

		Criteria criteria = this.dao.newCriteria(Risk.class);
		Criteria count = this.dao.newCriteria(Risk.class);

		// deleted
		if (!filters.filteringByDeleted()) {
			criteria.add(Restrictions.eq(DELETED_PROPERTY, false));
			count.add(Restrictions.eq(DELETED_PROPERTY, false));
		}

		// archived
		if (filters.getArchived() != null) {
			criteria.add(Restrictions.eq(ARCHIVED, filters.getArchived()));
			count.add(Restrictions.eq(ARCHIVED, filters.getArchived()));
		} else {
			criteria.add(Restrictions.eq(ARCHIVED, false));
			count.add(Restrictions.eq(ARCHIVED, false));
		}

		// imported
		if (filters.getImported() != null) {
			criteria.add(Restrictions.eq(IMPORTED, filters.getImported()));
			count.add(Restrictions.eq(IMPORTED, filters.getImported()));
		} else {
			criteria.add(Restrictions.eq(IMPORTED, false));
			count.add(Restrictions.eq(IMPORTED, false));
		}

		criteria.createAlias("unit", "unit", JoinType.INNER_JOIN)
				.createAlias("unit.planRisk", "planRisk", JoinType.INNER_JOIN)
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias(MANAGER, MANAGER, JoinType.LEFT_OUTER_JOIN);

		count.createAlias("unit", "unit", JoinType.INNER_JOIN)
				.createAlias("unit.planRisk", "planRisk", JoinType.INNER_JOIN)
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias(MANAGER, MANAGER, JoinType.LEFT_OUTER_JOIN)
				.setProjection(Projections.countDistinct("id"));

		List<Long> unitIds = filters.getUnitIds();
		if (unitIds == null)
			unitIds = Collections.emptyList();
		filters.setUnitIds(null);

		applyFiltersToCriteria(filters, criteria, count);

		if (!unitIds.isEmpty()) {
			var planOwner = Restrictions.and(
					Restrictions.eq("planRisk.id", planRiskId),
					Restrictions.in("unit.id", unitIds));

			List<Long> sharedIds = sharedRisksByUnit(unitIds);

			if (sharedIds != null && !sharedIds.isEmpty()) {
				var shared = Restrictions.in("id", sharedIds);
				criteria.add(Restrictions.or(planOwner, shared));
				count.add(Restrictions.or(planOwner, shared));
			} else {
				criteria.add(planOwner);
				count.add(planOwner);
			}
		} else {
			criteria.add(Restrictions.eq("planRisk.id", planRiskId));
			count.add(Restrictions.eq("planRisk.id", planRiskId));
		}

		filters.setUnitIds(unitIds);

		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((page - 1) * pageSize).setMaxResults(pageSize);

		results.setList(this.dao.findByCriteria(criteria, Risk.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}

	private List<Long> sharedRisksByUnit(List<Long> unitIds) {
		if (unitIds == null || unitIds.isEmpty())
			return Collections.emptyList();

		Criteria c = this.dao.newCriteria(RiskSharedUnit.class)
				.add(Restrictions.in("unit.id", unitIds))
				.add(Restrictions.eq("risk.deleted", false))
				.setProjection(Projections.property("risk.id"));

		@SuppressWarnings("unchecked")
		List<Long> raw = this.dao.findByCriteria(c, Long.class);
		if (raw == null)
			return Collections.emptyList();

		return new ArrayList<>(new LinkedHashSet<>(raw));
	}

	private void applyFiltersToCriteria(RiskFilterParams filters, Criteria criteria, Criteria count) {
		filterService.applyFilters(filters, criteria, count);

	}

	/**
	 * Retorna riscos de uma lista de unidades
	 *
	 * @param units lista de unidades
	 */
	@Transactional(Transactional.TxType.SUPPORTS)
	public PaginatedList<Risk> listRiskByUnitList(List<Unit> units) {
		PaginatedList<Risk> results = new PaginatedList<>();

		if (units == null || units.isEmpty()) {
			return results;
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Risk> cq = cb.createQuery(Risk.class);
		Root<Risk> root = cq.from(Risk.class);

		Predicate notDeleted = cb.equal(root.get(DELETED_PROPERTY), false);
		Predicate inUnits = root.get("unit").in(units);
		cq.select(root)
				.where(cb.and(notDeleted, inUnits));

		List<Risk> risks = em.createQuery(cq).getResultList();
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
	 *                     instância processunit
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

		Criteria criteria = this.dao.newCriteria(RiskLevel.class).add(Restrictions.eq(DELETED_PROPERTY, false))
				.add(Restrictions.eq(POLICY, policy)).add(Restrictions.eq("level", result));

		return (RiskLevel) criteria.uniqueResult();

	}

	public RiskLevel getRiskLevelByMonitor(Monitor monitor, Policy policy) {
		policy = resolvePolicy(monitor.getRisk(), policy);

		final int nL = policy.getNline();
		final int nC = policy.getNcolumn();
		final int total = nL * nC + nL + nC;

		String[][] matrix = getMatrixVector(policy);

		String prob = monitor.getProbability();
		String imp = monitor.getImpact();

		Integer linha = null;
		int headerStride = nL + 1;
		int lcount = 0;
		for (int i = 0; i < total; i += headerStride) {
			lcount++;
			if (Objects.equals(prob, matrix[i][0])) {
				linha = lcount;
				break;
			}
		}

		Integer coluna = null;
		int cstart = total - nC;
		int ccount = 0;
		for (int i = cstart; i < total; i++) {
			ccount++;
			if (Objects.equals(imp, matrix[i][0])) {
				coluna = ccount;
				break;
			}
		}

		if (linha == null || coluna == null) {
			return null;
		}

		int position = ((linha - 1) * (nC + 1)) + coluna;
		if (position < 0 || position >= total) {
			return null;
		}

		String result = matrix[position][0];

		Criteria criteria = this.dao.newCriteria(RiskLevel.class)
				.add(Restrictions.eq(DELETED_PROPERTY, false))
				.add(Restrictions.eq(POLICY, policy))
				.add(Restrictions.eq("level", result));

		return (RiskLevel) criteria.uniqueResult();
	}

	/**
	 * Resolve a política associada ao risco.
	 *
	 * @param risk   instância do risco
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
				.setMaxResults(pageSize).addOrder(Order.asc("name")).createAlias("unit", "unit")
				.createAlias(RISK_LEVEL, RISK_LEVEL)
				.add(Restrictions.eq("unit.planRisk", planRisk)).add(Restrictions.eq(DELETED_PROPERTY, false))
				.add(Restrictions.eq("riskLevel.level", level));

		Criteria counting = this.dao.newCriteria(Risk.class).createAlias("unit", "unit")
				.createAlias(RISK_LEVEL, RISK_LEVEL)
				.add(Restrictions.eq("unit.planRisk", planRisk)).add(Restrictions.eq(DELETED_PROPERTY, false))
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
				.add(Restrictions.eq("user.id", userId)).add(Restrictions.eq(DELETED_PROPERTY, false));

		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		} else {
			criteria.addOrder(Order.asc("name"));
		}
		Criteria counting = this.dao.newCriteria(Risk.class)
				.add(Restrictions.eq("user.id", userId)).add(Restrictions.eq(DELETED_PROPERTY, false))
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
	 *             instância de um risco
	 *
	 * @return PaginatedList<RiskActivity>
	 */
	public PaginatedList<RiskActivity> listActivityByRisk(Risk risk) {
		PaginatedList<RiskActivity> results = new PaginatedList<>();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RiskActivity> cq = cb.createQuery(RiskActivity.class);
		Root<RiskActivity> root = cq.from(RiskActivity.class);

		cq.select(root)
				.where(
						cb.equal(root.get(DELETED_PROPERTY), false),
						cb.equal(root.get("risk"), risk));

		List<RiskActivity> list = em.createQuery(cq).getResultList();

		CriteriaQuery<Long> countCQ = cb.createQuery(Long.class);
		Root<RiskActivity> countRoot = countCQ.from(RiskActivity.class);

		countCQ.select(cb.countDistinct(countRoot.get("id")))
				.where(
						cb.equal(countRoot.get(DELETED_PROPERTY), false),
						cb.equal(countRoot.get("risk"), risk));

		Long total = em.createQuery(countCQ).getSingleResult();

		results.setList(list);
		results.setTotal(total);
		return results;
	}

	/**
	 * Retorna os objetivos estrategicos vinculados ao risco
	 *
	 * @param risk
	 *             instância do Risk
	 * @return PaginatedList<RiskStrategy> lista de objetivos estrategicos
	 */
	public PaginatedList<RiskStrategy> listRiskStrategy(Risk risk) {
		PaginatedList<RiskStrategy> results = new PaginatedList<>();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RiskStrategy> cq = cb.createQuery(RiskStrategy.class);
		Root<RiskStrategy> root = cq.from(RiskStrategy.class);
		cq.select(root)
				.where(
						cb.equal(root.get(DELETED_PROPERTY), false),
						cb.equal(root.get("risk"), risk));

		List<RiskStrategy> list = em.createQuery(cq).getResultList();
		CriteriaQuery<Long> countCQ = cb.createQuery(Long.class);
		Root<RiskStrategy> countRoot = countCQ.from(RiskStrategy.class);

		countCQ.select(cb.countDistinct(countRoot.get("id")))
				.where(
						cb.equal(countRoot.get(DELETED_PROPERTY), false),
						cb.equal(countRoot.get("risk"), risk));

		Long total = em.createQuery(countCQ).getSingleResult();

		results.setList(list);
		results.setTotal(total);
		return results;
	}

	/**
	 * Retorna os objetivos estrategicos vinculados ao risco
	 *
	 * @param risk
	 *             instância do Risk
	 * @return PaginatedList<RiskStrategy> lista de objetivos estrategicos
	 */
	public PaginatedList<RiskStrategyBean> listReducedRiskStrategy(Risk risk) {
		PaginatedList<RiskStrategyBean> results = new PaginatedList<>();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RiskStrategyBean> cq = cb.createQuery(RiskStrategyBean.class);
		Root<RiskStrategy> root = cq.from(RiskStrategy.class);

		cq.select(cb.construct(
				RiskStrategyBean.class,
				root.get("id"),
				root.get("name"),
				root.get("structure").get("id"))).where(
						cb.equal(root.get(DELETED_PROPERTY), false),
						cb.equal(root.get("risk"), risk));

		List<RiskStrategyBean> list = em.createQuery(cq).getResultList();
		results.setList(list);
		return results;
	}

	/**
	 * Retorna os eixos tematicos vinculados ao risco
	 *
	 * @param risk
	 *             instância do Risk
	 * @return PaginatedList<RiskAxis> lista de eixos tematicos
	 */
	public PaginatedList<RiskAxis> listRiskAxis(Risk risk) {
		PaginatedList<RiskAxis> results = new PaginatedList<>();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RiskAxis> cq = cb.createQuery(RiskAxis.class);
		Root<RiskAxis> root = cq.from(RiskAxis.class);

		cq.select(root)
				.where(
						cb.equal(root.get(DELETED_PROPERTY), false),
						cb.equal(root.get("risk"), risk));

		List<RiskAxis> list = em.createQuery(cq).getResultList();
		CriteriaQuery<Long> countCQ = cb.createQuery(Long.class);
		Root<RiskAxis> countRoot = countCQ.from(RiskAxis.class);

		countCQ.select(cb.countDistinct(countRoot.get("id")))
				.where(
						cb.equal(countRoot.get(DELETED_PROPERTY), false),
						cb.equal(countRoot.get("risk"), risk));

		Long total = em.createQuery(countCQ).getSingleResult();

		results.setList(list);
		results.setTotal(total);
		return results;
	}

	/**
	 * Retorna os indicadores vinculados ao risco
	 *
	 * @param risk
	 *             instância do Risk
	 * @return PaginatedList<RiskIndicator> lista de indicadores
	 */
	public PaginatedList<RiskIndicator> listRiskIndicators(Risk risk) {
		PaginatedList<RiskIndicator> results = new PaginatedList<>();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RiskIndicator> cq = cb.createQuery(RiskIndicator.class);
		Root<RiskIndicator> root = cq.from(RiskIndicator.class);

		cq.select(root)
				.where(
						cb.equal(root.get(DELETED_PROPERTY), false),
						cb.equal(root.get("risk"), risk));

		List<RiskIndicator> list = em.createQuery(cq).getResultList();

		CriteriaQuery<Long> countCQ = cb.createQuery(Long.class);
		Root<RiskIndicator> countRoot = countCQ.from(RiskIndicator.class);

		countCQ.select(cb.countDistinct(countRoot.get("id")))
				.where(
						cb.equal(countRoot.get(DELETED_PROPERTY), false),
						cb.equal(countRoot.get("risk"), risk));
		Long total = em.createQuery(countCQ).getSingleResult();

		results.setList(list);
		results.setTotal(total);
		return results;
	}

	/**
	 * Retorna as metas vinculadas ao risco
	 *
	 * @param risk
	 *             instância do Risk
	 * @return PaginatedList<RiskGoal> lista de metas
	 */
	public PaginatedList<RiskGoal> listRiskGoals(Risk risk) {
		PaginatedList<RiskGoal> results = new PaginatedList<>();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RiskGoal> cq = cb.createQuery(RiskGoal.class);
		Root<RiskGoal> root = cq.from(RiskGoal.class);

		cq.select(root)
				.where(
						cb.equal(root.get(DELETED_PROPERTY), false),
						cb.equal(root.get("risk"), risk));

		List<RiskGoal> list = em.createQuery(cq).getResultList();
		CriteriaQuery<Long> countCQ = cb.createQuery(Long.class);
		Root<RiskGoal> countRoot = countCQ.from(RiskGoal.class);

		countCQ.select(cb.countDistinct(countRoot.get("id")))
				.where(
						cb.equal(countRoot.get(DELETED_PROPERTY), false),
						cb.equal(countRoot.get("risk"), risk));
		Long total = em.createQuery(countCQ).getSingleResult();

		results.setList(list);
		results.setTotal(total);
		return results;
	}

	/**
	 * Retorna as atividades do processo do risco
	 *
	 * @param risk
	 *             instância do Risk
	 * @return PaginatedList<RiskActivity> lista de atividades do processo
	 */
	public PaginatedList<RiskActivity> listRiskActivity(Risk risk) {
		PaginatedList<RiskActivity> results = new PaginatedList<>();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RiskActivity> cq = cb.createQuery(RiskActivity.class);
		Root<RiskActivity> root = cq.from(RiskActivity.class);

		cq.select(root)
				.where(
						cb.equal(root.get(DELETED_PROPERTY), false),
						cb.equal(root.get("risk"), risk));

		List<RiskActivity> list = em.createQuery(cq).getResultList();
		CriteriaQuery<Long> countCQ = cb.createQuery(Long.class);
		Root<RiskActivity> countRoot = countCQ.from(RiskActivity.class);

		countCQ.select(cb.countDistinct(countRoot.get("id")))
				.where(
						cb.equal(countRoot.get(DELETED_PROPERTY), false),
						cb.equal(countRoot.get("risk"), risk));
		Long total = em.createQuery(countCQ).getSingleResult();

		results.setList(list);
		results.setTotal(total);
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
				.add(Restrictions.eq(DELETED_PROPERTY, false))
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
	 *                instância de um processo
	 * @param boolean
	 *                booleano para verificação
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
	 *                instância de um processo
	 * @param boolean
	 *                booleano para verificação
	 */
	public boolean hasLinkedRiskActivity(Process process) {
		Criteria criteria = this.dao.newCriteria(RiskActivity.class).add(Restrictions.eq(DELETED_PROPERTY, false))
				.add(Restrictions.eq("process", process));
		criteria.setMaxResults(1);
		return criteria.uniqueResult() != null;
	}

	/**
	 * Retorna se o usuário tem ou não a permissao de editar um risco
	 *
	 * @param userSession
	 *                    sessão do usuario
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
	 *                    sessão do usuario
	 *
	 * @return boolean se o usuário tem permissão de editar algum item do risco
	 */
	public boolean hasPermissionToEditRiskItems(UserSession userSession) {
		return userSession.getAccessLevel() >= AccessLevels.MANAGER.getLevel() ||
				userSession.getPermissions().contains(ManageRiskItemsPermission.class.getCanonicalName());
	}

	public void sendUserLinkedToRiskNotification(Risk risk, Unit unit, User user, String baseUrl)
			throws EmailException {
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
			Risk risk, User manager, String url, String text, NotificationType notificationType) throws EmailException {
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
			throw new IllegalArgumentException(
					"A data de início do prazo de vigência não deve ser superior à data de término");
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

	private static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

    private boolean isReadyToPublish(Risk r) {
        return r.getManager() != null
            && r.getUser() != null
            && !isNullOrEmpty(r.getProbability())
            && !isNullOrEmpty(r.getImpact())
            && !isNullOrEmpty(r.getTipology());
    }

	public void updateRisk(Risk risk) throws EmailException {
		Risk existent = this.exists(risk.getId(), Risk.class);
		Unit unit = this.exists(risk.getUnit().getId(), Unit.class);
		User user = this.exists(risk.getUser().getId(), User.class);
		User manager = this.exists(risk.getManager().getId(), User.class);

		RiskLevel riskLevel = this.getRiskLevelByRisk(risk, null);

		this.validateRisk(risk, riskLevel);

        if (risk.getTipology() == null || risk.getTipology().isEmpty()) {
            throw new IllegalStateException("A tipologia do risco é obrigatória.");
        }

		boolean userChanged = !existent.getUser().equals(user);
		boolean managerChanged = existent.getManager() == null || !existent.getManager().equals(manager);
		boolean hasPermission = this.hasPermissionToEditRisk(userSession);
		boolean isResponsible = userSession.getUser().getId().equals(existent.getUser().getId());

		if (!hasPermission && !isResponsible) {
			throw new IllegalStateException("Usuário não tem permissão para realizar esta edição.");
		}

		risk.setAxesDescriptions(extractAxisNames(risk));
		risk.setStrategiesDescriptions(extractStrategyNames(risk));
		risk.setIndicatorsDescriptions(extractIndicatorNames(risk));
		risk.setGoalsDescriptions(extractGoalNames(risk));
		risk.setProcessObjectivesDescriptions(extractProcessObjectiveDescriptions(risk));

		riskEditHistoryBS.saveRiskChanges(
				existent,
				risk,
				unit, user, manager,
				riskLevel,
				hasPermission,
				userSession.getUser());

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
		existent.setLevel(risk.getLevel());
		existent.setProcessObjectives(risk.getProcessObjectives());
		existent.setArchived(risk.isArchived());
		if (existent.isImported() && this.isReadyToPublish(existent)) {
            existent.setImported(false);
        }
		existent.setRiskLevel(riskLevel);
		existent.setExistentControllerId(risk.getExistentControllerId());
		existent.setExecutionControllerId(risk.getExecutionControllerId());
        if (Objects.equals(risk.getResponse(), existent.getResponse())) {
            existent.setSharedUnits(listSharedUnits(risk));
        }
		existent.setResponse(risk.getResponse());

		if (risk.getSharedUnits() != null && risk.getUnit() != null && risk.getUnit().getId() != null) {
			Set<Long> oldSharedIds = Optional.ofNullable(existent.getSharedUnits())
					.orElse(Collections.emptyList())
					.stream()
					.filter(Objects::nonNull)
					.map(Unit::getId)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());

			for (Unit unit1 : risk.getSharedUnits()) {
				if (unit1 == null || unit1.getId() == null) {
					continue;
				}
				boolean isOwnerUnit = Objects.equals(risk.getUnit().getId(), unit1.getId());
				boolean isNewShare = !oldSharedIds.contains(unit1.getId());

				if (!isOwnerUnit && isNewShare) {
					notifySharedRisk(risk, unit1, unit1.getUser());
				}
			}
		}

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
		Date now = new Date();
		for (Risk risk : risks) {
			if (risk.getUnit().getPlanRisk().getValidityEnd().before(now)) {
				risk.setMonitoringState(RiskState.EXPIRED_PLAN.getId());
			} else {

				Monitor monitor = monitorBS.lastMonitorbyRisk(risk.getId());
				Date beginDate = monitor != null ? monitor.getBegin() : risk.getBegin();
				int monitoringState = RiskStateChecker.riskState(risk.getPeriodicity(), beginDate);
				risk.setMonitoringState(monitoringState);
			}
		}
	}

	private List<String> extractAxisNames(Risk risk) {
		if (risk.getAxes() == null || risk.getAxes().getList() == null)
			return Collections.emptyList();
		List<String> names = new ArrayList<>();
		for (RiskAxis axis : risk.getAxes().getList()) {
			if (axis.getStructure() == null || axis.getStructure().getId() == null)
				continue;
			StructureLevelInstance s = this.dao.exists(axis.getStructure().getId(), StructureLevelInstance.class);
			if (s != null && s.getName() != null)
				names.add(s.getName().trim());
		}
		return names;
	}

	private List<String> extractStrategyNames(Risk risk) {
		if (risk.getStrategies() == null || risk.getStrategies().getList() == null)
			return Collections.emptyList();
		List<String> names = new ArrayList<>();
		for (RiskStrategy st : risk.getStrategies().getList()) {
			if (st.getStructure() == null || st.getStructure().getId() == null)
				continue;
			StructureLevelInstance s = this.dao.exists(st.getStructure().getId(), StructureLevelInstance.class);
			if (s != null && s.getName() != null)
				names.add(s.getName().trim());
		}
		return names;
	}

	private List<String> extractIndicatorNames(Risk risk) {
		if (risk.getIndicators() == null || risk.getIndicators().getList() == null)
			return Collections.emptyList();
		List<String> names = new ArrayList<>();
		for (RiskIndicator ind : risk.getIndicators().getList()) {
			if (ind.getStructure() == null || ind.getStructure().getId() == null)
				continue;
			StructureLevelInstance s = this.dao.exists(ind.getStructure().getId(), StructureLevelInstance.class);
			if (s != null && s.getName() != null)
				names.add(s.getName().trim());
		}
		return names;
	}

	private List<String> extractGoalNames(Risk risk) {
		if (risk.getGoals() == null || risk.getGoals().getList() == null)
			return Collections.emptyList();
		List<String> names = new ArrayList<>();
		for (RiskGoal g : risk.getGoals().getList()) {
			if (g.getStructure() == null || g.getStructure().getId() == null)
				continue;
			StructureLevelInstance s = this.dao.exists(g.getStructure().getId(), StructureLevelInstance.class);
			if (s != null && s.getName() != null)
				names.add(s.getName().trim());
		}
		return names;
	}

	private List<String> extractProcessObjectiveDescriptions(Risk risk) {
		if (risk.getProcessObjectives() == null)
			return Collections.emptyList();
		List<String> descr = new ArrayList<>();
		for (RiskProcessObjective rpo : risk.getProcessObjectives()) {
			if (rpo == null || rpo.getId() == null)
				continue;
			ProcessObjective po = processBS.retrieveProcessObjectiveById(rpo.getId());
			if (po != null && po.getDescription() != null)
				descr.add(po.getDescription().trim());
		}
		return descr;
	}

	@Transactional
	public Date getLastMonitoringDate(Risk risk) {
		TypedQuery<Date> q = em.createQuery(
				"SELECT MAX(m.begin) " +
						"FROM " + Monitor.TABLE + " m " +
						"WHERE m.risk = :risk AND m.deleted = false",
				Date.class);
		q.setParameter("risk", risk);
		return q.getSingleResult();
	}
}
