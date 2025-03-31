package org.forpdi.system.reports.frisco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.utils.Counter;
import org.forpdi.core.utils.DateUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.system.reports.ReportGenerator;
import org.forpdi.system.reports.ReportGeneratorHelper;
import org.forpdi.system.reports.ReportGeneratorParams;
import org.forpdi.system.reports.pdf.LineChartGenerator;
import org.forpdi.system.reports.pdf.LineChartGenerator.DatasetBuilder;
import org.forpdi.system.reports.pdf.MatrixGenerator;
import org.forpdi.system.reports.pdf.MatrixGenerator.MatrixCell;
import org.forpdi.system.reports.pdf.PDFSettings;
import org.forpdi.system.reports.pdf.PdfWriterFactory;
import org.forpdi.system.reports.pdf.TOCEvent;
import org.forpdi.system.reports.pdf.table.CellBackground;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.plan.PlanRiskBS;
import org.forrisco.core.policy.Policy;
import org.forrisco.core.policy.PolicyBS;
import org.forrisco.core.policy.RiskMatrixColor;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskLevel;
import org.forrisco.risk.incident.Incident;
import org.forrisco.risk.incident.IncidentBS;
import org.forrisco.risk.incident.IncidentType;
import org.forrisco.risk.monitor.MonitorBS;
import org.forrisco.risk.monitor.MonitorHistory;
import org.forrisco.risk.monitor.MonitoringState;
import org.forrisco.risk.typology.RiskTypologyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;


@Component
public class BoardReportGenerator implements ReportGenerator {

    @Autowired
    private PolicyBS policyBS;
    @Autowired
    private PlanRiskBS planriskBS;
    @Autowired
    private UnitBS unitBS;
    @Autowired
    private RiskBS riskBS;
    @Autowired
    private ReportGeneratorHelper reportGeneratorHelper;

	@Autowired
	private IncidentBS incidentBS;
	@Autowired
	private MonitorBS monitorBS;

