package org.forpdi.dashboard.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Teste da classe PlanDetails")
class PlanDetailsTest {

	@Test
	@DisplayName("Verifica o construtor padrão e os valores iniciais")
	void testDefaultConstructor() {
		PlanDetails planDetails = new PlanDetails();

		assertEquals(0, planDetails.getNumberOfPlans(), "O número de planos deve ser inicializado como 0.");
		assertEquals(0L, planDetails.getNumberOfObjectives(), "O número de objetivos deve ser inicializado como 0.");
		assertEquals(0L, planDetails.getNumberOfIndicators(), "O número de indicadores deve ser inicializado como 0.");
		assertEquals(0, planDetails.getNumberOfGoals(), "O número de metas deve ser inicializado como 0.");
		assertEquals(0, planDetails.getNumberOfBudgets(), "O número de orçamentos deve ser inicializado como 0.");
		assertEquals(0.0, planDetails.getGoalsDelayedPerCent(), "A porcentagem de metas atrasadas deve ser inicializada como 0.0.");
		assertEquals(0, planDetails.getNumberOfThematicAxis(), "O número de eixos temáticos deve ser inicializado como 0.");
	}

	@Test
	@DisplayName("Testa os métodos getter e setter para número de planos")
	void testNumberOfPlans() {
		PlanDetails planDetails = new PlanDetails();
		planDetails.setNumberOfPlans(10);

		assertEquals(10, planDetails.getNumberOfPlans(), "O número de planos deve ser igual a 10.");
	}

	@Test
	@DisplayName("Testa os métodos getter e setter para número de objetivos")
	void testNumberOfObjectives() {
		PlanDetails planDetails = new PlanDetails();
		planDetails.setNumberOfObjectives(15L);

		assertEquals(15L, planDetails.getNumberOfObjectives(), "O número de objetivos deve ser igual a 15.");
	}

	@Test
	@DisplayName("Testa os métodos getter e setter para número de indicadores")
	void testNumberOfIndicators() {
		PlanDetails planDetails = new PlanDetails();
		planDetails.setNumberOfIndicators(25L);

		assertEquals(25L, planDetails.getNumberOfIndicators(), "O número de indicadores deve ser igual a 25.");
	}

	@Test
	@DisplayName("Testa os métodos getter e setter para número de metas")
	void testNumberOfGoals() {
		PlanDetails planDetails = new PlanDetails();
		planDetails.setNumberOfGoals(30);

		assertEquals(30, planDetails.getNumberOfGoals(), "O número de metas deve ser igual a 30.");
	}

	@Test
	@DisplayName("Testa os métodos getter e setter para número de orçamentos")
	void testNumberOfBudgets() {
		PlanDetails planDetails = new PlanDetails();
		planDetails.setNumberOfBudgets(20);

		assertEquals(20, planDetails.getNumberOfBudgets(), "O número de orçamentos deve ser igual a 20.");
	}

	@Test
	@DisplayName("Testa os métodos getter e setter para porcentagem de metas atrasadas")
	void testGoalsDelayedPerCent() {
		PlanDetails planDetails = new PlanDetails();
		planDetails.setGoalsDelayedPerCent(50.5);

		assertEquals(50.5, planDetails.getGoalsDelayedPerCent(), "A porcentagem de metas atrasadas deve ser igual a 50.5.");
	}

	@Test
	@DisplayName("Testa os métodos getter e setter para número de eixos temáticos")
	void testNumberOfThematicAxis() {
		PlanDetails planDetails = new PlanDetails();
		planDetails.setNumberOfThematicAxis(5);

		assertEquals(5, planDetails.getNumberOfThematicAxis(), "O número de eixos temáticos deve ser igual a 5.");
	}

	@Test
	@DisplayName("Verifica que os métodos getter e setter manipulam valores nulos para campos Long corretamente")
	void testNullValuesForLongFields() {
		PlanDetails planDetails = new PlanDetails();
		planDetails.setNumberOfObjectives(null);
		planDetails.setNumberOfIndicators(null);

		assertNull(planDetails.getNumberOfObjectives(), "O número de objetivos deve ser nulo.");
		assertNull(planDetails.getNumberOfIndicators(), "O número de indicadores deve ser nulo.");
	}
}
