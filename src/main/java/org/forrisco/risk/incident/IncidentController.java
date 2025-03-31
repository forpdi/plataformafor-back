package org.forrisco.risk.incident;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.user.User;
import org.forpdi.core.utils.Consts;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.system.ErrorMessages;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskController;
import org.forrisco.risk.RiskItemPermissionInfo;
import org.forrisco.risk.incident.dto.IncidentDto;
import org.forrisco.risk.incident.dto.ListIncidentsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class IncidentController extends AbstractController {

	public static final String PATH =  RiskController.PATH;
	
	@Autowired
	private IncidentBS bs;
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private RiskBS riskBS;

	/**
	 * Salvar Novo incidente
	 * 
	 * @Param Incident
	 * 			instancia de uma novo incidente
	 */
	@PostMapping(PATH + "/incidentnew")
	public ResponseEntity<?> save(@RequestBody IncidentDto dto){
		try {
			Incident incident = dto.incident();
			if (incident.getBegin().after(new Date())) {
				return this.fail("A data e hora do incidente não deve ser maior que a data e hora atual.");
			}

			Risk risk = this.bs.exists(incident.getRisk().getId(), Risk.class);
			if (risk == null) {
				return this.fail("Risco solicitado não foi encontrado.");
			}
			
			User user = this.bs.exists(incident.getUser().getId(), User.class);
			if (user == null) {
				return this.fail("Usuário solicitada não foi encontrado.");
			}
			
			User manager = this.bs.exists(incident.getManager().getId(), User.class);
			try {
				this.riskBS.validateManager(manager);
			} catch (IllegalStateException ex) {
				return this.fail(ex.getMessage());
			}

			if (IncidentType.getById(incident.getType()) == null) {
				return this.fail("Tipo de incidente inválido.");
			}
			
			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(risk, user);
			
            if (!permissionInfo.isRiskResponsibleOrHasPermission()) {
            	return this.fail("Usuário sem permissão de acesso");
            }

			incident.setId(null);
//			incident.setBegin(new Date());
			incident.setDescription(SanitizeUtil.sanitize(incident.getDescription()));
			
			this.bs.saveIncident(incident);
			
			if (this.domain != null) {
				String domainUrl = this.domain.get().getBaseUrl();
				String incidentUrl = domainUrl + "/#/forrisco/" + "risk/" + risk.getId()
					+ "/incident/" + incident.getId() + "/info";

				this.riskBS.sendNotificationToRiskItemManager(
					risk,
					manager,
					incidentUrl,
					"um Incidente",
					NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM
				);
			}
			
			return this.success(incident);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Retorna monitoramento.
	 * 
	 * @param id
	 *            Id do monitoramento a ser retornado.
	 */
	@GetMapping( PATH + "/incident")
	public ResponseEntity<?> listIncident(@RequestParam Long riskId, @ModelAttribute DefaultParams params) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null || risk.isDeleted()) {
				return this.fail("O risco solicitado não foi encontrado.");
			} 
			 PaginatedList<Incident> incident = this.bs.listIncidentsByRisk(risk, params);
			 
			return this.success(incident);
		} catch (Throwable ex) {
				LOGGER.error("Unexpected runtime error", ex);
				return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna incidente.
	 * 
	 * @param incidentId
	 *            Id do incidente a ser retornado.
	 */
	@GetMapping( PATH + "/incident/{incidentId}")
	public ResponseEntity<?> findIncident(@PathVariable Long incidentId) {
		try {
			Incident incident = this.riskBS.exists(incidentId, Incident.class);
			if (incident == null || incident.isDeleted()) {
				return this.fail("O incidente solicitado não foi encontrado.");
			}
			return this.success(incident);
		} catch (Throwable ex) {
				LOGGER.error("Unexpected runtime error", ex);
				return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna incidentes.
	 * 
	 * @param planId
	 *			Id do plano.
	 * @return <PaginedList> Incident
	 * 			 Retorna lista de incidentes do risco.
	 */
	@GetMapping( PATH + "/incidents")
	@CommunityDashboard
	public ResponseEntity<?> listIncidents(@RequestParam Long planId) {
		try {
			PlanRisk plan = this.riskBS.exists(planId, PlanRisk.class);
			if (plan == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			} 
			
			 List<IncidentBean> list = this.bs.listIncidentsByPlanRisk(plan);
	
			return this.success(new ListWrapper<IncidentBean>(list));
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna incidentes em uma lista paginada.
	 * 
	 * @param planId
	 *			Id do plano.
	 * @return <PaginedList> Incident
	 * 			 Retorna lista de incidentes do risco.
	 */
	@PostMapping( PATH + "/incidentsPaginated")
	public ResponseEntity<?> listIncidentsPaginated(@RequestBody ListIncidentsDto dto) {
		try {
			return this.success(this.bs.pagitaneIncidents(dto.incidentsId(), dto.page(), dto.pageSize()));
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna riscos a partir de uma unidade
	 * @param unitId
	 */
	
	@GetMapping( PATH + "/incidentByUnit")
	public ResponseEntity<?> listIncidentsByUnit(@RequestParam Long unitId) {
		
		try {
			Unit unit = this.riskBS.exists(unitId, Unit.class);
			List<Risk> risks = new ArrayList<>();
			List<Incident> incidents = new ArrayList<>();
			
			if (unit == null) {
				return this.fail("A unidade solicitada não foi encontrado.");
			}
			
			PaginatedList<Risk> listRisk = this.riskBS.listRiskByUnit(unit);
			risks.addAll(listRisk.getList());
			
			for(Risk risk : risks) {
				PaginatedList<Incident> list = this.bs.listIncidentsByRisk(risk);
				incidents.addAll(list.getList());
			}
			
			PaginatedList<Incident> incident = new  PaginatedList<Incident>();
			
			incident.setList(incidents);
			incident.setTotal((long) incidents.size());
			
			return this.success(incident);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
		
	}

	/**
	 * Atualiza incidente.
	 * 
	 * @param id
	 *            Id do incidente a ser atualizado.
	 */
	@PostMapping( PATH + "/incident/update")
	public ResponseEntity<?> updateIncident(@RequestBody IncidentDto dto) {
		try {
			Incident incident = dto.incident();
			if (incident.getBegin().after(new Date())) {
				return this.fail("A data do incidente não deve ser maior que a data atual.");
			}
			
			Risk risk = this.riskBS.exists(incident.getRisk().getId(), Risk.class);
			if (risk == null) {
				return this.fail("Risco solicitado não foi encontrado.");
			}
			
			Incident oldIncident = this.riskBS.exists(incident.getId(), Incident.class);
			if (oldIncident == null) {
				return this.fail("A ação solicitado não foi encontrada.");
			} 
			
			User user = this.riskBS.exists(incident.getUser().getId(), User.class);
			if (user == null) {
				return this.fail("O 'responsável técnico' solicitado não foi encontrado.");
			}
			
			User manager = this.riskBS.exists(incident.getManager().getId(), User.class);
			try {
				this.riskBS.validateManager(manager);
			} catch (IllegalStateException ex) {
				return this.fail(ex.getMessage());
			}

			if (IncidentType.getById(incident.getType()) == null) {
			    return this.fail("Tipo de incidente inválido.");
			}

			boolean managerChanged = oldIncident.getManager() == null || !oldIncident.getManager().equals(manager);

			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(risk, oldIncident.getUser());

            if (permissionInfo.isRiskResponsibleOrHasPermission()) {
                oldIncident.setUser(user);
				oldIncident.setManager(manager);
            }
            
			oldIncident.setAction(incident.getAction());
			oldIncident.setDescription(SanitizeUtil.sanitize(incident.getDescription()));
			oldIncident.setType(incident.getType());
			oldIncident.setBegin(incident.getBegin());
			
			this.bs.saveIncident(oldIncident);
			
			if (this.domain != null) {
				String domainUrl = this.domain.get().getBaseUrl();
				String incidentUrl = domainUrl + "/#/forrisco/" + "risk/" + risk.getId()
						+ "/incident/" + incident.getId() + "/info";
				
				
				NotificationType notificationType;
				String notificationAuxText = "um Incidente";
				if (managerChanged) {
					notificationType = NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM;
				} else {
					notificationType = NotificationType.FORRISCO_MANAGER_RISK_ITEM_UPDATED;
					notificationAuxText = StringUtils.capitalize(notificationAuxText);
				}  

				this.riskBS.sendNotificationToRiskItemManager(
					risk,
					manager,
					incidentUrl,
					notificationAuxText,
					notificationType
				);
			}
						
			return this.success(oldIncident);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
	
	/**
	 * Exclui incidente.
	 * 
	 * @param id
	 *            Id do incidente a ser excluido.
	 */
	@DeleteMapping( PATH + "/incident/{incidentId}")
	public ResponseEntity<?> deleteIncident(@PathVariable Long incidentId) {
		try {
			Incident incident = this.riskBS.exists(incidentId, Incident.class);
			if (incident == null) {
				return this.fail("O incidente solicitado não foi encontrado.");
			} 
			
			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(incident.getRisk(), incident.getUser());
			
            if (!permissionInfo.isRiskResponsibleOrHasPermission()) {
            	return this.fail("Usuário sem permissão de acesso");
            }

			bs.delete(incident);
			return this.success(incidentId);
			
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
}
