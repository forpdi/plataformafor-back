package org.forpdi.planning.fields.table;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableFieldsTest {

	@Test
	@DisplayName("Deve configurar e retornar a lista de TableStructures corretamente")
	void givenTableFields_whenSetTableStructures_thenShouldReturnUpdatedList() {
		TableFields tableFields = new TableFields();
		List<TableStructure> structures = Arrays.asList(new TableStructure(), new TableStructure());

		tableFields.setTableStructures(structures);

		assertNotNull(tableFields.getTableStructures(), "A lista de TableStructures não deve ser nula.");
		assertEquals(structures, tableFields.getTableStructures(), "A lista de TableStructures não foi configurada corretamente.");
	}

	@Test
	@DisplayName("Deve configurar e retornar a lista de TableInstances corretamente")
	void givenTableFields_whenSetTableInstances_thenShouldReturnUpdatedList() {
		TableFields tableFields = new TableFields();
		List<TableInstance> instances = Arrays.asList(new TableInstance(), new TableInstance());

		tableFields.setTableInstances(instances);

		assertNotNull(tableFields.getTableInstances(), "A lista de TableInstances não deve ser nula.");
		assertEquals(instances, tableFields.getTableInstances(), "A lista de TableInstances não foi configurada corretamente.");
	}

	@Test
	@DisplayName("Deve configurar e retornar o atributo AttributeId corretamente")
	void givenTableFields_whenSetAttributeId_thenShouldReturnUpdatedAttributeId() {
		TableFields tableFields = new TableFields();
		Long attributeId = 123L;

		tableFields.setAttributeId(attributeId);

		assertNotNull(tableFields.getAttributeId(), "O AttributeId não deve ser nulo.");
		assertEquals(attributeId, tableFields.getAttributeId(), "O AttributeId não foi configurado corretamente.");
	}

	@Test
	@DisplayName("Deve configurar e retornar o valor do atributo isDocument corretamente")
	void givenTableFields_whenSetIsDocument_thenShouldReturnUpdatedValue() {
		TableFields tableFields = new TableFields();

		tableFields.setIsDocument(true);

		assertTrue(tableFields.isDocument(), "O atributo isDocument não foi configurado corretamente como true.");

		tableFields.setIsDocument(false);

		assertFalse(tableFields.isDocument(), "O atributo isDocument não foi configurado corretamente como false.");
	}
}
