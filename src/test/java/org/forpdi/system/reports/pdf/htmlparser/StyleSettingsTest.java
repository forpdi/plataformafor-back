package org.forpdi.system.reports.pdf.htmlparser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StyleSettingsTest {

	@Test
	void testGetStyledTags() {
		var styledTags = StyleSettings.getStyledTags();
		assertNotNull(styledTags);
		assertTrue(styledTags.contains("h1"));
		assertTrue(styledTags.contains("h2"));
		assertTrue(styledTags.contains("a"));
		assertTrue(styledTags.contains("blockquote"));
	}

	@Test
	void testGetTagStylesAsHtml() {
		String aStyle = StyleSettings.getTagStylesAsHtml("a");
		assertNotNull(aStyle);
		assertTrue(aStyle.contains("color:" + ColorSettings.LINK_COLOR + ";"));

		String h1Style = StyleSettings.getTagStylesAsHtml("h1");
		assertNotNull(h1Style);
		assertTrue(h1Style.contains("font-size:20px;"));

		String noStyle = StyleSettings.getTagStylesAsHtml("nonexistent-tag");
		assertNotNull(noStyle);
		assertEquals("", noStyle);
	}

	@Test
	void testAddNewStyleBehavior() {
		String blockquoteStyle = StyleSettings.getTagStylesAsHtml("blockquote");
		assertNotNull(blockquoteStyle);
		assertTrue(blockquoteStyle.contains("border:0 0 0 4 #ccc;"));
	}
}
