package org.forpdi.system.reports.platfor;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import org.forpdi.core.company.Company;
import org.forpdi.core.location.CompanyIndicators;
import org.forpdi.core.location.Region;
import org.forpdi.dashboard.indicators.CompanyAccessHistory;
import org.forpdi.dashboard.indicators.IndicatorsDashboardHelper;
import org.forpdi.dashboard.indicators.RegionCounts;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndicatorsReportGeneratorHelperTest {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    @InjectMocks
    private IndicatorsReportGeneratorHelper helper;

    @Mock
    private IndicatorsDashboardHelper exportReportsHelper;

    private static Image dummyImage() throws Exception {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", baos);
        return Image.getInstance(baos.toByteArray());
    }

    @Test
    void getPercentage_companiesEmpty_returnsZero() {
        float pct = helper.getPercentage(1L, Collections.emptyList(), Collections.emptyList());
        assertEquals(0f, pct);
    }

    @Test
    void getPercentage_calculatesRoundedPercentage() {
        RegionCounts rc = mock(RegionCounts.class);
        when(rc.getRegionId()).thenReturn(7L);
        when(rc.getUniversityCount()).thenReturn((long) 1);
        when(rc.getInstituteCount()).thenReturn((long) 1);

        List<RegionCounts> counts = List.of(rc);
        List<Company> companies = Arrays.asList(mock(Company.class), mock(Company.class),
                mock(Company.class), mock(Company.class));

        float pct = helper.getPercentage(7L, companies, counts);
        assertEquals(50f, pct);
    }

    @Test
    void getRegionCounts_returnsMatch() {
        RegionCounts a = mock(RegionCounts.class);
        RegionCounts b = mock(RegionCounts.class);
        when(a.getRegionId()).thenReturn(1L);
        when(b.getRegionId()).thenReturn(2L);

        assertSame(b, helper.getRegionCounts(2L, List.of(a, b)));
        assertNull(helper.getRegionCounts(3L, List.of(a, b)));
    }

    @Test
    void getValueCell_formatsPercentageWithOneDecimal() {
        var cell = helper.getValueCell(12.345f, org.forpdi.system.reports.pdf.PDFSettings.TEXT_FONT);
        assertTrue(cell.getPhrase().getContent().contains("12.3%"));
    }

    @Test
    void getRegionMap_withRegionPositions_setsScaleAndAbsolutePosition() throws Exception {
        IndicatorsReportGeneratorHelper spy = Mockito.spy(new IndicatorsReportGeneratorHelper());
        doReturn(dummyImage()).when(spy).getImage(anyString());

        Region region = mock(Region.class);
        when(region.getId()).thenReturn(1L);
        when(region.getName()).thenReturn("NORTE");

        Image img = spy.getRegionMap(1L, List.of(region));
        assertNotNull(img);
        assertEquals(160f, img.getAbsoluteX(), 0.1f);
        assertEquals(520f, img.getAbsoluteY(), 0.1f);
        assertTrue(img.getScaledWidth() <= 300f);
        assertTrue(img.getScaledHeight() <= 170f);
    }

    @Test
    void getRegionMap_global_setsScaleAndAbsolutePosition() throws Exception {
        IndicatorsReportGeneratorHelper spy = Mockito.spy(new IndicatorsReportGeneratorHelper());
        doReturn(dummyImage()).when(spy).getImage(anyString());

        Image img = spy.getRegionMap();
        assertNotNull(img);
        assertEquals(175f, img.getAbsoluteX(), 0.1f);
        assertEquals(440f, img.getAbsoluteY(), 0.1f);
        assertTrue(img.getScaledWidth() <= 550f);
        assertTrue(img.getScaledHeight() <= 270f);
    }

    @Test
    void getRegionTable_singleRegion_buildsSingleColumnWithThreeRows() {
        Company c = mock(Company.class);
        List<Company> companies = Arrays.asList(c, c, c, c);

        RegionCounts rc = mock(RegionCounts.class);
        when(rc.getRegionId()).thenReturn(1L);
        when(rc.getUniversityCount()).thenReturn((long) 1);
        when(rc.getInstituteCount()).thenReturn((long) 1);

        Paragraph regionCount = new Paragraph("Região: 2 IF / 1 Univ.");
        PdfPTable table = helper.getRegionTable(regionCount, companies, List.of(rc), 1L);
        assertEquals(1, table.getNumberOfColumns());
        assertEquals(3, table.getRows().size());
    }

    @Test
    void getRegionTable_allRegions_buildsExpectedRows() {
        List<RegionCounts> counts = new ArrayList<>();
        for (long id = 1; id <= 5; id++) {
            RegionCounts rc = mock(RegionCounts.class);
            when(rc.getRegionId()).thenReturn(id);
            when(rc.getUniversityCount()).thenReturn((long) 1);
            when(rc.getInstituteCount()).thenReturn((long) 0);
            counts.add(rc);
        }
        List<Company> companies = Arrays.asList(mock(Company.class), mock(Company.class), mock(Company.class));

        Map<Long, Paragraph> paragraphs = new HashMap<>();
        for (long id = 1; id <= 5; id++) {
            paragraphs.put(id, new Paragraph("Região " + id));
        }

        PdfPTable table = helper.getRegionTable(paragraphs, companies, counts);
        assertEquals(1, table.getNumberOfColumns());
        assertEquals(11, table.getRows().size());
    }

    @Test
    void getPieChartTable_hasTwoColumnsAndOneRow() throws Exception {
        Image img = dummyImage();
        PdfPTable table = helper.getPieChartTable(img, Collections.emptyList());
        assertEquals(2, table.getNumberOfColumns());
        assertEquals(1, table.getRows().size());
    }

    @Test
    void getAccessHistoryLineChartImages_returnsPieAndLineImages() throws Exception {
        CompanyAccessHistory h1 = mock(CompanyAccessHistory.class);
        Map<String, Integer[]> m1 = new HashMap<>();
        m1.put(LocalDate.now().toString(), new Integer[]{1, 2});
        when(h1.getHistory()).thenReturn(m1);

        CompanyAccessHistory h2 = mock(CompanyAccessHistory.class);
        Map<String, Integer[]> m2 = new HashMap<>();
        m2.put(LocalDate.now().minusDays(1).toString(), new Integer[]{3, 0});
        when(h2.getHistory()).thenReturn(m2);

        Image[] images = helper.getAccessHistoryLineChartImages(List.of(h1, h2));
        assertNotNull(images);
        assertEquals(2, images.length);
        assertNotNull(images[0]);
        assertNotNull(images[1]);
    }

    @Test
    void getAccessHistoryPieChartTable_buildsTwoColumns() throws Exception {
        CompanyAccessHistory h = mock(CompanyAccessHistory.class);
        Map<String, Integer[]> m = new HashMap<>();
        m.put("2024-01-01", new Integer[]{5, 7});
        when(h.getHistory()).thenReturn(m);

        PdfPTable table = helper.getAccessHistoryPieChartTable(dummyImage(), List.of(h));
        assertEquals(2, table.getNumberOfColumns());
        assertEquals(1, table.getRows().size());
    }

    @Test
    void filterCompanyAccessHistories_keepsOnlySelectedCompanies() {
        Company c = mock(Company.class);
        when(c.getId()).thenReturn(10L);

        CompanyAccessHistory h10 = mock(CompanyAccessHistory.class);
        when(h10.getCompanyId()).thenReturn(10L);

        CompanyAccessHistory h20 = mock(CompanyAccessHistory.class);
        when(h20.getCompanyId()).thenReturn(20L);

        var accessResult = mock(org.forpdi.dashboard.indicators.AccessHistoryCalculator.AccessHistoryResult.class);
        when(accessResult.getCompaniesAccessHistory()).thenReturn(List.of(h10, h20));

        List<CompanyAccessHistory> filtered = helper.filterCompanyAccessHistories(accessResult, List.of(c));
        assertEquals(1, filtered.size());
        assertSame(h10, filtered.get(0));
    }

    @Test
    void getAccessHistoryList_flattensHistoryToListOfPairs() {
        CompanyAccessHistory h = mock(CompanyAccessHistory.class);
        Map<String, Integer[]> m = new LinkedHashMap<>();
        m.put("2024-01-01", new Integer[]{1, 2});
        m.put("2024-01-02", new Integer[]{0, 3});
        when(h.getHistory()).thenReturn(m);

        List<List<String>> out = helper.getAccessHistoryList(List.of(h));
        assertEquals(2, out.size());
        assertEquals(List.of("1","2"), out.get(0));
        assertEquals(List.of("0","3"), out.get(1));
    }

    @ParameterizedTest
    @CsvSource({
        "'2020-01 2020-12','2020-01 à 2020-12'",
        "'2020-01 *','A partir de 2020-01'",
        "'* 2020-12','Até 2020-12'",
        "'* *','Não determinado'"
    })
    void parsePeriodString_formatsPortugueseRanges(String in, String expected) {
        assertEquals(expected, helper.parsePeriodString(in));
    }

    @Test
    void getNumberOfFpdiAndFriskAcess_sumsAll() {
        CompanyAccessHistory h1 = mock(CompanyAccessHistory.class);
        Map<String, Integer[]> m1 = Map.of("2024-01-01", new Integer[]{1, 2});
        when(h1.getHistory()).thenReturn(m1);

        CompanyAccessHistory h2 = mock(CompanyAccessHistory.class);
        Map<String, Integer[]> m2 = Map.of("2024-01-01", new Integer[]{3, 4});
        when(h2.getHistory()).thenReturn(m2);

        List<Integer> totals = helper.getNumberOfFpdiAndFriskAcess(List.of(h1, h2));
        assertEquals(List.of(4, 6), totals);
    }
}
