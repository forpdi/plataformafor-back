package org.forpdi.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.CompanyDomainContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CriteriaCompanyFilter extends HibernateBusiness {
	
	@Autowired
	private CompanyDomainContext domain;
	
	public <E extends Serializable> List<E> filterAndList(Criteria criteria, Class<E> clazz){
		if(domain == null)
			return new ArrayList<E>();
		
		criteria.add(Restrictions.eq("company", domain.get().getCompany()));
		return this.dao.findByCriteria(criteria, clazz);
	}
	
	public <E extends Serializable> List<E> filterAndList(Criteria criteria, Class<E> clazz, String alias){
		if(domain == null)
			return new ArrayList<E>();
		
		criteria.add(Restrictions.eq(alias, domain.get().getCompany()));
		return this.dao.findByCriteria(criteria, clazz);
	}
	
	public <E extends Serializable> E filterAndFind(Criteria criteria){
		if(domain == null)
			return null;
		
		criteria.add(Restrictions.eq("company", domain.get().getCompany()));
		return (E) criteria.uniqueResult();
	}
	
	public <E extends Serializable> E filterAndFind(Criteria criteria, String alias){
		if(domain == null)
			return null;
		criteria.add(Restrictions.eq(alias, domain.get().getCompany()));
		return (E) criteria.uniqueResult();
	}

}
