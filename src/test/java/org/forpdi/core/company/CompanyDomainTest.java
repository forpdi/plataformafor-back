package org.forpdi.core.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class CompanyDomainTest {

	@Test
	@DisplayName("Deve definir e obter o host corretamente")
	void testGetAndSetHost() {
		CompanyDomain companyDomain = new CompanyDomain();
		String host = "example.com";

		companyDomain.setHost(host);

		assertEquals(host, companyDomain.getHost());
	}

	@Test
	@DisplayName("Deve definir e obter a baseUrl corretamente")
	void testGetAndSetBaseUrl() {
		CompanyDomain companyDomain = new CompanyDomain();
		String baseUrl = "https://example.com";

		companyDomain.setBaseUrl(baseUrl);

		assertEquals(baseUrl, companyDomain.getBaseUrl());
	}

	@Test
	@DisplayName("Deve definir e obter o tema corretamente")
	void testGetAndSetTheme() {
		CompanyDomain companyDomain = new CompanyDomain();
		String theme = "dark-theme";

		companyDomain.setTheme(theme);

		assertEquals(theme, companyDomain.getTheme());
	}

	@Test
	@DisplayName("Deve retornar o tema padrão na inicialização")
	void testDefaultTheme() {
		CompanyDomain companyDomain = new CompanyDomain();

		String defaultTheme = companyDomain.getTheme();

		assertEquals(CompanyThemeFactory.getDefaultTheme().getId(), defaultTheme);
	}

	@Test
	@DisplayName("Deve definir e obter a data de criação corretamente")
	void testGetAndSetCreation() {
		CompanyDomain companyDomain = new CompanyDomain();
		Date creationDate = new Date();

		companyDomain.setCreation(creationDate);

		assertEquals(creationDate, companyDomain.getCreation());
	}

	@Test
	@DisplayName("Deve definir e obter a empresa associada corretamente")
	void testGetAndSetCompany() {
		CompanyDomain companyDomain = new CompanyDomain();
		Company company = new Company();

		companyDomain.setCompany(company);

		assertEquals(company, companyDomain.getCompany());
	}
}
