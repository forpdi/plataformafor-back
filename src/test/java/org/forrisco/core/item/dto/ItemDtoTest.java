package org.forrisco.core.item.dto;

import org.forrisco.core.item.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ItemDtoTest {

	@DisplayName("ItemDto Criação do DTO a partir de um Item.")
	@Test
	void testItemDTOCreation() {
		Item item = new Item();
		item.setName("Test Item");
		item.setDescription("Test Description");

		ItemDto itemDto = new ItemDto(item);

		assertNotNull(itemDto);
		assertEquals(item, itemDto.item());
	}
}