package org.forpdi.planning.filters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeformanceFilterTypeTest {

	@Test
	@DisplayName("Deve retornar o valor associado ao tipo de filtro")
	void givenEnum_whenGetValue_thenReturnCorrectValue() {
		assertEquals(1, PeformanceFilterType.BELOW_MINIMUM.getValue(), "Valor esperado para BELOW_MINIMUM é 1.");
		assertEquals(2, PeformanceFilterType.BELOW_EXPECTED.getValue(), "Valor esperado para BELOW_EXPECTED é 2.");
		assertEquals(3, PeformanceFilterType.ENOUGH.getValue(), "Valor esperado para ENOUGH é 3.");
		assertEquals(4, PeformanceFilterType.ABOVE_MAXIMUM.getValue(), "Valor esperado para ABOVE_MAXIMUM é 4.");
		assertEquals(5, PeformanceFilterType.NOT_STARTED.getValue(), "Valor esperado para NOT_STARTED é 5.");
	}

	@Test
	@DisplayName("Deve retornar o tipo correto ao passar o valor associado")
	void givenValue_whenValueOf_thenReturnCorrectEnum() {
		assertEquals(PeformanceFilterType.BELOW_MINIMUM, PeformanceFilterType.valueOf(1), "Valor 1 deve corresponder a BELOW_MINIMUM.");
		assertEquals(PeformanceFilterType.BELOW_EXPECTED, PeformanceFilterType.valueOf(2), "Valor 2 deve corresponder a BELOW_EXPECTED.");
		assertEquals(PeformanceFilterType.ENOUGH, PeformanceFilterType.valueOf(3), "Valor 3 deve corresponder a ENOUGH.");
		assertEquals(PeformanceFilterType.ABOVE_MAXIMUM, PeformanceFilterType.valueOf(4), "Valor 4 deve corresponder a ABOVE_MAXIMUM.");
		assertEquals(PeformanceFilterType.NOT_STARTED, PeformanceFilterType.valueOf(5), "Valor 5 deve corresponder a NOT_STARTED.");
	}
	
	@Test
	void test_returns_null_for_input_zero() {
		Integer input = 0;

		PeformanceFilterType result = PeformanceFilterType.valueOf(input);

		assertNull(result);
	}

}
