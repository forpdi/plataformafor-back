package org.forrisco.core.policy;


import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ItemToSelect;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.ProjectionList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.Company;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.DateUtil;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.core.utils.Util;
import org.forrisco.core.item.FieldItem;
import org.forrisco.core.item.Item;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.risk.RiskLevel;
import org.forpdi.core.common.Order;
import org.forpdi.security.auth.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author Matheus Nascimento
 */
@Service
public class PolicyBS extends HibernateBusiness {

	private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Sao_Paulo");

	@Autowired
	private PolicyHistoryBS policyHistoryBS;

	@Autowired
	private UserBS userBS;

	/**
	 * Salva no banco de dados uma nova política
	 *
	 * @param policy,
	 *            instância da política a ser salva
	 */
	public void save(Policy policy) {
		policy.setDeleted(false);
		this.persist(policy);
	}

	/**
	 * Salva no banco de dados um item
	 *
	 * @param item,
	 *            instância da política a ser salva
	 */
	public void save(Item item) {
		this.persist(item);
	}


	/**
	 * Salva no banco de dados um campo de item
	 *
	 * @param Fielditem,
	 *            instância da política a ser salva
	 */
	public void save(FieldItem it) {
		this.persist(it);
	}

	/**
	 * Deleta do banco de dados uma política
	 *
	 * @param Policy,
	 *            política a ser deletada
	 */
	public void delete(Policy policy) {
		policy.setDeleted(true);
		this.persist(policy);
	}

	/**
	 * Lista as políticas de uma instituição.
	 * @param company
	 * 			Companhia da qual se deseja obter aspolíticas.
	 * @param archived
	 * 			Filtro para arquivado ou não (true ou false).
	 * @param page
	 * 			Número da página da lista a ser acessada.
	 * @return PaginatedList<Policy>
	 * 			Lista de políticas.
	 */
	public PaginatedList<Policy> listPolicies(Company company, boolean archived, DefaultParams params) {
		PaginatedList<Policy> results = new PaginatedList<Policy>();
		Criteria criteria = this.dao.newCriteria(Policy.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("archived", archived))
				.add(Restrictions.eq("company", company));

		Criteria count = this.dao.newCriteria(Policy.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("archived", archived))
				.add(Restrictions.eq("company", company))
				.setProjection(Projections.countDistinct("id"));

		if (params.hasTerm()) {
			String term = params.getTerm();
			Criterion name = Restrictions.like("name", "%" + term + "%").ignoreCase();
			Criterion validityBegin = Restrictions.like("validityBegin", "%" + DateUtil.formatSearchDate(term) + "%").ignoreCase();
			Criterion validityEnd = Restrictions.like("validityEnd", "%" + DateUtil.formatSearchDate(term) + "%").ignoreCase();

			criteria.add(Restrictions.or(name, validityBegin, validityEnd));
			count.add(Restrictions.or(name, validityBegin, validityEnd));
		}

		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		} else {
			criteria.addOrder(Order.asc("id"));
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((page-1) * pageSize)
			.setMaxResults(pageSize);

		results.setList(this.dao.findByCriteria(criteria, Policy.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}

	public List<ItemToSelect> listPoliciesToSelect(Company company) {
		Criteria criteria = this.dao.newCriteria(Policy.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("archived", false))
				.add(Restrictions.eq("company", company))
				.addOrder(Order.asc("name"));

		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("id"), "id");
		projList.add(Projections.property("name"), "name");

		criteria.setProjection(projList);
		criteria.setResultTransformer(ItemToSelect.class);

		return this.dao.findByCriteria(criteria, ItemToSelect.class);
	}

	public List<ItemToSelect> listPoliciesToSelectStillValid(Company company) {
		ZoneId zone = ZoneId.of("America/Sao_Paulo");
		Date hoje = Date.from(LocalDate.now(zone).atStartOfDay(zone).toInstant());

		Criteria criteria = this.dao.newCriteria(Policy.class)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.eq("archived", false))
			.add(Restrictions.eq("company", company))
			.add(
				Restrictions.or(
					Restrictions.isNull("validityEnd"),
					Restrictions.ge("validityEnd", hoje)
				)
			)
			.addOrder(Order.asc("name"));

		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("id"), "id");
		projList.add(Projections.property("name"), "name");

		criteria.setProjection(projList);
		criteria.setResultTransformer(ItemToSelect.class);

