package org.forpdi.planning.attribute;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttributeTypeTest {

    @Test
    @DisplayName("Deve retornar widget string válido de subclasse concreta")
    void test_concrete_subclass_returns_widget_string() {
        AttributeType concreteType = new AttributeType() {
			@Override
			public String getDisplayName() {
				return "";
			}

			@Override
            public String getWidget() {
                return "test-widget";
            }
        
            @Override
            public AttributeTypeWrapper getWrapper() {
                return null;
            }
        };

        String widget = concreteType.getWidget();

        assertNotNull(widget, "O widget não deve ser nulo");
        assertEquals("test-widget", widget, "O widget deve ter o valor esperado");
    }
}
