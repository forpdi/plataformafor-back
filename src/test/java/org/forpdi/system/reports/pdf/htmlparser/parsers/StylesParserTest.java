package org.forpdi.system.reports.pdf.htmlparser.parsers;

import static org.junit.jupiter.api.Assertions.*;

import org.forpdi.system.reports.pdf.htmlparser.StyleSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StylesParserTest {

	private StylesParser parser;

	@BeforeEach
	void setUp() {
		parser = new StylesParser();
	}

	@Test
	void testParseHtmlWithStyledTags() {
		StringBuilder html = new StringBuilder("<h1>Header 1</h1><h2>Header 2</h2><a href=\"#\">Link</a>");
		parser.parseHtml(html);

		String expectedHtml = "<h1 style=\"font-size:20px;\">Header 1</h1>"
			+ "<h2 style=\"font-size:16px;\">Header 2</h2>"
			+ "<a style=\"color:#06c;\" href=\"#\">Link</a>";
		assertEquals(expectedHtml, html.toString());
	}

	@Test
	void testParseHtmlWithNoStyledTags() {
		StringBuilder html = new StringBuilder("<p>Paragraph</p><div>Div content</div>");
		parser.parseHtml(html);

		String expectedHtml = "<p>Paragraph</p><div>Div content</div>";
		assertEquals(expectedHtml, html.toString());
	}

	@Test
	void testParseHtmlWithNestedStyledTags() {
		StringBuilder html = new StringBuilder("<h1>Header <a href=\"#\">Link</a></h1>");
		parser.parseHtml(html);

//		String expectedHtml = "<h1 style=\"font-size:20px;\">Header <a style=\"color:#0000EE;\" href=\"#\">Link</a></h1>";
		String expectedHtml = "<h1 style=\"font-size:20px;\">Header <a style=\"color:#06c;\" href=\"#\">Link</a></h1>";
		assertEquals(expectedHtml, html.toString());
	}

	@Test
	void testParseHtmlWithEmptyContent() {
		StringBuilder html = new StringBuilder("");
		parser.parseHtml(html);

		String expectedHtml = "";
		assertEquals(expectedHtml, html.toString());
	}

	@Test
	void testParseHtmlWithPartialStyledTags() {
		StringBuilder html = new StringBuilder("<h1>Header 1</h1><div>Div content</div><a href=\"#\">Link</a>");
		parser.parseHtml(html);

		String expectedHtml = "<h1 style=\"font-size:20px;\">Header 1</h1>"
			+ "<div>Div content</div>"
			+ "<a style=\"color:#06c;\" href=\"#\">Link</a>";
		assertEquals(expectedHtml, html.toString());
	}
}
