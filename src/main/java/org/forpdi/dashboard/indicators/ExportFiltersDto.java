package org.forpdi.dashboard.indicators;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.forpdi.core.common.GeneralUtils;

public record ExportFiltersDto(
		long regionId,
		long typeId,
		List<Long> companyIds,
		String companyCreationBegin,
		String companyCreationEnd) {
	
	public Date companyCreationBeginAsDate() {
		if (companyCreationBegin != null) {
			return GeneralUtils.parseDate(companyCreationBegin, new SimpleDateFormat("dd/MM/yyyy"));
		}
		
		return null;
	}
	
	public Date companyCreationEndAsDate() {
		if (companyCreationEnd != null) {
			return GeneralUtils.parseDate(companyCreationEnd, new SimpleDateFormat("dd/MM/yyyy"));
		}
		
		return null;
	}
}
