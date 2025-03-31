package org.forrisco.risk.preventiveaction.dto;

import org.forpdi.core.user.User;
import org.forrisco.risk.Risk;
import org.forrisco.risk.preventiveaction.PreventiveAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PreventiveActionDtoTest {

	@DisplayName("PreventiveActionDto Criação do DTO.")
	@Test
	void testPreventiveActionDtoCreation() {
		PreventiveAction action = new PreventiveAction();
		action.setAction("Test action");
		action.setAccomplished(false);
		action.setUser(new User());
		action.setRisk(new Risk());
		action.setValidityBegin(new Date());
		action.setValidityEnd(new Date());

		PreventiveActionDto dto = new PreventiveActionDto(action);

		assertNotNull(dto);
		assertEquals(action, dto.action());
	}
}