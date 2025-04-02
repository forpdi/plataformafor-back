package org.forrisco.core.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forrisco.core.item.dto.FieldItemDto;
import org.forrisco.core.item.dto.FieldSubItemDto;
import org.forrisco.core.item.dto.ItemDto;
import org.forrisco.core.item.dto.SubitemDto;
import org.forrisco.core.policy.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
	
	protected static final String PATH =  BASEPATH +"/item";
	
	
	/**
	 * Salvar Novo Item
	 * 
	 * @return ResponseEntity<?>
	 */
	@PostMapping( PATH + "/new")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> saveItem(@RequestBody ItemDto dto){
		try {	
			Item item = dto.item();
			if(item.getPolicy()== null) {
				return this.fail("Política não encontrada");
			}
			item.setId(null);
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
	public ResponseEntity<?> saveSubitem(@RequestBody SubitemDto dto){
		try {
			SubItem subitem = dto.subitem();
			
			if(subitem.getItem() == null) {
				return this.fail("Item não encontrada");
			}
			
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
	public ResponseEntity<?> saveFieldItem(@RequestBody FieldItemDto dto){
		try {
			FieldItem fieldItem = dto.fieldItem();
			fieldItem.setId(null);
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
	public ResponseEntity<?> saveFieldSubItem(@RequestBody FieldSubItemDto dto){
		try {
			FieldSubItem fieldSubItem = dto.fieldSubItem();
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
	public ResponseEntity<?> listItens(@RequestParam Long policyId) {
		try {
			Policy policy = this.itemBS.exists(policyId, Policy.class);

			if (policy == null) {
				return this.fail("Política não encontrada");
			}

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
	public ResponseEntity<?> retrieveItem(@PathVariable Long id) {
		try {
			Item item = this.itemBS.exists(id, Item.class);
			if (item == null) {
				return this.fail("O Item solicitado não foi encontrado.");
			} else {
				
			item.setFieldItem(this.itemBS.listFieldsByItem(item).getList());
						
			return this.success(item);
			
			}
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
	public ResponseEntity<?> retrieveSubItem(@PathVariable Long id) {
		try {
			SubItem subitem = this.itemBS.exists(id, SubItem.class);
			if (subitem == null) {
				return this.fail("O SubItem solicitado não foi encontrado.");
			} else {
				
				subitem.setFieldSubItem(this.itemBS.listFieldsBySubItem(subitem).getList());
				
				return this.success(subitem);
			}
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
	public ResponseEntity<?> retrieveSubitens(@PathVariable Long id) {
		try {
			Item item = this.itemBS.exists(id, Item.class);
			if (item == null) {
				return this.fail("O item solicitado não foi encontrado.");
			} else {
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
			}
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
	public ResponseEntity<?> retrieveField(@PathVariable Long id) {
		
		try {
			
			Item item = this.itemBS.exists(id, Item.class);
			if (item == null) {
				return this.fail("O item solicitado não foi encontrado.");
			} else {
				
				PaginatedList<FieldItem> fields = this.itemBS.listFieldsByItem(item);
				return this.success(fields);
			}
			
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
	public ResponseEntity<?> retrieveSubField(@PathVariable Long id) {
		
		try {
			
			SubItem subitem = this.itemBS.exists(id, SubItem.class);
			if (subitem == null) {
				return this.fail("O subitem solicitado não foi encontrado.");
			} else {
				
				PaginatedList<FieldSubItem> fields = this.itemBS.listFieldsBySubItem(subitem);
				return this.success(fields);
			}
			
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}



	

	/**
	 * Edita item.
	 * 
	 * @param item
	 *            Item a ser alterado com os novos campos.
	 */
	@PostMapping( PATH + "/update")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> updateItem(@RequestBody ItemDto dto) {
		try {
			Item item = dto.item();
			Item existent = this.itemBS.exists(item.getId(), Item.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}

			if(existent.getPolicy()==null) {
				return this.fail("Item sem política associada");
			}


			PaginatedList<FieldItem> fields = this.itemBS.listFieldsByItem(existent);
		
			for(int i=0; i<fields.getList().size();i++) {
				this.itemBS.delete(fields.getList().get(i));
			}

			
			for(int i=0; i<item.getFieldItem().size();i++) {
				FieldItem field =item.getFieldItem().get(i);
				
				field.setItem(existent);
				this.itemBS.save(field);
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
	 *            Item a ser alterado com os novos campos.
	 */
	@PostMapping( PATH + "/subitem/update")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> updateSubitem(@RequestBody SubitemDto dto) {
		try {
			SubItem subitem = dto.subitem();
			SubItem existent = this.itemBS.exists(subitem.getId(), SubItem.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}

			if(existent.getItem()==null) {
				return this.fail("SubItem sem Item associado");
			}


			PaginatedList<FieldSubItem> subfields = this.itemBS.listFieldsBySubItem(existent);
		
			for(int i=0; i<subfields.getList().size();i++) {
				this.itemBS.delete(subfields.getList().get(i));
			}

			for(int i=0; i<subitem.getFieldSubItem().size();i++) {
				FieldSubItem subfield =subitem.getFieldSubItem().get(i);
				
				subfield.setSubitem(existent);
				this.itemBS.save(subfield);
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
	public ResponseEntity<?> deleteItem(@PathVariable Long id) {
		try {
			Item item = this.itemBS.exists(id, Item.class);
			if (GeneralUtils.isInvalid(item)) {
				return this.notFound();
			}
			
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
	public ResponseEntity<?> deleteSubitem(@PathVariable Long id) {
		try {
			SubItem subitem = this.itemBS.exists(id, SubItem.class);
			if (GeneralUtils.isInvalid(subitem)) {
				return this.notFound();
			}
			
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
	 *            Id da política.
	 * @return Subitem Retorna os subitens de acordo com o id passado.
	 * 
	 */

	@GetMapping( PATH + "/allsubitens/{id}")
	public ResponseEntity<?> retrieveAllSubitem(@PathVariable Long id) {
		try {
			Policy policy = this.itemBS.exists(id, Policy.class);
			if (policy == null) {
				return this.fail("A política solicitada não foi encontrado.");
			}

			PaginatedList<Item> itens = this.itemBS.listItensByPolicy(policy);
			PaginatedList<SubItem> subitens = new PaginatedList<>();
			List<SubItem> list= new ArrayList<>();
			
			for(Item item :itens.getList()) {
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

}