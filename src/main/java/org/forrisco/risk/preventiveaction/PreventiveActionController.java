package org.forrisco.risk.preventiveaction;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.storage.file.ArchiveBS;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskController;
import org.forrisco.risk.RiskItemPermissionInfo;
import org.forrisco.risk.preventiveaction.dto.PreventiveActionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.cloudfoundry.AccessLevel;
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
	@Autowired
	private CompanyBS companyBS;
	@Autowired
	private PreventiveActionHistoryBS preventiveActionHistoryBS;
	@Autowired
	private UserBS userBS;

	/**
	 * Salvar Nova ação de prevenção
	 * 
	 * @Param PreventiveAction
	 * 			instancia de uma nova ação preventiva
	 */
	@PostMapping(PATH + "/actionnew")
	public ResponseEntity<?> save(@RequestBody PreventiveActionDto dto, UserSession userSession) {
		try {
			PreventiveAction action = dto.action();
			if (action.isAccomplished() && action.getFile() == null) {
				return this.fail("O anexo é obrigatório para uma ação realizada.");
			}
			
			Risk risk = this.bs.exists(action.getRisk().getId(), Risk.class);
			if (risk == null) {
				return this.fail("Risco solicitado não foi encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !risk.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
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
			
			Company riskCompany = risk.getUnit().getPlanRisk().getPolicy().getCompany();
			List<CompanyDomain> domains = this.companyBS.retrieveCompanyByDomain(riskCompany);
			if (domains != null && !domains.isEmpty()) {
				String domainUrl = domains.get(0).getBaseUrl();
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
	@GetMapping(PATH + "/action")
	public ResponseEntity<?> listActions(@RequestParam Long riskId, @ModelAttribute DefaultParams params, UserSession userSession) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !risk.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}
			
			PaginatedList<PreventiveAction> actions = bs.listActionByRisk(risk, params);
			
			return this.success(actions.getList(), actions.getTotal());

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
	@GetMapping(PATH + "/action/{actionId}")
	public ResponseEntity<?> findAction(@PathVariable Long actionId, UserSession userSession) {
		try {
			PreventiveAction preventiveAction = bs.findByActionId(actionId);

			if (preventiveAction == null) {
				return this.fail("A ação de prevenção solicitada não foi encontrada.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !preventiveAction.getRisk().getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}
			
			return this.success(preventiveAction);

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
				domain.validateTenant(plan.getPolicy().getCompany());
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
	@PostMapping(PATH + "/action/update")
	public ResponseEntity<?> updateAction(@RequestBody PreventiveActionDto dto, UserSession userSession) {
		try {
			PreventiveAction action = dto.action();
			if (action.isAccomplished() && action.getFile() == null) {
				return this.fail("O anexo é obrigatório para uma ação realizada.");
			}

			PreventiveAction oldAction = this.riskBS.exists(action.getId(), PreventiveAction.class);
			if (oldAction == null) {
				return this.fail("A ação solicitado não foi encontrada.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !oldAction.getRisk().getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			Risk risk = this.riskBS.exists(action.getRisk().getId(), Risk.class);
			if (risk == null) {
				return this.fail("Risco solicitado não foi encontrado.");
			}
			
			User user = this.riskBS.exists(action.getUser().getId(), User.class);
			if (user == null) {
				return this.fail("O 'responsável técnico' solicitado não foi encontrado.");
			}

			User manager = this.riskBS.exists(action.getManager().getId(), User.class);
			try {
				this.riskBS.validateManager(manager);
			} catch (IllegalStateException ex) {
				return this.fail();
			}
			
			try {
				riskBS.validateVigency(action.getValidityBegin(), action.getValidityEnd());
			} catch (IllegalArgumentException e) {
				return this.fail("Erro de validação da ação: " + e.getMessage());
			}

			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(risk, oldAction.getUser());
			boolean managerChanged = oldAction.getManager() == null || !oldAction.getManager().equals(manager);
			boolean canChangeAssignment = permissionInfo.isRiskResponsibleOrHasPermission();	
			
			Archive file = null;
			if (!GeneralUtils.isEmpty(action.getFileLink())) {
				file = archiveBS.getByFileLink(action.getFileLink());
			}

				if (userSession.getAuthData() == null) 
				return this.forbidden();

				long editorId = userSession.getAuthData().getId();
				User editor = this.riskBS.exists(editorId, User.class);
				if (editor == null) return this.forbidden();

			oldAction = this.bs.updateWithHistory(
					oldAction,
					action,
					user,
					manager,
					editor,
					canChangeAssignment
			);

			Company riskCompany = oldAction.getRisk().getUnit().getPlanRisk().getPolicy().getCompany();
			List<CompanyDomain> domains = this.companyBS.retrieveCompanyByDomain(riskCompany);
			if (domains != null && !domains.isEmpty()) {
				String domainUrl = domains.get(0).getBaseUrl();
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
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}

	/**
	 * Exclui ação preventiva.
	 * 
	 * @param id
	 *            Id da ação a ser excluida.
	 */
	@DeleteMapping(PATH + "/action/{actionId}")
	public ResponseEntity<?> deleteAction(@PathVariable Long actionId, UserSession userSession) {
		try {
			PreventiveAction action = this.riskBS.exists(actionId, PreventiveAction.class);
			if (action == null) {
				return this.fail("A ação solicitada não foi encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !action.getRisk().getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
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

	@GetMapping(PATH + "/action/{actionId}/history")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_AUTENTICATED)
	public ResponseEntity<?> listActionHistory(
			@PathVariable Long actionId,
			@ModelAttribute DefaultParams params,
			UserSession userSession) {

		try {
			PreventiveAction action = this.bs.findByActionId(actionId);
			if (action == null || action.isDeleted()) {
				return this.notFound();
			}

			// Deriva o risk a partir da action
			Risk risk = action.getRisk();
			if (risk == null || risk.isDeleted()) {
				return this.fail("O risco associado à ação não foi encontrado.");
			}

			// Validação de tenant/empresa (mesma usada nos outros módulos)
			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long uid = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(uid);
				if (userCompany == null ||
				!risk.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			PaginatedList<PreventiveActionHistoryBS.PreventiveActionHistoryDTO> hist =
				this.preventiveActionHistoryBS.listHistoryByAction(actionId, params);

			return this.success(hist);

		} catch (Throwable ex) {
			LOGGER.error("Erro ao buscar histórico da Ação: " + actionId, ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
}

}
