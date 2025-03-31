package org.forrisco.core.item;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.utils.Util;
import org.forrisco.core.policy.Policy;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Service;



/**
 * @author Matheus Nascimento
 */
@Service
public class ItemBS extends HibernateBusiness {
	
	
	/**
	 * Salva no banco de dados um novo item
	 * 
	 * @param Item,
	 *            instância do item a ser salvo
	 */
	public void save(Item item) {
		item.setId(null);
		item.setDeleted(false);
		this.persist(item);
	}
	
	/**
	 * Salva no banco de dados um novo subitem
	 * 
	 * @param Item,
	 *            instância do subitem a ser salvo
	 */
	public void save(SubItem subitem) {
		subitem.setId(null);
		subitem.setDeleted(false);
		this.persist(subitem);
	}
	
	/**
	 * Salva no banco de dados um novo campo
	 * 
	 * @param FieldItem,
	 *            instância do fieldItem a ser salvo
	 */
	public void save(FieldItem fieldItem) {
		fieldItem.setId(null);
		fieldItem.setDeleted(false);
		this.persist(fieldItem);
	}
	
	/**
	 * Salva no banco de dados um novo subcampo
	 * 
	 * @param FieldSubItem,
	 *            instância do fieldSubItem a ser salvo
	 */
	public void save(FieldSubItem subfield) {
		subfield.setId(null);
		subfield.setDeleted(false);
		this.persist(subfield);
	}
	
	
	
	/**
	 *Deleta do banco de dados um item
	 * 
	 * @param Item,
	 *            instância do item a ser deletado
	 */
	public void delete(Item item) {
		item.setDeleted(true);
		this.persist(item);
	}
	
	/**
	 *Deleta do banco de dados um subitem
	 * 
	 * @param SubItem,
	 *            instância do sbitem a ser deletado
	 */
	public void delete(SubItem subitem) {
		subitem.setDeleted(true);
		this.persist(subitem);
	}
	
	
	/**
	 *Deleta do banco de dados um campo
	 * 
	 * @param FieldItem,
	 *            instância do fielditem a ser deletado
	 */
	public void delete(FieldItem fieldItem) {
		fieldItem.setDeleted(true);
		this.persist(fieldItem);
	}

	/**
	 *Deleta do banco de dados um subcampo
	 * 
	 * @param FieldSubItem,
	 *            instância do fieldsubitem a ser deletado
	 */
	public void delete(FieldSubItem subfield) {
		subfield.setDeleted(true);
		this.persist(subfield);
	}

	
	/**
	 * Lista os itens de uma política.
	 * @param Policy
	 * 			política da qual se deseja obter os itens.
	 * 
	 * @return PaginatedList<Item>
	 * 			Lista de itens.
	 */
	public PaginatedList<Item> listItensByPolicy(Policy policy) {
	
		PaginatedList<Item> results = new PaginatedList<Item>();
		
		Criteria criteria = this.dao.newCriteria(Item.class)
				.add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("policy", policy))
				.addOrder(Order.asc("id"));
		
		Criteria count = this.dao.newCriteria(Item.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("policy", policy))
				.setProjection(Projections.countDistinct("id"));
		
