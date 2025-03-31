package org.forrisco.core.item.dto;

import org.forrisco.core.item.PlanRiskItem;
import org.forrisco.core.plan.PlanRisk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DuplicateItemDtoTest {

	@DisplayName("DuplicateItemDto Criação do DTO a partir de List<PlanRiskItem> e um PlanRisk.")
	@Test
	void testDuplicateItemDTOCreation() {
		PlanRisk planRisk = new PlanRisk();
		planRisk.setName("Test Plan Risk");

		List<PlanRiskItem> items = new ArrayList<>();
		PlanRiskItem item = new PlanRiskItem();
		item.setName("Test Item");
		item.setPlanRisk(planRisk);
		items.add(item);

		DuplicateItemDto dto = new DuplicateItemDto(items, planRisk);

		assertNotNull(dto);
		assertEquals(items, dto.itens());
		assertEquals(planRisk, dto.planRisk());
	}
}