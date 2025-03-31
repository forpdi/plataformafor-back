package org.forpdi.core.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.forpdi.core.common.CriteriaPropertiesContext.PropertyAlias;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;

@Deprecated
public final class CustomCriteria implements Criteria {

	private EntityManager entityManager;
	private Class<?> rootClass;
	private Class<?> resultClass;

	CriteriaBuilder builder;
	
	private List<Criterion> criterions;
	private Projection projection;
	private Integer maxResults;
	private Integer firstResult;
	private List<Order> orderList;
	
	private final LinkedHashMap<String, PropertyAlias> aliasesMap;
	
	public CustomCriteria(EntityManager entityManager, Class<?> clazz) {
		this.entityManager = entityManager;
		builder = entityManager.getCriteriaBuilder();

		rootClass = clazz;
		resultClass = clazz;
		
		criterions = new ArrayList<>();
		orderList = new LinkedList<Order>();
		
		aliasesMap = new LinkedHashMap<>();
	}

	@Override
	public Criteria setProjection(Projection projection) {
		this.projection = projection;
		return this;
	}
	
	@Override
	public Criteria add(Criterion criterion) {
		criterions.add(criterion);
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List list() throws HibernateException {
		Query query = createQuery();
		return query.getResultList();
	}

	@Override
	public Object uniqueResult() throws HibernateException {
		Query query = createQuery();
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Criteria setResultTransformer(Class<?> clazz) {
		this.resultClass = clazz;
		return this;
	}

	@Override
	public Criteria createAlias(String associationPath, String alias) {
		aliasesMap.put(alias, new PropertyAlias(associationPath, JoinType.INNER_JOIN));
		return this;
	}

	@Override
	public Criteria createAlias(String associationPath, String alias, JoinType joinType) {
		aliasesMap.put(alias, new PropertyAlias(associationPath, joinType));
		return this;
	}

	@Override
	public Criteria setMaxResults(int maxResults) {
		this.maxResults = maxResults;
		return this;
	}

	@Override
	public Criteria setFirstResult(int firstResult) {
		this.firstResult = firstResult;
		return this;
	}

	private Query createQuery() {
		CriteriaQuery<?> criteriaQuery = createCriteriaQuery();
		Root<?> root = criteriaQuery.from(rootClass);

		var propertiesContext = new CriteriaPropertiesContext(aliasesMap, root);
		
		if (projection != null) {
			Selection<?>[] selections = projection.getSelection(builder, propertiesContext);
			criteriaQuery.multiselect(selections);
		}
		
		if (!criterions.isEmpty()) {
			Predicate[] predicates = getPredicates(propertiesContext);
		    criteriaQuery.where(predicates);
		}
		
		criteriaQuery.orderBy(getOrderList(propertiesContext));
		
		Query query = entityManager.createQuery(criteriaQuery);
		
		if (maxResults != null) {
			query.setMaxResults(maxResults);
		}
		
		if (firstResult != null) {
			query.setFirstResult(firstResult);
		}
		
		return query;
	}

	@Override
	public Criteria addOrder(Order order) {
		orderList.add(order);
		return this;
	}

	private CriteriaQuery<?> createCriteriaQuery() {
		if (projection == null) {
			return builder.createQuery(resultClass);
		}

		if (projection instanceof AggregateProjection) {
			var aggregateProjection = (AggregateProjection) projection;
			return builder.createQuery(aggregateProjection.getResultClass());
		} else if (projection instanceof ProjectionList) {
			return builder.createQuery(resultClass);
		} else if (projection instanceof SimplePropertyProjection) {
			return builder.createQuery(Object.class);
		}
		
		throw new HibernateException("Unrecognized projection type: " + projection.getClass());
	}
	
	private Predicate[] getPredicates(CriteriaPropertiesContext propertiesContext) {
		Predicate[] predicates = new Predicate[criterions.size()];
		for (int i = 0; i < criterions.size(); i++) {
			Criterion criterion = criterions.get(i);
			predicates[i] = criterion.getPredicate(builder, propertiesContext);
		}

		return predicates; 
	}
	
	private List<javax.persistence.criteria.Order> getOrderList(CriteriaPropertiesContext propertiesContext) {
		List<javax.persistence.criteria.Order> criteriaOrders = new ArrayList<>(orderList.size());
		for (Order order : orderList) {
			Path<?> path = propertiesContext.getPath(order.getPropertyName());
			if (order.isAscending()) {
				criteriaOrders.add(builder.asc(path));
			} else {
				criteriaOrders.add(builder.desc(path));
			}
		}
		
		return criteriaOrders;
	}
}
