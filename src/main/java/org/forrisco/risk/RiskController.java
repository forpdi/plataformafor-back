package org.forrisco.risk;

import java.util.ArrayList;
import java.util.List;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.company.CompanyUser;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.Consts;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.contingency.Contingency;
import org.forrisco.risk.contingency.ContingencyBS;
import org.forrisco.risk.dto.RiskDto;
import org.forrisco.risk.dto.RiskReplicateDto;
import org.forrisco.risk.history.RiskEditHistoryBS;
import org.forrisco.risk.incident.Incident;
import org.forrisco.risk.incident.IncidentBS;
import org.forrisco.risk.links.RiskActivity;
import org.forrisco.risk.monitor.Monitor;
import org.forrisco.risk.monitor.MonitorBS;
import org.forrisco.risk.preventiveaction.PreventiveAction;
import org.forrisco.risk.preventiveaction.PreventiveActionBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Matheus Nascimento
 */
@RestController
public class RiskController extends AbstractController {
	
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private RiskBS riskBS;
	@Autowired
	private UnitBS unitBS ;
	@Autowired
	private UserBS userBS;

	@Autowired
	private ContingencyBS contingencyBS;
	@Autowired
	private IncidentBS incidentBS;
	@Autowired
	private MonitorBS monitorBS;
	@Autowired
	private PreventiveActionBS preventiveActionBS;
	
	@Autowired
	private RiskReplicationBS riskReplicationBS;

	@Autowired
	private RiskEditHistoryBS riskEditHistoryBS;

	@Autowired
	private CompanyBS companyBS;
	
