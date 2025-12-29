package org.forpdi.planning.plan.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UpdatePlanDtoTest {

	@DisplayName("UpdatePlanDto Teste da criação do UpdatePlanDto")
	@Test
	void testUpdatePlanDtoCreation() {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);

		UpdatePlanDto dtoCreated =
			new UpdatePlanDto(1L, "Dto", "Description", today.toString(), tomorrow.toString());

		assertNotNull(dtoCreated, "O Dto não deveria ser nulo.");
		assertEquals(1L, dtoCreated.id(), "O id do UpdatePlanDto não corresponde ao esperado.");
		assertEquals("Dto", dtoCreated.name(), "O name do UpdatePlanDto não corresponde ao esperado.");
		assertEquals(today.toString(), dtoCreated.begin(),
			"O valor begin do UpdatePlanDto não corresponde ao esperado.");
		assertEquals(tomorrow.toString(), dtoCreated.end(),
			"O valor end name do UpdatePlanDto não corresponde ao esperado.");
	}
}