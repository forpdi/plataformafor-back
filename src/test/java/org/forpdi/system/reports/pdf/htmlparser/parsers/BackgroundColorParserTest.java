package org.forpdi.system.reports.pdf.htmlparser.parsers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class BackgroundColorParserTest {

	private BackgroundColorParser parser;

	@BeforeEach
	void setUp() {
		parser = new BackgroundColorParser();
	}

	@Test
	@DisplayName("Deve substituir e aplicar as cores de fundo corretamente no HTML")
	void testParseHtml() {
		StringBuilder html = new StringBuilder("<div style='background-color: #FF5733;'>Texto com fundo</div>");

		parser.parseHtml(html);

		String contentMarker = html.toString().contains("#FF5733") ? RandomStringUtils.random(10, true, true) : "";
		assertTrue(html.toString().contains(contentMarker));
	}

}
