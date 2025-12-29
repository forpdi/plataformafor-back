package org.forpdi.dashboard;

import java.util.Date;

public class GoalsParams {
	private Double max;
	private Double min;
	private Double exp;
	private Double reach;
	private Date finish;
	public Double getMax() {
		return max;
	}
	public void setMax(Double max) {
		this.max = max;
	}
	public Double getMin() {
		return min;
	}
	public void setMin(Double min) {
		this.min = min;
	}
	public Double getExp() {
		return exp;
	}
	public void setExp(Double exp) {
		this.exp = exp;
	}
	public Double getReach() {
		return reach;
	}
	public void setReach(Double reach) {
		this.reach = reach;
	}
	public Date getFinish() {
		return finish;
	}
	public void setFinish(Date finish) {
		this.finish = finish;
	}

	public static class GoalsParamsBuilder {
		private Double max;
		private Double min;
		private Double exp;
		private Double reach;
		private Date finish;
		
		public GoalsParamsBuilder max(Double max) {
			this.max = max;
			return this;
		}
		public GoalsParamsBuilder min(Double min) {
			this.min = min;
			return this;
		}
		public GoalsParamsBuilder exp(Double exp) {
			this.exp = exp;
			return this;
		}
		public GoalsParamsBuilder reach(Double reach) {
			this.reach = reach;
			return this;
		}
		public GoalsParamsBuilder finish(Date finish) {
			this.finish = finish;
			return this;
		}
		public GoalsParams create() {
			GoalsParams goalsValues = new GoalsParams();
			goalsValues.setMax(max);
			goalsValues.setMin(min);
			goalsValues.setExp(exp);
			goalsValues.setReach(reach);
			goalsValues.setFinish(finish);
			return goalsValues;
		}
	}
}
