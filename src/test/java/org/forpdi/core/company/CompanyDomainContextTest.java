package org.forpdi.core.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyDomainContextTest {

	@Test
	@DisplayName("Deve armazenar corretamente o domínio atual")
	void testSetCurrentDomain() {
		CompanyDomainContext context = new CompanyDomainContext();
		CompanyDomain domain = mock(CompanyDomain.class);

		CompanyDomainContext.setCurrentDomain(domain);

		assertEquals(domain, context.get(), "O domínio atual não foi configurado corretamente.");
	}

	@Test
	@DisplayName("Deve limpar corretamente o domínio atual")
	void testClear() {
		CompanyDomainContext context = new CompanyDomainContext();
		CompanyDomain domain = mock(CompanyDomain.class);

		CompanyDomainContext.setCurrentDomain(domain);
		CompanyDomainContext.clear();

		assertNull(context.get(), "O domínio não foi limpo corretamente.");
	}

	@Test
	void test_validate_tenant_with_matching_companies() {
		CompanyDomainContext context = new CompanyDomainContext();
		Company company = new Company();
		company.setId(1L);
		CompanyDomain domain = mock(CompanyDomain.class);
		when(domain.getCompany()).thenReturn(company);
		CompanyDomainContext.setCurrentDomain(domain);

		context.validateTenant(company);

		verify(domain).getCompany();
	}

	@Test
	void test_validate_tenant_with_different_companies() {
		CompanyDomainContext context = new CompanyDomainContext();
		Company currentCompany = new Company();
		currentCompany.setId(1L);
		Company resourceCompany = new Company();
		resourceCompany.setId(2L);
		CompanyDomain domain = mock(CompanyDomain.class);
		when(domain.getCompany()).thenReturn(currentCompany);
		CompanyDomainContext.setCurrentDomain(domain);

		assertThrows(IllegalStateException.class, () -> {
			context.validateTenant(resourceCompany);
		});
	}

}

