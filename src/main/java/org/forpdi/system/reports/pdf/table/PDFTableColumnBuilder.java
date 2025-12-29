package org.forpdi.system.reports.pdf.table;

import java.util.LinkedList;
import java.util.function.Function;

import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;

public class PDFTableColumnBuilder<T> {
	private PDFTableGeneratorBuilder<T> tableGeneratorBuilder;
	private PDFTableColumn<T> column = new PDFTableColumn<T>();
	private Function<T, PdfPCell> cellRenderer;
	
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

	public PDFTableColumnBuilder<T> withCellRenderer(Function<T, PdfPCell> renderer) {
		this.cellRenderer = renderer;
		return this;
	}
	
	public PDFTableGeneratorBuilder<T> createColumn() {
		PDFTableGenerator<T> tableGenerator = tableGeneratorBuilder.tableGenerator;
		if (tableGenerator.cellRenderers == null) {
			tableGenerator.cellRenderers = new LinkedList<>();
		}
		tableGenerator.columnTitles.add(column.columnTitle);
		tableGenerator.contentExtractors.add(column.contentExtractor);
		tableGenerator.cellRenderers.add(this.cellRenderer);
		return tableGeneratorBuilder;
	}
}
