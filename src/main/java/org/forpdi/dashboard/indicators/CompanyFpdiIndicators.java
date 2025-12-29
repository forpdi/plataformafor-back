package org.forpdi.dashboard.indicators;

import org.forpdi.core.company.Company;

public class CompanyFpdiIndicators {

	private Company company;
	private long planMacros;
	private long plans;
	private long axes;
	private long objectives;
	private long indicators;
	private long goals;

	public Company getCompany() {
		return company;
	}
	public void setCompany(Company company) {
		this.company = company;
	}
	public long getPlanMacros() {
		return planMacros;
	}
	public void setPlanMacros(long planMacros) {
		this.planMacros = planMacros;
	}
	public long getPlans() {
		return plans;
	}
	public void setPlans(long plans) {
		this.plans = plans;
	}
	public long getAxes() {
		return axes;
	}
	public void setAxes(long axes) {
		this.axes = axes;
	}
	public long getObjectives() {
		return objectives;
	}
	public void setObjectives(long objectives) {
		this.objectives = objectives;
	}
	public long getIndicators() {
		return indicators;
	}
	public void setIndicators(long indicators) {
		this.indicators = indicators;
	}
	public long getGoals() {
		return goals;
	}
	public void setGoals(long goals) {
		this.goals = goals;
	}
}
