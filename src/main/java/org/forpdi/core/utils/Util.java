package org.forpdi.core.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.apache.commons.lang.StringUtils;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.SimpleIdentifiable;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Util {
	public static final String PERCENTAGE_PATTERN = "0,00%";
	public static final String DECIMAL_FORMAT_PATTERN = "#,##0.00";
	private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

	public static String getDownloadFilesURL(String fileLink, String baseUrl) {
		String formattedBaseUrl = baseUrl.endsWith("/")
				? baseUrl.substring(0, baseUrl.length() - 1)
				: baseUrl;

		return String.format("%s/forpdi/%s/%s",
	    		formattedBaseUrl, Consts.FILES_ENDPOINT_BASE_URL, fileLink);
	}
	
	public static void closeFile(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException e) {
			LOGGER.error("Error on closing file.", e);
		}
	}
	
	public static void replace(StringBuilder builder, String from, String to) {
	    int index = builder.indexOf(from);
        builder.replace(index, index + from.length(), to);
	}

	public static void replaceAll(StringBuilder builder, String from, String to) {
	    int index = builder.indexOf(from);
	    while (index != -1) {
	        builder.replace(index, index + from.length(), to);
	        index += to.length();
	        index = builder.indexOf(from, index);
	    }
	}
	
	public static List<Long> stringListToLongList(String strList) {
		if (strList == null) {
			return Collections.emptyList();
		}
		String[] split = strList.split(",");
		List<Long> longList = new ArrayList<>(split.length);
		for (String str : split) {
			longList.add(Long.parseLong(str));
		}
		return longList;
	}
	
	public static <K, V> Map<K, V> generateMap(List<V> list, Function<V, K> keyExtractor) {
		Map<K, V> map = new HashMap<K, V>();
		list.forEach((V value) -> {
			K key = keyExtractor.apply(value);
			map.put(key, value);
		});
		return map;
	}
	
	public static <K, V> Map<K, List<V>> generateGroupedMap(List<V> list, Function<V, K> keyExtractor) {
		Map<K, List<V>> map = new LinkedHashMap<>();
		list.forEach((V value) -> {
			K key = keyExtractor.apply(value);
			List<V> values = map.get(key);
			if (values == null) {
				values = new LinkedList<>();
				map.put(key, values);
			}
			values.add(value);
		});
		return map;
	}

	public static String censorCPF(String plainCPF) {
		if(!GeneralUtils.isEmpty(plainCPF)) {	
			
			return "*********" + plainCPF.substring(9);
		} 
		return "";
	}
	
	public static boolean hasOnlySpecialCharactersString(String text) {
		String splChrs = "-/@#$%^&_+=()'´`{[~}];:\\!¨*|?><.,";
		return StringUtils.containsOnly(text, splChrs);
	}

	public static boolean hasOnlyNumbers(String str) {
		try {
			Long.parseLong(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static List<Long> bigIntegerListToLongList(List<BigInteger> list) {
		if (list == null) {
			return null;
		}
		
		return list.stream()
			.map(bi -> bi.longValue())
			.collect(Collectors.toList());
	}
	
	public static int getPaginationOffset(int page, int pageSize) {
		return (page - 1) * pageSize;
	}
	
	public static String htmlToString(String str) {
		return str == null ? str : str.replaceAll("\\<[^>]*>","");
	}

	public static String htmlToText(String string) {
    if (string != null) {
        return Jsoup.parse(string).text();
    }
    return string;
}
	
	public static String truncateString(String text, Integer maxLength) {
		if (text == null || text.length() <= maxLength) {
			return text;
		}
		
		return text.substring(0, maxLength) + "...";
	}
	
	public static List<Long> mapEntityIds(List<? extends SimpleIdentifiable> entities) {
		return entities.stream().map(e -> e.getId()).collect(Collectors.toList());
	}
	
	public static <T extends Serializable> PaginatedList<T> clonePaginatedList(PaginatedList<T> paginatedList, Class<T> clazz) {
		List<T> list = new ArrayList<T>();
		if (paginatedList.getList() != null) {
			for (T t : paginatedList.getList()) {
				String json = JsonUtil.toJson(t);
				list.add(JsonUtil.fromJson(json, clazz));
			}
		}
		return new PaginatedList<T>(list, paginatedList.getTotal());
	}
	
	public static Predicate getLikePredicate(CriteriaBuilder builder, Expression<String> path, String term) {
		return builder.like(
				builder.upper(path), "%" + term.toUpperCase() + "%");

	}
	
	public static Double parseDoubleWithComma(String str) {
		String valueNoSeparator = str.replace(".", "");
		String valueFormated = valueNoSeparator.replace(",", ".");
		Double value = Double.parseDouble(valueFormated);
		return value;
	}

}
