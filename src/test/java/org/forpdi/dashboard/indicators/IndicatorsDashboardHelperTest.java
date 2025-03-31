package org.forpdi.dashboard.indicators;

import org.forpdi.core.company.Company;
import org.forpdi.core.utils.Counter;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndicatorsDashboardHelperTest {

	private final IndicatorsDashboardHelper helper = new IndicatorsDashboardHelper();

	@Test
	void testGetTotalUniversities() {
		List<RegionCounts> regionCounts = Arrays.asList(
			new RegionCounts(1L, "Region 1", 3, 2, 1),
			new RegionCounts(2L, "Region 2", 5, 1, 1)
		);

		long totalUniversities = helper.getTotalUniversities(regionCounts);
		assertEquals(8, totalUniversities);
	}

	@Test
	void testGetTotalInstitutes() {
		List<RegionCounts> regionCounts = Arrays.asList(
			new RegionCounts(1L, "Region 1", 3, 2, 1),
			new RegionCounts(2L, "Region 2", 5, 4, 1)
		);

		long totalInstitutes = helper.getTotalInstitutes(regionCounts);
		assertEquals(6, totalInstitutes);
	}

	@Test
	void testGetAdhesionsByYearMonths() {
		Calendar calendar = Calendar.getInstance();

		calendar.set(2023, Calendar.JANUARY, 1);
		Company company1 = new Company();
		company1.setCreation(calendar.getTime());

		calendar.set(2023, Calendar.FEBRUARY, 1);
		Company company2 = new Company();
		company2.setCreation(calendar.getTime());

		calendar.set(2023, Calendar.JANUARY, 1);
		Company company3 = new Company();
		company3.setCreation(calendar.getTime());

		List<Company> companies = Arrays.asList(company1, company2, company3);

		IndicatorsDashboardHelper.AdhesionByYearMonths adhesions = helper.getAhesionsByYearMonths(companies);

		Map<YearMonth, Counter> expectedMap = new HashMap<>();
		YearMonth jan2023 = YearMonth.of(2023, 1);
		YearMonth feb2023 = YearMonth.of(2023, 2);
		expectedMap.put(jan2023, new Counter(2));
		expectedMap.put(feb2023, new Counter(1));

//		assertEquals(expectedMap, adhesions.adhesionCountMap());
		assertEquals(Arrays.asList(jan2023, feb2023), adhesions.sortedYearMonths());
	}

	@Test
	void testCountFpdiIndicators() {
		List<CompanyFpdiIndicators> indicators = new ArrayList<>();

		CompanyFpdiIndicators indicator1 = new CompanyFpdiIndicators();
		indicator1.setPlanMacros(1);
		indicator1.setPlans(2);
		indicator1.setAxes(3);
		indicator1.setObjectives(4);
		indicator1.setIndicators(5);
		indicator1.setGoals(6);

		CompanyFpdiIndicators indicator2 = new CompanyFpdiIndicators();
		indicator2.setPlanMacros(2);
		indicator2.setPlans(3);
		indicator2.setAxes(4);
		indicator2.setObjectives(5);
		indicator2.setIndicators(6);
		indicator2.setGoals(7);

		indicators.add(indicator1);
		indicators.add(indicator2);

		List<Long> result = helper.countFpdiIndicators(indicators);
		assertEquals(Arrays.asList(3L, 5L, 7L, 9L, 11L, 13L), result);
	}

	@Test
	void testCountFriscoIndicators() {
		List<CompanyForriscoIndicators> indicators = new ArrayList<>();

		CompanyForriscoIndicators indicator1 = new CompanyForriscoIndicators();
		indicator1.setPlanRisks(1);
		indicator1.setPolicies(2);
		indicator1.setRisks(3);
		indicator1.setMonitoredRisks(4);

		CompanyForriscoIndicators indicator2 = new CompanyForriscoIndicators();
		indicator2.setPlanRisks(5);
		indicator2.setPolicies(6);
		indicator2.setRisks(7);
		indicator2.setMonitoredRisks(8);

		indicators.add(indicator1);
		indicators.add(indicator2);

		List<Long> result = helper.countFriscoIndicators(indicators);
		assertEquals(Arrays.asList(6L, 8L, 10L, 12L), result);
	}

}
