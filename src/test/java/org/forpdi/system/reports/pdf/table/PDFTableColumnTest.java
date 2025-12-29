package org.forpdi.system.reports.pdf.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;

public class PDFTableColumnTest {


    @Test
    @DisplayName("Testar a inicialização do construtor padrão")
    public void test_default_constructor_initialization_with_displayname() {
        PDFTableColumn<String> column = new PDFTableColumn<>();

        String title = column.columnTitle;
        Element content = column.contentExtractor.apply("test");

        assertEquals("", title, "Column title should be empty by default");
        assertTrue(content instanceof Phrase, "Content extractor should return Phrase");
        assertTrue(((Phrase)content).isEmpty(), "Default Phrase should be empty");
    }
}
