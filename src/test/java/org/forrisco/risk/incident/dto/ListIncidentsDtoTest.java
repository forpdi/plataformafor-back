package org.forrisco.risk.incident.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ListIncidentsDtoTest {

	@DisplayName("ListIncidentsDto Criação do DTO.")
	@Test
	void testListIncidentsDtoCreation() {
		List<Long> incidentIds = Arrays.asList(1L, 2L, 3L);
		Integer page = 0;
		Integer pageSize = 10;

		ListIncidentsDto dto = new ListIncidentsDto(incidentIds, page, pageSize);

		assertNotNull(dto);
		assertEquals(incidentIds, dto.incidentsId());
		assertEquals(page, dto.page());
		assertEquals(pageSize, dto.pageSize());
	}
}