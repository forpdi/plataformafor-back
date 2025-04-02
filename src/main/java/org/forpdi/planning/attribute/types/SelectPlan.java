package org.forpdi.planning.attribute.types;

import java.text.NumberFormat;
import java.util.Date;

import javax.validation.UnexpectedTypeException;

import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.properties.CoreMessages;
import org.forpdi.planning.attribute.AttributeType;
import org.forpdi.planning.attribute.AttributeTypeWrapper;

public class SelectPlan extends AttributeType {
	
	private AttributeTypeWrapper wrapper = new Wrapper();
	
	public final String WIDGET_NAME = "SelectPlan";
	@Override
	public String getWidget() {
		return WIDGET_NAME;
	}
	
	@Override
	public AttributeTypeWrapper getWrapper() {
		return wrapper;
	}

	@Override
	public String getDisplayName() {
		return "Campo de seleção de planos de metas";
	}
	
	public static class Wrapper implements AttributeTypeWrapper {
		
		NumberFormat formatter = NumberFormat.getNumberInstance(CoreMessages.DEFAULT_LOCALE);

		@Override
		public String fromDatabase(String databaseValue) {
			return databaseValue;
		}

		@Override
		public String fromDatabaseNumerical(Double databaseValue) {
			return formatter.format(databaseValue);
		}

		@Override
		public String toDatabase(String viewValue) {
			return GeneralUtils.isEmpty(viewValue) ? "":viewValue.trim();
		}

		@Override
		public Double toDatabaseNumerical(String viewValue) {
			try {
				return formatter.parse(viewValue).doubleValue();
			} catch(Exception ex) {
				throw new UnexpectedTypeException(ex);
			}
		}

		@Override
		public String fromDatabaseDate(Date databaseValue) {
			return null;
		}

		@Override
		public Date toDatabaseDate(String viewValue) {
			return null;
		}
		
		@Override
		public boolean isNumerical() {
			return true;
		}
		@Override
		public boolean isDate() {
			return false;
		}
		
		@Override
		public String prefix() {
			return "";
		}
		@Override
		public String suffix() {
			return "";
		}
	}
}
