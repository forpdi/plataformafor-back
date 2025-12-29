package org.forpdi.system.reports.pdf;

import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class TOCEvent extends PdfPageEventHelper {

	private int counter = 0;
	private List<SimpleEntry<String, SimpleEntry<String, Integer>>> toc = new ArrayList<>();

	private String lastText = "";

	@Override
	public void onGenericTag(PdfWriter writer, com.itextpdf.text.Document document, Rectangle rect, String text) {
		if (!text.equals(lastText)) {
			String name = "dest" + (counter++);
			int page = writer.getPageNumber();
			toc.add(new SimpleEntry<String, SimpleEntry<String, Integer>>(text,
					new SimpleEntry<String, Integer>(name, page)));
		}
		lastText = text;
	}

	public List<SimpleEntry<String, SimpleEntry<String, Integer>>> getTOC() {
		return toc;
	}
}
