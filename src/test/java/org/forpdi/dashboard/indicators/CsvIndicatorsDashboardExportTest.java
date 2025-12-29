package org.forpdi.dashboard.indicators;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.csvs.CsvExportHelper;
import org.forpdi.core.location.CompanyIndicators;
import org.forpdi.dashboard.indicators.AccessHistoryCalculator.AccessHistoryResult;
import org.forpdi.dashboard.indicators.IndicatorsDashboardHelper.AdhesionByYearMonths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CsvIndicatorsDashboardExportTest {

    @InjectMocks private CsvIndicatorsDashboardExport exporter;

    @Mock private CsvExportHelper helper;
    @Mock private IndicatorsDashboardBS bs;
    @Mock private CompanyBS companyBS;
    @Mock private IndicatorsDashboardHelper indicatorsDashboardHelper;

    @BeforeEach
    void setup() {
        lenient().when(helper.createRow(any(String[].class))).thenAnswer(inv -> {
            String[] arr = inv.getArgument(0);
            return String.join(";", arr) + "\n";
        });
    }

    private static Company company(long id, String initials, String name) {
        Company c = new Company();
        c.setId(id);
        c.setInitials(initials);
        c.setName(name);
        return c;
    }

    @Test
    @DisplayName("exportAllIndicatorsCsv encadeia todas as seções com linhas em branco entre blocos")
    void exportAllIndicatorsCsv_full() throws Exception {
        List<Company> companies = List.of(company(1L,"A","A"), company(2L,"B","B"));
        when(companyBS.listAll()).thenReturn(companies);

        RegionCounts rc = mock(RegionCounts.class);
        when(rc.getRegionId()).thenReturn(1L);
        when(rc.getRegionName()).thenReturn("NORTE");
        when(rc.getUniversityCount()).thenReturn(1L); // int, não long
        when(rc.getInstituteCount()).thenReturn(1L);
        when(rc.getCompaniesCount()).thenReturn(2L);
        when(bs.getRegionsCounts(anyList())).thenReturn(List.of(rc));

        when(indicatorsDashboardHelper.getTotalUniversities(anyList())).thenReturn(1L);
        when(indicatorsDashboardHelper.getTotalInstitutes(anyList())).thenReturn(1L);
        when(indicatorsDashboardHelper.getTotalOthers(anyList())).thenReturn(0L);

        AdhesionByYearMonths adhesions = mock(AdhesionByYearMonths.class);
        when(adhesions.adhesionCountMap())
            .thenReturn(Map.of(YearMonth.of(2025,1), new org.forpdi.core.utils.Counter(2)));
        when(adhesions.sortedYearMonths())
            .thenReturn(List.of(YearMonth.of(2025,1)));
        when(indicatorsDashboardHelper.getAhesionsByYearMonths(anyList()))
            .thenReturn(adhesions);

        AccessHistoryResult ahr = mock(AccessHistoryResult.class);
        when(ahr.getCompaniesAccessHistory()).thenReturn(Collections.emptyList());
        when(bs.getCompaniesAccessHistoryByPeriod(any(), any())).thenReturn(ahr);

        when(bs.getCompaniesFpdiIndicators(anyList())).thenReturn(Collections.emptyList());
        when(bs.getCompaniesFriscoIndicators(anyList())).thenReturn(Collections.emptyList());
        when(indicatorsDashboardHelper.countFpdiIndicators(anyList())).thenReturn(List.of(1L,2L,3L,4L,5L,6L));
        when(indicatorsDashboardHelper.countFriscoIndicators(anyList())).thenReturn(List.of(7L,8L,9L,10L));

        AccessHistoryResult ahrAll = mock(AccessHistoryResult.class);
        when(bs.getCompaniesAccessHistory()).thenReturn(ahrAll);
        when(bs.getCompaniesIndicators(anyList(), anyList(), anyList())).thenReturn(Collections.emptyList());
        when(ahrAll.getCompaniesAccessHistoryFilteredByPeriod(anyList(), any(), any()))
            .thenReturn(Collections.emptyMap());

        ExportFiltersDto filters = mock(ExportFiltersDto.class);
        when(filters.regionId()).thenReturn(-1L);
        when(filters.companyIds()).thenReturn(List.of(1L, 2L));
        when(filters.companyCreationBegin()).thenReturn(null);
        when(filters.companyCreationEnd()).thenReturn(null);
        when(filters.companyCreationBeginAsDate()).thenReturn(null);
        when(filters.companyCreationEndAsDate()).thenReturn(null);

        StringWriter w = new StringWriter();
        exporter.exportAllIndicatorsCsv(w, filters);
        String csv = w.toString();

        assertTrue(csv.contains("REGIÕES"));
        assertTrue(csv.contains("UNIVERSIDADES/INSTITUTOS"));
        assertTrue(csv.contains("ADESÕES"));
        assertTrue(csv.contains("ACESSOS"));
        assertTrue(csv.contains("PDI EM NÚMEROS"));
        assertTrue(csv.contains("RISCOS EM NÚMEROS"));
        assertTrue(csv.contains("INSTITUIÇÕES"));

        assertTrue(csv.contains("REGIÕES\n"));
        assertTrue(csv.contains("\nACESSOS\n"));
    }

    @Test
    @DisplayName("exportRegionsCountsCsv: escreve regiões, tipos e adesões (com filtro de região -1)")
    void exportRegionsCountsCsv_success() throws Exception {
        List<Company> companies = List.of(company(1L,"A","A"), company(2L,"B","B"));
        when(companyBS.listAll()).thenReturn(companies);

        RegionCounts rc1 = mock(RegionCounts.class);
        when(rc1.getRegionId()).thenReturn(1L);
        when(rc1.getRegionName()).thenReturn("NORTE");
        when(rc1.getUniversityCount()).thenReturn(1L);
        when(rc1.getInstituteCount()).thenReturn(0L);
        when(rc1.getCompaniesCount()).thenReturn(1L);

        RegionCounts rc2 = mock(RegionCounts.class);
        when(rc2.getRegionId()).thenReturn(2L);
        when(rc2.getRegionName()).thenReturn("SUL");
        when(rc2.getUniversityCount()).thenReturn(0L);
        when(rc2.getInstituteCount()).thenReturn(1L);
        when(rc2.getCompaniesCount()).thenReturn(1L);

        when(bs.getRegionsCounts(anyList())).thenReturn(List.of(rc1, rc2));

        when(indicatorsDashboardHelper.getTotalUniversities(anyList())).thenReturn(1L);
        when(indicatorsDashboardHelper.getTotalInstitutes(anyList())).thenReturn(1L);
        when(indicatorsDashboardHelper.getTotalOthers(anyList())).thenReturn(0L);

        AdhesionByYearMonths adhesions = mock(AdhesionByYearMonths.class);
        when(adhesions.adhesionCountMap())
            .thenReturn(Map.of(YearMonth.of(2025,1), new org.forpdi.core.utils.Counter(2)));
        when(adhesions.sortedYearMonths()).thenReturn(List.of(YearMonth.of(2025,1)));
        when(indicatorsDashboardHelper.getAhesionsByYearMonths(anyList())).thenReturn(adhesions);

        ExportFiltersDto filters = mock(ExportFiltersDto.class);
        when(filters.regionId()).thenReturn(-1L);

        StringWriter w = new StringWriter();
        exporter.exportRegionsCountsCsv(w, filters);

        String out = w.toString();
        assertTrue(out.contains("REGIÕES"));
        assertTrue(out.contains("NORTE"));
        assertTrue(out.contains("SUL"));
        assertTrue(out.contains("UNIVERSIDADES/INSTITUTOS"));
        assertTrue(out.contains("ADESÕES"));
    }

    @Test
    @DisplayName("getTotalOthers: soma otherCount de RegionCounts")
    void getTotalOthers_sums() {
        RegionCounts r1 = mock(RegionCounts.class); when(r1.getOtherCount()).thenReturn(2L);
        RegionCounts r2 = mock(RegionCounts.class); when(r2.getOtherCount()).thenReturn(3L);
        long total = exporter.getTotalOthers(List.of(r1, r2));
        assertEquals(5, total);
    }

    @Test
    @DisplayName("exportAccessHistoryCsv: agrega por mês, escreve linhas e totaliza FPDI/FRISCO")
    void exportAccessHistoryCsv_success() throws Exception {
        CompanyAccessHistory h1 = mock(CompanyAccessHistory.class);
        when(h1.getCompanyId()).thenReturn(10L);
        Map<String, Integer[]> hist1 = new LinkedHashMap<>();
        hist1.put(LocalDate.of(2025,1,10).toString(), new Integer[]{ 3, 1 });
        when(h1.getHistory()).thenReturn(hist1);

        CompanyAccessHistory h2 = mock(CompanyAccessHistory.class);
        when(h2.getCompanyId()).thenReturn(20L);
        Map<String, Integer[]> hist2 = new LinkedHashMap<>();
        hist2.put(LocalDate.of(2025,1,15).toString(), new Integer[]{ 2, 4 });
        when(h2.getHistory()).thenReturn(hist2);

        AccessHistoryResult ahr = mock(AccessHistoryResult.class);
        when(ahr.getCompaniesAccessHistory()).thenReturn(List.of(h1, h2));
        when(bs.getCompaniesAccessHistoryByPeriod(any(), any())).thenReturn(ahr);

        ExportFiltersDto filters = mock(ExportFiltersDto.class);
        when(filters.companyIds()).thenReturn(List.of(10L));
        when(filters.companyCreationBegin()).thenReturn(null);
        when(filters.companyCreationEnd()).thenReturn(null);

        StringWriter w = new StringWriter();
        exporter.exportAccessHistoryCsv(w, filters);
        String out = w.toString();

        assertTrue(out.contains("HISTÓRICO DE ACESSOS"));
        assertTrue(out.contains("01/2025"));
        assertTrue(out.contains("3"));
        assertTrue(out.contains("1"));
        assertTrue(out.contains("TOTAL DE ACESSOS"));
        assertTrue(out.contains("FORPDI"));
        assertTrue(out.contains("FORRISCO"));
    }

    @Test
    @DisplayName("exportFpdiIndicatorsCsv: escreve 6 indicadores FPDI dos filtrados")
    void exportFpdiIndicatorsCsv_success() throws Exception {
        Company c1 = company(1L, "A", "A");
        Company c2 = company(2L, "B", "B");
        when(companyBS.listAll()).thenReturn(List.of(c1, c2));

        CompanyFpdiIndicators fi1 = mock(CompanyFpdiIndicators.class);
        CompanyFpdiIndicators fi2 = mock(CompanyFpdiIndicators.class);
        when(fi1.getCompany()).thenReturn(c1);
        when(fi2.getCompany()).thenReturn(c2);
        when(bs.getCompaniesFpdiIndicators(anyList())).thenReturn(List.of(fi1, fi2));

        when(indicatorsDashboardHelper.countFpdiIndicators(anyList()))
                .thenReturn(List.of(1L, 2L, 3L, 4L, 5L, 6L));

        ExportFiltersDto filters = mock(ExportFiltersDto.class);
        when(filters.companyIds()).thenReturn(List.of(1L, 2L)); // Set, não List

        StringWriter w = new StringWriter();
        exporter.exportFpdiIndicatorsCsv(w, filters);
        String out = w.toString();

        assertTrue(out.contains("PDI EM NÚMEROS"));
        assertTrue(out.contains("Plano de metas"));
        assertTrue(out.contains("Indicadores"));
        assertTrue(out.contains("Metas"));
    }

    @Test
    @DisplayName("exportFriscoIndicatorsCsv: escreve 4 indicadores FRISCO dos filtrados")
    void exportFriscoIndicatorsCsv_success() throws Exception {
        Company c1 = company(1L, "A", "A");
        Company c2 = company(2L, "B", "B");
        when(companyBS.listAll()).thenReturn(List.of(c1, c2));

        CompanyForriscoIndicators ri1 = mock(CompanyForriscoIndicators.class);
        CompanyForriscoIndicators ri2 = mock(CompanyForriscoIndicators.class);
        when(ri1.getCompany()).thenReturn(c1);
        when(ri2.getCompany()).thenReturn(c2);
        when(bs.getCompaniesFriscoIndicators(anyList())).thenReturn(List.of(ri1, ri2));

        when(indicatorsDashboardHelper.countFriscoIndicators(anyList()))
                .thenReturn(List.of(7L, 8L, 9L, 10L));

        ExportFiltersDto filters = mock(ExportFiltersDto.class);
        when(filters.companyIds()).thenReturn(List.of(1L, 2L)); // Set, não List

        StringWriter w = new StringWriter();
        exporter.exportFriscoIndicatorsCsv(w, filters);
        String out = w.toString();

        assertTrue(out.contains("RISCOS EM NÚMEROS"));
        assertTrue(out.contains("Riscos"));
        assertTrue(out.contains("Realizando monitoramento"));
    }

    @Test
    @DisplayName("exportCompaniesIndicatorsCsv: escreve linha por instituição e período formatado")
    void exportCompaniesIndicatorsCsv_success() throws Exception {
        Company c1 = company(1L, "ABC", "Comp A");
        when(companyBS.listAll()).thenReturn(List.of(c1));

        AccessHistoryResult ahrAll = mock(AccessHistoryResult.class);
        when(bs.getCompaniesAccessHistory()).thenReturn(ahrAll);
        Map<Long, Integer[]> map = new HashMap<>();
        map.put(1L, new Integer[]{5, 7});
        when(ahrAll.getCompaniesAccessHistoryFilteredByPeriod(anyList(), any(), any())).thenReturn(map);

        CompanyIndicators ci = mock(CompanyIndicators.class);
        when(ci.getId()).thenReturn(1L);
        when(ci.getInitials()).thenReturn("ABC");
        when(ci.getName()).thenReturn("Comp A");
        when(ci.getCreation()).thenReturn(new Date());
        when(ci.getPlanMacrosCount()).thenReturn(3L);
        when(ci.getPlanRisksCount()).thenReturn(2L);
        when(bs.getCompaniesFpdiIndicators(anyList())).thenReturn(Collections.emptyList());
        when(bs.getCompaniesFriscoIndicators(anyList())).thenReturn(Collections.emptyList());
        when(bs.getCompaniesIndicators(anyList(), anyList(), anyList())).thenReturn(List.of(ci));

        ExportFiltersDto filters = mock(ExportFiltersDto.class);
        when(filters.companyIds()).thenReturn(List.of(1L));
        when(filters.companyCreationBegin()).thenReturn("2025-01");
        when(filters.companyCreationEnd()).thenReturn(null);
        when(filters.companyCreationBeginAsDate()).thenReturn(null);
        when(filters.companyCreationEndAsDate()).thenReturn(null);

        StringWriter w = new StringWriter();
        exporter.exportCompaniesIndicatorsCsv(w, filters);

        String out = w.toString();
        assertTrue(out.contains("INSTITUIÇÕES"));
        assertTrue(out.contains("ABC"));
        assertTrue(out.contains("Comp A"));
        assertTrue(out.contains("A partir de 2025-01"));
        assertTrue(out.contains("5"));
        assertTrue(out.contains("7"));
        assertTrue(out.contains("3"));
        assertTrue(out.contains("2"));
    }
}
