package org.forpdi.planning.attribute.types;

import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleFieldTest {

	@Test
	@DisplayName("Deve retornar o nome do widget corretamente")
	void testGetWidget() {
		ScheduleField scheduleField = new ScheduleField();

		String widgetName = scheduleField.getWidget();

		assertEquals("ScheduleField", widgetName);
	}

	@Test
	@DisplayName("Deve retornar o nome de exibição corretamente")
	void testGetDisplayName() {
		ScheduleField scheduleField = new ScheduleField();

		String displayName = scheduleField.getDisplayName();

		assertEquals("Cronograma", displayName);
	}

	@Test
	@DisplayName("Deve retornar o wrapper corretamente")
	void testGetWrapper() {
		ScheduleField scheduleField = new ScheduleField();

		AttributeTypeWrapper wrapper = scheduleField.getWrapper();

		assertNotNull(wrapper);
		assertTrue(wrapper instanceof ScheduleField.Wrapper);
	}

	@Test
	@DisplayName("Deve retornar null no método fromDatabase do wrapper")
	void testWrapperFromDatabase() {
		ScheduleField.Wrapper wrapper = new ScheduleField.Wrapper();

		String databaseValue = "databaseValue";
		String result = wrapper.fromDatabase(databaseValue);

		assertNull(result);
	}

	@Test
	@DisplayName("Deve retornar null no método toDatabase do wrapper")
	void testWrapperToDatabase() {
		ScheduleField.Wrapper wrapper = new ScheduleField.Wrapper();

		String viewValue = "viewValue";
		String result = wrapper.toDatabase(viewValue);

		assertNull(result);
	}
}