    @Override
    public InputStream generateReport(ReportGeneratorParams params) {
        try {
            Params parsedParams = ReportGeneratorParams.extractParams(params, Params.class);
            return exportBoardReport(parsedParams.title, parsedParams.planId,
                    parsedParams.selecao);
        } catch (IOException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream exportBoardReport(String title, Long planId, String selecao)
            throws IOException, DocumentException {

        File outputDir = TempFilesManager.getTempDir();

        final String prefix = String.format("frisco-report-export-%d", System.currentTimeMillis());

        File finalSummaryPdfFile = new File(outputDir, String.format("%s-final-summary.pdf", prefix));
        File destinationFile = new File(outputDir, String.format("%s-mounted.pdf", prefix));
        File finalPdfFile = new File(outputDir, String.format("%s-final.pdf", prefix));
        File coverPdfFile = new File(outputDir, String.format("%s-cover.pdf", prefix));
        File preTextFile = new File(outputDir, String.format("%s-pre-text.pdf", prefix));
        File contentFile = new File(outputDir, String.format("%s-content.pdf", prefix));

        Map<String, String> coverTextContent = new HashMap<>();
        coverTextContent.put("Relatório do painel de bordo", "");
        reportGeneratorHelper.generateCover(coverPdfFile, title, coverTextContent);

        TOCEvent event = new TOCEvent();
        PdfReader cover = new PdfReader(coverPdfFile.getPath());

        List<Long> unitIds = Util.stringListToLongList(selecao);

        exportBoardReport(contentFile, preTextFile, planId, unitIds, event);

        int summaryCountPages = reportGeneratorHelper.generateSummary(finalSummaryPdfFile, event,
                cover.getNumberOfPages());

        Document newDocument = new Document();

        PdfCopy combinedContent = new PdfCopy(newDocument, new FileOutputStream(destinationFile.getPath()));

        newDocument.open();
        newDocument.addTitle(title);

        PdfReader preText = new PdfReader(preTextFile.getPath());
        PdfReader summary = new PdfReader(finalSummaryPdfFile.getPath());
        PdfReader content = new PdfReader(contentFile.getPath());

        reportGeneratorHelper.copyDocumentContent(combinedContent, cover);
        reportGeneratorHelper.copyDocumentContent(combinedContent, preText);
        reportGeneratorHelper.copyDocumentContent(combinedContent, summary);
        reportGeneratorHelper.copyDocumentContent(combinedContent, content);

        newDocument.close();

        reportGeneratorHelper
                .manipulatePdf(destinationFile.getPath(), finalPdfFile.getPath(), summaryCountPages, "forrisco");

        /*
         * destinationFile.delete(); coverPdfFile.delete();
         * finalSummaryPdfFile.delete(); contentFile.delete();
         * 
         * outputDir.delete();
         */

        InputStream inputStream = new FileInputStream(finalPdfFile);

        TempFilesManager.cleanTempDir(outputDir, prefix);

        return inputStream; // capa+sumario+conteudo+paginação
    }

    private void exportBoardReport(File contentFile, File preTextFile, Long planId, List<Long> unitIds, TOCEvent event)
            throws DocumentException, IOException {

        PlanRisk planRisk = this.policyBS.exists(planId, PlanRisk.class);
        List<Unit> planRiskUnits = this.unitBS.listUnitsbyPlanRisk(planRisk).getList();
        if (planRisk == null || GeneralUtils.isEmpty(planRiskUnits)) {
            return;
        }

        List<Unit> units = planRiskUnits
                .stream()
                .filter(u -> unitIds.contains(u.getId()))
                .collect(Collectors.toList());

        generatePreText(preTextFile, planRisk, units);

        Document document = new Document();

        PdfWriterFactory.create(document, contentFile, event);

        reportGeneratorHelper.setDimensions(document);
        document.open();

        Policy policy = this.planriskBS.listPolicybyPlanRisk(planRisk);
        Map<String, RiskLevel> riskLevelsMap = this.policyBS.getRiskLevelsMap(policy);

        List<Risk> risks = new ArrayList<>();
        for (Unit unit : units) {
            risks.addAll(this.riskBS.listRiskByUnit(unit).getList());
        }

        generateRiskMatrixSection(document, policy, riskLevelsMap, risks);

        document.newPage();
        generateRiskQuantitySection(document, riskLevelsMap, risks);
        
        document.newPage();
        generateRiskTypologySection(document, risks);

        document.newPage();
		generateIncidentSection(units, document);
        
        document.newPage();
        generateMonitorSection(units, document, riskLevelsMap);

        document.close();
    }

    private void generatePreText(File preTextFile, PlanRisk planRisk, List<Unit> units)
            throws DocumentException, MalformedURLException, IOException {
        Document document = new Document();

        PdfWriterFactory.create(document, preTextFile);

        reportGeneratorHelper.setDimensions(document);
        document.open();

        document.add(reportGeneratorHelper.newSectionTitle("Plano de Risco:"));
        document.add(reportGeneratorHelper.listItem(planRisk.getName()));
        document.add(reportGeneratorHelper.newSectionTitle("Unidade(s) selecionada(s):"));

        for (Unit unit : units)
            document.add(reportGeneratorHelper.listItem(unit.getName()));

        document.close();
    }

    private void generateRiskMatrixSection(Document document, Policy policy,
            Map<String, RiskLevel> riskLevelsMap, List<Risk> risks) throws DocumentException, IOException {
        String sectionName = "1. Matriz de riscos";
        document.add(reportGeneratorHelper.newSummarySection(sectionName));

        generateRiskMatrixByType(document, policy, riskLevelsMap, risks, "Ameaça", "1.1 Ameaças");
        generateRiskMatrixByType(document, policy, riskLevelsMap, risks, "Oportunidade", "1.2 Oportunidades");
    }

    private void generateRiskMatrixByType(Document document, Policy policy,
            Map<String, RiskLevel> riskLevelsMap, List<Risk> risks, String riskType, String sectionTitle)
            throws DocumentException, IOException {
        document.add(reportGeneratorHelper.newSummarySection(sectionTitle));

        String[][] matrixLevels = policy.getMatrixLevels();

        MatrixCell[][] cells = new MatrixCell[matrixLevels.length][];
        for (int i = 0; i < matrixLevels.length; i++) {
            int rowLength = matrixLevels[i].length;
            cells[i] = new MatrixCell[rowLength];
            boolean isLastLine = i == matrixLevels.length - 1;
            for (int j = 0; j < rowLength; j++) {
                boolean isFirstColumn = j == 0;
                RiskLevel riskLevel = riskLevelsMap.get(matrixLevels[i][j]);
                String value;
                if (isLastLine || isFirstColumn) {
                    value = matrixLevels[i][j];
                } else {
                    String probability = matrixLevels[i][0];
                    String impact = matrixLevels[matrixLevels.length - 1][j];
                    value = countRisksOccurrences(risks, riskType, probability, impact);
                }

                String colorCode = null;
                if (riskLevel != null) {
                    colorCode = RiskMatrixColor.getCodeById(riskLevel.getColor());
                }
                cells[i][j] = new MatrixCell(value, colorCode);
            }
        }

        PdfPTable table = new MatrixGenerator(cells).generate();
        document.add(table);
    }

    private String countRisksOccurrences(List<Risk> risks, String riskType, String probability, String impact) {
        int count = 0;
        for (Risk risk : risks) {
            if (risk.getProbability().equals(probability)
                    && risk.getImpact().equals(impact) && risk.getType().equals(riskType)) {
                count += 1;
            }
        }
        return String.valueOf(count);
    }
    
    private void generateRiskTypologySection(Document document, List<Risk> risks) throws DocumentException {
        final String sectionName = "3. Tipologias de Risco";
        document.add(reportGeneratorHelper.newSummarySection(sectionName));

        Map<String, Map<RiskTypologyType, Counter>> riskTipologiesCount = countRiskTipologies(risks);

        generateRiskTypologyByType(document, riskTipologiesCount, "Ameaça", "3.1 Ameaças");
        document.newPage();
        generateRiskTypologyByType(document, riskTipologiesCount, "Oportunidade", "3.2 Oportunidades");
    }
    
    private void generateRiskTypologyByType(Document document, Map<String, Map<RiskTypologyType, Counter>> riskTipologiesCount,
        String riskType, String sectionName) throws DocumentException {
        document.add(reportGeneratorHelper.newSummarySection(sectionName));
        
        final int columnsPerLine = 3;
        PdfPTable table = new PdfPTable(columnsPerLine);
        table.setTotalWidth(420);
        table.setLockedWidth(true);
        
        Map<RiskTypologyType, Counter> riskTypeCount = Optional
            .ofNullable(riskTipologiesCount.get(riskType))
            .orElse(new TreeMap<>());
        
        for (RiskTypologyType riskTypology : RiskTypologyType.values()) {
            PdfPCell cell = new PdfPCell();
            cell.setFixedHeight(200);
            cell.setPaddingBottom(40);
            cell.setBorder(0);

            BaseColor color = WebColors.getRGBColor(riskTypology.getColorHex());
            CellBackground cellBackground = new CellBackground(color);
            cell.setCellEvent(cellBackground);

            Counter counter = Optional.ofNullable(riskTypeCount.get(riskTypology)).orElse(new Counter()) ;
            Paragraph quantity = new Paragraph(String.valueOf(counter.getCounter()), PDFSettings.CARD_FONT);
            String label = riskTypology.getValue().replace("/", " /\n");
            Paragraph typology = new Paragraph(label, PDFSettings.RISK_FONT);
            quantity.setAlignment(Element.ALIGN_CENTER);
            typology.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(quantity);
            cell.addElement(typology);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.addCell(cell);
        }

        document.add(table);
    }

    private void generateRiskQuantitySection(Document document, Map<String, RiskLevel> riskLevelsMap,
            List<Risk> risks) throws DocumentException {
        final String sectionName = "2. Quantidade de riscos";
        document.add(reportGeneratorHelper.newSummarySection(sectionName));

        generateRiskQuantityByType(document, riskLevelsMap, risks, "Ameaça", "2.1 Ameaças");
        generateRiskQuantityByType(document, riskLevelsMap, risks, "Oportunidade", "2.2 Oportunidades");
    }

    private void generateRiskQuantityByType(Document document, Map<String, RiskLevel> riskLevelsMap,
            List<Risk> risks, String riskType, String sectionTitle) throws DocumentException {
        document.add(reportGeneratorHelper.newSummarySection(sectionTitle));

        final int columnsPerLine = 3;
        List<RiskLevel> riskLevels = new ArrayList<>(riskLevelsMap.values());
        riskLevels.sort((r1, r2) -> r1.getId().compareTo(r2.getId()));
        PdfPTable table = new PdfPTable(columnsPerLine);
        table.setTotalWidth(330);
        table.setLockedWidth(true);

		final int columnsPerLineOver = riskLevels.size() <= columnsPerLine ?
				columnsPerLine : columnsPerLine * 2;  
		
		for (int i = riskLevels.size(); i < columnsPerLineOver; i++) {
			riskLevels.add(null);
		}

        for (RiskLevel riskLevel : riskLevels) {
            PdfPCell cell = new PdfPCell();
            cell.setFixedHeight(200);
            cell.setPaddingBottom(40);
            cell.setBorder(0);

            if (riskLevel != null) {
                BaseColor color = WebColors.getRGBColor(RiskMatrixColor.getCodeById(riskLevel.getColor()));
                CellBackground cellBackground = new CellBackground(color);
                cell.setCellEvent(cellBackground);

                String value = countRiskQuantity(risks, riskType, riskLevel);
                Paragraph quantity = new Paragraph(value, PDFSettings.CARD_FONT);
                Paragraph level = new Paragraph(riskLevel.getLevel().toUpperCase(), PDFSettings.RISK_FONT);
                quantity.setAlignment(Element.ALIGN_CENTER);
                level.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(quantity);
                cell.addElement(level);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            } else {
                cell.setBackgroundColor(BaseColor.WHITE);
            }

            table.addCell(cell);
        }

        document.add(table);
    }

    private String countRiskQuantity(List<Risk> risks, String riskType, RiskLevel riskLevel) {
        int count = 0;
        for (Risk risk : risks) {
            if (risk.getRiskLevel().equals(riskLevel) && risk.getType().equals(riskType)) {
                count += 1;
            }
        }
        return String.valueOf(count);
    }

    private void generateIncidentSection(List<Unit> units, Document document)
            throws BadElementException, MalformedURLException, IOException, DocumentException {
        String sectionName = "4. Incidentes";
        document.add(reportGeneratorHelper.newSummarySection(sectionName));

        Date dateLimit = getDateLimitToListIncidents();

        List<Incident> incidents = incidentBS.listIncidentsByUnitsAndDateLimit(units, dateLimit);

        DatasetBuilder datasetBuilder = new DatasetBuilder();

        addIncidentsToDatasetByType(incidents, IncidentType.AMEACA, datasetBuilder);
        addIncidentsToDatasetByType(incidents, IncidentType.OPORTUNIDADE, datasetBuilder);

        LineChartGenerator lineChartGenerator = new LineChartGenerator(datasetBuilder.create(), "Tempo", "Quantidade");
        Image chart = lineChartGenerator.generate();
        chart.setAlignment(Element.ALIGN_CENTER);
        document.add(chart);
    }

    private Date getDateLimitToListIncidents() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -1);
        return calendar.getTime();
    }

