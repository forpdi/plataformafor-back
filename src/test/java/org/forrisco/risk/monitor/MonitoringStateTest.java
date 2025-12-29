package org.forrisco.risk.monitor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MonitoringStateTest {

	@DisplayName("MonitoringState Criação da enum UP_TO_DATE.")
	@Test
	void testMonitoringStateCreationUP_TO_DATE() {
		MonitoringState upToDate = MonitoringState.UP_TO_DATE;

		String colorHex = upToDate.getColorHex();
		String reportLabel = upToDate.getReportLabel();
		Integer order = upToDate.getOrder();

		assertNotNull(upToDate, "A enum não deveria ser nula.");
		assertEquals("#83cab1", colorHex, "ColorHex deveria ser igual.");
		assertEquals("Em dia", reportLabel, "ReportLabel deveria ser igual.");
		assertEquals(1, order, "A ordem deveria ser igual.");
	}

	@DisplayName("MonitoringState Criação da enum CLOSE_TO_DUE.")
	@Test
	void testMonitoringStateCreationCLOSE_TO_DUE() {
		MonitoringState closeToDue = MonitoringState.CLOSE_TO_DUE;

		String colorHex = closeToDue.getColorHex();
		String reportLabel = closeToDue.getReportLabel();
		Integer order = closeToDue.getOrder();

		assertNotNull(closeToDue, "A enum não deveria ser nula.");
		assertEquals("#e2e470", colorHex, "ColorHex deveria ser igual.");
		assertEquals("Próximos à Vencer", reportLabel, "ReportLabel deveria ser igual.");
		assertEquals(2, order, "A ordem deveria ser igual.");
	}

	@DisplayName("MonitoringState Criação da enum LATE.")
	@Test
	void testMonitoringStateCreationLATE() {
		MonitoringState late = MonitoringState.LATE;

		String colorHex = late.getColorHex();
		String reportLabel = late.getReportLabel();
		Integer order = late.getOrder();

		assertNotNull(late, "A enum não deveria ser nula.");
		assertEquals("#e97c66", colorHex, "ColorHex deveria ser igual.");
		assertEquals("Atrasado", reportLabel, "ReportLabel deveria ser igual.");
		assertEquals(3, order, "A ordem deveria ser igual.");
	}

	@DisplayName("MonitoringState Obtendo a Enum por Value.")
	@Test
	public void testMonitoringStateGetByValue() {
		String value = "em dia";

		MonitoringState result = MonitoringState.getMonitoringStateByValue(value);

		assertEquals(MonitoringState.UP_TO_DATE, result);
	}

	@DisplayName("MonitoringState Realizando a comparação pelo método 'compare'.")
	@Test
	public void testMonitoringStateCompare() {
		MonitoringState upToDate = MonitoringState.UP_TO_DATE;
		MonitoringState late = MonitoringState.LATE;

		int result = upToDate.compare(upToDate, late);

		assertEquals(-2, result);
	}
}