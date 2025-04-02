package org.forrisco.risk.monitor;

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
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.system.ErrorMessages;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskController;
import org.forrisco.risk.RiskItemPermissionInfo;
import org.forrisco.risk.monitor.dto.MonitorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
public class MonitorController extends AbstractController {
	
	public static final String PATH =  RiskController.PATH;

	@Autowired
	private MonitorBS bs;
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private RiskBS riskBS;
	@Autowired
	private UnitBS unitBS;
	@Autowired
    private MonitorBS monitorBS;
	
	/**
	 * Salvar Novo monitoramento
	 * 
	 * @Param Monitor
	 * 			instancia de uma novo monitoramento
	 */
	@PostMapping(PATH + "/monitornew")
	public ResponseEntity<?> save(@RequestBody MonitorDto dto){
		try {
			Monitor monitor = dto.monitor();

			monitor.setReport(SanitizeUtil.sanitize(monitor.getReport()));
			
			if (monitor.getBegin().after(new Date())) {
				return this.fail("A data e hora do monitoramento não deve ser maior que a data e hora atual.");
			}
			
			Risk risk = this.bs.exists(monitor.getRisk().getId(), Risk.class);
			if (risk == null) {
				return this.fail("Risco solicitado não foi encontrado.");
			}
			
			User user = this.bs.exists(monitor.getUser().getId(), User.class);
			if (user == null) {
				return this.fail("Usuário solicitada não foi encontrado.");
			}
			
			User manager = this.bs.exists(monitor.getManager().getId(), User.class);
			try {
				this.riskBS.validateManager(manager);
			} catch (IllegalStateException ex) {
				return this.fail(ex.getMessage());
			}
			
			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(risk, user);
			
            if (!permissionInfo.isRiskResponsibleOrHasPermission()) {
            	return this.fail("Usuário sem permissão de acesso");
            }
			
			monitor.setId(null);
			
			this.bs.saveMonitor(monitor);
			
			risk.setImpact(monitor.getImpact());
			risk.setProbability(monitor.getProbability());
			risk.setRiskLevel(this.riskBS.getRiskLevelByRisk(risk, null));
			this.riskBS.persist(risk);
			
			if (this.domain != null) {
				String domainUrl = this.domain.get().getBaseUrl();
				String monitorUrl = domainUrl + "/#/forrisco/" + "risk/" + risk.getId()
					+ "/monitors/" + monitor.getId() + "/info";

				this.riskBS.sendNotificationToRiskItemManager(
					risk,
					manager,
					monitorUrl,
					"um Monitoramento",
					NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM
				);
			}
		
			return this.success(monitor);
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
	@GetMapping( PATH + "/monitor")
	public ResponseEntity<?> listMonitor(@RequestParam Long riskId, @ModelAttribute DefaultParams params) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null || risk.isDeleted()) {
				return this.fail("O risco solicitado não foi encontrado.");
			}
			
			PaginatedList<Monitor> monitor = this.bs.listMonitorByRisk(risk, params);
			 
			return this.success(monitor);
		} catch (Throwable ex) {
				LOGGER.error("Unexpected runtime error", ex);
				return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna monitor.
	 * 
	 * @param monitorId
	 *            Id do monitor a ser retornado.
	 */
	@GetMapping( PATH + "/monitor/{monitorId}")
	public ResponseEntity<?> findMonitor(@PathVariable Long monitorId) {
		try {
			Monitor monitor = this.riskBS.exists(monitorId, Monitor.class);
			if (monitor == null || monitor.isDeleted()) {
				return this.fail("O monitor solicitado não foi encontrado.");
			}
			return this.success(monitor);
		} catch (Throwable ex) {
				LOGGER.error("Unexpected runtime error", ex);
				return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna monitoramentos.
	 * 
	 * @param planId
	 *			Id do plano.
	 * @return <PaginedList> Monitor
	 * 			 Retorna lista de monitoramentos do risco.
	 */
	@GetMapping( PATH + "/monitors")
	public ResponseEntity<?> listMonitors(@RequestParam Long planId) {
		try {
			PlanRisk plan = this.riskBS.exists(planId, PlanRisk.class);
			if (plan == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			} 
			
			PaginatedList<Unit> units= this.unitBS.listUnitsbyPlanRisk(plan);
			
			List<Risk> risks = new ArrayList<>();
			List<Monitor> monitors = new ArrayList<>();
		
			
			 for(Unit unit : units.getList()) {
				 PaginatedList<Risk> list= this.riskBS.listRiskByUnit(unit);
				 risks.addAll(list.getList());
			 }
			 
			 for(Risk risk : risks) {
				 PaginatedList<Monitor> list = this.bs.listMonitorByRisk(risk);
				 monitors.addAll(list.getList());
			 }
			 
			 for(Monitor monitor : monitors) {
				 monitor.setUnitId(monitor.getRisk().getUnit().getId());
			 }
	
			PaginatedList<Monitor> monitor = new  PaginatedList<Monitor>();
			
			monitor.setList(monitors);
			monitor.setTotal((long) monitors.size());
			
			return this.success(monitor);
			
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna historico.
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
	@GetMapping( PATH + "/monitorHistory")
	public ResponseEntity<?> listMonitorHistory(@RequestParam Long planId, @RequestParam Long unitId) {
		try {
			PaginatedList<Unit> units= new PaginatedList<Unit>();
			
			PlanRisk plan = this.riskBS.exists(planId, PlanRisk.class);
			if (plan == null) {
				return this.fail("O plano de risco solicitado não foi encontrado.");
			} 
			
			this.bs.updateMonitorHistory(plan);
			
			
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
			
			List<MonitorHistoryBean> history = this.bs.listMonitorHistoryByUnits(units);
						
			return this.success(new ListWrapper<MonitorHistoryBean>(history));
			
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Atualiza monitoramento.
	 * 
	 * @param id
	 *            Id do monitoramento a ser atualizado.
	 */
	@PostMapping( PATH + "/monitor/update")
	public ResponseEntity<?> updateMonitor(@RequestBody MonitorDto dto) {
		try {
			Monitor monitor = dto.monitor();

			if (monitor.getBegin().after(new Date())) {
				return this.fail("A data do monitor não deve ser maior que a data atual.");
			}

			Monitor oldMonitor = this.riskBS.exists(monitor.getId(), Monitor.class);
			if (oldMonitor == null) {
				return this.fail("O monitoramento solicitado não foi encontrado.");
			}
			
			User user = this.riskBS.exists(monitor.getUser().getId(), User.class);
			if (user == null) {
				return this.fail("O 'responsável técnico' solicitado não foi encontrado.");
			}

			User manager = this.riskBS.exists(monitor.getManager().getId(), User.class);
			try {
				this.riskBS.validateManager(manager);
			} catch (IllegalStateException ex) {
				return this.fail(ex.getMessage());
			}
			
			Risk risk = this.riskBS.exists(oldMonitor.getRisk().getId(), Risk.class);
			if (risk == null) {
			    return this.fail("O risco solicitado não foi encontrado.");
			} 

			boolean managerChanged = oldMonitor.getManager() == null || !oldMonitor.getManager().equals(manager);

			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(risk, oldMonitor.getUser());

            if (permissionInfo.isRiskResponsibleOrHasPermission()) {
                oldMonitor.setUser(user);
				oldMonitor.setManager(manager);
            }
            
			oldMonitor.setImpact(monitor.getImpact());
			oldMonitor.setProbability(monitor.getProbability());
			oldMonitor.setReport(SanitizeUtil.sanitize(monitor.getReport()));
			oldMonitor.setBegin(monitor.getBegin());
			this.bs.saveMonitor(oldMonitor);
			
			
			risk.setImpact(monitor.getImpact());
			risk.setProbability(monitor.getProbability());
			risk.setRiskLevel(this.riskBS.getRiskLevelByRisk(risk,null));
			this.riskBS.persist(risk);
			
			if (this.domain != null) {
				String domainUrl = this.domain.get().getBaseUrl();
				String monitorUrl = domainUrl + "/#/forrisco/" + "risk/" + risk.getId()
						+ "/monitors/" + monitor.getId() + "/info";
						
				NotificationType notificationType;
				String notificationAuxText = "um Monitoramento";
				if (managerChanged) {
					notificationType = NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM;
				} else {
					notificationType = NotificationType.FORRISCO_MANAGER_RISK_ITEM_UPDATED;
					notificationAuxText = StringUtils.capitalize(notificationAuxText);
				}  

				this.riskBS.sendNotificationToRiskItemManager(
					risk,
					manager,
					monitorUrl,
					notificationAuxText,
					notificationType
				);
			}
			
			return this.success(oldMonitor);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}
	
	/**
	 * Exclui monitoramento.
	 * 
	 * @param id
	 *            Id do monitoramento a ser excluido.
	 */
	@DeleteMapping( PATH + "/monitor/{monitorId}")
	public ResponseEntity<?> deleteMonitor(@PathVariable Long monitorId) {
		try {
			Monitor monitor = this.riskBS.exists(monitorId, Monitor.class);
			if (monitor == null) {
				return this.fail("O monitoramento solicitado não foi encontrado.");
			} 
			
			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(monitor.getRisk(), monitor.getUser());
			
            if (!permissionInfo.isRiskResponsibleOrHasPermission()) {
            	return this.fail("Usuário sem permissão de acesso");
            }

			bs.delete(monitor);
			return this.success(monitor);
			
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

    /**
     * Listar todos os monitoramentos não arquivados
     * 
     * @return Lista de monitoramentos não arquivados
     */
    @GetMapping(PATH + "/all-monitors")
    public ResponseEntity<PaginatedList<Monitor>> listAllMonitors() {
        try {
            PaginatedList<Monitor> monitors = monitorBS.listAllMonitors();
            return ResponseEntity.ok(monitors);
        } catch (Throwable ex) {
            LOGGER.error("Unexpected runtime error", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
