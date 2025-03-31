package org.forpdi.system.reports.pdf.htmlparser.parsers;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;

class TextParserTest {

	private TextParser textParser;

	@BeforeEach
	@DisplayName("Configuração inicial para os testes do TextParser")
	void setUp() {
		textParser = new TextParser();
	}

	@Test
	@DisplayName("Testar alinhamento padrão de um parágrafo")
	void testDefaultParagraphAlignment() {
		Paragraph paragraph = new Paragraph("Texto de exemplo");
		textParser.parse(paragraph);

		assertEquals(Element.ALIGN_JUSTIFIED, paragraph.getAlignment(), "O alinhamento padrão deveria ser justificado.");
	}


	@Test
	@DisplayName("Testar remoção de classes de alinhamento do HTML")
	void testRemoveAlignmentClassFromHtml() {
		StringBuilder html = new StringBuilder("<p class=\"ql-align-right\">Texto</p>");
		textParser.parseHtml(html);

		assertTrue(html.toString().contains("Texto"), "O texto deveria permanecer no HTML.");
		assertFalse(html.toString().contains("ql-align-right"), "A classe de alinhamento deveria ser removida.");
	}


	@Test
	@DisplayName("Testar alinhamento inexistente não altera parágrafo")
	void testNonExistentAlignmentClassDoesNotAlterParagraph() {
		Paragraph paragraph = new Paragraph("Texto sem marcador de alinhamento");
		Paragraph parsedParagraph = textParser.parse(paragraph);

		assertEquals(paragraph, parsedParagraph, "O parágrafo sem marcador deveria permanecer inalterado.");
	}

	@Test
	void test_parse_list_with_null_chunks() {
		TextParser parser = new TextParser();
		com.itextpdf.text.List list = new com.itextpdf.text.List();
		ListItem item = new ListItem();
		item.add((Chunk)null);
		list.add(item);

		com.itextpdf.text.List result = parser.parse(list);

		assertEquals(1, result.getItems().size());
		ListItem parsedItem = (ListItem) result.getItems().get(0);
		assertTrue(parsedItem.getChunks().isEmpty());
	}

	@Test
	void test_parse_list_with_chunks_shorter_than_content_mark_length() {
		TextParser parser = new TextParser();
		com.itextpdf.text.List list = new com.itextpdf.text.List();
		ListItem item1 = new ListItem();
		ListItem item2 = new ListItem();

		Chunk chunk1 = new Chunk("Short1");
		Chunk chunk2 = new Chunk("Short2");

		item1.add(chunk1);
		item2.add(chunk2);
		list.add(item1);
		list.add(item2);

		com.itextpdf.text.List result = parser.parse(list);

		assertEquals(2, result.getItems().size());
		ListItem parsedItem1 = (ListItem) result.getItems().get(0);
		ListItem parsedItem2 = (ListItem) result.getItems().get(1);

		assertEquals("Short1", parsedItem1.getChunks().get(0).getContent());
		assertEquals("Short2", parsedItem2.getChunks().get(0).getContent());
	}

	@Test
	void test_parse_list_with_invalid_class_does_not_throw() {
		TextParser parser = new TextParser();
		com.itextpdf.text.List list = new com.itextpdf.text.List();
		ListItem item = new ListItem();

		String contentMarker = "INVALID1234";
		Chunk chunk = new Chunk(contentMarker + "Some content");

		item.add(chunk);
		list.add(item);

		parser.parse(list);

		assertTrue(true, "Nenhuma exceção foi lançada, como esperado.");
	}

	private void setPrivateField(String fieldName, Object target, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
}
