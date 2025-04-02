package org.forpdi.core.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SimplePropertyProjectionTest {


    @Test
    @DisplayName("Deve criar um SimplePropertyProjection com nome de propriedade válido")
    public void test_constructor_creates_instance_with_valid_property_name() {
        String propertyName = "testProperty";

        SimplePropertyProjection projection = new SimplePropertyProjection(propertyName);

        assertNotNull(projection, "O objeto SimplePropertyProjection não deve ser nulo");
        assertEquals(propertyName, projection.getPropertyName(), "O nome da propriedade deve ser igual ao fornecido");
    }
}
