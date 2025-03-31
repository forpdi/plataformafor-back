package org.forrisco.risk.monitor;

import java.io.Serializable;

public class MonitorHistoryBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private long unitId;
	private String estado;
	private int month;
	private int year;
	private int quantity;
	
	public MonitorHistoryBean(long id, long unitId, String estado, int month, int year, int quantity) {
		this.id = id;
		this.unitId = unitId;
		this.estado = estado;
		this.month = month;
		this.year = year;
		this.quantity = quantity;
	}
	public MonitorHistoryBean() {
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getUnitId() {
		return unitId;
	}
	public void setUnitId(long unitId) {
		this.unitId = unitId;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
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
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
