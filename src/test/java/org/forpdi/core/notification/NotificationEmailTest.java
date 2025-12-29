package org.forpdi.core.notification;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationEmailTest {

	@Test
	@DisplayName("Deve criar um objeto NotificationEmail com todos os campos corretamente")
	void testNotificationEmailConstructor() {
		String email = "user@example.com";
		String name = "User";
		String subject = "Subject of the notification";
		String body = "Body content of the notification";
		String attach = "attachment.pdf";
		NotificationType notificationType = NotificationType.WELCOME;

		NotificationEmail notificationEmail = new NotificationEmail(email, name, subject, body, attach, notificationType);

		assertEquals(email, notificationEmail.getEmail(), "O email deve ser igual ao informado.");
		assertEquals(name, notificationEmail.getName(), "O nome deve ser igual ao informado.");
		assertEquals(subject, notificationEmail.getSubject(), "O assunto deve ser igual ao informado.");
		assertEquals(body, notificationEmail.getBody(), "O corpo da mensagem deve ser igual ao informado.");
		assertEquals(attach, notificationEmail.getAttach(), "O arquivo anexado deve ser igual ao informado.");
		assertEquals(notificationType, notificationEmail.getNotificationType(), "O tipo de notificação deve ser igual ao informado.");
	}

	@Test
	@DisplayName("Deve alterar o email após a criação")
	void testSetEmail() {
		String email = "user@example.com";
		String newEmail = "newuser@example.com";
		NotificationEmail notificationEmail = new NotificationEmail(email, "User", "Subject", "Body", "attachment.pdf", NotificationType.WELCOME);

		notificationEmail.setEmail(newEmail);

		assertEquals(newEmail, notificationEmail.getEmail(), "O email deve ser atualizado.");
	}

	@Test
	@DisplayName("Deve alterar o nome após a criação")
	void testSetName() {
		String name = "User";
		String newName = "New User";
		NotificationEmail notificationEmail = new NotificationEmail("user@example.com", name, "Subject", "Body", "attachment.pdf", NotificationType.WELCOME);

		notificationEmail.setName(newName);

		assertEquals(newName, notificationEmail.getName(), "O nome deve ser atualizado.");
	}

	@Test
	@DisplayName("Deve alterar o assunto após a criação")
	void testSetSubject() {
		String subject = "Subject of the notification";
		String newSubject = "Updated Subject";
		NotificationEmail notificationEmail = new NotificationEmail("user@example.com", "User", subject, "Body", "attachment.pdf", NotificationType.WELCOME);

		notificationEmail.setSubject(newSubject);

		assertEquals(newSubject, notificationEmail.getSubject(), "O assunto deve ser atualizado.");
	}

	@Test
	@DisplayName("Deve alterar o corpo da mensagem após a criação")
	void testSetBody() {
		String body = "Body content of the notification";
		String newBody = "Updated content";
		NotificationEmail notificationEmail = new NotificationEmail("user@example.com", "User", "Subject", body, "attachment.pdf", NotificationType.WELCOME);

		notificationEmail.setBody(newBody);

		assertEquals(newBody, notificationEmail.getBody(), "O corpo da mensagem deve ser atualizado.");
	}

	@Test
	@DisplayName("Deve alterar o anexo após a criação")
	void testSetAttach() {
		String attach = "attachment.pdf";
		String newAttach = "newAttachment.pdf";
		NotificationEmail notificationEmail = new NotificationEmail("user@example.com", "User", "Subject", "Body", attach, NotificationType.WELCOME);

		notificationEmail.setAttach(newAttach);

		assertEquals(newAttach, notificationEmail.getAttach(), "O anexo deve ser atualizado.");
	}

	@Test
	@DisplayName("Deve alterar o tipo de notificação após a criação")
	void testSetNotificationType() {
		NotificationType notificationType = NotificationType.ACCESSLEVEL_CHANGED;
		NotificationType newNotificationType = NotificationType.PERMISSION_CHANGED;
		NotificationEmail notificationEmail = new NotificationEmail("user@example.com", "User", "Subject", "Body", "attachment.pdf", notificationType);

		notificationEmail.setNotificationType(newNotificationType);

		assertEquals(newNotificationType, notificationEmail.getNotificationType(), "O tipo de notificação deve ser atualizado.");
	}
}
