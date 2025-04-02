package org.forrisco.core.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projection;
import org.forrisco.core.policy.Policy;
import org.hibernate.criterion.Order;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ItemBSTest {

	@Mock
	HibernateDAO dao;

	@Mock
	Criteria criteria;

	@InjectMocks
	private ItemBS itemBs;

	@DisplayName("ItemBS Salvar Item.")
	@Test
	void testSave() {
		Item itemToSave = new Item();
		itemToSave.setName("Some name");
		itemToSave.setDescription("Some Description");
		itemToSave.setPolicy(new Policy());

		itemBs.save(itemToSave);

		verify(dao, atMostOnce()).persist(itemToSave);
		assertNull(itemToSave.getId());
		assertFalse(itemToSave.isDeleted());
	}

	@DisplayName("ItemBS Salvar SubItem.")
	@Test
	void testSaveSubItem() {
		SubItem subItemToSave = new SubItem();
		subItemToSave.setName("Some name");
		subItemToSave.setDescription("Some Description");
		subItemToSave.setItem(new Item());

		itemBs.save(subItemToSave);

		verify(dao, atMostOnce()).persist(subItemToSave);
		assertNull(subItemToSave.getId());
		assertFalse(subItemToSave.isDeleted());
	}

	@DisplayName("ItemBS Salvar Campo.")
	@Test
	void testSaveFieldItem() {
		FieldItem fieldItemToSave = new FieldItem();
		fieldItemToSave.setName("Some name");
		fieldItemToSave.setDescription("Some Description");
		fieldItemToSave.setItem(new Item());
		fieldItemToSave.setText(false);

		itemBs.save(fieldItemToSave);

		verify(dao, atMostOnce()).persist(fieldItemToSave);
		assertNull(fieldItemToSave.getId());
		assertFalse(fieldItemToSave.isDeleted());
	}

	@DisplayName("ItemBS Salvar SubCampo.")
	@Test
	void testSaveFieldSubItem() {
		FieldSubItem fieldSubItemToSave = new FieldSubItem();
		fieldSubItemToSave.setName("Some name");
		fieldSubItemToSave.setDescription("Some Description");
		fieldSubItemToSave.setSubitem(new SubItem());
		fieldSubItemToSave.setText(false);

		itemBs.save(fieldSubItemToSave);

		verify(dao, atMostOnce()).persist(fieldSubItemToSave);
		assertNull(fieldSubItemToSave.getId());
		assertFalse(fieldSubItemToSave.isDeleted());
	}

	@DisplayName("ItemBS Deletar Item.")
	@Test
	void testDelete() {
		Item itemToDelete = new Item();
		itemToDelete.setName("Some name");
		itemToDelete.setDescription("Some Description");
		itemToDelete.setPolicy(new Policy());

		itemBs.delete(itemToDelete);

		verify(dao, atMostOnce()).persist(itemToDelete);
		assertTrue(itemToDelete.isDeleted());
	}

	@DisplayName("ItemBS Deletar SubItem.")
	@Test
	void testDeleteSubItem() {
		SubItem subItemToDelete = new SubItem();
		subItemToDelete.setName("Some name");
		subItemToDelete.setDescription("Some Description");
		subItemToDelete.setItem(new Item());

		itemBs.delete(subItemToDelete);

		verify(dao, atMostOnce()).persist(subItemToDelete);
		assertTrue(subItemToDelete.isDeleted());
	}

	@DisplayName("ItemBS Deletar Campo.")
	@Test
	void testDeleteFieldItem() {
		FieldItem fieldItemToDelete = new FieldItem();
		fieldItemToDelete.setName("Some name");
		fieldItemToDelete.setDescription("Some Description");
		fieldItemToDelete.setItem(new Item());
		fieldItemToDelete.setText(false);

		itemBs.delete(fieldItemToDelete);

		verify(dao, atMostOnce()).persist(fieldItemToDelete);
		assertTrue(fieldItemToDelete.isDeleted());
	}

	@DisplayName("ItemBS Deletar SubCampo.")
	@Test
	void testDeleteFieldSubItem() {
		FieldSubItem fieldSubItemToDelete = new FieldSubItem();
		fieldSubItemToDelete.setName("Some name");
		fieldSubItemToDelete.setDescription("Some Description");
		fieldSubItemToDelete.setSubitem(new SubItem());
		fieldSubItemToDelete.setText(false);

		itemBs.delete(fieldSubItemToDelete);

		verify(dao, atMostOnce()).persist(fieldSubItemToDelete);
		assertTrue(fieldSubItemToDelete.isDeleted());
	}

	@DisplayName("ItemBS Listar itens de uma política.")
	@Test
	void testListItensByPolicy() {
		Policy policy = new Policy();
		policy.setId(1L);

		Item itemA = new Item();
		itemA.setPolicy(policy);

		Item itemB = new Item();
		itemB.setPolicy(policy);
		PaginatedList<Item> expectedList = new PaginatedList<>();
		expectedList.setList(List.of(itemA, itemB));
		expectedList.setTotal(2L);

		when(dao.newCriteria(Item.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Item.class)).thenReturn(expectedList.getList());
		when(criteria.uniqueResult()).thenReturn(expectedList.getTotal());

		PaginatedList<Item> returnedList = itemBs.listItensByPolicy(policy);

		verify(dao, times(2)).newCriteria(Item.class);
		verify(criteria, times(4)).add(any(Criterion.class));
		verify(criteria, atMostOnce()).addOrder(any(Order.class));
		verify(criteria, atMostOnce()).setProjection(any(Projection.class));
		verify(dao, atMostOnce()).findByCriteria(criteria, Item.class);
		assertEquals(expectedList.getTotal(), returnedList.getTotal());
		assertEquals(expectedList.getList(), returnedList.getList());
	}

	@DisplayName("ItemBS Lista as 'informações gerais' de uma política.")
	@Test
	void testListInfoByPolicy() {
		Policy policyOfExpectedItem = new Policy();
		policyOfExpectedItem.setId(5L);
		Item itemExpected = new Item();
		itemExpected.setId(1L);
		itemExpected.setPolicy(policyOfExpectedItem);

		when(dao.newCriteria(Item.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(itemExpected);

		Item returnedItem = itemBs.listInfoByPolicy(policyOfExpectedItem);


		verify(dao).newCriteria(Item.class);
		verify(criteria, times(3)).add(any(Criterion.class));
		verify(criteria, times(2)).addOrder(any(Order.class));
		verify(criteria).uniqueResult();
		assertEquals(itemExpected.getId(), returnedItem.getId(),
			"O id do item retornado deveria ser igual ao do esperado.");
		assertEquals(policyOfExpectedItem.getId(), returnedItem.getPolicy().getId(),
			"A política do Item retornado deveria ser igual ao do item esperado.");
	}

	@DisplayName("ItemBS lista os campos de um item")
	@Test
	void testListFieldsByItem() {
		FieldItem itemA = new FieldItem();
		FieldItem itemB = new FieldItem();

		Item item = new Item();
		item.setFieldItem(List.of(itemA, itemB));
		item.setId(1L);

		PaginatedList<FieldItem> expectedList = new PaginatedList<>();
		expectedList.setList(item.getFieldItem());
		expectedList.setTotal((long) item.getFieldItem().size());

		when(dao.newCriteria(FieldItem.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, FieldItem.class)).thenReturn(expectedList.getList());
		when(criteria.uniqueResult()).thenReturn(expectedList.getTotal());

		PaginatedList<FieldItem> returnedList = itemBs.listFieldsByItem(item);

		verify(dao, times(2)).newCriteria(FieldItem.class);
		verify(criteria, times(4)).add(any(Criterion.class));
		verify(criteria, atMostOnce()).addOrder(any(Order.class));
		verify(criteria, atMostOnce()).setProjection(any(Projection.class));
		verify(dao, atMostOnce()).findByCriteria(criteria, FieldItem.class);
		assertEquals(expectedList.getTotal(), returnedList.getTotal());
		assertEquals(expectedList.getList(), returnedList.getList());
	}

	@DisplayName("ItemBS lista os Subitens de um item")
	@Test
	void testListSubItensByItem() {
		SubItem itemA = new SubItem();
		SubItem itemB = new SubItem();

		Item item = new Item();
		item.setSubitems(List.of(itemA, itemB));
		item.setId(2L);

		PaginatedList<SubItem> expectedList = new PaginatedList<>();
		expectedList.setList(item.getSubitems());
		expectedList.setTotal((long) item.getSubitems().size());

		when(dao.newCriteria(SubItem.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, SubItem.class)).thenReturn(expectedList.getList());
		when(criteria.uniqueResult()).thenReturn(expectedList.getTotal());

		PaginatedList<SubItem> returnedList = itemBs.listSubItensByItem(item);

		verify(dao, times(2)).newCriteria(SubItem.class);
		verify(criteria, times(4)).add(any(Criterion.class));
		verify(criteria, atMostOnce()).addOrder(any(Order.class));
		verify(criteria, atMostOnce()).setProjection(any(Projection.class));
		verify(dao, atMostOnce()).findByCriteria(criteria, SubItem.class);
		assertEquals(expectedList.getTotal(), returnedList.getTotal());
		assertEquals(expectedList.getList(), returnedList.getList());
	}

	@DisplayName("ItemBS Lista os SubItens mapeados pelos ids dos itens passados.")
	@Test
	void testListSubItemsByItemIdsNotEmpty() {
		Item item1 = new Item();
		item1.setId(1L);
		Item item2 = new Item();
		item2.setId(2L);
		Item item3 = new Item();
		item3.setId(3L);
		List<Long> providedItemsIds = List.of(1L, 2L, 3L);

		SubItem subItem1 = new SubItem();
		subItem1.setItem(item1);
		SubItem subItem2 = new SubItem();
		subItem2.setItem(item2);
		SubItem subItem3 = new SubItem();
		subItem3.setItem(item3);

		List<SubItem> listOfSubitems = List.of(subItem1, subItem2, subItem3);

		when(dao.newCriteria(SubItem.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString())).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, SubItem.class)).thenReturn(listOfSubitems);

		Map<Long, List<SubItem>> returnedList = itemBs.listSubItemsByItemIds(providedItemsIds);

		verify(dao).newCriteria(SubItem.class);
		verify(criteria, times(2)).add(any(Criterion.class));
		verify(criteria).createAlias("item", "item");
		verify(criteria).addOrder(any(Order.class));
		verify(dao).findByCriteria(criteria, SubItem.class);

		assertEquals(3, returnedList.size());
		assertTrue(returnedList.get(1L).contains(subItem1));
		assertTrue(returnedList.get(2L).contains(subItem2));
		assertTrue(returnedList.get(3L).contains(subItem3));
		assertFalse(returnedList.get(1L).contains(subItem2));
		assertFalse(returnedList.get(2L).contains(subItem3));
	}

	@DisplayName("ItemBS Lista os SubItens mapeados pelos ids dos itens passados.")
	@Test
	void testListSubItemsByItemIdsEmpty() {
		List<Long> providedItemsIds = new ArrayList<>();

		Map<Long, List<SubItem>> returnedList = itemBs.listSubItemsByItemIds(providedItemsIds);

		assertTrue(returnedList.isEmpty());
	}

	@DisplayName("ItemBS lista os Campos de um SubItem")
	@Test
	void testListFieldsBySubItem() {
		FieldSubItem subItemA = new FieldSubItem();
		FieldSubItem subItemB = new FieldSubItem();

		SubItem subItem = new SubItem();
		subItem.setFieldSubItem(List.of(subItemA, subItemB));
		subItem.setId(2L);

		PaginatedList<FieldSubItem> expectedList = new PaginatedList<>();
		expectedList.setList(subItem.getFieldSubItem());
		expectedList.setTotal((long) subItem.getFieldSubItem().size());

		when(dao.newCriteria(FieldSubItem.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, FieldSubItem.class)).thenReturn(expectedList.getList());
		when(criteria.uniqueResult()).thenReturn(expectedList.getTotal());

		PaginatedList<FieldSubItem> returnedList = itemBs.listFieldsBySubItem(subItem);

		verify(dao, times(2)).newCriteria(FieldSubItem.class);
		verify(criteria, times(4)).add(any(Criterion.class));
		verify(criteria, atMostOnce()).addOrder(any(Order.class));
		verify(criteria, atMostOnce()).setProjection(any(Projection.class));
		verify(dao, atMostOnce()).findByCriteria(criteria, FieldSubItem.class);
		assertEquals(expectedList.getTotal(), returnedList.getTotal());
		assertEquals(expectedList.getList(), returnedList.getList());
	}

	@DisplayName("ItemBS Deletar SubItens de um Item.")
	@Test
	void testDeleteSubitens() {
		SubItem itemA = new SubItem();
		SubItem itemB = new SubItem();

		Item item = new Item();
		item.setSubitems(List.of(itemA, itemB));
		item.setId(2L);

		PaginatedList<SubItem> listSubItemExpected = new PaginatedList<>();
		listSubItemExpected.setList(item.getSubitems());
		listSubItemExpected.setTotal((long) item.getSubitems().size());

		int totalSubItens = listSubItemExpected.getTotal().intValue();

		ItemBS itemBSMock = mock(ItemBS.class);
		doCallRealMethod().when(itemBSMock).deleteSubitens(item);
		when(itemBSMock.listSubItensByItem(item)).thenReturn(listSubItemExpected);
		doNothing().when(itemBSMock).deleteSubitem(any(SubItem.class));

		itemBSMock.deleteSubitens(item);

		verify(itemBSMock, atMostOnce()).listFieldsByItem(item);
		verify(itemBSMock, times(totalSubItens)).deleteSubitem(any(SubItem.class));
	}

	@DisplayName("ItemBS Deletar FieldSubItem de um SubItem.")
	@Test
	void testDeleteSubitem() {
		FieldSubItem fieldSubItemA = new FieldSubItem();
		FieldSubItem fieldSubItemB = new FieldSubItem();

		SubItem subItem = new SubItem();
		subItem.setFieldSubItem(List.of(fieldSubItemA, fieldSubItemB));
		subItem.setId(2L);

		PaginatedList<FieldSubItem> listFieldSubItemExpected = new PaginatedList<>();
		listFieldSubItemExpected.setList(subItem.getFieldSubItem());
		listFieldSubItemExpected.setTotal((long) subItem.getFieldSubItem().size());

		int totalSubItens = listFieldSubItemExpected.getTotal().intValue();

		ItemBS itemBSMock = mock(ItemBS.class);
		doCallRealMethod().when(itemBSMock).deleteSubitem(subItem);
		when(itemBSMock.listFieldsBySubItem(subItem)).thenReturn(listFieldSubItemExpected);
		doNothing().when(itemBSMock).delete(any(FieldSubItem.class));
		doNothing().when(itemBSMock).delete(subItem);

		itemBSMock.deleteSubitem(subItem);

		verify(itemBSMock, atMostOnce()).listFieldsBySubItem(subItem);
		verify(itemBSMock, times(totalSubItens)).delete(any(FieldSubItem.class));
		verify(itemBSMock).delete(any(SubItem.class));
	}

	@DisplayName("Deve retornar uma pesquisa do item pelo seu id.")
	@Test
	void testRetrieveItemById() {
		Item expectedItem = new Item();
		expectedItem.setId(2L);

		when(dao.newCriteria(Item.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(expectedItem);

		Item returnedItem = itemBs.retrieveItembyId(2L);

		assertNotNull(returnedItem, "O item retornado não deveria ser nulo.");
		assertEquals(expectedItem, returnedItem, "Os items deveriam ser iguais");
	}

	@Test
	void testGetItemsWithDetailsByPolicyAndFieldSubItemIsText() {
		Policy policy = new Policy();
		policy.setId(1L);

		PaginatedList<Item> list = new PaginatedList<>();
		Item item1 = new Item();
		item1.setId(1L);
		item1.setPolicy(policy);
		list.setList(List.of(item1));
		list.setTotal(1L);

		PaginatedList<FieldItem> list2 = new PaginatedList<>();
		FieldItem fieldItem = new FieldItem();
		fieldItem.setText(true);
		fieldItem.setItem(item1);
		list2.setList(List.of(fieldItem));
		list2.setTotal(1L);

		List<SubItem> listSubItem = new ArrayList<>();
		SubItem subItem = new SubItem();
		subItem.setItem(item1);
		subItem.setItemId(item1.getId());
		listSubItem.add(subItem);

		Map<Long, List<SubItem>> map = new HashMap<>();
		map.put(1L, listSubItem);

		PaginatedList<FieldSubItem> fieldSubItems = new PaginatedList<>();
		FieldSubItem fieldSubItem = new FieldSubItem();
		fieldSubItem.setId(1L);
		fieldSubItem.setSubitem(subItem);
		fieldSubItem.setText(true);
		fieldSubItems.setList(List.of(fieldSubItem));
		fieldSubItems.setTotal(1L);

		ItemBS mock = mock(ItemBS.class);
		when(mock.listItensByPolicy(policy)).thenReturn(list);
		when(mock.listFieldsByItem(item1)).thenReturn(list2);
		when(mock.listSubItemsByItemIds(List.of(1L))).thenReturn(map);
		when(mock.listFieldsBySubItem(subItem)).thenReturn(fieldSubItems);
		doCallRealMethod().when(mock).getItemsWithDetailsByPolicy(policy);

		PaginatedList<Item> returnedList = mock.getItemsWithDetailsByPolicy(policy);

		Item itemOfList = returnedList.getList().get(0);
		assertNotNull(itemOfList.getPolicy());
		assertNotNull(itemOfList.getSubitems());
		assertTrue(itemOfList.getSubitems().get(0).hasText());
	}

	@Test
	void testGetItemsWithDetailsByPolicyAndFieldSubItemIsFile() {
		Policy policy = new Policy();
		policy.setId(1L);

		PaginatedList<Item> list = new PaginatedList<>();
		Item item1 = new Item();
		item1.setId(1L);
		item1.setPolicy(policy);
		list.setList(List.of(item1));
		list.setTotal(1L);

		PaginatedList<FieldItem> list2 = new PaginatedList<>();
		FieldItem fieldItem = new FieldItem();
		fieldItem.setText(false);
		fieldItem.setItem(item1);
		list2.setList(List.of(fieldItem));
		list2.setTotal(1L);

		List<SubItem> listSubItem = new ArrayList<>();
		SubItem subItem = new SubItem();
		subItem.setItem(item1);
		subItem.setItemId(item1.getId());
		listSubItem.add(subItem);

		Map<Long, List<SubItem>> map = new HashMap<>();
		map.put(1L, listSubItem);

		PaginatedList<FieldSubItem> fieldSubItems = new PaginatedList<>();
		FieldSubItem fieldSubItem = new FieldSubItem();
		fieldSubItem.setId(1L);
		fieldSubItem.setSubitem(subItem);
		fieldSubItem.setText(false);
		fieldSubItems.setList(List.of(fieldSubItem));
		fieldSubItems.setTotal(1L);

		ItemBS mock = mock(ItemBS.class);
		when(mock.listItensByPolicy(policy)).thenReturn(list);
		when(mock.listFieldsByItem(item1)).thenReturn(list2);
		when(mock.listSubItemsByItemIds(List.of(1L))).thenReturn(map);
		when(mock.listFieldsBySubItem(subItem)).thenReturn(fieldSubItems);
		doCallRealMethod().when(mock).getItemsWithDetailsByPolicy(policy);

		PaginatedList<Item> returnedList = mock.getItemsWithDetailsByPolicy(policy);

		Item itemOfList = returnedList.getList().get(0);
		assertNotNull(itemOfList.getPolicy());
		assertNotNull(itemOfList.getSubitems());
		assertTrue(itemOfList.getSubitems().get(0).hasFile());
	}
}