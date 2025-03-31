package org.forpdi.planning.attribute.types;

import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SelectFieldTest {

	@Test
	@DisplayName("Deve retornar o nome do widget corretamente")
	void testGetWidget() {
		SelectField selectField = new SelectField();

		String widgetName = selectField.getWidget();

		assertEquals("SelectField", widgetName);
	}

	@Test
	@DisplayName("Deve retornar o nome de exibição corretamente")
	void testGetDisplayName() {
		SelectField selectField = new SelectField();

		String displayName = selectField.getDisplayName();

		assertEquals("Campo de multipla seleção", displayName);
	}

	@Test
	@DisplayName("Deve retornar o wrapper corretamente")
	void testGetWrapper() {
		SelectField selectField = new SelectField();

		AttributeTypeWrapper wrapper = selectField.getWrapper();

		assertNotNull(wrapper);
		assertTrue(wrapper instanceof SelectField.Wrapper);
	}

	@Test
	@DisplayName("Deve retornar o valor original no método fromDatabase do wrapper")
	void testWrapperFromDatabase() {
		SelectField.Wrapper wrapper = new SelectField.Wrapper();

		String databaseValue = "databaseValue";
		String result = wrapper.fromDatabase(databaseValue);

		assertEquals(databaseValue, result);
	}

	@Test
	@DisplayName("Deve retornar o valor original no método toDatabase do wrapper")
	void testWrapperToDatabase() {
		SelectField.Wrapper wrapper = new SelectField.Wrapper();

		String viewValue = "viewValue";
		String result = wrapper.toDatabase(viewValue);

		assertEquals(viewValue, result);
	}
}
