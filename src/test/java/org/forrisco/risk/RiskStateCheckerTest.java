package org.forrisco.risk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskStateCheckerTest {

	RiskStateCheckerTestDateGenerator dateGenetador;

	@DisplayName("Retorna um estado padrão caso não seja informada uma periodicidade inválida.")
	@Test
	void testRiskStateDefaultCase() {
		String periodicity = "invalidPeriodicity";
		Date date;
		Calendar calendar = Calendar.getInstance();
		calendar.set(2024, Calendar.AUGUST, 25);
		date = calendar.getTime();

		int returnedState;

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.UP_TO_DATE.getId(), returnedState, "Era esperado que o estado fosse LATE");
	}

	@DisplayName("Retorna o estado de cada risco a partir da data de acordo com a periodicidade diária.")
	@Test
	void testRiskStateDaily() {

		String periodicity = "diária";

		Date date;

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.LATE.getId());
		int returnedState;

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.LATE.getId(), returnedState, "Era esperado que o estado fosse LATE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.CLOSE_TO_EXPIRE.getId());

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.CLOSE_TO_EXPIRE.getId(), returnedState, "Era esperado que o estado fosse CLOSE_TO_EXPIRE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.UP_TO_DATE.getId());
		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.UP_TO_DATE.getId(), returnedState, "Era esperado que o estado fosse UP_TO_DATE");
	}

	@DisplayName("Retorna o estado de cada risco a partir da data de acordo com a periodicidade semanal.")
	@Test
	void testRiskStateWeekly() {

		String periodicity = "semanal";

		Date date;

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.LATE.getId());
		int returnedState;

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.LATE.getId(), returnedState, "Era esperado que o estado fosse LATE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.CLOSE_TO_EXPIRE.getId());

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.CLOSE_TO_EXPIRE.getId(), returnedState, "Era esperado que o estado fosse CLOSE_TO_EXPIRE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.UP_TO_DATE.getId());
		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.UP_TO_DATE.getId(), returnedState, "Era esperado que o estado fosse UP_TO_DATE");
	}

	@DisplayName("Retorna o estado de cada risco a partir da data de acordo com a periodicidade quinzenal.")
	@Test
	void testRiskStateBiweekly() {

		String periodicity = "quinzenal";

		Date date;

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.LATE.getId());
		int returnedState;

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.LATE.getId(), returnedState, "Era esperado que o estado fosse LATE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.CLOSE_TO_EXPIRE.getId());

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.CLOSE_TO_EXPIRE.getId(), returnedState, "Era esperado que o estado fosse CLOSE_TO_EXPIRE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.UP_TO_DATE.getId());
		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.UP_TO_DATE.getId(), returnedState, "Era esperado que o estado fosse UP_TO_DATE");
	}

	@DisplayName("Retorna o estado de cada risco a partir da data de acordo com a periodicidade mensal.")
	@Test
	void testRiskStateMonthly() {

		String periodicity = "mensal";

		Date date;

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.LATE.getId());
		int returnedState;

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.LATE.getId(), returnedState, "Era esperado que o estado fosse LATE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.CLOSE_TO_EXPIRE.getId());

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.CLOSE_TO_EXPIRE.getId(), returnedState, "Era esperado que o estado fosse CLOSE_TO_EXPIRE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.UP_TO_DATE.getId());
		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.UP_TO_DATE.getId(), returnedState, "Era esperado que o estado fosse UP_TO_DATE");
	}

	@DisplayName("Retorna o estado de cada risco a partir da data de acordo com a periodicidade bimestral.")
	@Test
	void testRiskStateBimonthly() {

		String periodicity = "bimestral";

		Date date;

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.LATE.getId());
		int returnedState;

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.LATE.getId(), returnedState, "Era esperado que o estado fosse LATE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.CLOSE_TO_EXPIRE.getId());

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.CLOSE_TO_EXPIRE.getId(), returnedState, "Era esperado que o estado fosse CLOSE_TO_EXPIRE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.UP_TO_DATE.getId());
		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.UP_TO_DATE.getId(), returnedState, "Era esperado que o estado fosse UP_TO_DATE");
	}

	@DisplayName("Retorna o estado de cada risco a partir da data de acordo com a periodicidade trimestral.")
	@Test
	void testRiskStateQuarterly() {

		String periodicity = "trimestral";

		Date date;

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.LATE.getId());
		int returnedState;

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.LATE.getId(), returnedState, "Era esperado que o estado fosse LATE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.CLOSE_TO_EXPIRE.getId());

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.CLOSE_TO_EXPIRE.getId(), returnedState, "Era esperado que o estado fosse CLOSE_TO_EXPIRE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.UP_TO_DATE.getId());
		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.UP_TO_DATE.getId(), returnedState, "Era esperado que o estado fosse UP_TO_DATE");
	}

	@DisplayName("Retorna o estado de cada risco a partir da data de acordo com a periodicidade semestral.")
	@Test
	void testRiskStateSemiannual() {

		String periodicity = "semestral";

		Date date;

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.LATE.getId());
		int returnedState;

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.LATE.getId(), returnedState, "Era esperado que o estado fosse LATE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.CLOSE_TO_EXPIRE.getId());

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.CLOSE_TO_EXPIRE.getId(), returnedState, "Era esperado que o estado fosse CLOSE_TO_EXPIRE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.UP_TO_DATE.getId());
		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.UP_TO_DATE.getId(), returnedState, "Era esperado que o estado fosse UP_TO_DATE");
	}

	@DisplayName("Retorna o estado de cada risco a partir da data de acordo com a periodicidade anual.")
	@Test
	void testRiskStateAnnual() {

		String periodicity = "anual";

		Date date;

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.LATE.getId());
		int returnedState;

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.LATE.getId(), returnedState, "Era esperado que o estado fosse LATE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.CLOSE_TO_EXPIRE.getId());

		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.CLOSE_TO_EXPIRE.getId(), returnedState, "Era esperado que o estado fosse CLOSE_TO_EXPIRE");

		date = RiskStateCheckerTestDateGenerator.getRiskStateDate(periodicity, RiskState.UP_TO_DATE.getId());
		returnedState = RiskStateChecker.riskState(periodicity, date);

		assertEquals(RiskState.UP_TO_DATE.getId(), returnedState, "Era esperado que o estado fosse UP_TO_DATE");
	}
}