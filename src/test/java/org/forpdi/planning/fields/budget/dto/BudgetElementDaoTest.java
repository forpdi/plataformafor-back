package org.forpdi.planning.fields.budget.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BudgetElementDaoTest {

	@DisplayName("BudgetElementDao Criação do objeto pelo construtor completo.")
	@Test
	public void BudgetElementDaoCreation(){

		BudgetElementDao daoCreated =
			new BudgetElementDao(1L, "Budget", "Str", 3L);

		assertEquals(1L, daoCreated.idBudgetElement(),
			"O valor do Dao id não corresponde ao esperado.");
		assertEquals("Budget", daoCreated.subAction(),
			"O texto de subAction não corresponde ao esperado.");
		assertEquals("Str", daoCreated.budgetLoa(),
			"O valor de budgetLoa não corresponde ao esperado.");
		assertEquals(3L, daoCreated.companyId(),
			"O valor de company_id do Dao não corresponde ao esperado.");
	}
}