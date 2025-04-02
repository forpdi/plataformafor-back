package org.forpdi.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ErrorMessagesTest {


    @Test
    @DisplayName("Deve verificar o conteúdo da mensagem de erro inesperado")
    void test_unexpected_error_message_content() {
        String expectedMessage = "Erro inesperado: ";

        assertEquals(expectedMessage, ErrorMessages.UNEXPECTED_ERROR, 
            "A mensagem de erro inesperado deve ter o conteúdo correto");
    }
}
