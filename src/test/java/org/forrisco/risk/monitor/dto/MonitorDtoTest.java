package org.forrisco.risk.monitor.dto;

import org.forpdi.core.user.User;
import org.forrisco.risk.Risk;
import org.forrisco.risk.monitor.Monitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MonitorDtoTest {
	@DisplayName("MonitorDto Criação do DTO.")
	@Test
	void testMonitorDtoCreation() {
		Monitor monitor = new Monitor();
		monitor.setBegin(new Date());
		monitor.setImpact("High");
		monitor.setProbability("Medium");
		monitor.setReport("Test Report");
		monitor.setUser(new User());
		monitor.setRisk(new Risk());

		MonitorDto dto = new MonitorDto(monitor);

		assertNotNull(dto);
		assertEquals(monitor, dto.monitor());
	}
}