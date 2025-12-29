package org.forpdi.dashboard.indicators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.forpdi.core.company.Company;

class CompanyFpdiIndicatorsTest {

	@Test
	@DisplayName("Dado uma instância de CompanyFpdiIndicators com valores válidos, quando os getters são chamados, então os valores corretos são retornados")
	void testGetters_ValidCompanyFpdiIndicators() {
		Company company = new Company();
		CompanyFpdiIndicators indicators = new CompanyFpdiIndicators();
		indicators.setCompany(company);
		indicators.setPlanMacros(10);
		indicators.setPlans(5);
		indicators.setAxes(3);
		indicators.setObjectives(7);
		indicators.setIndicators(15);
		indicators.setGoals(8);

		Company retrievedCompany = indicators.getCompany();
		long planMacros = indicators.getPlanMacros();
		long plans = indicators.getPlans();
		long axes = indicators.getAxes();
		long objectives = indicators.getObjectives();
		long indicatorsCount = indicators.getIndicators();
		long goals = indicators.getGoals();

		assertEquals(company, retrievedCompany, "A empresa deve ser a mesma.");
		assertEquals(10, planMacros, "O número de 'planMacros' deve ser 10.");
		assertEquals(5, plans, "O número de 'plans' deve ser 5.");
		assertEquals(3, axes, "O número de 'axes' deve ser 3.");
		assertEquals(7, objectives, "O número de 'objectives' deve ser 7.");
		assertEquals(15, indicatorsCount, "O número de 'indicators' deve ser 15.");
		assertEquals(8, goals, "O número de 'goals' deve ser 8.");
	}

	@Test
	@DisplayName("Dado uma instância de CompanyFpdiIndicators sem valores definidos, quando os getters são chamados, então os valores retornados devem ser zero ou null")
	void testGetters_EmptyCompanyFpdiIndicators() {
		CompanyFpdiIndicators indicators = new CompanyFpdiIndicators();

		Company retrievedCompany = indicators.getCompany();
		long planMacros = indicators.getPlanMacros();
		long plans = indicators.getPlans();
		long axes = indicators.getAxes();
		long objectives = indicators.getObjectives();
		long indicatorsCount = indicators.getIndicators();
		long goals = indicators.getGoals();

		assertNull(retrievedCompany, "A empresa deve ser null.");
		assertEquals(0, planMacros, "O número de 'planMacros' deve ser 0 por padrão.");
		assertEquals(0, plans, "O número de 'plans' deve ser 0 por padrão.");
		assertEquals(0, axes, "O número de 'axes' deve ser 0 por padrão.");
		assertEquals(0, objectives, "O número de 'objectives' deve ser 0 por padrão.");
		assertEquals(0, indicatorsCount, "O número de 'indicators' deve ser 0 por padrão.");
		assertEquals(0, goals, "O número de 'goals' deve ser 0 por padrão.");
	}

	@Test
	@DisplayName("Dado uma instância de CompanyFpdiIndicators, quando o setter de planMacros é chamado, então o valor de planMacros é alterado corretamente")
	void testSetPlanMacros() {
		CompanyFpdiIndicators indicators = new CompanyFpdiIndicators();
		indicators.setPlanMacros(10);

		indicators.setPlanMacros(20);

		assertEquals(20, indicators.getPlanMacros(), "O valor de 'planMacros' deve ser 20.");
	}

	@Test
	@DisplayName("Dado uma instância de CompanyFpdiIndicators, quando o setter de plans é chamado, então o valor de plans é alterado corretamente")
	void testSetPlans() {
		CompanyFpdiIndicators indicators = new CompanyFpdiIndicators();
		indicators.setPlans(5);

		indicators.setPlans(10);

		assertEquals(10, indicators.getPlans(), "O valor de 'plans' deve ser 10.");
	}

	@Test
	@DisplayName("Dado uma instância de CompanyFpdiIndicators, quando o setter de axes é chamado, então o valor de axes é alterado corretamente")
	void testSetAxes() {
		CompanyFpdiIndicators indicators = new CompanyFpdiIndicators();
		indicators.setAxes(3);

		indicators.setAxes(6);

		assertEquals(6, indicators.getAxes(), "O valor de 'axes' deve ser 6.");
	}

	@Test
	@DisplayName("Dado uma instância de CompanyFpdiIndicators, quando o setter de objectives é chamado, então o valor de objectives é alterado corretamente")
	void testSetObjectives() {
		CompanyFpdiIndicators indicators = new CompanyFpdiIndicators();
		indicators.setObjectives(7);

		indicators.setObjectives(14);

		assertEquals(14, indicators.getObjectives(), "O valor de 'objectives' deve ser 14.");
	}

	@Test
	@DisplayName("Dado uma instância de CompanyFpdiIndicators, quando o setter de indicators é chamado, então o valor de indicators é alterado corretamente")
	void testSetIndicators() {
		CompanyFpdiIndicators indicators = new CompanyFpdiIndicators();
		indicators.setIndicators(10);

		indicators.setIndicators(25);

		assertEquals(25, indicators.getIndicators(), "O valor de 'indicators' deve ser 25.");
	}

	@Test
	@DisplayName("Dado uma instância de CompanyFpdiIndicators, quando o setter de goals é chamado, então o valor de goals é alterado corretamente")
	void testSetGoals() {
		CompanyFpdiIndicators indicators = new CompanyFpdiIndicators();
		indicators.setGoals(5);

		indicators.setGoals(10);

		assertEquals(10, indicators.getGoals(), "O valor de 'goals' deve ser 10.");
	}
}
