package org.forpdi.system.reports.pdf.table;

import java.util.List;
import java.util.function.Function;

import org.forpdi.system.reports.pdf.PDFSettings;

import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

public class PDFTableGenerator<T> {

	protected Iterable<T> items;
	protected String title;
	protected float[] relativeColumnWidths;
	protected List<Function<T, Element>> contentExtractors;
	protected List<String> columnTitles;
	protected List<Function<T, PdfPCell>> cellRenderers;

	protected PDFTableGenerator() {
	}

	public PdfPTable generate() {
		PdfPTable table = new PdfPTable(relativeColumnWidths);
		table.setHeaderRows(2);
		table.setWidthPercentage(100);
		PdfPCell cell = new PdfPCell(new Phrase(title, PDFSettings.TITLE_FONT));
		cell.setBackgroundColor(PDFSettings.HEADER_BG_COLOR_FRISCO);
		cell.setBorderColor(PDFSettings.HEADER_BG_COLOR_FRISCO);
		cell.setColspan(relativeColumnWidths.length);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);

		for (String columnTitle : columnTitles) {
			cell = new PdfPCell(new Phrase(columnTitle, PDFSettings.TITLE_FONT));
			cell.setBackgroundColor(PDFSettings.HEADER_BG_COLOR_FRISCO_SUB);
			cell.setBorderColor(PDFSettings.HEADER_BG_COLOR_FRISCO);
			table.addCell(cell);
		}

		for (T item : items) {
			for (int c = 0; c < columnTitles.size(); c++) {
				PdfPCell dataCell;
				if (cellRenderers != null && c < cellRenderers.size() && cellRenderers.get(c) != null) {
					dataCell = cellRenderers.get(c).apply(item);
				} else {
					java.util.function.Function<T, Element> extractor =
						(contentExtractors != null && c < contentExtractors.size())
							? contentExtractors.get(c)
							: null;

					dataCell = new PdfPCell();
					dataCell.setBorderColor(PDFSettings.HEADER_BG_COLOR_FRISCO);

					if (extractor != null) {
						dataCell.addElement(extractor.apply(item));
					} else {
						dataCell.addElement(new Phrase("", PDFSettings.RISK_FONT));
					}
				}
				dataCell.setBorderColor(PDFSettings.HEADER_BG_COLOR_FRISCO);
				table.addCell(dataCell);
			}
		}

		return table;
	}

	public Iterable<T> getItems() {
		return items;
	}

	public String getTitle() {
		return title;
	}

	public float[] getRelativeColumnWidths() {
		return relativeColumnWidths;
	}

	public List<Function<T, Element>> getContentExtractors() {
		return contentExtractors;
	}

	public List<String> getColumnTitles() {
		return columnTitles;
	}
}