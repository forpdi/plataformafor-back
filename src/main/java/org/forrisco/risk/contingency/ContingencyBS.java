package org.forrisco.risk.contingency;

import java.util.ArrayList;
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
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.utils.DateUtil;
import org.forrisco.risk.Risk;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ContingencyBS extends HibernateBusiness {
	@Autowired
	private EntityManager entityManager;

	/**
	 * Salva no banco de dados contigenciamento
	 * 
	 * @param Contigency,
	 *            instância de um contigenciamento a ser salvo
	 */
	public void saveContingency(Contingency contingency) {
		contingency.setDeleted(false);
		this.persist(contingency);
	}

	/**
	 * Deleta do banco de dados um Contingencye
	 * 
	 * @param Contingency,
	 *            instância do Contingencye a ser deletado
	 */
	public void delete(Contingency contingency) {
		contingency.setDeleted(true);
		this.persist(contingency);
	}
	
	/**
	 * Retorna os contingenciamentos a partir de um risco
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<Contingency>
	 */
	public PaginatedList<Contingency> listContingenciesByRisk(Risk risk) {
		return listContingenciesByRisk(risk, -1);
	}
	
	/**
	 * Retorna os contingenciamentos a partir de um risco
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<Contingency>
	 */
	public PaginatedList<Contingency> listContingenciesByRisk(Risk risk, Integer selectedYear) {
		PaginatedList<Contingency> results = new PaginatedList<Contingency>();

		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Contingency> cq = builder.createQuery(Contingency.class);
		Root<Contingency> root = cq.from(Contingency.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get("deleted"), false));
		predicates.add(builder.equal(root.get("risk"), risk));
		
		if (selectedYear != -1) {
			Expression<Integer> yearExpression = builder.function("year", Integer.class, root.get("begin"));
			predicates.add(builder.equal(yearExpression, selectedYear));
		}
		
		cq.where(predicates.toArray(new Predicate[] {}));

		List<Contingency> list = entityManager.createQuery(cq).getResultList();
		
		results.setList(list);	
		results.setTotal((long) list.size());

		return results;		
	}
	
	/**
	 * Retorna os contingenciamentos a partir de um risco com paginacao
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<Contingency>
	 */
	public PaginatedList<Contingency> listContingenciesByRisk(Risk risk, DefaultParams params) {

		PaginatedList<Contingency> results = new PaginatedList<Contingency>();

		Criteria criteria = this.dao.newCriteria(Contingency.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("manager", "manager", JoinType.LEFT_OUTER_JOIN)
				.add(Restrictions.eq("risk", risk));

		Criteria count = this.dao.newCriteria(Contingency.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("manager", "manager", JoinType.LEFT_OUTER_JOIN)
				.add(Restrictions.eq("risk", risk))
				.setProjection(Projections.countDistinct("id"));
		
		if (params.hasTerm()) {
			String term = params.getTerm();
			Criterion name = Restrictions.like("action", "%" + term + "%").ignoreCase();
			Criterion responsible = Restrictions.like("user.name", "%" + term + "%").ignoreCase();
			Criterion manager = Restrictions.like("manager.name", "%" + term + "%").ignoreCase();
			Criterion validityBegin = Restrictions.like("validityBegin", "%" + DateUtil.formatSearchDate(term) + "%").ignoreCase();
			Criterion validityEnd = Restrictions.like("validityEnd", "%" + DateUtil.formatSearchDate(term) + "%").ignoreCase();

			criteria.add(Restrictions.or(name, responsible, manager, validityBegin, validityEnd));
			count.add(Restrictions.or(name, responsible, manager, validityBegin, validityEnd));
		}
		
		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((page-1) * pageSize)
			.setMaxResults(pageSize);

		results.setList(this.dao.findByCriteria(criteria, Contingency.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	/**
	 * Retorna um contingenciamento
	 * 
	 * @param id
	 *            Id do contingenciamento
	 */
	public Contingency findByContingencyId(Long id) {
		Criteria criteria = this.dao.newCriteria(Contingency.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("id", id));

		return (Contingency) criteria.uniqueResult();
	}
}
