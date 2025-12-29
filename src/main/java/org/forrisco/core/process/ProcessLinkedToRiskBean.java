package org.forrisco.core.process;

import java.io.Serializable;
import java.util.Objects;

public class ProcessLinkedToRiskBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private Long riskId;
	private Long unitId;
	
	public ProcessLinkedToRiskBean(Long id, String name, Long riskId, Long unitId) {
		this.id = id;
		this.name = name;
		this.riskId = riskId;
		this.unitId = unitId;
	}
	public ProcessLinkedToRiskBean() {
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getRiskId() {
		return riskId;
	}
	public void setRiskId(Long riskId) {
		this.riskId = riskId;
	}
	public Long getUnitId() {
		return unitId;
	}
	public void setUnitId(Long unitId) {
		this.unitId = unitId;
	}
	@Override
	public int hashCode() {
		return Objects.hash(id, riskId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessLinkedToRiskBean other = (ProcessLinkedToRiskBean) obj;
		return Objects.equals(id, other.id) && Objects.equals(riskId, other.riskId);
	}
}
