package org.forpdi.dashboard.admin;

import java.io.Serializable;

public class PlanDetails implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private long numberOfPlans;
	private Long numberOfObjectives;
	private Long numberOfIndicators;
	private long numberOfGoals;
	private long numberOfIndicatorsThematicAxis;
	private Double goalsDelayedPerCent;
	private long numberOfBudgets;
	
	public PlanDetails(){
		this.numberOfBudgets = 0;
		this.numberOfGoals = 0;
		this.numberOfIndicators = 0L;
		this.numberOfObjectives = 0L;
		this.goalsDelayedPerCent = 0.0;
		this.numberOfIndicatorsThematicAxis = 0;
	}
	
	public long getNumberOfPlans() {
		return numberOfPlans;
	}
	public void setNumberOfPlans(long numberOfPlans) {
		this.numberOfPlans = numberOfPlans;
	}
	public Long getNumberOfObjectives() {
		return numberOfObjectives;
	}
	public void setNumberOfObjectives(Long numberOfObjectives) {
		this.numberOfObjectives = numberOfObjectives;
	}
	public Long getNumberOfIndicators() {
		return numberOfIndicators;
	}
	public void setNumberOfIndicators(Long numberOfIndicators) {
		this.numberOfIndicators = numberOfIndicators;
	}
	public long getNumberOfGoals() {
		return numberOfGoals;
	}
	public void setNumberOfGoals(long numberOfGoals) {
		this.numberOfGoals = numberOfGoals;
	}
	public Double getGoalsDelayedPerCent() {
		return goalsDelayedPerCent;
	}
	public void setGoalsDelayedPerCent(Double goalsDelayedPerCent) {
		this.goalsDelayedPerCent = goalsDelayedPerCent;
	}
	public long getNumberOfBudgets() {
		return numberOfBudgets;
	}
	public void setNumberOfBudgets(long numberOfBudgets) {
		this.numberOfBudgets = numberOfBudgets;
	}
	
	public long getNumberOfThematicAxis() {
		return numberOfIndicatorsThematicAxis;
	}
	public void setNumberOfThematicAxis(long numberOfIndicatorsThematicAxis) {
		this.numberOfIndicatorsThematicAxis = numberOfIndicatorsThematicAxis;
	}
	
	
}
