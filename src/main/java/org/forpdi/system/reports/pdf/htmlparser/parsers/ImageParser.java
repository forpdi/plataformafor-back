package org.forpdi.system.reports.pdf.htmlparser.parsers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.forpdi.core.utils.RegexUtil;
import org.forpdi.core.utils.Util;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;

public class ImageParser implements HtmlParser {
	private static final String IMAGE_MARKER = "||IMAGE||";

	private Document document;
	private Queue<String> imgElementsQueue;
	
	public ImageParser(Document document) {
		this.document = document;
	}

	@Override
	public void parseHtml(StringBuilder html) {
		createImageQueue(html);
		replaceHtmlImages(html);
	}

	public boolean elementContainsImage(Element element) {
		if (element instanceof Paragraph) {
			Paragraph paragraph = (Paragraph) element;
			return paragraph.getContent().contains(IMAGE_MARKER);
		}
		return false;
	}
	
	public Image getNextImage() throws BadElementException, MalformedURLException, IOException {
		return parseImage();
	}
	
	public boolean hasNextImage() {
		return !imgElementsQueue.isEmpty();
	}

	private void createImageQueue(StringBuilder html) {
		imgElementsQueue = new LinkedList<>();
		String regex = RegexUtil.getHTMLOpenTagRegex("img");
		Matcher m = Pattern.compile(regex).matcher(html);
		while (m.find()) {
			String match = m.group();
			imgElementsQueue.add(match);
		}
	}

	private void replaceHtmlImages(StringBuilder html) {
		String paragraphElementMarker = "<p>" + IMAGE_MARKER + "</p>";
		for (String imgElement : imgElementsQueue) {
			Util.replace(html, imgElement, paragraphElementMarker);
		}
	}

	private Image parseImage() throws BadElementException, MalformedURLException, IOException {
		if (!hasNextImage()) {
			return null;
		}

		Image image = getImageFromHtmlElement(imgElementsQueue.poll());

		fitImageSize(image);
		
		return image;
	}

	private void fitImageSize(Image image) {
		float documentWidth = calculateDocumentWidth(document);
		float documentHeight = calculateDocumentHeight(document);
		float widthRatio = image.getWidth() / documentWidth;
		float heightRatio = image.getHeight() / documentHeight;
		if (imageWiderThanDocument(widthRatio) && widthRatio > heightRatio) {
			image.scalePercent((documentWidth / image.getWidth()) * 100);
		} else if (imageLongerThanDocument(heightRatio)) {
			image.scalePercent((documentHeight / image.getHeight()) * 100);
		}
	}

	private float calculateDocumentWidth(Document document) {
		return document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
	}

	private float calculateDocumentHeight(Document document) {
		return document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
	}

	private Image getImageFromHtmlElement(String imgTag) throws BadElementException, MalformedURLException, IOException {
		String encodedStr = imgTag.replaceAll("<img src=\"data:image/.*;base64,", "").replace("\">", "");
		byte bytes[] = java.util.Base64.getDecoder().decode(encodedStr);
		return Image.getInstance(bytes);
	}
	
	private boolean imageWiderThanDocument(float widthRatio) {
		return widthRatio > 1;
	}
	
	private boolean imageLongerThanDocument(float heightRatio) {
		return heightRatio > 1;
	}
}
