package org.forpdi.planning.attribute.types;

import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActionPlanFieldTest {


	@Test
	void test_get_widget_returns_widget_name() {
		ActionPlanField field = new ActionPlanField();

		String widget = field.getWidget();

		assertEquals("ActionPlanField", widget);
	}

	@Test
	void test_from_database_returns_null() {
		ActionPlanField field = new ActionPlanField();
		AttributeTypeWrapper wrapper = field.getWrapper();

		String result = wrapper.fromDatabase("test value");

		assertNull(result);
	}

	@Test
	void test_to_database_returns_null_for_any_input() {
		ActionPlanField.Wrapper wrapper = new ActionPlanField.Wrapper();

		String result = wrapper.toDatabase("any input");

		assertNull(result);
	}

	@Test
	void test_get_wrapper_returns_wrapper_instance() {
		ActionPlanField field = new ActionPlanField();

		AttributeTypeWrapper wrapper = field.getWrapper();

		assertNotNull(wrapper);
		assertInstanceOf(ActionPlanField.Wrapper.class, wrapper);
	}

	@Test
	void test_get_display_name_returns_correct_string() {
		ActionPlanField field = new ActionPlanField();

		String displayName = field.getDisplayName();

		assertEquals("Plano de ação", displayName);
	}

	@Test
	void test_widget_name_is_immutable() {
		ActionPlanField field = new ActionPlanField();

		String initialWidgetName = field.WIDGET_NAME;

		try {
			java.lang.reflect.Field widgetNameField = ActionPlanField.class.getDeclaredField("WIDGET_NAME");
			widgetNameField.setAccessible(true);
			widgetNameField.set(field, "ModifiedWidgetName");
		} catch (Exception e) {
		}

		assertEquals("ActionPlanField", field.WIDGET_NAME);
	}
}