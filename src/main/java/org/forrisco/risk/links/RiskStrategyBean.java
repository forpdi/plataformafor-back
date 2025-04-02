package org.forrisco.risk.links;

import java.io.Serializable;

public class RiskStrategyBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private String name;
	private long structureId;
	
	public RiskStrategyBean(long id, String name, long structureId) {
		this.id = id;
		this.name = name;
		this.structureId = structureId;
	}
	
	public RiskStrategyBean() {
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
	public long getStructureId() {
		return structureId;
	}
	public void setStructureId(long structureId) {
		this.structureId = structureId;
	}
}
