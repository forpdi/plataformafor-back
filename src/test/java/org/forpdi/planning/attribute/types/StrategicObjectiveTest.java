package org.forpdi.planning.attribute.types;

import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StrategicObjectiveTest {

	@Test
	@DisplayName("Deve retornar o nome do widget corretamente")
	void testGetWidget() {
		StrategicObjective strategicObjective = new StrategicObjective();

		String widgetName = strategicObjective.getWidget();

		assertEquals("StrategicObjectivesField", widgetName);
	}

	@Test
	@DisplayName("Deve retornar o nome de exibição corretamente")
	void testGetDisplayName() {
		StrategicObjective strategicObjective = new StrategicObjective();

		String displayName = strategicObjective.getDisplayName();

		assertEquals("Objetivos estratégicos", displayName);
	}

	@Test
	@DisplayName("Deve retornar o wrapper correto")
	void testGetWrapper() {
		StrategicObjective strategicObjective = new StrategicObjective();

		AttributeTypeWrapper wrapper = strategicObjective.getWrapper();

		assertNotNull(wrapper);
		assertTrue(wrapper instanceof StrategicObjective.Wrapper);
	}

	@Test
	@DisplayName("Deve retornar null para valores do banco de dados no wrapper")
	void testWrapperFromDatabase() {
		StrategicObjective.Wrapper wrapper = new StrategicObjective.Wrapper();

		String result = wrapper.fromDatabase("someDatabaseValue");

		assertNull(result);
	}

	@Test
	@DisplayName("Deve retornar null para valores de visualização no wrapper")
	void testWrapperToDatabase() {
		StrategicObjective.Wrapper wrapper = new StrategicObjective.Wrapper();

		String result = wrapper.toDatabase("someViewValue");

		assertNull(result);
	}
}
