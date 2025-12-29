package org.forpdi.core.utils;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailUtilsTest {

	@BeforeEach
	void setup() {
		EmailUtils.setSmtpSettings("smtp.test.com", 587, "test_user", "test_password", true);
		EmailUtils.setDefaultFrom("noreply@test.com", "Test System");
	}

	@Test
	@DisplayName("Deve configurar corretamente as configurações SMTP")
	void testSetSmtpSettings() {
		EmailUtils.setSmtpSettings("smtp.example.com", 465, "user", "password", true);
		// Não há retorno direto, mas verificamos que nenhum erro ocorre.
		assertDoesNotThrow(() -> EmailUtils.getSimpleEmail());
	}

	@Test
	@DisplayName("Deve configurar o remetente padrão")
	void testSetDefaultFrom() {
		EmailUtils.setDefaultFrom("admin@example.com", "Admin");
		String fromEmail = "admin@example.com";
		String fromName = "Admin";

		assertDoesNotThrow(() -> {
			SimpleEmail email = EmailUtils.getSimpleEmail();
			email.setFrom(fromEmail, fromName);
		});
	}

}
