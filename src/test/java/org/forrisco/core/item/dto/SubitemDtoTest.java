package org.forrisco.core.item.dto;

import org.forrisco.core.item.SubItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SubitemDtoTest {

	@DisplayName("SubitemDto Criação do DTO a partir de um Subitem.")
	@Test
	void testSubItemDTOCreation() {
		SubItem subitem = new SubItem();
		subitem.setId(1L);
		subitem.setName("Test Subitem");
		subitem.setDescription("Test Description");

		SubitemDto dto = new SubitemDto(subitem);

		assertNotNull(dto);
		assertEquals(subitem, dto.subitem());
	}
}