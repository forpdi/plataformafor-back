package org.forpdi.dashboard.indicators;

import org.forpdi.core.company.Company;

public class CompanyForriscoIndicators {
	private Company company;
	private long policies;
	private long planRisks;
	private long risks;
	private long monitoredRisks;
	
	public Company getCompany() {
		return company;
	}
	public void setCompany(Company company) {
		this.company = company;
	}
	public long getPolicies() {
		return policies;
	}
	public void setPolicies(long policies) {
		this.policies = policies;
	}
	public long getPlanRisks() {
		return planRisks;
	}
	public void setPlanRisks(long planRisks) {
		this.planRisks = planRisks;
	}
	public long getRisks() {
		return risks;
	}
	public void setRisks(long risks) {
		this.risks = risks;
	}
	public long getMonitoredRisks() {
		return monitoredRisks;
	}
	public void setMonitoredRisks(long monitoredRisks) {
		this.monitoredRisks = monitoredRisks;
	}
}
