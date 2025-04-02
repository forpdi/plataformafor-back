package org.forpdi.system.reports.pdf.htmlparser.parsers;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.RandomStringUtils;
import org.forpdi.core.utils.Util;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;

public class TextParser implements HtmlParser {
	private static final String JUSTIFY_TEXT_CLASS = "ql-align-justify";
	private static final String RIGHT_TEXT_CLASS = "ql-align-right";
	private static final String CENTER_TEXT_CLASS = "ql-align-center";
	private static final Map<String, Integer> TEXT_CLASS_MAP;
	private static final String ELEMENT_WITH_ALIGN_CLASS_REGEX;
	private static final int PARAGRAPH_DEFAULT_ALIGN = Element.ALIGN_JUSTIFIED;

	private Map<String, String> textModifiersMap = new HashMap<>();

	static {
		TEXT_CLASS_MAP = createTextClassMap();
		ELEMENT_WITH_ALIGN_CLASS_REGEX = createAlightClassRegex(TEXT_CLASS_MAP);
	}

	private static Map<String, Integer> createTextClassMap() {
		Map<String, Integer> map = new HashMap<>();
		map.put(JUSTIFY_TEXT_CLASS, Element.ALIGN_JUSTIFIED);
		map.put(RIGHT_TEXT_CLASS, Element.ALIGN_RIGHT);
		map.put(CENTER_TEXT_CLASS, Element.ALIGN_CENTER);
		return map;
	}
	
	private static String createAlightClassRegex(Map<String, Integer> textClassMap) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> iterator = textClassMap.keySet().iterator();
		while(iterator.hasNext()) {
			sb.append(iterator.next()).append("[^>]+>");
			if (iterator.hasNext()) {
				sb.append("|");
			}
		}
		return sb.toString();
	}

	@Override
	public void parseHtml(StringBuilder html) {
		Pattern pattern = Pattern.compile(ELEMENT_WITH_ALIGN_CLASS_REGEX);
		Matcher matcher = pattern.matcher(html);
		List<Replacement> toReplace = new LinkedList<>();
		while (matcher.find()) {
			String partOfTagThatHasTextModifierClass = matcher.group();
			String className = extractClassName(partOfTagThatHasTextModifierClass);
			String partOfTagWithoutModifierClass = partOfTagThatHasTextModifierClass.replace(className, "");
			String contentMarker = RandomStringUtils.random(CONTENT_MARK_LENGTH, 0, 0, true, true, null, new SecureRandom());
			Replacement replacement = new Replacement(partOfTagThatHasTextModifierClass, partOfTagWithoutModifierClass + contentMarker);
			textModifiersMap.put(contentMarker, className);
			toReplace.add(replacement);
		}

		for (Replacement replacement : toReplace) {
			Util.replace(html, replacement.from, replacement.to);
		}
	}

	private String extractClassName(String partOfTagThatHasTextModifierClass) {
		for (String className : TEXT_CLASS_MAP.keySet()) {
			if (partOfTagThatHasTextModifierClass.contains(className)) {
				return className;
			}
		}

		throw getAssertException();
	}

	
	public Paragraph parse(Paragraph paragraph) {
		setDefaultAlignment(paragraph);
		if (paragraph.getContent().length() < CONTENT_MARK_LENGTH) {
			return paragraph;
		}
		String contentMark = paragraph.getContent().substring(0, CONTENT_MARK_LENGTH);
		String className = textModifiersMap.get(contentMark);
		if (className != null) {
			Paragraph parsedParagraph = new Paragraph(paragraph.getContent().replace(contentMark, ""));
			parsedParagraph.setAlignment(getAlignmentByClass(className));
			return parsedParagraph;
		}
		
		return paragraph;
	}

	private void setDefaultAlignment(Paragraph paragraph) {
		paragraph.setAlignment(PARAGRAPH_DEFAULT_ALIGN);
	}

	public com.itextpdf.text.List parse(com.itextpdf.text.List list) {
		com.itextpdf.text.List parsedList = new com.itextpdf.text.List();
		for (Element listItem : list.getItems()) {
			ListItem parsedListItem = new ListItem();
			for (Chunk chunk : listItem.getChunks()) {
				Chunk parsedChunk;
				if (chunk.getContent().length() < CONTENT_MARK_LENGTH) {
					parsedChunk = chunk;
				} else {
					String contentMark = chunk.getContent().substring(0, CONTENT_MARK_LENGTH);
					String className = textModifiersMap.get(contentMark);
					if (className != null) {
						parsedChunk = new Chunk(chunk.getContent().replace(contentMark, ""));
						parsedListItem.setAlignment(getAlignmentByClass(className));
					} else {
						parsedChunk = chunk;
					}
				}
				parsedListItem.add(parsedChunk);
			}
			parsedList.add(parsedListItem);
		}
		
		return parsedList;
	}

	private int getAlignmentByClass(String className) {
		if (TEXT_CLASS_MAP.containsKey(className)) {
			return TEXT_CLASS_MAP.get(className);
		}

		throw getAssertException();
	}

	private AssertionError getAssertException() {
		return new AssertionError("Unexpected exeption");
	}
}
