package org.forpdi.core.common;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("unchecked")
public class HibernateDAO implements DAO {
	
    @Autowired
    private EntityManager entityManager;

    @Transactional
	public void execute(TransactionalOperation operation) {
		operation.execute(this);
	}
	
	@Override
	@Transactional
	public void persist(Serializable entity) {
		entityManager.persist(entity);
	}
	
	@Override
	@Transactional
	public <E extends Serializable> E merge(E entity) {
		return entityManager.merge(entity);
	}
	
	@Override
	@Transactional
	public void delete(Serializable entity) {
		entityManager.remove(entity);
	}
	
	@Override
	public <E extends Serializable> E exists(Serializable id, Class<E> clazz) {
		if (id == null)
			return null;
		return entityManager.find(clazz, id);
	}

	@Deprecated
	public <E extends Serializable> Criteria newCriteria(Class<E> clazz) {
		return new CustomCriteria(entityManager, clazz);
	}

	@Deprecated
	public <E extends Serializable> List<E> findByCriteria(Criteria criteria, Class<E> dtoClass) {
		return criteria.list();
	}
	
	public Query newSQLQuery(String sql) {
		return entityManager.createNativeQuery(sql);
	}
	
	public static interface TransactionalOperation {
		public void execute(HibernateDAO dao) throws HibernateException;
	}
	
}
