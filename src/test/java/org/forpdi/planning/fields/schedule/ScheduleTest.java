package org.forpdi.planning.fields.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;

class ScheduleTest {

	@Test
	@DisplayName("Quando a periodicidade for configurada, então deve retornar o valor correto")
	void givenSchedule_whenGetPeriodicityEnable_thenShouldReturnCorrectValue() {
		Schedule schedule = new Schedule();
		schedule.setPeriodicityEnable(true);

		boolean result = schedule.isPeriodicityEnable();

		assertTrue(result, "O valor da periodicidade deve ser verdadeiro.");
	}

	@Test
	@DisplayName("Quando a periodicidade for desabilitada, então deve retornar o valor correto")
	void givenSchedule_whenGetPeriodicityEnable_thenShouldReturnFalse() {
		Schedule schedule = new Schedule();
		schedule.setPeriodicityEnable(false);

		boolean result = schedule.isPeriodicityEnable();

		assertFalse(result, "O valor da periodicidade deve ser falso.");
	}

	@Test
	@DisplayName("Quando a lista de ScheduleStructure for configurada, então deve retornar a lista correta")
	void givenSchedule_whenGetScheduleStructures_thenShouldReturnCorrectScheduleStructures() {
		ScheduleStructure scheduleStructure1 = new ScheduleStructure(); 
		ScheduleStructure scheduleStructure2 = new ScheduleStructure();
		List<ScheduleStructure> scheduleStructures = Arrays.asList(scheduleStructure1, scheduleStructure2);
		Schedule schedule = new Schedule();
		schedule.setScheduleStructures(scheduleStructures);

		List<ScheduleStructure> result = schedule.getScheduleStructures();

		assertEquals(scheduleStructures, result, "A lista de ScheduleStructures deve ser retornada corretamente.");
	}

	@Test
	@DisplayName("Quando a lista de ScheduleInstance for configurada, então deve retornar a lista correta")
	void givenSchedule_whenGetScheduleInstances_thenShouldReturnCorrectScheduleInstances() {
		ScheduleInstance scheduleInstance1 = new ScheduleInstance();
		ScheduleInstance scheduleInstance2 = new ScheduleInstance();
		List<ScheduleInstance> scheduleInstances = Arrays.asList(scheduleInstance1, scheduleInstance2);
		Schedule schedule = new Schedule();
		schedule.setScheduleInstances(scheduleInstances);

		List<ScheduleInstance> result = schedule.getScheduleInstances();

		assertEquals(scheduleInstances, result, "A lista de ScheduleInstances deve ser retornada corretamente.");
	}

	@Test
	@DisplayName("Quando o campo attributeId for configurado, então deve retornar o valor correto")
	void givenSchedule_whenGetAttributeId_thenShouldReturnCorrectAttributeId() {
		Long expectedAttributeId = 123L;
		Schedule schedule = new Schedule();
		schedule.setAttributeId(expectedAttributeId);

		Long result = schedule.getAttributeId();

		assertEquals(expectedAttributeId, result, "O valor de attributeId deve ser retornado corretamente.");
	}

	@Test
	@DisplayName("Quando o campo isDocument for configurado, então deve retornar o valor correto")
	void givenSchedule_whenGetIsDocument_thenShouldReturnCorrectIsDocument() {
		Schedule schedule = new Schedule();
		schedule.setIsDocument(true);

		boolean result = schedule.isDocument();

		assertTrue(result, "O valor de isDocument deve ser verdadeiro.");
	}

	@Test
	@DisplayName("Quando o campo isDocument for desabilitado, então deve retornar o valor correto")
	void givenSchedule_whenGetIsDocument_thenShouldReturnFalse() {
		Schedule schedule = new Schedule();
		schedule.setIsDocument(false);

		boolean result = schedule.isDocument();

		assertFalse(result, "O valor de isDocument deve ser falso.");
	}

}
