package org.forpdi.system.reports.platfor;

import static org.forpdi.dashboard.indicators.CompanyAccessHistory.FPDI_ACCESS_IDX;
import static org.forpdi.dashboard.indicators.CompanyAccessHistory.FRISCO_ACCESS_IDX;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.company.Company;
import org.forpdi.core.location.CompanyIndicators;
import org.forpdi.core.location.Region;
import org.forpdi.core.utils.Counter;
import org.forpdi.dashboard.indicators.AccessHistoryCalculator.AccessHistoryResult;
import org.forpdi.dashboard.indicators.CompanyAccessHistory;
import org.forpdi.dashboard.indicators.IndicatorsDashboardHelper;
import org.forpdi.dashboard.indicators.RegionCounts;
import org.forpdi.system.reports.pdf.PDFSettings;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.io.ByteStreams;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

@Component
public class IndicatorsReportGeneratorHelper {
	private static final Map<Long, BaseColor> regionsColorsMap = new HashMap<>() {
		private static final long serialVersionUID = 1L;
		{
			put(1L, WebColors.getRGBColor("#4bda33"));
			put(2L, WebColors.getRGBColor("#599fd2"));
			put(3L, WebColors.getRGBColor("#d97c2f"));
			put(4L, WebColors.getRGBColor("#fe0000"));
			put(5L, WebColors.getRGBColor("#d7dc2a"));
		}
	};

	private static final int FPDI_COLOR = 28 << 16 | 82 << 8 | 123;
	private static final int FRISCO_COLOR = 216 << 16 | 98 << 8 | 225;
	private static final int DEFAULT_COLOR_1 = 220 << 16 | 57 << 8 | 18; // red
	private static final int DEFAULT_COLOR_2 = 51 << 16 | 102 << 8 | 204; // blue
	private static final int DEFAULT_COLOR_3 = 255 << 16 | 179 << 8 | 71;
	private static final String FPDI_COLOR_HEX = "#1c527b";
	private static final String FRISCO_COLOR_HEX = "#d862e1";
	private static final String DEFAULT_COLOR_1_HEX = "#3266cc"; // red
	private static final String DEFAULT_COLOR_2_HEX = "#dc3812"; // blue

