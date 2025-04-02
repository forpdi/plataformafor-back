package org.forpdi.system.reports.pdf.htmlparser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class StyleSettings {
	protected static final Map<String, TagStyles> STYLE_MAP = new HashMap<String, TagStyles>();
	
	static {
		settings();
	}
	
	private static void settings() {
		addNewStyle("h1", "font-size", "20px");
		addNewStyle("h2", "font-size", "16px");
		addNewStyle("a", "color", ColorSettings.LINK_COLOR);
		addNewStyle("blockquote", "border", "0 0 0 4 #ccc");
	}

	public static Set<String> getStyledTags() {
		return StyleSettings.STYLE_MAP.keySet();
	}
	
	public static String getTagStylesAsHtml(String tag) {
		TagStyles tagStyles = StyleSettings.STYLE_MAP.get(tag);
		if (tagStyles == null) {
			return "";
		}
		Map<String, String> properties = tagStyles.getProperties();
		StringBuilder builder = new StringBuilder("style=\"");
		for (Entry<String, String> entry : properties.entrySet()) {
			builder
				.append(entry.getKey())
				.append(":")
				.append(entry.getValue())
				.append(";");
		}
		builder.append("\"");
		return builder.toString();
	}

	private static void addNewStyle(String tag, String propertyName, String propertyValue) {
		TagStyles tagStyles = STYLE_MAP.get(tag);
		if (tagStyles == null) {
			tagStyles = new TagStyles(tag);
			STYLE_MAP.put(tag, tagStyles);
		}
		tagStyles.addProperty(propertyName, propertyValue);
	}
}
