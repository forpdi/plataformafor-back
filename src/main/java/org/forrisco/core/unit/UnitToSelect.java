package org.forrisco.core.unit;

import java.io.Serializable;

public class UnitToSelect implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private String name;
	private String abbreviation;
	private Long parentId;
	
	public UnitToSelect(long id, String name,String abbreviation, Long parentId) {
		this.id = id;
		this.name = name;
		this.abbreviation = abbreviation;
		this.parentId = parentId;
	}
	public UnitToSelect() {
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
}
