package org.forpdi.core.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SimpleEntityTest extends SimpleEntity {
}

public class SimpleEntityJunitTest {
    private SimpleEntityTest entity;

    @BeforeEach
    public void setUp() {
        entity = new SimpleEntityTest();
    }

    @Test
    @DisplayName("Verifica o método de definir e obter ID na entidade")
    public void testSetAndGetId() {

        Long expectedId = 1L;
        
        entity.setId(expectedId);
        
        assertEquals(expectedId, entity.getId(), "O ID não foi corretamente recuperado.");
    }
    
    @Test
    @DisplayName("Verifica se o ID inicial da entidade é nulo")
    public void testInitialIdIsNull() {

        assertNull(entity.getId(), "O ID inicial deveria ser nulo.");
    }
}
