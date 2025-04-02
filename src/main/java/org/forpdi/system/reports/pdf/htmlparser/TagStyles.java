package org.forpdi.system.reports.pdf.htmlparser;

import java.util.HashMap;
import java.util.Map;

public class TagStyles {

	private String tag;
	private Map<String, String> properties = new HashMap<>();

	public TagStyles(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public Map<String, String> getProperties() {
		return new HashMap<>(properties);
	}
	
	public void addProperty(String name, String value) {
		properties.put(name, value);
	}
	
	public String getPropertyValue(String name) {
		return properties.get(name);
	}
}
