package org.forpdi.core.notification;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.forpdi.core.company.Company;
import org.forpdi.core.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationTest {

	@Test
	@DisplayName("Testa o getter e setter de picture")
	void testPicture() {
		Notification notification = new Notification();
		String picture = "http://example.com/picture.png";

		notification.setPicture(picture);
		assertEquals(picture, notification.getPicture());
	}

	@Test
	@DisplayName("Testa o getter e setter de description")
	void testDescription() {
		Notification notification = new Notification();
		String description = "Teste de descrição";

		notification.setDescription(description);
		assertEquals(description, notification.getDescription());
	}

	@Test
	@DisplayName("Testa o getter e setter de creation")
	void testCreation() {
		Notification notification = new Notification();
		Date now = new Date();

		notification.setCreation(now);
		assertEquals(now, notification.getCreation());
	}

	@Test
	@DisplayName("Testa o getter e setter de user")
	void testUser() {
		Notification notification = new Notification();
		User user = new User();
		user.setId(1L);

		notification.setUser(user);
		assertEquals(user, notification.getUser());
	}

	@Test
	@DisplayName("Testa o getter e setter de company")
	void testCompany() {
		Notification notification = new Notification();
		Company company = new Company();
		company.setId(1L);

		notification.setCompany(company);
		assertEquals(company, notification.getCompany());
	}

	@Test
	@DisplayName("Testa o getter e setter de vizualized")
	void testVizualized() {
		Notification notification = new Notification();
		notification.setVizualized(true);
		assertTrue(notification.isVizualized());

		notification.setVizualized(false);
		assertFalse(notification.isVizualized());
	}

	@Test
	@DisplayName("Testa o getter e setter de onlyEmail")
	void testOnlyEmail() {
		Notification notification = new Notification();
		notification.setOnlyEmail(true);
		assertTrue(notification.isOnlyEmail());

		notification.setOnlyEmail(false);
		assertFalse(notification.isOnlyEmail());
	}

	@Test
	@DisplayName("Testa o getter e setter de type")
	void testType() {
		Notification notification = new Notification();
		Integer type = 1;

		notification.setType(type);
		assertEquals(type, notification.getType());
	}

	@Test
	@DisplayName("Testa o getter e setter de url")
	void testUrl() {
		Notification notification = new Notification();
		String url = "http://example.com";

		notification.setUrl(url);
		assertEquals(url, notification.getUrl());
	}

	@Test
	@DisplayName("Testa o getter e setter de responded")
	void testResponded() {
		Notification notification = new Notification();
		notification.setResponded(true);
		assertTrue(notification.getResponded());

		notification.setResponded(false);
		assertFalse(notification.getResponded());
	}
}
