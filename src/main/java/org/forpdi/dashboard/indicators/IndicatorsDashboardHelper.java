package org.forpdi.dashboard.indicators;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.forpdi.core.company.Company;
import org.forpdi.core.utils.Counter;
import org.springframework.stereotype.Component;

@Component
public class IndicatorsDashboardHelper {
	public static final int ALL_CARDS_ID = -1;
	public static final int REGIONS_COUNT_CARD_ID = 1;
	public static final int ACCESS_CARD_ID = 2;
	public static final int INSTITUTIONS_CARD_ID = 3;

	
	public long getTotalUniversities(List<RegionCounts> companiesRegionCounts) {
		return companiesRegionCounts.stream()
				.map(RegionCounts::getUniversityCount)
				.reduce(0L, Long::sum);
	}
	
	public long getTotalInstitutes(List<RegionCounts> companiesRegionCounts) {
		return companiesRegionCounts.stream()
				.map(RegionCounts::getInstituteCount)
				.reduce(0L, Long::sum);
	}

	public long getTotalOthers(List<RegionCounts> companiesRegionCounts) {
		if (companiesRegionCounts == null || companiesRegionCounts.isEmpty()) {
			return 0L;
		}
		return companiesRegionCounts.stream()
			.mapToLong(RegionCounts::getOtherCount)
			.sum();
	}
	
	public AdhesionByYearMonths getAhesionsByYearMonths(List<Company> companies) {
		Map<YearMonth, Counter> adhesionsCountMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
		companies.forEach(c -> {
			Date creation = c.getCreation();
	        calendar.setTime(creation);
	        int year = calendar.get(Calendar.YEAR);
	        int month = calendar.get(Calendar.MONTH) + 1;
			YearMonth yearMonth = YearMonth.of(year, month);
			if (!adhesionsCountMap.containsKey(yearMonth)) {
				adhesionsCountMap.put(yearMonth, new Counter());
			}
			adhesionsCountMap.get(yearMonth).increment();
		});
		
		List<YearMonth> yearMonths = new ArrayList<YearMonth>(adhesionsCountMap.keySet());
		Collections.sort(yearMonths);
		
		return new AdhesionByYearMonths(adhesionsCountMap, yearMonths);
	}
	
    public List<Long> countFpdiIndicators(List<CompanyFpdiIndicators> companyFpdiIndicators) {
        List<Long> values = Arrays.asList(0L, 0L, 0L, 0L, 0L, 0L);

        for(CompanyFpdiIndicators companyFpdiIndicator : companyFpdiIndicators) {
            values.set(0, values.get(0) + companyFpdiIndicator.getPlanMacros());
            values.set(1, values.get(1) + companyFpdiIndicator.getPlans());
            values.set(2, values.get(2) + companyFpdiIndicator.getAxes());
            values.set(3, values.get(3) + companyFpdiIndicator.getObjectives());
            values.set(4, values.get(4) + companyFpdiIndicator.getIndicators());
            values.set(5, values.get(5) + companyFpdiIndicator.getGoals());
        }

        return values;
    }

    public List<Long> countFriscoIndicators(List<CompanyForriscoIndicators> companyForriscoIndicators) {
        List<Long> values = Arrays.asList(0L, 0L, 0L, 0L);;

        for(CompanyForriscoIndicators companyForriscoIndicator : companyForriscoIndicators) {
            values.set(0, values.get(0) + companyForriscoIndicator.getPlanRisks());
            values.set(1, values.get(1) + companyForriscoIndicator.getPolicies());
            values.set(2, values.get(2) + companyForriscoIndicator.getRisks());
            values.set(3, values.get(3) + companyForriscoIndicator.getMonitoredRisks());
        }

        return values;
    }
	
	public static record AdhesionByYearMonths(Map<YearMonth, Counter> adhesionCountMap, List<YearMonth> sortedYearMonths) { };
}
