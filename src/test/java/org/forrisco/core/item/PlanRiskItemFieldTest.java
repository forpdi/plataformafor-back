package org.forrisco.core.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PlanRiskItemFieldTest {

	@DisplayName("PlanRiskItemField Validação da Classe por meio dos Getters and Setters.")
	@Test
	public void testPlanRiskItemFieldGettersAndSetters() {

		PlanRiskItem mockPlanRiskItem = mock(PlanRiskItem.class);
		PlanRiskItemField planRiskItemField = new PlanRiskItemField();

		planRiskItemField.setPlanRiskItem(mockPlanRiskItem);
		planRiskItemField.setName("Field Name");
		planRiskItemField.setDescription("This is a description");
		planRiskItemField.setText(true);
		planRiskItemField.setFileLink("http://filelink.com");
		planRiskItemField.setValue("Some Value");

		assertEquals(mockPlanRiskItem, planRiskItemField.getPlanRiskItem());
		assertEquals("Field Name", planRiskItemField.getName());
		assertEquals("This is a description", planRiskItemField.getDescription());
		assertTrue(planRiskItemField.isText());
		assertEquals("http://filelink.com", planRiskItemField.getFileLink());
		assertEquals("Some Value", planRiskItemField.getValue());
	}
}