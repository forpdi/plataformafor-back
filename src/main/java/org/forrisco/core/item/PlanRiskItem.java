package org.forrisco.core.item;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forrisco.core.plan.PlanRisk;

/**
 * @author Juliano Afonso
 * 
 */
@Entity(name = PlanRiskItem.TABLE)
@Table(name = PlanRiskItem.TABLE)

public class PlanRiskItem extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_plan_risk_item";
	private static final long serialVersionUID = 1L;
	
	@Column(nullable = false, length=400)
	private String name;
	
	@Column(nullable = true, length=10000)
	private String description;
	
	@SkipSerialization
	@ManyToOne(targetEntity=PlanRisk.class, optional=false, fetch=FetchType.EAGER)
	private PlanRisk planRisk;
	
	@Transient
	private List<PlanRiskItemField> fields;
	
	@Transient
	private List<PlanRiskSubItem> subitems;

	@Transient
	private boolean hasFile;

	@Transient
	private boolean hasText;

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public PlanRisk getPlanRisk() {
		return planRisk;
	}

	public void setPlanRisk(PlanRisk planRisk) {
		this.planRisk = planRisk;
	}

	public List<PlanRiskItemField> getPlanRiskItemField() {
		return fields;
	}

	public void setPlanRiskItemField(List<PlanRiskItemField> planRiskItemField) {
		this.fields = planRiskItemField;
	}
	
	public List<PlanRiskSubItem> getSubitems() {
		return subitems;
	}

	public void setSubitems(List<PlanRiskSubItem> subitems) {
		this.subitems = subitems;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setHasText(boolean hasText) {
		this.hasText = hasText;
	}

	public void setHasFile(boolean hasFile) {
		this.hasFile = hasFile;
	}
	
	public boolean hasFile() {
		return hasFile;
	}
	
	public boolean hasText() {
		return hasText;
	}
}