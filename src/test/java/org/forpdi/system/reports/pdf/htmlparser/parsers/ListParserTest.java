package org.forpdi.system.reports.pdf.htmlparser.parsers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListParserTest {

	private ListParser parser;

	@BeforeEach
	void setUp() {
		parser = new ListParser();
	}

	@Test
	void testParseHtmlWithUnorderedList() {
		StringBuilder html = new StringBuilder("<ul><li>Item 1</li><li>Item 2</li></ul>");
		parser.parseHtml(html);

		String expectedHtml = "<ul><li>" + ListParser.BULLET_CHAR + " Item 1</li><li>" + ListParser.BULLET_CHAR + " Item 2</li></ul>";
		assertEquals(expectedHtml, html.toString());
	}

	@Test
	void testParseHtmlWithOrderedList() {
		StringBuilder html = new StringBuilder("<ol><li>Item 1</li><li>Item 2</li><li>Item 3</li></ol>");
		parser.parseHtml(html);

//		String expectedHtml = "<ol><li>1. Item 1</li><li>2. Item 2</li><li>3. Item 3</li></ol>";
		String expectedHtml = "<ol><li>1.  Item 1</li><li>2.  Item 2</li><li>3.  Item 3</li></ol>";
		assertEquals(expectedHtml, html.toString());
	}

	@Test
	void testParseHtmlWithEmptyList() {
		StringBuilder html = new StringBuilder("<ul></ul>");
		parser.parseHtml(html);

		String expectedHtml = "<ul></ul>";
		assertEquals(expectedHtml, html.toString());
	}

	@Test
	void testParseHtmlWithNoLists() {
		StringBuilder html = new StringBuilder("<p>This is a paragraph.</p>");
		parser.parseHtml(html);

		String expectedHtml = "<p>This is a paragraph.</p>";
		assertEquals(expectedHtml, html.toString());
	}
}
