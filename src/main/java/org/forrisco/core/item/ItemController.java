package org.forrisco.core.item;

import java.util.ArrayList;
import java.util.List;

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
import org.forrisco.core.item.dto.FieldItemDto;
import org.forrisco.core.item.dto.FieldSubItemDto;
import org.forrisco.core.item.dto.ItemDto;
import org.forrisco.core.item.dto.SubitemDto;
import org.forrisco.core.item.dto.SubitensRequestDto;
import org.forrisco.core.item.history.ItemHistory;
import org.forrisco.core.item.history.ItemHistoryBS;
import org.forrisco.core.item.history.ItemHistoryQueryBS;
import org.forrisco.core.item.history.ItemHistoryScope;
import org.forrisco.core.policy.Policy;
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
 * @author Matheus Nascimento
 */
@RestController
public class ItemController extends AbstractController {
	
	@Autowired
	private ItemBS itemBS;

	@Autowired
	private CompanyDomainContext domain;

	@Autowired
	private CompanyBS companyBS;

	@Autowired
	private ItemHistoryBS itemHistoryBS;

	@Autowired
	private ItemHistoryQueryBS itemHistoryQueryBS;
	
	protected static final String PATH =  BASEPATH +"/item";

	private boolean isSystemAdmin(UserSession userSession) {
		return userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel();
	}

	private ResponseEntity<?> validateCompanyAccess(UserSession userSession, Company resourceCompany,
			String actionDescription) {
		if (this.isSystemAdmin(userSession)) {
			return null;
		}

		Long targetCompanyId = (resourceCompany == null) ? null : resourceCompany.getId();
		Long loggedInUserId = userSession.getAuthData().getId();
		Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
		if (userCompany == null || targetCompanyId == null || !targetCompanyId.equals(userCompany.getId())) {
			LOGGER.warn(String.format(
					"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou %s.",
					userSession.getAuthData().getName(), loggedInUserId, actionDescription));
			return this.fail("Acesso negado.");
		}
		return null;
	}
	
