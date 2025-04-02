package org.forpdi.system.reports.frisco;

import org.forpdi.system.reports.ReportGeneratorHelper;
import org.forrisco.core.item.PlanRiskItemBS;
import org.forrisco.core.plan.PlanRiskBS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlanRiskReportGeneratorTest {

	@Mock
	private PlanRiskBS planRiskBS;

	@Mock
	private PlanRiskItemBS planRiskItemBS;

	@Mock
	private ReportGeneratorHelper reportGeneratorHelper;

	@InjectMocks
	private PlanRiskReportGenerator reportGenerator;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testGenerateReport_invalidParameters() {
		PlanRiskReportGenerator.Params params = new PlanRiskReportGenerator.Params(
			null,
			null,
			null,
			null
		);

		try {
			reportGenerator.generateReport(params);
		} catch (RuntimeException e) {
			assertNotNull(e.getMessage(), "Exception should have a meaningful message");
		}
	}
}
