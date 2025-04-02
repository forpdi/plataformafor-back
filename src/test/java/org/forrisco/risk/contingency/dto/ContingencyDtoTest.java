package org.forrisco.risk.contingency.dto;

import org.forpdi.core.user.User;
import org.forrisco.risk.Risk;
import org.forrisco.risk.contingency.Contingency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContingencyDtoTest {

	@DisplayName("ContingencyDto criação do DTO.")
	@Test
	void testContingencyDtoCreation() {
		User user = new User();
		Risk risk = new Risk();
		String action = "Test action";
		Date validityBegin = new Date();
		Date validityEnd = new Date();

		Contingency contingency = new Contingency();
		contingency.setUser(user);
		contingency.setRisk(risk);
		contingency.setAction(action);
		contingency.setValidityBegin(validityBegin);
		contingency.setValidityEnd(validityEnd);

		ContingencyDto dto = new ContingencyDto(contingency);

		assertNotNull(dto);
		assertEquals(contingency, dto.contingency());
	}
}