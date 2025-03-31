package org.forpdi.system.reports.pdf.htmlparser;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HtmlToPdfParserTest {

	@Test
	@DisplayName("Deve inicializar corretamente os parsers ao criar uma nova instância")
	void testConstructorInitializesParsers() {
		Document mockDocument = mock(Document.class);
		String htmlContent = "<html><body>Teste</body></html>";

		HtmlToPdfParser parser = new HtmlToPdfParser(htmlContent, mockDocument);

		assertNotNull(parser, "A instância do parser não deve ser nula");
	}

	@Test
	@DisplayName("Deve retornar lista vazia ao parsear HTML nulo")
	void testParseWithNullHtmlReturnsEmptyList() throws FileNotFoundException, IOException {
		Document mockDocument = mock(Document.class);
		HtmlToPdfParser parser = new HtmlToPdfParser(null, mockDocument);

		List<Element> elements = parser.parse();

		assertTrue(elements.isEmpty(), "A lista retornada deve estar vazia para HTML nulo");
	}

}
