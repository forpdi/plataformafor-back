package org.forpdi.planning.fields.budget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BudgetDTOTest {

	BudgetDTO budgetExpected;

	@BeforeEach
	public void setup(){
		budgetExpected = new BudgetDTO();
	}

	@DisplayName("BudgetDTO Criação do objeto pelo construtor vazio.")
	@Test
	public void testBudgetDTOCreationObject(){

		assertNotNull(budgetExpected, "O objeto instânciado é nulo");
	}

	@DisplayName("BudgetDTO Criação do objeto e setando os parâmetros do objeto.")
	@Test
	public void tessBudgetDTOCreationWithParams(){

		budgetExpected.setBudget(new Budget());
		budgetExpected.setBudgetLoa(3D);
		budgetExpected.setBalanceAvailable(4D);

		assertNotNull(budgetExpected.getBudget(), "O objeto instânciado é nulo");
		assertEquals(3D, budgetExpected.getBudgetLoa(),
			"O valor esperado do BudgetLoa é diferente");
		assertEquals(4D, budgetExpected.getBalanceAvailable(),
			"O saldo disponível é diferente do esperado.");

	}
}