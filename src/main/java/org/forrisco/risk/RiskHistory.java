package org.forrisco.risk;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forrisco.core.unit.Unit;

/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = RiskHistory.TABLE)
@Table(name = RiskHistory.TABLE)
public class RiskHistory extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_risk_history";
	private static final long serialVersionUID = 1L;

	
	@ManyToOne(targetEntity=RiskLevel.class, optional=false, fetch=FetchType.EAGER)
	private RiskLevel riskLevel;

	@ManyToOne(targetEntity=Unit.class, optional=false, fetch=FetchType.EAGER)
	private Unit unit;
	
	@Column(nullable=false)
	private int month;

	@Column(nullable=false)
	private int year;
	
	@Column(nullable=false)
	private boolean threat;

	@Column(nullable=false)
	private int quantity;

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

	public RiskLevel getRiskLevel() {
		return riskLevel;
	}

	public void setRiskLevel(RiskLevel riskLevel) {
		this.riskLevel = riskLevel;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public boolean isThreat() {
		return threat;
	}

	public void setThreat(boolean threat) {
		this.threat = threat;
	}

	
}
