package org.forpdi.dashboard.indicators;

import org.forpdi.core.company.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class CompanyForriscoIndicatorsTest {

	private CompanyForriscoIndicators companyForriscoIndicators;
	private Company company;

	@BeforeEach
	void setUp() {
		companyForriscoIndicators = new CompanyForriscoIndicators();
		company = new Company();
	}

	@Test
	@DisplayName("Dado um CompanyForriscoIndicators, quando setCompany é chamado, então a empresa é definida corretamente")
	void testSetCompany() {
		company.setId(1L);

		companyForriscoIndicators.setCompany(company);

		assertEquals(company, companyForriscoIndicators.getCompany(), "A empresa deve ser a mesma que foi definida.");
	}

	@Test
	@DisplayName("Dado um CompanyForriscoIndicators, quando setPolicies é chamado, então o número de policies é definido corretamente")
	void testSetPolicies() {
		long policies = 5;

		companyForriscoIndicators.setPolicies(policies);

		assertEquals(policies, companyForriscoIndicators.getPolicies(), "O número de policies deve ser o mesmo que foi definido.");
	}

	@Test
	@DisplayName("Dado um CompanyForriscoIndicators, quando setPlanRisks é chamado, então o número de planRisks é definido corretamente")
	void testSetPlanRisks() {
		long planRisks = 10;

		companyForriscoIndicators.setPlanRisks(planRisks);

		assertEquals(planRisks, companyForriscoIndicators.getPlanRisks(), "O número de planRisks deve ser o mesmo que foi definido.");
	}

	@Test
	@DisplayName("Dado um CompanyForriscoIndicators, quando setRisks é chamado, então o número de risks é definido corretamente")
	void testSetRisks() {
		long risks = 15;

		companyForriscoIndicators.setRisks(risks);

		assertEquals(risks, companyForriscoIndicators.getRisks(), "O número de risks deve ser o mesmo que foi definido.");
	}

	@Test
	@DisplayName("Dado um CompanyForriscoIndicators, quando setMonitoredRisks é chamado, então o número de monitoredRisks é definido corretamente")
	void testSetMonitoredRisks() {
		long monitoredRisks = 3;

		companyForriscoIndicators.setMonitoredRisks(monitoredRisks);

		assertEquals(monitoredRisks, companyForriscoIndicators.getMonitoredRisks(), "O número de monitoredRisks deve ser o mesmo que foi definido.");
	}

	@Test
	@DisplayName("Dado um CompanyForriscoIndicators com dados definidos, quando os getters são chamados, então os valores corretos são retornados")
	void testGetters() {
		long policies = 5;
		long planRisks = 10;
		long risks = 15;
		long monitoredRisks = 3;
		companyForriscoIndicators.setPolicies(policies);
		companyForriscoIndicators.setPlanRisks(planRisks);
		companyForriscoIndicators.setRisks(risks);
		companyForriscoIndicators.setMonitoredRisks(monitoredRisks);

		long retrievedPolicies = companyForriscoIndicators.getPolicies();
		long retrievedPlanRisks = companyForriscoIndicators.getPlanRisks();
		long retrievedRisks = companyForriscoIndicators.getRisks();
		long retrievedMonitoredRisks = companyForriscoIndicators.getMonitoredRisks();

		assertEquals(policies, retrievedPolicies, "O número de policies retornado deve ser o mesmo que foi definido.");
		assertEquals(planRisks, retrievedPlanRisks, "O número de planRisks retornado deve ser o mesmo que foi definido.");
		assertEquals(risks, retrievedRisks, "O número de risks retornado deve ser o mesmo que foi definido.");
		assertEquals(monitoredRisks, retrievedMonitoredRisks, "O número de monitoredRisks retornado deve ser o mesmo que foi definido.");
	}
}
