package org.forpdi.core.company;

import org.forpdi.core.notification.NotificationSetting;
import org.forpdi.core.user.User;
import org.forpdi.security.authz.AccessLevels;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompanyUserTest {

	@Test
	void test_create_company_user_with_valid_objects() {
		Company company = new Company();
		company.setName("Test Company");
		company.setLogo("test-logo.png");

		User user = new User();
		user.setName("Test User");
		user.setEmail("test@test.com");

		CompanyUser companyUser = new CompanyUser();
		companyUser.setCompany(company);
		companyUser.setUser(user);

		assertNotNull(companyUser);
		assertEquals(company, companyUser.getCompany());
		assertEquals(user, companyUser.getUser());
		assertEquals(AccessLevels.AUTHENTICATED.getLevel(), companyUser.getAccessLevel());
		assertEquals(NotificationSetting.DEFAULT.getSetting(), companyUser.getNotificationSetting());
		assertFalse(companyUser.isBlocked());
	}

/*
	@Test
	void test_create_company_user_with_null_company() {
		User user = new User();
		user.setName("Test User");
		user.setEmail("test@test.com");

		CompanyUser companyUser = new CompanyUser();

		assertThrows(IllegalArgumentException.class, () -> {
			companyUser.setCompany(null);
		});
	}

 */
	@Test
	void test_to_string_handles_null_names() {
		Company company = new Company();
		company.setName(null);

		User user = new User();
		user.setName(null);

		CompanyUser companyUser = new CompanyUser();
		companyUser.setCompany(company);
		companyUser.setUser(user);

		String result = companyUser.toString();

		assertNotNull(result);
		assertTrue(result.contains("company=null"));
		assertTrue(result.contains("user=null"));
	}

	@Test
	void test_set_and_get_access_level() {
		CompanyUser companyUser = new CompanyUser();

		companyUser.setAccessLevel(AccessLevels.MANAGER.getLevel());

		assertEquals(AccessLevels.MANAGER.getLevel(), companyUser.getAccessLevel());
	}
}