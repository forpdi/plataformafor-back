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
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.html.WebColors;

public class BackgroundColorParser implements HtmlParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundColorParser.class);

	private static final String BG_COLOR_ATT_NAME = "background-color";
	private static final String ELEMENT_WITH_BG_REGEX = "background-color[^>]+>";
	private static final String BG_COLOR_REGEX = "(?<=background-color:).+?;";
	
	private Map<String, String> backgroundColorsMap = new HashMap<>();
	
	@Override
	public void parseHtml(StringBuilder html) {
		Pattern pattern = Pattern.compile(ELEMENT_WITH_BG_REGEX);
		Matcher matcher = pattern.matcher(html);
		List<Replacement> toReplace = new LinkedList<>();
		while (matcher.find()) {
			String partOfTagThatHasBackgroundAttribute = matcher.group();
			String backgroundColor = getBackgroundColor(partOfTagThatHasBackgroundAttribute);
			String contentMarker = RandomStringUtils.random(CONTENT_MARK_LENGTH, 0, 0, true, true, null, new SecureRandom());
			Replacement replacement = getStyleAttributesReplacement(partOfTagThatHasBackgroundAttribute, backgroundColor, contentMarker);
			toReplace.add(replacement);
			backgroundColorsMap.put(contentMarker, backgroundColor);
		}

		for (Replacement replacement : toReplace) {
			Util.replace(html, replacement.from, replacement.to);
		}
	}
	
	public Paragraph parse(Paragraph paragraph) {
		Paragraph parsedParagraph = new Paragraph();
		parsedParagraph.addAll(getChunksParsed(paragraph));
		
		return parsedParagraph;
	}

	public com.itextpdf.text.List parse(com.itextpdf.text.List list) {
		com.itextpdf.text.List parsedList = new com.itextpdf.text.List();
		for (Element listItem : list.getItems()) {
			List<Chunk> parsedElements = getChunksParsed(listItem);
			ListItem parsedListItem = new ListItem();
			parsedListItem.addAll(parsedElements);
			parsedList.add(parsedListItem);
		}
		
		return parsedList;
	}

	private String getBackgroundColor(String partOfTagThatHasBackgroundAttribute) {
		Pattern pattern = Pattern.compile(BG_COLOR_REGEX);
		Matcher matcher = pattern.matcher(partOfTagThatHasBackgroundAttribute);
		if (matcher.find()) {
			return matcher.group()
					.trim()
					.replace(";", "");
		}
		return "#FFF";
	}

	private Replacement getStyleAttributesReplacement(String partOfTagThatHasBackgroundAttribute,
			String backgroundColor, String contentMarker) {
		StringBuilder replaceFrom = new StringBuilder()
				.append(BG_COLOR_ATT_NAME)
				.append(": ")
				.append(backgroundColor)
				.append(";");
		String partOfTagWithoutBackground = partOfTagThatHasBackgroundAttribute.replace(replaceFrom , "");
		return new Replacement(partOfTagThatHasBackgroundAttribute, partOfTagWithoutBackground + contentMarker);
	}

	private List<Chunk> getChunksParsed(Element element) {
		List<Chunk> chunks = element.getChunks();
		List<Chunk> parsedChunks = new ArrayList<Chunk>(chunks.size());
		for (Chunk chunk : chunks) {
			if (chunk.getContent().length() < CONTENT_MARK_LENGTH) {
				parsedChunks.add(chunk);
				continue;
			}
			String contentMark = chunk.getContent().substring(0, CONTENT_MARK_LENGTH);
			String backgroundColor = backgroundColorsMap.get(contentMark);
			if (backgroundColor != null) {
				Chunk parsed = new Chunk(chunk.getContent().replace(contentMark, ""));
				parsed.setFont(chunk.getFont());
				parsed.setBackground(getColor(backgroundColor));
				parsedChunks.add(parsed);
			} else {
				parsedChunks.add(chunk);
			}
		}
		return parsedChunks;
	}

	private BaseColor getColor(String backgroundColor) {
		try {
			return WebColors.getRGBColor(backgroundColor);
		} catch (Exception e) {
			LOGGER.error("Unrecognized color: " + backgroundColor);
			return WebColors.getRGBColor("#FFF");
		}
	}
}
