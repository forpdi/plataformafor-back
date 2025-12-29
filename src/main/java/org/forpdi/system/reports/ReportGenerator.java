package org.forpdi.system.reports;

import java.io.InputStream;

public interface ReportGenerator {
	InputStream generateReport(ReportGeneratorParams params);
}
