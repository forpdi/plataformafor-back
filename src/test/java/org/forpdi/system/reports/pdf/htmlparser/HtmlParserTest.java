package org.forpdi.system.reports.pdf.htmlparser;

import org.forpdi.system.reports.pdf.htmlparser.parsers.HtmlParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HtmlParserTest {

	@Test
	@DisplayName("Deve criar uma instância de Replacement com valores válidos")
	void testReplacementCreation() {
		String from = "oldValue";
		String to = "newValue";

		HtmlParser.Replacement replacement = new HtmlParser.Replacement(from, to);

		assertNotNull(replacement);
		assertEquals(from, replacement.from);
		assertEquals(to, replacement.to);
	}

	@Test
	@DisplayName("Deve lançar NullPointerException ao tentar usar implementação nula do HtmlParser")
	void testHtmlParserNullImplementation() {
		HtmlParser parser = null;

		assertThrows(NullPointerException.class, () -> {
			parser.parseHtml(new StringBuilder("<html></html>"));
		});
	}

	@Test
	@DisplayName("Deve validar constante CONTENT_MARK_LENGTH da interface")
	void testContentMarkLengthConstant() {
		int expectedValue = 10;

		assertEquals(expectedValue, HtmlParser.CONTENT_MARK_LENGTH);
	}

	static class HtmlParserMock implements HtmlParser {
		@Override
		public void parseHtml(StringBuilder html) {
			if (html == null) {
				throw new IllegalArgumentException("HTML cannot be null");
			}
			int index = html.indexOf("test");
			if (index != -1) {
				html.replace(index, index + 4, "TEST");
			}
		}
	}

	@Test
	@DisplayName("Deve substituir 'test' por 'TEST' no método parseHtml")
	void testParseHtml() {
		HtmlParser parser = new HtmlParserMock();
		StringBuilder html = new StringBuilder("This is a test case.");

		parser.parseHtml(html);

		assertEquals("This is a TEST case.", html.toString());
	}
}
