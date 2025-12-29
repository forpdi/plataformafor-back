package org.forrisco.core.item.dto;

import org.forrisco.core.item.PlanRiskSubItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlanRiskSubItemDtoTest {

	@DisplayName("PlanRiskSubItemDto Criação do DTO a partir de um PlanRiskSubItem.")
	@Test
	public void testPlanRiskSubItemDtoCreation() {
		PlanRiskSubItem subItem = new PlanRiskSubItem();
		subItem.setName("Test Sub Item");
		subItem.setDescription("Test Description");

		PlanRiskSubItemDto dto = new PlanRiskSubItemDto(subItem);

		assertNotNull(dto);
		assertEquals(subItem, dto.planRiskSubItem());
	}
}