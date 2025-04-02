package org.forrisco.risk.preventiveaction;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.storage.file.ArchiveBS;
import org.forpdi.core.user.User;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.system.ErrorMessages;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskController;
import org.forrisco.risk.RiskItemPermissionInfo;
import org.forrisco.risk.preventiveaction.dto.PreventiveActionDto;
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
public class PreventiveActionController extends AbstractController {

	public static final String PATH =  RiskController.PATH;
	
	@Autowired
	private PreventiveActionBS bs;
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private RiskBS riskBS;
	@Autowired
	private ArchiveBS archiveBS;

	/**
	 * Salvar Nova ação de prevenção
	 * 
	 * @Param PreventiveAction
	 * 			instancia de uma nova ação preventiva
	 */
	@PostMapping(PATH + "/actionnew")
	public ResponseEntity<?> save(@RequestBody PreventiveActionDto dto){
		try {
			PreventiveAction action = dto.action();
			if(action.isAccomplished() && action.getFile() == null){
				return this.fail("O anexo é obrigatório para uma ação realizada.");
			}
			
			Risk risk = this.bs.exists(action.getRisk().getId(), Risk.class);
			if (risk == null) {
				return this.fail("Risco solicitado não foi encontrado.");
			}
			
			User user = this.bs.exists(action.getUser().getId(), User.class);
			if (user == null) {
				return this.fail("Usuário solicitada não foi encontrado.");
			}
			
			User manager = this.bs.exists(action.getManager().getId(), User.class);
			try {
				this.riskBS.validateManager(manager);
			} catch (IllegalStateException ex) {
				return this.fail(ex.getMessage());
			}

			try {
				riskBS.validateVigency(action.getValidityBegin(), action.getValidityEnd());
			} catch (IllegalArgumentException e) {
				return this.fail("Erro de validação da ação: " + e.getMessage());
			}
			
			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(risk, user);
			
            if (!permissionInfo.isRiskResponsibleOrHasPermission()) {
            	return this.fail("Usuário sem permissão de acesso");
            }
			
			action.setId(null);
			this.bs.saveAction(action);
			
			if (this.domain != null) {
				String domainUrl = this.domain.get().getBaseUrl();
				String actionUrl = domainUrl + "/#/forrisco/" + "risk/" + risk.getId()
					+ "/preventiveAction/" + action.getId() + "/info";

				this.riskBS.sendNotificationToRiskItemManager(
					risk,
					manager,
					actionUrl,
					"uma Ação de Prevenção",
					NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM
				);
			}
			
			return this.success(action);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Retorna ações de prevenção.
	 * 
	 * @param id
	 *			Id do risco.
	 * @return <PaginedList> PreventiveAcion
	 * 			 Retorna lista de ações de prevenção do risco.
	 */
	@GetMapping( PATH + "/action")
	public ResponseEntity<?> listActions(@RequestParam Long riskId, @ModelAttribute DefaultParams params) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			} 
			
			PaginatedList<PreventiveAction> actions = bs.listActionByRisk(risk, params);
			
			return this.success(actions);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna Ação de Prevenção.
	 * 
	 * @param actionId Id da ação de prevenção buscada
	 * 
	 * @return PreventiveAction Retorna a ação de prevenção de acordo com o id passado.
	 */
	@GetMapping( PATH + "/action/{actionId}")
	public ResponseEntity<?> findAction(@PathVariable Long actionId) {
		try {
			PreventiveAction preventiveAction = bs.findByActionId(actionId);
	
			if (preventiveAction == null) {
				return this.fail("A ação de prevenção solicitada não foi encontrada.");
			} else {
				return this.success(preventiveAction);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna plano de ações de uma unidade
	 * 
	 * @param planId
	 *			Id do plano.
	 * @return <PaginedList> PreventiveAction
	 * 			 Retorna lista de planos de ação do risco.
	 */
	@GetMapping( PATH + "/filteredActions")
	@CommunityDashboard
	public ResponseEntity<?> listFiltredActions(@RequestParam Long planId) {
		try {
			PlanRisk plan = this.riskBS.exists(planId, PlanRisk.class);
			if (plan == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			} 
			
			 List<PreventiveActionBean> actions = bs.listActionsByPlanRisk(plan);
			 
			return this.success(new ListWrapper<PreventiveActionBean>(actions));
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Atualiza ação preventiva.
	 * 
	 * @param id
	 *            Id da ação a ser atualizada.
	 */
	@PostMapping( PATH + "/action/update")
	public ResponseEntity<?> updateAction(@RequestBody PreventiveActionDto dto) {
		try {
			PreventiveAction action = dto.action();
			if(action.isAccomplished() && action.getFile() == null){
				return this.fail("O anexo é obrigatório para uma ação realizada.");
			}

			PreventiveAction oldAction = this.riskBS.exists(action.getId(), PreventiveAction.class);
			Risk risk = this.riskBS.exists(action.getRisk().getId(), Risk.class);
			if (risk == null) {
				return this.fail("Risco solicitado não foi encontrado.");
			}
			
			if (oldAction == null) {
				return this.fail("A ação solicitado não foi encontrada.");
			}
			
			User user = this.riskBS.exists(action.getUser().getId(), User.class);
			if (user == null) {
				return this.fail("O 'responsável técnico' solicitado não foi encontrado.");
			}

			User manager = this.riskBS.exists(action.getManager().getId(), User.class);
			try {
				this.riskBS.validateManager(manager);
			} catch (IllegalStateException ex) {
				return this.fail(ex.getMessage());
			}
			
			try {
				riskBS.validateVigency(action.getValidityBegin(), action.getValidityEnd());
			} catch (IllegalArgumentException e) {
				return this.fail("Erro de validação da ação: " + e.getMessage());
			}

			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(risk, oldAction.getUser());
			boolean managerChanged = oldAction.getManager() == null || !oldAction.getManager().equals(manager);
			
            if (permissionInfo.isRiskResponsibleOrHasPermission()) {
                oldAction.setUser(user);
				oldAction.setManager(manager);
            }

            Archive file = null;
			if (!GeneralUtils.isEmpty(action.getFileLink())) {
				file = archiveBS.getByFileLink(action.getFileLink());
			}

			oldAction.setFileLink(action.getFileLink());
			oldAction.setFile(file);
			oldAction.setAccomplished(action.isAccomplished());
			oldAction.setAction(action.getAction());
			oldAction.setValidityBegin(action.getValidityBegin());
			oldAction.setValidityEnd(action.getValidityEnd());
			this.bs.saveAction(oldAction);

			if (this.domain != null) {
				String domainUrl = this.domain.get().getBaseUrl();
				String actionUrl = domainUrl + "/#/forrisco/" + "risk/" + risk.getId()
					+ "/preventiveAction/" + action.getId() + "/info";
				
				String notificationAuxText = "uma Ação de Prevenção";
				NotificationType notificationType;
				if (managerChanged) {
					notificationType = NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM;
				} else {
					notificationType = NotificationType.FORRISCO_MANAGER_RISK_ITEM_UPDATED;
					notificationAuxText = StringUtils.capitalize(notificationAuxText);
				}  

				this.riskBS.sendNotificationToRiskItemManager(
					risk,
					manager,
					actionUrl,
					notificationAuxText,
					notificationType
				);
			}
			
			return this.success(oldAction);
		} catch (Throwable ex) {
			return this.fail(ex.getMessage());
		}
	}

	/**
	 * Exclui ação preventiva.
	 * 
	 * @param id
	 *            Id da ação a ser excluida.
	 */
	@DeleteMapping( PATH + "/action/{actionId}")
	public ResponseEntity<?> deleteAction(@PathVariable Long actionId) {
		try {
			PreventiveAction action = this.riskBS.exists(actionId, PreventiveAction.class);
			if (action == null) {
				return this.fail("A ação solicitada não foi encontrado.");
			} 

			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(action.getRisk(), action.getUser());
			
            if (!permissionInfo.isRiskResponsibleOrHasPermission()) {
            	return this.fail("Usuário sem permissão de acesso");
            }
			
			bs.delete(action);
			return this.success(action);
			
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
}
