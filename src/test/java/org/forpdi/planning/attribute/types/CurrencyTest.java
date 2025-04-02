package org.forpdi.planning.attribute.types;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.UnexpectedTypeException;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyTest {

	private Currency currency;

	@BeforeEach
	void setUp() {
		currency = new Currency();
	}

	@Test
	@DisplayName("Deve retornar o nome correto do widget")
	void testGetWidget() {
		assertEquals("Currency", currency.getWidget());
	}

	@Test
	@DisplayName("Deve retornar o Wrapper correto")
	void testGetWrapper() {
		assertTrue(currency.getWrapper() instanceof Currency.Wrapper);
	}

	@Test
	@DisplayName("Deve retornar o nome de exibição correto")
	void testGetDisplayName() {
		assertEquals("Monetário", currency.getDisplayName());
	}

	@Test
	@DisplayName("Deve converter corretamente valor numérico de banco para String")
	void testFromDatabaseNumerical() {
		Double databaseValue = 1234.56;

		String formattedValue = currency.getWrapper().fromDatabaseNumerical(databaseValue);

		assertEquals("1.234,56", formattedValue);
	}

	@Test
	@DisplayName("Deve lançar exceção quando tentar converter valor de String para Double com formato inválido")
	void testToDatabaseNumericalWithInvalidFormat() {
		String invalidValue = "abc";

		assertThrows(UnexpectedTypeException.class, () -> currency.getWrapper().toDatabaseNumerical(invalidValue));
	}

	@Test
	@DisplayName("Deve converter corretamente valor String para Double")
	void testToDatabaseNumerical() {
		String validValue = "1.234,56";

		Double numericValue = currency.getWrapper().toDatabaseNumerical(validValue);

		assertEquals(1234.56, numericValue);
	}
}