		results.setList(this.dao.findByCriteria(criteria, Item.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	

	/**
	 * Lista as 'inormações gerais' de uma política.
	 * @param Policy
	 * 			política da qual se deseja obter o item.
	 * 
	 * @return Item
	 * 	
	 */
	public Item listInfoByPolicy(Policy policy) {
		Item result = new Item();
		
		Criteria criteria = this.dao.newCriteria(Item.class).add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("policy", policy)).addOrder(Order.asc("name"))
				.add(Restrictions.eq("name", "Informações gerais"))
				.addOrder(Order.asc("id"));
		
		/*Criteria count = this.dao.newCriteria(Item.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("policy", policy)).setProjection(Projections.countDistinct("id"));
		
		results.setList(this.dao.findByCriteria(criteria, Item.class));
		results.setTotal((Long) count.uniqueResult());*/
		
		result=(Item) criteria.uniqueResult();
		return result;
	}
	
	
	/**
	 * Lista campos de um item.
	 * 
	 * @param Item
	 * 			Item para recuperar os campos.
	 * 
	 * @return PaginatedList<FieldItem>
	 * 	
	 */
	public  PaginatedList<FieldItem> listFieldsByItem(Item item) {
		
		PaginatedList<FieldItem> results = new PaginatedList<FieldItem>();
		
		Criteria criteria = this.dao.newCriteria(FieldItem.class)
				.add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("item", item))
				.addOrder(Order.asc("id"));
		
		Criteria count = this.dao.newCriteria(FieldItem.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("item", item))
				.setProjection(Projections.countDistinct("id"));
		
		results.setList(this.dao.findByCriteria(criteria, FieldItem.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	/**
	 * Lista os subitens de um item.
	 * @param Item
	 * 			item do qual se deseja obter os subitens.
	 * 
	 * @return PaginatedList<SubItem>
	 * 			Lista de subitens.
	 */
	public PaginatedList<SubItem> listSubItensByItem(Item item) {
		PaginatedList<SubItem> results = new PaginatedList<SubItem>();
		
		Criteria criteria = this.dao.newCriteria(SubItem.class)
				.add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("item", item))
				.addOrder(Order.asc("id"));
		
		Criteria count = this.dao.newCriteria(SubItem.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("item", item))
				.setProjection(Projections.countDistinct("id"));
		
		results.setList(this.dao.findByCriteria(criteria, SubItem.class));
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
	public Map<Long, List<SubItem>> listSubItemsByItemIds(List<Long> itemIds) {
		if (itemIds.isEmpty()) {
			return new HashMap<>();
		}

		Criteria criteria = this.dao.newCriteria(SubItem.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("item", "item")
				.add(Restrictions.in("item.id", itemIds))
				.addOrder(Order.asc("id"));
		
		List<SubItem> subitems = this.dao.findByCriteria(criteria, SubItem.class);

		Map<Long, List<SubItem>> subitemsMap = Util.generateGroupedMap(subitems, si -> si.getItem().getId());
		
		for (Long itemId : itemIds) {
			if (!subitemsMap.containsKey(itemId)) {
				subitemsMap.put(itemId, Collections.emptyList());
			}
		}
		
		return subitemsMap;
	}

	/**
	 * Lista campos de um subitem.
	 * 
	 * @param SubItem
	 * 			SubItem para recuperar os campos.
	 * 
	 * @return PaginatedList<FieldSubItem>
	 * 	
	 */
	public PaginatedList<FieldSubItem> listFieldsBySubItem(SubItem subitem) {
		
		PaginatedList<FieldSubItem> results = new PaginatedList<FieldSubItem>();
		
		Criteria criteria = this.dao.newCriteria(FieldSubItem.class)
				.add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("subitem", subitem))
				.addOrder(Order.asc("name"));
		
		Criteria count = this.dao.newCriteria(FieldSubItem.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("subitem", subitem))
				.setProjection(Projections.countDistinct("id"));
		
		results.setList(this.dao.findByCriteria(criteria, FieldSubItem.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}

	public void deleteSubitens(Item item) {
		
		PaginatedList<SubItem> subitens = this.listSubItensByItem(item);
		
		for(int i=0;i<subitens.getList().size();i++) {
			this.deleteSubitem(subitens.getList().get(i));
		}
	}
	
	public void deleteSubitem(SubItem subitem) {
		
		PaginatedList<FieldSubItem> subfields = this.listFieldsBySubItem(subitem);
		
		for(int i=0;i<subfields.getList().size();i++) {
			this.delete(subfields.getList().get(i));
		}
		this.delete(subitem);
	}

	public Item retrieveItembyId(long id) {
		
		Criteria criteria = this.dao.newCriteria(Item.class)
				.add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("id", id));
		
	return	(Item) criteria.uniqueResult();
	
	}

	public PaginatedList<Item> getItemsWithDetailsByPolicy(Policy policy) {
		PaginatedList<Item> itens = this.listItensByPolicy(policy);
		List<Item> itemsList = itens.getList();
		List<Long> itemIds = extractItemIds(itemsList);

		Map<Long, List<SubItem>> subitemsMap = this.listSubItemsByItemIds(itemIds);

		for (Item item : itemsList) {
			enrichItemWithFieldsAndSubitems(item, subitemsMap);
		}

		return itens;
	}

	private List<Long> extractItemIds(List<Item> itemsList) {
		List<Long> itemIds = new ArrayList<>(itemsList.size());
		for (Item item : itemsList) {
			itemIds.add(item.getId());
		}
		return itemIds;
	}

	private void enrichItemWithFieldsAndSubitems(Item item, Map<Long, List<SubItem>> subitemsMap) {
		List<FieldItem> fields = this.listFieldsByItem(item).getList();
		setItemFlags(item, fields);

		List<SubItem> subitems = subitemsMap.get(item.getId());
		if (subitems != null) {
			for (SubItem subItem : subitems) {
				List<FieldSubItem> subItemFields = this.listFieldsBySubItem(subItem).getList();
				setSubItemFlags(subItem, subItemFields);
			}

			updateSubItemReferences(item, subitems);
		}

		item.setSubitems(subitems);
	}

	private void setItemFlags(Item item, List<FieldItem> fields) {
		for (FieldItem field : fields) {
			if (field.isText()) {
				item.setHasText(true);
			} else {
				item.setHasFile(true);
			}
		}
	}

	private void setSubItemFlags(SubItem subItem, List<FieldSubItem> subItemFields) {
		for (FieldSubItem field : subItemFields) {
			if (field.isText()) {
				subItem.setHasText(true);
			} else {
				subItem.setHasFile(true);
			}
		}
	}

	private void updateSubItemReferences(Item item, List<SubItem> subitems) {
		for (SubItem subItem : subitems) {
			subItem.setItem(null);
			subItem.setItemId(item.getId());
		}
	}
}
