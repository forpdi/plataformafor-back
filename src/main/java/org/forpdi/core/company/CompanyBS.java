package org.forpdi.core.company;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.Disjunction;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.utils.JsonUtil;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Service;

/**
 * @author Renato R. R. de Oliveira
 */
@Service
public class CompanyBS extends HibernateBusiness {

	/**
	 * Recupera a instância do objeto CompanyDomain que está ativo no momento
	 * 
	 * @return Domínio ativo no momento
	 */
	public CompanyDomain currentDomain() {
		try {
		Criteria criteria = this.dao.newCriteria(CompanyDomain.class)
				.add(Restrictions.eq("host", this.request.getHeader("Host")));
			return (CompanyDomain) criteria.uniqueResult();
		}catch(NullPointerException ex) {
			return null;
		}
	}

	/**
	 * Recupera uma instância do objeto CompanyDomain utilizando um host
	 * específico
	 * 
	 * @param host
	 *             à ser utilizado na query
	 * @return Domínio que utiliza o host epecificado
	 */
	public CompanyDomain retrieveByHost(String host) {
		Criteria criteria = this.dao.newCriteria(CompanyDomain.class).add(Restrictions.eq("host", host));
		return (CompanyDomain) criteria.uniqueResult();
	}
	
	/**
	 * Recupera uma instância do objeto CompanyDomain utilizando um host
	 * específico
	 * 
	 * @param host
	 *             à ser utilizado na query
	 * @return Domínio que utiliza o host epecificado
	 */
	
	public List<CompanyDomain> retrieveCompanyByDomain (Company company) {
		Criteria criteria  = this.dao.newCriteria(CompanyDomain.class).add(Restrictions.eq("company",company));
		
		List<CompanyDomain> list = this.dao.findByCriteria(criteria, CompanyDomain.class);
		return list;
		//return (CompanyDomain) criteria.uniqueResult();
	}
	
	/**
	 * Recupera uma instância do objeto CompanyDomain utilizando um id
	 * 
	 * @param id
	 *             à ser utilizado na query
	 * @return Domínio que utiliza o host epecificado
	 */
	public Company retrieveCompanyById (Long id) {
		Criteria criteria  = this.dao.newCriteria(Company.class)
			.add(Restrictions.eq("id",id))
			.add(Restrictions.eq("deleted", false));
		return (Company) criteria.uniqueResult();
	}

	public List<CompanyMessage> retrieveMessages(Company company) {
		Criteria criteria = this.dao.newCriteria(CompanyMessage.class);
		criteria.add(Restrictions.eq("company", company));
		return this.dao.findByCriteria(criteria, CompanyMessage.class);
	}
	
	public Map<String, String> retrieveMessagesOverlay(Company company) {
		List<CompanyMessage> messages = this.retrieveMessages(company);
		Map<String, String> overlay = new HashMap<String, String>();
		if (!GeneralUtils.isEmpty(messages)) {
			for (CompanyMessage message : messages) {
				overlay.put(message.getMessageKey(), message.getMessageValue());
			}
		}
		return overlay;
	}

	public void updateMessageOverlay(Company company, String key, String value) {
		Criteria criteria = dao.newCriteria(CompanyMessage.class);
		criteria.add(Restrictions.eq("company", company));
		criteria.add(Restrictions.eq("messageKey", key));
		CompanyMessage message = (CompanyMessage) criteria.uniqueResult();
		if (message == null) {
			message = new CompanyMessage();
			message.setCompany(company);
			message.setMessageKey(key);
		}
		message.setLastUpdated(new Date());
		message.setMessageValue(value);
		dao.persist(message);
	}

	/**
	 * Salva no banco de dados uma nova companhia
	 * 
	 * @param company,
	 *            instância da companhia a ser salva
	 * @return void
	 */
	public void save(Company company) {
		company.setDeleted(false);
		this.persist(company);
	}
	
	public List<Company> listAll() {
		return list(0, Integer.MAX_VALUE).getList();
	}

