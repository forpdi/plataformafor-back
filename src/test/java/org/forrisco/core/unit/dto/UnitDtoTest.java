package org.forrisco.core.unit.dto;

import org.forpdi.core.user.User;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UnitDtoTest {

	@DisplayName("UnitDto Criação do DTO")
	@Test
	void testUnitDtoCreation() {
		Unit unit = new Unit();
		unit.setName("Test Unit");
		unit.setAbbreviation("TU");
		unit.setDescription("Test Description");
		unit.setUser(new User());
		unit.setPlanRisk(new PlanRisk());

		UnitDto unitDto = new UnitDto(unit);

		assertNotNull(unitDto);
		assertEquals("Test Unit", unitDto.unit().getName());
		assertEquals("TU", unitDto.unit().getAbbreviation());
		assertEquals("Test Description", unitDto.unit().getDescription());
		assertNotNull(unitDto.unit().getUser());
		assertNotNull(unitDto.unit().getPlanRisk());
	}
}