package org.forpdi.core.common;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;

@Deprecated
public interface Criteria {

	public Criteria setProjection(Projection projection);
	
	public Criteria add(Criterion criterion);

	@SuppressWarnings("rawtypes")
	List list() throws HibernateException;

	Object uniqueResult();

	Criteria setResultTransformer(Class<?> clazz);
	
	Criteria createAlias(String associationPath, String alias);
	
	Criteria createAlias(String associationPath, String alias, JoinType joinType);
	
	Criteria setMaxResults(int maxResults);
	
	Criteria setFirstResult(int firstResult);
	
	Criteria addOrder(Order order);
}
