package org.forpdi.system.reports.platfor;

import static org.junit.jupiter.api.Assertions.*;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.location.RegionRepository;
import org.forpdi.dashboard.indicators.IndicatorsDashboardBS;
import org.forpdi.system.reports.ReportGeneratorHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class IndicatorsBoardReportGeneratorTest {

	@InjectMocks
	private IndicatorsBoardReportGenerator reportGenerator;

	@Mock
	private IndicatorsDashboardBS indicatorsDashboardBS;

	@Mock
	private ReportGeneratorHelper reportGeneratorHelper;

	@Mock
	private RegionRepository regionRepository;

	@Mock
	private CompanyBS companyBS;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Deve lançar uma exceção para parâmetros inválidos")
	void testGenerateReportWithInvalidParams() {
		IndicatorsBoardReportGenerator.Params params = new IndicatorsBoardReportGenerator.Params("invalid", "1", "2");

		assertThrows(RuntimeException.class, () -> reportGenerator.generateReport(params),
			"Deve lançar exceção ao passar parâmetros inválidos.");
	}

}
