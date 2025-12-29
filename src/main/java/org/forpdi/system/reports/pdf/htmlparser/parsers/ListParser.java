package org.forpdi.system.reports.pdf.htmlparser.parsers;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.forpdi.core.utils.RegexUtil;
import org.forpdi.core.utils.Util;

public class ListParser implements HtmlParser {
	
	public static final char BULLET_CHAR = '\u2022';

	@Override
	public void parseHtml(StringBuilder html) {
		formatULs(html);
		formatOLs(html);
	}

	private void formatULs(StringBuilder html) {
		addItemMarksToList(html, "ul", number -> String.valueOf(BULLET_CHAR));
	}
	
	private void formatOLs(StringBuilder html) {
		addItemMarksToList(html, "ol", number -> new StringBuilder().append(number).append(". ").toString());
	}
	
	private void addItemMarksToList(StringBuilder html, String listTagName, Function<Integer, String> getMark) {
		String itemsTag = "<li>";

		String regex = RegexUtil.getHTMLElementTagRegex(listTagName);
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(html);
		while (matcher.find()) {
			String listMatched = matcher.group();
			StringBuilder builder = new StringBuilder(listMatched);
			int index = builder.indexOf(itemsTag);
			int counter = 1;
			while (index != -1) {
				String replaceTo = new StringBuilder()
						.append(itemsTag)
						.append(getMark.apply(counter))
						.append(" ")
						.toString();
				
				builder.replace(index, itemsTag.length() + index, replaceTo);
		        index += replaceTo.length();
		        index = builder.indexOf(itemsTag, index);
		        counter += 1;
			}
			Util.replace(html, listMatched, builder.toString());
		}
	}
}
