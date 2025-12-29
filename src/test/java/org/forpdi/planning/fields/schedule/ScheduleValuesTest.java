package org.forpdi.planning.fields.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

class ScheduleValuesTest {

	@Test
	@DisplayName("Dado uma instância de ScheduleValues com valores válidos, quando os getters são chamados, então os valores corretos são retornados")
	void testGetters_ValidScheduleValues() {
		ScheduleInstance scheduleInstance = new ScheduleInstance();
		ScheduleStructure scheduleStructure = new ScheduleStructure();

		ScheduleValues scheduleValues = new ScheduleValues();
		scheduleValues.setValue("Sample Value");
		scheduleValues.setValueAsNumber(123.45);
		scheduleValues.setValueAsDate(new Date());
		scheduleValues.setScheduleInstance(scheduleInstance);
		scheduleValues.setScheduleStructure(scheduleStructure);
		scheduleValues.setExportScheduleStructureId(100L);
		scheduleValues.setExportScheduleInstanceId(200L);

		String value = scheduleValues.getValue();
		Double valueAsNumber = scheduleValues.getValueAsNumber();
		Date valueAsDate = scheduleValues.getValueAsDate();
		ScheduleInstance retrievedScheduleInstance = scheduleValues.getScheduleInstance();
		ScheduleStructure retrievedScheduleStructure = scheduleValues.getScheduleStructure();
		Long exportScheduleStructureId = scheduleValues.getExportScheduleStructureId();
		Long exportScheduleInstanceId = scheduleValues.getExportScheduleInstanceId();

		assertEquals("Sample Value", value, "O valor deve ser 'Sample Value'.");
		assertEquals(123.45, valueAsNumber, "O valor numérico deve ser 123.45.");
		assertNotNull(valueAsDate, "A data não deve ser nula.");
		assertEquals(scheduleInstance, retrievedScheduleInstance, "A instância do cronograma deve ser a mesma.");
		assertEquals(scheduleStructure, retrievedScheduleStructure, "A estrutura do cronograma deve ser a mesma.");
		assertEquals(100L, exportScheduleStructureId, "O ID da estrutura do cronograma de exportação deve ser 100.");
		assertEquals(200L, exportScheduleInstanceId, "O ID da instância do cronograma de exportação deve ser 200.");
	}

	@Test
	@DisplayName("Dado uma instância de ScheduleValues sem valores definidos, quando os getters são chamados, então os valores retornados devem ser nulos ou default")
	void testGetters_EmptyScheduleValues() {
		ScheduleValues scheduleValues = new ScheduleValues();

		String value = scheduleValues.getValue();
		Double valueAsNumber = scheduleValues.getValueAsNumber();
		Date valueAsDate = scheduleValues.getValueAsDate();
		ScheduleInstance retrievedScheduleInstance = scheduleValues.getScheduleInstance();
		ScheduleStructure retrievedScheduleStructure = scheduleValues.getScheduleStructure();
		Long exportScheduleStructureId = scheduleValues.getExportScheduleStructureId();
		Long exportScheduleInstanceId = scheduleValues.getExportScheduleInstanceId();

		assertNull(value, "O valor deve ser null por padrão.");
		assertNull(valueAsNumber, "O valor numérico deve ser null por padrão.");
		assertNull(valueAsDate, "A data deve ser null por padrão.");
		assertNull(retrievedScheduleInstance, "A instância do cronograma deve ser null por padrão.");
		assertNull(retrievedScheduleStructure, "A estrutura do cronograma deve ser null por padrão.");
		assertNull(exportScheduleStructureId, "O ID da estrutura do cronograma de exportação deve ser null por padrão.");
		assertNull(exportScheduleInstanceId, "O ID da instância do cronograma de exportação deve ser null por padrão.");
	}

	@Test
	@DisplayName("Dado uma instância de ScheduleValues com valor numérico, quando o setter de valueAsNumber é chamado, então o valor numérico é alterado corretamente")
	void testSetValueAsNumber() {
		ScheduleValues scheduleValues = new ScheduleValues();
		scheduleValues.setValueAsNumber(500.75);

		scheduleValues.setValueAsNumber(800.50);

		assertEquals(800.50, scheduleValues.getValueAsNumber(), "O valor numérico deve ser 800.50.");
	}

	@Test
	@DisplayName("Dado uma instância de ScheduleValues com valor de data, quando o setter de valueAsDate é chamado, então o valor de data é alterado corretamente")
	void testSetValueAsDate() {
		ScheduleValues scheduleValues = new ScheduleValues();
		Date currentDate = new Date();
		scheduleValues.setValueAsDate(currentDate);

		Date newDate = new Date(currentDate.getTime() + 1000000);
		scheduleValues.setValueAsDate(newDate);

		assertEquals(newDate, scheduleValues.getValueAsDate(), "A data deve ser alterada para a nova data.");
	}

	@Test
	@DisplayName("Dado uma instância de ScheduleValues com exportScheduleStructureId, quando o setter é chamado, então o ID de exportação da estrutura do cronograma é atribuído corretamente")
	void testSetExportScheduleStructureId() {
		ScheduleValues scheduleValues = new ScheduleValues();

		scheduleValues.setExportScheduleStructureId(555L);

		assertEquals(555L, scheduleValues.getExportScheduleStructureId(), "O ID da estrutura do cronograma de exportação deve ser 555.");
	}

	@Test
	@DisplayName("Dado uma instância de ScheduleValues com exportScheduleInstanceId, quando o setter é chamado, então o ID de exportação da instância do cronograma é atribuído corretamente")
	void testSetExportScheduleInstanceId() {
		ScheduleValues scheduleValues = new ScheduleValues();

		scheduleValues.setExportScheduleInstanceId(999L);

		assertEquals(999L, scheduleValues.getExportScheduleInstanceId(), "O ID da instância do cronograma de exportação deve ser 999.");
	}
}
