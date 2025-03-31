package org.forpdi.system.reports.pdf.htmlparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class ColorSettingsTest {


	@Test
	void test_link_color_constant_value() {
		String expectedColor = "#06c";

		String actualColor = ColorSettings.LINK_COLOR;

		assertEquals(expectedColor, actualColor);
	}

	@Test
	void test_link_color_thread_safety() throws InterruptedException {
		int threadCount = 10;
		CountDownLatch latch = new CountDownLatch(threadCount);
		List<String> results = Collections.synchronizedList(new ArrayList<>());

		for (int i = 0; i < threadCount; i++) {
			new Thread(() -> {
				results.add(ColorSettings.LINK_COLOR);
				latch.countDown();
			}).start();
		}
		latch.await(1, TimeUnit.SECONDS);

		assertEquals(threadCount, results.size());
		assertTrue(results.stream().allMatch(color -> color.equals("#06c")));
	}
}
