package org.forpdi.system.reports.pdf;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.CMYKColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFSettingsTest {

	@Test
	@DisplayName("Dado a constante TEXT_FONT, quando acessamos a fonte, então ela deve ser do tipo correto e ter o nome e o tamanho esperado")
	void testTextFont() {
		Font expectedFont = FontFactory.getFont("Verdana", 12.0f, Font.NORMAL);

		Font result = PDFSettings.TEXT_FONT;

		assertEquals(expectedFont.getFamily(), result.getFamily());
		assertEquals(expectedFont.getSize(), result.getSize());
		assertEquals(expectedFont.getStyle(), result.getStyle());
	}

	@Test
	@DisplayName("Dado a constante TEXT_FONT_BOLD, quando acessamos a fonte, então ela deve ser do tipo correto e ter o nome e o tamanho esperado")
	void testTextFontBold() {
		Font expectedFont = FontFactory.getFont("Verdana", 14.0f, Font.BOLD);

		Font result = PDFSettings.TEXT_FONT_BOLD;

		assertEquals(expectedFont.getFamily(), result.getFamily());
		assertEquals(expectedFont.getSize(), result.getSize());
		assertEquals(expectedFont.getStyle(), result.getStyle());
	}

	@Test
	@DisplayName("Dado a constante HEADER_BG_COLOR, quando acessamos a cor, então ela deve ser do tipo correto e ter os valores CMYK esperados")
	void testHeaderBgColor() {
		CMYKColor expectedColor = new CMYKColor(55, 45, 42, 7);

		CMYKColor result = PDFSettings.HEADER_BG_COLOR;

		assertEquals(expectedColor.getCyan(), result.getCyan(), 0.01);
		assertEquals(expectedColor.getMagenta(), result.getMagenta(), 0.01);
		assertEquals(expectedColor.getYellow(), result.getYellow(), 0.01);
		assertEquals(expectedColor.getBlack(), result.getBlack(), 0.01);
	}

	@Test
	@DisplayName("Dado a constante HEADER_BG_COLOR_FRISCO, quando acessamos a cor, então ela deve ser do tipo correto e ter os valores CMYK esperados")
	void testHeaderBgColorFrisco() {
		CMYKColor expectedColor = new CMYKColor(20, 38, 0, 37);

		CMYKColor result = PDFSettings.HEADER_BG_COLOR_FRISCO;

		assertEquals(expectedColor.getCyan(), result.getCyan(), 0.01);
		assertEquals(expectedColor.getMagenta(), result.getMagenta(), 0.01);
		assertEquals(expectedColor.getYellow(), result.getYellow(), 0.01);
		assertEquals(expectedColor.getBlack(), result.getBlack(), 0.01);
	}

	@Test
	@DisplayName("Dado a constante PARAGRAPH_SPACING, quando acessamos o valor, então ele deve ser o valor esperado")
	void testParagraphSpacing() {
		float expectedSpacing = 22.6772f;

		float result = PDFSettings.PARAGRAPH_SPACING;

		assertEquals(expectedSpacing, result, 0.01);
	}

	@Test
	@DisplayName("Dado a constante FIRST_LINE_IDENT, quando acessamos o valor, então ele deve ser o valor esperado")
	void testFirstLineIdent() {
		float expectedIndent = 35.43307f;

		float result = PDFSettings.FIRST_LINE_IDENT;

		assertEquals(expectedIndent, result, 0.01);
	}

	@Test
	@DisplayName("Dado a constante INTER_LINE_SPACING, quando acessamos o valor, então ele deve ser o valor esperado com 1,5 de entrelinha")
	void testInterLineSpacing() {
		float expectedSpacing = PDFSettings.TEXT_FONT.getCalculatedLeading(1.5f);

		float result = PDFSettings.INTER_LINE_SPACING;

		assertEquals(expectedSpacing, result, 0.01);
	}

	@Test
	@DisplayName("Dado a constante ROTATED_A4, quando acessamos a página, então ela deve ser o formato A4 rotacionado")
	void testRotatedA4() {
		Rectangle expectedPage = PageSize.A4.rotate();

		Rectangle result = PDFSettings.ROTATED_A4;

		assertEquals(expectedPage.getWidth(), result.getWidth());
		assertEquals(expectedPage.getHeight(), result.getHeight());
	}
}
