package org.forpdi.planning.structure.xml;

import java.util.List;

import org.dom4j.Element;

import com.google.gson.Gson;


public final class StructureBeans {
	
	public static class Structure {
		public String name;
		public String description;
		public List<StructureLevel> levels;
		
		public Structure deepClone() {
			Gson gson = new Gson();
			String json = gson.toJson(this);
			return gson.fromJson(json, Structure.class);
		}
	}
	
	public static class StructureLevel {
		public String name;
		public String description;
		public int sequence;
		public List<Attribute> attributes;
		public boolean leaf;
		public boolean goal;
		public boolean indicator;
		public boolean objective;
		public transient Element child;
	}
	
	public static class Attribute {
		public String label;
		public String description;
		public String type;
		public boolean periodicity;
		public List<OptionsField> optionsField;
		public List<ScheduleValue> scheduleValues;
		public boolean required;
		public boolean visibleInTables;
		public boolean finishDate;
		public boolean expectedField;
		public boolean minimumField;
		public boolean maximumField;
		public boolean reachedField;
		public boolean referenceField;
		public boolean justificationField;
		public boolean polarityField;
		public boolean formatField;
		public boolean periodicityField;
		public boolean beginField;
		public boolean endField;
		public boolean bscField;
	}
	
	public static class ScheduleValue {
		public String label;
		public String type;
	}
	
	public static class OptionsField {
		public String label;
		public Attribute attribute;
	}
}
