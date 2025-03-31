package org.forpdi.dashboard.goalsinfo;

import java.io.Serializable;
import java.util.Date;

public class AttributeInstanceToGoalsInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private Double valueAsNumber;
	private Date valueAsDate;
	private Boolean finishDate;
	private Boolean expectedField;
	private Boolean reachedField;
	private Boolean minimumField;
	private Boolean maximumField;
	private Long levelInstanceId;
	
	public AttributeInstanceToGoalsInfo(Double valueAsNumber, Date valueAsDate, Boolean finishDate,
			Boolean expectedField, Boolean reachedField, Boolean minimumField, Boolean maximumField,
			Long levelInstanceId) {
		this.valueAsNumber = valueAsNumber;
		this.valueAsDate = valueAsDate;
		this.finishDate = finishDate;
		this.expectedField = expectedField;
		this.reachedField = reachedField;
		this.minimumField = minimumField;
		this.maximumField = maximumField;
		this.levelInstanceId = levelInstanceId;
	}
	
	public AttributeInstanceToGoalsInfo() {
	}

	public Double getValueAsNumber() {
		return valueAsNumber;
	}
	public void setValueAsNumber(Double valueAsNumber) {
		this.valueAsNumber = valueAsNumber;
	}
	public Date getValueAsDate() {
		return valueAsDate;
	}
	public void setValueAsDate(Date valueAsDate) {
		this.valueAsDate = valueAsDate;
	}
	public Boolean isFinishDate() {
		return finishDate;
	}
	public void setFinishDate(Boolean finishDate) {
		this.finishDate = finishDate;
	}
	public Boolean isExpectedField() {
		return expectedField;
	}
	public void setExpectedField(Boolean expectedField) {
		this.expectedField = expectedField;
	}
	public Boolean isReachedField() {
		return reachedField;
	}
	public void setReachedField(Boolean reachedField) {
		this.reachedField = reachedField;
	}
	public Boolean isMinimumField() {
		return minimumField;
	}
	public void setMinimumField(Boolean minimumField) {
		this.minimumField = minimumField;
	}
	public Boolean isMaximumField() {
		return maximumField;
	}
	public void setMaximumField(Boolean maximumField) {
		this.maximumField = maximumField;
	}
	public Long getLevelInstanceId() {
		return levelInstanceId;
	}
	public void setLevelInstanceId(Long levelInstanceId) {
		this.levelInstanceId = levelInstanceId;
	}
}
