package org.forpdi.planning.fields.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleStructureTest {

	@Test
	@DisplayName("Deve permitir definir e obter o rótulo (label)")
	void givenLabel_whenSetLabel_thenGetLabelShouldReturnSameValue() {
		ScheduleStructure scheduleStructure = new ScheduleStructure();
		String expectedLabel = "Teste de Rótulo";

		scheduleStructure.setLabel(expectedLabel);

		assertEquals(expectedLabel, scheduleStructure.getLabel(), "O rótulo obtido deve ser igual ao definido.");
	}

	@Test
	@DisplayName("Deve permitir definir e obter o tipo (type)")
	void givenType_whenSetType_thenGetTypeShouldReturnSameValue() {
		ScheduleStructure scheduleStructure = new ScheduleStructure();
		String expectedType = "Texto";

		scheduleStructure.setType(expectedType);

		assertEquals(expectedType, scheduleStructure.getType(), "O tipo obtido deve ser igual ao definido.");
	}

	@Test
	@DisplayName("Deve permitir definir e obter o agendamento (schedule)")
	void givenSchedule_whenSetSchedule_thenGetScheduleShouldReturnSameObject() {
		ScheduleStructure scheduleStructure = new ScheduleStructure();
		Schedule expectedSchedule = new Schedule();

		scheduleStructure.setSchedule(expectedSchedule);

		assertSame(expectedSchedule, scheduleStructure.getSchedule(), "O agendamento obtido deve ser o mesmo que foi definido.");
	}

	@Test
	@DisplayName("Deve permitir definir e obter o ID de exportação (exportScheduleId)")
	void givenExportScheduleId_whenSetExportScheduleId_thenGetExportScheduleIdShouldReturnSameValue() {
		ScheduleStructure scheduleStructure = new ScheduleStructure();
		Long expectedExportScheduleId = 123L;

		scheduleStructure.setExportScheduleId(expectedExportScheduleId);

		assertEquals(expectedExportScheduleId, scheduleStructure.getExportScheduleId(), "O ID de exportação obtido deve ser igual ao definido.");
	}
}
