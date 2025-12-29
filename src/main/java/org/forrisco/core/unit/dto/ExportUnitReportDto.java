package org.forrisco.core.unit.dto;

import java.util.List;

import org.forrisco.risk.RiskFilterParams;

public class ExportUnitReportDto {

    private Long planRiskId;
    private String title;
    private List<Long> units;
    private List<Long> subunits;
    private RiskFilterParams riskFilters;
    private Integer selectedYear;

    public Long getPlanRiskId() {
        return planRiskId;
    }

    public void setPlanRiskId(Long id) {
        this.planRiskId = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Long> getUnits() {
        return units;
    }

    public void setUnits(List<Long> units) {
        this.units = units;
    }

    public List<Long> getSubunits() {
        return subunits;
    }

    public void setSubunits(List<Long> subs) {
        this.subunits = subs;
    }

    public RiskFilterParams getRiskFilters() {
        return riskFilters;
    }

    public void setRiskFilters(RiskFilterParams rf) {
        this.riskFilters = rf;
    }

    public Integer getSelectedYear() {
        return selectedYear;
    }

    public void setSelectedYear(Integer yr) {
        this.selectedYear = yr;
    }
}