	public static final String PATH =  BASEPATH + "/risk";
	
	
	/**
	 * Salvar Novo Risco
	 * 
	 * @Param Risk
	 * 			instancia de um novo risco
	 */
	@PostMapping(PATH + "/new")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> save(@RequestBody RiskDto dto, UserSession userSession){
		try {
			Risk risk = dto.risk();
			Unit unit = this.unitBS.exists(risk.getUnit().getId(), Unit.class);
			if (unit == null || unit.isDeleted()) {
				return this.fail("A unidade associada ao risco não foi encontrada.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !unit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou criar Risco na Unidade ID %d da Empresa ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, unit.getId(), unit.getPlanRisk().getPolicy().getCompany().getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(unit.getPlanRisk().getPolicy().getCompany());

			this.riskBS.createNewRisk(risk);
			if (this.domain != null) {
				User user = riskBS.exists(risk.getUser().getId(), User.class);
				User manager = riskBS.exists(risk.getManager().getId(), User.class);
				String domainUrl = this.domain.get().getBaseUrl();
				this.riskBS.sendUserLinkedToRiskNotification(risk, unit, user, domainUrl);
				this.riskBS.sendUserLinkedToRiskNotification(risk, unit, manager, domainUrl);
			}

			return this.success(risk);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}
	
	
	/**
	 * Retorna risco.
	 * 
	 * @param id
	 *			Id do risco a ser retornado.
	 * @return Risk Retorna o risco de acordo com o id passado.
	 */
	@GetMapping( PATH + "/{id}")
	public ResponseEntity<?> getRisk(@PathVariable Long id, UserSession userSession) {
		try {
			Risk risk = this.riskBS.listRiskById(id);

			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !risk.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar o Risco ID %d da Empresa ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, risk.getId(), risk.getUnit().getPlanRisk().getPolicy().getCompany().getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(risk.getUnit().getPlanRisk().getPolicy().getCompany());

			risk.setStrategies(this.riskBS.listRiskStrategy(risk));
			risk.setAxes(this.riskBS.listRiskAxis(risk));
			risk.setIndicators(this.riskBS.listRiskIndicators(risk));
			risk.setGoals(this.riskBS.listRiskGoals(risk));
			risk.setActivities(this.riskBS.listRiskActivity(risk));

			if (risk.getResponse() != null && (RiskResponse.SHARE.getId() == risk.getResponse() || RiskResponse.TRANSFER.getId() == risk.getResponse())) {
				risk.setSharedUnits(riskBS.listSharedUnits(risk));
			}
			risk.setProcessObjectives(riskBS.listRiskProcessObjectives(risk));

			return this.success(risk);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna riscos.
	 * 
	 * @param PLanRisk
	 *            Id do plano de risco.
	 * @return <PaginatedList> Unit
	 */
	@GetMapping( PATH + "")
		@CommunityDashboard
		public ResponseEntity<?> listRisksbyPlan(@RequestParam Long planId) {
			try {			
				PlanRisk plan = this.riskBS.exists(planId, PlanRisk.class);

				domain.validateTenant(plan.getPolicy().getCompany());

				PaginatedList<Unit> units = this.unitBS.listUnitsbyPlanRisk(plan);
				
				List<RiskBean> list = this.riskBS.listRiskReducedByUnits(units.getList());
				
				return this.success(new ListWrapper<RiskBean>(list));
			} catch (Throwable ex) {
				LOGGER.error("Unexpected runtime error", ex);
				return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
			}
		}
	
		/**
		 * Retorna riscos pela unidade.
		 *
		 * @param planRiskId Id do plano de risco
		 * @return <PaginatedList> Risk
		 */
		@PreAuthorize(AccessLevels.HAS_ROLE_AUTENTICATED)
		@PostMapping(PATH + "/filter/{planRiskId}")
		public ResponseEntity<?> filterRisksPost(
				@PathVariable Long planRiskId,
				@RequestBody(required = false) RiskFilterParams filterParams,
				@ModelAttribute DefaultParams params,
				UserSession userSession) {

			return filterRisksInternal(planRiskId, filterParams, params, userSession);
		}

		/**
		 * Compatibilidade legada: aceita filtros via querystring (JSON em "filters")
		 * e delega para o mesmo fluxo do POST.
		 */
		@PreAuthorize(AccessLevels.HAS_ROLE_AUTENTICATED)
		@GetMapping(PATH + "/filter/{planRiskId}")
		public ResponseEntity<?> filterRisksGet(
				@PathVariable Long planRiskId,
				@RequestParam(required = false) String filters,
				@ModelAttribute DefaultParams params,
				UserSession userSession) {

			RiskFilterParams filterParams = RiskFilterParams.fromJson(filters);
			return filterRisksInternal(planRiskId, filterParams, params, userSession);
		}

		/** Fluxo comum aos dois endpoints */
		private ResponseEntity<?> filterRisksInternal(
				Long planRiskId,
				RiskFilterParams filterParams,
				DefaultParams params,
				UserSession userSession) {

			try {
				if (filterParams == null) {
					filterParams = new RiskFilterParams();
				}

				PlanRisk plan = this.riskBS.exists(planRiskId, PlanRisk.class);
				if (plan == null) {
					return this.fail("O Plano de Risco não foi encontrado.");
				}

				// --- BOLA/IDOR (versão dev) ---
				if (userSession == null || userSession.getAuthData() == null) {
					LOGGER.warn("Acesso sem sessão válida ao filtrar riscos do Plano de Risco ID {}.", plan.getId());
					return this.fail("Acesso negado.");
				}
				boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
				if (!isSystemAdmin) {
					Long loggedInUserId = userSession.getAuthData().getId();
					Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
					if (userCompany == null || !plan.getPolicy().getCompany().getId().equals(userCompany.getId())) {
						LOGGER.warn(String.format(
								"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou filtrar riscos do Plano de Risco ID %d.",
								userSession.getAuthData().getName(), loggedInUserId, plan.getId()));
						return this.fail("Acesso negado.");
					}
				}
				// --- fim BOLA/IDOR ---

				PaginatedList<Risk> risks = this.riskBS.filterRisks(planRiskId, filterParams, params);
				this.riskBS.setRisksMonitoringState(risks.getList());

				return this.success(risks);
			} catch (Throwable ex) {
				LOGGER.error("Unexpected runtime error", ex);
				return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
			}
		}

	/**
	 * Retorna riscos pelo responsavel.
	 * 
	 * @param PLanRisk
	 *            Id do responsavel.
	 * @return <PaginatedList> Risk
	 */
	@GetMapping( PATH + "/listByUser")
	public ResponseEntity<?> listRisksByUser(@RequestParam Long userId, @ModelAttribute DefaultParams params) {
		try {

			User targetUser = this.userBS.exists(userId, User.class);
			CompanyUser companyUser = this.userBS.retrieveCompanyUser(targetUser, this.domain.get().getCompany());

			if (companyUser == null) {
            	return this.forbidden();
        	}

			if (!userBS.isLoggedUserOrHasAccess(userId, AccessLevels.COMPANY_ADMIN)) {
				return this.forbidden();
			}

			PaginatedList<Risk> risks = this.riskBS.listRiskByUser(userId, params);
			return this.success(risks.getList(), risks.getTotal());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna riscos pela unidade.
	 * 
	 * @param PLanRisk
	 *            Id da unidade.
	 * @return <PaginatedList> Risk
	 */
	@GetMapping( PATH + "/listbysubunits/{unitId}")
	public ResponseEntity<?> listSubunitsRisksByUnit(@PathVariable Long unitId, UserSession userSession) {
		try {
			Unit subunit = this.riskBS.exists(unitId, Unit.class);
			if (subunit == null || subunit.isDeleted()) {
				return this.fail("A Subunidade não foi encontrada.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !subunit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou listar riscos da Subunidade ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, subunit.getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(subunit.getPlanRisk().getPolicy().getCompany());

			PaginatedList<Risk> risks = this.riskBS.listRiskByUnit(subunit);
			return this.success(risks);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Endpoint para listar todos os riscos não arquivados.
	 * 
	 * @return Lista de riscos não arquivados.
	 */
	@GetMapping(PATH + "/unarchivedrisklisted") //segurança
	public ResponseEntity<?> listAllUnarchivedRisks() {
		try {
			PaginatedList<Risk> risks = riskBS.listAllRisks();
			return ResponseEntity.ok(risks);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Retorna Risco a partir do nível (level)
	 * 
	 * @param level
	 */
	@GetMapping(PATH + "/listByLevel")
	public ResponseEntity<?> listRiskByLevel(
		@RequestParam Long planId,
		@RequestParam String level,
		@RequestParam(required = false) Integer page,
		@RequestParam(required = false) Integer pageSize,
		UserSession userSession) {

		if (page == null || page < 0)
			page = 0;
		if (pageSize == null) {
			pageSize = Consts.MIN_PAGE_SIZE;
		}

		PlanRisk plan = this.riskBS.exists(planId, PlanRisk.class);
		if (plan == null || plan.isDeleted()) {
			return this.fail("O Plano não foi encontrado.");
		}

		try {
			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !plan.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou listar riscos por nível do Plano de Risco ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, plan.getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(plan.getPolicy().getCompany());
			PaginatedList<Risk> risks = this.riskBS.listRiskByLevel(plan, level, page, pageSize);
			return this.success(risks.getList(), risks.getTotal());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Retorna histórico agregado de riscos por unidade para um Plano de Risco.
	 *
	 * Regras de acesso:
	 *  - Apenas SYSTEM_ADMIN e COMPANY_ADMIN;
	 *  - COMPANY_ADMIN só enxerga dados da própria empresa;
	 *  - Bloqueia completamente acesso anônimo.
	 */
	@GetMapping(PATH + "/history")
	@PreAuthorize(AccessLevels.HAS_ROLE_AUTENTICATED)
	public ResponseEntity<?> listHistory(
			@RequestParam Long planId,
			@RequestParam Long unitId,
			UserSession userSession) {
		try {
			PlanRisk plan = this.riskBS.exists(planId, PlanRisk.class);
			if (plan == null) {
				return this.fail("O plano de risco solicitado não foi encontrado.");
			}

			Company company = plan.getPolicy().getCompany();

			boolean isDashboardCall = (unitId != null && unitId == -1L);

			if (!isDashboardCall) {
				ResponseEntity<?> authError = ensureAdminOfCompany(
					userSession,
					company,
					String.format("o histórico de riscos do Plano de Risco ID %d", planId)
				);
				if (authError != null) return authError;
			}
			domain.validateTenant(company);

			this.unitBS.updateHistory(plan, true);
			this.unitBS.updateHistory(plan, false);

			PaginatedList<Unit> units = new PaginatedList<>();

			if (unitId != null && unitId == -1L) {
				units = this.unitBS.listUnitsbyPlanRisk(plan);
			} else {
				Unit unit = this.riskBS.exists(unitId, Unit.class);
				if (unit == null) {
					return this.fail("A unidade associada ao plano de risco não foi encontrada.");
				}
				domain.validateTenant(unit.getPlanRisk().getPolicy().getCompany());

				List<Unit> list = new ArrayList<>();
				list.add(unit);
				units.setList(list);
			}

			List<RiskHistoryBean> history = this.riskBS.listHistoryByUnits(units);
			return this.success(new ListWrapper<RiskHistoryBean>(history));

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

		
	/**
	 * Retorna atividades do processo.
	 * 
	 * @param RiskId
	 *			Id do risco.
	 *
	 *
	 * @return <PaginedList> RiskActivity
	 * 			 Retorna lista de atividades do processo
	 */
	@GetMapping( PATH + "/activity")
	public ResponseEntity<?> lisActivity(@RequestParam Long riskId, UserSession userSession) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !risk.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format("Tentativa de acesso indevido a atividades do Risco ID %d.", riskId));
					return this.fail("Acesso negado.");
				}
			}

			PaginatedList<RiskActivity> activities = this.riskBS.listActivityByRisk(risk);
			return this.success(activities);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Atualiza risco.
	 * 
	 * @param id
	 *            Id da ação a ser atualizada.
	 * @throws Exception 
	 */
	@PostMapping( PATH + "/update")
	public ResponseEntity<?> updateRisk(@RequestBody RiskDto dto, UserSession userSession) throws Exception {
		try {
			Risk risk = dto.risk();
			Risk existent = this.riskBS.exists(risk.getId(), Risk.class);
			if (existent == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !existent.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format("Tentativa de acesso indevido para atualizar o Risco ID %d.", existent.getId()));
					return this.fail("Acesso negado.");
				}
			}

			this.riskBS.updateRisk(risk);
			return this.success(risk);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Arquivar um risco pelo seu id.
	 * 
	 * @param riskId
	 *               Id do risco.
	 */
	@PostMapping(PATH + "/archive/{riskId}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> archiveRisk(@PathVariable Long riskId, UserSession userSession) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !risk.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format("Tentativa de acesso indevido para arquivar o Risco ID %d.", riskId));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(risk.getUnit().getPlanRisk().getPolicy().getCompany());

			if (risk.isArchived()) {
				return this.fail("O risco já encontra-se arquivado.");
			}

			risk.setArchived(true);
			this.riskBS.saveRisk(risk);
			return this.success(risk);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Desarquivar/Publicar um risco pelo seu id.
	 * 
	 * @param riskId
	 *               Id do risco.
	 */
	@PostMapping(PATH + "/unarchive/{riskId}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> unarchiveRisk(@PathVariable Long riskId, UserSession userSession) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !risk.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format("Tentativa de acesso indevido para desarquivar o Risco ID %d.", riskId));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(risk.getUnit().getPlanRisk().getPolicy().getCompany());

			if (!risk.isArchived()) {
				return this.fail("O risco já encontra-se publicado.");
			}

			risk.setArchived(false);
			this.riskBS.saveRisk(risk);
			return this.success(risk);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Exclui um risco.
	 * 
	 * @param id
	 *           Id do risco.
	 */
	@DeleteMapping(PATH + "/{riskId}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> deleteRisk(@PathVariable Long riskId, UserSession userSession) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !risk.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format("Tentativa de acesso indevido para deletar o Risco ID %d.", riskId));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(risk.getUnit().getPlanRisk().getPolicy().getCompany());

			this.riskBS.deleteAPS(risk);

			PaginatedList<Monitor> monitors = this.monitorBS.listMonitorByRisk(risk);
			PaginatedList<Incident> incidents = this.incidentBS.listIncidentsByRisk(risk);
			PaginatedList<Contingency> contingencies = this.contingencyBS.listContingenciesByRisk(risk);
			PaginatedList<PreventiveAction> actions = this.preventiveActionBS.listPreventiveActionByRisk(risk);

			for (Monitor monitor : monitors.getList()) {
				this.monitorBS.delete(monitor);
			}
			for (Incident incident : incidents.getList()) {
				this.incidentBS.delete(incident);
			}
			for (Contingency contingency : contingencies.getList()) {
				this.contingencyBS.delete(contingency);
			}
			for (PreventiveAction action : actions.getList()) {
				this.preventiveActionBS.delete(action);
			}

			this.riskBS.delete(risk);
			return this.success(risk);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	@PostMapping(PATH + "/replicate")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> replicateRisk(@RequestBody RiskReplicateDto dto, UserSession userSession)
			throws Exception {
		try {
			Risk sourceRisk = this.riskBS.exists(dto.risk().getId(), Risk.class);
			if (sourceRisk == null) {
				return this.fail("O risco de origem não foi encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					return this.fail("Acesso negado.");
				}
				if (!sourceRisk.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format("Tentativa de acesso indevido para replicar o Risco ID %d.",
							sourceRisk.getId()));
					return this.fail("Acesso negado.");
				}
				for (Long targetUnitId : dto.targetUnitIds()) {
					Unit targetUnit = this.unitBS.exists(targetUnitId, Unit.class);
					if (targetUnit != null
							&& !targetUnit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
						LOGGER.warn(String.format("Tentativa de acesso indevido para replicar para a Unidade ID %d.",
								targetUnitId));
						return this.fail("Acesso negado.");
					}
				}
			}

			domain.validateTenant(sourceRisk.getUnit().getPlanRisk().getPolicy().getCompany());

			for (Long targetUnitId : dto.targetUnitIds()) {
				Unit targetUnit = this.unitBS.exists(targetUnitId, Unit.class);
				if (targetUnit == null) {
					return this.fail("Uma das unidades de destino não foi encontrada: ID " + targetUnitId);
				}
				domain.validateTenant(targetUnit.getPlanRisk().getPolicy().getCompany());
			}
			dto.risk().setUnit(sourceRisk.getUnit());
			riskReplicationBS.replicateRisksInUnits(dto.risk(), dto.targetUnitIds());
			return this.success();
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Retorna histórico de edições do Risco (HU02).
	 */
	@GetMapping(PATH + "/edit-history")
	@PreAuthorize(AccessLevels.HAS_ROLE_AUTENTICATED)
	public ResponseEntity<?> listEditHistory(
			@RequestParam Long riskId,
			@ModelAttribute DefaultParams params,
			UserSession userSession) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			}

			Company company = risk.getUnit().getPlanRisk().getPolicy().getCompany();

			ResponseEntity<?> authError = ensureAdminOfCompany(
					userSession,
					company,
					String.format("o histórico de edições do Risco ID %d", riskId)
			);
			if (authError != null) {
				return authError;
			}

			domain.validateTenant(company);

			PaginatedList<org.forrisco.risk.history.RiskEditHistoryBS.RiskEditHistoryDTO> history =
					this.riskEditHistoryBS.listHistoryByRisk(riskId, params);
			return this.success(history);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	/**
	 * Valida se o usuário atual é administrador (da empresa) ou administrador de sistema
	 * e se está autorizado a acessar recursos da empresa informada.
	 *
	 * - Retorna null quando o acesso é permitido;
	 * - Retorna ResponseEntity com erro ("Acesso negado.") quando o acesso é proibido.
	 */
	private ResponseEntity<?> ensureAdminOfCompany(
			UserSession userSession,
			Company targetCompany,
			String contextDescription) {

		if (userSession == null || userSession.getAuthentication() == null) {
			LOGGER.warn("Acesso sem sessão válida a {}.", contextDescription);
			return this.fail(
				"Acesso negado — apenas administradores e administradores de sistema podem acessar o histórico de riscos."
			);
		}

		int accessLevel = userSession.getAccessLevel();
		boolean isSystemAdmin  = (accessLevel == AccessLevels.SYSTEM_ADMIN.getLevel());
		boolean isCompanyAdmin = (accessLevel == AccessLevels.COMPANY_ADMIN.getLevel());

		if (!isSystemAdmin && !isCompanyAdmin) {
			LOGGER.warn(
				"Usuário '{}' (ID: {}) tentou acessar {} sem perfil de administrador (nível atual: {}).",
				userSession.getAuthData().getName(),
				userSession.getAuthData().getId(),
				contextDescription,
				accessLevel
			);
			return this.fail(
				"Acesso negado — apenas administradores e administradores de sistema podem acessar o histórico de riscos."
			);
		}

		if (!isSystemAdmin) {
			Long loggedInUserId = userSession.getAuthData().getId();
			Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

			if (userCompany == null || !targetCompany.getId().equals(userCompany.getId())) {
				LOGGER.warn(
					"Tentativa de acesso indevido. Usuário '{}' (ID: {}) tentou acessar {} da Empresa ID {}.",
					userSession.getAuthData().getName(),
					loggedInUserId,
					contextDescription,
					targetCompany.getId()
				);
				return this.fail(
					"Acesso negado — apenas administradores e administradores de sistema podem acessar o histórico de riscos."
				);
			}
		}

		return null;
	}

}