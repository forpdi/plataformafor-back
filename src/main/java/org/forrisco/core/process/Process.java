package org.forrisco.core.process;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.company.Company;
import org.forpdi.core.storage.file.Archive;
import org.forrisco.core.unit.Unit;

/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = Process.TABLE)
@Table(name = Process.TABLE)

public class Process extends SimpleLogicalDeletableEntity implements Serializable {
	public static final String TABLE = "frisco_process";
	private static final long serialVersionUID = 1L;

	@SkipSerialization
	@ManyToOne(targetEntity=Company.class,  fetch=FetchType.EAGER)
	private Company company;
	
	@ManyToOne(targetEntity=Archive.class,  fetch=FetchType.EAGER)
	private Archive file;
	
	@ManyToOne(targetEntity=Unit.class,  fetch=FetchType.EAGER)
	private Unit unitCreator;
	
	
	@Column(nullable=false, length=255)
	private String name;

	@Column(nullable=true, length=4000)
	@Deprecated
	private String objective;
	
	@Column(length=4000)
	private String fileLink;
	
	
	@Transient
	private Unit unit;
	
	@Transient
	private List<Unit> relatedUnits;
	
	@Transient
	private List<ProcessObjective> allObjectives;
	
	
	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Deprecated
	public String getObjective() {
		return objective;
	}

	@Deprecated
	public void setObjective(String objective) {
		this.objective = objective;
	}

	public String getFileLink() {
		return fileLink;
	}

	public void setFileLink(String fileLink) {
		this.fileLink = fileLink;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public List<Unit> getRelatedUnits() {
		return relatedUnits;
	}

	public void setRelatedUnits(List<Unit> relatedUnits) {
		this.relatedUnits = relatedUnits;
	}

	public Archive getFile() {
		return file;
	}

	public void setFile(Archive file) {
		this.file = file;
	}

	public Unit getUnitCreator() {
		return unitCreator;
	}

	public void setUnitCreator(Unit unitCreator) {
		this.unitCreator = unitCreator;
	}
	
	public List<ProcessObjective> getAllObjectives() {
		return allObjectives;
	}

	public void setAllObjectives(List<ProcessObjective> allObjectives) {
		if (allObjectives != null) {
			for (ProcessObjective objective : allObjectives) {
				objective.setProcess(null);
			}
		}
		this.allObjectives = allObjectives;
	}
	
	public List<String> getRelatedUnitNames() {
		if (relatedUnits == null) {
			return Collections.emptyList();
		}
		
		return relatedUnits
				.stream()
				.map(unit -> unit.getName())
				.collect(Collectors.toList());
	}
	
	public List<String> getProcessObjectivesDescriptions() {
		if (allObjectives == null) {
			return Collections.emptyList();
		}
		
		return allObjectives
				.stream()
				.map(objective -> objective.getDescription())
				.collect(Collectors.toList());
	}
	
	public String getProcessObjectivesDescriptionsString() {
		if (allObjectives == null) {
			return "";
		}
		
		return String.join(", ", this.getProcessObjectivesDescriptions());
	}
 }
