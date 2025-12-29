package org.forpdi.system.reports.pdf.htmlparser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class ColorSettingsTest {

	@Test
	@DisplayName("LINK_COLOR deve ter valor #06c")
	void linkColor_hasExpectedValue() {
		assertEquals("#06c", ColorSettings.LINK_COLOR);
	}

	@Test
	@DisplayName("LINK_COLOR deve ser constante (public static final)")
	void linkColor_isConstant() throws Exception {
		Field f = ColorSettings.class.getDeclaredField("LINK_COLOR");
		int mods = f.getModifiers();
		assertTrue(Modifier.isPublic(mods));
		assertTrue(Modifier.isStatic(mods));
		assertTrue(Modifier.isFinal(mods));
	}

	@Test
	@DisplayName("LINK_COLOR não deve ser nulo")
	void linkColor_notNull() {
		assertNotNull(ColorSettings.LINK_COLOR);
		assertFalse(ColorSettings.LINK_COLOR.isBlank());
	}
}
