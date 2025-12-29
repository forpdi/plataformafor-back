package org.forrisco.risk.monitor;

import org.forrisco.core.unit.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class MonitorHistoryTest {

	@DisplayName("MonitorHistory Validação da Classe por meio dos Getters and Setters.")
	@Test
	public void testMonitorHistoryCreationWithSettersAndGetters() {
		MonitorHistory monitorHistory = new MonitorHistory();

		Unit unit = mock(Unit.class);
		monitorHistory.setUnit(unit);
		monitorHistory.setEstado("Inativo");
		monitorHistory.setMonth(6);
		monitorHistory.setYear(2025);
		monitorHistory.setQuantity(50);

		assertEquals(unit, monitorHistory.getUnit());
		assertEquals("Inativo", monitorHistory.getEstado());
		assertEquals(6, monitorHistory.getMonth());
		assertEquals(2025, monitorHistory.getYear());
		assertEquals(50, monitorHistory.getQuantity());
	}
}