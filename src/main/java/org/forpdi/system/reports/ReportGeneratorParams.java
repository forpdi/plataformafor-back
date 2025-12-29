package org.forpdi.system.reports;

public interface ReportGeneratorParams {

	public static <T extends ReportGeneratorParams> T extractParams(ReportGeneratorParams params, Class<T> clazz) {
		if (clazz.isInstance(params)) {
			return clazz.cast(params);
		}
		throw new IllegalArgumentException("Invalid params passed in " + clazz.getName());
	}

}
