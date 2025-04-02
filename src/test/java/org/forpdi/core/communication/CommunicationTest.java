package org.forpdi.core.communication;

import org.forpdi.core.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class CommunicationTest {

	private Communication communication;
	private User user;

	@BeforeEach
	void setUp() {
		communication = new Communication();
		user = new User();
		user.setId(1L);
	}

	@Test
	@DisplayName("Dado que um título é atribuído, quando obter o título, então ele deve ser retornado corretamente")
	void testSetTitle() {
		String title = "Test Title";

		communication.setTitle(title);

		assertEquals(title, communication.getTitle(), "O título atribuído deve ser retornado corretamente.");
	}

	@Test
	@DisplayName("Dado que uma mensagem é atribuída, quando obter a mensagem, então ela deve ser retornada corretamente")
	void testSetMessage() {
		String message = "Test Message";

		communication.setMessage(message);

		assertEquals(message, communication.getMessage(), "A mensagem atribuída deve ser retornada corretamente.");
	}

	@Test
	@DisplayName("Dado que a data de validade de início é atribuída, quando obter a data de validade de início, então ela deve ser retornada corretamente")
	void testSetValidityBegin() {
		Date validityBegin = new Date(2024, 11, 10); 

		communication.setValidityBegin(validityBegin);

		assertEquals(validityBegin, communication.getValidityBegin(), "A data de início de validade atribuída deve ser retornada corretamente.");
	}

	@Test
	@DisplayName("Dado que a data de validade de término é atribuída, quando obter a data de validade de término, então ela deve ser retornada corretamente")
	void testSetValidityEnd() {
		Date validityEnd = new Date(2024, 12, 31);  

		communication.setValidityEnd(validityEnd);

		assertEquals(validityEnd, communication.getValidityEnd(), "A data de término de validade atribuída deve ser retornada corretamente.");
	}

	@Test
	@DisplayName("Dado que a data de última modificação é atribuída, quando obter a data de última modificação, então ela deve ser retornada corretamente")
	void testSetLastModification() {
		Date lastModification = new Date(2024, 11, 1);  

		communication.setLastModification(lastModification);

		assertEquals(lastModification, communication.getlastModification(), "A data de última modificação atribuída deve ser retornada corretamente.");
	}

	@Test
	@DisplayName("Dado que o valor de showPopup é atribuído, quando obter o valor de showPopup, então ele deve ser retornado corretamente")
	void testSetShowPopup() {
		Boolean showPopup = true;

		communication.setShowPopup(showPopup);

		assertEquals(showPopup, communication.getShowPopup(), "O valor de 'showPopup' atribuído deve ser retornado corretamente.");
	}

	@Test
	@DisplayName("Dado que um responsável é atribuído, quando obter o responsável, então ele deve ser retornado corretamente")
	void testSetResponsible() {
		communication.setResponsible(user);

		User responsible = communication.getResponsible();

		assertEquals(user, responsible, "O responsável atribuído deve ser retornado corretamente.");
	}
}
