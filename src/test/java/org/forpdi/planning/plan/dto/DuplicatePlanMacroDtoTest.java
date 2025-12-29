package org.forpdi.planning.plan.dto;

import org.forpdi.planning.plan.PlanMacro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DuplicatePlanMacroDtoTest {

	@DisplayName("DuplicatePlanMacroDto Criação do DuplicatePlanMacroDto")
	@Test
	void testDuplicatePlanMacroDtoCreation() {
		PlanMacro mockMacro = mock(PlanMacro.class);
		DuplicatePlanMacroDto dtoCreated = new DuplicatePlanMacroDto(mockMacro, true, true, true, true);
		assertNotNull(dtoCreated, "O Dto não deveria ser nulo.");
		assertEquals(mockMacro, dtoCreated.macro(), "O plano macro não é o esperado.");
		assertTrue(dtoCreated.keepPlanLevel(), "keepPlanLevel não corresponde ao esperado, true.");
		assertTrue(dtoCreated.keepPlanContent(), "keepPlanContent não corresponde ao esperado, true.");
		assertTrue(dtoCreated.keepDocSection(), "keepDocSection não corresponde ao esperado, true.");
		assertTrue(dtoCreated.keepDocContent(), "keepDocContent não corresponde ao esperado, true.");
	}
}