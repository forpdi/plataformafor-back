package org.forpdi.dashboard.indicators;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forpdi.system.reports.platfor.IndicatorsBoardReportGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndicatorsDashboardController extends AbstractController {

	@Autowired
	private IndicatorsDashboardBS bs;
	@Autowired
	private IndicatorsBoardReportGenerator indicatorsBoardReportGenerator;
	@Autowired
	private CsvIndicatorsDashboardExport csvExport;
	@Autowired
	private CompanyDomainContext domain;
	
	
	public static final String PATH = BASEPATH + "/indicators-dashboard";
	
	@GetMapping(PATH)
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> getIndicators() {
		try {
			return this.success(bs.getIndicatorsData());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	@GetMapping(PATH + "/exportBoardReport")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> exportBoardReport(
			@RequestParam String selecao,
			@RequestParam String selectedCompanies,
			@RequestParam String specific) {

		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
		
			IndicatorsBoardReportGenerator.Params params = new IndicatorsBoardReportGenerator.Params(selecao, selectedCompanies, specific);
			inputStream = this.indicatorsBoardReportGenerator.generateReport(params);
			
			this.response.reset();
			this.response.setHeader("Content-Type", "application/pdf");
			this.response.setHeader("Content-Disposition", "inline; filename=\"Relatório de indicadores.pdf\"");
			outputStream = this.response.getOutputStream();
			
			IOUtils.copy(inputStream, outputStream);
			return this.nothing();
		} catch (Throwable ex) {
			LOGGER.error("Error while proxying the file upload.", ex);
			return this.fail(ex.getMessage());
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				LOGGER.error("Error while exporting report.", e);
			}
		}
	}
	
	@GetMapping(PATH + "/exportCSV")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> exportAxesRiskCSV(ExportFiltersDto filters, @RequestParam int specific) {
		this.response.setHeader("Content-Disposition", String.format("attachment; filename=painel-de-indicadores-%d-%s.csv",
			domain.get().getCompany().getId(), LocalDateTime.now().toString()));
		this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		try (OutputStream os = this.response.getOutputStream();
				Writer writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
			if (IndicatorsDashboardHelper.ALL_CARDS_ID == specific) {
				csvExport.exportAllIndicatorsCsv(writer, filters);
			} else if (IndicatorsDashboardHelper.REGIONS_COUNT_CARD_ID == specific) {
				csvExport.exportRegionsCountsCsv(writer, filters);
			} else if (IndicatorsDashboardHelper.ACCESS_CARD_ID == specific) {
				csvExport.exportAccessHistoryCsv(writer, filters);
			} else if (IndicatorsDashboardHelper.INSTITUTIONS_CARD_ID == specific) {
				csvExport.exportCompaniesIndicatorsCsv(writer, filters);
			} else {
				return this.fail("Unrecognized export");
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}
}
