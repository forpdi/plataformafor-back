package org.forpdi.core.communication;

import java.util.Date;
import java.util.List;

import org.forpdi.core.common.Conjunction;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.Disjunction;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Service;


/**
 * 
 */
@Service
public class CommunicationBS extends HibernateBusiness {

	/**
	 * Salvar um novo comunicado.
	 * 
	 * @param communicationHistory
	 *            comunicado que será salvo.
	 * 
	 * @return communicationHistory Comunicado que foi salvo.
	 */
	public Communication saveNewCommunication(Communication communicationHistory) {
		communicationHistory.setLastModification(new Date());
		communicationHistory.setShowPopup(true);
		communicationHistory.setDeleted(false);
		this.persist(communicationHistory);
		
		return communicationHistory;
	}
	
	/**
	 * Recupera uma instância do objeto Communication utilizando um id
	 * 
	 * @param id
	 *             à ser utilizado na query
	 * @return Comunicado que utiliza o id epecificado
	 */
	public Communication retrieveCommunicationById (Long id) {
		Criteria criteria  = this.dao.newCriteria(Communication.class)
			.add(Restrictions.eq("id",id))
			.add(Restrictions.eq("deleted", false));
		return (Communication) criteria.uniqueResult();
	}
	
	/**
	 * Retorna uma lista de comunicados
	 * 
	 * @param Unit,
	 *            instância da unidade
	 * 
	 */
	public PaginatedList<Communication> listCommunications(Integer page, Integer pageSize) {
		PaginatedList<Communication> results = new PaginatedList<Communication>();
		if (page == null) {
			page = 1;
		}
		if (pageSize == null) {
			pageSize = 5;
		}

		Criteria criteria = this.dao.newCriteria(Communication.class)
				.add(Restrictions.eq("deleted", false))
				.addOrder(Order.desc("lastModification"))
				.setFirstResult((page-1) * pageSize)
				.setMaxResults(pageSize);

		Criteria count = this.dao.newCriteria(Communication.class)
				.add(Restrictions.eq("deleted", false))
				.addOrder(Order.desc("lastModification"))
				.setProjection(Projections.countDistinct("id"));

		List<Communication> communications = this.dao.findByCriteria(criteria, Communication.class);
				
		results.setList(communications);
		results.setTotal((long) communications.size());
		results.setTotal((Long) count.uniqueResult());

		return results;
	}
	
	/**
	 * Recupera uma instância do objeto Communication utilizando um id
	 * 
	 * @param id
	 *             à ser utilizado na query
	 * @return Comunicado que utiliza o id epecificado
	 */
	public Communication retrieveCommunicationByValidity(Date validityBegin) {
		
		Criteria criteria  = this.dao.newCriteria(Communication.class)
			.add(Restrictions.eq("showPopup", true))
			.add(Restrictions.eq("deleted", false));
		
			Criterion afterBegin = Restrictions.le("validityBegin", validityBegin);
			Criterion beforeEnd = Restrictions.ge("validityEnd", validityBegin);
			Criterion noLimit = Restrictions.isNull("validityEnd");
			
			Disjunction orExp = Restrictions.or(beforeEnd, noLimit);
			Conjunction andExp = Restrictions.and(afterBegin, orExp);
			
			criteria.add(andExp)
			.addOrder(Order.desc("lastModification"))	
			.setMaxResults(1);
		
		Communication communication = (Communication) criteria.uniqueResult();
		return communication;
	}
	
	/**
	 * Recupera uma instância do objeto Communication utilizando um id
	 * 
	 * @param id
	 *             à ser utilizado na query
	 * @return Comunicado que utiliza o id epecificado
	 */
	public Communication retrieveCommunicationByValidity() {
		Criteria criteria  = this.dao.newCriteria(Communication.class)
			.add(Restrictions.eq("showPopup", true))
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.le("validityBegin", new Date()));
			
		Criterion limit = Restrictions.ge("validityEnd", new Date());
		Criterion noLimit = Restrictions.isNull("validityEnd");
		Disjunction orExp = Restrictions.or(limit, noLimit);
		
		criteria.add(orExp)
			.addOrder(Order.desc("lastModification"))
			.setMaxResults(1);
		
		Communication communication = (Communication) criteria.uniqueResult();
		return communication;
	}

	/**
	 * Encerra um comunicado
	 * 
	 * @param communicationHistory,
	 *            instância do comunicado a ser desativado
	 */
	public void endCommunication(Communication communicationHistory) {
		communicationHistory.setShowPopup(false);
		this.persist(communicationHistory);
	}

}
