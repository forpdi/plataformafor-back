package org.forrisco.core.unit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;
import org.forrisco.core.plan.PlanRisk;


/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = Unit.TABLE)
@Table(name = Unit.TABLE)

public class Unit extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_unit";
	private static final long serialVersionUID = 1L;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(nullable = false, length = 255)
	private String abbreviation;

	@Column(nullable = true, length = 4000)
	private String description;

	@OneToOne(targetEntity = Unit.class, optional = true, fetch = FetchType.EAGER)
	private Unit parent;

	@ManyToOne(targetEntity = User.class, optional = false, fetch = FetchType.EAGER)
	private User user;

	@ManyToOne(targetEntity = PlanRisk.class, optional = false, fetch = FetchType.EAGER)
	private PlanRisk planRisk;

	@Transient
	private Long riskSearchId;

	public Unit() {
	}

	public Unit(Unit unit) {
		this.name = unit.getName();
		this.abbreviation = unit.getAbbreviation();
		this.description = unit.getDescription();
		this.user = unit.getUser();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Unit getParent() {
		return parent;
	}

	public void setParent(Unit parent) {
		this.parent = parent;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public PlanRisk getPlanRisk() {
		return planRisk;
	}

	public void setPlanRisk(PlanRisk plan) {
		this.planRisk = plan;
	}

	public Long getRiskSearchId() {
		return riskSearchId;
	}

	public void setRiskSearchId(Long riskSearchId) {
		this.riskSearchId = riskSearchId;
	}
	
	public boolean isSubunit() {
		return parent != null;
	}
}