package org.forpdi.planning.attribute.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AttachmentFieldTest {


    @Test
    @DisplayName("getWidget() deve retornar a string constante 'AttachmentField'")
    public void test_get_widget_returns_constant_string() {
        AttachmentField attachmentField = new AttachmentField();

        String widget = attachmentField.getWidget();

        assertNotNull(widget, "O widget não deve ser nulo");
        assertEquals("AttachmentField", widget, "O widget deve ser igual a 'AttachmentField'");
    }
}
