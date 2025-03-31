package org.forrisco.core.bean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemSearchBeanTest {

	@DisplayName("ItemSearchBean Criação do objeto ItemSearchBean.")
	@Test
	void testItemSearchBeanCretionWithAllParams() {

		ItemSearchBean newItem = new ItemSearchBean();
		newItem.setId(1L);
		newItem.setParentId(2L);
		newItem.setName("Name");
		newItem.setDescription("One valid description");
		newItem.setLevel("Level");

		assertEquals(1L, newItem.getId());
		assertEquals(2L, newItem.getParentId());
		assertEquals("Name", newItem.getName());
		assertEquals("One valid description", newItem.getDescription());
		assertEquals("Level", newItem.getLevel());

	}
}