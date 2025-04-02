package org.forpdi.planning.structure.xml;

import java.util.List;
import org.dom4j.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StructureBeansTest {

	@Test
	@DisplayName("Deve clonar profundamente um objeto Structure")
	void testStructureDeepClone() {
		StructureBeans.Structure original = new StructureBeans.Structure();
		original.name = "Estrutura Teste";
		original.description = "Descrição da Estrutura";
		StructureBeans.StructureLevel level = new StructureBeans.StructureLevel();
		level.name = "Nível Teste";
		original.levels = List.of(level);

		StructureBeans.Structure clone = original.deepClone();

		assertNotNull(clone);
		assertEquals(original.name, clone.name);
		assertEquals(original.description, clone.description);
		assertNotNull(clone.levels);
		assertEquals(original.levels.size(), clone.levels.size());
		assertEquals(original.levels.get(0).name, clone.levels.get(0).name);
		assertNotSame(original, clone);
		assertNotSame(original.levels, clone.levels);
	}

	@Test
	@DisplayName("Deve inicializar os campos de StructureLevel")
	void testStructureLevelInitialization() {
		StructureBeans.StructureLevel level = new StructureBeans.StructureLevel();

		assertNull(level.name);
		assertNull(level.description);
		assertEquals(0, level.sequence);
		assertNull(level.attributes);
		assertFalse(level.leaf);
		assertFalse(level.goal);
		assertFalse(level.indicator);
		assertFalse(level.objective);
		assertNull(level.child);
	}

	@Test
	@DisplayName("Deve inicializar os campos de Attribute")
	void testAttributeInitialization() {
		StructureBeans.Attribute attribute = new StructureBeans.Attribute();

		assertNull(attribute.label);
		assertNull(attribute.description);
		assertNull(attribute.type);
		assertFalse(attribute.periodicity);
		assertNull(attribute.optionsField);
		assertNull(attribute.scheduleValues);
		assertFalse(attribute.required);
		assertFalse(attribute.visibleInTables);
		assertFalse(attribute.finishDate);
		assertFalse(attribute.expectedField);
		assertFalse(attribute.minimumField);
		assertFalse(attribute.maximumField);
		assertFalse(attribute.reachedField);
		assertFalse(attribute.referenceField);
		assertFalse(attribute.justificationField);
		assertFalse(attribute.polarityField);
		assertFalse(attribute.formatField);
		assertFalse(attribute.periodicityField);
		assertFalse(attribute.beginField);
		assertFalse(attribute.endField);
		assertFalse(attribute.bscField);
	}

	@Test
	@DisplayName("Deve inicializar os campos de ScheduleValue")
	void testScheduleValueInitialization() {
		StructureBeans.ScheduleValue scheduleValue = new StructureBeans.ScheduleValue();

		assertNull(scheduleValue.label);
		assertNull(scheduleValue.type);
	}

	@Test
	@DisplayName("Deve inicializar os campos de OptionsField")
	void testOptionsFieldInitialization() {
		StructureBeans.OptionsField optionsField = new StructureBeans.OptionsField();

		assertNull(optionsField.label);
		assertNull(optionsField.attribute);
	}

	@Test
	@DisplayName("Deve configurar e verificar valores em StructureLevel")
	void testStructureLevelSetAndGet() {
		StructureBeans.StructureLevel level = new StructureBeans.StructureLevel();
		Element mockElement = mock(Element.class);
		level.name = "Nível Teste";
		level.description = "Descrição Teste";
		level.sequence = 1;
		level.leaf = true;
		level.goal = true;
		level.indicator = false;
		level.objective = true;
		level.child = mockElement;

		assertEquals("Nível Teste", level.name);
		assertEquals("Descrição Teste", level.description);
		assertEquals(1, level.sequence);
		assertTrue(level.leaf);
		assertTrue(level.goal);
		assertFalse(level.indicator);
		assertTrue(level.objective);
		assertEquals(mockElement, level.child);
	}

	@Test
	@DisplayName("Deve configurar e verificar valores em Attribute")
	void testAttributeSetAndGet() {
		StructureBeans.Attribute attribute = new StructureBeans.Attribute();
		StructureBeans.OptionsField option = new StructureBeans.OptionsField();
		StructureBeans.ScheduleValue scheduleValue = new StructureBeans.ScheduleValue();
		attribute.label = "Atributo Teste";
		attribute.type = "String";
		attribute.optionsField = List.of(option);
		attribute.scheduleValues = List.of(scheduleValue);
		attribute.required = true;

		assertEquals("Atributo Teste", attribute.label);
		assertEquals("String", attribute.type);
		assertNotNull(attribute.optionsField);
		assertNotNull(attribute.scheduleValues);
		assertEquals(1, attribute.optionsField.size());
		assertEquals(1, attribute.scheduleValues.size());
		assertTrue(attribute.required);
	}
}
