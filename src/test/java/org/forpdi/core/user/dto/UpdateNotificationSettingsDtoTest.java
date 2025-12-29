package org.forpdi.core.user.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UpdateNotificationSettingsDtoTest {

    @Test
    @DisplayName("Criação do DTO de atualização de configurações de notificação")
    public void testUpdateNotificationSettingsDtoCreation() {
        Long id = 1L;
        int notificationSetting = 3;

        UpdateNotificationSettingsDto dto = new UpdateNotificationSettingsDto(id, notificationSetting);

        assertEquals(id, dto.id(), "O ID no DTO não corresponde ao esperado.");
        assertEquals(notificationSetting, dto.notificationSetting(), "A configuração de notificação no DTO não corresponde ao esperado.");
    }
}
