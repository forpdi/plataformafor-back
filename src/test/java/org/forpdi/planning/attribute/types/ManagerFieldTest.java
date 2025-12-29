package org.forpdi.planning.attribute.types;

import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagerFieldTest {

	@Test
	@DisplayName("Deve retornar o nome do widget correto")
	void testGetWidget() {
		ManagerField managerField = new ManagerField();

		String result = managerField.getWidget();

		assertEquals("ManagerField", result, "O nome do widget deve ser 'ManagerField'.");
	}

	@Test
	@DisplayName("Deve retornar o wrapper correto")
	void testGetWrapper() {
		ManagerField managerField = new ManagerField();

		AttributeTypeWrapper result = managerField.getWrapper();

		assertNotNull(result, "O wrapper não deve ser nulo.");
		assertTrue(result instanceof ManagerField.Wrapper, "O wrapper deve ser uma instância de ManagerField.Wrapper.");
	}

	@Test
	@DisplayName("Deve retornar o nome de exibição correto")
	void testGetDisplayName() {
		ManagerField managerField = new ManagerField();

		String result = managerField.getDisplayName();

		assertEquals("Campo de seleção do gestor", result, "O nome de exibição deve ser 'Campo de seleção do gestor'.");
	}

	@Test
	@DisplayName("Deve retornar o valor correto ao converter de banco de dados no Wrapper")
	void testWrapperFromDatabase() {
		ManagerField.Wrapper wrapper = new ManagerField.Wrapper();
		String databaseValue = "valorDoBanco";

		String result = wrapper.fromDatabase(databaseValue);

		assertEquals(databaseValue, result, "O valor do banco de dados deve ser retornado corretamente.");
	}

	@Test
	@DisplayName("Deve retornar o valor correto ao converter para banco de dados no Wrapper")
	void testWrapperToDatabase() {
		ManagerField.Wrapper wrapper = new ManagerField.Wrapper();
		String viewValue = "valorDeVisualizacao";

		String result = wrapper.toDatabase(viewValue);

		assertEquals(viewValue, result, "O valor de visualização deve ser retornado corretamente.");
	}
}
