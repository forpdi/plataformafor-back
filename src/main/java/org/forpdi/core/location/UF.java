package org.forpdi.core.location;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.forpdi.core.common.SimpleEntity;

@Entity(name = UF.TABLE)
@Table(name = UF.TABLE)
public class UF extends SimpleEntity {
	public static final String TABLE = "fpdi_uf";
	private static final long serialVersionUID = 1L;

	@Column(nullable=false, length=255)
	private String name;

	@Column(nullable=false, length=255)
	private String acronym;
	
	@ManyToOne(targetEntity=Region.class, fetch=FetchType.EAGER, optional=false)
	private Region region;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}
}
