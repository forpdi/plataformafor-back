package org.forpdi.system.reports.pdf.table;

import java.util.function.Function;

import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;

public class PDFTableColumnBuilder<T> {
	private PDFTableGeneratorBuilder<T> tableGeneratorBuilder;
	private PDFTableColumn<T> column = new PDFTableColumn<T>();
	
	protected PDFTableColumnBuilder(PDFTableGeneratorBuilder<T> pdfGeneratorBuilder) {
		this.tableGeneratorBuilder = pdfGeneratorBuilder;
	}

	public PDFTableColumnBuilder<T> withColumnTitle(String columnTitle) {
		this.column.columnTitle = columnTitle;
		return this;
	}
	
	public PDFTableColumnBuilder<T> withContentExtractor(Function<T, String> contentExtractor) {
		this.column.contentExtractor = item -> new Phrase(contentExtractor.apply(item));
		return this;
	}
	
	public PDFTableColumnBuilder<T> withFormattedContentExtractor(Function<T, Element> contentExtractor) {
		this.column.contentExtractor = contentExtractor;
		return this;
	}		
	
	public PDFTableGeneratorBuilder<T> createColumn() {
		PDFTableGenerator<T> tableGenerator = tableGeneratorBuilder.tableGenerator;
		tableGenerator.columnTitles.add(column.columnTitle);
		tableGenerator.contentExtractors.add(column.contentExtractor);
		return tableGeneratorBuilder;
	}
}
