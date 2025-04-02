package org.forpdi.system.reports.pdf;

import java.io.File;
import java.io.FileNotFoundException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PdfWriterFactoryTest {

	@Test
	void create_writer_without_toc_event() throws FileNotFoundException, DocumentException {
		Document document = new Document();
		File contentFile = new File("test.pdf");

		PdfWriter writer = PdfWriterFactory.create(document, contentFile);

		assertNotNull(writer);
		assertTrue(writer.isStrictImageSequence());
		assertNull(writer.getPageEvent());
	}

	@Test
	void create_writer_with_null_document() {
		Document document = null;
		File contentFile = new File("test.pdf");

		assertThrows(NullPointerException.class, () -> {
			PdfWriterFactory.create(document, contentFile);
		});
	}
}