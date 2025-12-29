package org.forpdi.planning.attribute.types;

import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DateTimeFieldTest {

	@Test
	void test_get_widget_returns_datetime_field_constant() {
		DateTimeField dateTimeField = new DateTimeField();

		String widget = dateTimeField.getWidget();

		assertEquals("DateTimeField", widget);
	}

	@Test
	void test_get_wrapper_returns_same_instance() {
		DateTimeField dateTimeField = new DateTimeField();

		AttributeTypeWrapper wrapper1 = dateTimeField.getWrapper();
		AttributeTypeWrapper wrapper2 = dateTimeField.getWrapper();

		assertSame(wrapper1, wrapper2);
	}
}