package org.forpdi.planning.structure;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.company.Company;

/**
 * @author Renato R. R. de Oliveira
 * 
 */
@Entity(name = Structure.TABLE)
@Table(name = Structure.TABLE)
public class Structure extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_structure";
	private static final long serialVersionUID = 1L;

	@Column(nullable = false, length=255)
	private String name;

	@Column(nullable = true, length=10000)
	private String description;
	
	@SkipSerialization
	@ManyToOne(targetEntity=Company.class, optional=true, fetch=FetchType.EAGER)
	private Company company;

	@Transient
	private List<StructureLevel> levels;
	
	@Transient
	private Long exportCompanyId;

	@Transient
	private Boolean structureIsDuplicated;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<StructureLevel> getLevels() {
		return levels;
	}

	public void setLevels(List<StructureLevel> levels) {
		this.levels = levels;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}
	
	public Long getExportCompanyId() {
		return exportCompanyId;
	}

	public void setExportCompanyId(Long exportCompanyId) {
		this.exportCompanyId = exportCompanyId;
	}

	public Boolean isStructureDuplicated() {
		return structureIsDuplicated;
	}

	public void setStructureIsDuplicated(Boolean structureIsDuplicated) {
		this.structureIsDuplicated = structureIsDuplicated;
	}
}