package org.forrisco.risk.preventiveaction;

import java.io.Serializable;
import java.util.Date;

public class PreventiveActionBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private long riskId;
	private String riskName;
	private String riskType;
	private long unitId;
	private String action;
	private boolean accomplished;
	private Date validityBegin;

	public PreventiveActionBean(long id, long riskId, String riskName, String riskType, long unitId, String action,
			boolean accomplished, Date validityBegin) {
		this.id = id;
		this.riskId = riskId;
		this.riskName = riskName;
		this.riskType = riskType;
		this.unitId = unitId;
		this.action = action;
		this.accomplished = accomplished;
		this.validityBegin = validityBegin;
	}
	public PreventiveActionBean() {
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
	public String getRiskName() {
		return riskName;
	}
	public void setRiskName(String riskName) {
		this.riskName = riskName;
	}
	public String getRiskType() {
		return riskType;
	}
	public void setRiskType(String riskType) {
		this.riskType = riskType;
	}
	public long getUnitId() {
		return unitId;
	}
	public void setUnitId(long unitId) {
		this.unitId = unitId;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public boolean isAccomplished() {
		return accomplished;
	}
	public void setAccomplished(boolean accomplished) {
		this.accomplished = accomplished;
	}
	public Date getValidityBegin() {
		return validityBegin;
	}
	public void setValidityBegin(Date validityBegin) {
		this.validityBegin = validityBegin;
	}
}
