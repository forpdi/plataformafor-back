package org.forrisco.risk;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.forpdi.core.common.PaginatedList;
import org.forrisco.risk.links.RiskProcessObjective;
import org.forrisco.risk.links.RiskStrategyBean;

public class RiskBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private long userId;
	private Long managerId;
	private long unitId;
	private RiskLevel riskLevel;
	private String name;
	private String probability;
	private String impact;
	private String periodicity;
	private String tipology;
	private String otherTipologies;
	private String type;
	private Date begin;
	private Integer response;
	private int monitoringState;
	private PaginatedList<RiskStrategyBean> strategies;
	
	public RiskBean(long id, long userId, Long managerId, long unitId, RiskLevel riskLevel, String name,
			String probability, String impact, String periodicity, String tipology, String otherTipologies, String type,
			Date begin, Integer response) {
		this.id = id;
		this.userId = userId;
		this.managerId = managerId;
		this.unitId = unitId;
		this.riskLevel = riskLevel;
		this.name = name;
		this.probability = probability;
		this.impact = impact;
		this.periodicity = periodicity;
		this.tipology = tipology;
		this.otherTipologies = otherTipologies;
		this.type = type;
		this.begin = begin;
		this.response = response;
	}
	
	public RiskBean() {
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public Long getManagerId() {
		return managerId;
	}
	public void setManagerId(Long managerId) {
		this.managerId = managerId;
	}
	public long getUnitId() {
		return unitId;
	}
	public void setUnitId(long unitId) {
		this.unitId = unitId;
	}
	public RiskLevel getRiskLevel() {
		return riskLevel;
	}
	public void setRiskLevel(RiskLevel riskLevel) {
		this.riskLevel = riskLevel;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getProbability() {
		return probability;
	}
	public void setProbability(String probability) {
		this.probability = probability;
	}
	public String getImpact() {
		return impact;
	}
	public void setImpact(String impact) {
		this.impact = impact;
	}
	public String getPeriodicity() {
		return periodicity;
	}
	public void setPeriodicity(String periodicity) {
		this.periodicity = periodicity;
	}
	public String getTipology() {
		return tipology;
	}
	public void setTipology(String tipology) {
		this.tipology = tipology;
	}
	public String getOtherTipologies() {
		return otherTipologies;
	}
	public void setOtherTipologies(String otherTipologies) {
		this.otherTipologies = otherTipologies;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getBegin() {
		return begin;
	}
	public void setBegin(Date begin) {
		this.begin = begin;
	}
	public Integer getResponse() {
		return response;
	}
	public void setResponse(Integer response) {
		this.response = response;
	}
	public int getMonitoringState() {
		return monitoringState;
	}
	public void setMonitoringState(int monitoringState) {
		this.monitoringState = monitoringState;
	}

	public PaginatedList<RiskStrategyBean> getStrategies() {
		return strategies;
	}

	public void setStrategies(PaginatedList<RiskStrategyBean> strategies) {
		this.strategies = strategies;
	}
}
