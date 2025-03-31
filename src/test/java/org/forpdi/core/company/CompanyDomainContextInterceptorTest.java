package org.forpdi.core.company;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CompanyDomainContextInterceptorTest {

	@Test
	@DisplayName("preHandle sets domain and returns true")
	void test_prehandle_sets_domain_and_returns_true() throws Exception {
		CompanyDomainContextInterceptor interceptor = new CompanyDomainContextInterceptor();

		CompanyBS companyBS = mock(CompanyBS.class);
		CompanyDomain expectedDomain = new CompanyDomain();
		when(companyBS.currentDomain()).thenReturn(expectedDomain);

		ReflectionTestUtils.setField(interceptor, "companyBS", companyBS);

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		InheritableThreadLocal<CompanyDomain> currentDomain = new InheritableThreadLocal<>();
		ReflectionTestUtils.setField(CompanyDomainContext.class, "currentDomain", currentDomain);

		boolean result = interceptor.preHandle(request, response, null);

		assertTrue(result, "O método preHandle deve retornar true");

		assertEquals(expectedDomain, currentDomain.get(), "O domínio atual deve ser o esperado");

		verify(companyBS).currentDomain();
	}
}
