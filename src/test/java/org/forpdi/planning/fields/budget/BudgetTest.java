package org.forpdi.planning.fields.budget;

import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BudgetTest {
	@DisplayName("Budget Validação do objeto por meio de Setters e Getters.")
	@Test
	void testBudgetObjectValidation() {
		Budget budget = new Budget();

		String subAction = "SubAction Example";
		String name = "Budget Name";
		Double committed = 1500.75;
		Double realized = 1200.50;
		Date creation = new Date();
		BudgetElement budgetElement = new BudgetElement();
		StructureLevelInstance levelInstance = new StructureLevelInstance();
		long exportBudgetElementId = 101L;
		long exportStructureLevelInstanceId = 202L;
		levelInstance.setId(exportStructureLevelInstanceId);
		Long exportPlanId = 303L;

		budget.setSubAction(subAction);
		budget.setName(name);
		budget.setCommitted(committed);
		budget.setRealized(realized);
		budget.setCreation(creation);
		budget.setBudgetElement(budgetElement);
		budget.setLevelInstance(levelInstance);
		budget.setExportBudgetElementId(exportBudgetElementId);
		budget.setExportStructureLevelInstanceId(exportStructureLevelInstanceId);
		budget.setExportPlanId(exportPlanId);

		assertEquals(subAction, budget.getSubAction(), "As sub-ações deveriam ser iguais.");
		assertEquals(name, budget.getName(), "Os nomes deveriam corresponder.");
		assertEquals(committed, budget.getCommitted(), "Os valores empenhados deveriam ser iguais.");
		assertEquals(realized, budget.getRealized(), "Os valores utilizados deveriam ser iguais.");
		assertEquals(creation, budget.getCreation(), "A data de criação deveria corresponder.");
		assertEquals(budgetElement, budget.getBudgetElement(), "O budget deveria ser igual.");
		assertEquals(levelInstance, budget.getLevelInstance(), "Os níveis da instância deveriam ser iguais.");
		assertEquals(exportBudgetElementId, budget.getExportBudgetElementId(), "Os id's deveriam ser iguais.");
		assertEquals(exportStructureLevelInstanceId, budget.getExportStructureLevelInstanceId(), "Os id's do" +
			"nível da instância deveriam ser iguais");
		assertEquals(exportPlanId, budget.getExportPlanId(), "Os id's dos planos deveriam ser iguais.");
	}
}