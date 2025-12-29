package org.forpdi.planning.attribute;

import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttributeInstanceTest {
	@Test
	@DisplayName("Deve validar a criação de um objeto AttributeInstance, " +
		"definindo seus parâmetros pelos setters e acessando pelos getters.")
	void testGettersAndSetters() {
		AttributeInstance attributeInstance = new AttributeInstance();

		String value = "Test Value";
		Double valueAsNumber = 123.45;
		Date valueAsDate = new Date();
		Date creation = new Date(System.currentTimeMillis() - 86400000);
		String formattedValue = "Formatted Test Value";
		Long exportAttributeId = 1L;
		Long exportStructureLevelInstanceId = 2L;

		Attribute attribute = mock(Attribute.class);
		StructureLevelInstance levelInstance = mock(StructureLevelInstance.class);

		when(attribute.getId()).thenReturn(10L);
		when(levelInstance.getName()).thenReturn("Test Level Instance");

		attributeInstance.setValue(value);
		attributeInstance.setValueAsNumber(valueAsNumber);
		attributeInstance.setValueAsDate(valueAsDate);
		attributeInstance.setCreation(creation);
		attributeInstance.setFormattedValue(formattedValue);
		attributeInstance.setExportAttributeId(exportAttributeId);
		attributeInstance.setExportStructureLevelInstanceId(exportStructureLevelInstanceId);
		attributeInstance.setAttribute(attribute);
		attributeInstance.setLevelInstance(levelInstance);

		assertEquals(value, attributeInstance.getValue());
		assertEquals(valueAsNumber, attributeInstance.getValueAsNumber());
		assertEquals(valueAsDate, attributeInstance.getValueAsDate());
		assertEquals(creation, attributeInstance.getCreation());
		assertEquals(formattedValue, attributeInstance.getFormattedValue());
		assertEquals(exportAttributeId, attributeInstance.getExportAttributeId());
		assertEquals(exportStructureLevelInstanceId, attributeInstance.getExportStructureLevelInstanceId());
		assertEquals(attribute, attributeInstance.getAttribute());
		assertEquals(levelInstance, attributeInstance.getLevelInstance());
	}

	@Test
	@DisplayName("Deve retornar o objeto de acordo como definido no método toString.")
	void testToString() {
		AttributeInstance attributeInstance = new AttributeInstance();

		Attribute attribute = mock(Attribute.class);
		StructureLevelInstance levelInstance = mock(StructureLevelInstance.class);

		when(attribute.getId()).thenReturn(10L);
		when(levelInstance.getName()).thenReturn("Test Level Instance");

		attributeInstance.setValue("Test Value");
		attributeInstance.setValueAsNumber(123.45);
		attributeInstance.setValueAsDate(new Date());
		attributeInstance.setCreation(new Date());
		attributeInstance.setAttribute(attribute);
		attributeInstance.setLevelInstance(levelInstance);
		attributeInstance.setFormattedValue("Formatted Value");

		String toStringResult = attributeInstance.toString();
		assertTrue(toStringResult.contains("value=Test Value"));
		assertTrue(toStringResult.contains("valueAsNumber=123.45"));
		assertTrue(toStringResult.contains("formattedValue=Formatted Value"));
		assertTrue(toStringResult.contains("attribute=10"));
		assertTrue(toStringResult.contains("levelInstance=Test Level Instance"));
	}
}