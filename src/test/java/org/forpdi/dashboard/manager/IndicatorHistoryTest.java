package org.forpdi.dashboard.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndicatorHistoryTest {

	private IndicatorHistory indicatorHistory1;
	private IndicatorHistory indicatorHistory2;
	private IndicatorHistory indicatorHistory3;

	@BeforeEach
	void setUp() {
		indicatorHistory1 = new IndicatorHistory();
		indicatorHistory1.setPeriod("2024");
		indicatorHistory1.setValue(100.0);

		indicatorHistory2 = new IndicatorHistory();
		indicatorHistory2.setPeriod("2023");
		indicatorHistory2.setValue(200.0);

		indicatorHistory3 = new IndicatorHistory();
		indicatorHistory3.setPeriod("2024");
		indicatorHistory3.setValue(150.0);
	}

	@Test
	@DisplayName("Verificar a comparação de IndicatorHistory com o mesmo período")
	void testCompareTo_mesmoPeriodo() {
		int result = indicatorHistory1.compareTo(indicatorHistory3);

		assertEquals(0, result, "A comparação de indicadores com o mesmo período deve retornar 0");
	}

	@Test
	@DisplayName("Verificar a comparação de IndicatorHistory com período diferente")
	void testCompareTo_periodosDiferentes() {
		int result = indicatorHistory1.compareTo(indicatorHistory2);

		assertEquals(1, result, "O indicador com período maior deve vir primeiro");
	}

	@Test
	@DisplayName("Verificar a comparação de IndicatorHistory com valor numérico do período")
	void testCompareTo_periodoNumerico() {
		indicatorHistory1.setPeriod("2024");
		indicatorHistory2.setPeriod("2023");

		int result = indicatorHistory1.compareTo(indicatorHistory2);

		assertEquals(1, result, "O indicador com o período maior deve vir primeiro");
	}

	@Test
	@DisplayName("Verificar a comparação de IndicatorHistory com valor de período invertido")
	void testCompareTo_periodoInvertido() {
		indicatorHistory1.setPeriod("2023");
		indicatorHistory2.setPeriod("2024");

		int result = indicatorHistory1.compareTo(indicatorHistory2);

		assertEquals(-1, result, "O indicador com o período menor deve vir segundo");
	}
}
