package org.forpdi.core.properties;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CoreMessagesTest {

	@Test
	void test_get_message_from_overlay() {
		CoreMessages messages = new CoreMessages(Locale.getDefault());
		Map<String, String> overlay = new HashMap<>();
		overlay.put("test.key", "overlay message");
		messages.setOverlay(overlay);

		String result = messages.getMessage("test.key");

		assertEquals("overlay message", result);
	}

	@Test
	void test_get_json_messages() {
		CoreMessages messages = new CoreMessages(Locale.getDefault());

		String result = messages.getJSONMessages();

		assertTrue(result.startsWith("{"));
		assertTrue(result.endsWith("}"));
		assertTrue(result.contains("':'"));
	}

	@Test
	void test_handle_missing_key() {
		String nonExistentKey = "non.existent.key";

		String result = CoreMessages.getText(nonExistentKey);

		assertEquals("???" + nonExistentKey + "???", result);
	}

	@Test
	void test_handle_null_message_from_bundle() {
		CoreMessages messages = new CoreMessages(Locale.getDefault());
		String key = "null.message.key";

		String result = messages.getMessage(key);

		assertEquals("???" + key + "???", result);
	}

}