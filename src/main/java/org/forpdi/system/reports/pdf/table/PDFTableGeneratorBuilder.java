package org.forpdi.system.reports.pdf.table;

import java.util.Arrays;
import java.util.LinkedList;

public class PDFTableGeneratorBuilder<T> {
	protected PDFTableGenerator<T> tableGenerator = new PDFTableGenerator<>();
	private PDFTableColumnBuilder<T> columnBuilder = new PDFTableColumnBuilder<>(this);
	
	public PDFTableGeneratorBuilder() {
		tableGenerator.columnTitles = new LinkedList<>();
		tableGenerator.contentExtractors = new LinkedList<>();
	}

	public PDFTableGeneratorBuilder<T> withItems(Iterable<T> items) {
		this.tableGenerator.items = items;
		return this;
	}

	public PDFTableGeneratorBuilder<T> withTitle(String title) {
		this.tableGenerator.title = title;
		return this;
	}
	
	public PDFTableGeneratorBuilder<T> withRelativeColumnWidths(float... relativeColumnWidths) {
		this.tableGenerator.relativeColumnWidths = relativeColumnWidths;
		return this;
	}

	public PDFTableColumnBuilder<T> addColmn() {
		return columnBuilder;
	}

	public PDFTableGenerator<T> create() {
		final int cols = tableGenerator.columnTitles.size();

		while (tableGenerator.contentExtractors.size() < cols) {
			tableGenerator.contentExtractors.add(null);
		}
		if (tableGenerator.cellRenderers == null) {
			tableGenerator.cellRenderers = new LinkedList<>();
		}
		while (tableGenerator.cellRenderers.size() < cols) {
			tableGenerator.cellRenderers.add(null);
		}
		if (tableGenerator.relativeColumnWidths == null) {
			tableGenerator.relativeColumnWidths = new float[tableGenerator.contentExtractors.size()];
			Arrays.fill(tableGenerator.relativeColumnWidths, 1);
		} else if (tableGenerator.relativeColumnWidths.length != tableGenerator.contentExtractors.size()) {
			throw new IllegalArgumentException("Error to build a pdf table: " + "relative colmuns widths should be equals to number of columns");
		}

		if (tableGenerator.items == null) {
			throw new IllegalArgumentException("Error to build a pdf table: " + "items should not be null");
		}
		
		return tableGenerator;
	}
}
