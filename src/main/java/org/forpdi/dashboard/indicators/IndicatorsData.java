package org.forpdi.dashboard.indicators;

import java.io.Serializable;
import java.util.List;

import org.forpdi.core.location.CompanyIndicators;

public class IndicatorsData implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<RegionCounts> regionsCounts;
	
	private List<CompanyAccessHistory> companiesAccessHistory;
	
	private List<CompanyFpdiIndicators> companiesFpdiIndicators;
	
	private List<CompanyForriscoIndicators> companiesFriscoIndicators;
	
	private List<CompanyIndicators> companiesIndicators;

	public List<RegionCounts> getRegionsCounts() {
		return regionsCounts;
	}

	public void setRegionsCounts(List<RegionCounts> regionsCounts) {
		this.regionsCounts = regionsCounts;
	}

	public List<CompanyAccessHistory> getCompaniesAccessHistory() {
		return companiesAccessHistory;
	}

	public void setCompaniesAccessHistory(List<CompanyAccessHistory> companiesAccessHistory) {
		this.companiesAccessHistory = companiesAccessHistory;
	}

	public List<CompanyFpdiIndicators> getCompaniesFpdiIndicators() {
		return companiesFpdiIndicators;
	}

	public void setCompaniesFpdiIndicators(List<CompanyFpdiIndicators> companiesFpdiIndicators) {
		this.companiesFpdiIndicators = companiesFpdiIndicators;
	}

	public List<CompanyForriscoIndicators> getCompaniesFriscoIndicators() {
		return companiesFriscoIndicators;
	}

	public void setCompaniesFriscoIndicators(List<CompanyForriscoIndicators> companiesFriscoIndicators) {
		this.companiesFriscoIndicators = companiesFriscoIndicators;
	}

	public List<CompanyIndicators> getCompaniesIndicators() {
		return companiesIndicators;
	}

	public void setCompaniesIndicators(List<CompanyIndicators> companiesIndicators) {
		this.companiesIndicators = companiesIndicators;
	}
}