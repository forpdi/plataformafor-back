package org.forpdi.system.reports.pdf.table;

import java.util.function.Function;

import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;

public class PDFTableColumn<T> {
	protected String columnTitle = "";
	protected Function<T, Element> contentExtractor = item -> new Phrase();
}
