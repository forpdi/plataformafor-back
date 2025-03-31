package org.forpdi.system.reports.frisco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.utils.Counter;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.system.reports.ReportGenerator;
import org.forpdi.system.reports.ReportGeneratorHelper;
import org.forpdi.system.reports.ReportGeneratorParams;
import org.forpdi.system.reports.pdf.PDFSettings;
import org.forpdi.system.reports.pdf.PdfWriterFactory;
import org.forpdi.system.reports.pdf.TOCEvent;
import org.forpdi.system.reports.pdf.table.PDFTableGenerator;
import org.forpdi.system.reports.pdf.table.PDFTableGeneratorBuilder;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.forrisco.core.item.FieldItem;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.plan.PlanRiskBS;
import org.forrisco.core.process.Process;
import org.forrisco.core.process.ProcessBS;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskFilterParams;
import org.forrisco.risk.contingency.Contingency;
import org.forrisco.risk.contingency.ContingencyBS;
import org.forrisco.risk.incident.Incident;
import org.forrisco.risk.incident.IncidentBS;
import org.forrisco.risk.monitor.Monitor;
import org.forrisco.risk.monitor.MonitorBS;
import org.forrisco.risk.preventiveaction.PreventiveAction;
import org.forrisco.risk.preventiveaction.PreventiveActionBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;


@Component
public class UnitReportGenerator implements ReportGenerator {

    @Autowired
    private UnitBS unitBS;
    @Autowired
    private ProcessBS processBS;
    @Autowired
    private ReportGeneratorHelper reportGeneratorHelper;
    @Autowired
    private RiskBS riskBS;
    @Autowired
    private CompanyDomainContext domain;
    @Autowired 
    private PlanRiskBS planRiskBS;
    
	@Autowired
	private ContingencyBS contingencyBS;
	@Autowired
	private IncidentBS incidentBS;
	@Autowired
	private MonitorBS monitorBS;
	@Autowired
	private PreventiveActionBS preventiveActionBS;

