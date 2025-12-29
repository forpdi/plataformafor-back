package org.forpdi.core.csvs;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.forpdi.planning.fields.budget.BudgetBS;
import org.forpdi.planning.fields.budget.BudgetElement;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.GoalsFilterHelper;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskFilterParams;
import org.forrisco.risk.dto.RiskExportAxesCSVRequest;
import org.forrisco.risk.dto.RiskExportRequestDto;
import org.forrisco.risk.incident.dto.FilterIncidentsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class CsvExportController extends AbstractController {
	@Autowired
	private CsvPlanExport csvPlanExport;
	@Autowired
	private CsvGoalExport csvGoalsExport;
	@Autowired
	private CsvRiskExport csvRiskExport;
	@Autowired
	private CsvLevelInstanceBudgetExport csvLevelInstanceBudgetExport;
	@Autowired
	private CsvActionPlanExport csvActionPlanExport;
	@Autowired
	private CsvBudgetExport csvBudgetExport;
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private PlanBS planBS;
	@Autowired
	private BudgetBS budgetBS;
	@Autowired
	private StructureBS structureBS;
	@Autowired
	private GoalsFilterHelper goalsFilterHelper;
	@Autowired
	private CompanyBS companyBS;
	@Autowired
	private CsvIncidentExport csvIncidentExport;
	@Autowired
	private RiskBS riskBS;

	@GetMapping("plan/export/{planMacroId}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_AUTENTICATED)
	public ResponseEntity<?> exportPlan(@PathVariable Long planMacroId, UserSession userSession) {
		try {
			PlanMacro planMacro = this.planBS.retrievePlanMacroById(planMacroId);
			if (planMacro == null) {
				this.response.sendError(HttpServletResponse.SC_NOT_FOUND, "Plano Macro não encontrado.");
				return this.nothing();
			}

			CompanyDomain currentDomain = this.domain.get();
			Company domainCompany = currentDomain != null ? currentDomain.getCompany() : null;

			if (domainCompany != null && !planMacro.getCompany().getId().equals(domainCompany.getId())) {
				LOGGER.warn(
					"Tentativa de acesso cruzado ao Plano Macro. Host empresa {} tentando acessar Plano Macro ID {} da empresa {}.",
					domainCompany.getId(), planMacro.getId(), planMacro.getCompany().getId());
				this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
				return this.nothing();
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					LOGGER.warn("Usuário {} sem empresa vinculada tentou exportar Plano Macro ID {}.", loggedInUserId, planMacro.getId());
					this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
					return this.nothing();
				}

				if (!planMacro.getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou exportar o Plano Macro ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, planMacro.getId(),
							planMacro.getCompany().getId()));
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Você não tem permissão para exportar este plano.");
				}

				if (domainCompany != null && !domainCompany.getId().equals(userCompany.getId())) {
					LOGGER.warn(
						"Tentativa de acesso cruzado ao Plano Macro. Usuário {} da empresa {} tentou exportar via domínio da empresa {}.",
						loggedInUserId, userCompany.getId(), domainCompany.getId());
					this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
					return this.nothing();
				}
			}

			try {
				domain.validateTenant(planMacro.getCompany());
			} catch (IllegalStateException ex) {
				LOGGER.warn("Violação de tenant ao exportar Plano Macro {}: {}", planMacro.getId(), ex.getMessage());
				this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
				return this.nothing();
			}

			LOGGER.info("Starting export plans {}'...", this.domain.get().getCompany().getName());
			this.response.setHeader("Content-Disposition", String.format("attachment; filename=plans-%d-%s.csv",
					domain.get().getCompany().getId(), LocalDateTime.now().toString()));
			this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

			List<Plan> plans = this.planBS.listPlansForPlanMacro(planMacro);
			csvPlanExport.exportCSV(plans, this.response.getOutputStream());

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}

	@GetMapping("plan/budget/export/{levelInstanceId}")
	public ResponseEntity<?> exportLevelInstanceBudget(@PathVariable Long levelInstanceId, UserSession userSession) {
		try {
			StructureLevelInstance levelInstance = this.structureBS.retrieveLevelInstanceNoDeleted(levelInstanceId);
			if (levelInstance == null) {
				this.response.sendError(HttpServletResponse.SC_NOT_FOUND, "Nível não encontrado.");
				return this.nothing();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !levelInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou exportar orçamento do Nível ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, levelInstance.getId(),
							levelInstance.getPlan().getParent().getCompany().getId()));
					this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
					return this.nothing();
				}
			}

			domain.validateTenant(levelInstance.getPlan().getParent().getCompany());

			this.response.setHeader("Content-Disposition", String.format("attachment; filename=budget-%d-%s.csv",
					domain.get().getCompany().getId(), LocalDateTime.now().toString()));
			this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

			csvLevelInstanceBudgetExport.exportLevelInstaceBudgets(levelInstance, this.response.getOutputStream());

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}

	@GetMapping("api/goal/exportCSV")
	public ResponseEntity<?> exportGoalCsv(
			@RequestParam Long parentId,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String attributesToFilter,
			@RequestParam(required = false) Integer progressStatusId,
			UserSession userSession) {
		try {
			StructureLevelInstance parent = this.structureBS.retrieveLevelInstance(parentId);
			if (parent == null) {
				this.response.sendError(HttpServletResponse.SC_NOT_FOUND, "Nível pai não encontrado.");
				return this.nothing();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !parent.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou exportar metas do Nível Pai ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, parent.getId(),
							parent.getPlan().getParent().getCompany().getId()));
					this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
					return this.nothing();
				}
			}

			domain.validateTenant(parent.getPlan().getParent().getCompany());

			PaginatedList<StructureLevelInstance> goals = this.goalsFilterHelper.retrieveFilteredGoals(
					parentId, name, progressStatusId, attributesToFilter, 1, Integer.MAX_VALUE);

			this.response.setHeader("Content-Disposition", String.format("attachment; filename=goals-%d-%s.csv",
					domain.get().getCompany().getId(), LocalDateTime.now().toString()));
			this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			csvGoalsExport.exportGoalsCSV(goals.getList(), this.response.getOutputStream());

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}

	@GetMapping("api/field/actionplan/exportCSV")
	public ResponseEntity<?> exportActionPlanCsv(
			@RequestParam Long levelInstanceId,
			@RequestParam(required = false) String description,
			@RequestParam(required = false) Boolean checked,
			@RequestParam(required = false) Boolean notChecked,
			@RequestParam(required = false) Integer year,
			UserSession userSession) {
		try {
			StructureLevelInstance levelInstance = this.structureBS.retrieveLevelInstance(levelInstanceId);
			if (levelInstance == null) {
				this.response.sendError(HttpServletResponse.SC_NOT_FOUND, "Estrutura incorreta!");
				return this.nothing();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !levelInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou exportar planos de ação do Nível ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, levelInstance.getId(),
							levelInstance.getPlan().getParent().getCompany().getId()));
					this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
					return this.nothing();
				}
			}

			domain.validateTenant(levelInstance.getPlan().getParent().getCompany());

			List<Attribute> attributeList = this.structureBS.retrieveLevelAttributes(levelInstance.getLevel());
			PaginatedList<Attribute> attributeListPagined = new PaginatedList<>();
			attributeListPagined.setList(attributeList);

			attributeListPagined = this.structureBS.setActionPlansAttributes(levelInstance, attributeListPagined, 1,
					Integer.MAX_VALUE, description, checked, notChecked, year);

			ArrayList<ActionPlan> actionPlans = new ArrayList<>();
			for (Attribute attribute : attributeListPagined.getList()) {
				if (attribute.getActionPlans() != null) {
					actionPlans.addAll(attribute.getActionPlans());
				}
			}

			this.response.setHeader("Content-Disposition", String.format("attachment; filename=action-plans-%d-%s.csv",
					domain.get().getCompany().getId(), LocalDateTime.now().toString()));
			this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

			csvActionPlanExport.exportLevelInstaceActionPlanCSV(actionPlans, this.response.getOutputStream());

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}

	@PostMapping("budget/exportCSV")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_AUTENTICATED)
	public ResponseEntity<?> exportBudgets(UserSession userSession) {
		try {
			CompanyDomain currentDomain = this.domain.get();
			Company domainCompany = currentDomain != null ? currentDomain.getCompany() : null;

			if (domainCompany == null) {
				LOGGER.warn("Tentativa de exportar orçamentos sem domínio definido.");
				this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
				return this.nothing();
			}

			Company targetCompany;
			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (isSystemAdmin) {
				targetCompany = domainCompany;
			} else {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null) {
					LOGGER.warn("Usuário {} sem empresa vinculada tentou exportar orçamentos.", loggedInUserId);
					this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
					return this.nothing();
				}

				if (!domainCompany.getId().equals(userCompany.getId())) {
					LOGGER.warn(
						"Tentativa de acesso cruzado ao orçamento. Usuário {} da empresa {} tentou exportar via domínio da empresa {}.",
						loggedInUserId, userCompany.getId(), domainCompany.getId());
					this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
					return this.nothing();
				}

				targetCompany = userCompany;
			}

			try {
				domain.validateTenant(targetCompany);
			} catch (IllegalStateException ex) {
				LOGGER.warn("Violação de tenant ao exportar orçamentos da empresa {}: {}", targetCompany.getId(), ex.getMessage());
				this.response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
				return this.nothing();
			}

			LOGGER.info("Starting export budget '{}'...", targetCompany.getName());
			this.response.setHeader(
					"Content-Disposition",
					String.format("attachment; filename=budgets-%d-%s.csv",
							targetCompany.getId(),
							LocalDateTime.now().toString()));

			this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

			List<BudgetElement> list = this.budgetBS.listBudgetElement(targetCompany).getList();

			csvBudgetExport.exportBudgetCSV(list, this.response.getOutputStream());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}

	/**
	 * Exportar os riscos de um plano específico em formato CSV.
	 * O arquivo CSV é gerado com base nos parâmetros de filtro fornecidos
	 *
	 * @param planRiskId ID do plano de risco a ser exportado.
	 * @param filters    Parâmetros de filtros opcionais, no formato JSON.
	 * @return ResponseEntity representando o status da operação.
	 */
	@PostMapping(path = "risk/exportCSV", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void exportRiskCsv(
			@RequestBody RiskExportRequestDto req,
			UserSession userSession,
			HttpServletResponse resp) {
		try {
			PlanRisk planRisk = this.planBS.exists(req.planRiskId(), PlanRisk.class);
			if (planRisk == null) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND,
						"Plano de Risco não encontrado.");
				return;
			}

			boolean isSysAdmin = userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel();

			if (!isSysAdmin) {
				Long userId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(userId);

				boolean acessoNegado = (userCompany == null) ||
						!planRisk.getPolicy().getCompany()
								.getId()
								.equals(userCompany.getId());

				if (acessoNegado) {
					LOGGER.warn("Tentativa de acesso indevido. Usuário '{}' (ID: {}) " +
							"tentou exportar riscos do Plano ID {} da Empresa ID {}.",
							userSession.getAuthData().getName(),
							userId,
							planRisk.getId(),
							planRisk.getPolicy().getCompany().getId());
					resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
					return;
				}
			}

			domain.validateTenant(planRisk.getPolicy().getCompany());

			resp.setHeader("Content-Disposition",
					"attachment; filename=riscos-" +
							domain.get().getCompany().getId() + "-" +
							LocalDateTime.now() + ".csv");
			resp.setContentType("text/csv");

			RiskFilterParams filters = RiskFilterParams.fromJson(req.filters());
			csvRiskExport.exportRiskCsv(
					req.planRiskId(), filters, resp.getOutputStream());

			LOGGER.info("CSV de riscos exportado com sucesso (Plano ID {}).",
					req.planRiskId());

		} catch (Throwable e) {
			LOGGER.error("Erro inesperado ao exportar CSV", e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("risk/linked-axes/exportCSV")
	public ResponseEntity<?> exportAxesRiskCSV(
			@RequestBody RiskExportAxesCSVRequest request,
			UserSession userSession) {
		LOGGER.info("--- [EXPORT DEBUG] Iniciando processo de exportação de riscos. ---");
		LOGGER.info("[EXPORT DEBUG] Parâmetros Recebidos -> Plan ID: {}, Axis ID: {}, Year: {}",
				request.planId(), request.selectedAxis(), request.selectedYear());

		try {
			Plan plan = this.planBS.retrieveById(request.planId());
			if (plan == null) {
				this.response.sendError(HttpServletResponse.SC_NOT_FOUND, "Plano não encontrado.");
				return this.nothing();
			}

			if (request.selectedAxis() != -1L) {
				StructureLevelInstance axis = this.structureBS.retrieveLevelInstance(request.selectedAxis());
				if (axis == null) {
					this.response.sendError(HttpServletResponse.SC_NOT_FOUND, "Eixo estratégico não encontrado.");
					return ResponseEntity.notFound().build();
				}
			}

			this.response.setHeader(
					"Content-Disposition",
					String.format("attachment; filename=riscos-%s.csv", LocalDateTime.now()));
			this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

			LOGGER.info("[EXPORT DEBUG] Chamando o serviço 'csvRiskExport.exportAxesRiskCSV'...");

			csvRiskExport.exportAxesRiskCSV(
					request.planId(),
					request.selectedAxis(),
					request.selectedYear(),
					this.response.getOutputStream());
			this.response.flushBuffer();

			LOGGER.info("[EXPORT DEBUG] Serviço 'csvRiskExport.exportAxesRiskCSV' finalizado.");
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error durante a exportação.", e);
			return ResponseEntity.internalServerError().build();
		}

		LOGGER.info("--- [EXPORT DEBUG] Finalizando requisição de exportação. ---");
		return ResponseEntity.ok().build();
	}

	@PostMapping(path = "/incident/exportCSV", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void exportIncidentCsv(
			@RequestBody FilterIncidentsDto req,
			UserSession userSession,
			HttpServletResponse resp) {
		try {
			if (req.getPlanRiskId() == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "planRiskId é obrigatório.");
				return;
			}

			PlanRisk plan = this.riskBS.exists(req.getPlanRiskId(), PlanRisk.class);
			if (plan == null) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Plano de gestão não encontrado.");
				return;
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !plan.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
					return;
				}
			}
			this.domain.validateTenant(plan.getPolicy().getCompany());

			resp.setContentType("text/csv; charset=UTF-8");
			resp.setHeader("Content-Disposition",
					"attachment; filename=incidentes-" + plan.getId() + ".csv");

			csvIncidentExport.exportIncidentsCsv(plan, req, resp.getOutputStream());

		} catch (Throwable e) {
			LOGGER.error("Erro ao exportar CSV de incidentes", e);
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (Exception ignore) {
			}
		}
	}
}
