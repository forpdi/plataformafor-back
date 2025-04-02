package org.forpdi.system.reports.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.CMYKColor;

public class PDFSettings {

	public static final String FONT_NAME = "Verdana";

    // DEFINIÇÕES DE FONTE, MARGENS, ESPAÇAMENTO E CORES
    public static final Font TEXT_FONT = FontFactory.getFont(FONT_NAME, 12.0f, Font.NORMAL);
    public static final Font TEXT_FONT_BOLD = FontFactory.getFont(FONT_NAME, 14.0f, Font.BOLD); 
    public static final Font SUMMARY_FONT = FontFactory.getFont(FONT_NAME, 12.0f, Font.BOLD); 

    public static final Font CARD_FONT = FontFactory.getFont(FontFactory.HELVETICA, 48.0f, BaseColor.WHITE);
    public static final Font RISK_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12.0f, BaseColor.WHITE);
    public static final Font TITLE_FONT = FontFactory.getFont(FONT_NAME, 14.0f, Font.BOLD); 
    public static final Font SUB_TITLE_FONT = FontFactory.getFont(FONT_NAME, 14.0f);
    public static final Font FOOTER_FONT = FontFactory.getFont(FONT_NAME, 12.0f);
    public static final Font MAIN_TITLE_FONT = FontFactory.getFont(FONT_NAME, 16.0f, Font.BOLD); 
    public static final Font COVER_PAGE_TITLE_FONT = FontFactory.getFont(FONT_NAME, 20.0f, Font.BOLD); 
    public static final Font TABLE_FONT = FontFactory.getFont(FONT_NAME, 10.0f);
    public static final Font ITALIC_FONT = FontFactory.getFont(FONT_NAME, 12.0f, Font.ITALIC);
    public static final Font PAGE_NUMBER_FONT_LIGHT = FontFactory.getFont(FONT_NAME, 10.0f, Font.BOLD, BaseColor.WHITE); 
    public static final Font PAGE_NUMBER_FONT_DARK = FontFactory.getFont(FONT_NAME, 10.0f, Font.BOLD, BaseColor.BLACK); 

    // Cor cinza - cabeçalho das tabelas
    public static final CMYKColor HEADER_BG_COLOR = new CMYKColor(55, 45, 42, 7);
    public static final CMYKColor HEADER_BG_COLOR_FRISCO = new CMYKColor(20, 38, 0, 37);
    public static final CMYKColor HEADER_BG_COLOR_FRISCO_SUB = new CMYKColor(3, 5, 0, 8);
 
    // 0,8 cm acima e abaixo
    public static final float PARAGRAPH_SPACING = 22.6772f;
    public static final float PARAGRAPH_SPACING_SM = 11f;
    // Parágrafo com 1,25 cm na primeira linha
    public static final float FIRST_LINE_IDENT = 35.43307f;
    // 1,5 entrelinhas
    public static final float INTER_LINE_SPACING = TEXT_FONT.getCalculatedLeading(1.5f);

    public static final float HORIZONTAL_MARGIN = 85.0394f;

    public static final float VERTICAL_MARGIN = 70f;
    
    public static final Rectangle ROTATED_A4 = PageSize.A4.rotate();
}
