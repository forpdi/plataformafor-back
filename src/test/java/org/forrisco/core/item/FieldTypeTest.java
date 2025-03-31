package org.forrisco.core.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldTypeTest {

	@DisplayName("FieldType Criação da Enum NO_TYPE.")
	@Test
	public void testFieldTypeNO_TYPECreation() {
		FieldType noType = FieldType.NO_TYPE;

		int consultedValue = noType.getValue();
		String consultedName = noType.getName();

		assertEquals(0, consultedValue);
		assertEquals("SEM INFORMAÇÃO", consultedName);
	}

	@DisplayName("FieldType Criação da Enum FILE.")
	@Test
	public void testFieldTypeFILECreation() {
		FieldType file = FieldType.FILE;

		int consultedValue = file.getValue();
		String consultedName = file.getName();

		assertEquals(1, consultedValue);
		assertEquals("ARQUIVO", consultedName);
	}

	@DisplayName("FieldType Criação da Enum TEXT.")
	@Test
	public void testFieldTypeTEXTCreation() {
		FieldType text = FieldType.TEXT;

		int consultedValue = text.getValue();
		String consultedName = text.getName();

		assertEquals(2, consultedValue);
		assertEquals("TEXTO", consultedName);
	}

	@DisplayName("FieldType Obtendo a Enum pelo seu nome. Caso sem exceção.")
	@Test
	public void testFielTypeGetPeriodicityByNameValidCase() {
		String consultName = "ARQUIVO";

		FieldType returnedEnum = FieldType.getPeriodicityByName(consultName);

		assertNotNull(returnedEnum, "A Enum retornada não deveria ser nula.");
		assertEquals(FieldType.FILE, returnedEnum, "As enum deveriam ser correspondentes.");
	}

	@DisplayName("FieldType Obtendo a Enum pelo seu nome. Caso que gera uma exceção.")
	@Test
	public void testFielTypeGetPeriodicityByNameInvalidCase() {
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> FieldType.getPeriodicityByName("INVALID_NAME")
		);
		assertEquals("Invalid file type name: INVALID_NAME", exception.getMessage());
	}
}