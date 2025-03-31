package org.forrisco.core.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanRiskSubItemTest {
	@DisplayName("PlanRiskSubItem Criação do objeto e validação dos seus dados.")
	@Test
	void testPlanRiskSubItemObjectCreation() {

		PlanRiskSubItem planRiskSubItem = new PlanRiskSubItem();

		PlanRiskItem testPlanRiskItem = new PlanRiskItem();
		String testName = "PlanRiskSubItem name";
		String testDescription = "Test PlanRiskSubItem Description";
		List<PlanRiskSubItemField> testFields = new ArrayList<>();
		Long testItemId = 123L;
		boolean testHasFile = true;
		boolean testHasText = false;

		planRiskSubItem.setPlanRiskItem(testPlanRiskItem);
		planRiskSubItem.setName(testName);
		planRiskSubItem.setDescription(testDescription);
		planRiskSubItem.setPlanRiskSubItemField(testFields);
		planRiskSubItem.setItemId(testItemId);
		planRiskSubItem.setHasFile(testHasFile);
		planRiskSubItem.setHasText(testHasText);

		assertEquals(testPlanRiskItem, planRiskSubItem.getPlanRiskItem(), "O PlanRiskItem deveria ser igual ao definido");
		assertEquals(testName, planRiskSubItem.getName(), "O nome deveria ser igual ao definido");
		assertEquals(testDescription, planRiskSubItem.getDescription(), "A descrição deveria ser igual à definida");
		assertEquals(testFields, planRiskSubItem.getPlanRiskSubItemField(), "Os campos deveriam ser iguais aos definidos");
		assertEquals(testItemId, planRiskSubItem.getItemId(), "O ID do item deveria ser igual ao definido");
		assertTrue(planRiskSubItem.hasFile(), "O campo hasFile deveria ser verdadeiro");
		assertEquals(testHasText, planRiskSubItem.hasText(), "O campo hasText deveria ser igual ao definido");
	}
}