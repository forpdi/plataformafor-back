package org.forrisco.core.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PlanRiskSubItemFieldTest {

	@DisplayName("PlanRiskSubItemField Validação da Classe por meio dos Getters and Setters.")
	@Test
	public void testGettersAndSetters() {
		PlanRiskSubItemField planRiskSubItemField = new PlanRiskSubItemField();
		PlanRiskSubItem mockPlanRiskSubItem = mock(PlanRiskSubItem.class);

		planRiskSubItemField.setPlanRiskSubItem(mockPlanRiskSubItem);
		planRiskSubItemField.setName("Field Name");
		planRiskSubItemField.setDescription("One description");
		planRiskSubItemField.setText(true);
		planRiskSubItemField.setFileLink("http://filelink.com");
		planRiskSubItemField.setValue("Some Value");

		assertEquals(mockPlanRiskSubItem, planRiskSubItemField.getPlanRiskSubItem());
		assertEquals("Field Name", planRiskSubItemField.getName());
		assertEquals("One description", planRiskSubItemField.getDescription());
		assertTrue(planRiskSubItemField.isText());
		assertEquals("http://filelink.com", planRiskSubItemField.getFileLink());
		assertEquals("Some Value", planRiskSubItemField.getValue());
	}
}