package org.forpdi.system.reports.platfor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyType;
import org.forpdi.core.location.CompanyIndicators;
import org.forpdi.core.location.Region;
import org.forpdi.core.location.RegionRepository;
import org.forpdi.core.utils.Util;
import org.forpdi.dashboard.indicators.AccessHistoryCalculator.AccessHistoryResult;
import org.forpdi.dashboard.indicators.CompanyAccessHistory;
import org.forpdi.dashboard.indicators.CompanyForriscoIndicators;
import org.forpdi.dashboard.indicators.CompanyFpdiIndicators;
import org.forpdi.dashboard.indicators.IndicatorsDashboardBS;
import org.forpdi.dashboard.indicators.IndicatorsDashboardHelper;
import org.forpdi.system.reports.ReportGenerator;
import org.forpdi.system.reports.ReportGeneratorHelper;
import org.forpdi.system.reports.ReportGeneratorParams;
import org.forpdi.system.reports.pdf.PdfWriterFactory;
import org.forpdi.system.reports.pdf.TOCEvent;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;

@Component
public class IndicatorsBoardReportGenerator implements ReportGenerator {
	protected final Font mainFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12.0f, BaseColor.BLACK);
	protected final Font secondaryFont = FontFactory.getFont(FontFactory.HELVETICA, 12.0f, BaseColor.BLACK);
	protected final Font tableMainFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.0f, BaseColor.BLACK);
	protected final Font tableSecondaryFont = FontFactory.getFont(FontFactory.HELVETICA, 10.0f, BaseColor.BLACK);

	@Autowired
	private IndicatorsDashboardBS indicatorsDashboardBS;

	@Autowired
	private ReportGeneratorHelper reportGeneratorHelper;

	@Autowired
	private IndicatorsReportGeneratorHelper indicatorsReportGeneratorHelper;

	@Autowired
	private IndicatorsDashboardHelper indicatorsDashboardHelper;

	@Autowired
	private CompanyBS companyBS;

	@Autowired
	private RegionRepository regionRepository;

	@Override
	public InputStream generateReport(ReportGeneratorParams params) {
		try {
			Params parsedParams = ReportGeneratorParams.extractParams(params, Params.class);
			return exportBoardReport(parsedParams.selecao, parsedParams.selectedCompanies, parsedParams.specific);
		} catch (IOException | DocumentException e) {
			throw new RuntimeException(e);
		}
	}

	private InputStream exportBoardReport(String selecao, String selectedCompanies, String specific)
			throws IOException, DocumentException {
		int selectedSection = Integer.parseInt(specific);
		String title = "Relatório de indicadores";
		int summaryCountPages = 0;
		File outputDir = TempFilesManager.getTempDir();

		final String prefix = String.format("frisco-report-export-%d", System.currentTimeMillis());

		File contentFile = new File(outputDir, String.format("%s-content.pdf", prefix));

		List<String> filters = Arrays.asList(selecao.split(","));

		List<Region> regions = regionRepository.findAll();
		List<Company> allCompanies = companyBS.listAll();
		List<Company> companies = filterCompanies(selectedCompanies, allCompanies);

		File destinationFile = new File(outputDir, String.format("%s-mounted.pdf", prefix));
		File finalPdfFile = new File(outputDir, String.format("%s-final.pdf", prefix));

		Document newDocument = new Document();

		PdfCopy combinedContent = new PdfCopy(newDocument, new FileOutputStream(destinationFile.getPath()));

		newDocument.open();
		newDocument.addTitle(title);

		TOCEvent event = new TOCEvent();

		boolean hasCoverPage = (selectedSection == IndicatorsDashboardHelper.ALL_CARDS_ID);

		if (hasCoverPage) {
			File finalSummaryPdfFile = new File(outputDir, String.format("%s-final-summary.pdf", prefix));
			File coverPdfFile = new File(outputDir, String.format("%s-cover.pdf", prefix));

			Map<String, String> coverTextContent = new HashMap<>();
			coverTextContent.put("Relatório de indicadores", "");
			reportGeneratorHelper.generateCover(coverPdfFile, title, coverTextContent);

			PdfReader cover = new PdfReader(coverPdfFile.getPath());

			exportBoardReport(contentFile, filters, allCompanies, companies, regions, event);

			summaryCountPages = reportGeneratorHelper.generateSummary(finalSummaryPdfFile, event,
					cover.getNumberOfPages());

			PdfReader summary = new PdfReader(finalSummaryPdfFile.getPath());

			reportGeneratorHelper.copyDocumentContent(combinedContent, cover);
			reportGeneratorHelper.copyDocumentContent(combinedContent, summary);

		} else {
			exportBoardReportSpecific(contentFile, selectedSection, filters, allCompanies, companies, regions, event);
		}

		PdfReader content = new PdfReader(contentFile.getPath());

		reportGeneratorHelper.copyDocumentContent(combinedContent, content);

		newDocument.close();

		reportGeneratorHelper.manipulatePdf(destinationFile.getPath(), finalPdfFile.getPath(), summaryCountPages,
				"platfor", Collections.emptySet(), hasCoverPage);

		InputStream inputStream = new FileInputStream(finalPdfFile);

		TempFilesManager.cleanTempDir(outputDir, prefix);

		return inputStream;
	}

	private void exportBoardReport(File contentFile, List<String> filters, List<Company> allCompanies,
			List<Company> companies, List<Region> regions, TOCEvent event) throws DocumentException, IOException {
		Document document = new Document();

		PdfWriterFactory.create(document, contentFile, event);

		reportGeneratorHelper.setDimensions(document);
		document.open();

		var accessHistoryResult = getFilteredAccessHistory(filters);
		var companiesFpdiIndicators = indicatorsDashboardBS.getCompaniesFpdiIndicators(companies);
		var companiesFriscoIndicators = indicatorsDashboardBS.getCompaniesFriscoIndicators(companies);
		var companiesIndicators = indicatorsDashboardBS.getCompaniesIndicators(companies, companiesFpdiIndicators,
				companiesFriscoIndicators);

		generateIntroductionSection(document, filters, allCompanies, companies, regions);

		document.newPage();
		generateRegionsCountsSection(document, filters, allCompanies, companies, regions, false);

		document.newPage();
		generateCompaniesFpdiIndicatorsSection(document, companies, companiesFpdiIndicators);
		generateCompaniesFriscoIndicatorsSection(document, companies, companiesFriscoIndicators);

		document.newPage();
		generateCompaniesAccessHistorySection(document, companies, accessHistoryResult.getCompaniesAccessHistory(),
				false);

		document.newPage();
		generateCompaniesIndicatorsSection(document, companies, companiesFpdiIndicators, companiesFriscoIndicators,
				accessHistoryResult, companiesIndicators, filters.get(2), false);

		document.close();
	}

	private void exportBoardReportSpecific(File contentFile, int selectedSection, List<String> filters,
			List<Company> allComapnies, List<Company> companies, List<Region> regions, TOCEvent event)
			throws DocumentException, IOException {

		Document document = new Document();

		PdfWriterFactory.create(document, contentFile, event);

		reportGeneratorHelper.setDimensions(document);
		document.open();

		if (selectedSection == IndicatorsDashboardHelper.REGIONS_COUNT_CARD_ID) {
			generateRegionsCountsSection(document, filters, allComapnies, companies, regions, true);
		} else if (selectedSection == IndicatorsDashboardHelper.ACCESS_CARD_ID) {
			var accessHistoryResult = getFilteredAccessHistory(filters);
			List<CompanyAccessHistory> companiesAccessHistory = accessHistoryResult.getCompaniesAccessHistory();
			generateCompaniesAccessHistorySection(document, companies, companiesAccessHistory, true);
		} else if (selectedSection == IndicatorsDashboardHelper.INSTITUTIONS_CARD_ID) {
			var accessHistoryResult = getFilteredAccessHistory(filters);
			var companiesFpdiIndicators = indicatorsDashboardBS.getCompaniesFpdiIndicators(companies);
			var companiesFriscoIndicators = indicatorsDashboardBS.getCompaniesFriscoIndicators(companies);
			var companiesIndicators = indicatorsDashboardBS.getCompaniesIndicators(companies, companiesFpdiIndicators,
					companiesFriscoIndicators);
			generateCompaniesIndicatorsSection(document, companies, companiesFpdiIndicators, companiesFriscoIndicators,
					accessHistoryResult, companiesIndicators, filters.get(2), true);
		} else {
			throw new IllegalArgumentException("Invalid section id: " + selectedSection);
		}

		document.close();
	}

	private AccessHistoryResult getFilteredAccessHistory(List<String> filters) {
		String period = filters.get(2);
		String[] splitPeriod = period.split(" ");
		String beginStr = splitPeriod[0].equals("*") ? null : splitPeriod[0];
		String endStr = splitPeriod[1].equals("*") ? null : splitPeriod[1];
		System.out.println(period);

		return indicatorsDashboardBS.getCompaniesAccessHistoryByPeriod(beginStr, endStr);
	}

	private void generateIntroductionSection(Document document, List<String> filters, List<Company> allCompanies,
			List<Company> companies, List<Region> regions)
			throws DocumentException, MalformedURLException, IOException {
		String sectionName = "1. Introdução";
		document.add(reportGeneratorHelper.newSummarySection(sectionName));

		document.add(reportGeneratorHelper.newSummarySection("1.1 Filtros utilizados"));

		Long typeId = (long) Integer.valueOf(filters.get(1));
		Long regionId = (long) Integer.valueOf(filters.get(0));

		String typeString;
		String regionString = regionId != -1
				? this.indicatorsReportGeneratorHelper.getRegion(regionId, regions).getName()
				: "Todas";
		String periodString = this.indicatorsReportGeneratorHelper.parsePeriodString(filters.get(2));

		if (typeId == -1) {
			typeString = "Todos";
		} else {
			typeString = typeId == CompanyType.INSTITUTE.getId() ? CompanyType.INSTITUTE.getLabel()
					: CompanyType.UNIVERSITY.getLabel();
		}

		boolean allCompaniesIsSelected = allCompanies.size() == companies.size();

		document.add(new Paragraph(this.indicatorsReportGeneratorHelper.getItem("Região: ", regionString)));
		document.add(new Paragraph(this.indicatorsReportGeneratorHelper.getItem("Tipo: ", typeString)));
		document.add(new Paragraph(
				this.indicatorsReportGeneratorHelper.getItem("Instituição:", allCompaniesIsSelected ? " Todas" : "")));
		if (!allCompaniesIsSelected) {
			for (Company company : companies) {
				document.add(this.reportGeneratorHelper.listItem(company.getName()));
			}
		}
		document.add(new Paragraph(this.indicatorsReportGeneratorHelper.getItem("Período: ", periodString)));
	}

	private void generateRegionsCountsSection(Document document, List<String> filters, List<Company> allCompanies,
			List<Company> companies, List<Region> regions, boolean isSpecific)
			throws DocumentException, MalformedURLException, IOException {
		String sectionNumber = (isSpecific ? "1" : "2");
		String sectionName = sectionNumber + ". Adesões";
		document.add(reportGeneratorHelper.newSummarySection(sectionName));

		var companiesRegionsCount = this.indicatorsDashboardBS.getRegionsCounts(companies);
		var allCompaniesRegionsCount = this.indicatorsDashboardBS.getRegionsCounts(allCompanies);

		boolean isFullMap = Integer.parseInt(filters.get(0)) == -1;
		boolean isOneCompany = companies.size() == 1;

		document.add(reportGeneratorHelper.newSummarySection(sectionNumber + ".1 Por região:"));

		Company company = companies.get(0);
		Long regionId = isOneCompany ? company.getCounty().getUf().getRegion().getId()
				: Integer.parseInt(filters.get(0));

		if (!isFullMap) {
			Image regionImage = this.indicatorsReportGeneratorHelper.getRegionMap(regionId, regions);
			document.add(regionImage);

			Paragraph regionCount = this.indicatorsReportGeneratorHelper.getRegionListItem(allCompaniesRegionsCount,
					regionId);

			PdfPTable table = this.indicatorsReportGeneratorHelper.getRegionTable(regionCount, allCompanies,
					allCompaniesRegionsCount, regionId);
			document.add(table);

		} else {
			Image image = this.indicatorsReportGeneratorHelper.getRegionMap();
			document.add(image);

			Map<Long, Paragraph> regionCount = this.indicatorsReportGeneratorHelper
					.getRegionListItem(allCompaniesRegionsCount);

			PdfPTable table = this.indicatorsReportGeneratorHelper.getRegionTable(regionCount, allCompanies,
					allCompaniesRegionsCount);
			document.add(table);
		}

		if (isOneCompany) {
			String type = company.getType() == CompanyType.INSTITUTE.getId() ? CompanyType.INSTITUTE.getLabel()
					: CompanyType.UNIVERSITY.getLabel();
			String adhesionDate = GeneralUtils.parseDateToString(company.getCreation());

			document.add(reportGeneratorHelper.newSummarySection(sectionNumber + ".2 Detalhes da instituição:"));
			document.add(new Paragraph(this.indicatorsReportGeneratorHelper.getItem("Nome: ", company.getName())));
			document.add(new Paragraph(this.indicatorsReportGeneratorHelper.getItem("Sigla: ", company.getInitials())));
			document.add(new Paragraph(
					this.indicatorsReportGeneratorHelper.getItem("Descrição: ", company.getDescription())));
			document.add(new Paragraph(this.indicatorsReportGeneratorHelper.getItem("Tipo: ", type)));
			document.add(new Paragraph(this.indicatorsReportGeneratorHelper.getItem("Data de adesão: ", adhesionDate)));
		} else {
			document.add(reportGeneratorHelper.newSummarySection(sectionNumber + ".2 Por tipo de instituição:"));
			Image pieChartImage = this.indicatorsReportGeneratorHelper.getPieChartImage(regionId,
					companiesRegionsCount);
			PdfPTable pieChartTable = this.indicatorsReportGeneratorHelper.getPieChartTable(pieChartImage,
					companiesRegionsCount);
			document.add(pieChartTable);

			document.add(reportGeneratorHelper.newSummarySection(sectionNumber + ".3 Por período de adesão:"));
			Image graphImage = this.indicatorsReportGeneratorHelper.getGraphImage(companies);
			document.add(graphImage);
		}
	}

	private void generateCompaniesAccessHistorySection(Document document, List<Company> companies,
			List<CompanyAccessHistory> companiesAccessHistory, boolean isSpecific)
			throws DocumentException, MalformedURLException, IOException {

		String sectionNumber = (isSpecific ? "1" : "5");
		String sectionName = sectionNumber + ". Acessos";
		document.add(reportGeneratorHelper.newSummarySection(sectionName));

		List<Long> companyIds = companies.stream().map(c -> c.getId()).toList();
		List<CompanyAccessHistory> filteredCompaniesAccessHistory = companiesAccessHistory.stream()
				.filter(cah -> companyIds.contains(cah.getCompanyId())).toList();

		if (accessHistoryIsEmty(filteredCompaniesAccessHistory)) {
			Paragraph paragraph = new Paragraph("Não foi possível encontrar dados de histórico de acessos.");
			paragraph.setAlignment(Element.ALIGN_CENTER);
			document.add(paragraph);
			return;
		}

		document.add(
				reportGeneratorHelper.newSummarySection(sectionNumber + ".1 Percentuais de acessos por plataforma"));

		Image[] chartImages = this.indicatorsReportGeneratorHelper
				.getAccessHistoryLineChartImages(filteredCompaniesAccessHistory);

		Image pieChartImage = chartImages[0];
		Image lineChartImage = chartImages[1];

		PdfPTable pieChartTable = this.indicatorsReportGeneratorHelper.getAccessHistoryPieChartTable(pieChartImage,
				filteredCompaniesAccessHistory);

		if (pieChartTable != null) {
			document.add(pieChartTable);
		} else {
			Paragraph noDataParagraph = new Paragraph("Não foi possível gerar o gráfico de acessos.");
			noDataParagraph.setAlignment(Element.ALIGN_CENTER);
			document.add(noDataParagraph);
		}

		document.add(new Paragraph(" "));
		document.add(reportGeneratorHelper.newSummarySection(sectionNumber + ".2 Acessos durante o período"));

		if (lineChartImage != null) {
			document.add(lineChartImage);
		} else {
			Paragraph noDataParagraph = new Paragraph("Não foi possível gerar o gráfico de acessos durante o período.");
			noDataParagraph.setAlignment(Element.ALIGN_CENTER);
			document.add(noDataParagraph);
		}
	}

	private boolean accessHistoryIsEmty(List<CompanyAccessHistory> companiesAccessHistory) {
		if (companiesAccessHistory != null) {
			for (CompanyAccessHistory cah : companiesAccessHistory) {
				if (!cah.getHistory().isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	private void generateCompaniesFpdiIndicatorsSection(Document document, List<Company> companies,
			List<CompanyFpdiIndicators> companiesFpdiIndicators)
			throws DocumentException, MalformedURLException, IOException {
		String sectionName = "3. PDI em números";
		document.add(reportGeneratorHelper.newSummarySection(sectionName));

		if (companiesFpdiIndicators.isEmpty()) {
			Paragraph paragraph = new Paragraph("Não foi possível encontrar dados cadastrados.");
			paragraph.setAlignment(Element.ALIGN_CENTER);

			document.add(paragraph);

			return;
		}

		List<Long> values = indicatorsDashboardHelper.countFpdiIndicators(companiesFpdiIndicators);

		document.add(new Paragraph("Quantidade de PDI vigentes: " + values.get(0), mainFont));
		document.add(reportGeneratorHelper.listItem("Plano de metas: " + values.get(1)));
		document.add(reportGeneratorHelper.listItem("Eixos temáticos: " + values.get(2)));
		document.add(reportGeneratorHelper.listItem("Objetivos: " + values.get(3)));
		document.add(reportGeneratorHelper.listItem("Indicadores: " + values.get(4)));
		document.add(reportGeneratorHelper.listItem("Metas: " + values.get(5)));
	}

	private void generateCompaniesFriscoIndicatorsSection(Document document, List<Company> companies,
			List<CompanyForriscoIndicators> companiesFriscoIndicators)
			throws DocumentException, MalformedURLException, IOException {
		Paragraph ident = new Paragraph();
		ident.setSpacingBefore(25);
		document.add(ident);

		String sectionName = "4. Risco em números";
		document.add(reportGeneratorHelper.newSummarySection(sectionName));

		if (companiesFriscoIndicators.isEmpty()) {
			Paragraph paragraph = new Paragraph("Não foi possível encontrar dados cadastrados.");
			paragraph.setAlignment(Element.ALIGN_CENTER);

			document.add(paragraph);

			return;
		}

		List<Long> values = indicatorsDashboardHelper.countFriscoIndicators(companiesFriscoIndicators);

		document.add(new Paragraph("Quantidade de Plano de Gestão de riscos vigentes: " + values.get(0), mainFont));
		document.add(reportGeneratorHelper.listItem("Políticas: " + values.get(1)));
		document.add(reportGeneratorHelper.listItem("Riscos: " + values.get(2)));
		document.add(reportGeneratorHelper.listItem("Realizando monitoramento: " + values.get(3)));
	}

	private void generateCompaniesIndicatorsSection(Document document, List<Company> companies,
			List<CompanyFpdiIndicators> companiesFpdiIndicators,
			List<CompanyForriscoIndicators> companiesFriscoIndicators, AccessHistoryResult accessHistoryResult,
			List<CompanyIndicators> companiesIndicators, String period, boolean isSpecific)
			throws DocumentException, MalformedURLException, IOException {
		String sectionName = (isSpecific ? "1" : "6") + ". Instituições";
		document.add(reportGeneratorHelper.newSummarySection(sectionName));

		if (companiesIndicators.isEmpty()) {
			Paragraph paragraph = new Paragraph("Não foi possível encontrar instituições cadastradas.");
			paragraph.setAlignment(Element.ALIGN_CENTER);

			document.add(paragraph);

			return;
		}

		String[] splitPeriod = period.split(" ");
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String companyCreationBeginStr = splitPeriod[0];
		String companyCreationEndStr = splitPeriod[1];
		Date companyCreationBegin = !companyCreationBeginStr.equals("*")
				? GeneralUtils.parseDate(companyCreationBeginStr, df)
				: null;
		Date companyCreationEnd = !companyCreationEndStr.equals("*") ? GeneralUtils.parseDate(companyCreationEndStr, df)
				: null;

		Map<Long, Integer[]> companiesAccessHistory = accessHistoryResult
				.getCompaniesAccessHistoryFilteredByPeriod(companies, companyCreationBegin, companyCreationEnd);

		var companiesIndicatorsList = this.indicatorsReportGeneratorHelper
				.getCompanyIndicatorsTable(companiesIndicators, companiesAccessHistory, period);

		PdfPTable table = new PdfPTable(7);
		table.setTotalWidth(500);
		table.setLockedWidth(true);

		BaseColor mainColor = new BaseColor(99, 185, 102);

		for (int i = 0; i < companiesIndicatorsList.size(); i++) {
			for (int j = 0; j < companiesIndicatorsList.get(i).size(); j++) {
				PdfPCell cell = new PdfPCell(new Paragraph(companiesIndicatorsList.get(i).get(j),
						(i == 0 ? tableMainFont : tableSecondaryFont)));

				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setPadding(10);
				cell.setBorderWidth(1);
				cell.setBackgroundColor(i == 0 ? mainColor : BaseColor.WHITE);

				table.addCell(cell);
			}
		}

		document.add(table);
	}

	private List<Company> filterCompanies(String selectedCompanies, List<Company> allCompanies) {
		List<Long> companyIds = Util.stringListToLongList(selectedCompanies);

		return allCompanies.stream().filter(c -> companyIds.contains(c.getId())).toList();
	}

	public static class Params implements ReportGeneratorParams {
		public String selecao;
		public String selectedCompanies;
		public String specific;

		public Params(String selecao, String selectedCompanies, String specific) {
			super();
			this.selecao = selecao;
			this.selectedCompanies = selectedCompanies;
			this.specific = specific;
		}
	}
}
