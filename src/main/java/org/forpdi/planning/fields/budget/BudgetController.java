package org.forpdi.planning.fields.budget;

import java.util.List;

import org.forpdi.core.bean.IdDto;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.utils.Util;
import org.forpdi.planning.fields.budget.dto.BudgetElementDao;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BudgetController extends AbstractController {

	@Autowired
	private BudgetBS bs;
	@Autowired
	private CompanyBS companyBs;
	@Autowired
	private CompanyDomainContext domain;


	/**
	 * Salvar um novo elemento orçamentário, relacionando a uma instancia company
	 *
	 * @param subAction,
	 *            sub ação orçamentaria
	 * @param instanceId,
	 *            id da instancia de nível
	 * 
	 * @return item, dto do orçamento salvo, com os valores conforme a sub ação
	 */
	@PostMapping(BASEPATH + "/budget/element/create")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> save(@RequestBody BudgetElementDao dao) {

		try {
			Company company = domain.getCompany();
			BudgetElement budget = this.bs.budgetElementExistsBySubActionAndCompany(dao.subAction(), company);
			if (budget != null) {
				return this.fail("Nome de ação orcamentária já existente!");
			}

			Double budgetLoaD = Util.parseDoubleWithComma(dao.budgetLoa());

			BudgetElement budgetElement = new BudgetElement();
			budgetElement.setCompany(company);
			budgetElement.setSubAction(dao.subAction());
			budgetElement.setBudgetLoa(budgetLoaD);
			budgetElement.setBalanceAvailable(budgetLoaD);

			this.bs.saveBudgetElement(budgetElement);
			return this.success(budgetElement);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Listar todos elementos orçamentários de uma company
	 * 
	 * @param companyId
	 *            Id da company
	 * 
	 * @return list, todos as simulações de orçamento
	 */
	public ResponseEntity<?> listBudgetAction(Long companyId) {

		try {

			Company company = this.bs.exists(companyId, Company.class);

			if (company == null) {
				return this.fail("A empresa solicitada não foi encontrada.");
			}

			PaginatedList<BudgetElement> list = this.bs.listBudgetElement(company);

			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}


	/**
	 * Buscar uma página com vários elementos orçamentários de uma company de acordo com o número da página.
	 *
	 * @param companyId
	 *            Id da company
	 *
	 * @param page
	 *            Número da página que se deseja recuperar os usuários
	 *
	 * @param pageSize
	 *            Qauntidade de elementos por página
	 *
	 * @return list, as simulações de orçamento
	 */
	@GetMapping(BASEPATH + "/budget/element/list/{companyId}")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> listBudgetActionPaginated(
			@PathVariable Long companyId,
			@RequestParam(required = false) String term,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false, value = "sortedBy[]") List<String> sortedBy ) {

		if(page == null && pageSize == null) {
			 return listBudgetAction(companyId);
		}

		try {

			Company company = this.bs.exists(companyId, Company.class);

			if (company == null) {
				return this.fail("A empresa solicitada não foi encontrada.");
			}

			PaginatedList<BudgetElement> list = this.bs.listBudgetElementPaginated(company, term, page, pageSize, sortedBy);

			return this.success(list.getList(), list.getTotal());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Atualizar um Elemento orçamentário existente no banco de dados.
	 * 
	 * @param name,
	 *            novo nome do orçamento
	 * @param subAction,
	 *            nova sub ação orçametaria
	 * @param id,
	 *            referente ao orçamento existente
	 * 
	 * @return item, dto do orçamento atualizado
	 */
	@PostMapping(BASEPATH + "/budget/element/update")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> update(@RequestBody BudgetElementDao dao) {
		try {
			BudgetElement budgetElement = this.bs.budgetElementExistsById(dao.idBudgetElement());
			
			if (budgetElement == null) {
				return this.fail("Orçamento inválido.");
			}

			domain.validateTenant(budgetElement.getCompany());

			BudgetElement budgetElementNewName = this.bs.budgetElementExistsBySubActionAndCompany(dao.subAction(),
					budgetElement.getCompany());
			if (budgetElementNewName != null && !budgetElementNewName.getId().equals(budgetElement.getId())) {
				return this.fail("Nome de ação orcamentária já existente!");
			}

			double diferenca = budgetElement.getBudgetLoa() - budgetElement.getBalanceAvailable();
			Double committedTotal = 0d;

			Double budgetLoa = Util.parseDoubleWithComma(dao.budgetLoa());
			if (budgetLoa < diferenca) {
				return this.fail("Orçamento loa não permitido.");
			} else {
				PaginatedList<Budget> list = this.bs.listBudgetsByBudgetElement(budgetElement);

				for (Budget budget : list.getList()) {
					committedTotal += budget.getCommitted();
				}

			}

			Double budgetLoaTotal = budgetLoa - committedTotal;

			budgetElement.setSubAction(dao.subAction());
			budgetElement.setBudgetLoa(budgetLoa);
			budgetElement.setBalanceAvailable(budgetLoaTotal);

			this.bs.update(budgetElement);

			return this.success(budgetElement);

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}

	}

	/**
	 * Excluir um elemento existente no banco de dados
	 * 
	 * @param id,
	 *            referente ao orçamento a ser excluido
	 * @return budget, orçamento excluído
	 */
	@PostMapping(BASEPATH + "/budget/element/delete")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> deleteBudget(@RequestBody IdDto dto) {
		try {
			BudgetElement budgetElement = this.bs.budgetElementExistsById(dto.id());

			domain.validateTenant(budgetElement.getCompany());
			
			if (budgetElement.getLinkedObjects() > 0) {
				return this.fail("Não foi possivél deletar, elemento orçamentario possui orçamento relacionado.");
			}

			this.bs.deleteBudget(budgetElement);
			return this.success(budgetElement);
		} catch (Throwable e) {
			LOGGER.error("Erro ao deletar o orçamento", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	
	/**
	 * Lista os objetivos vinculados de um plano orçamentário
	 * 
	 * @param id referente ao orçamento a ser buscado
	 * @return lista de objetivos vinculados
	 */
	@GetMapping(BASEPATH + "/budget/element/list-linked-objectives/{id}")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> listLinkedObjectives(@PathVariable Long id) {
		try {
			BudgetElement budgetElement = this.bs.budgetElementExistsById(id);

			PaginatedList<Budget> budgetList = this.bs.listBudgetsByBudgetElement(budgetElement);
			
			for (Budget budget : budgetList.getList()) {
				StructureLevelInstance levelInstance = budget.getLevelInstance();
				budget.setExportPlanId(levelInstance.getPlan().getParent().getId());
				budget.setExportStructureLevelInstanceId(levelInstance.getId());
			}
			
			return this.success(budgetList);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
}
