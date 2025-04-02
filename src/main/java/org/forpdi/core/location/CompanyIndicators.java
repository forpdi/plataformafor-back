package org.forpdi.core.location;

import java.util.Date;

import org.forpdi.core.company.Company;

public class CompanyIndicators {
	private Long id;
	private String name;
	private String initials;
	private Date creation;
	private long planMacrosCount;
	private long planRisksCount;
	
	public CompanyIndicators(Company company, long planMacrosCount, long planRisksCount) {
		this.id = company.getId();
		this.name = company.getName();
		this.initials = company.getInitials();
		this.creation = company.getCreation();
		this.planMacrosCount = planMacrosCount;
		this.planRisksCount = planRisksCount;
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
	public String getInitials() {
		return initials;
	}
	public void setInitials(String initials) {
		this.initials = initials;
	}
	public Date getCreation() {
		return creation;
	}
	public void setCreation(Date creation) {
		this.creation = creation;
	}
	public long getPlanMacrosCount() {
		return planMacrosCount;
	}
	public void setPlanMacrosCount(long planMacrosCount) {
		this.planMacrosCount = planMacrosCount;
	}
	public long getPlanRisksCount() {
		return planRisksCount;
	}
	public void setPlanRisksCount(long planRisksCount) {
		this.planRisksCount = planRisksCount;
	}
	
	public static final class Builder {
		private Company company;
		private long planMacrosCount;
		private long planRisksCount;

		public Builder company(Company company) {
			this.company = company;
			return this;
		}
		
		public Builder planMacrosCount(long planMacrosCount) {
			this.planMacrosCount = planMacrosCount;
			return this;
		}
		
		public Builder planRisksCount(long planRisksCount) {
			this.planRisksCount = planRisksCount;
			return this;
		}
		public CompanyIndicators build() {
			return new CompanyIndicators(company, planMacrosCount, planRisksCount);
		}
	}
}
