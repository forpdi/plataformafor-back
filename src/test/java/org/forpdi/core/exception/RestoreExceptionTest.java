package org.forpdi.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RestoreExceptionTest {


    @Test
    @DisplayName("Deve criar RestoreException com causa correta")
    void test_restore_exception_with_cause() {
        RuntimeException cause = new RuntimeException("Causa original");

        RestoreException exception = new RestoreException(cause);

        assertEquals("Erro ao restaurar backup.", exception.getMessage(), "A mensagem deve ser a padrão");
        assertEquals(cause, exception.getCause(), "A causa deve ser igual à fornecida");
    }
}
