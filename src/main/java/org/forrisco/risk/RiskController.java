package org.forrisco.risk;

import java.util.ArrayList;
import java.util.List;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.Consts;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.contingency.Contingency;
import org.forrisco.risk.contingency.ContingencyBS;
import org.forrisco.risk.dto.RiskDto;
import org.forrisco.risk.dto.RiskReplicateDto;
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
	
	public static final String PATH =  BASEPATH + "/risk";
	
	
	/**
	 * Salvar Novo Risco
	 * 
	 * @Param Risk
	 * 			instancia de um novo risco
	 */
	@PostMapping(PATH + "/new")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> save(@RequestBody RiskDto dto){
		try {
			Risk risk = dto.risk();
			this.riskBS.createNewRisk(risk);
			if (this.domain != null) {
				Unit unit = this.riskBS.exists(risk.getUnit().getId(), Unit.class);
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
	public ResponseEntity<?> getRisk(@PathVariable Long id) {
		try {
			Risk risk = this.riskBS.listRiskById(id);
	
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			} else {
				
				risk.setStrategies(this.riskBS.listRiskStrategy(risk));
				risk.setAxes(this.riskBS.listRiskAxis(risk));
				risk.setIndicators(this.riskBS.listRiskIndicators(risk));
				risk.setGoals(this.riskBS.listRiskGoals(risk));
				risk.setActivities(this.riskBS.listRiskActivity(risk));
				
				if (risk.getResponse() != null && RiskResponse.SHARE.getId() == risk.getResponse()) {
					risk.setSharedUnits(riskBS.listSharedUnits(risk));
				}
				risk.setProcessObjectives(riskBS.listRiskProcessObjectives(risk));
				
				return this.success(risk);

			}
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
	 * @param unitId
	 *            Id da unidade.
	 *            
	 * @return <PaginatedList> Risk
	 */
	@GetMapping(PATH + "/filter/{planRiskId}")
	public ResponseEntity<?> filterRisks(
			@PathVariable Long planRiskId,
			@RequestParam(required = false) String filters,
			@ModelAttribute DefaultParams params) {

		try {
			RiskFilterParams filterParams = RiskFilterParams.fromJson(filters);

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
	public ResponseEntity<?> listSubunitsRisksByUnit(@PathVariable Long unitId) {
		try {			
			Unit subunit = this.riskBS.exists(unitId, Unit.class);
			if (subunit == null || subunit.isDeleted()) {
				return this.fail("A Subunidade não foi encontrada.");
			}
			/*List<Unit> subunits = this.unitBS.listSubunitByUnit(unit).getList();
			if (GeneralUtils.isEmpty(subunits)) {
				return this.success(new ArrayList<>(0));
			}*/
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
	@GetMapping(PATH + "/unarchivedrisklisted")
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
			@RequestParam(required = false) Integer pageSize) {

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
		
			PaginatedList<Risk> risks = this.riskBS.listRiskByLevel(plan, level, page, pageSize);
			return this.success(risks.getList(), risks.getTotal());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Retorna histórico.
	 * 
	 * @param planId
	 *			Id do plano de risco.
	 *
	 * @param unitId
	 *			Id da unidade.
	 *
	 * @return <PaginedList> RiskHistory
	 * 			 Retorna lista de historicos de riscos
	 */
	@GetMapping( PATH + "/history")
	@CommunityDashboard
	public ResponseEntity<?> listHistory(@RequestParam Long planId, @RequestParam Long unitId) {
		try {
			PaginatedList<Unit> units= new PaginatedList<Unit>();
			PlanRisk plan = this.riskBS.exists(planId, PlanRisk.class);
			if (plan == null) {
				return this.fail("O plano de risco solicitado não foi encontrado.");
			} 
			
			this.unitBS.updateHistory(plan,true);
			this.unitBS.updateHistory(plan,false);
			
			
			if(unitId== -1) {
				units = this.unitBS.listUnitsbyPlanRisk(plan);
			}else {
				Unit unit = this.riskBS.exists(unitId, Unit.class);
				
				if (unit == null) {
					return this.fail("O risco solicitado não foi encontrado.");
				}
				
				List<Unit> list = new ArrayList<Unit>();
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
	public ResponseEntity<?> lisActivity(@RequestParam Long riskId) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
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
	public ResponseEntity<?> updateRisk(@RequestBody RiskDto dto) throws Exception {
		
		try {
			Risk risk = dto.risk();
			Risk existent = this.riskBS.exists(risk.getId(), Risk.class);
			
			if (existent == null) {
				return this.fail("O risco solicitado não foi encontrado.");
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
	public ResponseEntity<?> archiveRisk(@PathVariable Long riskId) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			}
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
	public ResponseEntity<?> unarchiveRisk(@PathVariable Long riskId) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			}
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
	 *            Id do risco.
	 */
	@DeleteMapping( PATH + "/{riskId}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> deleteRisk(@PathVariable Long riskId) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			} 
			
				this.riskBS.deleteAPS(risk);
				
				PaginatedList<Monitor> monitors = this.monitorBS.listMonitorByRisk(risk);
				PaginatedList<Incident> incidents = this.incidentBS.listIncidentsByRisk(risk);
				PaginatedList<Contingency> contingencies = this.contingencyBS.listContingenciesByRisk(risk);
				PaginatedList<PreventiveAction> actions = this.preventiveActionBS.listPreventiveActionByRisk(risk);
			
				 for(Monitor monitor : monitors.getList()) {
					 this.monitorBS.delete(monitor);
				 }
				 
				 for(Incident incident : incidents.getList()) {
					 this.incidentBS.delete(incident);
				 }
				 
				 for(Contingency contingency : contingencies.getList()) {
					 this.contingencyBS.delete(contingency);
				 }
				 
				 for( PreventiveAction action : actions.getList()) {
					 this.preventiveActionBS.delete(action);
				 }
				 
			 
				 this.riskBS.delete(risk);
				 return this.success(risk);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
	
	@PostMapping( PATH + "/replicate")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> replicateRisk(@RequestBody RiskReplicateDto dto) throws Exception {
		try {
			riskReplicationBS.replicateRisksInUnits(dto.risk(), dto.targetUnitIds());
			return this.success();
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
}