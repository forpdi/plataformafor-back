package org.forpdi.core.common;

import java.io.Serializable;

public interface DAO {

	public void persist(Serializable entity);
	public <E extends Serializable> E merge(E entity);
	public void delete(Serializable entity);
	public <E extends Serializable> E exists(Serializable id, Class<E> clazz);
}
