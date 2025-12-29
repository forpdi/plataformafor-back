package org.forpdi.core.communication;

import java.util.Date;
import java.util.List;

import org.forpdi.core.common.Conjunction;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.Disjunction;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.Order;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.Company;
import org.springframework.stereotype.Service;

@Service
public class CommunicationBS extends HibernateBusiness {

	private static final String FIELD_COMPANY = "company";

	/**
	 * Salvar um novo comunicado.
	 *
	 * @param communicationHistory comunicado que será salvo.
	 * @return Comunicado que foi salvo.
	 */
	public Communication saveNewCommunication(Communication communicationHistory) {
		communicationHistory.setLastModification(new Date());
		communicationHistory.setShowPopup(true);
		communicationHistory.setDeleted(false);
		this.persist(communicationHistory);
		return communicationHistory;
	}

	/**
	 * Recupera uma instância do objeto Communication utilizando um id.
	 *
	 * @param id a ser utilizado na query.
	 * @return Comunicado que utiliza o id especificado.
	 */
	public Communication retrieveCommunicationById(Long id) {
		Criteria criteria = this.dao.newCriteria(Communication.class)
				.add(Restrictions.eq("id", id))
				.add(Restrictions.eq("deleted", false));
		return (Communication) criteria.uniqueResult();
	}

	/**
	 * Retorna uma lista paginada de comunicados.
	 *
	 * @param page     página solicitada (começando em 1).
	 * @param pageSize quantidade de itens por página.
	 * @return Lista paginada de comunicados.
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
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize);

		Criteria count = this.dao.newCriteria(Communication.class)
				.add(Restrictions.eq("deleted", false))
				.setProjection(Projections.countDistinct("id"));

		List<Communication> communications = this.dao.findByCriteria(criteria, Communication.class);

		results.setList(communications);
		results.setTotal((Long) count.uniqueResult());

		return results;
	}

	/**
	 * Retorna uma lista paginada de comunicados filtrada por tenant (Company).
	 * Este é o método seguro a ser usado pelo controller.
	 *
	 * @param company  tenant que será usado no filtro.
	 * @param page     página solicitada (começando em 1).
	 * @param pageSize quantidade de itens por página.
	 * @return Lista paginada de comunicados do tenant.
	 */
	public PaginatedList<Communication> listByCompany(Company company, Integer page, Integer pageSize) {
		if (page == null) {
			page = 1;
		}
		if (pageSize == null) {
			pageSize = 5;
		}

		// 1. Cria o critério para buscar os itens da página correta
		Criteria criteria = this.dao.newCriteria(Communication.class)
				.add(Restrictions.eq(FIELD_COMPANY, company))
				.add(Restrictions.eq("deleted", false))
				.addOrder(Order.desc("lastModification"))
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize);

		// 2. Cria o critério para contar o total de itens do tenant
		Criteria count = this.dao.newCriteria(Communication.class)
				.add(Restrictions.eq(FIELD_COMPANY, company))
				.add(Restrictions.eq("deleted", false))
				.setProjection(Projections.countDistinct("id"));

		// 3. Executa as buscas no banco de dados
		List<Communication> list = this.dao.findByCriteria(criteria, Communication.class);
		Long total = (Long) count.uniqueResult();

		// 4. Constrói o resultado usando o construtor vazio e os métodos 'set'
		PaginatedList<Communication> results = new PaginatedList<>();
		results.setList(list);
		results.setTotal(total);

		return results;
	}

	/**
	 * Recupera um comunicado ativo pela data de validade.
	 *
	 * @deprecated Este método não é seguro para multi-tenant. Utilize
	 *             {@link #retrieveCommunicationByValidity(Company, Date)}.
	 */
	@Deprecated
	public Communication retrieveCommunicationByValidity(Date validityBegin) {

		Criteria criteria = this.dao.newCriteria(Communication.class)
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

		return (Communication) criteria.uniqueResult();
	}

	/**
	 * Recupera um comunicado ativo pela data de validade para um tenant específico.
	 *
	 * @param company       tenant a ser filtrado.
	 * @param validityBegin data de referência.
	 * @return Comunicado válido para o tenant e data especificados.
	 */
	public Communication retrieveCommunicationByValidity(Company company, Date validityBegin) {
		Criteria criteria = this.dao.newCriteria(Communication.class)
				.add(Restrictions.eq(FIELD_COMPANY, company))
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

		return (Communication) criteria.uniqueResult();
	}

	/**
	 * Recupera o comunicado ativo no momento.
	 *
	 * @deprecated Este método não é seguro para multi-tenant. Utilize
	 *             {@link #retrieveCommunicationByValidity(Company)}.
	 */
	@Deprecated
	public Communication retrieveCommunicationByValidity() {
		return this.retrieveCommunicationByValidity(new Date());
	}

	/**
	 * Recupera o comunicado ativo no momento para um tenant específico.
	 *
	 * @param company tenant a ser filtrado.
	 * @return Comunicado válido para o tenant e o momento atual.
	 */
	public Communication retrieveCommunicationByValidity(Company company) {
		return this.retrieveCommunicationByValidity(company, new Date());
	}

	/**
	 * Encerra um comunicado.
	 *
	 * @param communicationHistory comunicado a ser encerrado.
	 */
	public void endCommunication(Communication communicationHistory) {
		communicationHistory.setShowPopup(false);
		this.persist(communicationHistory);
	}
}
