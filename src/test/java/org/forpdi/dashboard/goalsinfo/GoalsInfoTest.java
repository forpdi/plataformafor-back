package org.forpdi.dashboard.goalsinfo;

import org.forpdi.planning.plan.PlanMacro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class GoalsInfoTest {

	private GoalsInfo goalsInfo;

	@BeforeEach
	void setUp() {
		goalsInfo = new GoalsInfo();
		goalsInfo.setId(1L);
		goalsInfo.setInDay(5L);
		goalsInfo.setLate(2L);
		goalsInfo.setBelowMininum(0L);
		goalsInfo.setBelowExpected(1L);
		goalsInfo.setReached(10L);
		goalsInfo.setAboveExpected(3L);
		goalsInfo.setNotStarted(0L);
		goalsInfo.setFinished(7L);
		goalsInfo.setCloseToMaturity(4L);
		goalsInfo.setTotal(30L);
		goalsInfo.setUpdatedAt(new Date());
	}

	@Test
	void testGetId() {
		assertEquals(1L, goalsInfo.getId(), "O ID deve ser 1");
	}

	@Test
	void testSetId() {
		goalsInfo.setId(2L);
		assertEquals(2L, goalsInfo.getId(), "O ID deve ser atualizado para 2");
	}

	@Test
	void testUpdateInfos() {
		GoalsInfo newInfo = new GoalsInfo();
		newInfo.setInDay(10L);
		newInfo.setLate(5L);
		newInfo.setBelowMininum(2L);
		newInfo.setBelowExpected(3L);
		newInfo.setReached(15L);
		newInfo.setAboveExpected(4L);
		newInfo.setNotStarted(1L);
		newInfo.setFinished(10L);
		newInfo.setCloseToMaturity(5L);
		newInfo.setTotal(50L);
		newInfo.setUpdatedAt(new Date());

		goalsInfo.updateInfos(newInfo);

		assertEquals(10L, goalsInfo.getInDay(), "O valor de 'InDay' deve ser 10");
		assertEquals(5L, goalsInfo.getLate(), "O valor de 'Late' deve ser 5");
		assertEquals(2L, goalsInfo.getBelowMininum(), "O valor de 'BelowMininum' deve ser 2");
		assertEquals(3L, goalsInfo.getBelowExpected(), "O valor de 'BelowExpected' deve ser 3");
		assertEquals(15L, goalsInfo.getReached(), "O valor de 'Reached' deve ser 15");
		assertEquals(4L, goalsInfo.getAboveExpected(), "O valor de 'AboveExpected' deve ser 4");
		assertEquals(1L, goalsInfo.getNotStarted(), "O valor de 'NotStarted' deve ser 1");
		assertEquals(10L, goalsInfo.getFinished(), "O valor de 'Finished' deve ser 10");
		assertEquals(5L, goalsInfo.getCloseToMaturity(), "O valor de 'CloseToMaturity' deve ser 5");
		assertEquals(50L, goalsInfo.getTotal(), "O valor de 'Total' deve ser 50");
	}

	@Test
	void testSetUpdatedAt() {
		Date currentDate = new Date();
		goalsInfo.setUpdatedAt(currentDate);
		assertEquals(currentDate, goalsInfo.getUpdatedAt(), "A data de 'updatedAt' deve ser a mesma que foi definida");
	}

	@Test
	void testGetPlanMacro() {
		PlanMacro planMacro = new PlanMacro();
		goalsInfo.setPlanMacro(planMacro);
		assertEquals(planMacro, goalsInfo.getPlanMacro(), "O PlanMacro deve ser o mesmo que foi definido");
	}

	@Test
	void testSetPlanMacro() {
		PlanMacro planMacro = new PlanMacro();
		goalsInfo.setPlanMacro(planMacro);
		assertNotNull(goalsInfo.getPlanMacro(), "O PlanMacro não pode ser nulo após ser definido");
	}
}
