package org.forpdi.core.company.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.forpdi.core.notification.dto.NotificationIdDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationIdDtoTest {

    @Test
    @DisplayName("Deve criar um NotificationIdDto com valores corretos")
    void testNotificationIdDtoCreation() {
        Long notificationId = 456L;

        NotificationIdDto dto = new NotificationIdDto(notificationId);

        assertNotNull(dto, "O objeto NotificationIdDto não deve ser nulo.");
        assertEquals(notificationId, dto.notificationId(), "O ID da notificação deve ser igual ao fornecido.");
    }
}

