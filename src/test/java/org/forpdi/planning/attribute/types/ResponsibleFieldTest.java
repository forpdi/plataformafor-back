package org.forpdi.planning.attribute.types;

import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponsibleFieldTest {

	@Test
	@DisplayName("Deve retornar o nome do widget correto")
	void testGetWidget() {
		ResponsibleField responsibleField = new ResponsibleField();

		String result = responsibleField.getWidget();

		assertEquals("ResponsibleField", result, "O nome do widget deve ser 'ResponsibleField'.");
	}

	@Test
	@DisplayName("Deve retornar o wrapper correto")
	void testGetWrapper() {
		ResponsibleField responsibleField = new ResponsibleField();

		AttributeTypeWrapper result = responsibleField.getWrapper();

		assertNotNull(result, "O wrapper não deve ser nulo.");
		assertTrue(result instanceof ResponsibleField.Wrapper, "O wrapper deve ser uma instância de ResponsibleField.Wrapper.");
	}

	@Test
	@DisplayName("Deve retornar o nome de exibição correto")
	void testGetDisplayName() {
		ResponsibleField responsibleField = new ResponsibleField();

		String result = responsibleField.getDisplayName();

		assertEquals("Campo de seleção do usuário", result, "O nome de exibição deve ser 'Campo de seleção do usuário'.");
	}

	@Test
	@DisplayName("Deve retornar o valor correto ao converter de banco de dados no Wrapper")
	void testWrapperFromDatabase() {
		ResponsibleField.Wrapper wrapper = new ResponsibleField.Wrapper();
		String databaseValue = "valorDoBanco";

		String result = wrapper.fromDatabase(databaseValue);

		assertEquals(databaseValue, result, "O valor do banco de dados deve ser retornado corretamente.");
	}

	@Test
	@DisplayName("Deve retornar o valor correto ao converter para banco de dados no Wrapper")
	void testWrapperToDatabase() {
		ResponsibleField.Wrapper wrapper = new ResponsibleField.Wrapper();
		String viewValue = "valorDeVisualizacao";

		String result = wrapper.toDatabase(viewValue);

		assertEquals(viewValue, result, "O valor de visualização deve ser retornado corretamente.");
	}
}
