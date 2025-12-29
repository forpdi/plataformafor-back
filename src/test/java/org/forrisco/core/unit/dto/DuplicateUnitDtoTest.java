package org.forrisco.core.unit.dto;

import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DuplicateUnitDtoTest {

	@DisplayName("DuplicateUnitDto criando instância da classe.")
	@Test
	void testDuplicateUnitDtoObjectCreation() {
		List<Unit> units = new ArrayList<>();
		Unit unit1 = new Unit();
		unit1.setName("Unit 1");
		Unit unit2 = new Unit();
		unit2.setName("Unit 2");
		units.add(unit1);
		units.add(unit2);

		PlanRisk planRisk = new PlanRisk();
		planRisk.setName("Test Plan");

		DuplicateUnitDto dto = new DuplicateUnitDto(units, planRisk);

		assertNotNull(dto, "O objeto não deveria ser nulo.");
		assertEquals(2, dto.units().size(), "A quantidade de unidades não corresponde.");
		assertEquals(planRisk, dto.planRisk(), "O plano de risco não deveria ser diferente.");
	}
}