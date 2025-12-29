package org.forpdi.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InvalidTenantAccessTest {


    @Test
    @DisplayName("Deve criar InvalidTenantAccess com mensagem acessível")
    void test_exception_message_is_accessible() {
        String errorMessage = "Acesso inválido ao tenant";

        InvalidTenantAccess exception = new InvalidTenantAccess(errorMessage);

        assertNotNull(exception, "A exceção não deve ser nula");
        assertEquals(errorMessage, exception.getMessage(), "A mensagem da exceção deve ser igual à fornecida");
    }
}
