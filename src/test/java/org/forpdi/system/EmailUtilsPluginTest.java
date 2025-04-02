package org.forpdi.system;

import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.mail.EmailException;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.properties.SystemConfigs;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class EmailUtilsPluginTest {

	@Test
	void test_build_welcome_email() {
		EmailUtilsPlugin.EmailBuilder builder = new EmailUtilsPlugin.EmailBuilder(NotificationType.WELCOME, "http://test.com");

		String body = builder.getBody();
		String subject = builder.getSubject();

		assertTrue(body.contains("Seja bem-vindo(a) a Plataforma FOR"));
		assertEquals("Bem-vindo a PlataformaFor", subject);
		assertTrue(body.contains(SystemConfigs.getEmailLogoUrl()));
	}

	@Test
	void test_build_notification_email() {
		String message = "Test notification";
		String url = "http://test.com";
		EmailUtilsPlugin.EmailBuilder builder = new EmailUtilsPlugin.EmailBuilder(NotificationType.GOAL_CLOSED, message, url);

		String body = builder.getBody();

		assertTrue(body.contains(message));
		assertTrue(body.contains(url));
		assertTrue(body.contains("Notifica&ccedil;&atilde;o"));
	}

	@Test
	void test_build_invite_user_email() {
		String user = "Test User";
		String url = "http://test.com/invite";
		EmailUtilsPlugin.EmailBuilder builder = new EmailUtilsPlugin.EmailBuilder(NotificationType.INVITE_USER, user, url);

		String body = builder.getBody();
		String subject = builder.getSubject();

		assertTrue(body.contains(user));
		assertTrue(body.contains(url));
		assertEquals("Complete seu cadastro na Plataforma For", subject);
	}

	@Test
	void test_build_password_recovery_email() {
		String user = "Test User";
		String url = "http://test.com/recover";
		EmailUtilsPlugin.EmailBuilder builder = new EmailUtilsPlugin.EmailBuilder(NotificationType.RECOVER_PASSWORD, user, url);

		String body = builder.getBody();

		assertTrue(body.contains(user));
		assertTrue(body.contains(url));
		assertTrue(body.contains("Recupera&ccedil;&atilde;o de Senha"));
	}

	@Test
	void test_email_sender_enabled_with_host() {
		EmailUtilsPlugin emailUtils = new EmailUtilsPlugin();
		ReflectionTestUtils.setField(emailUtils, "host", "smtp.test.com");

		boolean enabled = emailUtils.emailSenderIsEnabled();

		assertTrue(enabled);
	}

	@Test
	void test_send_email_null_recipient() {
		EmailUtilsPlugin emailUtils = new EmailUtilsPlugin();

		assertThrows(NullPointerException.class, () -> {
			emailUtils.sendHtmlEmail(null, "Test", "Subject", "Message", null);
		});
	}

	@Test
	void test_send_email_invalid_credentials() {
		EmailUtilsPlugin emailUtils = new EmailUtilsPlugin();
		ReflectionTestUtils.setField(emailUtils, "user", "invalid");
		ReflectionTestUtils.setField(emailUtils, "password", "wrong");

		assertThrows(NullPointerException.class, () -> {
			emailUtils.sendHtmlEmail("test@test.com", "Test", "Subject", "Message", null);
		});
	}

	@Test
	void test_build_email_missing_params() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
			new EmailUtilsPlugin.EmailBuilder(NotificationType.WELCOME);
		});
	}

	@Test
	void test_invalid_notification_type() {
		assertThrows(NullPointerException.class, () -> {
			new EmailUtilsPlugin.EmailBuilder(null, "test");
		});
	}

	@Test
	void test_email_sender_disabled_empty_host() {
		EmailUtilsPlugin emailUtils = new EmailUtilsPlugin();
		ReflectionTestUtils.setField(emailUtils, "host", "");

		boolean enabled = emailUtils.emailSenderIsEnabled();

		assertFalse(enabled);
	}

	@Test
	void test_attach_nonexistent_file() {
		EmailUtilsPlugin emailUtils = new EmailUtilsPlugin();

		assertThrows(NullPointerException.class, () -> {
			emailUtils.sendHtmlEmail("test@test.com", "Test", "Subject", "Message", "nonexistent.txt");
		});
	}
}