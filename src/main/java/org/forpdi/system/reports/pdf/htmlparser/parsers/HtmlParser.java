package org.forpdi.system.reports.pdf.htmlparser.parsers;

public interface HtmlParser {
	static final int CONTENT_MARK_LENGTH = 10;
	
	void parseHtml(StringBuilder html);
	
	class Replacement {
		public String from;
		public String to;
		public Replacement(String from, String to) {
			this.from = from;
			this.to = to;
		}
	}
}
