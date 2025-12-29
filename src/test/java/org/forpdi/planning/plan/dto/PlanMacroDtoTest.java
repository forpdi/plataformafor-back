package org.forpdi.planning.plan.dto;

import org.forpdi.planning.plan.PlanMacro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class PlanMacroDtoTest {

	@DisplayName("PlanMacroDto Criação do PnanMacroDTO")
	@Test
	void testPlanMacroDtoCreation() {
		PlanMacro plan = mock(PlanMacro.class);

		PlanMacroDto dtoCreated = new PlanMacroDto(plan);

		assertNotNull(dtoCreated, "O Dto não deveria ser nulo.");
		assertEquals(plan, dtoCreated.plan(), "O plano do Dto não corresponde ao esperado.");
	}
}