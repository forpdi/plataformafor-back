package org.forpdi.planning.fields.budget;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.planning.structure.StructureLevelInstance;


@Entity(name = Budget.TABLE)
@Table(name = Budget.TABLE)
public class Budget extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_budget";
	private static final long serialVersionUID = 1L;

	@Column(nullable = false, unique = false)
	private String subAction;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(nullable = true)
	private Double committed; // empenhado

	@Column(nullable = true)
	private Double realized; // realizado

	private Date creation = new Date();

	@ManyToOne(targetEntity = BudgetElement.class, optional = false, fetch = FetchType.EAGER)
	private BudgetElement budgetElement;

	@SkipSerialization
	@ManyToOne(targetEntity = StructureLevelInstance.class, optional = false, fetch = FetchType.EAGER)
	private StructureLevelInstance levelInstance;
	
	@Transient private long exportBudgetElementId;
	@Transient private long exportStructureLevelInstanceId;
	@Transient private Long exportPlanId;
	
	public String getSubAction() {
		return subAction;
	}

	public void setSubAction(String subAction) {
		this.subAction = subAction;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getCommitted() {
		return committed;
	}

	public void setCommitted(Double committed) {
		this.committed = committed;
	}

	public Double getRealized() {
		return realized;
	}

	public void setRealized(Double realized) {
		this.realized = realized;
	}

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public BudgetElement getBudgetElement() {
		return budgetElement;
	}

	public void setBudgetElement(BudgetElement budgetElement) {
		this.budgetElement = budgetElement;
	}

	public StructureLevelInstance getLevelInstance() {
		return levelInstance;
	}

	public void setLevelInstance(StructureLevelInstance levelInstance) {
		this.levelInstance = levelInstance;
	}
	
	public long getExportBudgetElementId() {
		return exportBudgetElementId;
	}

	public void setExportBudgetElementId(long exportBudgetElementId) {
		this.exportBudgetElementId = exportBudgetElementId;
	}
	
	public long getExportStructureLevelInstanceId() {
		return exportStructureLevelInstanceId;
	}

	public void setExportStructureLevelInstanceId(long exportStructureLevelInstanceId) {
		this.exportStructureLevelInstanceId = exportStructureLevelInstanceId;
	}

	public Long getExportPlanId() {
		return exportPlanId;
	}

	public void setExportPlanId(Long exportPlanId) {
		this.exportPlanId = exportPlanId;
	}
}