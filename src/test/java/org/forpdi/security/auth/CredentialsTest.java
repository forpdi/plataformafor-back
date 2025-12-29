package org.forpdi.security.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CredentialsTest {

	@Test
	void test_get_email_returns_stored_value() {
		Credentials credentials = new Credentials();
		String expectedEmail = "test@example.com";
		credentials.setEmail(expectedEmail);

		String actualEmail = credentials.getEmail();

		assertEquals(expectedEmail, actualEmail);
	}

	@Test
	void test_set_email_with_null_value() {
		Credentials credentials = new Credentials();

		credentials.setEmail(null);

		assertNull(credentials.getEmail());
	}

	@Test
	void test_get_password_returns_stored_value() {
		Credentials credentials = new Credentials();
		String expectedPassword = "securePassword123";
		credentials.setPassword(expectedPassword);

		String actualPassword = credentials.getPassword();

		assertEquals(expectedPassword, actualPassword);
	}
}