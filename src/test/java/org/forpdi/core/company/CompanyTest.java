package org.forpdi.core.company;

import java.util.Date;

import org.forpdi.core.location.County;
import org.forpdi.core.storage.file.Archive;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompanyTest {

	private Company company;

	@BeforeEach
	void setUp() {
		company = new Company();
	}

	@Test
	@DisplayName("Testa os getters e setters básicos")
	void testSettersAndGetters() {
		String name = "Test Company";
		String logo = "test-logo.png";
		String description = "Description of Test Company";
		String initials = "TC";
		Integer type = 1;
		boolean showDashboard = true;
		boolean showMaturity = false;
		boolean showBudgetElement = true;
		boolean enableForrisco = false;
		Date creation = new Date();

		company.setName(name);
		company.setLogo(logo);
		company.setDescription(description);
		company.setInitials(initials);
		company.setType(type);
		company.setShowDashboard(showDashboard);
		company.setShowMaturity(showMaturity);
		company.setShowBudgetElement(showBudgetElement);
		company.setEnableForrisco(enableForrisco);
		company.setCreation(creation);

		assertEquals(name, company.getName());
		assertEquals(logo, company.getLogo());
		assertEquals(description, company.getDescription());
		assertEquals(initials, company.getInitials());
		assertEquals(type, company.getType());
		assertEquals(showDashboard, company.isShowDashboard());
		assertEquals(showMaturity, company.isShowMaturity());
		assertEquals(showBudgetElement, company.isShowBudgetElement());
		assertEquals(enableForrisco, company.isEnableForrisco());
		assertEquals(creation, company.getCreation());
	}

	@Test
	@DisplayName("Testa o método hasLogo")
	void testHasLogo() {
		assertFalse(company.hasLogo());

		Archive archive = new Archive();
		company.setLogoArchive(archive);

		assertTrue(company.hasLogo());
	}

	@Test
	@DisplayName("Testa o método equals")
	void testEquals() {
		Company anotherCompany = new Company();

		company.setId(1L);
		anotherCompany.setId(1L);

		assertEquals(company, anotherCompany);

		anotherCompany.setId(2L);
		assertNotEquals(company, anotherCompany);
	}

	@Test
	@DisplayName("Testa o método hashCode")
	void testHashCode() {
		company.setId(1L);
		int hashCode1 = company.hashCode();

		Company anotherCompany = new Company();
		anotherCompany.setId(1L);

		int hashCode2 = anotherCompany.hashCode();

		assertEquals(hashCode1, hashCode2);
	}

	@Test
	@DisplayName("Testa a criação padrão de um objeto Company")
	void testDefaultCreation() {
		assertNotNull(company.getCreation());
	}

	@Test
	@DisplayName("Testa getters e setters de Archive e County")
	void testArchiveAndCounty() {
		Archive archive = new Archive();
		County county = new County();

		company.setLogoArchive(archive);
		company.setCounty(county);

		assertEquals(archive, company.getLogoArchive());
		assertEquals(county, company.getCounty());
	}

	@Test
	public void test_throws_exception_when_county_null() {
		County location = new County();
		company.setCounty(location);

		assertThrows(NullPointerException.class, () -> {
			company.getLocalization();
		});
	}

}
