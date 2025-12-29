package org.forrisco.risk.incident.dto;

import org.forpdi.core.user.User;
import org.forrisco.risk.Risk;
import org.forrisco.risk.incident.Incident;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IncidentDtoTest {

	@DisplayName("IncidentDto Criação do DTO.")
	@Test
	void testIncidentDtoCreation() {
		User user = new User();
		Risk risk = new Risk();
		Date begin = new Date();

		Incident incident = new Incident(user, risk, "Test description", "Test action", 1, begin, 1L);

		IncidentDto dto = new IncidentDto(incident);

		assertNotNull(dto);
		assertEquals(incident, dto.incident());
		assertEquals("Test description", dto.incident().getDescription());
		assertEquals("Test action", dto.incident().getAction());
		assertEquals(1, dto.incident().getType());
		assertEquals(begin, dto.incident().getBegin());
		assertEquals(user, dto.incident().getUser());
		assertEquals(risk, dto.incident().getRisk());
	}
}