	/**
	 * Salvar Novo Item
	 * 
	 * @return ResponseEntity<?>
	 */
	@PostMapping( PATH + "/new")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> saveItem(@RequestBody ItemDto dto, UserSession userSession){
		try {
			Item item = dto.item();
			if(item.getPolicy()== null) {
				return this.fail("Política não encontrada");
			}
			Policy policy = this.itemBS.exists(item.getPolicy().getId(), Policy.class);
			if (policy == null) {
				return this.fail("A política informada é inválida.");
			}

			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession, policy.getCompany(),
					"criar Item na Política ID " + policy.getId());
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(policy.getCompany());

			item.setId(null);
			item.setPolicy(policy);
			this.itemBS.save(item);
			if (item.getFieldItem() != null) {
				for (FieldItem fieldItem : item.getFieldItem()) {
					fieldItem.setItem(item);
					this.itemBS.save(fieldItem);
				}
			}
			return this.success(item);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Salvar Novo Subitem
	 * 
	 * @return ResponseEntity<?>
	 */
	@PostMapping( PATH + "/subnew")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> saveSubitem(@RequestBody SubitemDto dto, UserSession userSession){
		try {
			SubItem subitem = dto.subitem();

			if(subitem.getItem() == null) {
				return this.fail("Item não encontrado");
			}
			Item parentItem = this.itemBS.exists(subitem.getItem().getId(), Item.class);
			if (parentItem == null) {
				return this.fail("O item pai informado é inválido.");
			}

			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession,
					parentItem.getPolicy().getCompany(), "criar Subitem no Item ID " + parentItem.getId());
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(parentItem.getPolicy().getCompany());

			subitem.setItem(parentItem);
			this.itemBS.save(subitem);

			if (subitem.getFieldSubItem() != null) {
				for (FieldSubItem fieldSubItem : subitem.getFieldSubItem()) {
					fieldSubItem.setSubitem(subitem);
					this.itemBS.save(fieldSubItem);
				}
			}

			return this.success(subitem);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Salvar Novo FieldItem
	 * 
	 * @return ResponseEntity<?>
	 */
	@PostMapping( PATH + "/field")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> saveFieldItem(@RequestBody FieldItemDto dto, UserSession userSession){
		try {
			FieldItem fieldItem = dto.fieldItem();
			Item parentItem = this.itemBS.exists(fieldItem.getItem().getId(), Item.class);
			if (parentItem == null) {
				return this.fail("O item pai informado é inválido.");
			}

			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession,
					parentItem.getPolicy().getCompany(), "criar FieldItem no Item ID " + parentItem.getId());
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(parentItem.getPolicy().getCompany());

			fieldItem.setId(null);
			fieldItem.setItem(parentItem);
			this.itemBS.save(fieldItem);
			return this.success(fieldItem);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Salvar Novo FieldSubItem
	 * 
	 * @return ResponseEntity<?>
	 */
	@PostMapping( PATH + "/subfield")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> saveFieldSubItem(@RequestBody FieldSubItemDto dto, UserSession userSession){
		try {
			FieldSubItem fieldSubItem = dto.fieldSubItem();
			SubItem parentSubItem = this.itemBS.exists(fieldSubItem.getSubitem().getId(), SubItem.class);
			if (parentSubItem == null) {
				return this.fail("O SubItem pai informado é inválido.");
			}

			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession,
					parentSubItem.getItem().getPolicy().getCompany(),
					"criar FieldSubItem no SubItem ID " + parentSubItem.getId());
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(parentSubItem.getItem().getPolicy().getCompany());

			fieldSubItem.setSubitem(parentSubItem);
			this.itemBS.save(fieldSubItem);
			return this.success(fieldSubItem);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna itens.
	 * 
	 * @param Policy
	 *            Id da política.
	 * @return <List> item
	 */
	@GetMapping( PATH + "")
	public ResponseEntity<?> listItens(@RequestParam Long policyId, UserSession userSession) {
		try {
			Policy policy = this.itemBS.exists(policyId, Policy.class);
			if (policy == null) {
				return this.fail("Política não encontrada");
			}

			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession, policy.getCompany(),
					"listar Itens da Política ID " + policy.getId());
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(policy.getCompany());

			PaginatedList<Item> itens = this.itemBS.getItemsWithDetailsByPolicy(policy);
			return this.success(itens);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Erro inesperado: " + ex.getMessage());
		}
	}
	
	/**
	 * Retorna item.
	 * 
	 * @param id
	 *			Id do item a ser retornado.
	 * @return Item Retorna o item de acordo com o id passado.
	 */
	@GetMapping( PATH + "/{id}")
	public ResponseEntity<?> retrieveItem(@PathVariable Long id, UserSession userSession) {
		try {
			Item item = this.itemBS.exists(id, Item.class);
			if (item == null) {
				return this.fail("O Item solicitado não foi encontrado.");
			}

			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession, item.getPolicy().getCompany(),
					"acessar o Item ID " + id);
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(item.getPolicy().getCompany());
			item.setFieldItem(this.itemBS.listFieldsByItem(item).getList());
			return this.success(item);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna SubItem.
	 * 
	 * @param id
	 *			Id do SubItem a ser retornado.
	 * @return SubItem 
	 * 			Retorna o subitem de acordo com o id passado.
	 */

	@GetMapping( PATH + "/subitem/{id}")
	public ResponseEntity<?> retrieveSubItem(@PathVariable Long id, UserSession userSession) {
		try {
			SubItem subitem = this.itemBS.exists(id, SubItem.class);
			if (subitem == null) {
				return this.fail("O SubItem solicitado não foi encontrado.");
			}

			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession,
					subitem.getItem().getPolicy().getCompany(), "acessar o SubItem ID " + id);
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(subitem.getItem().getPolicy().getCompany());
			subitem.setFieldSubItem(this.itemBS.listFieldsBySubItem(subitem).getList());
			return this.success(subitem);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna subitens.
	 * 
	 * @param id
	 *            Id do item.
	 * @return Subitem Retorna os subitens de acordo com o id passado.
	 * 
	 */
	@GetMapping( PATH + "/subitens/{id}")
	public ResponseEntity<?> retrieveSubitens(@PathVariable Long id, UserSession userSession) {
		try {
			Item item = this.itemBS.exists(id, Item.class);
			if (item == null) {
				return this.fail("O item solicitado não foi encontrado.");
			}

			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession, item.getPolicy().getCompany(),
					"listar subitens do Item ID " + id);
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(item.getPolicy().getCompany());

			PaginatedList<SubItem> subitens = this.itemBS.listSubItensByItem(item);
			for (SubItem subItem : subitens.getList()) {
				List<FieldSubItem> fields = this.itemBS.listFieldsBySubItem(subItem).getList();
				if (fields == null) {
					continue;
				}
				for (FieldSubItem field: fields) {
					if (field.isText()) {
						subItem.setHasText(true);
					} else {
						subItem.setHasFile(true);
					}
				}
			}
			return this.success(subitens);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
		
	/**
	 * Retorna campo.
	 * 
	 * @param id
	 *            Id do item.
	 */

	@GetMapping( PATH + "/field/{id}")
	public ResponseEntity<?> retrieveField(@PathVariable Long id, UserSession userSession) {
		try {
			Item item = this.itemBS.exists(id, Item.class);
			if (item == null) {
				return this.fail("O item solicitado não foi encontrado.");
			}

			// BOLA/IDOR Security Check
			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession, item.getPolicy().getCompany(),
					"acessar campos do Item ID " + id);
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(item.getPolicy().getCompany());

			PaginatedList<FieldItem> fields = this.itemBS.listFieldsByItem(item);
			return this.success(fields);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna subcampo.
	 * 
	 * @param id
	 *            Id do subitem.
	 */

	@GetMapping( PATH + "/subfield/{id}")
	public ResponseEntity<?> retrieveSubField(@PathVariable Long id, UserSession userSession) {
		try {
			SubItem subitem = this.itemBS.exists(id, SubItem.class);
			if (subitem == null) {
				return this.fail("O subitem solicitado não foi encontrado.");
			}

			// BOLA/IDOR Security Check
			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession,
					subitem.getItem().getPolicy().getCompany(), "acessar campos do SubItem ID " + id);
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(subitem.getItem().getPolicy().getCompany());

			PaginatedList<FieldSubItem> fields = this.itemBS.listFieldsBySubItem(subitem);
			return this.success(fields);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Edita item.
	 *
	 * @param item
	 *             Item a ser alterado com os novos campos.
	 */
	@PostMapping(PATH + "/update")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> updateItem(@RequestBody ItemDto dto, UserSession userSession) {
		try {
			Item item = dto.item();
			Item existent = this.itemBS.exists(item.getId(), Item.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}

			if (existent.getPolicy() == null) {
				return this.fail("Item sem política associada");
			}

			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession, existent.getPolicy().getCompany(),
					"atualizar o Item ID " + existent.getId());
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(existent.getPolicy().getCompany());

			java.util.List<FieldItem> oldFields = this.itemBS.listFieldsByItem(existent).getList();
			java.util.List<FieldItem> newFields = (item.getFieldItem() == null)
					? java.util.Collections.emptyList()
					: item.getFieldItem();

			org.forpdi.core.user.User responsibleUser = this.itemBS.exists(userSession.getAuthData().getId(),
					org.forpdi.core.user.User.class);
			if (responsibleUser == null) {
				return this.fail("Usuário responsável pela alteração não foi encontrado.");
			}

			this.itemHistoryBS.saveItemChanges(
					existent,
					item,
					oldFields,
					newFields,
					responsibleUser);

			PaginatedList<FieldItem> fields = this.itemBS.listFieldsByItem(existent);
			for (int i = 0; i < fields.getList().size(); i++) {
				this.itemBS.delete(fields.getList().get(i));
			}

			if (item.getFieldItem() != null) {
				for (int i = 0; i < item.getFieldItem().size(); i++) {
					FieldItem field = item.getFieldItem().get(i);
					field.setItem(existent);
					this.itemBS.save(field);
				}
			}

			existent.setDescription(item.getDescription());
			existent.setName(item.getName());
			this.itemBS.persist(existent);

			existent.setFieldItem(this.itemBS.listFieldsByItem(existent).getList());

			return this.success(existent);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
	
	/**
	 * Edita subitem.
	 *
	 * @param subitem
	 *                Subitem a ser alterado com os novos campos.
	 */
	@PostMapping(PATH + "/subitem/update")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> updateSubitem(@RequestBody SubitemDto dto, UserSession userSession) {
		try {
			SubItem subitem = dto.subitem();
			SubItem existent = this.itemBS.exists(subitem.getId(), SubItem.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}

			if (existent.getItem() == null) {
				return this.fail("SubItem sem Item associado");
			}

			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession,
					existent.getItem().getPolicy().getCompany(), "atualizar o SubItem ID " + existent.getId());
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(existent.getItem().getPolicy().getCompany());

			java.util.List<FieldSubItem> oldFields = this.itemBS.listFieldsBySubItem(existent).getList();
			java.util.List<FieldSubItem> newFields = (subitem.getFieldSubItem() == null)
					? java.util.Collections.emptyList()
					: subitem.getFieldSubItem();

			org.forpdi.core.user.User responsibleUser = this.itemBS.exists(userSession.getAuthData().getId(),
					org.forpdi.core.user.User.class);
			if (responsibleUser == null) {
				return this.fail("Usuário responsável pela alteração não foi encontrado.");
			}

			this.itemHistoryBS.saveSubItemChanges(
					existent,
					subitem,
					oldFields,
					newFields,
					responsibleUser);

			PaginatedList<FieldSubItem> subfields = this.itemBS.listFieldsBySubItem(existent);
			for (int i = 0; i < subfields.getList().size(); i++) {
				this.itemBS.delete(subfields.getList().get(i));
			}

			if (subitem.getFieldSubItem() != null) {
				for (int i = 0; i < subitem.getFieldSubItem().size(); i++) {
					FieldSubItem subfield = subitem.getFieldSubItem().get(i);
					subfield.setSubitem(existent);
					this.itemBS.save(subfield);
				}
			}

			existent.setDescription(subitem.getDescription());
			existent.setName(subitem.getName());
			this.itemBS.persist(existent);

			return this.success(existent);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
	
	/**
	 * Exclui item.
	 * 
	 * @param id
	 *            Id do item a ser excluído.
	 *            
	 */
	@DeleteMapping( PATH + "/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> deleteItem(@PathVariable Long id, UserSession userSession) {
		try {
			Item item = this.itemBS.exists(id, Item.class);
			if (GeneralUtils.isInvalid(item)) {
				return this.notFound();
			}

			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession, item.getPolicy().getCompany(),
					"deletar o Item ID " + id);
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(item.getPolicy().getCompany());

			PaginatedList<FieldItem> fields = this.itemBS.listFieldsByItem(item);
			for(int i=0;i<fields.getList().size();i++) {
				this.itemBS.delete(fields.getList().get(i));
			}

			this.itemBS.deleteSubitens(item);
			this.itemBS.delete(item);

			return this.success(item);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}
	
	/**
	 * Exclui subitem.
	 * 
	 * @param id
	 *            Id do subitem a ser excluído.
	 *            
	 */
	@DeleteMapping( PATH + "/subitem/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> deleteSubitem(@PathVariable Long id, UserSession userSession) {
		try {
			SubItem subitem = this.itemBS.exists(id, SubItem.class);
			if (GeneralUtils.isInvalid(subitem)) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession,
					subitem.getItem().getPolicy().getCompany(), "deletar o SubItem ID " + id);
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(subitem.getItem().getPolicy().getCompany());

			this.itemBS.deleteSubitem(subitem);

			return this.success(subitem);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}
	
	/**
	 * Retorna todos subitens da política.
	 * 
	 * @param id
	 *           Id da política.
	 * @return Subitem Retorna os subitens de acordo com o id passado.
	 * 
	 */
	@PostMapping(PATH + "/allsubitens")
	public ResponseEntity<?> retrieveAllSubitem(@RequestBody SubitensRequestDto subitensRequestDto,
			UserSession userSession) {
		try {
			Long id = subitensRequestDto.id();
			Policy policy = this.itemBS.exists(id, Policy.class);
			if (policy == null) {
				return this.fail("A política solicitada não foi encontrada.");
			}

			ResponseEntity<?> accessDenied = this.validateCompanyAccess(userSession, policy.getCompany(),
					"listar todos os subitens da Política ID " + id);
			if (accessDenied != null) {
				return accessDenied;
			}

			domain.validateTenant(policy.getCompany());

			PaginatedList<Item> itens = this.itemBS.listItensByPolicy(policy);
			PaginatedList<SubItem> subitens = new PaginatedList<>();
			List<SubItem> list = new ArrayList<>();

			for (Item item : itens.getList()) {
				PaginatedList<SubItem> subitem = this.itemBS.listSubItensByItem(item);
				list.addAll(subitem.getList());
			}

			subitens.setList(list);
			subitens.setTotal((long) list.size());
			return this.success(subitens);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	@GetMapping(PATH + "/edit-history")
	public ResponseEntity<?> itemHistory(
			@RequestParam("itemId") Long itemId,
			@ModelAttribute DefaultParams params) {
		try {
			Item item = this.itemBS.exists(itemId, Item.class);
			if (item == null || item.isDeleted()) {
				return this.notFound();
			}

			PaginatedList<ItemHistory> results = this.itemHistoryQueryBS.find(ItemHistoryScope.ITEM.name(), itemId,
					params);

			return this.success(results);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	@GetMapping(PATH + "/subitem/edit-history")
	public ResponseEntity<?> subitemHistory(
			@RequestParam("subitemId") Long subitemId,
			@ModelAttribute DefaultParams params) {
		try {
			SubItem sub = this.itemBS.exists(subitemId, SubItem.class);
			if (sub == null || sub.isDeleted()) {
				return this.notFound();
			}

			PaginatedList<ItemHistory> results = this.itemHistoryQueryBS.find(ItemHistoryScope.SUBITEM.name(),
					subitemId, params);

			return this.success(results);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
}
