package org.forrisco.core.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PIDescriptionsTest {

	@DisplayName("PIDescriptions Validação da Classe por meio dos Getters and Setters.")
	@Test
	public void testPIDescriptionsValidadeObjectWithSettersAndGetters() {
		PIDescriptions piDescriptions = new PIDescriptions();

		PIDescriptions.Description[] idDescriptions = new PIDescriptions.Description[2];
		idDescriptions[0] = new PIDescriptions.Description("desc1", "val1");
		idDescriptions[1] = new PIDescriptions.Description("desc2", "val2");

		PIDescriptions.Description[] pdDescriptions = new PIDescriptions.Description[1];
		pdDescriptions[0] = new PIDescriptions.Description("desc1", "val1");

		piDescriptions.setIdescriptions(idDescriptions);
		piDescriptions.setPdescriptions(pdDescriptions);


		PIDescriptions.Description[] idResult = piDescriptions.getIdescriptions();
		PIDescriptions.Description[] pdResult = piDescriptions.getPdescriptions();

		assertEquals(2, idResult.length);
		assertEquals(1, pdResult.length);
		assertArrayEquals(idDescriptions, idResult);
		assertArrayEquals(pdDescriptions, pdResult);

	}

	@DisplayName("PIDescriptions Criação Description e uso dos Getters e Setters.")
	@Test
	void testDescriptionStaticCreationObjectAndUsingSetterAndGetter() {
		PIDescriptions.Description createdDescription =
			new PIDescriptions.Description("Description", "Value");

		createdDescription.setDescription("New description");
		createdDescription.setValue("New value");

		assertNotEquals("Description", createdDescription.getDescription(),
			"A descrição foi alterada, os valores deveriam ser diferentes.");
		assertNotEquals("Value", createdDescription.getValue(),
			"O valor foi alterado, os valores deveriam ser diferentes.");

	}
}