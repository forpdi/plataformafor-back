package org.forpdi.planning.fields.budget;

import org.forpdi.core.company.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BudgetElementTest {
	private BudgetElement budgetElement;

	@BeforeEach
	void setUp() {
		budgetElement = new BudgetElement();
	}

	@Test
	void testBudgetElementAttributes() {

		String subAction = "Subação Teste";
		double budgetLoa = 50000.0;
		double balanceAvailable = 25000.0;
		Long linkedObjects = 5L;
		Date creation = new Date();
		Company company = new Company();
		Long exportCompanyId = 100L;

		budgetElement.setSubAction(subAction);
		budgetElement.setBudgetLoa(budgetLoa);
		budgetElement.setBalanceAvailable(balanceAvailable);
		budgetElement.setLinkedObjects(linkedObjects);
		budgetElement.setCreation(creation);
		budgetElement.setCompany(company);
		budgetElement.setExportCompanyId(exportCompanyId);

		assertEquals(subAction, budgetElement.getSubAction(), "O valor de 'subAction' está incorreto.");
		assertEquals(budgetLoa, budgetElement.getBudgetLoa(), "O valor de 'budgetLoa' está incorreto.");
		assertEquals(balanceAvailable, budgetElement.getBalanceAvailable(), "O valor de 'balanceAvailable' está incorreto.");
		assertEquals(linkedObjects, budgetElement.getLinkedObjects(), "O valor de 'linkedObjects' está incorreto.");
		assertEquals(creation, budgetElement.getCreation(), "O valor de 'creation' está incorreto.");
		assertEquals(company, budgetElement.getCompany(), "O valor de 'company' está incorreto.");
		assertEquals(exportCompanyId, budgetElement.getExportCompanyId(), "O valor de 'exportCompanyId' está incorreto.");
	}
}