    private void addIncidentsToDatasetByType(List<Incident> incidents, IncidentType type,
            DatasetBuilder datasetBuilder) {
        Map<Integer, Counter> incidentCounterByMonth = generateEmptyIncidentCounterMap();

        Calendar calendar = Calendar.getInstance();
        for (Incident incident : incidents) {
            if (incident.getTypeEnum().equals(type)) {
                calendar.setTime(incident.getBegin());
                int month = calendar.get(Calendar.MONTH) + 1;
                Counter counter = incidentCounterByMonth.get(month);
                if (counter != null) {
                    counter.increment();
                }
            }
        }

        datasetBuilder.newSerie(type.getLabel() + "s");

        for (Entry<Integer, Counter> entry : incidentCounterByMonth.entrySet()) {
            String x = DateUtil.getMonthNameAbbreviated(entry.getKey());
            Number y = entry.getValue().getCounter();
            datasetBuilder.add(x, y);
        }
    }

    private Map<Integer, Counter> generateEmptyIncidentCounterMap() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Map<Integer, Counter> incidentCounterByMonth = new HashMap<>();
        for (int i = 0; i < 12; i++) {
            int month = calendar.get(Calendar.MONTH) + 1;
            incidentCounterByMonth.put(month, new Counter());
            if (i < 11) {
                calendar.add(Calendar.MONTH, -1);
            }
        }
        return incidentCounterByMonth;
    }

    private void generateMonitorSection(List<Unit> units, Document document,
            Map<String, RiskLevel> riskLevelsMap) throws DocumentException {
        String sectionName = "5. Monitoramento";
        document.add(reportGeneratorHelper.newSummarySection(sectionName));

        Map<MonitoringState, Counter> monitorSummaryMap = getMonitorHistoryMapped(units);

        Counter total = monitorSummaryMap.values()
                .stream()
                .reduce(new Counter(), (current, value) -> {
                    current.add(value.getCounter());
                    return current;
                });

        PdfPTable table = new PdfPTable(3);
        table.setTotalWidth(330);
        table.setLockedWidth(true);

        Font mainFont = FontFactory.getFont(FontFactory.HELVETICA, 48.0f, BaseColor.WHITE);
        Font secondaryFont = FontFactory.getFont(FontFactory.HELVETICA, 12.0f, BaseColor.WHITE);
        for (MonitoringState estado : monitorSummaryMap.keySet()) {
            Counter counter = monitorSummaryMap.get(estado);
            String percentValue = total.getCounter() > 0
                    ? String.format(
                            "(%.2f",
                            ((float) counter.getCounter() / total.getCounter()) * 100)
                    : "(0";
            percentValue += "%)";
            percentValue = percentValue.replace(',', '.');

            PdfPCell cell = new PdfPCell();
            cell.setPaddingTop(60);
            cell.setFixedHeight(200);
            cell.setBorder(0);
            CellBackground cellBackground = new CellBackground(WebColors.getRGBColor(estado.getColorHex()));
            cell.setCellEvent(cellBackground);

            Paragraph quantity = new Paragraph(counter.toString(), mainFont);
            quantity.setAlignment(Element.ALIGN_CENTER);
            quantity.setLeading(25);
            Paragraph percent = new Paragraph(percentValue, secondaryFont);
            percent.setAlignment(Element.ALIGN_CENTER);
            percent.setLeading(25);
            Paragraph status = new Paragraph(StringUtils.capitalize(estado.getReportLabel()), secondaryFont);
            status.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(quantity);
            cell.addElement(percent);
            cell.addElement(status);

            table.addCell(cell);
        }

        document.add(table);
    }

    private Map<MonitoringState, Counter> getMonitorHistoryMapped(List<Unit> units) {
        List<MonitorHistory> history = monitorBS.listLastMonitorHistoryByUnits(units);
        Map<MonitoringState, Counter> monitorHistoryMap = new TreeMap<>();
        for (MonitorHistory monitorHistory : history) {
            MonitoringState estado = MonitoringState.getMonitoringStateByValue(monitorHistory.getEstado());
            Counter counter = monitorHistoryMap.get(estado);
            if (counter == null) {
                counter = new Counter();
                monitorHistoryMap.put(estado, counter);
            }
            counter.add(monitorHistory.getQuantity());
        }

        return monitorHistoryMap;
    }
    
    private Map<String, Map<RiskTypologyType, Counter>> countRiskTipologies(List<Risk> risks) {
        Map<String, Map<RiskTypologyType, Counter>> riskTypeTypologyCount = new TreeMap<>();
        for (Risk risk : risks) {
            String riskType = risk.getType();
            Map<RiskTypologyType, Counter> typologyCount = riskTypeTypologyCount.get(riskType);
            
            if (typologyCount == null) {
                typologyCount = new TreeMap<>();
                riskTypeTypologyCount.put(riskType, typologyCount);
            }
            
            String riskTypologyString = risk.getTipology();
            List<RiskTypologyType> riskTipologies = RiskTypologyType.getRiskTypologyTypesByValue(riskTypologyString);
        
            for (RiskTypologyType typology : riskTipologies) {
                if(typology == null) {
                    continue;
                }
                
                Counter counter = typologyCount.get(typology);
                if (counter == null) {
                    counter = new Counter();
                    typologyCount.put(typology, counter);
                }
                counter.increment();
            }
        }
        
        return riskTypeTypologyCount;
    }

    public static class Params implements ReportGeneratorParams {
        public String title;
        public Long planId;
        public String selecao;

        public Params(String title, Long planId, String selecao) {
            super();
            this.title = title;
            this.planId = planId;
            this.selecao = selecao;
        }
    }
}
