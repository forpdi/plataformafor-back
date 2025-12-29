package org.forpdi.planning.structure.dto;

import org.forpdi.planning.plan.PDIFilterParams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoalsReportRequestDtoTest {

    @Test
    @DisplayName("from: aceita nulos (propaga nulls do filtro e de companyId)")
    void from_allowsNulls() {
        PDIFilterParams f = mock(PDIFilterParams.class);

        GoalsReportRequestDto dto = GoalsReportRequestDto.from(f, null);

        assertNotNull(dto);

        assertNullOrZero(dto.macroId());
        assertNullOrZero(dto.planId());
        assertNullOrZero(dto.levelInstanceId());
        assertNullOrZero(dto.goalId());

        assertNull(dto.startDate());
        assertNull(dto.endDate());

        assertNullOrFalse(dto.goalStatus());

        Integer ps = dto.progressStatus();
        assertTrue(ps == null || ps == 0, "progressStatus deve ser null ou 0 quando não stubado");

        assertNull(dto.companyId());
    }

    private static void assertNullOrZero(Long v) {
        assertTrue(v == null || v == 0L, "esperado null ou 0L quando origem é primitiva não stubada");
    }

    private static void assertNullOrFalse(Boolean b) {
        assertTrue(b == null || !b, "esperado null ou false quando origem é primitiva não stubada");
    }

    @Test
    @DisplayName("from: mapeia todos os campos de PDIFilterParams + companyId")
    void from_mapsAllFields() {
        PDIFilterParams f = mock(PDIFilterParams.class);

        when(f.getMacro()).thenReturn(1L);
        when(f.getPlan()).thenReturn(2L);
        when(f.getLevelInstance()).thenReturn(3L);
        when(f.getGoalId()).thenReturn(4L);
        when(f.getStartDate()).thenReturn("2025-01-01");
        when(f.getEndDate()).thenReturn("2025-12-31");
        when(f.getGoalStatus()).thenReturn(Boolean.TRUE);
        doReturn(7).when(f).getProgressStatus();

        Long companyId = 99L;

        GoalsReportRequestDto dto = GoalsReportRequestDto.from(f, companyId);

        assertEquals(1L, dto.macroId());
        assertEquals(2L, dto.planId());
        assertEquals(3L, dto.levelInstanceId());
        assertEquals(4L, dto.goalId());
        assertEquals("2025-01-01", dto.startDate());
        assertEquals("2025-12-31", dto.endDate());
        assertEquals(Boolean.TRUE, dto.goalStatus());
        assertEquals(7, dto.progressStatus());
        assertEquals(99L, dto.companyId());
    }

    @Test
    @DisplayName("record: equals/hashCode/toString coerentes")
    void record_equalityAndToString() {
        GoalsReportRequestDto a = new GoalsReportRequestDto(
                1L, 2L, 3L, 4L, "s", "e", true, 5, 6L);
        GoalsReportRequestDto b = new GoalsReportRequestDto(
                1L, 2L, 3L, 4L, "s", "e", true, 5, 6L);
        GoalsReportRequestDto c = new GoalsReportRequestDto(
                1L, 2L, 3L, 4L, "s", "e", true, 6, 6L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);

        String ts = a.toString();
        assertTrue(ts.contains("macroId=1"));
        assertTrue(ts.contains("planId=2"));
        assertTrue(ts.contains("progressStatus=5"));
    }
}
