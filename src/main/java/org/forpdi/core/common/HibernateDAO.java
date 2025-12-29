package org.forpdi.core.common;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

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
		Serializable id = extractId(entity);
		if (id == null) {
			entityManager.persist(entity);
		} else {
			entityManager.merge(entity);
		}
	}

	@Override
	@Transactional
	public <E extends Serializable> E merge(E entity) {
		return entityManager.merge(entity);
	}

	@Override
	@Transactional
	public void delete(Serializable entity) {
		Object managedEntity = this.entityManager.merge(entity);
		this.entityManager.remove(managedEntity);
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

	private Serializable extractId(Serializable entity) {
		try {
			Method getter = entity.getClass().getMethod("getId");
			Object value = getter.invoke(entity);
			if (value instanceof Serializable) {
				return (Serializable) value;
			}
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	@Transactional
    public void flush() {
        entityManager.flush();
    }

}