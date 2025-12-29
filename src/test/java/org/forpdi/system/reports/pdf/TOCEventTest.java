package org.forpdi.system.reports.pdf;

import com.itextpdf.text.pdf.PdfWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;

import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import static org.mockito.Mockito.*;

class TOCEventTest {

	@InjectMocks
	private TOCEvent tocEvent;

	@Mock
	private PdfWriter writer;

	@Mock
	private Document document;

	@Mock
	private Rectangle rect;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Verificar que a TOC é atualizada corretamente ao chamar onGenericTag")
	void testOnGenericTag_atualizaTOC() {
		String text1 = "Seção 1";
		String text2 = "Seção 2";

		when(writer.getPageNumber()).thenReturn(1);

		tocEvent.onGenericTag(writer, document, rect, text1);

		tocEvent.onGenericTag(writer, document, rect, text2);

		List<SimpleEntry<String, SimpleEntry<String, Integer>>> tocList = tocEvent.getTOC();

		assertEquals(2, tocList.size(), "A lista de TOC deve ter dois elementos");
		assertEquals("Seção 1", tocList.get(0).getKey(), "O título da primeira seção está incorreto");
		assertEquals("Seção 2", tocList.get(1).getKey(), "O título da segunda seção está incorreto");
		assertEquals(1, tocList.get(0).getValue().getValue(), "O número da página da primeira seção está incorreto");
	}

	@Test
	@DisplayName("Verificar que a TOC não é atualizada com o mesmo texto consecutivamente")
	void testOnGenericTag_naoAtualizaComTextoRepetido() {
		String text = "Seção Repetida";

		when(writer.getPageNumber()).thenReturn(1);

		tocEvent.onGenericTag(writer, document, rect, text);
		tocEvent.onGenericTag(writer, document, rect, text);

		List<SimpleEntry<String, SimpleEntry<String, Integer>>> tocList = tocEvent.getTOC();

		assertEquals(1, tocList.size(), "A lista de TOC não deve ter entradas duplicadas");
	}

	@Test
	@DisplayName("Verificar que a lista de TOC retorna corretamente")
	void testGetTOC() {
		tocEvent.onGenericTag(writer, document, rect, "Primeira Seção");
		tocEvent.onGenericTag(writer, document, rect, "Segunda Seção");

		List<SimpleEntry<String, SimpleEntry<String, Integer>>> tocList = tocEvent.getTOC();

		assertEquals(2, tocList.size(), "A TOC deve ter dois itens");
		assertEquals("Primeira Seção", tocList.get(0).getKey(), "O título da primeira seção está incorreto");
		assertEquals("Segunda Seção", tocList.get(1).getKey(), "O título da segunda seção está incorreto");
	}
}
