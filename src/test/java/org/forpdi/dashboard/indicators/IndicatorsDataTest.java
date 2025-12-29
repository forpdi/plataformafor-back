package org.forpdi.dashboard.indicators;

import org.forpdi.core.company.Company;
import org.forpdi.core.location.CompanyIndicators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IndicatorsDataTest {

	private IndicatorsData indicatorsData;

	@BeforeEach
	void setUp() {
		indicatorsData = new IndicatorsData();
	}

	@Test
	@DisplayName("Dado um IndicatorsData, quando setRegionsCounts é chamado, então a lista de RegionCounts é atualizada corretamente")
	void testSetRegionsCounts() {
		RegionCounts region1 = new RegionCounts(1L, "Região 1", 10, 5, 1);
		RegionCounts region2 = new RegionCounts(2L, "Região 2", 8, 3, 1);
		List<RegionCounts> regionCountsList = Arrays.asList(region1, region2);

		indicatorsData.setRegionsCounts(regionCountsList);

		assertEquals(regionCountsList, indicatorsData.getRegionsCounts(), "A lista de RegionCounts deve ser a mesma que foi definida.");
	}

	@Test
	@DisplayName("Dado um IndicatorsData, quando setCompaniesAccessHistory é chamado, então a lista de CompanyAccessHistory é atualizada corretamente")
	void testSetCompaniesAccessHistory() {
		CompanyAccessHistory history = new CompanyAccessHistory(1L, null);

		indicatorsData.setCompaniesAccessHistory(Arrays.asList(history));

		assertEquals(1, indicatorsData.getCompaniesAccessHistory().size(), "A lista de CompanyAccessHistory deve conter 1 item.");
	}

	@Test
	@DisplayName("Dado um IndicatorsData, quando setCompaniesFpdiIndicators é chamado, então a lista de CompanyFpdiIndicators é atualizada corretamente")
	void testSetCompaniesFpdiIndicators() {
		CompanyFpdiIndicators fpdiIndicators = new CompanyFpdiIndicators();

		indicatorsData.setCompaniesFpdiIndicators(Arrays.asList(fpdiIndicators));

		assertEquals(1, indicatorsData.getCompaniesFpdiIndicators().size(), "A lista de CompanyFpdiIndicators deve conter 1 item.");
	}

	@Test
	@DisplayName("Dado um IndicatorsData, quando setCompaniesFriscoIndicators é chamado, então a lista de CompanyForriscoIndicators é atualizada corretamente")
	void testSetCompaniesFriscoIndicators() {
		CompanyForriscoIndicators friscoIndicators = new CompanyForriscoIndicators();

		indicatorsData.setCompaniesFriscoIndicators(Arrays.asList(friscoIndicators));

		assertEquals(1, indicatorsData.getCompaniesFriscoIndicators().size(), "A lista de CompanyForriscoIndicators deve conter 1 item.");
	}

	@Test
	@DisplayName("Dado um IndicatorsData, quando setCompaniesIndicators é chamado, então a lista de CompanyIndicators é atualizada corretamente")
	void testSetCompaniesIndicators() {
		Company company = new Company();
		company.setId(1L);

		CompanyIndicators companyIndicators = new CompanyIndicators(company, 5L, 10L);

		indicatorsData.setCompaniesIndicators(Arrays.asList(companyIndicators));

		assertEquals(1, indicatorsData.getCompaniesIndicators().size(), "A lista de CompanyIndicators deve conter 1 item.");
	}

	@Test
	@DisplayName("Dado um IndicatorsData sem dados, quando getRegionsCounts é chamado, então a lista de RegionCounts deve retornar null ou vazia")
	void testGetRegionsCountsEmpty() {
		List<RegionCounts> regions = indicatorsData.getRegionsCounts();

		assertNull(regions, "A lista de RegionCounts deve ser nula inicialmente.");
	}

	@Test
	@DisplayName("Dado um IndicatorsData com dados definidos, quando getCompaniesAccessHistory é chamado, então deve retornar a lista correta de CompanyAccessHistory")
	void testGetCompaniesAccessHistory() {
		CompanyAccessHistory accessHistory = new CompanyAccessHistory(1L, null);

		indicatorsData.setCompaniesAccessHistory(Arrays.asList(accessHistory));
		List<CompanyAccessHistory> accessHistoryList = indicatorsData.getCompaniesAccessHistory();

		assertEquals(1, accessHistoryList.size(), "A lista de CompanyAccessHistory deve conter 1 item.");
	}
}
