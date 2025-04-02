package org.forrisco.core.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.ProjectionList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.utils.Util;
import org.forrisco.core.plan.PlanRisk;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;
import org.springframework.stereotype.Service;

/**
 * @author Juliano Afonso
 */

@Service
@SuppressWarnings("deprecation")
public class PlanRiskItemBS extends HibernateBusiness {
		
	// ------------LISTAGENS------------//
	
	/**
	 * Lista os itens de um plano de risco.
	 * @param PlanRisk
	 * 			plano de risco no qual se deseja obter os itens.
	 * 
	 * @return PaginatedList<PlanRiskItem>
	 * 			Lista de itens.
	 */
	public PaginatedList<PlanRiskItem> listItensByPlanRisk(PlanRisk planRisk) {
	
		PaginatedList<PlanRiskItem> results = new PaginatedList<PlanRiskItem>();
		
		Criteria criteria = this.dao.newCriteria(PlanRiskItem.class)
				.add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("planRisk", planRisk))
				.addOrder(Order.asc("id"));
		
		Criteria count = this.dao.newCriteria(PlanRiskItem.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRisk", planRisk))
				.setProjection(Projections.countDistinct("id"));
		results.setList(this.dao.findByCriteria(criteria, PlanRiskItem.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	public PaginatedList<PlanRiskItem> listItensByPlanRisk(PlanRisk planRisk, DefaultParams params) {
	
		PaginatedList<PlanRiskItem> results = new PaginatedList<PlanRiskItem>();
		
		Criteria criteria = this.dao.newCriteria(PlanRiskItem.class)
				.add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("planRisk", planRisk));
		
		Criteria count = this.dao.newCriteria(PlanRiskItem.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRisk", planRisk))
				.setProjection(Projections.countDistinct("id"));
		
		
		if (params.hasTerm()) {
			String term = params.getTerm();
			Criterion name = Restrictions.like("name", "%" + term + "%").ignoreCase();

			List<PlanRiskItem> items = this.dao.findByCriteria(criteria, PlanRiskItem.class);

			List<Long> itemsWithFields = this.listItemIdsWithFields(items, term);
			Criterion fields = Restrictions.in("id", itemsWithFields);
			
			List<Long> itemsWithSubitems = this.listItemIdsWithSubItems(items, term);
			Criterion subitems = Restrictions.in("id", itemsWithSubitems);

			criteria.add(Restrictions.or(name, fields, subitems));
			count.add(Restrictions.or(name, fields, subitems));
		}
		
		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((page-1) * pageSize)
			.setMaxResults(pageSize);
		
		results.setList(this.dao.findByCriteria(criteria, PlanRiskItem.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	/**
	 * Lista os subitens mapeados pelos ids dos itens passados.
	 * @param itemIds
	 * 			lista com os ids dos itens
	 * 
	 * @return Map<Long, List<SubItem>>
	 * 			Map com os subitens dos itens
	 */
	public Map<Long, List<PlanRiskSubItem>> listSubItemsByItemIds(List<Long> itemIds) {
		if (itemIds.isEmpty()) {
			return new HashMap<>();
		}

		Criteria criteria = this.dao.newCriteria(PlanRiskSubItem.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("planRiskItem", "planRiskItem")
				.add(Restrictions.in("planRiskItem.id", itemIds))
				.addOrder(Order.asc("id"));
		
		List<PlanRiskSubItem> subitems = this.dao.findByCriteria(criteria, PlanRiskSubItem.class);

		Map<Long, List<PlanRiskSubItem>> subitemsMap = Util.generateGroupedMap(subitems, si -> si.getPlanRiskItem().getId());
		
		for (Long itemId : itemIds) {
			if (!subitemsMap.containsKey(itemId)) {
				subitemsMap.put(itemId, Collections.emptyList());
			}
		}
		
		return subitemsMap;
	}

	
	/**
	 * Lista os campos de um item.
	 * @param PlanRiskItemField
	 * 			item no qual se deseja obter os campos.
	 * 
	 * @return PaginatedList<PlanRiskItemField>
	 * 			Lista de campos.
	 */
	public PaginatedList<PlanRiskItemField> listFieldsByPlanRiskItem(PlanRiskItem planRiskItem) {
		
		PaginatedList<PlanRiskItemField> results = new PaginatedList<PlanRiskItemField>();
		
		Criteria criteria = this.dao.newCriteria(PlanRiskItemField.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRiskItem", planRiskItem));
		
		Criteria count = this.dao.newCriteria(PlanRiskItemField.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRiskItem", planRiskItem))
				.setProjection(Projections.countDistinct("id"));
		
		results.setList(this.dao.findByCriteria(criteria, PlanRiskItemField.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
		
	}
	
	/**
	 * Lista os campos de um item.
	 * @param PlanRiskItemField
	 * 			item no qual se deseja obter os campos.
	 * 
	 * @return PaginatedList<PlanRiskItemField>
	 * 			Lista de campos.
	 */
	public Map<Long, String> listFirstFieldNamesByPlanRiskItemIds(List<Long> ids) {
		
		if (ids != null && ids.isEmpty()) {
			return new HashMap<>();
		}
		
		Criteria criteria = this.dao.newCriteria(PlanRiskItemField.class)
				.createAlias("planRiskItem", "planRiskItem", JoinType.INNER_JOIN)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.in("planRiskItem.id", ids))
				.addOrder(Order.desc("id"));
		
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("planRiskItem.id"), "id");
		projList.add(Projections.property("name"), "name");
		
		criteria.setProjection(projList);
		
		List<Object[]> result = this.dao.findByCriteria(criteria, Object[].class);
		
		Map<Long, String> firstFieldNamesMap = new HashMap<>();
		result.forEach(obj -> {
			long planRiskItemId = (long) obj[0];
			String fieldName = (String) obj[1];
			firstFieldNamesMap.put(planRiskItemId, fieldName);
		});
		
		return firstFieldNamesMap;
	}
	
	/**
	 * Lista os campos de um item.
	 * @param PlanRiskItemField
	 * 			item no qual se deseja obter os campos.
	 * 
	 * @return PaginatedList<PlanRiskItemField>
	 * 			Lista de campos.
	 */
	public Map<Long, String> listFirstFieldNamesByPlanRiskSubItemIds(List<Long> ids) {
		
		if (ids != null && ids.isEmpty()) {
			return new HashMap<>();
		}
			
		Criteria criteria = this.dao.newCriteria(PlanRiskSubItemField.class)
				.createAlias("planRiskSubItem", "planRiskSubItem", JoinType.INNER_JOIN)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.in("planRiskSubItem.id", ids))
				.addOrder(Order.desc("id"));
		
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("planRiskSubItem.id"), "id");
		projList.add(Projections.property("name"), "name");
		
		criteria.setProjection(projList);
		
		List<Object[]> result = this.dao.findByCriteria(criteria, Object[].class);
		
		Map<Long, String> firstFieldNamesMap = new HashMap<>();
		result.forEach(obj -> {
			long planRiskSubItemId = (long) obj[0];
			String fieldName = (String) obj[1];
			firstFieldNamesMap.put(planRiskSubItemId, fieldName);
		});
		
		return firstFieldNamesMap;
	}
	
	/**
	 * Lista os subitens de um item.
	 * @param PlanRiskItem
	 * 			item no qual se deseja obter os subitens.
	 * 
	 * @return PaginatedList<PlanRiskItemField>
	 * 			Lista de subitens.
	 */
	public PaginatedList<PlanRiskSubItem> listSubItemByItem(PlanRiskItem planRiskItem, String term) {
		
		PaginatedList<PlanRiskSubItem> results = new PaginatedList<PlanRiskSubItem>();
		
		Criteria criteria = this.dao.newCriteria(PlanRiskSubItem.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRiskItem", planRiskItem));

		if (term != null) {
			Criterion name = Restrictions.like("name", "%" + term + "%").ignoreCase();

			criteria.add(name);
		}
		
		results.setList(this.dao.findByCriteria(criteria, PlanRiskSubItem.class));
		return results;
	}

	
	/**
	 * Lista de campos de um subitem
	 * 
	 * @param planRiskSubItem
	 * @return
	 */
	
	public PaginatedList<PlanRiskSubItemField> listSubFieldsBySubItem(PlanRiskSubItem planRiskSubItem) {
		
		PaginatedList<PlanRiskSubItemField> results = new PaginatedList<PlanRiskSubItemField>();
		
		Criteria criteria = this.dao.newCriteria(PlanRiskSubItemField.class)
				.add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("planRiskSubItem", planRiskSubItem))
				.addOrder(Order.asc("id"));
		
		Criteria count = this.dao.newCriteria(PlanRiskSubItemField.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRiskSubItem", planRiskSubItem))
				.setProjection(Projections.countDistinct("id"));
		
		results.setList(this.dao.findByCriteria(criteria, PlanRiskSubItemField.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	// ------------SAVES------------//
	
	/**
	 * Salva no banco de dados um novo item
	 * 
	 * @param plaRiskItem instância do item a ser salvo
	 */
	public void save(PlanRiskItem plaRiskItem) {
		plaRiskItem.setId(null);
		plaRiskItem.setDeleted(false);
		
		this.persist(plaRiskItem);
	}
	
	/**
	 * Salva no banco de dados um novo campo
	 * 
	 * @param planRiskItemField, instância do PlanRiskItemField a ser salvo
	 */
	public void save(PlanRiskItemField planRiskItemField) {
		planRiskItemField.setId(null);
		planRiskItemField.setDeleted(false);
		this.persist(planRiskItemField);
	}
	
	/**
	 * Salva no banco de dados um novo subitem
	 * 
	 * @param plaRiskItem instância do subitem a ser salvo
	 */
	public void save(PlanRiskSubItem plaRiskSubItem) {
		plaRiskSubItem.setId(null);
		plaRiskSubItem.setDeleted(false);
		
		this.persist(plaRiskSubItem);
	}
	
	/**
	 * Salva no banco de dados um campo de um  subitem
	 * 
	 * @param plaRiskItem instância do campo a ser salvo
	 */
	public void save(PlanRiskSubItemField plaRiskSubItemField) {
		plaRiskSubItemField.setId(null);
		plaRiskSubItemField.setDeleted(false);
		
		this.persist(plaRiskSubItemField);
	}
	
	// ------------DELETES------------//
	/**
	 * Deleta do banco de dados um item
	 * 
	 * @param PlanRiskItem,
	 *            instância do planRiskItem a ser deletado
	 */
	public void delete(PlanRiskItem planRiskItem) {
		planRiskItem.setDeleted(true);
		this.persist(planRiskItem);
	}
	
	/**
	 * Deleta do banco de dados um campo
	 * 
	 * @param PlanRiskItemField,
	 *            instância do PlanRiskItemField a ser deletado
	 */
	public void delete(PlanRiskItemField planRiskItemField) {
		planRiskItemField.setDeleted(true);
		this.persist(planRiskItemField);
	}
	
	/**
	 * Deleta do banco de dados um subitem
	 * 
	 * @param PlanRiskSubItem,
	 *            instância do PlanRiskSubItem a ser deletado
	 */
	
	public void delete(PlanRiskSubItem planRiskSubItem) {
		planRiskSubItem.setDeleted(true);
		this.persist(planRiskSubItem);
	}
	
	/**
	 * Deleta uma serie de subitens
	 * 
	 * @param PlanRiskSubItem,
	 *            instância do PlanRiskSubItem a ser deletado
	 */
	public void deleteSubItens(PlanRiskItem planRiskItem) {
		
		PaginatedList<PlanRiskSubItem> subitens = this.listSubItemByItem(planRiskItem, null);
		
		for(int j = 0; j < subitens.getList().size(); j ++) {
			this.delete(subitens.getList().get(j));
		}
	}
	
	/**
	 * Deleta do banco de dados um subcampo
	 * 
	 * @param PlanRiskSubItemField,
	 *            instância do PlanRiskSubItemField a ser deletado
	 */
	
	public void delete(PlanRiskSubItemField planRiskSubItemField) {
		planRiskSubItemField.setDeleted(true);
		this.persist(planRiskSubItemField);
	}

	
	/*public void deleteSubitens(PlanRiskItem planRiskItem) {
		
		PaginatedList<PlanRiskSubItem> subitens = this.listSubItemByItem(planRiskItem);
		
		for(int i = 0; i < subitens.getList().size(); i++) {
			this.deleteSubitem(subitens.getList().get(i));
		}
	}*/
	
	public PlanRiskItem retrievePlanRiskItembyId(long id) {
		
		Criteria criteria = this.dao.newCriteria(PlanRiskItem.class)
				.add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("id", id));
		
	return	(PlanRiskItem) criteria.uniqueResult();

}
	public List<Long> listItemIdsWithSubItems(List<PlanRiskItem> items, String term) {
		List<Long> itemsWithSubitems = new ArrayList<>();

		for (PlanRiskItem item : items) {
			List<PlanRiskSubItem> curSubitems = this.listSubItemByItem(item, term).getList();
			curSubitems.forEach((subitem) -> itemsWithSubitems.add(subitem.getPlanRiskItem().getId()));
		}
		
		return itemsWithSubitems;
	}

	public List<Long> listItemIdsWithFields(List<PlanRiskItem> items, String term) {
		List<Long> itemsWithFields = new ArrayList<>();

		for (PlanRiskItem item : items) {
			List<PlanRiskItemField> fields = this.listFieldsByPlanRiskItem(item).getList();

			if(fields.isEmpty() && FieldType.NO_TYPE.getName().contains(term.toUpperCase())) {
				itemsWithFields.add(item.getId());
			}

			for (PlanRiskItemField field: fields) {
				if (field.isText()) {
					if (FieldType.TEXT.getName().contains(term.toUpperCase())) {
						itemsWithFields.add(item.getId());
						break;
					}
				} else if (FieldType.FILE.getName().contains(term.toUpperCase())) {
					itemsWithFields.add(item.getId());
					break;
				}
			}
		}
		
		return itemsWithFields;
	}

	public PaginatedList<PlanRiskItem> getPlanRiskItemsWithDetails(PlanRisk planRisk, DefaultParams params) {
		PaginatedList<PlanRiskItem> items = this.listItensByPlanRisk(planRisk, params);
		List<PlanRiskItem> itemsList = items.getList();
		List<Long> itemIds = extractItemIds(itemsList);

		Map<Long, List<PlanRiskSubItem>> subitemsMap = this.listSubItemsByItemIds(itemIds);

		for (PlanRiskItem item : itemsList) {
			enrichPlanRiskItem(item, subitemsMap);
		}

		return items;
	}

	private List<Long> extractItemIds(List<PlanRiskItem> itemsList) {
		List<Long> itemIds = new ArrayList<>(itemsList.size());
		for (PlanRiskItem item : itemsList) {
			itemIds.add(item.getId());
		}
		return itemIds;
	}

	private void enrichPlanRiskItem(PlanRiskItem item, Map<Long, List<PlanRiskSubItem>> subitemsMap) {
		List<PlanRiskItemField> fields = this.listFieldsByPlanRiskItem(item).getList();
		for (PlanRiskItemField field : fields) {
			if (field.isText()) {
				item.setHasText(true);
			} else {
				item.setHasFile(true);
			}
		}

		List<PlanRiskSubItem> subitems = subitemsMap.get(item.getId());
		if (subitems != null) {
			enrichPlanRiskSubItems(item, subitems);
		}
	}

	private void enrichPlanRiskSubItems(PlanRiskItem item, List<PlanRiskSubItem> subitems) {
		for (PlanRiskSubItem subItem : subitems) {
			List<PlanRiskSubItemField> subItemFields = this.listSubFieldsBySubItem(subItem).getList();

			for (PlanRiskSubItemField field : subItemFields) {
				if (field.isText()) {
					subItem.setHasText(true);
				} else {
					subItem.setHasFile(true);
				}
			}

			subItem.setPlanRiskItem(null);
			subItem.setItemId(item.getId());
		}

		item.setSubitems(subitems);
	}

    public PaginatedList<PlanRiskSubItem> getAllSubitensForPlanRisk(PlanRisk planRisk) {
        PaginatedList<PlanRiskItem> itens = this.listItensByPlanRisk(planRisk);
        List<PlanRiskSubItem> subitensList = new ArrayList<>();
    
        for (PlanRiskItem item : itens.getList()) {
            PaginatedList<PlanRiskSubItem> subitemList = this.listSubItemByItem(item, null);
            subitensList.addAll(subitemList.getList());
        }
    
        PaginatedList<PlanRiskSubItem> subitens = new PaginatedList<>();
        subitens.setList(subitensList);
        subitens.setTotal((long) subitensList.size());
    
        return subitens;
    }
}
