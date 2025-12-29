package org.forpdi.planning.fields.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduleInstanceTest {
	@Test
	@DisplayName("Test ScheduleInstance creation and setters")
	public void testScheduleInstanceCreationAndSetters() {
		ScheduleInstance scheduleInstance = new ScheduleInstance();

		Long number = 1L;
		String description = "Test description";
		Date begin = new Date();
		Date end = new Date();
		Date creation = new Date();
		String periodicity = "Weekly";
		Schedule schedule = new Schedule();
		List<ScheduleValues> scheduleValues = new ArrayList<>();
		Long exportScheduleId = 10L;

		scheduleInstance.setNumber(number);
		scheduleInstance.setDescription(description);
		scheduleInstance.setBegin(begin);
		scheduleInstance.setEnd(end);
		scheduleInstance.setCreation(creation);
		scheduleInstance.setPeriodicity(periodicity);
		scheduleInstance.setSchedule(schedule);
		scheduleInstance.setScheduleValues(scheduleValues);
		scheduleInstance.setExportScheduleId(exportScheduleId);
		
		assertEquals(number, scheduleInstance.getNumber());
		assertEquals(description, scheduleInstance.getDescription());
		assertEquals(begin, scheduleInstance.getBegin());
		assertEquals(end, scheduleInstance.getEnd());
		assertEquals(creation, scheduleInstance.getCreation());
		assertEquals(periodicity, scheduleInstance.getPeriodicity());
		assertEquals(schedule, scheduleInstance.getSchedule());
		assertEquals(scheduleValues, scheduleInstance.getScheduleValues());
		assertEquals(exportScheduleId, scheduleInstance.getExportScheduleId());
	}
}