package org.forrisco.core.process;

import org.forpdi.core.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("Testes para a entidade ProcessHistory")
class ProcessHistoryTest {

    @Test
    @DisplayName("Deve definir e obter todos os campos corretamente")
    void shouldSetAndGetFieldsCorrectly() {
        Process mockProcess = mock(Process.class);
        User mockUser = mock(User.class);
        LocalDateTime now = LocalDateTime.now();

        ProcessHistory history = new ProcessHistory();
        history.setProcess(mockProcess);
        history.setResponsibleUser(mockUser);
        history.setChangedField("Status");
        history.setOldValue("Em Andamento");
        history.setNewValue("Concluído");
        history.setChangeDate(now);

        assertNotNull(history);
        assertEquals(mockProcess, history.getProcess());
        assertEquals(mockUser, history.getResponsibleUser());
        assertEquals("Status", history.getChangedField());
        assertEquals("Em Andamento", history.getOldValue());
        assertEquals("Concluído", history.getNewValue());
        assertEquals(now, history.getChangeDate());
    }
}