package org.forpdi.dashboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Teste da classe GoalsParams e GoalsParamsBuilder")
class GoalsParamsTest {

	@Test
	@DisplayName("Teste dos getters e setters de GoalsParams")
	void testGettersAndSetters() {
		GoalsParams goalsParams = new GoalsParams();

		Double max = 100.0;
		Double min = 10.0;
		Double exp = 50.0;
		Double reach = 75.0;
		Date finish = new Date();

		goalsParams.setMax(max);
		goalsParams.setMin(min);
		goalsParams.setExp(exp);
		goalsParams.setReach(reach);
		goalsParams.setFinish(finish);

		assertEquals(max, goalsParams.getMax());
		assertEquals(min, goalsParams.getMin());
		assertEquals(exp, goalsParams.getExp());
		assertEquals(reach, goalsParams.getReach());
		assertEquals(finish, goalsParams.getFinish());
	}

	@Test
	@DisplayName("Teste do builder GoalsParamsBuilder")
	void testGoalsParamsBuilder() {
		Double max = 120.0;
		Double min = 20.0;
		Double exp = 60.0;
		Double reach = 90.0;
		Date finish = new Date();

		GoalsParams goalsParams = new GoalsParams.GoalsParamsBuilder()
			.max(max)
			.min(min)
			.exp(exp)
			.reach(reach)
			.finish(finish)
			.create();

		assertEquals(max, goalsParams.getMax());
		assertEquals(min, goalsParams.getMin());
		assertEquals(exp, goalsParams.getExp());
		assertEquals(reach, goalsParams.getReach());
		assertEquals(finish, goalsParams.getFinish());
	}

	@Test
	@DisplayName("Teste de valores nulos no builder")
	void testBuilderWithNullValues() {
		GoalsParams goalsParams = new GoalsParams.GoalsParamsBuilder()
			.max(null)
			.min(null)
			.exp(null)
			.reach(null)
			.finish(null)
			.create();

		assertNull(goalsParams.getMax());
		assertNull(goalsParams.getMin());
		assertNull(goalsParams.getExp());
		assertNull(goalsParams.getReach());
		assertNull(goalsParams.getFinish());
	}
}
