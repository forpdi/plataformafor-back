package org.forpdi.planning.fields.dto;

import org.forpdi.planning.fields.schedule.ScheduleInstance;

public record ScheduleDto(ScheduleInstance scheduleInstance, Long scheduleId, String beginDate, String endDate) {

}
