package org.forpdi.core;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.forpdi.core.common.GeneralUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

@Configuration
public class JsonConfig {

	@Bean
	public Gson objectMapper() {
		GsonBuilder gsonBuilder = getBuilder();

		return gsonBuilder.create();
	}
	
	public static GsonBuilder getBuilder() {
		return new GsonBuilder()
				.setDateFormat("dd/MM/yyyy HH:mm:ss")
				.registerTypeAdapter(Date.class, new CustomDateDeserializer())
				.addSerializationExclusionStrategy(new CustomExclusionStrategy());
	}
	
	private static class CustomExclusionStrategy implements ExclusionStrategy {
		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}

		@Override
		public boolean shouldSkipField(FieldAttributes field) {
			return field.getAnnotation(SkipSerialization.class) != null;
		}
	}

	private static class CustomDateDeserializer implements JsonDeserializer<Date> {
		private static final Logger LOG = LoggerFactory.getLogger(CustomDateDeserializer.class);
		
		private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			try {
				String dateString = json.getAsString();
				if (GeneralUtils.isEmpty(dateString)) {
					return null;
				}

				if (dateString.length() < 10) {
					return dateFormat.parse(transformReducedDate(dateString));
				}
				
				if (dateString.length() == 10) {
					return dateFormat.parse(dateString);
				}
				return dateTimeFormat.parse(dateString);
			} catch (Exception e) {
				LOG.error("Fail to deserialize json", e);
				throw new JsonParseException("Failed to parse date: " + e.getMessage());
			}
		}
		
		private String transformReducedDate(String dateString) {
			String[] split = dateString.split("/");
			if (split.length != 3) {
				throw new JsonParseException("Failed to parse date with invalid format: " + dateString);
			}
			String day = String.format("%02d", Integer.parseInt(split[0]));
			String month = String.format("%02d", Integer.parseInt(split[1]));
			String year = split[2];
			return day + "/" + month + "/" + year;
		}
	}
}
