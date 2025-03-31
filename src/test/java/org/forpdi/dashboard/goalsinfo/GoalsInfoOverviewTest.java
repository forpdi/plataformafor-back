package org.forpdi.dashboard.goalsinfo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GoalsInfoOverviewTest {

	@Test
	void testConstructorWithParameters() {
		Long inDay = 5L;
		Long late = 10L;
		Long belowMininum = 0L;
		Long belowExpected = 2L;
		Long reached = 20L;
		Long aboveExpected = 3L;
		Long notStarted = 1L;
		Long finished = 30L;
		Long closeToMaturity = 4L;
		Long total = 75L;

		GoalsInfoOverview goalsInfo = new GoalsInfoOverview(inDay, late, belowMininum, belowExpected, reached,
			aboveExpected, notStarted, finished, closeToMaturity, total);

		assertEquals(inDay, goalsInfo.getInDay());
		assertEquals(late, goalsInfo.getLate());
		assertEquals(belowMininum, goalsInfo.getBelowMininum());
		assertEquals(belowExpected, goalsInfo.getBelowExpected());
		assertEquals(reached, goalsInfo.getReached());
		assertEquals(aboveExpected, goalsInfo.getAboveExpected());
		assertEquals(notStarted, goalsInfo.getNotStarted());
		assertEquals(finished, goalsInfo.getFinished());
		assertEquals(closeToMaturity, goalsInfo.getCloseToMaturity());
		assertEquals(total, goalsInfo.getTotal());
	}

	@Test
	public void testConstructorWithoutParameters() {
		GoalsInfoOverview goalsInfo = new GoalsInfoOverview();

		assertEquals(0L, goalsInfo.getInDay());
		assertEquals(0L, goalsInfo.getLate());
		assertEquals(0L, goalsInfo.getBelowMininum());
		assertEquals(0L, goalsInfo.getBelowExpected());
		assertEquals(0L, goalsInfo.getReached());
		assertEquals(0L, goalsInfo.getAboveExpected());
		assertEquals(0L, goalsInfo.getNotStarted());
		assertEquals(0L, goalsInfo.getFinished());
		assertEquals(0L, goalsInfo.getCloseToMaturity());
		assertEquals(0L, goalsInfo.getTotal());
	}

	@Test
	public void testSettersAndGetters() {
		GoalsInfoOverview goalsInfo = new GoalsInfoOverview();

		goalsInfo.setInDay(5L);
		goalsInfo.setLate(10L);
		goalsInfo.setBelowMininum(1L);
		goalsInfo.setBelowExpected(3L);
		goalsInfo.setReached(20L);
		goalsInfo.setAboveExpected(4L);
		goalsInfo.setNotStarted(2L);
		goalsInfo.setFinished(30L);
		goalsInfo.setCloseToMaturity(6L);
		goalsInfo.setTotal(100L);

		assertEquals(5L, goalsInfo.getInDay());
		assertEquals(10L, goalsInfo.getLate());
		assertEquals(1L, goalsInfo.getBelowMininum());
		assertEquals(3L, goalsInfo.getBelowExpected());
		assertEquals(20L, goalsInfo.getReached());
		assertEquals(4L, goalsInfo.getAboveExpected());
		assertEquals(2L, goalsInfo.getNotStarted());
		assertEquals(30L, goalsInfo.getFinished());
		assertEquals(6L, goalsInfo.getCloseToMaturity());
		assertEquals(100L, goalsInfo.getTotal());
	}

	@Test
	void testGetReachedOrAboveExpected() {
		GoalsInfoOverview goalsInfo = new GoalsInfoOverview();
		goalsInfo.setReached(20L);
		goalsInfo.setAboveExpected(5L);

		long result = goalsInfo.getReachedOrAboveExpected();

		assertEquals(25L, result);
	}

	@Test
	void testZeroIfNullWithNullValues() {
		GoalsInfoOverview goalsInfo = new GoalsInfoOverview();

		long result = goalsInfo.getReached();

		assertEquals(0L, result); 
	}
}
