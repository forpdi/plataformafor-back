package org.forpdi.core.jobs;

import org.apache.commons.mail.EmailException;
import org.forpdi.core.notification.NotificationEmail;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.system.EmailUtilsPlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSenderTaskTest {

	@Mock
	private EmailUtilsPlugin emailUtilsPlugin;

	@InjectMocks
	private EmailSenderTask emailSenderTask;

	@Test
	@DisplayName("Deve adicionar e recuperar e-mails na fila")
	void testAddAndGetQueue() {
		NotificationType notificationType = NotificationType.WELCOME;
		NotificationEmail email = new NotificationEmail("test@example.com", "Test User", "Test Subject", "Test Body", null, notificationType);

		emailSenderTask.add(email);
		Queue<NotificationEmail> queue = emailSenderTask.getQueue();

		assertNotNull(queue);
		assertEquals(1, queue.size());
		assertEquals(email, queue.peek());
	}

	@Test
	@DisplayName("Deve enviar e-mails da fila com sucesso")
	void testExecuteSendEmails() throws EmailException {
		NotificationType notificationType = NotificationType.WELCOME;
		NotificationEmail email1 = new NotificationEmail("user1@example.com", "User1", "Subject1", "Body1", null, notificationType);
		NotificationEmail email2 = new NotificationEmail("user2@example.com", "User2", "Subject2", "Body2", null, notificationType);
		emailSenderTask.add(email1);
		emailSenderTask.add(email2);

		when(emailUtilsPlugin.emailSenderIsEnabled()).thenReturn(true);

		emailSenderTask.execute();

		verify(emailUtilsPlugin, times(1)).sendHtmlEmail(email1.getEmail(), email1.getName(), email1.getSubject(), email1.getBody(), email1.getAttach());
		verify(emailUtilsPlugin, times(1)).sendHtmlEmail(email2.getEmail(), email2.getName(), email2.getSubject(), email2.getBody(), email2.getAttach());
		assertTrue(emailSenderTask.getQueue().isEmpty());
	}

	@Test
	@DisplayName("Deve registrar erro ao falhar no envio de e-mail")
	void testExecuteWithEmailSendFailure() throws EmailException {
		NotificationType notificationType = NotificationType.WELCOME;
		NotificationEmail email = new NotificationEmail("user1@example.com", "User1", "Subject1", "Body1", null, notificationType);
		emailSenderTask.add(email);

		when(emailUtilsPlugin.emailSenderIsEnabled()).thenReturn(true);
		doThrow(new RuntimeException("Simulated send failure")).when(emailUtilsPlugin).sendHtmlEmail(
			email.getEmail(), email.getName(), email.getSubject(), email.getBody(), email.getAttach()
		);

		emailSenderTask.execute();

		verify(emailUtilsPlugin, times(1)).sendHtmlEmail(email.getEmail(), email.getName(), email.getSubject(), email.getBody(), email.getAttach());
		assertTrue(emailSenderTask.getQueue().isEmpty());
	}

	@Test
	@DisplayName("Não deve enviar e-mails se o plugin estiver desabilitado")
	void testExecuteWithEmailPluginDisabled() throws EmailException {
		NotificationType notificationType = NotificationType.WELCOME;
		NotificationEmail email = new NotificationEmail("user1@example.com", "User1", "Subject1", "Body1", null, notificationType);
		emailSenderTask.add(email);

		when(emailUtilsPlugin.emailSenderIsEnabled()).thenReturn(false);

		emailSenderTask.execute();

		verify(emailUtilsPlugin, never()).sendHtmlEmail(any(), any(), any(), any(), any());
		assertFalse(emailSenderTask.getQueue().isEmpty());
	}
}
