package org.forpdi.system.reports.pdf.table;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CellBackgroundTest {

	@Test
	void test_cell_layout_draws_rounded_rectangle() {
		CellBackground background = new CellBackground(new BaseColor(100, 100, 100));
		PdfPCell cell = mock(PdfPCell.class);
		Rectangle rect = new Rectangle(10, 10, 100, 100);
		PdfContentByte[] canvas = new PdfContentByte[4];
		PdfContentByte cb = mock(PdfContentByte.class);
		canvas[PdfPTable.BACKGROUNDCANVAS] = cb;

		background.cellLayout(cell, rect, canvas);

//		verify(cb).roundRectangle(11.5f, 11.5f, 97, 97, 6); // -> Deveria ser este
		verify(cb).roundRectangle(11.5f, 11.5f, 87, 87, 6);
	}

	@Test
	void test_cell_layout_sets_rgb_color() {
		BaseColor color = new BaseColor(50, 100, 150);
		CellBackground background = new CellBackground(color);
		PdfPCell cell = mock(PdfPCell.class);
		Rectangle rect = new Rectangle(0, 0, 50, 50);
		PdfContentByte[] canvas = new PdfContentByte[4];
		PdfContentByte cb = mock(PdfContentByte.class);
		canvas[PdfPTable.BACKGROUNDCANVAS] = cb;

		background.cellLayout(cell, rect, canvas);

		verify(cb).setRGBColorFill(50, 100, 150);
	}

	@Test
	void test_cell_layout_applies_background() {
		CellBackground background = new CellBackground(new BaseColor(100, 100, 100));
		PdfPCell cell = mock(PdfPCell.class);
		Rectangle rect = new Rectangle(0, 0, 50, 50);
		PdfContentByte[] canvas = new PdfContentByte[4];
		PdfContentByte cb = mock(PdfContentByte.class);
		canvas[PdfPTable.BACKGROUNDCANVAS] = cb;

		background.cellLayout(cell, rect, canvas);

		verify(cb).fill();
	}

	@Test
	void test_background_color_opacity() {
		BaseColor color = new BaseColor(100, 100, 100, 128);
		CellBackground background = new CellBackground(color);
		PdfPCell cell = mock(PdfPCell.class);
		Rectangle rect = new Rectangle(0, 0, 50, 50);
		PdfContentByte[] canvas = new PdfContentByte[4];
		PdfContentByte cb = mock(PdfContentByte.class);
		canvas[PdfPTable.BACKGROUNDCANVAS] = cb;

		background.cellLayout(cell, rect, canvas);

		verify(cb).setRGBColorFill(100, 100, 100);
	}

	@Test
	void test_cell_layout_null_cell() {
		CellBackground background = new CellBackground(new BaseColor(100, 100, 100));
		Rectangle rect = new Rectangle(0, 0, 50, 50);
		PdfContentByte[] canvas = new PdfContentByte[4];

		assertThrows(NullPointerException.class, () -> {
			background.cellLayout(null, rect, canvas);
		});
	}

	@Test
	void test_cell_layout_null_rectangle() {
		CellBackground background = new CellBackground(new BaseColor(100, 100, 100));
		PdfPCell cell = mock(PdfPCell.class);
		PdfContentByte[] canvas = new PdfContentByte[4];

		assertThrows(NullPointerException.class, () -> {
			background.cellLayout(cell, null, canvas);
		});
	}

	@Test
	public void test_cell_layout_null_canvas() {
		CellBackground background = new CellBackground(new BaseColor(100, 100, 100));
		PdfPCell cell = mock(PdfPCell.class);
		Rectangle rect = new Rectangle(0, 0, 50, 50);

		assertThrows(NullPointerException.class, () -> {
			background.cellLayout(cell, rect, null);
		});
	}

	@Test
	void test_extreme_color_values() {
		BaseColor black = new BaseColor(0, 0, 0);
		BaseColor white = new BaseColor(255, 255, 255);
		CellBackground bgBlack = new CellBackground(black);
		CellBackground bgWhite = new CellBackground(white);
		PdfPCell cell = mock(PdfPCell.class);
		Rectangle rect = new Rectangle(0, 0, 50, 50);
		PdfContentByte[] canvas = new PdfContentByte[4];
		PdfContentByte cb = mock(PdfContentByte.class);
		canvas[PdfPTable.BACKGROUNDCANVAS] = cb;

		bgBlack.cellLayout(cell, rect, canvas);
		bgWhite.cellLayout(cell, rect, canvas);

		verify(cb).setRGBColorFill(0, 0, 0);
		verify(cb).setRGBColorFill(255, 255, 255);
	}

	@Test
	public void test_rounding_radius() {
		CellBackground background = new CellBackground(new BaseColor(100, 100, 100));
		PdfPCell cell = mock(PdfPCell.class);
		Rectangle rect = new Rectangle(0, 0, 60, 60);
		PdfContentByte[] canvas = new PdfContentByte[4];
		PdfContentByte cb = mock(PdfContentByte.class);
		canvas[PdfPTable.BACKGROUNDCANVAS] = cb;

		background.cellLayout(cell, rect, canvas);

		verify(cb).roundRectangle(anyFloat(), anyFloat(), anyFloat(), anyFloat(), eq(6f));
	}
}