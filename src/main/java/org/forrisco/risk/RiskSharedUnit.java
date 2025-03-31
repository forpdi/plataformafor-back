package org.forrisco.risk;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.forpdi.core.common.SimpleIdentifiable;
import org.forrisco.core.unit.Unit;


@Entity(name = RiskSharedUnit.TABLE)
@Table(name = RiskSharedUnit.TABLE)
public class RiskSharedUnit implements SimpleIdentifiable {
	private static final long serialVersionUID = 1L;
	public static final String TABLE = "frisco_risk_shared_unit";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@JoinColumn(name="risk_id") 
	@ManyToOne(targetEntity=Risk.class, fetch=FetchType.EAGER)
	private Risk risk;

	@JoinColumn(name="unit_id") 
	@ManyToOne(targetEntity=Unit.class, fetch=FetchType.EAGER)
	private Unit unit;
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Risk getRisk() {
		return risk;
	}

	public void setRisk(Risk risk) {
		this.risk = risk;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}
}
