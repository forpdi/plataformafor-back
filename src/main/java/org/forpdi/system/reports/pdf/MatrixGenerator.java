package org.forpdi.system.reports.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.forpdi.core.utils.Util;
import org.forpdi.system.reports.pdf.table.CellBackground;
import org.forpdi.system.reports.pdf.table.TempFilesManager;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPRow;
import com.itextpdf.text.pdf.PdfPTable;

public class MatrixGenerator {	
	private MatrixCell[][] matrixCells;
	
	public MatrixGenerator(MatrixCell[][] matrixCells) {
		this.matrixCells = matrixCells;
	}

	public PdfPTable generate() throws IOException {
		
		String html = getHtmlTable();
		
		List<Element> elements = parseHtmlToElements(html);
		
		PdfPTable matrix = createMatrix(elements);
				
		if (matrix == null) {
			throw new AssertionError("Table not created");
		}
		
		return matrix;
	}

	private String getHtmlTable() {
		StringBuilder html = new StringBuilder("<table>");
		Integer maxTextLength = 18 - 2 * matrixCells[0].length;
		for (int i = 0; i < matrixCells.length; i++) {
			html.append("<tr>");
			boolean isLastLine = i == matrixCells.length - 1;
			for (int j = 0; j < matrixCells[i].length; j++) {
				boolean isFirstColumn = j == 0;
				MatrixCell cell = matrixCells[i][j];
				String openTag = isLastLine || isFirstColumn
						? "<td style='text-align:center;'>"
						: "<td style='font-weight:bold;text-align:center;color:#fff;'>";
				String value = cell.value != null ? cell.value : "";
				html.append(openTag)
					.append(Util.truncateString(value, maxTextLength))
					.append("</td>");
			}
			html.append("</tr>");
		}
		html.append("</table>");
		return html.toString();
	}
	
	private List<Element> parseHtmlToElements(String html) throws IOException {
		final String prefix = String.format("matrix-generator-%d", System.currentTimeMillis());
		File outputDir = TempFilesManager.getTempDir();

		List<Element> elements = Collections.emptyList();
		File htmlFile = new File(outputDir, String.format("%s-1.html", prefix));
		try (FileOutputStream out = new FileOutputStream(htmlFile);
				FileReader fr = new FileReader(htmlFile.getPath())) {
			out.write(html.getBytes());
			elements = HTMLWorker.parseToList(fr, null);
		} finally {
			TempFilesManager.deleteTempFile(htmlFile);
		}

		return elements;
	}
	
	private PdfPTable createMatrix(List<Element> elements) {
		PdfPTable matrix = null;

		for (Element element : elements) {
			if (element instanceof PdfPTable) {
				matrix = (PdfPTable) element;
				processMatrixRows(matrix);
			}
		}

		return matrix;
	}

	private void processMatrixRows(PdfPTable matrix) {
		ArrayList<PdfPRow> rows = matrix.getRows();
		for (int i = 0; i < rows.size(); i++) {
			PdfPCell[] cells = rows.get(i).getCells();
			boolean isLastLine = i == rows.size() - 1;

			for (int j = 0; j < cells.length; j++) {
				processCell(cells[j], i, j, isLastLine);
			}
		}
	}

	private void processCell(PdfPCell cell, int rowIndex, int columnIndex, boolean isLastLine) {
		boolean isFirstColumn = columnIndex == 0;

		cell.setBorder(0);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPaddingBottom(15);
		cell.setPaddingTop(15);
		cell.setPaddingLeft(10);
		cell.setPaddingRight(10);
		cell.setFixedHeight(50);

		if (!isFirstColumn && !isLastLine) {
			BaseColor color = WebColors.getRGBColor(matrixCells[rowIndex][columnIndex].codeColor);
			CellBackground cellBackground = new CellBackground(color);
			cell.setCellEvent(cellBackground);
		}
	}

	public static class MatrixCell {
		private String value;
		private String codeColor;

		public MatrixCell(String value, String codeColor) {
			this.value = value;
			this.codeColor = codeColor;
		}
	}
}
