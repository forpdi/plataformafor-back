package org.forpdi.planning.plan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlanDetailedTest {

	@Test
	@DisplayName("Dado uma instância de PlanDetailed com dados válidos, quando os getters são chamados, então os valores corretos são retornados")
	void testGetters_ValidPlanDetailed() {
		Plan plan = new Plan();

		PlanDetailed planDetailed = new PlanDetailed();
		planDetailed.setMonth(5);
		planDetailed.setYear(2024);
		planDetailed.setPlan(plan);
		planDetailed.setPerformance(85.5);
		planDetailed.setMinimumAverage(60.0);
		planDetailed.setMaximumAverage(100.0);
		planDetailed.setExportPlanId(123L);

		int month = planDetailed.getMonth();
		int year = planDetailed.getYear();
		Plan retrievedPlan = planDetailed.getPlan();
		Double performance = planDetailed.getPerformance();
		Double minimumAverage = planDetailed.getMinimumAverage();
		Double maximumAverage = planDetailed.getMaximumAverage();
		Long exportPlanId = planDetailed.getExportPlanId();

		assertEquals(5, month, "O mês deve ser 5.");
		assertEquals(2024, year, "O ano deve ser 2024.");
		assertEquals(plan, retrievedPlan, "O plano deve ser o mesmo.");
		assertEquals(85.5, performance, "O desempenho deve ser 85.5.");
		assertEquals(60.0, minimumAverage, "A média mínima deve ser 60.0.");
		assertEquals(100.0, maximumAverage, "A média máxima deve ser 100.0.");
		assertEquals(123L, exportPlanId, "O ID de exportação do plano deve ser 123.");
	}

	@Test
	@DisplayName("Dado uma instância de PlanDetailed sem valores definidos, quando os getters são chamados, então os valores retornados devem ser nulos ou default")
	void testGetters_EmptyPlanDetailed() {
		PlanDetailed planDetailed = new PlanDetailed();

		int month = planDetailed.getMonth();
		int year = planDetailed.getYear();
		Plan retrievedPlan = planDetailed.getPlan();
		Double performance = planDetailed.getPerformance();
		Double minimumAverage = planDetailed.getMinimumAverage();
		Double maximumAverage = planDetailed.getMaximumAverage();
		Long exportPlanId = planDetailed.getExportPlanId();

		assertEquals(0, month, "O mês deve ser 0 por padrão.");
		assertEquals(0, year, "O ano deve ser 0 por padrão.");
		assertNull(retrievedPlan, "O plano deve ser null por padrão.");
		assertNull(performance, "O desempenho deve ser null por padrão.");
		assertNull(minimumAverage, "A média mínima deve ser null por padrão.");
		assertNull(maximumAverage, "A média máxima deve ser null por padrão.");
		assertNull(exportPlanId, "O ID de exportação do plano deve ser null por padrão.");
	}

	@Test
	@DisplayName("Dado uma instância de PlanDetailed com valores válidos, quando a função setPerformance é chamada, então o desempenho é alterado corretamente")
	void testSetPerformance() {
		Plan plan = new Plan();

		PlanDetailed planDetailed = new PlanDetailed();
		planDetailed.setMonth(5);
		planDetailed.setYear(2024);
		planDetailed.setPlan(plan);

		planDetailed.setPerformance(90.0);

		assertEquals(90.0, planDetailed.getPerformance(), "O desempenho deve ser 90.0.");
	}

	@Test
	@DisplayName("Dado uma instância de PlanDetailed com valores válidos, quando a função setMinimumAverage é chamada, então a média mínima é alterada corretamente")
	void testSetMinimumAverage() {
		Plan plan = new Plan();

		PlanDetailed planDetailed = new PlanDetailed();
		planDetailed.setMonth(5);
		planDetailed.setYear(2024);
		planDetailed.setPlan(plan);

		planDetailed.setMinimumAverage(55.0);

		assertEquals(55.0, planDetailed.getMinimumAverage(), "A média mínima deve ser 55.0.");
	}

	@Test
	@DisplayName("Dado uma instância de PlanDetailed com valores válidos, quando a função setMaximumAverage é chamada, então a média máxima é alterada corretamente")
	void testSetMaximumAverage() {
		Plan plan = new Plan();

		PlanDetailed planDetailed = new PlanDetailed();
		planDetailed.setMonth(5);
		planDetailed.setYear(2024);
		planDetailed.setPlan(plan);

		planDetailed.setMaximumAverage(110.0);

		assertEquals(110.0, planDetailed.getMaximumAverage(), "A média máxima deve ser 110.0.");
	}

	@Test
	@DisplayName("Dado uma instância de PlanDetailed com um valor de exportPlanId válido, quando o setter é chamado, então o ID de exportação do plano é atribuído corretamente")
	void testSetExportPlanId() {
		Plan plan = new Plan();

		PlanDetailed planDetailed = new PlanDetailed();
		planDetailed.setMonth(5);
		planDetailed.setYear(2024);
		planDetailed.setPlan(plan);

		planDetailed.setExportPlanId(987L);

		assertEquals(987L, planDetailed.getExportPlanId(), "O ID de exportação do plano deve ser 987.");
	}
}
