package org.forpdi.system.reports.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfWriterFactory {

	public static PdfWriter create(Document document, File contentFile)
			throws FileNotFoundException, DocumentException {
		return create(document, contentFile, null);
	}

	public static PdfWriter create(Document document, File contentFile, TOCEvent event)
			throws FileNotFoundException, DocumentException {
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(contentFile));
		writer.setPageEvent(event);
		writer.setStrictImageSequence(true);
		
		return writer;
	}
	
}
