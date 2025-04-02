package org.forpdi.planning.attribute.types;

import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BudgetFieldTest {

	@Test
	@DisplayName("Deve retornar o nome do widget correto")
	void testGetWidget() {
		BudgetField budgetField = new BudgetField();

		String result = budgetField.getWidget();

		assertEquals("BudgetField", result, "O nome do widget deve ser 'BudgetField'.");
	}

	@Test
	@DisplayName("Deve retornar o wrapper correto")
	void testGetWrapper() {
		BudgetField budgetField = new BudgetField();

		AttributeTypeWrapper result = budgetField.getWrapper();

		assertNotNull(result, "O wrapper não deve ser nulo.");
		assertTrue(result instanceof BudgetField.Wrapper, "O wrapper deve ser uma instância de BudgetField.Wrapper.");
	}

	@Test
	@DisplayName("Deve retornar o nome de exibição correto")
	void testGetDisplayName() {
		BudgetField budgetField = new BudgetField();

		String result = budgetField.getDisplayName();

		assertEquals("Orçamento", result, "O nome de exibição deve ser 'Orçamento'.");
	}

	@Test
	@DisplayName("Deve retornar null ao converter de banco de dados no Wrapper")
	void testWrapperFromDatabase() {
		BudgetField.Wrapper wrapper = new BudgetField.Wrapper();
		String databaseValue = "1000";

		String result = wrapper.fromDatabase(databaseValue);

		assertNull(result, "O valor retornado ao converter do banco de dados deve ser null.");
	}

	@Test
	@DisplayName("Deve retornar null ao converter para banco de dados no Wrapper")
	void testWrapperToDatabase() {
		BudgetField.Wrapper wrapper = new BudgetField.Wrapper();
		String viewValue = "1500";

		String result = wrapper.toDatabase(viewValue);

		assertNull(result, "O valor retornado ao converter para o banco de dados deve ser null.");
	}
}
