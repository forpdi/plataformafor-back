package org.forpdi.planning.plan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDIFilterParams {
    private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    
    private Long macro;
    private Long plan;
    private Long levelInstance;
    private Long goalId;
    private String startDate;
    private String endDate;
    private Boolean goalStatus;
    private Integer page;
    private Integer pageSize;
	private Integer progressStatus;

    public Long getMacro() { return macro; }
    public void setMacro(Long macro) { this.macro = macro; }

    public Long getPlan() { return plan; }
    public void setPlan(Long plan) { this.plan = plan; }

    public Long getLevelInstance() { return levelInstance; }
    public void setLevelInstance(Long levelInstance) { this.levelInstance = levelInstance; }

    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Boolean getGoalStatus() { return goalStatus; }
    public void setGoalStatus(Boolean goalStatus) { this.goalStatus = goalStatus; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

    public Integer getProgressStatus() { return progressStatus; }
    public void setProgressStatus(Integer progressStatus) { this.progressStatus = progressStatus; }

    private Date parseDate(String dateStr) throws ParseException {
        if (dateStr != null && !dateStr.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            sdf.setLenient(false);
            return sdf.parse(dateStr);
        }
        return null;
    }

    public Date getStartDateAsDate() throws ParseException {
        return parseDate(this.startDate);
    }

    public Date getEndDateAsDate() throws ParseException {
        return parseDate(this.endDate);
    }

    public boolean filteringByMacro() {
        return macro != null;
    }

    public boolean filteringByPlan() {
        return plan != null;
    }

    public boolean filteringByLevelInstance() {
        return levelInstance != null;
    }

    public boolean filteringByGoalId() {
        return goalId != null;
    }

    public boolean filteringByStartDate() {
        return startDate != null && !startDate.isEmpty();
    }

    public boolean filteringByEndDate() {
        return endDate != null && !endDate.isEmpty();
    }

    public boolean filteringByGoalStatus() {
        return goalStatus != null;
    }
    
    public void validateParams() {
		if (macro == null) {
			throw new IllegalArgumentException("Você deve selecionar um plano");
		}
		if (plan == null) {
			throw new IllegalArgumentException("Você deve selecionar um plano de ação");
		}
		if (levelInstance == null) {
			throw new IllegalArgumentException("Você deve selecionar um eixo temático");
		}
    }
}
