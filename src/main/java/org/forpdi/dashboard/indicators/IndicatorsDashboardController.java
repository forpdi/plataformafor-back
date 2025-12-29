package org.forpdi.dashboard.indicators;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.forpdi.core.common.AbstractController;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forpdi.system.reports.platfor.IndicatorsBoardReportGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;       // ← novo
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

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

    /*───────────────────────────────────────────────*
     * Painel resumido                               *
     *───────────────────────────────────────────────*/
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
        try {
            var in = indicatorsBoardReportGenerator.generateReport(
                    new IndicatorsBoardReportGenerator.Params(selecao, selectedCompanies, specific));

            Set<Long> requestedIds = Arrays.stream(selectedCompanies.split(","))
                                            .map(Long::parseLong)
                                            .collect(Collectors.toSet());
            if (!requestedIds.contains(domain.get().getCompany().getId()) && !userHasRole("SYSTEM_ADMIN")) {
                LOGGER.warn("Tentativa de acesso não autorizado ao relatório");
                return this.forbidden();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename("relatorio-de-indicadores.pdf")
                    .build());
            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));

        } catch (Throwable ex) {
            LOGGER.error("Error while exporting report.", ex);
            return this.fail(ex.getMessage());
        }
    }

    @GetMapping(PATH + "/exportCSV")
    @PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
    public ResponseEntity<?> exportAxesRiskCSV(ExportFiltersDto filters,
            @RequestParam int specific) {

        this.response.setHeader(
                "Content-Disposition",
                String.format("attachment; filename=painel-de-indicadores-%d-%s.csv",
                        domain.get().getCompany().getId(), LocalDateTime.now()));
        this.response.setContentType("text/csv; charset=UTF-8");

        try (OutputStream os = this.response.getOutputStream(); Writer writer = new BufferedWriter(
                new OutputStreamWriter(os, StandardCharsets.UTF_8))) {

            switch (specific) {
                case IndicatorsDashboardHelper.ALL_CARDS_ID ->
                    csvExport.exportAllIndicatorsCsv(writer, filters);
                case IndicatorsDashboardHelper.REGIONS_COUNT_CARD_ID ->
                    csvExport.exportRegionsCountsCsv(writer, filters);
                case IndicatorsDashboardHelper.ACCESS_CARD_ID ->
                    csvExport.exportAccessHistoryCsv(writer, filters);
                case IndicatorsDashboardHelper.INSTITUTIONS_CARD_ID ->
                    csvExport.exportCompaniesIndicatorsCsv(writer, filters);
                default -> {
                    return this.fail("Unrecognized export");
                }
            }
        } catch (Throwable e) {
            LOGGER.error("Unexpected runtime error", e);
            this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return this.nothing();
    }

    /*───────────────────────────────────────────────*
     * Checa papel do usuário pelo SecurityContext   *
     *───────────────────────────────────────────────*/
    private boolean userHasRole(String roleName) {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleName));
    }
}
