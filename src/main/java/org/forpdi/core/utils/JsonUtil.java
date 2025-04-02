package org.forpdi.core.utils;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import org.forpdi.core.JsonConfig;

public class JsonUtil {

	private static final Gson GSON = JsonConfig.getBuilder().create();

	public static <T> T fromJson(String json, Class<T> clazz) {
		return GSON.fromJson(json, clazz);
	}

	public static <T> T fromJson(String json, Type type) {
		return GSON.fromJson(json, type);
	}

	public static String toJson(Object obj) {
		return GSON.toJson(obj);
	}
	
	public static <T> T jsonClone(T t, Class<T> clazz) {
		String json = toJson(t);
		return JsonUtil.fromJson(json, clazz);
	}
}
