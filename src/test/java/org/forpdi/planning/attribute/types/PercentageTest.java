package org.forpdi.planning.attribute.types;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.UnexpectedTypeException;

import static org.junit.jupiter.api.Assertions.*;

class PercentageTest {

	private Percentage percentage;

	@BeforeEach
	void setUp() {
		percentage = new Percentage();
	}

	@Test
	@DisplayName("Deve retornar o nome correto do widget")
	void testGetWidget() {
		assertEquals("Percentage", percentage.getWidget());
	}

	@Test
	@DisplayName("Deve retornar o Wrapper correto")
	void testGetWrapper() {
		assertTrue(percentage.getWrapper() instanceof Percentage.Wrapper);
	}

	@Test
	@DisplayName("Deve retornar o nome de exibição correto")
	void testGetDisplayName() {
		assertEquals("Porcentagem", percentage.getDisplayName());
	}

	@Test
	@DisplayName("Deve converter corretamente valor numérico de banco para String")
	void testFromDatabaseNumerical() {
		Double databaseValue = 45.67;

		String formattedValue = percentage.getWrapper().fromDatabaseNumerical(databaseValue);

		assertEquals("45,67", formattedValue);
	}

	@Test
	@DisplayName("Deve lançar exceção quando tentar converter valor de String para Double com formato inválido")
	void testToDatabaseNumericalWithInvalidFormat() {
		String invalidValue = "abc";

		assertThrows(UnexpectedTypeException.class, () -> percentage.getWrapper().toDatabaseNumerical(invalidValue));
	}

	@Test
	@DisplayName("Deve converter corretamente valor String para Double")
	void testToDatabaseNumerical() {
		String validValue = "45,67%";

		Double numericValue = percentage.getWrapper().toDatabaseNumerical(validValue);

		assertEquals(45.67, numericValue);
	}
}
