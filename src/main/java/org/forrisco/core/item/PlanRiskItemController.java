package org.forrisco.core.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forrisco.core.item.dto.DuplicateItemDto;
import org.forrisco.core.item.dto.PlanRiskSubItemDto;
import org.forrisco.core.item.history.ItemHistory;
import org.forrisco.core.item.history.ItemHistoryBS;
import org.forrisco.core.item.history.ItemHistoryQueryBS;
import org.forrisco.core.item.history.ItemHistoryScope;
import org.forrisco.core.plan.PlanRisk;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @author Juliano Afonso
 */

@RestController
public class PlanRiskItemController extends AbstractController {

	@Autowired
	private PlanRiskItemBS planRiskItemBS;
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private CompanyBS companyBS;
	@Autowired
	private ItemHistoryBS itemHistoryBS;
	@Autowired
	private ItemHistoryQueryBS itemHistoryQueryBS;
	
	protected static final String PATH = BASEPATH +"/planrisk/item";
	
	/**
	 * Retorna itens.
	 * 
	 * @param PlanRisk Id do plano de risco a ser retornado.
	 * @return <List> item
	 */
	@GetMapping(PATH + "")
	public ResponseEntity<?> listItens(@RequestParam Long planRiskId, @ModelAttribute DefaultParams params, UserSession userSession) {
		try {
			PlanRisk planRisk = this.planRiskItemBS.exists(planRiskId, PlanRisk.class);
			if (planRisk == null) {
				return this.fail("Plano de Risco não encontrado");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !planRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou listar itens do Plano de Risco ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, planRiskId
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(planRisk.getPolicy().getCompany());
			PaginatedList<PlanRiskItem> items = this.planRiskItemBS.getPlanRiskItemsWithDetails(planRisk, params);
			return this.success(items);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
		
	/**
	 * Salva um item
	 *  
	 * @return ResponseEntity<?>
	 */
	@PostMapping(PATH + "/new")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> saveItem(@RequestBody PlanRiskItem planRiskItem, UserSession userSession) {
		try {
			if (planRiskItem.getPlanRisk() == null || planRiskItem.getPlanRisk().getId() == null) {
				return this.fail("Plano de Risco não encontrado no objeto enviado.");
			}
			Long parentId = planRiskItem.getPlanRisk().getId();

			PlanRisk trustedPlanRisk = this.planRiskItemBS.exists(parentId, PlanRisk.class);
			if (trustedPlanRisk == null) {
				return this.fail("Plano de Risco não encontrado no banco de dados.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !trustedPlanRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou salvar item no Plano de Risco ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, parentId
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(trustedPlanRisk.getPolicy().getCompany());

			planRiskItem.setId(null);
			planRiskItem.setPlanRisk(trustedPlanRisk);
			this.planRiskItemBS.save(planRiskItem);

			for (PlanRiskItemField planRiskItemField : planRiskItem.getPlanRiskItemField()) {
				planRiskItemField.setPlanRiskItem(planRiskItem);
				this.planRiskItemBS.save(planRiskItemField);
			}

			return this.success(planRiskItem);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Salva um subitem
	 *  
	 * @return ResponseEntity<?>
	 */
	@PostMapping(PATH + "/new/subitem")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> saveSubItem(@RequestBody PlanRiskSubItemDto dto, UserSession userSession) {
		try {
			PlanRiskSubItem planRiskSubItem = dto.planRiskSubItem();

			if(planRiskSubItem.getPlanRiskItem() == null) {
				return this.fail("Item Vinculado não encontrado");
			}
			PlanRiskItem parentItem = this.planRiskItemBS.exists(planRiskSubItem.getPlanRiskItem().getId(), PlanRiskItem.class);
			if (parentItem == null) {
				return this.fail("O item pai informado é inválido.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !parentItem.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou salvar subitem no Item ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, parentItem.getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(parentItem.getPlanRisk().getPolicy().getCompany());

			planRiskSubItem.setId(null);
			this.planRiskItemBS.save(planRiskSubItem);

			for(PlanRiskSubItemField planRiskSubItemField : planRiskSubItem.getPlanRiskSubItemField()) {
				planRiskSubItemField.setPlanRiskSubItem(planRiskSubItem);
				this.planRiskItemBS.save(planRiskSubItemField);
			}

			return this.success(planRiskSubItem);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Duplica uma lista de itens
	 *  
	 *  @Param List<PlanRiskItem>lista de ites
	 *  @Param PlanRisk plano a ser vinculado o item duplicado
	 *  
	 * @return ResponseEntity<?>
	 */
	@PostMapping(PATH + "/duplicate")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> duplicateItem(@RequestBody DuplicateItemDto dto, UserSession userSession) {
		try {
			List<PlanRiskItem> itens = dto.itens();
			PlanRisk planRisk = dto.planRisk();
			PlanRisk plan = this.planRiskItemBS.exists(planRisk.getId(), PlanRisk.class);

			if(plan == null || plan.isDeleted()) {
				return this.fail("Plano de Risco não encontrado");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			Company userCompany = null;
			Long loggedInUserId = null;
			if (!isSystemAdmin) {
				loggedInUserId = userSession.getAuthData().getId();
				userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					return this.fail("Acesso negado.");
				}
				if (!plan.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn("Tentativa de acesso indevido para duplicar itens para o Plano de Risco ID {}.", plan.getId());
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(plan.getPolicy().getCompany());

			List<PlanRiskItem> result = new ArrayList<>();

			for(PlanRiskItem planRiskItem : itens) {
				PlanRiskItem item = this.planRiskItemBS.exists(planRiskItem.getId(), PlanRiskItem.class);
				if(item == null || item.isDeleted()) {
					continue;
				}

				if (!isSystemAdmin) {
					if (!item.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
						LOGGER.warn("Tentativa de acesso indevido para duplicar o Item ID {}.", item.getId());
						continue;
					}
				}

				domain.validateTenant(item.getPlanRisk().getPolicy().getCompany());

				planRiskItem.setId(null);
				planRiskItem.setDescription(item.getDescription());
				planRiskItem.setName(item.getName());
				planRiskItem.setPlanRisk(plan);
				this.planRiskItemBS.save(planRiskItem);
				result.add(planRiskItem);

				PaginatedList<PlanRiskItemField> fields = this.planRiskItemBS.listFieldsByPlanRiskItem(item);
				for(PlanRiskItemField field :fields.getList()) {
					PlanRiskItemField f= new PlanRiskItemField();
					f.setPlanRiskItem(planRiskItem);
					f.setDescription(field.getDescription());
					f.setFileLink(field.getFileLink());
					f.setName(field.getName());
					f.setText(field.isText());
					f.setValue(field.getValue());
					f.setId(null);
					this.planRiskItemBS.save(f);
				}

				PaginatedList<PlanRiskSubItem> subitens = this.planRiskItemBS.listSubItemByItem(item, null);
				for(PlanRiskSubItem subitem :subitens.getList()) {
					PlanRiskSubItem sub = new PlanRiskSubItem();
					sub.setId(null);
					sub.setPlanRiskItem(planRiskItem);
					sub.setDescription(subitem.getDescription());
					sub.setName(subitem.getName());
					this.planRiskItemBS.save(sub);

					PaginatedList<PlanRiskSubItemField> subfields = this.planRiskItemBS.listSubFieldsBySubItem(subitem);
					for( PlanRiskSubItemField subfield : subfields.getList()) {
						PlanRiskSubItemField sf= new PlanRiskSubItemField();
						sf.setPlanRiskSubItem(sub);
						sf.setDescription(subfield.getDescription());
						sf.setFileLink(subfield.getFileLink());
						sf.setName(subfield.getName());
						sf.setText(subfield.isText());
						sf.setValue(subfield.getValue());
						sf.setId(null);
						this.planRiskItemBS.save(sf);
					}
				}
			}

			PaginatedList<PlanRiskItem> list = new PaginatedList<>();
			list.setList(result);
			list.setTotal((long) result.size());
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	
	/**
	 * Retorna as informações e os Campos de um Subitem
	 * @param id do item a ser consultado
	 *  
	 * @return ResponseEntity<?>
	 */
	@GetMapping(PATH + "/sub-itens/{id}")
	public ResponseEntity<?> listSubitens(@PathVariable Long id, UserSession userSession) {
		try {
			PlanRiskItem planRiskItem = this.planRiskItemBS.exists(id, PlanRiskItem.class);
			if (planRiskItem == null) {
				return this.fail("O Item solicitado não foi encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !planRiskItem.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou listar subitens do Item ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, id
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(planRiskItem.getPlanRisk().getPolicy().getCompany());

			PaginatedList<PlanRiskSubItem> itens = this.planRiskItemBS.listSubItemByItem(planRiskItem, null);

			for (PlanRiskSubItem subItem : itens.getList()) {
				List<PlanRiskSubItemField> fields = this.planRiskItemBS.listSubFieldsBySubItem(subItem).getList();
				if (fields == null) continue;
				for (PlanRiskSubItemField field: fields) {
					if (field.isText()) {
						subItem.setHasText(true);
					} else {
						subItem.setHasFile(true);
					}
				}
			}

			return this.success(itens);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	
	/**
	 * Retorna as informações e os Campos de todos os Subitens
	 * @param id do item a ser consultado
	 *  
	 * @return ResponseEntity<?>
	 */
	@GetMapping(PATH + "/allsubitens/{id}")
	public ResponseEntity<?> listAllSubitens(@PathVariable Long id, UserSession userSession) {
		try {
			PlanRisk planRisk = this.planRiskItemBS.exists(id, PlanRisk.class);
			if (planRisk == null) {
				return this.fail("O Plano solicitado não foi encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !planRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou listar todos os subitens do Plano de Risco ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, id
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(planRisk.getPolicy().getCompany());

			PaginatedList<PlanRiskSubItem> subitens = this.planRiskItemBS.getAllSubitensForPlanRisk(planRisk);
			return this.success(subitens);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	
	/**
	 * Retorna as informaçõesde um Item
	 * @param id do item a ser detalhado
	 *  
	 * @return ResponseEntity<?>
	 */
	@GetMapping(PATH + "/{id}")
	public ResponseEntity<?> detailItem(@PathVariable Long id, UserSession userSession) {
		try {
			PlanRiskItem planRiskItem = this.planRiskItemBS.exists(id, PlanRiskItem.class);
			if (planRiskItem == null) {
				return this.fail("O Item solicitado não foi encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !planRiskItem.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou detalhar o Item ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, id
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(planRiskItem.getPlanRisk().getPolicy().getCompany());

			planRiskItem.setPlanRiskItemField(this.planRiskItemBS.listFieldsByPlanRiskItem(planRiskItem).getList());
			return this.success(planRiskItem);

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}
	
	/**
	 * Retorna as informaçõesde um subitem
	 * @param id do subitem a ser detalhado
	 *  
	 * @return ResponseEntity<?>
	 */
	@GetMapping(PATH + "/subitem/{id}")
	public ResponseEntity<?> detailSubItem(@PathVariable Long id, UserSession userSession) {
		try {
			PlanRiskSubItem planRiskSubItem = this.planRiskItemBS.exists(id, PlanRiskSubItem.class);
			if (planRiskSubItem == null) {
				return this.fail("O Subitem solicitado não foi encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !planRiskSubItem.getPlanRiskItem().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou detalhar o Subitem ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, id
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(planRiskSubItem.getPlanRiskItem().getPlanRisk().getPolicy().getCompany());

			planRiskSubItem.setPlanRiskSubItemField(this.planRiskItemBS.listSubFieldsBySubItem(planRiskSubItem).getList());
			return this.success(planRiskSubItem);

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Atualiza campos e título do item
	 * @param planRiskItem
	 */
	@PostMapping( PATH + "/update")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> updatePlanRiskItem(@RequestBody PlanRiskItem planRiskItem, UserSession userSession) {
		try {
			PlanRiskItem existent = planRiskItemBS.exists(planRiskItem.getId(), PlanRiskItem.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			if (existent.getPlanRisk() == null) {
				return this.fail("Item sem plano de risco associado");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !existent.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou atualizar o Item ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, existent.getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(existent.getPlanRisk().getPolicy().getCompany());

			List<PlanRiskItemField> oldFields = this.planRiskItemBS
					.listFieldsByPlanRiskItem(existent)
					.getList();

			List<PlanRiskItemField> newFields = (planRiskItem.getPlanRiskItemField() == null)
					? java.util.Collections.emptyList()
					: planRiskItem.getPlanRiskItemField();

			org.forpdi.core.user.User responsibleUser = this.planRiskItemBS.exists(userSession.getAuthData().getId(),
					org.forpdi.core.user.User.class);

			this.itemHistoryBS.savePlanRiskItemChanges(
					existent,
					planRiskItem,
					oldFields,
					newFields,
					responsibleUser);

			PaginatedList<PlanRiskItemField> fields = this.planRiskItemBS.listFieldsByPlanRiskItem(existent);
			for (int i = 0; i < fields.getList().size(); i++) {
				this.planRiskItemBS.delete(fields.getList().get(i));
			}

			if (planRiskItem.getPlanRiskItemField() != null) {
				for (int i = 0; i < planRiskItem.getPlanRiskItemField().size(); i++) {
					PlanRiskItemField planRiskItemField = planRiskItem.getPlanRiskItemField().get(i);
					planRiskItemField.setPlanRiskItem(existent);
					this.planRiskItemBS.save(planRiskItemField);
				}
			}

			existent.setDescription(planRiskItem.getDescription());
			existent.setName(planRiskItem.getName());
			this.planRiskItemBS.persist(existent);

			return this.success(existent);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}
	
	/**
	 * Atualiza subitem e seus campos
	 * 
	 * @param planRiskSubItem
	 */
	@PostMapping(PATH + "/update-subitem")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> updatePlanRiskSubItem(@RequestBody PlanRiskSubItemDto dto, UserSession userSession) {
		try {
			PlanRiskSubItem planRiskSubItem = dto.planRiskSubItem();
			PlanRiskSubItem existent = this.planRiskItemBS.exists(planRiskSubItem.getId(), PlanRiskSubItem.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			if (existent.getPlanRiskItem() == null) {
				return this.fail("Subitem sem item associado");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !existent.getPlanRiskItem().getPlanRisk().getPolicy().getCompany().getId()
								.equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou atualizar o Subitem ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, existent.getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(existent.getPlanRiskItem().getPlanRisk().getPolicy().getCompany());

			PaginatedList<PlanRiskSubItemField> oldFieldsPage = this.planRiskItemBS.listSubFieldsBySubItem(existent);
			java.util.List<PlanRiskSubItemField> oldFields = oldFieldsPage.getList();

			java.util.List<PlanRiskSubItemField> newFields = (planRiskSubItem.getPlanRiskSubItemField() == null)
					? java.util.Collections.emptyList()
					: planRiskSubItem.getPlanRiskSubItemField();

			org.forpdi.core.user.User responsibleUser = this.planRiskItemBS.exists(userSession.getAuthData().getId(),
					org.forpdi.core.user.User.class);
			if (responsibleUser == null) {
				return this.fail("Usuário responsável pela alteração não foi encontrado.");
			}

			this.itemHistoryBS.savePlanRiskSubItemChanges(
					existent,
					planRiskSubItem,
					oldFields,
					newFields,
					responsibleUser);

			for (PlanRiskSubItemField f : oldFields) {
				this.planRiskItemBS.delete(f);
			}

			if (planRiskSubItem.getPlanRiskSubItemField() != null) {
				for (PlanRiskSubItemField field : planRiskSubItem.getPlanRiskSubItemField()) {
					field.setPlanRiskSubItem(existent);
					this.planRiskItemBS.save(field);
				}
			}

			existent.setDescription(planRiskSubItem.getDescription());
			existent.setName(planRiskSubItem.getName());
			this.planRiskItemBS.persist(existent);

			return this.success(existent);

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}
	
	/**
	 * Deleta um item do plano de risco
	 * @param id
	 */
	@DeleteMapping(PATH + "/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> deletePlanRiskItem(@PathVariable Long id, UserSession userSession) {
		try {
			PlanRiskItem planRiskItem = this.planRiskItemBS.exists(id, PlanRiskItem.class);
			if (GeneralUtils.isInvalid(planRiskItem)) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !planRiskItem.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou deletar o Item ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, id
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(planRiskItem.getPlanRisk().getPolicy().getCompany());

			PaginatedList<PlanRiskItemField> fields = this.planRiskItemBS.listFieldsByPlanRiskItem(planRiskItem);
			for(int i = 0; i < fields.getList().size(); i ++) {
				PlanRiskItemField planRiskItemField = fields.getList().get(i);
				this.planRiskItemBS.deleteSubItens(planRiskItem);
				this.planRiskItemBS.delete(planRiskItemField);
			}

			this.planRiskItemBS.delete(planRiskItem);
			return this.success(planRiskItem);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Deleta um subitem do plano de risco
	 * @param id
	 */
	@DeleteMapping(PATH + "/delete-subitem/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> deletePlanRiskSubItem(@PathVariable Long id, UserSession userSession) {
		try {
			PlanRiskSubItem planRiskSubItem = this.planRiskItemBS.exists(id, PlanRiskSubItem.class);
			if (GeneralUtils.isInvalid(planRiskSubItem)) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !planRiskSubItem.getPlanRiskItem().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou deletar o Subitem ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, id
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(planRiskSubItem.getPlanRiskItem().getPlanRisk().getPolicy().getCompany());

			PaginatedList<PlanRiskSubItemField> fields = this.planRiskItemBS.listSubFieldsBySubItem(planRiskSubItem);
			for(int i = 0; i < fields.getList().size(); i ++) {
				PlanRiskSubItemField result = fields.getList().get(i);
				this.planRiskItemBS.delete(result);
			}

			this.planRiskItemBS.delete(planRiskSubItem);
			return this.success(planRiskSubItem);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	@GetMapping(PATH + "/edit-history")
	public ResponseEntity<?> planRiskItemHistory(
			@RequestParam("itemId") Long itemId,
			@ModelAttribute DefaultParams params,
			UserSession userSession) {
		try {
			PlanRiskItem item = this.planRiskItemBS.exists(itemId, PlanRiskItem.class);
			if (item == null)
				return this.notFound();

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null ||
						!item.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(item.getPlanRisk().getPolicy().getCompany());

			PaginatedList<ItemHistory> results = this.itemHistoryQueryBS.find(ItemHistoryScope.PLAN_RISK_ITEM.name(),
					itemId, params);
			return this.success(results);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	@GetMapping(PATH + "/subitem/edit-history")
	public ResponseEntity<?> planRiskSubitemHistory(
			@RequestParam("subitemId") Long subitemId,
			@ModelAttribute DefaultParams params,
			UserSession userSession) {
		try {
			PlanRiskSubItem sub = this.planRiskItemBS.exists(subitemId, PlanRiskSubItem.class);
			if (sub == null)
				return this.notFound();

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				Company itemCompany = sub.getPlanRiskItem().getPlanRisk().getPolicy().getCompany();
				if (userCompany == null || !itemCompany.getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(sub.getPlanRiskItem().getPlanRisk().getPolicy().getCompany());

			PaginatedList<ItemHistory> results = this.itemHistoryQueryBS.find(ItemHistoryScope.PLAN_RISK_SUBITEM.name(),
					subitemId, params);
			return this.success(results);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

}
