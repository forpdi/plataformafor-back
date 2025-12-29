package org.forpdi.planning.attribute.types;

import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TextAreaTest {

	@Test
	void test_get_widget_returns_text_area_constant() {
		TextArea textArea = new TextArea();

		String widget = textArea.getWidget();

		assertEquals("TextArea", widget);
	}

	@Test
	void test_wrapper_handles_null_input() {
		TextArea textArea = new TextArea();
		AttributeTypeWrapper wrapper = textArea.getWrapper();

		String nullDbValue = wrapper.fromDatabase(null);
		String nullNumValue = wrapper.fromDatabaseNumerical(null);
		String nullDateValue = wrapper.fromDatabaseDate(null);

		assertNull(nullDbValue);
		assertNull(nullNumValue);
		assertNull(nullDateValue);
	}

	@Test
	void test_get_display_name_returns_area_de_texto() {
		TextArea textArea = new TextArea();

		String displayName = textArea.getDisplayName();

		assertEquals("Área de Texto", displayName);
	}
}