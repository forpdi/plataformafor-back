package org.forpdi.planning.attribute.types;

import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableFieldTest {

	@Test
	@DisplayName("Deve retornar o nome do widget corretamente")
	void testGetWidget() {
		TableField tableField = new TableField();

		String widgetName = tableField.getWidget();

		assertEquals("TableField", widgetName);
	}

	@Test
	@DisplayName("Deve retornar o nome de exibição corretamente")
	void testGetDisplayName() {
		TableField tableField = new TableField();

		String displayName = tableField.getDisplayName();

		assertEquals("Tabela", displayName);
	}

	@Test
	@DisplayName("Deve retornar o wrapper correto")
	void testGetWrapper() {
		TableField tableField = new TableField();

		AttributeTypeWrapper wrapper = tableField.getWrapper();

		assertNotNull(wrapper);
		assertTrue(wrapper instanceof TableField.Wrapper);
	}

	@Test
	@DisplayName("Deve retornar null para valores do banco de dados no wrapper")
	void testWrapperFromDatabase() {
		TableField.Wrapper wrapper = new TableField.Wrapper();

		String result = wrapper.fromDatabase("someDatabaseValue");

		assertNull(result);
	}

	@Test
	@DisplayName("Deve retornar null para valores de visualização no wrapper")
	void testWrapperToDatabase() {
		TableField.Wrapper wrapper = new TableField.Wrapper();

		String result = wrapper.toDatabase("someViewValue");

		assertNull(result);
	}
}
