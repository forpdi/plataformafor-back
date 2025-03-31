package org.forrisco.risk.incident;

import java.io.Serializable;
import java.util.Date;

public class IncidentBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private long riskId;
	private long unitId;
	private String description;
	private int type;
	private Date begin;

	public IncidentBean(long id, long riskId, long unitId, String description, int type, Date begin) {
		super();
		this.id = id;
		this.riskId = riskId;
		this.unitId = unitId;
		this.description = description;
		this.type = type;
		this.begin = begin;
	}
	public IncidentBean() {
		super();
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getRiskId() {
		return riskId;
	}
	public void setRiskId(long riskId) {
		this.riskId = riskId;
	}
	public long getUnitId() {
		return unitId;
	}
	public void setUnitId(long unitId) {
		this.unitId = unitId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Date getBegin() {
		return begin;
	}
	public void setBegin(Date begin) {
		this.begin = begin;
	}
}
