package org.forrisco.risk.contingency;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.user.User;
import org.forpdi.core.utils.Consts;
import org.forpdi.system.ErrorMessages;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskController;
import org.forrisco.risk.RiskItemPermissionInfo;
import org.forrisco.risk.contingency.dto.ContingencyDto;
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
public class ContingencyController extends AbstractController {

	public static final String PATH =  RiskController.PATH;
	
	@Autowired
	private ContingencyBS bs;
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private RiskBS riskBS;

	/**
	 * Salvar Novo Contingenciamento
	 * 
	 * @Param Contingency
	 * 			instancia de uma novo contingenciamento
	 */
	@PostMapping(PATH + "/contingencynew")
	public ResponseEntity<?> save(@RequestBody ContingencyDto dto){
		try {
			Contingency contingency = dto.contingency();
			Risk risk = this.bs.exists(contingency.getRisk().getId(), Risk.class);
			if (risk == null) {
				return this.fail("Risco solicitado não foi encontrado.");
			}
			
			User user = this.bs.exists(contingency.getUser().getId(), User.class);
			if (user == null) {
				return this.fail("Usuário solicitada não foi encontrado.");
			}
			
			User manager = this.bs.exists(contingency.getManager().getId(), User.class);
			try {
				this.riskBS.validateManager(manager);
			} catch (IllegalStateException ex) {
				return this.fail(ex.getMessage());
			}

			try {
				riskBS.validateVigency(contingency.getValidityBegin(), contingency.getValidityEnd());
			} catch (IllegalArgumentException e) {
				return this.fail("Erro de validação da ação: " + e.getMessage());
			}
			
			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(risk, user);
			
            if (!permissionInfo.isRiskResponsibleOrHasPermission()) {
            	return this.fail("Usuário sem permissão de acesso");
            }

			contingency.setId(null);
			this.bs.saveContingency(contingency);
			
			if (this.domain != null) {
				String domainUrl = this.domain.get().getBaseUrl();
				String contingencyUrl = domainUrl + "/#/forrisco/" + "risk/" + risk.getId()
					+ "/contingency/" + contingency.getId() + "/info";

				this.riskBS.sendNotificationToRiskItemManager(
					risk,
					manager,
					contingencyUrl,
					"um Contingenciamento",
					NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM
				);
			}
			
			return this.success(contingency);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Retorna contingeciamentos.
	 * 
	 * @param id
	 *			Id do risco.
	 * @return <PaginedList> Contingency
	 * 			 Retorna lista de contingenciamentos do risco.
	 */
	@GetMapping(PATH + "/contingency")
	public ResponseEntity<?> listContingencies(@RequestParam Long riskId, @ModelAttribute DefaultParams params) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null) {
				return this.fail("O risco solicitado não foi encontrado.");
			} 
			
			PaginatedList<Contingency> contingencies = this.bs.listContingenciesByRisk(risk, params);
			
			return this.success(contingencies);
			
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna Contingenciamento.
	 * 
	 * @param contingencyId Id do contingenciamento buscado
	 * 
	 * @return Contingency Retorna o contingenciamento de acordo com o id passado.
	 */
	@GetMapping(PATH + "/contingency/{contingencyId}")
	public ResponseEntity<?> findContingency(@PathVariable Long contingencyId) {
		try {
			Contingency contingency = this.bs.findByContingencyId(contingencyId);
	
			if (contingency == null) {
				return this.fail("O contingenciamento solicitado não foi encontrada.");
			} else {
				return this.success(contingency);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Exclui contingenciamento.
	 * 
	 * @param id
	 *            Id do contingenciamento a ser excluido.
	 */
	@DeleteMapping( PATH + "/contingency/{contingencyId}")
	public ResponseEntity<?> deleteContigency(@PathVariable Long contingencyId) {
		try {
			Contingency contingency = this.riskBS.exists(contingencyId, Contingency.class);
			if (contingency == null) {
				return this.fail("O contingenciamento solicitado não foi encontrado.");
			} 
			
			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(contingency.getRisk(), contingency.getUser());
			
            if (!permissionInfo.isRiskResponsibleOrHasPermission()) {
            	return this.fail("Usuário sem permissão de acesso");
            }

			bs.delete(contingency);
			return this.success(contingency);
			
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
	
	/**
	 * Atualiza contingenciamento.
	 * 
	 * @param id
	 *            Id do contingenciamento a ser atualizado.
	 */
	@PostMapping( PATH + "/contingency/update")
	public ResponseEntity<?> updateContingency(@RequestBody ContingencyDto dto) {
		try {
			Contingency contingency = dto.contingency();
			Risk risk = this.riskBS.exists(contingency.getRisk().getId(), Risk.class);
			if (risk == null) {
				return this.fail("Risco solicitado não foi encontrado.");
			}
			
			Contingency oldContingency = this.riskBS.exists(contingency.getId(), Contingency.class);
			if (oldContingency == null) {
				return this.fail("A ação solicitado não foi encontrada.");
			} 
			
			User user = this.riskBS.exists(contingency.getUser().getId(), User.class);
			if (user == null) {
				return this.fail("O 'responsável técnico' solicitado não foi encontrado.");
			}
			
			User manager = this.riskBS.exists(contingency.getManager().getId(), User.class);
			try {
				this.riskBS.validateManager(manager);
			} catch (IllegalStateException ex) {
				return this.fail(ex.getMessage());
			}

			try {
				riskBS.validateVigency(contingency.getValidityBegin(), contingency.getValidityEnd());
			} catch (IllegalArgumentException e) {
				return this.fail("Erro de validação da ação: " + e.getMessage());
			}

			boolean managerChanged = oldContingency.getManager() == null || !oldContingency.getManager().equals(manager);
            
			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(risk, oldContingency.getUser());

            if (permissionInfo.isRiskResponsibleOrHasPermission()) {
                oldContingency.setUser(user);
				oldContingency.setManager(manager);
            }
			
			oldContingency.setAction(contingency.getAction());
			oldContingency.setValidityBegin(contingency.getValidityBegin());
			oldContingency.setValidityEnd(contingency.getValidityEnd());
			
			this.bs.saveContingency(oldContingency);
			
			if (this.domain != null) {
				String domainUrl = this.domain.get().getBaseUrl();
				String contingencyUrl = domainUrl + "/#/forrisco/" + "risk/" + risk.getId()
						+ "/contingency/" + contingency.getId() + "/info";
						
				NotificationType notificationType;
				String notificationAuxText = "um Contingenciamento";
				if (managerChanged) {
					notificationType = NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM;
				} else {
					notificationType = NotificationType.FORRISCO_MANAGER_RISK_ITEM_UPDATED;
					notificationAuxText = StringUtils.capitalize(notificationAuxText);
				}  

				this.riskBS.sendNotificationToRiskItemManager(
					risk,
					manager,
					contingencyUrl,
					notificationAuxText,
					notificationType
				);
			}
			
			return this.success(oldContingency);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
}
