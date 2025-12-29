package org.forrisco.core.plan;


import java.util.List;

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
import org.forpdi.security.auth.UserSession;
import org.forrisco.core.policy.Policy;
import org.forrisco.core.policy.PolicyBS;
import org.forpdi.core.common.Order;
import org.forpdi.core.common.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlanRiskBS extends HibernateBusiness {

	@Autowired private PlanRiskHistoryBS planRiskHistoryBS;
	@Autowired private PolicyBS policyBS;
	
	public PlanRisk retrieveById(Long id) {
		Criteria criteria  = this.dao.newCriteria(PlanRisk.class)
			.add(Restrictions.eq("id",id))
			.add(Restrictions.eq("deleted", false));

		return (PlanRisk) criteria.uniqueResult();
	}

	public PlanRisk retrieveByName(String name, Long planRiskId) {
		Criteria criteria  = this.dao.newCriteria(PlanRisk.class)
			.add(Restrictions.eq("name", name))
			.add(Restrictions.eq("deleted", false));
		if (planRiskId != null) {
			criteria.add(Restrictions.ne("id", planRiskId));
		}
		return (PlanRisk) criteria.uniqueResult();
	}

	/**
	 * Salva no banco de dados um novo item
	 * 
	 * @param PlanRisk,
	 *            instância do item a ser salvo
	 */
	public void save(PlanRisk planRisk) {
		sanitizePlanRiskFields(planRisk);
		planRisk.setId(null);
		planRisk.setDeleted(false);
		this.persist(planRisk);
	}

	private void sanitizePlanRiskFields(PlanRisk planRisk) {
		planRisk.setName(SanitizeUtil.sanitize(planRisk.getName()));
		planRisk.setDescription(SanitizeUtil.sanitize(planRisk.getDescription()));
	}
	
	/**
	 * Lista os planos de risco
	 * 
	 * @param Company
	 * @param Integer
	 * 
	 * @return
	 */
	public PaginatedList<PlanRisk> listPlanRisk(Company company, DefaultParams params) {
		
		PaginatedList<PlanRisk> results = new PaginatedList<PlanRisk>();
		
		Criteria criteria = this.dao.newCriteria(PlanRisk.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("policy", "policy", JoinType.INNER_JOIN)
				.add(Restrictions.eq("policy.company", company));
		
		Criteria count = this.dao.newCriteria(PlanRisk.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("policy", "policy", JoinType.INNER_JOIN)
				.add(Restrictions.eq("policy.company", company))
				.setProjection(Projections.countDistinct("id"));
		
		if (params.hasTerm()) {
			String term = params.getTerm();
			Criterion name = Restrictions.like("name", "%" + term + "%").ignoreCase();
			Criterion policy = Restrictions.like("policy.name", "%" + term + "%").ignoreCase();
			Criterion validityBegin = Restrictions.like("validityBegin", "%" + DateUtil.formatSearchDate(term) + "%").ignoreCase();
			Criterion validityEnd = Restrictions.like("validityEnd", "%" + DateUtil.formatSearchDate(term) + "%").ignoreCase();

			criteria.add(Restrictions.or(name, policy, validityBegin, validityEnd));
			count.add(Restrictions.or(name, policy, validityBegin, validityEnd));
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

		results.setList(this.dao.findByCriteria(criteria, PlanRisk.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	public List<ItemToSelect> listPlanRisksToSelect(Company company, Long policyId) {
		Criteria criteria = this.dao.newCriteria(PlanRisk.class)
				.createAlias("policy", "policy")
				.add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("archived", false))
				.add(Restrictions.eq("policy.company", company))
				.addOrder(Order.asc("name"));

		if (policyId != null) {
			criteria.add(Restrictions.eq("policy.id", policyId));
		}
		
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("id"), "id");
		projList.add(Projections.property("name"), "name");
		
		criteria.setProjection(projList);
		criteria.setResultTransformer(ItemToSelect.class);

		List<ItemToSelect> result = this.dao.findByCriteria(criteria, ItemToSelect.class);
		
		return result;
	}
	
	/**
	 * 
	 * @param plan
	 * @return
	 */
	public Policy listPolicybyPlanRisk (PlanRisk plan) {
			
		Criteria criteria = this.dao.newCriteria(Policy.class)
				.add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("id",plan.getPolicy().getId()));		
		
		Policy result = (Policy) criteria.uniqueResult();
		return result;
	}

	public void delete(PlanRisk planRisk) {
		planRisk.setDeleted(true);
		this.persist(planRisk);
	}

	@Autowired
	private UserBS userBS;

public PlanRisk updatePlanRisk(PlanRisk existent, PlanRisk incoming, UserSession userSession) {
    Long userId = userSession.getAuthData().getId();
    User responsible = this.userBS.exists(userId, User.class);

    PlanRisk merged = new PlanRisk();
    merged.setId(existent.getId());

    merged.setName(incoming.getName() != null ? incoming.getName() : existent.getName());
    merged.setDescription(incoming.getDescription() != null ? incoming.getDescription() : existent.getDescription());
    merged.setValidityBegin(incoming.getValidityBegin() != null ? incoming.getValidityBegin() : existent.getValidityBegin());
    merged.setValidityEnd(incoming.getValidityEnd() != null ? incoming.getValidityEnd() : existent.getValidityEnd());

    if (incoming.getPolicy() != null && incoming.getPolicy().getId() != null) {
        merged.setPolicy(this.policyBS.exists(incoming.getPolicy().getId(), Policy.class));
    } else {
        merged.setPolicy(existent.getPolicy());
    }

    this.planRiskHistoryBS.savePlanChanges(existent, merged, responsible);

    existent.setName(merged.getName());
    existent.setDescription(merged.getDescription());
    existent.setValidityBegin(merged.getValidityBegin());
    existent.setValidityEnd(merged.getValidityEnd());
    existent.setPolicy(merged.getPolicy());

    this.persist(existent);
    return existent;
}

}
