package org.forpdi.dashboard.indicators;

import java.util.Map;

public class CompanyAccessHistory {
	public static final int FPDI_ACCESS_IDX = 0;
	public static final int FRISCO_ACCESS_IDX = 1;

	private Long companyId;
	private Map<String, Integer[]> history;
	
	public CompanyAccessHistory(Long companyId, Map<String, Integer[]> history) {
		this.companyId = companyId;
		this.history = history;
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public Map<String, Integer[]> getHistory() {
		return history;
	}

	public void setHistory(Map<String, Integer[]> history) {
		this.history = history;
	}
}
