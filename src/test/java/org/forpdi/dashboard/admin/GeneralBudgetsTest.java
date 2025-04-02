package org.forpdi.dashboard.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class GeneralBudgetsTest {

	@Test
	@DisplayName("Deve inicializar os valores padrão corretamente")
	void givenGeneralBudgets_whenInstantiated_thenShouldHaveDefaultValues() {
		GeneralBudgets budgets = new GeneralBudgets();

		assertNotNull(budgets.getPlanned(), "O valor planejado (planned) não deve ser nulo.");
		assertEquals(0.0, budgets.getPlanned(), "O valor planejado (planned) deveria ser inicializado como 0.0.");
		assertNotNull(budgets.getConducted(), "O valor realizado (conducted) não deve ser nulo.");
		assertEquals(0.0, budgets.getConducted(), "O valor realizado (conducted) deveria ser inicializado como 0.0.");
		assertNotNull(budgets.getCommitted(), "O valor comprometido (committed) não deve ser nulo.");
		assertEquals(0.0, budgets.getCommitted(), "O valor comprometido (committed) deveria ser inicializado como 0.0.");
	}

	@Test
	@DisplayName("Deve configurar e retornar o valor planejado corretamente")
	void givenGeneralBudgets_whenSetPlanned_thenShouldReturnUpdatedValue() {
		GeneralBudgets budgets = new GeneralBudgets();
		Double planned = 5000.0;

		budgets.setPlanned(planned);

		assertEquals(planned, budgets.getPlanned(), "O valor planejado (planned) não foi configurado corretamente.");
	}

	@Test
	@DisplayName("Deve configurar e retornar o valor realizado corretamente")
	void givenGeneralBudgets_whenSetConducted_thenShouldReturnUpdatedValue() {
		GeneralBudgets budgets = new GeneralBudgets();
		Double conducted = 3000.0;

		budgets.setConducted(conducted);

		assertEquals(conducted, budgets.getConducted(), "O valor realizado (conducted) não foi configurado corretamente.");
	}

	@Test
	@DisplayName("Deve configurar e retornar o valor comprometido corretamente")
	void givenGeneralBudgets_whenSetCommitted_thenShouldReturnUpdatedValue() {
		GeneralBudgets budgets = new GeneralBudgets();
		Double committed = 2000.0;

		budgets.setCommitted(committed);

		assertEquals(committed, budgets.getCommitted(), "O valor comprometido (committed) não foi configurado corretamente.");
	}
}
