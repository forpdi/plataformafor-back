package org.forpdi.core.notification.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VisualizeAllNotificationsDtoTest {

	@DisplayName("VisualizeAllNotificationsDto criação do Dto.")
	@Test
	void testVisualizeAllNotificationsDtoCreation(){

		Integer maxResults = 1;
		Integer page = 1;

		VisualizeAllNotificationsDto visualizeAllNotificationsDto = new VisualizeAllNotificationsDto(maxResults, page);

		assertEquals(maxResults, visualizeAllNotificationsDto.maxResults(), "O limite de página não é o esperado.");
		assertEquals(page, visualizeAllNotificationsDto.page(), "O número das páginas não correspondem");
	}
}