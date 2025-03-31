package org.forpdi.system.reports.pdf.htmlparser.parsers;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.Base64;
import java.lang.reflect.Method;

import com.itextpdf.text.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImageParserTest {

	private ImageParser imageParser;
	private Document document;

	@BeforeEach
	void setUp() {
		document = new Document();
		imageParser = new ImageParser(document);
	}

	@Test
	void testParseHtmlReplacesImagesWithMarkers() {
		String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(new byte[]{1, 2, 3});
		StringBuilder html = new StringBuilder("<p>Text before image</p><img src=\"" + base64Image + "\"><p>Text after image</p>");

		imageParser.parseHtml(html);

		String expectedHtml = "<p>Text before image</p><p>||IMAGE||</p><p>Text after image</p>";
		assertEquals(expectedHtml, html.toString());
	}

	@Test
	void testElementContainsImage() {
		String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(new byte[]{1, 2, 3});
		StringBuilder html = new StringBuilder("<p>||IMAGE||</p>");
		imageParser.parseHtml(html);

		assertTrue(imageParser.elementContainsImage(new com.itextpdf.text.Paragraph("||IMAGE||")));
	}

	@Test
	void testHasNextImage() {
		String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(new byte[]{1, 2, 3});
		StringBuilder html = new StringBuilder("<img src=\"" + base64Image + "\">");

		imageParser.parseHtml(html);
		assertTrue(imageParser.hasNextImage());
	}

	@Test
	void testNoImagesInHtml() {
		StringBuilder html = new StringBuilder("<p>No images here</p>");
		imageParser.parseHtml(html);

		assertFalse(imageParser.hasNextImage());
	}

	@Test
	void test_null_element_returns_false() {
		Document document = new Document();
		ImageParser parser = new ImageParser(document);
		Element element = null;

		boolean result = parser.elementContainsImage(element);

		assertFalse(result);
	}

	@Test
	void test_get_next_image_returns_valid_image() throws Exception {
		Document document = new Document();
		ImageParser parser = new ImageParser(document);
		StringBuilder html = new StringBuilder();
		html.append("<img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==\">");
		parser.parseHtml(html);

		Image result = parser.getNextImage();

		assertNotNull(result);
		assertInstanceOf(Image.class, result);
	}

	@Test
	void test_image_wider_than_document() {
		Document document = new Document();
		ImageParser parser = new ImageParser(document);
		Paragraph paragraph = new Paragraph();
		paragraph.add("||IMAGE||");

		boolean result = parser.elementContainsImage(paragraph);

		assertTrue(result);
	}

}
