package org.forrisco.risk;

import java.io.Serializable;

public class RiskHistoryBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private long riskLevelId;
	private long unitId;
	private int month;
	private int year;
	private boolean threat;
	private int quantity;

	public RiskHistoryBean(long id, long riskLevelId, long unitId, int month, int year, boolean threat, int quantity) {
		this.id = id;
		this.riskLevelId = riskLevelId;
		this.unitId = unitId;
		this.month = month;
		this.year = year;
		this.threat = threat;
		this.quantity = quantity;
	}
	public RiskHistoryBean() {
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getRiskLevelId() {
		return riskLevelId;
	}
	public void setRiskLevelId(long riskLevelId) {
		this.riskLevelId = riskLevelId;
	}
	public long getUnitId() {
		return unitId;
	}
	public void setUnitId(long unitId) {
		this.unitId = unitId;
	}
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public boolean isThreat() {
		return threat;
	}
	public void setThreat(boolean threat) {
		this.threat = threat;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
