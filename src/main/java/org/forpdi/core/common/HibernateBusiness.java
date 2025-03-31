package org.forpdi.core.common;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class HibernateBusiness {

	protected final Logger LOGGER;
	
	@Autowired
	protected HibernateDAO dao;

	@Autowired
	protected HttpServletRequest request;
	
	public HibernateBusiness() {
		LOGGER = LoggerFactory.getLogger(this.getClass());
	}
	
	public <E extends Serializable> E exists(Serializable id, Class<E> clazz) {
		return this.dao.exists(id, clazz);
	}
	
	public <E extends Serializable> void persist(E model) {
		this.dao.persist(model);
	}
	
	public <E extends Serializable> void remove(E model) {
		this.dao.delete(model);
	}
}
