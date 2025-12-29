package org.forpdi.planning.attribute.types;

import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SelectMultiFieldTest {

	@Test
	@DisplayName("Deve retornar o nome do widget corretamente")
	void testGetWidget() {
		SelectMultiField selectMultiField = new SelectMultiField();

		String widgetName = selectMultiField.getWidget();

		assertEquals("SelectMultiField", widgetName);
	}

	@Test
	@DisplayName("Deve retornar o nome de exibição corretamente")
	void testGetDisplayName() {
		SelectMultiField selectMultiField = new SelectMultiField();

		String displayName = selectMultiField.getDisplayName();

		assertEquals("Campo de seleção", displayName);
	}

	@Test
	@DisplayName("Deve retornar o wrapper corretamente")
	void testGetWrapper() {
		SelectMultiField selectMultiField = new SelectMultiField();

		AttributeTypeWrapper wrapper = selectMultiField.getWrapper();

		assertNotNull(wrapper);
		assertTrue(wrapper instanceof SelectMultiField.Wrapper);
	}

	@Test
	@DisplayName("Deve retornar o valor original no método fromDatabase do wrapper")
	void testWrapperFromDatabase() {
		SelectMultiField.Wrapper wrapper = new SelectMultiField.Wrapper();

		String databaseValue = "databaseValue";
		String result = wrapper.fromDatabase(databaseValue);

		assertEquals(databaseValue, result);
	}

	@Test
	@DisplayName("Deve retornar o valor original no método toDatabase do wrapper")
	void testWrapperToDatabase() {
		SelectMultiField.Wrapper wrapper = new SelectMultiField.Wrapper();

		String viewValue = "viewValue";
		String result = wrapper.toDatabase(viewValue);

		assertEquals(viewValue, result);
	}
}
