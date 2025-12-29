package org.forrisco.core.unit.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.forrisco.risk.RiskFilterParams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExportUnitReportDtoTest {

    @Test
    @DisplayName("Getters e Setters devem armazenar e retornar valores corretamente")
    void testGettersAndSetters() {
        ExportUnitReportDto dto = new ExportUnitReportDto();

        Long planRiskId = 10L;
        String title = "Relatório de Unidade";
        List<Long> units = List.of(1L, 2L);
        List<Long> subunits = List.of(3L, 4L);
        RiskFilterParams filters = new RiskFilterParams();
        Integer year = 2025;

        dto.setPlanRiskId(planRiskId);
        dto.setTitle(title);
        dto.setUnits(units);
        dto.setSubunits(subunits);
        dto.setRiskFilters(filters);
        dto.setSelectedYear(year);

        assertEquals(planRiskId, dto.getPlanRiskId());
        assertEquals(title, dto.getTitle());
        assertEquals(units, dto.getUnits());
        assertEquals(subunits, dto.getSubunits());
        assertEquals(filters, dto.getRiskFilters());
        assertEquals(year, dto.getSelectedYear());
    }

    @Test
    @DisplayName("Deve aceitar valores nulos sem lançar exceção")
    void testNullValues() {
        ExportUnitReportDto dto = new ExportUnitReportDto();

        dto.setPlanRiskId(null);
        dto.setTitle(null);
        dto.setUnits(null);
        dto.setSubunits(null);
        dto.setRiskFilters(null);
        dto.setSelectedYear(null);

        assertNull(dto.getPlanRiskId());
        assertNull(dto.getTitle());
        assertNull(dto.getUnits());
        assertNull(dto.getSubunits());
        assertNull(dto.getRiskFilters());
        assertNull(dto.getSelectedYear());
    }
}
