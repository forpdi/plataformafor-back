package org.forpdi.core.csvs;


import java.util.List;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.lang.Arrays;

@Component
public class CsvExportHelper {
	private final char csvDelimiter = '|';
	
	public char getCsvDelimiter() {
		return csvDelimiter;
	}
	
	public String fromStringToCsvFieldFormat(String string) {
		return "\"" + string.replace("\"", "\"\"") + "\"" + csvDelimiter;
	}

	public String fromDoubleToCsvPerformanceFormat(Double performance) {
		if (performance == null) {
			return "-";
		}

		return String.format("%.2f", performance) + "%";
	}
	
	public String createRow(String[] values) {
		return createRow(Arrays.asList(values), getCsvDelimiter());
	}

	public String createRow(String[] values, char delimiter) {
		return createRow(Arrays.asList(values), delimiter);
	}
	
	public String createRow(List<String> values, char delimiter) {
		StringBuilder builder = new StringBuilder();
		values.forEach(value -> builder.append(value).append(delimiter));
		builder.deleteCharAt(builder.length() - 1);
		builder.append("\n");
		return builder.toString();
	}
}