	private static final DecimalFormat df = new DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.US));
	private final Font mainFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12.0f, BaseColor.BLACK);
	private final Font secondaryFont = FontFactory.getFont(FontFactory.HELVETICA, 12.0f, BaseColor.BLACK);

	@Autowired
	private IndicatorsDashboardHelper exportReportsHelper;

	public RegionCounts getRegionCounts(Long regionId, List<RegionCounts> companiesRegionCounts) {
		return companiesRegionCounts.stream().filter(cRC -> cRC.getRegionId().equals(regionId)).findFirst()
				.orElse(null);
	}

	public Image getRegionMap(Long regionId, List<Region> regions)
			throws MalformedURLException, IOException, DocumentException {
		Map<Long, List<Float>> individualMapPos = new HashMap<>();
		individualMapPos.put(1L, Arrays.asList(160f, 520f));
		individualMapPos.put(2L, Arrays.asList(220f, 520f));
		individualMapPos.put(3L, Arrays.asList(180f, 520f));
		individualMapPos.put(4L, Arrays.asList(215f, 520f));
		individualMapPos.put(5L, Arrays.asList(205f, 520f));
		Region region = getRegion(regionId, regions);

		Image image = getImage(region.getName().toLowerCase());
		image.scaleToFit(300, 170);
		List<Float> pos = individualMapPos.get(regionId);
		image.setAbsolutePosition(pos.get(0), pos.get(1));

		return image;
	}

	public Region getRegion(Long regionId, List<Region> regions) {
		Region region = regions.stream().filter(rg -> rg.getId() == regionId).findFirst().orElse(null);
		return region;
	}

	public Image getRegionMap() throws MalformedURLException, IOException, DocumentException {
		Image image = getImage("regioes");
		image.scaleToFit(550, 270);
		image.setAbsolutePosition(175, 440);
		return image;
	}

	public PdfPTable getRegionTable(Paragraph regionCount, List<Company> companies,
			List<RegionCounts> companiesRegionsCount, Long regionId) {
		PdfPTable table = new PdfPTable(1);
		Font font = PDFSettings.TEXT_FONT_BOLD;

		Float percentage = getPercentage(regionId, companies, companiesRegionsCount);

		PdfPCell valueCell = getValueCell(percentage, font);
		valueCell.setBorder(PdfPCell.NO_BORDER);
		valueCell.setPaddingTop(75);
		valueCell.setPaddingBottom(-30);
		valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(valueCell);

		PdfPCell cell = new PdfPCell();
		cell.setBorder(PdfPCell.NO_BORDER);
		cell.setFixedHeight(100);
		table.addCell(cell);

		PdfPCell regionCounts = new PdfPCell(regionCount);
		regionCounts.setBorder(PdfPCell.NO_BORDER);
		regionCounts.setPaddingBottom(30);
		regionCounts.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(regionCounts);

		table.setWidthPercentage(75);
		table.setHorizontalAlignment(Element.ALIGN_CENTER);
		return table;
	}

	public PdfPTable getRegionTable(Map<Long, Paragraph> regionCount, List<Company> companies,
			List<RegionCounts> companiesRegionsCount) {
		Map<Long, List<Long>> individualMapPos = new HashMap<>();
		individualMapPos.put(1L, Arrays.asList(35L, 60L, 0L, 0L));
		individualMapPos.put(2L, Arrays.asList(15L, -160L, 0L, 0L));
		individualMapPos.put(5L, Arrays.asList(35L, -20L, 0L, 0L));
		individualMapPos.put(3L, Arrays.asList(10L, -120L, 0L, 0L));
		individualMapPos.put(4L, Arrays.asList(35L, -43L, 0L, 0L));
		PdfPTable table = new PdfPTable(1);
		Font font = PDFSettings.TEXT_FONT_BOLD;

		List<RegionCounts> companieRegionCountsFiltered = Arrays.asList(companiesRegionsCount.get(0),
				companiesRegionsCount.get(1), companiesRegionsCount.get(4), companiesRegionsCount.get(2),
				companiesRegionsCount.get(3));
		for (RegionCounts regionCounts : companieRegionCountsFiltered) {
			Long regionId = regionCounts.getRegionId();
			Float percentage = getPercentage(regionId, companies, companiesRegionsCount);

			List<Long> pos = individualMapPos.get(regionId);

			PdfPCell valueCell = getValueCell(percentage, font);
			valueCell.setBorder(PdfPCell.NO_BORDER);
			valueCell.setPaddingTop(pos.get(0));
			valueCell.setPaddingRight(pos.get(1));
			valueCell.setPaddingBottom(pos.get(2));
			valueCell.setPaddingLeft(pos.get(3));
			valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(valueCell);
		}

		PdfPCell cell = new PdfPCell();
		cell.setBorder(PdfPCell.NO_BORDER);
		cell.setFixedHeight(100);
		table.addCell(cell);

		for (RegionCounts regionCounts : companiesRegionsCount) {
			Long regionId = regionCounts.getRegionId();
			PdfPCell regionCountsCell = new PdfPCell(regionCount.get(regionId));
			regionCountsCell.setPaddingTop(-60);
			regionCountsCell.setPaddingBottom(30);
			regionCountsCell.setBorder(PdfPCell.NO_BORDER);
			regionCountsCell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table.addCell(regionCountsCell);
		}

		table.setWidthPercentage(75);
		table.setHorizontalAlignment(Element.ALIGN_CENTER);
		return table;
	}

	public PdfPCell getValueCell(Float percentage, Font font) {
		String formattedPercentage = df.format(percentage);
		return new PdfPCell(new Phrase(formattedPercentage + "%", font));
	}

	public float getPercentage(Long regionId, List<Company> companies, List<RegionCounts> companiesRegionsCount) {
		Float companiesSize = (float) companies.size();

		if (companiesSize == 0) {
			return 0;
		}

		RegionCounts regionCounts = getRegionCounts(regionId, companiesRegionsCount);
		Float universityCount = (float) regionCounts.getUniversityCount();
		Float instituteCount = (float) regionCounts.getInstituteCount();

		int percentage = Math.round(((universityCount + instituteCount) * 100) / companiesSize);
		return percentage;
	}

	public Image getImage(String filePath) throws MalformedURLException, IOException, DocumentException {
		String imagePath = "/img/" + filePath + ".png";
		InputStream is = getClass().getResourceAsStream(imagePath);
		Image image = Image.getInstance(ByteStreams.toByteArray(is));
		return image;
	}

	public Image getPieChartImage(Long regionId, List<RegionCounts> companiesRegionsCount)
			throws MalformedURLException, IOException, DocumentException {
		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
		Float universityCount = 0f;
		Float instituteCount = 0f;
		Float otherCount = 0f;

		if (regionId == -1) {
			for (RegionCounts regionCounts : companiesRegionsCount) {
				universityCount += regionCounts.getUniversityCount();
				instituteCount += regionCounts.getInstituteCount();
				otherCount += regionCounts.getOtherCount();
			}
		} else {
			RegionCounts regionCounts = getRegionCounts(regionId, companiesRegionsCount);
			universityCount += regionCounts.getUniversityCount();
			instituteCount += regionCounts.getInstituteCount();
			otherCount += regionCounts.getOtherCount();
		}

		Float totalCount = universityCount + instituteCount + otherCount;
		Float universityPercentage = universityCount * 100 / totalCount;
		Float institutePercentage = 100 - universityPercentage;
		Float otherPercentage = (otherCount * 100) / totalCount;

		String universityCategory = " " + df.format(universityPercentage) + "%";
		String instituteCategory = df.format(institutePercentage) + "% ";
		String otherCategory = df.format(otherPercentage) + "% ";

		dataset.setValue(universityCategory, universityPercentage);
		dataset.setValue(instituteCategory, institutePercentage);
		dataset.setValue(otherCategory, otherPercentage);

		JFreeChart chart = ChartFactory.createPieChart(null, dataset, false, false, false);

		chart.setBackgroundPaint(ChartColor.WHITE);

		PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
		plot.setBackgroundPaint(ChartColor.WHITE);
		plot.setOutlineVisible(false);
		plot.setShadowPaint(null);
		plot.setInsets(new RectangleInsets(0, 0, 0, 0));
		plot.setSimpleLabels(true);
		plot.setLabelBackgroundPaint(null);
		plot.setLabelOutlinePaint(null);
		plot.setLabelShadowPaint(null);
		plot.setLabelPaint(Color.WHITE);

		Color defaultColor1 = new Color(DEFAULT_COLOR_1);
		Color defaultColor2 = new Color(DEFAULT_COLOR_2);
		Color defaultColor3 = new Color(DEFAULT_COLOR_3);

		plot.setSectionPaint(universityCategory,
			(universityPercentage == 0) ? defaultColor1 : defaultColor2);
		plot.setSectionPaint(instituteCategory,
			(institutePercentage == 0) ? defaultColor2 : defaultColor1);
		plot.setSectionPaint(otherCategory,
			(otherPercentage == 0) ? defaultColor1 : defaultColor3);

		if (universityPercentage == 0 || institutePercentage == 0 || otherPercentage == 0) {
			plot.setLabelGenerator(null);
		}

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int width = 250;
		int height = 150;
		ChartUtils.writeChartAsPNG(byteArrayOutputStream, chart, width, height);

		Image image = Image.getInstance(byteArrayOutputStream.toByteArray());
		return image;
	}

	public PdfPTable getPieChartTable(Image pieChartImage, List<RegionCounts> companiesRegionsCount) {
		PdfPTable table = new PdfPTable(2);

		PdfPCell imageCell = new PdfPCell(pieChartImage);
		imageCell.setPaddingTop(15);
		imageCell.setBorder(0);
		imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.addCell(imageCell);

		PdfPCell legendCell = new PdfPCell();
		legendCell.addElement(getChartListItem("Universidades", WebColors.getRGBColor(DEFAULT_COLOR_1_HEX)));
		legendCell.addElement(getChartListItem("Institutos Federais", WebColors.getRGBColor(DEFAULT_COLOR_2_HEX)));
		legendCell.setBorder(0);
		legendCell.setPaddingTop(45);
		legendCell.setPaddingLeft(50);
		legendCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(legendCell);

		return table;
	}

	public Image getGraphImage(List<Company> companies) throws IOException, DocumentException {
		TimeSeries seriesAdhesions = new TimeSeries("Adesões");

		var adhesionByYearMonths = exportReportsHelper.getAhesionsByYearMonths(companies);
		Map<YearMonth, Counter> adhesionsCountMap = adhesionByYearMonths.adhesionCountMap();
		for (YearMonth ym : adhesionByYearMonths.sortedYearMonths()) {
			int adhesions = adhesionsCountMap.get(ym).getCounter();
			seriesAdhesions.add(new Month(ym.getMonthValue(), ym.getYear()), adhesions);
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(seriesAdhesions);

		Image chart = getChartImageByDataset(dataset, "Mês/Ano", "Adesões", true, null, DEFAULT_COLOR_2,
				DEFAULT_COLOR_2);

		return chart;
	}

	public Map<String, Integer> getAllRegionDateMap(List<Company> companies) {
		Map<String, Integer> allDateMap = new HashMap<>();

		for (Company company : companies) {
			Date creationDate = company.getCreation();
			String[] dateTimeSplit = creationDate.toString().split(" ");
			String dateString = dateTimeSplit[0];
			String yearAndMonth = dateString.length() > 7 ? dateString.substring(0, 7) : dateString;

			Integer value = allDateMap.get(yearAndMonth);
			allDateMap.put(yearAndMonth, value == null ? 1 : value + 1);
		}

		return allDateMap;
	}

	public PdfPTable getAccessHistoryPieChartTable(Image pieChartImage,
			List<CompanyAccessHistory> companiesAccessHistory) {
		List<Integer> fpdiAndFriskNumbersAccesses = getNumberOfFpdiAndFriskAcess(companiesAccessHistory);
		int totalFpdiAccesses = fpdiAndFriskNumbersAccesses.get(FPDI_ACCESS_IDX);
		int totalFriscoAccesses = fpdiAndFriskNumbersAccesses.get(FRISCO_ACCESS_IDX);
		PdfPTable table = new PdfPTable(2);

		PdfPCell imageCell = new PdfPCell(pieChartImage);
		imageCell.setPaddingTop(15);
		imageCell.setBorder(0);
		imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.addCell(imageCell);

		PdfPCell legendCell = new PdfPCell();
		legendCell.addElement(
				getChartListItem("Acessos PDI (" + totalFpdiAccesses + ")", WebColors.getRGBColor(FPDI_COLOR_HEX)));
		legendCell.addElement(getChartListItem("Acessos RISCO (" + totalFriscoAccesses + ")",
				WebColors.getRGBColor(FRISCO_COLOR_HEX)));
		legendCell.setBorder(0);
		legendCell.setPaddingTop(45);
		legendCell.setPaddingLeft(25);
		legendCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(legendCell);

		return table;
	}

	public Image[] getAccessHistoryLineChartImages(List<CompanyAccessHistory> companiesAccessHistory)
			throws IOException, DocumentException {
		int totalFpdiAccess = 0;
		int totalFriscoAccess = 0;
		Map<LocalDate, Integer[]> dateAccesses = new HashMap<>();

		for (var cah : companiesAccessHistory) {
			for (var entry : cah.getHistory().entrySet()) {
				LocalDate date = LocalDate.parse(entry.getKey());
				if (!dateAccesses.containsKey(date)) {
					dateAccesses.put(date, new Integer[] { 0, 0 });
				}
				Integer[] accesses = entry.getValue();
				int fpdiAccess = accesses[FPDI_ACCESS_IDX];
				int friscoAccess = accesses[FRISCO_ACCESS_IDX];
				totalFpdiAccess += fpdiAccess;
				totalFriscoAccess += friscoAccess;
				dateAccesses.get(date)[FPDI_ACCESS_IDX] += accesses[FPDI_ACCESS_IDX];
				dateAccesses.get(date)[FRISCO_ACCESS_IDX] += accesses[FRISCO_ACCESS_IDX];
			}
		}

		Image pieChart = getAccessHistoryPieChartImage(totalFpdiAccess, totalFriscoAccess);
		Image lineChartImage = getAccessHistoryLineChartImage(dateAccesses);

		return new Image[] { pieChart, lineChartImage };
	}

	private Image getAccessHistoryPieChartImage(int totalFpdiAccess, int totalFriscoAccess)
			throws MalformedURLException, IOException, DocumentException {

		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
		String fpdiCategory;
		String friscoCategory;

		if (totalFpdiAccess + totalFriscoAccess > 0) {
			int total = totalFpdiAccess + totalFriscoAccess;

			BigDecimal fpdiPercentageBd = BigDecimal.valueOf((totalFpdiAccess * 100.0) / total);
			float fpdiPercentage = fpdiPercentageBd.setScale(1, RoundingMode.HALF_UP).floatValue();
			float friscoPercentage = 100 - fpdiPercentage;
			fpdiCategory = " " + fpdiPercentage + "%";
			friscoCategory = friscoPercentage + "% ";
			dataset.setValue(fpdiCategory, fpdiPercentage);
			dataset.setValue(friscoCategory, friscoPercentage);
		} else {
			fpdiCategory = "50%";
			friscoCategory = "50%";
			dataset.setValue(fpdiCategory, 50);
			dataset.setValue(friscoCategory, 50);
		}

		JFreeChart chart = ChartFactory.createPieChart(null, dataset, false, false, false);

		chart.setBackgroundPaint(ChartColor.WHITE);

		PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
		plot.setBackgroundPaint(ChartColor.WHITE);
		plot.setOutlineVisible(false);
		plot.setShadowPaint(null);
		plot.setInsets(new RectangleInsets(0, 0, 0, 0));
		plot.setSimpleLabels(true);
		plot.setLabelBackgroundPaint(null);
		plot.setLabelOutlinePaint(null);
		plot.setLabelShadowPaint(null);
		plot.setLabelPaint(Color.WHITE);
		plot.setSectionPaint(fpdiCategory, new Color(FPDI_COLOR));
		plot.setSectionPaint(friscoCategory, new Color(FRISCO_COLOR));

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int width = 250;
		int height = 150;
		ChartUtils.writeChartAsPNG(byteArrayOutputStream, chart, width, height);

		Image image = Image.getInstance(byteArrayOutputStream.toByteArray());
		return image;
	}

	private Image getAccessHistoryLineChartImage(Map<LocalDate, Integer[]> dateAccesses)
			throws IOException, DocumentException {
		TimeSeries seriesPDI = new TimeSeries("PDI");
		TimeSeries seriesRiscos = new TimeSeries("RISCOS");
		Set<YearMonth> yearMonthsSet = new HashSet<>();

		for (var entry : dateAccesses.entrySet()) {
			LocalDate date = entry.getKey();
			Integer[] accesses = entry.getValue();
			int fpdiAccess = accesses[FPDI_ACCESS_IDX];
			int friscoAccess = accesses[FRISCO_ACCESS_IDX];
			seriesPDI.add(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), fpdiAccess);
			seriesRiscos.add(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), friscoAccess);
			yearMonthsSet.add(YearMonth.from(date));
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(seriesPDI);
		dataset.addSeries(seriesRiscos);

		DateTickUnitType dateTickUnitType = yearMonthsSet.size() == 1 ? DateTickUnitType.DAY : DateTickUnitType.MONTH;
		Image chart = getChartImageByDataset(dataset, "Mês/Ano", "Acessos", false, dateTickUnitType, FPDI_COLOR,
				FRISCO_COLOR);

		return chart;
	}

	private Image getChartImageByDataset(TimeSeriesCollection dataset, String xLabel, String yLabel,
			boolean showSeriesShapes, DateTickUnitType dateTickUnitType, int colorRgb1, int colorRgb2)
			throws IOException, DocumentException {
		JFreeChart chart = ChartFactory.createTimeSeriesChart(null, xLabel, yLabel, dataset, true, true, false);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(ChartColor.WHITE);
		plot.setDomainGridlinePaint(ChartColor.LIGHT_GRAY);
		plot.setRangeGridlinePaint(ChartColor.LIGHT_GRAY);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesLinesVisible(0, true);
		renderer.setSeriesShapesVisible(0, showSeriesShapes);
		renderer.setSeriesLinesVisible(1, true);
		renderer.setSeriesShapesVisible(1, showSeriesShapes);
		renderer.setSeriesPaint(0, new Color(colorRgb1));
		renderer.setSeriesPaint(1, new Color(colorRgb2));
		plot.setRenderer(renderer);

		DateAxis axis = (DateAxis) plot.getDomainAxis();
		DateFormat dateFormat = new SimpleDateFormat("MM/yyyy");
		axis.setDateFormatOverride(dateFormat);
		if (dateTickUnitType != null) {
			axis.setTickUnit(new DateTickUnit(dateTickUnitType, 1));
		}

		var tickLabelFont = new java.awt.Font("Arial", java.awt.Font.PLAIN, 8);
		axis.setTickLabelFont(tickLabelFont);
		var rotatedFont = tickLabelFont;
		axis.setTickLabelFont(rotatedFont);

		NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		yAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int width = 560;
		int height = 250;

		ChartUtils.writeChartAsPNG(byteArrayOutputStream, chart, width, height);
		Image chartImage = Image.getInstance(byteArrayOutputStream.toByteArray());
		chartImage.setAlignment(Image.ALIGN_CENTER);

		return chartImage;
	}

	public Paragraph getRegionListItem(List<RegionCounts> companiesRegionCounts, Long regionId) {
		Paragraph paragraph = new Paragraph("", PDFSettings.TEXT_FONT);
		RegionCounts regionCounts = getRegionCounts(regionId, companiesRegionCounts);

		String text = "  " + regionCounts.getRegionName() + ": " + regionCounts.getUniversityCount()
				+ " Universidades / " + regionCounts.getInstituteCount() + " Institutos Federais";

		Phrase phrase = new Phrase(
				getChartListItem(text, IndicatorsReportGeneratorHelper.regionsColorsMap.get(regionId)));

		paragraph.add(phrase);
		return paragraph;
	}

	public Phrase getChartListItem(String text, BaseColor color) {
		Font bulletFont = new Font(Font.FontFamily.HELVETICA, 50, Font.NORMAL, color);
		Chunk bulletPointSymbol = new Chunk("\u2022", bulletFont);
		bulletPointSymbol.setTextRise(-12);

		Phrase phrase = new Phrase("", PDFSettings.TEXT_FONT);

		phrase.add(bulletPointSymbol);
		phrase.add(text);
		return phrase;
	}

	public Map<Long, Paragraph> getRegionListItem(List<RegionCounts> companiesRegionCounts) {
		Map<Long, Paragraph> paragraphs = new HashMap<>();

		for (RegionCounts regionCounts : companiesRegionCounts) {
			Long regionId = regionCounts.getRegionId();
			Paragraph paragraph = getRegionListItem(companiesRegionCounts, regionId);

			paragraphs.put(regionId, paragraph);
		}
		return paragraphs;
	}

	public List<List<String>> getCompanyIndicatorsTable(List<CompanyIndicators> companyIndicatorsList,
			Map<Long, Integer[]> companiesAccessHistory, String period) {
		List<List<String>> resultList = new ArrayList<>();

		List<String> headerList = new ArrayList<>();
		headerList.add("Sigla");
		headerList.add("Data de Adesão");
		headerList.add("Período");
		headerList.add("Acessos PDI");
		headerList.add("Acessos Riscos");
		headerList.add("PDIs cadastrados");
		headerList.add("Planos de Risco cadastrados");
		resultList.add(headerList);

		for (int i = 0; i < companyIndicatorsList.size(); i++) {
			List<String> attributesList = new ArrayList<>();
			CompanyIndicators company = companyIndicatorsList.get(i);
			Integer[] accesses = companiesAccessHistory.get(company.getId());
			attributesList.add(company.getInitials());
			attributesList.add(GeneralUtils.parseDateToString(company.getCreation()));
			attributesList.add(parsePeriodString(period));
			attributesList.add(accesses[CompanyAccessHistory.FPDI_ACCESS_IDX].toString());
			attributesList.add(accesses[CompanyAccessHistory.FRISCO_ACCESS_IDX].toString());
			attributesList.add(String.valueOf(company.getPlanMacrosCount()));
			attributesList.add(String.valueOf(company.getPlanRisksCount()));

			resultList.add(attributesList);
		}

		return resultList;
	}

	public List<CompanyAccessHistory> filterCompanyAccessHistories(AccessHistoryResult accessHistoryResult,
			List<Company> companies) {
		List<CompanyAccessHistory> companiesAccessHistory = accessHistoryResult.getCompaniesAccessHistory();

		List<Long> companyIds = companies.stream().map(Company::getId).collect(Collectors.toList());

		List<CompanyAccessHistory> filteredList = companiesAccessHistory.stream()
				.filter(history -> companyIds.contains(history.getCompanyId())).collect(Collectors.toList());

		return filteredList;
	}

	public List<List<String>> getAccessHistoryList(List<CompanyAccessHistory> companiesAccessHistory) {
		List<List<String>> result = new ArrayList<>();

		for (CompanyAccessHistory companyHistory : companiesAccessHistory) {
			Map<String, Integer[]> historyMap = companyHistory.getHistory();

			for (Map.Entry<String, Integer[]> entry : historyMap.entrySet()) {
				List<String> attributesList = new ArrayList<>();

				Integer[] accesses = entry.getValue();
				int fpdiAccessCount = accesses[0];
				int friscoAccessCount = accesses[1];
				attributesList.add(String.valueOf(fpdiAccessCount));
				attributesList.add(String.valueOf(friscoAccessCount));

				result.add(attributesList);
			}
		}

		return result;
	}

	public String parsePeriodString(String period) {
		String[] periodSplit = period.split(" ");
		String periodBegin = periodSplit[0];
		String periodEnd = periodSplit[1];
		String periodString = ((!periodBegin.equals("*") && periodEnd.equals("*")) ? ("A partir de " + periodBegin)
				: "") + ((!periodBegin.equals("*") && !periodEnd.equals("*")) ? (periodBegin + " à " + periodEnd) : "")
				+ ((periodBegin.equals("*") && !periodEnd.equals("*")) ? ("Até " + periodEnd) : "")
				+ ((periodBegin.equals("*") && periodEnd.equals("*")) ? ("Não determinado") : "");
		return periodString;
	}

	public Phrase getItem(String bold, String item) {
		Chunk boldChunk = new Chunk(bold, mainFont);
		Chunk normalChunk = new Chunk(item, secondaryFont);

		Phrase phrase = new Phrase();
		phrase.add(boldChunk);
		phrase.add(normalChunk);

		return phrase;
	}

	public List<Integer> getNumberOfFpdiAndFriskAcess(List<CompanyAccessHistory> companiesAccessHistory) {
		int totalFpdiAccess = 0;
		int totalFriscoAccess = 0;
		for (var cah : companiesAccessHistory) {
			for (var entry : cah.getHistory().entrySet()) {
				Integer[] accesses = entry.getValue();
				int fpdiAccess = accesses[FPDI_ACCESS_IDX];
				int friscoAccess = accesses[FRISCO_ACCESS_IDX];
				totalFpdiAccess += fpdiAccess;
				totalFriscoAccess += friscoAccess;
			}
		}
		return List.of(totalFpdiAccess, totalFriscoAccess);
	}
}