package org.forpdi.planning.fields;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class OptionsFieldTest {

	@Test
	@DisplayName("Deve definir e obter o rótulo corretamente")
	void testGetAndSetLabel() {
		OptionsField optionsField = new OptionsField();
		String label = "Opção 1";

		optionsField.setLabel(label);

		assertEquals(label, optionsField.getLabel());
	}

	@Test
	@DisplayName("Deve definir e obter a data de criação corretamente")
	void testGetAndSetCreation() {
		OptionsField optionsField = new OptionsField();
		Date creationDate = new Date();

		optionsField.setCreation(creationDate);

		assertEquals(creationDate, optionsField.getCreation());
	}

	@Test
	@DisplayName("Deve definir e obter o ID do atributo corretamente")
	void testGetAndSetAttributeId() {
		OptionsField optionsField = new OptionsField();
		Long attributeId = 1L;

		optionsField.setAttributeId(attributeId);

		assertEquals(attributeId, optionsField.getAttributeId());
	}

	@Test
	@DisplayName("Deve definir e obter o ID da coluna corretamente")
	void testGetAndSetColumnId() {
		OptionsField optionsField = new OptionsField();
		Long columnId = 10L;

		optionsField.setColumnId(columnId);

		assertEquals(columnId, optionsField.getColumnId());
	}

	@Test
	@DisplayName("Deve definir e verificar se é documento corretamente")
	void testGetAndSetIsDocument() {
		OptionsField optionsField = new OptionsField();
		boolean isDocument = true;

		optionsField.setDocument(isDocument);

		assertTrue(optionsField.isDocument());

		optionsField.setDocument(false);

		assertFalse(optionsField.isDocument());
	}

	@Test
	@DisplayName("Deve inicializar a data de criação corretamente")
	void testDefaultCreationDate() {
		OptionsField optionsField = new OptionsField();

		Date creationDate = optionsField.getCreation();

		assertNotNull(creationDate);
	}
}
