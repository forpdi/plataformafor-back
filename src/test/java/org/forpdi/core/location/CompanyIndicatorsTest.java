package org.forpdi.core.location;

import static org.junit.jupiter.api.Assertions.*;
import org.forpdi.core.company.Company;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

class CompanyIndicatorsTest {

	@Test
	@DisplayName("Testa a criação do objeto CompanyIndicators usando o Builder")
	void test_CompanyIndicators_builder() {
		Company company = new Company();
		company.setId(2L);
		company.setName("Outra Empresa");
		company.setInitials("OE");
		company.setCreation(new Date());

		long planMacrosCount = 10;
		long planRisksCount = 7;

		CompanyIndicators indicators = new CompanyIndicators.Builder()
			.company(company)
			.planMacrosCount(planMacrosCount)
			.planRisksCount(planRisksCount)
			.build();

		assertEquals(2L, indicators.getId(), "O ID da empresa deve ser inicializado corretamente pelo builder");
		assertEquals("Outra Empresa", indicators.getName(), "O nome da empresa deve ser inicializado corretamente pelo builder");
		assertEquals("OE", indicators.getInitials(), "As iniciais da empresa devem ser inicializadas corretamente pelo builder");
		assertEquals(company.getCreation(), indicators.getCreation(), "A data de criação deve ser inicializada corretamente pelo builder");
		assertEquals(10, indicators.getPlanMacrosCount(), "O número de macros deve ser inicializado corretamente pelo builder");
		assertEquals(7, indicators.getPlanRisksCount(), "O número de riscos deve ser inicializado corretamente pelo builder");
	}

	@Test
	@DisplayName("Testa a criação do objeto CompanyIndicators com Company nulo")
	void test_CompanyIndicators_constructor_withNullCompany() {
		Company company = null;
		long planMacrosCount = 5;
		long planRisksCount = 3;

		assertThrows(NullPointerException.class, () -> {
			new CompanyIndicators(company, planMacrosCount, planRisksCount);
		}, "Deve lançar NullPointerException ao passar um Company nulo");
	}

	@Test
	@DisplayName("Testa a criação do objeto CompanyIndicators com valores válidos")
	void test_CompanyIndicators_constructor_withValidCompany() {
		Company company = new Company();
		company.setId(1L);
		company.setName("Empresa Exemplo");
		company.setInitials("EE");
		company.setCreation(new Date());

		long planMacrosCount = 5;
		long planRisksCount = 3;

		CompanyIndicators indicators = new CompanyIndicators(company, planMacrosCount, planRisksCount);

		assertEquals(1L, indicators.getId(), "O ID da empresa deve ser inicializado corretamente");
		assertEquals("Empresa Exemplo", indicators.getName(), "O nome da empresa deve ser inicializado corretamente");
		assertEquals("EE", indicators.getInitials(), "As iniciais da empresa devem ser inicializadas corretamente");
		assertEquals(company.getCreation(), indicators.getCreation(), "A data de criação deve ser inicializada corretamente");
		assertEquals(5, indicators.getPlanMacrosCount(), "O número de macros deve ser inicializado corretamente");
		assertEquals(3, indicators.getPlanRisksCount(), "O número de riscos deve ser inicializado corretamente");
	}

	@Test
	void test_set_positive_id_value() {
		CompanyIndicators indicators = new CompanyIndicators.Builder()
			.company(new Company())
			.planMacrosCount(0)
			.planRisksCount(0)
			.build();
		Long expectedId = 123L;

		indicators.setId(expectedId);

		assertEquals(expectedId, indicators.getId());
	}

	@Test
	void test_set_valid_company_name() {
		CompanyIndicators indicators = new CompanyIndicators.Builder()
			.company(new Company())
			.planMacrosCount(0)
			.planRisksCount(0)
			.build();
		String expectedName = "Test Company";

		indicators.setName(expectedName);

		assertEquals(expectedName, indicators.getName());
	}

	@Test
	void test_set_valid_initials() {
		CompanyIndicators indicators = new CompanyIndicators.Builder()
			.company(new Company())
			.planMacrosCount(0)
			.planRisksCount(0)
			.build();
		String expectedInitials = "ABC";

		indicators.setInitials(expectedInitials);

		assertEquals(expectedInitials, indicators.getInitials());
	}

	@Test
	void test_set_valid_creation_date() {
		CompanyIndicators indicators = new CompanyIndicators.Builder()
			.company(new Company())
			.planMacrosCount(0)
			.planRisksCount(0)
			.build();
		Date testDate = new Date();

		indicators.setCreation(testDate);

		assertEquals(testDate, indicators.getCreation());
	}

	@Test
	void test_set_positive_plan_macros_count() {
		CompanyIndicators indicators = new CompanyIndicators.Builder()
			.company(new Company())
			.planMacrosCount(0)
			.planRisksCount(0)
			.build();

		indicators.setPlanMacrosCount(42);

		assertEquals(42, indicators.getPlanMacrosCount());
	}

	@Test
	void test_set_positive_plan_risks_count() {
		CompanyIndicators indicators = new CompanyIndicators.Builder()
			.company(new Company())
			.planMacrosCount(0)
			.planRisksCount(0)
			.build();
		indicators.setPlanRisksCount(42);

		assertEquals(42, indicators.getPlanRisksCount());
	}

}
