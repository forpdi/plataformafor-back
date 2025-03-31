package org.forpdi.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConstsTest {


    @Test
    @DisplayName("Deve verificar se as constantes possuem os valores esperados")
    void test_constants_have_expected_values() {
        assertEquals(5, Consts.MIN_PAGE_SIZE, "MIN_PAGE_SIZE deve ser 5");
    
        assertEquals(5, Consts.MED_PAGE_SIZE, "MED_PAGE_SIZE deve ser 5");
    
        assertEquals("api/file", Consts.FILES_ENDPOINT_BASE_URL, "FILES_ENDPOINT_BASE_URL deve ser 'api/file'");
    }
}
