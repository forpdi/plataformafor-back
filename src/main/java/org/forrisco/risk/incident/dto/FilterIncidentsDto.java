package org.forrisco.risk.incident.dto;

import java.util.Date;
import java.util.List;

public class FilterIncidentsDto {
    private Long planRiskId;
    private List<Long> unitIds;
    private String incident;
    private Integer type;
    private Date startCreation;
    private Date endCreation;
    private Integer page = 1;
    private Integer pageSize = 50;

    public Long getPlanRiskId() { return planRiskId; }
    public void setPlanRiskId(Long planRiskId) { this.planRiskId = planRiskId; }

    public List<Long> getUnitIds() { return unitIds; }
    public void setUnitIds(List<Long> unitIds) { this.unitIds = unitIds; }

    public String getIncident() { return incident; }
    public void setIncident(String incident) { this.incident = incident; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public Date getStartCreation() { return startCreation; }
    public void setStartCreation(Date startCreation) { this.startCreation = startCreation; }

    public Date getEndCreation() { return endCreation; }
    public void setEndCreation(Date endCreation) { this.endCreation = endCreation; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
