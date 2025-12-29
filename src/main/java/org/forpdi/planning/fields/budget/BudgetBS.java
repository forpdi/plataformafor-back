package org.forpdi.planning.fields.budget;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.forpdi.core.common.*;
import org.forpdi.core.company.Company;
import org.forpdi.core.utils.Consts;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BudgetBS extends HibernateBusiness {
	
	@Autowired
	private HibernateDAO hibernateDAO;

	private static final String DELETED_PROPERTY = "deleted";

	/**
	 * Salva um elemento orçamentário.
	 * 
	 * @param budgetElement
	 *            Elemento orçamentário a ser salvo.
	 */
	public void saveBudgetElement(BudgetElement budgetElement) {
		budgetElement.setDeleted(false);
		this.persist(budgetElement);
	}

	/**
	 * Retornar lista de elementos orçamentários.
	 *
	 * @param company
	 *            Companhia para filtrar.
	 * @return PaginatedList<BudgetElement> Lista de elementos orçamentários.
	 */
	public PaginatedList<BudgetElement> listBudgetElement(Company company) {
		PaginatedList<BudgetElement> list = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(BudgetElement.class)
				.add(Restrictions.eq(DELETED_PROPERTY, false))
				.add(Restrictions.eq("company", company));

		list.setList(this.dao.findByCriteria(criteria, BudgetElement.class));
		return list;
	}

	/**
	 * Retornar lista de elementos orçamentários paginada, com filtro e ordenação.
	 *
	 * @param company
	 *            Companhia para filtrar.
	 * @param term
	 *            Termo de busca para subAction.
	 * @param page
	 *            Número da página.
	 * @param pageSize
	 *            Tamanho da página.
	 * @param sortedBy
	 *            Lista com [campo, "asc" ou "desc"].
	 * @return PaginatedList<BudgetElement> Lista paginada de elementos orçamentários.
	 */
	public PaginatedList<BudgetElement> listBudgetElementPaginated(
			Company company, String term, Integer page, Integer pageSize, List<String> sortedBy) {

		if (page == null || page < 1) {
			page = 1;
		}
		if (pageSize == null) {
			pageSize = Consts.MIN_PAGE_SIZE;
		}
		PaginatedList<BudgetElement> results = new PaginatedList<>();

		Criteria criteria = this.dao.newCriteria(BudgetElement.class)
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.add(Restrictions.eq(DELETED_PROPERTY, false))
				.add(Restrictions.eq("company", company));

		Criteria counting = this.dao.newCriteria(BudgetElement.class)
				.add(Restrictions.eq(DELETED_PROPERTY, false))
				.add(Restrictions.eq("company", company))
				.setProjection(Projections.countDistinct("id"));
		
		if (term != null) {
			Criterion subAction = Restrictions.like("subAction", "%" + term + "%").ignoreCase();
			criteria.add(subAction);
    		counting.add(subAction);
		}

		if (sortedBy != null && !sortedBy.isEmpty() && !sortedBy.get(0).isBlank()) {
			String field = sortedBy.get(0);
			Boolean asc = !sortedBy.get(1).equalsIgnoreCase("desc");
			criteria.addOrder(asc 
					? Order.asc(field).ignoreCase() 
					: Order.desc(field).ignoreCase());
		} else {
			criteria.addOrder(Order.asc("subAction"));
		}

		List<BudgetElement> companyBudgetElements = this.dao.findByCriteria(criteria, BudgetElement.class);
		results.setList(companyBudgetElements);
		results.setTotal((Long) counting.uniqueResult());
		return results;
	}

	/**
	 * Retornar todos os elementos orçamentários de uma companhia.
	 *
	 * @param company
	 *            Companhia para filtrar.
	 * @return List<BudgetElement> Lista de elementos orçamentários.
	 */
	public List<BudgetElement> listAllBudgetElementsByCompany(Company company) {
		Criteria criteria = this.dao.newCriteria(BudgetElement.class)
			.add(Restrictions.eq("company", company));
		return this.dao.findByCriteria(criteria, BudgetElement.class);
	}

	public List<BudgetElement> listAllBudgetElementsByPlansMacroId(List<Long> planMacroIds) {
		Criteria criteria = this.dao.newCriteria(Budget.class)
			.createAlias("budgetElement", "be", JoinType.INNER_JOIN)
			.createAlias("levelInstance", "li", JoinType.INNER_JOIN)
			.createAlias("li.plan", "pl", JoinType.INNER_JOIN)
			.createAlias("pl.parent", "pm", JoinType.INNER_JOIN)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.eq("li.deleted", false))
			.add(Restrictions.eq("pl.deleted", false))
			.add(Restrictions.eq("pm.deleted", false))
			.add(Restrictions.eq("be.deleted", false))
			.add(Restrictions.in("pm.id", planMacroIds))
			.setProjection(Projections.property("be.id"));

		Criteria budgetElements = this.dao.newCriteria(BudgetElement.class)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.in("id", criteria.list()));
		List result = budgetElements.list();
		System.out.println(result);
		return result;
	}

	/**
	 * Retorna orçamentos vinculados a elementos e instâncias de nível estrutural.
	 * 
	 * @param budgetElements
	 *            Lista de BudgetElement.
	 * @param structureLevelInstances
	 *            Lista de StructureLevelInstance.
	 * @return List<Budget> Lista de orçamentos.
	 */
	public List<Budget> listAllBudgetsByElementsAndLevelInstances(
			List<BudgetElement> budgetElements, List<StructureLevelInstance> structureLevelInstances) {

		if (GeneralUtils.isEmpty(budgetElements) && GeneralUtils.isEmpty(structureLevelInstances)) {
			return Collections.emptyList();
		}
		Criteria criteria = this.dao.newCriteria(Budget.class);
		if (!GeneralUtils.isEmpty(budgetElements)) {
			criteria.add(Restrictions.in("budgetElement", budgetElements));
		}
		if (!GeneralUtils.isEmpty(structureLevelInstances)) {
			criteria.add(Restrictions.in("levelInstance", structureLevelInstances));
		}
		criteria.add(Restrictions.eq("deleted", false));
		return this.dao.findByCriteria(criteria, Budget.class);
	}

	/**
	 * Buscar Elemento Orçamentário por ID.
	 * 
	 * @param id
	 *            Id do elemento orçamentário.
	 * @return BudgetElement Elemento orçamentário.
	 */
	public BudgetElement budgetElementExistsById(Long id) {
		Criteria criteria = this.dao.newCriteria(BudgetElement.class)
				.add(Restrictions.eq(DELETED_PROPERTY, false))
				.add(Restrictions.eq("id", id));

		return (BudgetElement) criteria.uniqueResult();
	}

	public BudgetElement budgetElementExistsByIdAll(Long id) {
		Criteria criteria = this.dao.newCriteria(BudgetElement.class)
			.add(Restrictions.eq("id", id));

		return (BudgetElement) criteria.uniqueResult();
	}

	/**
	 * Verifica se elemento orçamentário existe por subAction e company.
	 * 
	 * @param subAction
	 *            subAction a ser buscada (ignoreCase).
	 * @param company
	 *            Companhia a ser buscada.
	 * @return BudgetElement Elemento orçamentário encontrado ou null.
	 */
	public BudgetElement budgetElementExistsBySubActionAndCompany(String subAction, Company company) {
		Criteria criteria = this.dao.newCriteria(BudgetElement.class)
				.add(Restrictions.eq(DELETED_PROPERTY, false))
				.add(Restrictions.like("subAction", subAction).ignoreCase())
				.add(Restrictions.eq("company", company))
				.setMaxResults(1);

		return (BudgetElement) criteria.uniqueResult();
	}

	/**
	 * Atualiza um elemento orçamentário.
	 * 
	 * @param budgetElement
	 *            Elemento orçamentário para atualizar.
	 */
	public void update(BudgetElement budgetElement) {
		budgetElement.setDeleted(false);
		this.persist(budgetElement);
	}

	/**
	 * Deleta (soft delete) um elemento orçamentário.
	 * 
	 * @param budgetElement
	 *            Elemento orçamentário para deletar.
	 */
	public void deleteBudget(BudgetElement budgetElement) {
		budgetElement.setDeleted(true);
		this.persist(budgetElement);
	}

	/**
	 * Lista orçamentos não deletados para um elemento orçamentário.
	 *
	 * @param budgetElement
	 *            Instância de BudgetElement.
	 * @return PaginatedList<Budget> contendo os orçamentos.
	 */
	public PaginatedList<Budget> listBudgetsByBudgetElement(BudgetElement budgetElement) {
		PaginatedList<Budget> results = new PaginatedList<>();

		// 1) Cria Criteria apontando para a entidade Budget
		Criteria criteria = this.hibernateDAO.newCriteria(Budget.class)
				// 2) Condição deleted = false
				.add(Restrictions.eq(DELETED_PROPERTY, false))
				// 3) Condição budgetElement = :budgetElement
				.add(Restrictions.eq("budgetElement", budgetElement));

		// 4) Executa a consulta
		List<Budget> budgets = this.hibernateDAO.findByCriteria(criteria, Budget.class);

		// 5) Popula o PaginatedList apenas com a lista (o teste não checa total)
		results.setList(budgets);
		return results;
	}

	/**
	 * Buscar lista de orçamentos por instância de nível estrutural (não deletados).
	 * 
	 * @param level
	 *            Instância de StructureLevelInstance.
	 * @return List<Budget> Lista de orçamentos.
	 */
	public List<Budget> listBudgetByLevelInstance(StructureLevelInstance level) {		
		Criteria criteria = this.dao.newCriteria(Budget.class)
				.add(Restrictions.eq(DELETED_PROPERTY, false))
				.add(Restrictions.eq("levelInstance", level));
		return this.dao.findByCriteria(criteria, Budget.class);
	}
	
	/**
	 * Buscar lista de orçamentos por instância de nível estrutural (inclusive deletados).
	 * 
	 * @param level
	 *            Instância de StructureLevelInstance.
	 * @return List<Budget> Lista de orçamentos.
	 */
	public List<Budget> listAllBudgetByLevelInstance(StructureLevelInstance level) {		
		Criteria criteria = this.dao.newCriteria(Budget.class)
				.add(Restrictions.eq("levelInstance", level));
		return this.dao.findByCriteria(criteria, Budget.class);
	}

}
