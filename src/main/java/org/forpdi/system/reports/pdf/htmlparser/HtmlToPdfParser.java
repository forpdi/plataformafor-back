package org.forpdi.system.reports.pdf.htmlparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.forpdi.core.common.GeneralUtils;
import org.forpdi.system.reports.pdf.htmlparser.parsers.BackgroundColorParser;
import org.forpdi.system.reports.pdf.htmlparser.parsers.BorderParser;
import org.forpdi.system.reports.pdf.htmlparser.parsers.ImageParser;
import org.forpdi.system.reports.pdf.htmlparser.parsers.ListParser;
import org.forpdi.system.reports.pdf.htmlparser.parsers.StylesParser;
import org.forpdi.system.reports.pdf.htmlparser.parsers.TextParser;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.html.simpleparser.HTMLWorker;


public class HtmlToPdfParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(HtmlToPdfParser.class);
	
	private StringBuilder html;

	private StylesParser stylesParser;
	private ListParser listParser;
	private ImageParser imageParser;
	private BackgroundColorParser backgroundColorParser;
	private TextParser textParser;
	private BorderParser borderParser;
	
	public HtmlToPdfParser(String htmlStr, Document document) {
		if (!GeneralUtils.isEmpty(htmlStr)) {
			this.html = new StringBuilder(htmlStr);
			stylesParser = new StylesParser();
			listParser = new ListParser();
			imageParser = new ImageParser(document);
			backgroundColorParser = new BackgroundColorParser();
			textParser = new TextParser();
			borderParser = new BorderParser();
		}
	}

	public static void addElementsParsedToDocument(String html, Document document)
			throws FileNotFoundException, IOException, DocumentException {
		List<Element> elements = new HtmlToPdfParser(html, document).parse();
		for (Element element : elements) {
			document.add(element);
		}
	}
	
	public List<Element> parse() throws FileNotFoundException, IOException {
		if (html == null) {
			return Collections.emptyList();
		}

		applyParsers();
		
		return getParsedElements();
	}

	private void applyParsers() {
		stylesParser.parseHtml(html);
		listParser.parseHtml(html);
		imageParser.parseHtml(html);
		backgroundColorParser.parseHtml(html);
		textParser.parseHtml(html);
		borderParser.parseHtml(html);
	}
	
	private List<Element> getParsedElements() throws FileNotFoundException, IOException {
		List<Element> rawElements = parseHtmlToElements();
		List<Element> parsedElements = new LinkedList<Element>();
		
		for (int i = 0; i < rawElements.size(); i++) {
			Element element = rawElements.get(i);
			
			if (element instanceof com.itextpdf.text.List) {
				com.itextpdf.text.List list = backgroundColorParser.parse((com.itextpdf.text.List) element);
				rawElements.addAll(borderParser.parse(list).getItems());
			} else if (imageParser.elementContainsImage(element)) {
				try {
					Image image = imageParser.getNextImage();
					parsedElements.add(image);
				} catch (Exception e) {
					parsedElements.add(getImageNotFoundMessage());
					LOGGER.error("Imagem não foi exportada no documento pdf.", e);
				}
			} else if (element instanceof Paragraph) {
				Paragraph paragraph = backgroundColorParser.parse((Paragraph) element);
				paragraph = borderParser.parse(paragraph);
				parsedElements.add(textParser.parse(paragraph));
			}
		}
		
		return parsedElements;
	}

	private List<Element> parseHtmlToElements() throws FileNotFoundException, IOException {
		final String prefix = String.format("frisco-report-export-%d", System.currentTimeMillis());
		File outputDir = TempFilesManager.getTempDir();

		File htmlFile = new File(outputDir, String.format("%s-1.html", prefix));
		List<Element> elements = Collections.emptyList();
		
		try (FileOutputStream out = new FileOutputStream(htmlFile);
				FileReader fr = new FileReader(htmlFile.getPath())) {
			out.write(html.toString().getBytes());
			elements = HTMLWorker.parseToList(fr, null);
		}

		TempFilesManager.deleteTempFile(htmlFile);
		return elements;
	}

	private Paragraph getImageNotFoundMessage() {
		Paragraph imageNotFondText = new Paragraph("Imagem indisponível.");
		imageNotFondText.setAlignment(Element.ALIGN_CENTER);
		return imageNotFondText;
	}
}
