package org.forrisco.core.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forrisco.core.item.dto.DuplicateItemDto;
import org.forrisco.core.item.dto.PlanRiskSubItemDto;
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
	
	protected static final String PATH = BASEPATH +"/planrisk/item";
	
	/**
	 * Retorna itens.
	 * 
	 * @param PlanRisk Id do plano de risco a ser retornado.
	 * @return <List> item
	 */
	@GetMapping(PATH + "")
	public ResponseEntity<?> listItens(@RequestParam Long planRiskId, @ModelAttribute DefaultParams params) {
		try {
			PlanRisk planRisk = this.planRiskItemBS.exists(planRiskId, PlanRisk.class);

			if (planRisk == null) {
				return this.fail("Plano de Risco não encontrado");
			}

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
	public ResponseEntity<?> saveItem(@RequestBody PlanRiskItem planRiskItem) {
		try {
			if(planRiskItem.getPlanRisk() == null) {
				return this.fail("Plano de Risco não encontrado");
			}
			
			planRiskItem.setId(null);
			this.planRiskItemBS.save(planRiskItem);
			
			for(int i = 0; i < planRiskItem.getPlanRiskItemField().size(); i++) {
				PlanRiskItemField planRiskItemField = planRiskItem.getPlanRiskItemField().get(i);
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
	public ResponseEntity<?> saveSubItem(@RequestBody PlanRiskSubItemDto dto) {
		try {
			PlanRiskSubItem planRiskSubItem = dto.planRiskSubItem();
			
			if(planRiskSubItem.getPlanRiskItem() == null) {
				return this.fail("Item Vinculado não encontrado não encontrado");
			}
			
			planRiskSubItem.setId(null);
			this.planRiskItemBS.save(planRiskSubItem);
			
			for(int i = 0; i < planRiskSubItem.getPlanRiskSubItemField().size(); i++) {
				PlanRiskSubItemField planRiskSubItemField = planRiskSubItem.getPlanRiskSubItemField().get(i);
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
	public ResponseEntity<?> duplicateItem(@RequestBody DuplicateItemDto dto) {
		try {
			List<PlanRiskItem> itens = dto.itens();
			PlanRisk planRisk = dto.planRisk();
			PlanRisk plan = this.planRiskItemBS.exists(planRisk.getId(), PlanRisk.class);
			
			if(plan == null || plan.isDeleted()) {
				return this.fail("Plano de Risco não encontrado");
			}
			
			
			List<PlanRiskItem> result = new ArrayList<>();
			
			for(PlanRiskItem planRiskItem : itens) {
				
				PlanRiskItem item = this.planRiskItemBS.exists(planRiskItem.getId(), PlanRiskItem.class);
				
				if(item == null || item.isDeleted()) {
					//return this.fail("Item não encontrado");
					//return;
					continue;
				}
				
				planRiskItem.setId(null);
				planRiskItem.setDescription(item.getDescription());
				planRiskItem.setName(item.getName());
				planRiskItem.setPlanRisk(plan);
				
				this.planRiskItemBS.save(planRiskItem);
				
				result.add(planRiskItem);
				
				PaginatedList<PlanRiskItemField> fields = this.planRiskItemBS.listFieldsByPlanRiskItem(item);
				PaginatedList<PlanRiskSubItem> subitens = this.planRiskItemBS.listSubItemByItem(item, null);
				
				
				//fields
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
				
				//subitens
				for(PlanRiskSubItem subitem :subitens.getList()) {
					
					PlanRiskSubItem sub = new PlanRiskSubItem();
					
					sub.setId(null);
					sub.setPlanRiskItem(planRiskItem);
					sub.setDescription(subitem.getDescription());
					sub.setName(subitem.getName());	
					this.planRiskItemBS.save(sub);
					
					//subfields
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
			
			PaginatedList<PlanRiskItem> list = new PaginatedList<PlanRiskItem>();
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
	public ResponseEntity<?> listSubitens(@PathVariable Long id) {
		try {
			PlanRiskItem planRiskItem = this.planRiskItemBS.exists(id, PlanRiskItem.class);
			
			if (planRiskItem == null) {
				return this.fail("O Item solicitado não foi encontrado.");
			} else {
				PaginatedList<PlanRiskSubItem> itens = this.planRiskItemBS.listSubItemByItem(planRiskItem, null);
				
				List<Long> subItemIds = new ArrayList<>(itens.getList().size());
				for (PlanRiskSubItem subItem : itens.getList()) {
					subItemIds.add(subItem.getId());
				}
				
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
			}
			
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
	public ResponseEntity<?> listAllSubitens(@PathVariable Long id) {
		try {
			PlanRisk planRisk = this.planRiskItemBS.exists(id, PlanRisk.class);

			if (planRisk == null) {
				return this.fail("O Plano solicitado não foi encontrado.");
			}

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
	public ResponseEntity<?> detailItem(@PathVariable Long id) {
		try {
			PlanRiskItem planRiskItem = this.planRiskItemBS.exists(id, PlanRiskItem.class);
			
			if (planRiskItem == null) {
				return this.fail("O Item solicitado não foi encontrado.");
			} else {
				planRiskItem.setPlanRiskItemField(this.planRiskItemBS.listFieldsByPlanRiskItem(planRiskItem).getList());
				return this.success(planRiskItem);
			}
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
	public ResponseEntity<?> detailSubItem(@PathVariable Long id) {
		try {
			PlanRiskSubItem planRiskSubItem = this.planRiskItemBS.exists(id, PlanRiskSubItem.class);
			
			if (planRiskSubItem == null) {
				return this.fail("O Subitem solicitado não foi encontrado.");
			} else {
				planRiskSubItem.setPlanRiskSubItemField(this.planRiskItemBS.listSubFieldsBySubItem(planRiskSubItem).getList());
				return this.success(planRiskSubItem);
			}
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
	public ResponseEntity<?> updatePlanRiskItem(@RequestBody PlanRiskItem planRiskItem) {
		try {
			PlanRiskItem existent = planRiskItemBS.exists(planRiskItem.getId(), PlanRiskItem.class);
			
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			
			if(existent.getPlanRisk() == null) {
				return this.fail("Item sem plano de risco associado");	
			}
			
			PaginatedList<PlanRiskItemField> fields = this.planRiskItemBS.listFieldsByPlanRiskItem(planRiskItem);
			
			for(int i = 0; i < fields.getList().size(); i++) {
				this.planRiskItemBS.delete(fields.getList().get(i));
			}
			
			for(int i = 0; i < planRiskItem.getPlanRiskItemField().size(); i++) {
				PlanRiskItemField planRiskItemField = planRiskItem.getPlanRiskItemField().get(i);
				
				planRiskItemField.setPlanRiskItem(existent);
				this.planRiskItemBS.save(planRiskItemField);
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
	 * Atualiza subitem eseus campos
	 * @param planRiskSubItem
	 */
	@PostMapping( PATH + "/update-subitem")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> updatePlanRiskSubItem(@RequestBody PlanRiskSubItemDto dto) {
		try {
			PlanRiskSubItem planRiskSubItem = dto.planRiskSubItem();

			PlanRiskSubItem existent = planRiskItemBS.exists(planRiskSubItem.getId(), PlanRiskSubItem.class);
			
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			
			if(existent.getPlanRiskItem() == null) {
				return this.fail("Subitem sem item associado");	
			}
			
			PaginatedList<PlanRiskSubItemField> fields = this.planRiskItemBS.listSubFieldsBySubItem(planRiskSubItem);
			
			for(int i = 0; i < fields.getList().size(); i++) {
				this.planRiskItemBS.delete(fields.getList().get(i));
			}
			
			for(int i = 0; i < planRiskSubItem.getPlanRiskSubItemField().size(); i++) {
				PlanRiskSubItemField planRiskItemField = planRiskSubItem.getPlanRiskSubItemField().get(i);
				
				planRiskItemField.setPlanRiskSubItem(existent);
				this.planRiskItemBS.save(planRiskItemField);
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
	public ResponseEntity<?> deletePlanRiskItem(@PathVariable Long id) {
		try {
			PlanRiskItem planRiskItem = this.planRiskItemBS.exists(id, PlanRiskItem.class);
			
			if (GeneralUtils.isInvalid(planRiskItem)) {
				return this.notFound();
			}
			
			PaginatedList<PlanRiskItemField> fields = this.planRiskItemBS.listFieldsByPlanRiskItem(planRiskItem);
			
			for(int i = 0; i < fields.getList().size(); i ++) {
				PlanRiskItemField planRiskItemField = fields.getList().get(i);
				
				this.planRiskItemBS.deleteSubItens(planRiskItem);  //Deleta os SubItens
				this.planRiskItemBS.delete(planRiskItemField);     //Delete os campos do item
			}
			
			this.planRiskItemBS.delete(planRiskItem); //Delete o Item
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
	public ResponseEntity<?> deletePlanRiskSubItem(@PathVariable Long id) {
		try {
			PlanRiskSubItem planRiskSubItem = this.planRiskItemBS.exists(id, PlanRiskSubItem.class);
			
			if (GeneralUtils.isInvalid(planRiskSubItem)) {
				return this.notFound();
			}
			
			PaginatedList<PlanRiskSubItemField> fields = this.planRiskItemBS.listSubFieldsBySubItem(planRiskSubItem);
			
			for(int i = 0; i < fields.getList().size(); i ++) {
				PlanRiskSubItemField result = fields.getList().get(i);
				this.planRiskItemBS.delete(result);     //Delete os campos do subitem
			}
			
			this.planRiskItemBS.delete(planRiskSubItem); //Delete o subitem
			return this.success(planRiskSubItem);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

}