	/**
	 * Lista as companhias limitados a uma dada página
	 * 
	 * @param page,
	 *            número da página a ser listada
	 * @return PaginatedList<Company>, lista de companhias
	 */
	public PaginatedList<Company> list(int page, Integer pageSize) {
		PaginatedList<Company> results = new PaginatedList<Company>();
		Criteria criteria = this.dao.newCriteria(Company.class)
			.add(Restrictions.eq("deleted", false))
			.addOrder(Order.asc("name"));
		if (pageSize == null) {
			pageSize = 5;
		}
		if (page > 0) {
			criteria.setFirstResult((page-1) * pageSize)
				.setMaxResults(pageSize);
		}
		Criteria counting = this.dao.newCriteria(Company.class)
			.setProjection(Projections.countDistinct("id"))
			.add(Restrictions.eq("deleted", false));
		results.setList(this.dao.findByCriteria(criteria, Company.class));
		results.setTotal((Long) counting.uniqueResult());
		return results;
	}

	public PaginatedList<Company> search(DefaultParams params) {
	    PaginatedList<Company> results = new PaginatedList<Company>();
		
	    Criteria criteria = this.dao.newCriteria(Company.class)
	    		.createAlias("county", "county")
	    		.createAlias("county.uf", "uf")
	            .add(Restrictions.eq("deleted", false));

	    Criteria counting = this.dao.newCriteria(Company.class)
	    		.createAlias("county", "county")
	    		.createAlias("county.uf", "uf")
	            .setProjection(Projections.countDistinct("id"))
	            .add(Restrictions.eq("deleted", false));

	    if (params.hasTerm()) {
	    	String term = params.getTerm();
    		Criterion name = Restrictions.like("name", "%" + term + "%").ignoreCase();
    		Criterion countyName = Restrictions.like("county.name", "%" + term + "%").ignoreCase();
    		Criterion ufAcronym = Restrictions.like("uf.acronym", "%" + term + "%").ignoreCase();
    		Criterion initials = Restrictions.like("initials", "%" + term + "%").ignoreCase();
    		Disjunction exp = Restrictions.or(name, countyName, ufAcronym, initials);
    		
    		criteria.add(exp);
    		counting.add(exp);
	    }

		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		} else {
			criteria.addOrder(Order.asc("name"));
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((page-1) * pageSize)
			.setMaxResults(pageSize);

		results.setList(this.dao.findByCriteria(criteria, Company.class));
		results.setTotal((Long) counting.uniqueResult());
		return results;
	}
	
	public PaginatedList<CompanyDomain> searchDomain(DefaultParams params) {
		DefaultParams paramsClone = JsonUtil.jsonClone(params, DefaultParams.class);
		paramsClone.setSortedBy(null);
		PaginatedList<Company> matches = this.search(paramsClone);
		PaginatedList<CompanyDomain> results = new PaginatedList<CompanyDomain>();

		Criteria criteria = this.dao.newCriteria(Company.class)
		        .add(Restrictions.eq("deleted", false))
		        .addOrder(Order.asc("name"));
		
		if (params.hasTerm()) {
			String term = params.getTerm();
    		Criterion name = Restrictions.like("name", "%" + term + "%");
    		Criterion initials = Restrictions.like("initials", "%" + term + "%");
    		Disjunction orExp = Restrictions.or(name, initials);

    		criteria.add(orExp);
		}
		
		matches.setList(this.dao.findByCriteria(criteria, Company.class));
		results.setList(new ArrayList<CompanyDomain>());
		results.setTotal((long) 0);
		
		if (matches.getList().size() > 0) {
			List<CompanyDomain> domains = new ArrayList<CompanyDomain>();
			
			Criteria id = this.dao.newCriteria(CompanyDomain.class).add(Restrictions.in("company", matches.getList()));
			Criteria counting = this.dao.newCriteria(CompanyDomain.class)
					.add(Restrictions.in("company", matches.getList()))
					.setProjection(Projections.countDistinct("id"));
			
			
			int page = params.getPage();
			int pageSize = params.getPageSize();
			id.setFirstResult((page-1) * pageSize)
				.setMaxResults(pageSize);

			if (params.isSorting()) {
				id.addOrder(params.getSortOrder());
			}

			domains.addAll(this.dao.findByCriteria(id, CompanyDomain.class));
			results.setList(domains);
			results.setTotal((Long) counting.uniqueResult());
		}
		
		return results;
	}
	
	public Boolean alreadyExistsURL(CompanyDomain company) {
		Criteria criteria = this.dao.newCriteria(CompanyDomain.class);
		
		criteria
			.add(Restrictions.like("baseUrl", company.getBaseUrl() + "%").ignoreCase())
			.setMaxResults(1);
		
		if (company.getId() != null) {
			criteria.add(Restrictions.ne("id", company.getId()));
		}
		
		return criteria.uniqueResult() != null;
	}
}