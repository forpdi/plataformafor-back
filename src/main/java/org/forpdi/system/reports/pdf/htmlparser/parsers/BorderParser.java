package org.forpdi.system.reports.pdf.htmlparser.parsers;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.RandomStringUtils;
import org.forpdi.core.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.html.WebColors;

public class BorderParser implements HtmlParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(BorderParser.class);

	private static final String BORDER_ATT_NAME = "border";
	private static final String ELEMENT_WITH_BORDER_REGEX = "border[^>]+>";
	private static final String BORDER_REGEX = "(?<=border:).+?;";
	
	private Map<String, String> bordersMap = new HashMap<>();
	private Map<Chunk, PdfPTable> bordersParsed = new HashMap<>();
	
	@Override
	public void parseHtml(StringBuilder html) {
		Pattern pattern = Pattern.compile(ELEMENT_WITH_BORDER_REGEX);
		Matcher matcher = pattern.matcher(html);
		List<Replacement> toReplace = new LinkedList<>();
		while (matcher.find()) {
			String partOfTagThatHasBorderAttribute = matcher.group();
			String border = getBorderAttributes(partOfTagThatHasBorderAttribute);
			String contentMarker = RandomStringUtils.random(CONTENT_MARK_LENGTH, 0, 0, true, true, null, new SecureRandom());
			Replacement replacement = getStyleAttributesReplacement(partOfTagThatHasBorderAttribute, border, contentMarker);
			toReplace.add(replacement);
			bordersMap.put(contentMarker, border);
		}
		
		for (Replacement replacement : toReplace) {
			Util.replace(html, replacement.from, replacement.to);
		}
	}
	
	public Paragraph parse(Paragraph paragraph) {
		Paragraph parsedParagraph = new Paragraph();
		for (Chunk chunk : getBordersParsed(paragraph)) {
			if (bordersParsed.get(chunk) != null) {
				parsedParagraph.add(bordersParsed.get(chunk));
			} else {
				parsedParagraph.add(chunk);
			}
		}
		return parsedParagraph;
	}

	public com.itextpdf.text.List parse(com.itextpdf.text.List list) {
		com.itextpdf.text.List parsedList = new com.itextpdf.text.List();
		for (Element listItem : list.getItems()) {
			List<Chunk> parsedElements = getBordersParsed(listItem);
			ListItem parsedListItem = new ListItem();
			for (Chunk chunk : parsedElements) {
				if (bordersParsed.get(chunk) != null) {
					parsedListItem.add(bordersParsed.get(chunk));
				} else {
					parsedListItem.add(chunk);
				}
			}
			parsedList.add(parsedListItem);
		}
		
		return parsedList;
	}
	
	private String getBorderAttributes(String partOfTagThatHasBorderAttribute) {
		Pattern pattern = Pattern.compile(BORDER_REGEX);
		Matcher matcher = pattern.matcher(partOfTagThatHasBorderAttribute);
		if (matcher.find()) {
			return matcher.group()
					.trim()
					.replace(";", "");
		}
		return "1 1 1 1 #000";
	}

	private Replacement getStyleAttributesReplacement(String partOfTagThatHasBorderAttribute,
			String border, String contentMarker) {
		StringBuilder replaceFrom = new StringBuilder()
				.append(BORDER_ATT_NAME)
				.append(":")
				.append(border)
				.append(";");
		String partOfTagWithoutBorder = partOfTagThatHasBorderAttribute.replace(replaceFrom , "");
		
		return new Replacement(partOfTagThatHasBorderAttribute, partOfTagWithoutBorder + contentMarker);
	}

	private List<Chunk> getBordersParsed(Element element) {
		List<Chunk> chunks = element.getChunks();
		List<Chunk> parsedChunks = new ArrayList<Chunk>(chunks.size());
		
		for (Chunk chunk : chunks) {
			if (chunk.getContent().length() < CONTENT_MARK_LENGTH) {
				parsedChunks.add(chunk);
				bordersParsed.put(chunk, null);
				continue;
			}
			String contentMark = chunk.getContent().substring(0, CONTENT_MARK_LENGTH);
			String border = bordersMap.get(contentMark);
			
			if (border != null) {
				PdfPCell cell = new PdfPCell();
				PdfPTable table = new PdfPTable(1);
				table.setHorizontalAlignment(Element.ALIGN_JUSTIFIED_ALL);
				table.setWidthPercentage(100);
				
				Chunk parsed = new Chunk(chunk.getContent().replace(contentMark, ""));
				List<Float> borderSizes = getBorderSizes(border);

				if (borderSizes != null) {
					cell.setBorderWidthTop(borderSizes.get(0));
					cell.setBorderWidthRight(borderSizes.get(1));
					cell.setBorderWidthBottom(borderSizes.get(2));
					cell.setBorderWidthLeft(borderSizes.get(3));					
					cell.setBorderColor(getColor(border));
					cell.setPaddingLeft(15);
					cell.setPaddingBottom(13);
				}

				parsed.setFont(chunk.getFont());
				cell.addElement(parsed);
				table.addCell(cell);
				
				parsedChunks.add(parsed);
				bordersParsed.put(parsed, table);
			} else {
				bordersParsed.put(chunk, null);
				parsedChunks.add(chunk);
			}
		}
		return parsedChunks;
	}
	
	private List<Float> getBorderSizes(String border) {
		try {
			List<Float> borderSizes = new ArrayList<Float>();
			
			for (int i = 0; i < border.split(" ").length - 1; i++) {
				borderSizes.add(Float.parseFloat(border.split(" ")[i]));
			}
			return borderSizes;
		} catch (Exception e) {
			LOGGER.error("Unrecognized sizes: " + border);
			return null;
		}
	}
	
	private BaseColor getColor(String border) {
		try {
			return WebColors.getRGBColor(border.split("(?<=#)")[1]);
		} catch (Exception e) {
			LOGGER.error("Unrecognized color: " + border);
			return WebColors.getRGBColor("#000");
		}
	}
}
