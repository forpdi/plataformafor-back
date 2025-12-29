package org.forpdi.planning.structure;

import org.forpdi.planning.attribute.AttributeInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolarityTest {

	@Test
	@DisplayName("Dado que a polaridade é 'BIGGER_BETTER', quando obter o valor, então deve retornar 'Maior-melhor'")
	void testGetValueBiggerBetter() {
		Polarity polarity = Polarity.BIGGER_BETTER;

		String value = polarity.getValue();

		assertEquals("Maior-melhor", value, "O valor da polaridade 'BIGGER_BETTER' deve ser 'Maior-melhor'.");
	}

	@Test
	@DisplayName("Dado que a polaridade é 'SMALLER_BETTER', quando obter o valor, então deve retornar 'Menor-melhor'")
	void testGetValueSmallerBetter() {
		Polarity polarity = Polarity.SMALLER_BETTER;

		String value = polarity.getValue();

		assertEquals("Menor-melhor", value, "O valor da polaridade 'SMALLER_BETTER' deve ser 'Menor-melhor'.");
	}

	@Test
	@DisplayName("Dado que a polaridade é 'BIGGER_BETTER', quando obter o operador, então deve retornar '>'")
	void testGetOperatorBiggerBetter() {
		Polarity polarity = Polarity.BIGGER_BETTER;

		String operator = polarity.getOperator();

		assertEquals(">", operator, "O operador da polaridade 'BIGGER_BETTER' deve ser '>'.");
	}

	@Test
	@DisplayName("Dado que a polaridade é 'SMALLER_BETTER', quando obter o operador, então deve retornar '<'")
	void testGetOperatorSmallerBetter() {
		Polarity polarity = Polarity.SMALLER_BETTER;

		String operator = polarity.getOperator();

		assertEquals("<", operator, "O operador da polaridade 'SMALLER_BETTER' deve ser '<'.");
	}

	@Test
	@DisplayName("Dado que a polaridade é 'BIGGER_BETTER', quando obter o operador inverso, então deve retornar '<'")
	void testGetReverseOperatorBiggerBetter() {
		Polarity polarity = Polarity.BIGGER_BETTER;

		String reverseOperator = polarity.getReverseOperator();

		assertEquals("<", reverseOperator, "O operador inverso da polaridade 'BIGGER_BETTER' deve ser '<'.");
	}

	@Test
	@DisplayName("Dado que a polaridade é 'SMALLER_BETTER', quando obter o operador inverso, então deve retornar '>'")
	void testGetReverseOperatorSmallerBetter() {
		Polarity polarity = Polarity.SMALLER_BETTER;

		String reverseOperator = polarity.getReverseOperator();

		assertEquals(">", reverseOperator, "O operador inverso da polaridade 'SMALLER_BETTER' deve ser '>'.");
	}

	@Test
	@DisplayName("Dado o valor 'Maior-melhor', quando buscar a polaridade, então deve retornar 'BIGGER_BETTER'")
	void testGetByValueBiggerBetter() {
		String value = "Maior-melhor";

		Polarity polarity = Polarity.getByValue(value);

		assertEquals(Polarity.BIGGER_BETTER, polarity, "A polaridade para 'Maior-melhor' deve ser 'BIGGER_BETTER'.");
	}

	@Test
	@DisplayName("Dado o valor 'Menor-melhor', quando buscar a polaridade, então deve retornar 'SMALLER_BETTER'")
	void testGetByValueSmallerBetter() {
		String value = "Menor-melhor";

		Polarity polarity = Polarity.getByValue(value);

		assertEquals(Polarity.SMALLER_BETTER, polarity, "A polaridade para 'Menor-melhor' deve ser 'SMALLER_BETTER'.");
	}

	@Test
	@DisplayName("Dado um valor inválido, quando buscar a polaridade, então deve lançar IllegalArgumentException")
	void testGetByValueInvalid() {
		String invalidValue = "Valor Inválido";

		assertThrows(IllegalArgumentException.class, () -> {
			Polarity.getByValue(invalidValue);
		}, "Deve lançar uma exceção ao passar um valor inválido.");
	}

	@Test
	@DisplayName("Dado a polaridade 'BIGGER_BETTER', quando comparar valores, então deve retornar true se x > y")
	void testPolarityComparisonBiggerBetter() {
		AttributeInstance polarity = new AttributeInstance();
		polarity.setValue("Maior-melhor");
		Double x = 10.0;
		Double y = 5.0;

		boolean result = Polarity.polarityComparison(polarity, x, y);

		assertTrue(result, "Para 'BIGGER_BETTER', x deve ser maior que y.");
	}

	@Test
	@DisplayName("Dado a polaridade 'SMALLER_BETTER', quando comparar valores, então deve retornar true se x < y")
	void testPolarityComparisonSmallerBetter() {
		AttributeInstance polarity = new AttributeInstance();
		polarity.setValue("Menor-melhor");
		Double x = 5.0;
		Double y = 10.0;

		boolean result = Polarity.polarityComparison(polarity, x, y);

		assertTrue(result, "Para 'SMALLER_BETTER', x deve ser menor que y.");
	}

	@Test
	@DisplayName("Dado que os valores x ou y são nulos, quando comparar a polaridade, então deve retornar false")
	void testPolarityComparisonNullValues() {
		AttributeInstance polarity = new AttributeInstance();
		polarity.setValue("Maior-melhor");
		Double x = null;
		Double y = 10.0;

		boolean result = Polarity.polarityComparison(polarity, x, y);

		assertFalse(result, "Se x ou y forem nulos, a comparação deve retornar false.");
	}

	@Test
	@DisplayName("Dado que a polaridade é inválida, quando comparar valores, então deve lançar IllegalArgumentException")
	void testPolarityComparisonInvalidPolarity() {
		AttributeInstance polarity = new AttributeInstance();
		polarity.setValue("Valor Inválido");
		Double x = 10.0;
		Double y = 5.0;

		assertThrows(IllegalArgumentException.class, () -> {
			Polarity.polarityComparison(polarity, x, y);
		}, "Deve lançar exceção para polaridade inválida.");
	}
}