		return this.dao.findByCriteria(criteria, ItemToSelect.class);
	}

	public PaginatedList<Policy> listPoliciesHasPlans(PaginatedList<Policy> policies) {
		if (policies.getList() != null) {
			for(Policy policy : policies.getList()) {
				policy.setHasLinkedPlans(this.hasLinkedPlans(policy));
			}
		}

		return policies;
	}

	/**
	 * Lista os planos com esta política.
	 *
	 * @param Policy
	 * 			política da qual se deseja obter os planos.
	 *
	 * @return PaginatedList<PlanRisk>
	 * 			Lista os planos de risco.
	 */
	public  PaginatedList<PlanRisk> listPlanbyPolicy(Policy policy) {
		PaginatedList<PlanRisk> results = new PaginatedList<PlanRisk>();

		Criteria criteria = this.dao.newCriteria(PlanRisk.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("policy", policy))
				.addOrder(Order.asc("name"));

		Criteria count = this.dao.newCriteria(PlanRisk.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("policy", policy))
				.setProjection(Projections.countDistinct("id"));


		results.setList(this.dao.findByCriteria(criteria, PlanRisk.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}

	public boolean hasLinkedPlans(Policy policy) {
		Criteria criteria = this.dao.newCriteria(PlanRisk.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("policy", policy));
		criteria.setMaxResults(1);

		return criteria.uniqueResult() != null;
	}

	public void validateDates(Policy policy) {
		if ((policy.getValidityBegin() == null && policy.getValidityEnd() != null) ||
				(policy.getValidityEnd() == null && policy.getValidityBegin() != null)) {
			throw new IllegalArgumentException("Não é permitido preencher somente uma das datas do prazo de vigência");
		}

		if (policy.getValidityBegin() != null && policy.getValidityEnd() != null &&
				policy.getValidityEnd().before(policy.getValidityBegin())) {
			throw new IllegalArgumentException("A data de início do prazo de vigência não deve ser superior à data de término");
		}
	}

	public void validateEndDateChange(Policy policy, Date newValidityEnd) {
		if (newValidityEnd == null) {
			throw new IllegalArgumentException("A nova data fim da vigência é obrigatória.");
		}

		LocalDate newEndDate = this.toLocalDate(newValidityEnd);
		LocalDate today = LocalDate.now(DEFAULT_ZONE);

		if (newEndDate.isBefore(today)) {
			throw new IllegalArgumentException("A data fim deve ser maior ou igual à data atual.");
		}
	}

	public Policy updateValidityEnd(Policy policy, Date newValidityEnd, UserSession userSession) {
		this.validateEndDateChange(policy, newValidityEnd);

		User responsible = this.userBS.exists(userSession.getAuthData().getId(), User.class);

		LocalDate normalizedEndDate = this.toLocalDate(newValidityEnd);
		Date normalizedEnd = Date.from(normalizedEndDate.atStartOfDay(DEFAULT_ZONE).toInstant());

		this.policyHistoryBS.saveValidityChange(policy, policy.getValidityEnd(), normalizedEnd, responsible);

		policy.setValidityEnd(normalizedEnd);
		this.persist(policy);

		return policy;
	}

	private Date findLatestPlanValidityEnd(Policy policy) {
		Criteria criteria = this.dao.newCriteria(PlanRisk.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("policy", policy))
				.setProjection(Projections.max("validityEnd"));
		return (Date) criteria.uniqueResult();
	}

	private LocalDate toLocalDate(Date date) {
		if (date == null) {
			return null;
		}
		// java.sql.Date#toInstant throws UnsupportedOperationException; handle both types safely
		if (date instanceof java.sql.Date sqlDate) {
			return sqlDate.toLocalDate();
		}
		/*
		 * Jackson converte datas simples ("dd/MM/yyyy") para java.util.Date no fuso UTC
		 * quando o servidor roda em UTC. Se convertermos usando o fuso DEFAULT_ZONE
		 * (America/Sao_Paulo) podemos retroceder um dia. Forçamos UTC aqui para preservar
		 * o dia informado pelo usuário.
		 */
		return date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
	}

	/**
	 * Mapeia niveis de risco pelo nome do nivel.
	 *
	 * @param Policy
	 * 			política da qual se deseja obter os planos.
	 *
	 * @return Map
	 */
	public Map<String, RiskLevel> getRiskLevelsMap(Policy policy) {
		List<RiskLevel> riskLevels = this.listRiskLevelbyPolicy(policy).getList();

		return Util.generateMap(riskLevels, rl -> rl.getLevel());
	}

	/**
	 * Lista os niveis de risco com esta política.
	 *
	 * @param Policy
	 * 			política da qual se deseja obter os planos.
	 *
	 * @return PaginatedList<RiskLevel>
	 * 			Lista os graus de risco desta política.
	 */
	public PaginatedList<RiskLevel> listRiskLevelbyPolicy(Policy policy) {

		PaginatedList<RiskLevel> results = new PaginatedList<RiskLevel>();

		Criteria criteria = this.dao.newCriteria(RiskLevel.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("policy", policy))
				.addOrder(Order.asc("id"));

		Criteria count = this.dao.newCriteria(RiskLevel.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("policy", policy))
				.setProjection(Projections.countDistinct("id"));


		results.setList(this.dao.findByCriteria(criteria, RiskLevel.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
}
