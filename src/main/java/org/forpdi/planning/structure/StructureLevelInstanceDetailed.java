package org.forpdi.planning.structure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;


/**
 * @author Rodrigo de Freitas Santos
 * 
 */
@Entity(name = StructureLevelInstanceDetailed.TABLE)
@Table(name = StructureLevelInstanceDetailed.TABLE)
public class StructureLevelInstanceDetailed extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_structure_level_instance_detailed";
	private static final long serialVersionUID = 1L;

	@Column(nullable = false)
	private int month;

	@Column(nullable = false)
	private int year;

	@ManyToOne(targetEntity = StructureLevelInstance.class, optional = false, fetch = FetchType.EAGER)
	private StructureLevelInstance levelInstance;

	@Transient
	private Long exportStructureLevelInstanceId;

	private Double levelValue;
	private Double levelMinimum;
	private Double levelMaximum;

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

	public StructureLevelInstance getLevelInstance() {
		return levelInstance;
	}

	public void setLevelInstance(StructureLevelInstance levelInstance) {
		this.levelInstance = levelInstance;
	}

	public Double getLevelValue() {
		return levelValue;
	}

	public void setLevelValue(Double levelValue) {
		this.levelValue = levelValue;
	}

	public Double getLevelMinimum() {
		return levelMinimum;
	}

	public void setLevelMinimum(Double levelMinimum) {
		this.levelMinimum = levelMinimum;
	}

	public Double getLevelMaximum() {
		return levelMaximum;
	}

	public void setLevelMaximum(Double levelMaximum) {
		this.levelMaximum = levelMaximum;
	}

	public Long getExportStructureLevelInstanceId() {
		return exportStructureLevelInstanceId;
	}

	public void setExportStructureLevelInstanceId(Long exportStructureLevelInstanceId) {
		this.exportStructureLevelInstanceId = exportStructureLevelInstanceId;
	}

}
