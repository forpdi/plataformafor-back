package org.forpdi.core.notification;

import org.forpdi.core.company.Company;
import org.forpdi.core.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

class MessageHistoryTest {

	@Test
	@DisplayName("Dado um histórico de mensagem com dados válidos, quando os getters são chamados, então os valores corretos são retornados")
	void testGetters_ValidMessageHistory() {
		User sender = new User();
		User receiver = new User();
		Company company = new Company();
		Notification notification = new Notification();

		MessageHistory messageHistory = new MessageHistory();
		messageHistory.setSubject("Assunto da mensagem");
		messageHistory.setMessage("Esta é a mensagem de teste.");
		messageHistory.setCreation(new Date());
		messageHistory.setUserSender(sender);
		messageHistory.setUserReceiver(receiver);
		messageHistory.setCompany(company);
		messageHistory.setNotification(notification);

		String subject = messageHistory.getSubject();
		String message = messageHistory.getMessage();
		Date creation = messageHistory.getCreation();
		User userSender = messageHistory.getUserSender();
		User userReceiver = messageHistory.getUserReceiver();
		Company companyFromMessage = messageHistory.getCompany();
		Notification notificationFromMessage = messageHistory.getNotification();

		assertEquals("Assunto da mensagem", subject, "O assunto da mensagem deve ser 'Assunto da mensagem'.");
		assertEquals("Esta é a mensagem de teste.", message, "A mensagem deve ser 'Esta é a mensagem de teste.'");
		assertNotNull(creation, "A data de criação não deve ser nula.");
		assertEquals(sender, userSender, "O usuário remetente deve ser o usuário sender.");
		assertEquals(receiver, userReceiver, "O usuário destinatário deve ser o usuário receiver.");
		assertEquals(company, companyFromMessage, "A empresa deve ser a empresa fornecida.");
		assertEquals(notification, notificationFromMessage, "A notificação deve ser a notificação fornecida.");
	}

	@Test
	@DisplayName("Dado um histórico de mensagem sem notificação, quando o getter de notificação é chamado, então retorna null")
	void testGetters_MessageHistoryWithoutNotification() {
		User sender = new User();
		User receiver = new User();
		Company company = new Company();

		MessageHistory messageHistory = new MessageHistory();
		messageHistory.setSubject("Assunto sem notificação");
		messageHistory.setMessage("Mensagem sem notificação.");
		messageHistory.setCreation(new Date());
		messageHistory.setUserSender(sender);
		messageHistory.setUserReceiver(receiver);
		messageHistory.setCompany(company);

		Notification notification = messageHistory.getNotification();

		assertNull(notification, "A notificação deve ser null.");
	}

	@Test
	@DisplayName("Dado um histórico de mensagem com dados inválidos, quando os setters são chamados, então os dados são atribuídos corretamente")
	void testSetters_InvalidMessageHistory() {
		MessageHistory messageHistory = new MessageHistory();

		messageHistory.setSubject(null);
		messageHistory.setMessage(null);
		messageHistory.setCreation(null);
		messageHistory.setUserSender(null);
		messageHistory.setUserReceiver(null);
		messageHistory.setCompany(null);

		assertNull(messageHistory.getSubject(), "O assunto deve ser null.");
		assertNull(messageHistory.getMessage(), "A mensagem deve ser null.");
		assertNull(messageHistory.getCreation(), "A data de criação deve ser null.");
		assertNull(messageHistory.getUserSender(), "O usuário remetente deve ser null.");
		assertNull(messageHistory.getUserReceiver(), "O usuário destinatário deve ser null.");
		assertNull(messageHistory.getCompany(), "A empresa deve ser null.");
	}

	@Test
	@DisplayName("Dado um histórico de mensagem com dados de criação válidos, quando a data de criação é alterada, então a nova data é atribuída corretamente")
	void testSetCreationDate() {
		MessageHistory messageHistory = new MessageHistory();
		Date initialCreationDate = new Date();
		messageHistory.setCreation(initialCreationDate);

		Date newCreationDate = new Date(System.currentTimeMillis() + 100000);
		messageHistory.setCreation(newCreationDate);

		assertEquals(newCreationDate, messageHistory.getCreation(), "A data de criação deve ser a nova data atribuída.");
	}
}
