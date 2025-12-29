package org.forrisco.core.item;

import org.forrisco.core.plan.PlanRisk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanRiskItemTest {

	@DisplayName("PlanRiskItem Criação do objeto e validação dos seus dados.")
	@Test
	void testPlanRiskItemObjectCreation() {

		PlanRiskItem planRiskItem = new PlanRiskItem();

		String testName = "PlanRiskItem name";
		String testDescription = "Test Description";
		PlanRisk testPlanRisk = new PlanRisk();
		List<PlanRiskItemField> testFields = new ArrayList<>();
		List<PlanRiskSubItem> testSubitems = new ArrayList<>();
		boolean testHasFile = true;
		boolean testHasText = false;

		planRiskItem.setName(testName);
		planRiskItem.setDescription(testDescription);
		planRiskItem.setPlanRisk(testPlanRisk);
		planRiskItem.setPlanRiskItemField(testFields);
		planRiskItem.setSubitems(testSubitems);
		planRiskItem.setHasFile(testHasFile);
		planRiskItem.setHasText(testHasText);

		assertEquals(testName, planRiskItem.getName(), "O nome deveria ser igual ao definido");
		assertEquals(testDescription, planRiskItem.getDescription(), "A descrição deveria ser igual à definida");
		assertEquals(testPlanRisk, planRiskItem.getPlanRisk(), "O PlanRisk deveria ser igual ao definido");
		assertEquals(testFields, planRiskItem.getPlanRiskItemField(), "Os campos deveriam ser iguais aos definidos");
		assertEquals(testSubitems, planRiskItem.getSubitems(), "Os subitens deveriam ser iguais aos definidos");
		assertTrue(planRiskItem.hasFile(), "O campo hasFile deveria ser verdadeiro");
		assertEquals(testHasText, planRiskItem.hasText(), "O campo hasText deveria ser igual ao definido");
	}

}