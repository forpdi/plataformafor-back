package org.forrisco.core.item.dto;

import org.forrisco.core.item.FieldSubItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FieldSubItemDtoTest {

	@DisplayName("FieldSubItemDto Criação do DTO a partir de um FieldSubItem.")
	@Test
	public void testFieldSubItemDtoCreation() {
		FieldSubItem fieldSubItem = new FieldSubItem();
		fieldSubItem.setName("Test Field");
		fieldSubItem.setDescription("Test Description");
		fieldSubItem.setText(true);

		FieldSubItemDto dto = new FieldSubItemDto(fieldSubItem);

		assertNotNull(dto);
		assertEquals(fieldSubItem, dto.fieldSubItem());
	}
}