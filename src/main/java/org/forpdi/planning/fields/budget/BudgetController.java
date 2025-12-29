package org.forpdi.planning.fields.budget;

import java.util.List;

import org.forpdi.core.bean.IdDto;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.utils.Util;
import org.forpdi.planning.fields.budget.dto.BudgetElementDao;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.auth.UserSession;
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
	private CompanyBS companyBS;
	@Autowired
	private CompanyDomainContext domain;

	/**
	 * Salvar um novo elemento orçamentário, relacionando a uma instancia company
	 *
	 * @param subAction,
	 *                    sub ação orçamentaria
	 * @param instanceId,
	 *                    id da instancia de nível
	 *
	 * @return item, dto do orçamento salvo, com os valores conforme a sub ação
	 */
    @PostMapping(BASEPATH + "/budget/element/create")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
    public ResponseEntity<?> save(@RequestBody BudgetElementDao dao, UserSession userSession) {
        try {
            Company targetCompany;
            boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

            if (isSystemAdmin) {
                targetCompany = this.domain.getCompany();
            } else {
                Long loggedInUserId = userSession.getAuthData().getId();
                targetCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
            }

            if (targetCompany == null) {
                return this.fail("Acesso negado. Empresa não identificada.");
            }

            BudgetElement budget = this.bs.budgetElementExistsBySubActionAndCompany(dao.subAction(), targetCompany);
            if (budget != null) {
                return this.fail("Nome de ação orcamentária já existente!");
            }

            Double budgetLoaD = Util.parseDoubleWithComma(dao.budgetLoa());

            BudgetElement budgetElement = new BudgetElement();
            budgetElement.setCompany(targetCompany);
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
	 *                  Id da company
	 *
	 * @return list, todos as simulações de orçamento
	 * */
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
	 * Buscar uma página com vários elementos orçamentários de uma company de acordo
	 * com o número da página.
	 *
	 * @param companyId
	 *                  Id da company
	 *
	 * @param page
	 *                  Número da página que se deseja recuperar os usuários
	 *
	 * @param pageSize
	 *                  Qauntidade de elementos por página
	 *
	 * @return list, as simulações de orçamento
	 */
    @GetMapping(BASEPATH + "/budget/element/list")
	  @PreAuthorize(value = AccessLevels.HAS_ROLE_AUTENTICATED)
    public ResponseEntity<?> listBudgetActionPaginated(
            UserSession userSession,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false, value = "sortedBy[]") List<String> sortedBy) {

        try {
            Company targetCompany;
            Long loggedInUserId = userSession.getAuthData().getId();
            boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
            CompanyDomain currentDomain = this.domain.get();
            Company domainCompany = currentDomain != null ? currentDomain.getCompany() : null;

            if (isSystemAdmin) {
                targetCompany = domainCompany;
            } else {
                targetCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

                if (targetCompany == null) {
                    LOGGER.warn("Usuário {} sem empresa vinculada tentou listar elementos orçamentários.", loggedInUserId);
                    return this.fail("Acesso negado.");
                }

                if (domainCompany != null && !domainCompany.getId().equals(targetCompany.getId())) {
                    LOGGER.warn(
                        "Tentativa de acesso cruzado aos elementos orçamentários. Usuário {} da empresa {} tentou acessar dados da empresa {}.",
                        loggedInUserId, targetCompany.getId(), domainCompany.getId());
                    return this.fail("Acesso negado.");
                }
            }

            if (targetCompany == null) {
                return this.fail("A empresa solicitada não foi encontrada.");
            }

            List<String> normalizedSort = this.normalizeSortParams(sortedBy);

            PaginatedList<BudgetElement> list = this.bs.listBudgetElementPaginated(
                targetCompany, term, page, pageSize, normalizedSort);

            return this.success(list.getList(), list.getTotal());

        } catch (Throwable ex) {
            LOGGER.error("Unexpected runtime error", ex);
            return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
        }
    }

    private List<String> normalizeSortParams(List<String> sortedBy) {
        if (sortedBy == null || sortedBy.isEmpty()) {
            return null;
        }

        if (sortedBy.size() == 1 && sortedBy.get(0) != null && sortedBy.get(0).contains(",")) {
            String[] parts = sortedBy.get(0).split(",", 2);
            if (parts.length == 2 && !parts[0].isBlank()) {
                return List.of(parts[0], parts[1]);
            }
        }

        if (sortedBy.size() >= 2 && !sortedBy.get(0).isBlank()) {
            return sortedBy;
        }

        return null;
    }

	/**
	 * Atualizar um Elemento orçamentário existente no banco de dados.
	 *
	 * @param name,
	 *                   novo nome do orçamento
	 * @param subAction,
	 *                   nova sub ação orçametaria
	 * @param id,
	 *                   referente ao orçamento existente
	 *
	 * @return item, dto do orçamento atualizado
	 */
    @PostMapping(BASEPATH + "/budget/element/update")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
    public ResponseEntity<?> update(@RequestBody BudgetElementDao dao, UserSession userSession) {
        try {
            BudgetElement budgetElement = this.bs.budgetElementExistsById(dao.idBudgetElement());

            if (budgetElement == null) {
                return this.fail("Orçamento inválido.");
            }

            boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

            if (!isSystemAdmin) {
                Long loggedInUserId = userSession.getAuthData().getId();
                Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

                if (userCompany == null || !budgetElement.getCompany().getId().equals(userCompany.getId())) {
                    LOGGER.warn(
                        "Tentativa de acesso indevido ao BudgetElement ID {}. Usuário ID {} não pertence à empresa do arquivo.",
                        budgetElement.getId(), loggedInUserId);
                    return this.fail("Acesso negado.");
                }
            }

            BudgetElement budgetElementNewName = this.bs.budgetElementExistsBySubActionAndCompany(dao.subAction(), budgetElement.getCompany());
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
			BudgetElement budgetElement = this.bs.budgetElementExistsByIdAll(dto.id());

			// Obter a lista de orçamentos vinculados ao elemento orçamentário
			PaginatedList<Budget> linkedBudgets = this.bs.listBudgetsByBudgetElement(budgetElement);

			// Mostrar informações sobre os orçamentos vinculados no log
			LOGGER.info("Elemento orçamentário: " + budgetElement.getSubAction());
			LOGGER.info("Valor do linkedObjects: " + budgetElement.getLinkedObjects());
			LOGGER.info("Quantidade real de orçamentos vinculados: " + linkedBudgets.getList().size());

			if (!linkedBudgets.getList().isEmpty()) {
				// Mostrar detalhes de cada orçamento vinculado
				for (Budget budget : linkedBudgets.getList()) {
					LOGGER.info("Orçamento ID: " + budget.getId() +
						", deleted: " + budget.isDeleted() +
						", levelInstance: "
						+ (budget.getLevelInstance() != null ? budget.getLevelInstance().getId() : "null") +
						", levelInstance deleted: "
						+ (budget.getLevelInstance() != null ? budget.getLevelInstance().isDeleted() : "N/A"));
				}

				return this.fail("Não foi possível deletar, elemento orçamentario possui " +
					linkedBudgets.getList().size() + " orçamento(s) ativo(s) relacionado(s).");
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
    public ResponseEntity<?> listLinkedObjectives(@PathVariable Long id, UserSession userSession) {
        try {
            BudgetElement budgetElement = this.bs.budgetElementExistsById(id);

            if (budgetElement == null) {
                return this.fail("Acesso negado.");
            }

            boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

            if (!isSystemAdmin) {
                Long loggedInUserId = userSession.getAuthData().getId();
                Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

                if (userCompany == null || !budgetElement.getCompany().getId().equals(userCompany.getId())) {
                    LOGGER.warn(
                        "Tentativa de listagem indevida de objetivos do BudgetElement ID {}. Usuário ID {} não pertence à empresa do arquivo.",
                        id, loggedInUserId);
                    return this.fail("Acesso negado.");
                }
            }

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
