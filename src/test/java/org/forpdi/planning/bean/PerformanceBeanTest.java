package org.forpdi.planning.bean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceBeanTest {

	private PerformanceBean performanceBean;

	@BeforeEach
	void setUp() {
		performanceBean = new PerformanceBean(85.5, 80.0, 90.0);
	}

	@Test
	@DisplayName("Dado um PerformanceBean com dados definidos, quando o método getPerformance é chamado, então o valor de performance é retornado corretamente")
	void testGetPerformance() {
		Double performance = performanceBean.getPerformance();

		assertEquals(85.5, performance, "O valor de performance deve ser 85.5");
	}

	@Test
	@DisplayName("Dado um PerformanceBean com dados definidos, quando o método getMinimumAverage é chamado, então o valor mínimo médio é retornado corretamente")
	void testGetMinimumAverage() {
		Double minimumAverage = performanceBean.getMinimumAverage();

		assertEquals(80.0, minimumAverage, "O valor mínimo médio deve ser 80.0");
	}

	@Test
	@DisplayName("Dado um PerformanceBean com dados definidos, quando o método getMaximumAverage é chamado, então o valor máximo médio é retornado corretamente")
	void testGetMaximumAverage() {
		Double maximumAverage = performanceBean.getMaximumAverage();

		assertEquals(90.0, maximumAverage, "O valor máximo médio deve ser 90.0");
	}

	@Test
	@DisplayName("Dado um PerformanceBean, quando o método setPerformance é chamado, então o valor de performance é atualizado corretamente")
	void testSetPerformance() {
		performanceBean.setPerformance(92.0);

		assertEquals(92.0, performanceBean.getPerformance(), "O valor de performance deve ser 92.0 após a atualização");
	}

	@Test
	@DisplayName("Dado um PerformanceBean, quando o método setMinimumAverage é chamado, então o valor mínimo médio é atualizado corretamente")
	void testSetMinimumAverage() {
		performanceBean.setMinimumAverage(75.0);

		assertEquals(75.0, performanceBean.getMinimumAverage(), "O valor mínimo médio deve ser 75.0 após a atualização");
	}

	@Test
	@DisplayName("Dado um PerformanceBean, quando o método setMaximumAverage é chamado, então o valor máximo médio é atualizado corretamente")
	void testSetMaximumAverage() {
		performanceBean.setMaximumAverage(95.0);

		assertEquals(95.0, performanceBean.getMaximumAverage(), "O valor máximo médio deve ser 95.0 após a atualização");
	}

	@Test
	@DisplayName("Dado um PerformanceBean sem dados definidos, quando o PerformanceBean for instanciado, então os valores devem ser nulos")
	void testDefaultConstructor() {
		PerformanceBean emptyPerformanceBean = new PerformanceBean();

		Double performance = emptyPerformanceBean.getPerformance();
		Double minimumAverage = emptyPerformanceBean.getMinimumAverage();
		Double maximumAverage = emptyPerformanceBean.getMaximumAverage();

		assertNull(performance, "O valor de performance deve ser nulo");
		assertNull(minimumAverage, "O valor mínimo médio deve ser nulo");
		assertNull(maximumAverage, "O valor máximo médio deve ser nulo");
	}
}