    @Override
    public InputStream generateReport(ReportGeneratorParams params) {
        try {
            Params parsedParams = ReportGeneratorParams.extractParams(params, Params.class);
            Integer selectedYear = parsedParams.selectedYear;
            RiskFilterParams riskFilters = parsedParams.riskFilters;
        	if (selectedYear != -1) {
        		try {
            		Date startCreation = GeneralUtils.DATETIME_FORMAT.parse("01/01/" + selectedYear + " 00:00:00");
            		Date endCreation = GeneralUtils.DATETIME_FORMAT.parse("31/12/" + selectedYear + " 23:59:59");
            		riskFilters.setStartCreation(startCreation);
            		riskFilters.setEndCreation(endCreation);
        		} catch (ParseException e) {
        			throw new IllegalArgumentException("Ano inválido: " + selectedYear);
        		}
        	}

            
            return exportUnitReport(parsedParams.planRiskId, parsedParams.title, parsedParams.units,
            		parsedParams.subunits, riskFilters, selectedYear);
        } catch (IOException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream exportUnitReport(Long planRiskId, String title, String units, String subunits,
    		RiskFilterParams riskFilters, Integer selectedYear) throws IOException, DocumentException {

        File outputDir = TempFilesManager.getTempDir();

        final String prefix = String.format("frisco-unit-report-export-%d", System.currentTimeMillis());

        File finalSummaryPdfFile = new File(outputDir, String.format("%s-final-summary.pdf", prefix));
        File destinationFile = new File(outputDir, String.format("%s-mounted.pdf", prefix));
        File finalPdfFile = new File(outputDir, String.format("%s-final.pdf", prefix));
        File coverPdfFile = new File(outputDir, String.format("%s-cover.pdf", prefix));
        File contentFile = new File(outputDir, String.format("%s-content.pdf", prefix));

        PlanRisk planRisk = planRiskBS.exists(planRiskId, PlanRisk.class);
        String planRiskName = planRisk.getName();
        String policyName = planRisk.getPolicy().getName();
        
        Map<String, String> coverTextContent = new LinkedHashMap<>();
        coverTextContent.put("Política vinculada:", policyName);
        coverTextContent.put("Plano de gestão de risco:", planRiskName);
        reportGeneratorHelper.generateCover(coverPdfFile, title, coverTextContent);

        TOCEvent event = new TOCEvent();
        PdfReader cover = new PdfReader(coverPdfFile.getPath());

        generateUnitContent(contentFile, units, subunits, riskFilters, selectedYear, event);

        int summaryCountPages = reportGeneratorHelper.generateSummary(finalSummaryPdfFile, event,
                cover.getNumberOfPages());

        Document newDocument = new Document();
        PdfCopy combinedContent = new PdfCopy(newDocument, new FileOutputStream(destinationFile.getPath()));

        newDocument.open();
        newDocument.addTitle(title);

        PdfReader summary = new PdfReader(finalSummaryPdfFile.getPath());
        PdfReader content = new PdfReader(contentFile.getPath());

        reportGeneratorHelper.copyDocumentContent(combinedContent, cover);
        reportGeneratorHelper.copyDocumentContent(combinedContent, summary);
        reportGeneratorHelper.copyDocumentContent(combinedContent, content);

        cover.close();
        summary.close();
        newDocument.close();

        reportGeneratorHelper
                .manipulatePdf(destinationFile.getPath(), finalPdfFile.getPath(), summaryCountPages, "forrisco");

        InputStream inputStream = new FileInputStream(finalPdfFile);

        TempFilesManager.cleanTempDir(outputDir, prefix);

        return inputStream; // capa+sumario+conteudo+paginação
    }

    private void generateUnitContent(File contentFile, String units, String subunits, RiskFilterParams riskFilters,
    		Integer selectedYear, TOCEvent event) throws DocumentException, IOException, MalformedURLException {

        Document document = new Document();
        PdfWriterFactory.create(document, contentFile, event);
        File outputDir = contentFile.getParentFile();

        String[] selectedUnits = null;
        if (units != null)
            selectedUnits = units.split(",");

        String[] selectedSubunits = null;
        if (subunits != null)
            selectedSubunits = subunits.split(",");

        Counter sectionIndex = new Counter();
        Counter subSectionIndex = new Counter();

        reportGeneratorHelper.setDimensions(document);
        document.open();

        // para cada unidade selecionada
        if (selectedUnits != null) {
            for (int i = 0; i < selectedUnits.length; i++) {
                Unit unit = this.unitBS.retrieveUnitById(Long.parseLong(selectedUnits[i]));
                if (unit == null)
                    continue;

                List<Unit> allSubunits = this.unitBS.listSubunitByUnit(unit).getList();
                List<Unit> selectedSubUnits = getSelectedSubunits(allSubunits, selectedSubunits);

                sectionIndex.increment();
                subSectionIndex.increment();

                document.add(reportGeneratorHelper.newSummarySection(sectionIndex + ". " + unit.getName()));
                generateInfo(document, unit, sectionIndex.toString());

                generateProcessesTable(document, unit, sectionIndex.toString());

                generateRisks(document, unit, riskFilters, selectedYear, sectionIndex.toString() + "." + subSectionIndex.toString());

                subSectionIndex.increment();

                if (selectedSubUnits.size() > 0) {
                    document.add(reportGeneratorHelper.newSummarySection(sectionIndex.toString() + "." + subSectionIndex.toString() + ". Subunidade(s)"));

                    Counter subUnitSectionIndex = new Counter();
                    subUnitSectionIndex.increment();
                    
                    for (Unit subunit : selectedSubUnits) {
                    	document.add(reportGeneratorHelper.newSummarySection(
                    			sectionIndex.toString() + "." + subSectionIndex.toString()+ "." + subUnitSectionIndex.toString() + ". " + subunit.getName()));

                        List<FieldItem> sublist = new ArrayList<FieldItem>();

                        generateInfo(document, subunit, sectionIndex.toString() + "." + subSectionIndex.toString());

                        for (FieldItem fieldsub : sublist) {
                            if (!GeneralUtils.isEmpty(fieldsub.getDescription())) {
                                Paragraph attTitle = new Paragraph(fieldsub.getName(), PDFSettings.TITLE_FONT);
                                attTitle.setLeading(PDFSettings.INTER_LINE_SPACING);
                                attTitle.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
                                attTitle.setSpacingBefore(PDFSettings.PARAGRAPH_SPACING);
                                document.add(attTitle);

                                Paragraph attDescription = new Paragraph(fieldsub.getDescription(),
                                        PDFSettings.TEXT_FONT);
                                attDescription.setFirstLineIndent(PDFSettings.FIRST_LINE_IDENT);
                                document.add(attDescription);
                            }
                        }
                        generateProcessesTable(document, subunit,
                                sectionIndex.toString() + "." + subSectionIndex.toString());

                        String riskIndex = sectionIndex.toString() + "." + subSectionIndex.toString() + "." + subUnitSectionIndex.toString() + ".1";
                        generateRisks(document, subunit, riskFilters, selectedYear, riskIndex);
                        subUnitSectionIndex.increment();
                    }
                }
                subSectionIndex = new Counter();
            }
        }
        
        if (GeneralUtils.isEmpty(units)) {
        	document.add(new Paragraph("Sem conteúdo"));
        }

        document.close();
    }
    
    private List<Unit> getSelectedSubunits(List<Unit> allSubunits, String[] selectedList) {
        List<Unit> selectedSubUnits = new ArrayList<Unit>();

        for (Unit subunit : allSubunits) {
            if (selectedList != null && allSubunits.size() > 0) {
                for (int j = 0; j < selectedList.length; j++) {
                    if (subunit.getId().equals(Long.parseLong(selectedList[j]))) {
                        selectedSubUnits.add(subunit);
                    }
                }
            }
        }

        return selectedSubUnits;
    }

    private void generateInfo(Document document, Unit unit, String secIndex)
            throws DocumentException, FileNotFoundException, IOException {

        document.add(reportGeneratorHelper.attributeDisplay("Nome da unidade", unit.getName()));
        document.add(reportGeneratorHelper.attributeDisplay("Responsável", unit.getUser().getName()));
        document.add(reportGeneratorHelper.attributeDisplay("Sigla", unit.getAbbreviation()));
        document.add(reportGeneratorHelper.richTextAttributeDisplay("Descrição", unit.getDescription(), document, PDFSettings.TITLE_FONT));
    }

    private void generateProcessesTable(Document document, Unit unit, String sectionIndex) throws DocumentException {
        List<Process> processes = this.processBS.listProcessByUnit(unit).getList();

        if (!processes.isEmpty()) {
        	document.add(reportGeneratorHelper.paragraphSpacing());
            PDFTableGenerator<Process> tableGenerator = new PDFTableGeneratorBuilder<Process>()
                    .withTitle("Processos da Unidade")
                    .withRelativeColumnWidths(1, 1, 1, 1)
                    .withItems(processes)
                    .addColmn()
                    .withColumnTitle("Processo")
                    .withContentExtractor(process -> process.getName())
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Objetivo")
                    .withContentExtractor(process -> process.getProcessObjectivesDescriptionsString())
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Unidade(s) relacionada(s)")
                    .withContentExtractor(process -> String.join(",\n", process.getRelatedUnitNames()))
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Anexo")
                    .withFormattedContentExtractor(process -> {
                        Chunk chunk;
                        if (process.getFile() != null) {
                            chunk = new Chunk(
                                    process.getFile().getName(),
                                    new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL,
                                            WebColors.getRGBColor("#0085D9")));
                            chunk.setAnchor(Util.getDownloadFilesURL(process.getFileLink(), domain.get().getBaseUrl()));
                        } else {
                            chunk = new Chunk();
                        }
                        return chunk;
                    })
                    .createColumn()
                    .create();

            PdfPTable table = tableGenerator.generate();
            document.add(table);
        } else
            document.add(reportGeneratorHelper.listItemBold("Unidade não possui processos cadastrados."));
    }

    private void generateRisks(Document document, Unit unit, RiskFilterParams riskFilters, Integer selectedYear,
    		String sectionIndex) throws DocumentException, FileNotFoundException, IOException {

    	Long planRiskId = unit.getPlanRisk().getId();
    	riskFilters.setUnitIds(Arrays.asList(unit.getId()));;
    	
    	List<Risk> riskList = riskBS.filterFullRisks(planRiskId, riskFilters);
    	
        document.add(reportGeneratorHelper.paragraphSpacing());
        document.add(reportGeneratorHelper.newSummarySection(sectionIndex + ". Risco(s)"));

        if (riskList.size() > 0) {
        	Counter riskIndex = new Counter();
        	riskIndex.increment();
            for (Risk risk : riskList) {
            	document.add(reportGeneratorHelper.newSummarySection(sectionIndex + "." + riskIndex.toString() + ". " + risk.getName()));
            	riskIndex.increment();

                generateRiskInfo(document, risk);
                document.add(reportGeneratorHelper.paragraphSpacing());

                generateRiskPreventiveActions(document, risk, selectedYear);

                generateRiskMonitors(document, risk, selectedYear);

                generateRiskIncidents(document, risk, selectedYear);

                generateRiskContingencies(document, risk, selectedYear);
            }
        } else
            document.add(reportGeneratorHelper.listItemWithoutBullet("Unidade não possui riscos cadastrados."));

        document.newPage();
    }

    private void generateRiskInfo(Document document, Risk risk) throws DocumentException, FileNotFoundException, IOException {
    	final String reasonDescription = "Causa(s)";
    	final String resultDescription = "Consequência(s)";
        String[] attributeDescription = {
        		"Código de identificação do risco",
        		"Responsável",
        		"Gestor",
                "Causa(s)",
                "Consequência(s)",
                "Probabilidade",
                "Impacto",
                "Grau de risco",
                "Periodicidade do monitoramento",
                "Tipologia",
                "Resposta ao Risco",
                "Nível Organizacional",
                "Data e hora da criação do risco",
                "Objetivo(s) estratégico(s) do PDI vinculado(s)",
                "Objetivo(s) do(s) processo(s) vinculado(s)",
                "Atividade(s) do(s) processo(s) vinculado(s)",
                "Atividade(s) do(s) processo(s) vinculado(s)" };

        String[] attributeValue = { 
            risk.getCode().toString(), 
            risk.getUser().getName(),
            risk.getManagerNameIfExists(),
            risk.getReason(), 
            risk.getResult(), 
            risk.getProbability().toString(),
            risk.getImpact().toString(), 
            risk.getRiskLevel().getLevel(), 
            risk.getPeriodicity(),
            risk.getFormattedTipologies(),
            risk.getRiskResponseLabel(),
            risk.getRiskLevelLabel(),
            GeneralUtils.DATETIME_FORMAT.format(risk.getBegin()),
            String.join("\n", risk.getStrategiesDescriptions()),
            String.join("\n", risk.getActivitiesDescriptions())};

        for (int i = 0; i < attributeValue.length; i++) {
            if (!attributeValue[i].isEmpty())
            	if (attributeDescription[i].equals(reasonDescription) || attributeDescription[i].equals(resultDescription)) {
            		var element = reportGeneratorHelper.richTextAttributeDisplay(
            				attributeDescription[i], attributeValue[i], document, PDFSettings.TITLE_FONT);
            		document.add(element);
            	} else {
                    document.add(reportGeneratorHelper.attributeDisplay(attributeDescription[i], attributeValue[i]));
            	}
        }
    }

    private void generateRiskPreventiveActions(Document document, Risk risk, Integer selectedYear) throws DocumentException {
        PaginatedList<PreventiveAction> paginatedList = preventiveActionBS.listActionByRisk(risk, selectedYear);
        List<PreventiveAction> preventiveActions = paginatedList.getList(); 
        if (!preventiveActions.isEmpty()) {
            PDFTableGenerator<PreventiveAction> tableGenerator = new PDFTableGeneratorBuilder<PreventiveAction>()
                    .withTitle("Ações de prevenção")
                    .withRelativeColumnWidths(2, 2, 1)
                    .withItems(preventiveActions)
                    .addColmn()
                    .withColumnTitle("Ação")
                    .withContentExtractor(preventAction -> preventAction.getAction())
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Responsável")
                    .withContentExtractor(preventAction -> preventAction.getUser().getName())
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Ação realizada?")
                    .withContentExtractor(preventAction -> preventAction.isAccomplishedDescription())
                    .createColumn()
                    .create();

            PdfPTable table = tableGenerator.generate();
            table.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
            document.add(table);
        }
    }

    private void generateRiskMonitors(Document document, Risk risk, Integer selectedYear) throws DocumentException {
        PaginatedList<Monitor> paginatedList = monitorBS.listMonitorByRisk(risk, selectedYear);
        List<Monitor> monitors = paginatedList.getList();
        if (!monitors.isEmpty()) {
            PDFTableGenerator<Monitor> tableGenerator = new PDFTableGeneratorBuilder<Monitor>()
                    .withTitle("Histórico de monitoramentos")
                    .withRelativeColumnWidths(2, 1, 1, 1, 1)
                    .withItems(monitors)
                    .addColmn()
                    .withColumnTitle("Parecer")
                    .withContentExtractor(monitor -> Util.htmlToText(monitor.getReport()))
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Probabilidade")
                    .withContentExtractor(monitor -> monitor.getProbability())
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Impacto")
                    .withContentExtractor(monitor -> monitor.getImpact())
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Responsável")
                    .withContentExtractor(monitor -> monitor.getUser().getName())
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Data e horário")
                    .withContentExtractor(monitor -> GeneralUtils.DATETIME_FORMAT.format(monitor.getBegin()))
                    .createColumn()
                    .create();

            PdfPTable table = tableGenerator.generate();
            table.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
            document.add(table);
        }
    }

    private void generateRiskIncidents(Document document, Risk risk, Integer selectedYear) throws DocumentException {
        PaginatedList<Incident> paginatedList = incidentBS.listIncidentsByRisk(risk, selectedYear);
        List<Incident> incidents = paginatedList.getList();
        if (!incidents.isEmpty()) {
            PDFTableGenerator<Incident> tableGenerator = new PDFTableGeneratorBuilder<Incident>()
                    .withTitle("Histórico de incidentes")
                    .withRelativeColumnWidths(3, 3, 2.5F, 3, 2)
                    .withItems(incidents)
                    .addColmn()
                    .withColumnTitle("Descrição")
                    .withContentExtractor(incident -> Util.htmlToText(incident.getDescription()))
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Ações corretivas")
                    .withContentExtractor(incident -> incident.getAction())
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Tipo")
                    .withContentExtractor(incident -> incident.getTypeEnum().getLabel())
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Responsável")
                    .withContentExtractor(incident -> incident.getUser().getName())
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Data e hora")
                    .withContentExtractor(incident -> GeneralUtils.DATETIME_FORMAT.format(incident.getBegin()))
                    .createColumn()
                    .create();

            PdfPTable table = tableGenerator.generate();
            table.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
            document.add(table);
        }
    }

    private void generateRiskContingencies(Document document, Risk risk, Integer selectedYear) throws DocumentException {
        PaginatedList<Contingency> paginatedList = contingencyBS.listContingenciesByRisk(risk, selectedYear);
        List<Contingency> contingencies = paginatedList.getList();
        if (!contingencies.isEmpty()) {
            PDFTableGenerator<Contingency> tableGenerator = new PDFTableGeneratorBuilder<Contingency>()
                    .withTitle("Ações de contingenciamento")
                    .withRelativeColumnWidths(3, 2)
                    .withItems(contingencies)
                    .addColmn()
                    .withColumnTitle("Ação")
                    .withContentExtractor(contingency -> contingency.getAction())
                    .createColumn()
                    .addColmn()
                    .withColumnTitle("Responsável")
                    .withContentExtractor(contingency -> contingency.getUser().getName())
                    .createColumn()
                    .create();
            PdfPTable table = tableGenerator.generate();
            table.setSpacingAfter(PDFSettings.PARAGRAPH_SPACING);
            document.add(table);
        }
    }

    public static class Params implements ReportGeneratorParams {
    	public Long planRiskId;
        public String title;
        public String units;
        public String subunits;
        public Integer selectedYear;
        public RiskFilterParams riskFilters;

        public Params(Long planRiskId, String title, String units, String subunits,
        		Integer selectedYear, RiskFilterParams riskFilters) {
        	this.planRiskId = planRiskId;
        	this.title = title;
            this.units = units;
            this.subunits = subunits;
            this.selectedYear = selectedYear;
            this.riskFilters = riskFilters;
        }
    }
}
