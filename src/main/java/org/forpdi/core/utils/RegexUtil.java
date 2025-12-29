package org.forpdi.core.utils;

public class RegexUtil {

	public static final String HTML_ELEMENT_REGEX = "<([\\w]+)[^>]*>(.*?)<\\/\\1>";
	
	public static String getHTMLOpenTagRegex(String tag) {
		return "<" + tag + "[^>]*>";
	}
	
	public static String getHTMLElementTagRegex(String tag) {
		return 	"<([" + tag +  "]+)[^>]*>(.*?)<\\/\\1>";
	}
}
