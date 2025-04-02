package org.forpdi.system.reports.pdf.htmlparser.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.forpdi.core.utils.RegexUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.system.reports.pdf.htmlparser.StyleSettings;

public class StylesParser implements HtmlParser {

	@Override
	public void parseHtml(StringBuilder html) {
		Set<String> tags = StyleSettings.getStyledTags();
		for (String tag : tags) {
			injectStyles(html, tag);
		}
	}

	private void injectStyles(StringBuilder html, String targetTag) {
		String regex = RegexUtil.getHTMLOpenTagRegex(targetTag);
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(html);
		List<String[]> tagsToReplace = new ArrayList<>();
		while (matcher.find()) {
			String tagMatched = matcher.group();
			String tagMatchedWithStyles = getTagWithStyles(targetTag, tagMatched);
			tagsToReplace.add(new String[] { tagMatched, tagMatchedWithStyles });
		}
		
		for (String[] tagReplacement : tagsToReplace) {
			String tag = tagReplacement[0];
			String tagWithStyles = tagReplacement[1];
			Util.replace(html, tag, tagWithStyles);
		}
	}

	private String getTagWithStyles(String targetTag, String tagMatched) {
		String replaceFrom = new StringBuilder()
				.append("<")
				.append(targetTag)
				.toString();
		String replaceTo = new StringBuilder()
				.append("<")
				.append(targetTag)
				.append(" ")
				.append(StyleSettings.getTagStylesAsHtml(targetTag))
				.toString();
		return tagMatched.replace(replaceFrom, replaceTo);
	}
}
