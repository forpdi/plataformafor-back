package org.forpdi.planning.fields.dto;

import org.forpdi.planning.fields.schedule.Schedule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class ScheduleToSaveDtoTest {

    @Test
    public void testCreateScheduleToSaveDto_WithValidSchedule() {
        Schedule schedule = new Schedule();
        
        ScheduleToSaveDto dto = new ScheduleToSaveDto(schedule);

        assertEquals(schedule, dto.schedule(), "O Schedule não foi armazenado corretamente no DTO.");
    }

    @Test
    public void testCreateScheduleToSaveDto_WithNullSchedule() {
        Schedule schedule = null;

        ScheduleToSaveDto dto = new ScheduleToSaveDto(schedule);

        assertNull(dto.schedule(), "O Schedule deveria ser nulo.");
    }
}

