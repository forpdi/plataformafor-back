package org.forpdi.planning.fields.dto;

import org.forpdi.planning.fields.schedule.ScheduleInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class ScheduleDtoTest {

    @Test
    public void testCreateScheduleDto_WithValidData() {
        ScheduleInstance scheduleInstance = new ScheduleInstance();
        Long scheduleId = 123L;
        String beginDate = "2024-01-01";
        String endDate = "2024-12-31";

        ScheduleDto dto = new ScheduleDto(scheduleInstance, scheduleId, beginDate, endDate);

        assertEquals(scheduleInstance, dto.scheduleInstance(), "O scheduleInstance está incorreto.");
        assertEquals(scheduleId, dto.scheduleId(), "O scheduleId está incorreto.");
        assertEquals(beginDate, dto.beginDate(), "O beginDate está incorreto.");
        assertEquals(endDate, dto.endDate(), "O endDate está incorreto.");
    }

    @Test
    public void testCreateScheduleDto_WithNullValues() {
        ScheduleInstance scheduleInstance = null;
        Long scheduleId = null;
        String beginDate = null;
        String endDate = null;

        ScheduleDto dto = new ScheduleDto(scheduleInstance, scheduleId, beginDate, endDate);

        assertNull(dto.scheduleInstance(), "O scheduleInstance deveria ser nulo.");
        assertNull(dto.scheduleId(), "O scheduleId deveria ser nulo.");
        assertNull(dto.beginDate(), "O beginDate deveria ser nulo.");
        assertNull(dto.endDate(), "O endDate deveria ser nulo.");
    }

    @Test
    public void testCreateScheduleDto_WithMixedValues() {
        ScheduleInstance scheduleInstance = new ScheduleInstance();
        Long scheduleId = null;
        String beginDate = "2024-01-01";
        String endDate = null;

        ScheduleDto dto = new ScheduleDto(scheduleInstance, scheduleId, beginDate, endDate);

        assertEquals(scheduleInstance, dto.scheduleInstance(), "O scheduleInstance está incorreto.");
        assertNull(dto.scheduleId(), "O scheduleId deveria ser nulo.");
        assertEquals(beginDate, dto.beginDate(), "O beginDate está incorreto.");
        assertNull(dto.endDate(), "O endDate deveria ser nulo.");
    }
}

