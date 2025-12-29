package org.forpdi.system.reports.frisco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.forpdi.core.common.GeneralUtils;
import org.forpdi.system.reports.ReportGenerator;
import org.forpdi.system.reports.ReportGeneratorHelper;
import org.forpdi.system.reports.ReportGeneratorParams;
import org.forpdi.system.reports.pdf.PDFSettings;
import org.forpdi.system.reports.pdf.PdfWriterFactory;
import org.forpdi.system.reports.pdf.TOCEvent;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.forrisco.core.item.PlanRiskItem;
import org.forrisco.core.item.PlanRiskItemBS;
import org.forrisco.core.item.PlanRiskItemField;
import org.forrisco.core.item.PlanRiskSubItem;
import org.forrisco.core.item.PlanRiskSubItemField;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.plan.PlanRiskBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;


@Component
public class PlanRiskReportGenerator implements ReportGenerator {

    @Autowired
    private PlanRiskBS planriskBS;
    @Autowired
    private PlanRiskItemBS planRiskItemBS;
    @Autowired
    private ReportGeneratorHelper reportGeneratorHelper;

    @Override
    public InputStream generateReport(ReportGeneratorParams params) {
        try {
            Params parsedParams = ReportGeneratorParams.extractParams(params, Params.class);
            return exportPlanRiskReport(
                    parsedParams.title, parsedParams.planId, parsedParams.items,
                    parsedParams.subitems);
        } catch (IOException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream exportPlanRiskReport(String title, Long planId, String items, String subitems)
            throws IOException, DocumentException {
        File outputDir = TempFilesManager.getTempDir();

        final String prefix = String.format("frisco-report-export-%d", System.currentTimeMillis());

        File finalSummaryPdfFile = new File(outputDir, String.format("%s-final-summary.pdf", prefix));
        File destinationFile = new File(outputDir, String.format("%s-mounted.pdf", prefix));
        File finalPdfFile = new File(outputDir, String.format("%s-final.pdf", prefix));
        File coverPdfFile = new File(outputDir, String.format("%s-cover.pdf", prefix));
        File contentFile = new File(outputDir, String.format("%s-content.pdf", prefix));

        Map<String, String> coverTextContent = new HashMap<>();
        coverTextContent.put("Relatório de um plano de risco", "");
        reportGeneratorHelper.generateCover(coverPdfFile, title, coverTextContent);

        TOCEvent event = new TOCEvent();
        PdfReader cover = new PdfReader(coverPdfFile.getPath());

        generatePlanRiskContent(contentFile, planId, items, subitems, event);

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

        newDocument.close();

        reportGeneratorHelper
                .manipulatePdf(destinationFile.getPath(), finalPdfFile.getPath(), summaryCountPages, "forrisco");

        InputStream inputStream = new FileInputStream(finalPdfFile);

        TempFilesManager.cleanTempDir(outputDir, prefix);

        return inputStream; // capa+sumario+conteudo+paginação
    }

    private void generatePlanRiskContent(File contentFile, Long planId, String items, String subitems, TOCEvent event)
            throws DocumentException, IOException, MalformedURLException {
        Document document = new Document();
        PdfWriterFactory.create(document, contentFile, event);
        File outputDir = contentFile.getParentFile();
        int chapterIndex = 1;

        String[] sections = {};
        if (items != null)
            sections = items.split(",");

        String[] subsections = {};
        if (subitems != null)
            subsections = subitems.split(",");

        reportGeneratorHelper.setDimensions(document);
        document.open();

        PlanRisk plan = this.planriskBS.exists(planId, PlanRisk.class);

        if (Arrays.stream(sections).anyMatch("0"::equals)) {
        	generatePlanRiskInformation(document, plan);
        	chapterIndex++;
        }

        document.newPage();
        document.add(reportGeneratorHelper.newSummarySection(String.format("%d. Items", chapterIndex)));

        generatePlanRiskItems(document, sections, subsections, chapterIndex);

        document.close();
    }

    public void generatePlanRiskInformation(Document document, PlanRisk plan)
            throws DocumentException, FileNotFoundException, IOException {
        document.add(reportGeneratorHelper.newSummarySection("1. Plano de risco"));
        document.add(reportGeneratorHelper.newSummarySection("1.1 Informações Gerais"));
        document.add(reportGeneratorHelper.richTextAttributeDisplay("Descrição", plan.getDescription(), document, PDFSettings.TITLE_FONT));

        if (plan.getValidityBegin() != null && plan.getValidityEnd() != null)
            document.add(reportGeneratorHelper.attributeDisplay("Prazo de vigência",
                    String.format("%s à %s", GeneralUtils.DATE_FORMAT.format(plan.getValidityBegin()),
                            GeneralUtils.DATE_FORMAT.format(plan.getValidityEnd()))));

        document.add(reportGeneratorHelper.attributeDisplay("Política Vinculada", plan.getPolicy().getName()));
    }

    private void generatePlanRiskItems(Document document, String[] sections, String[] subsections, int chapterIndex)
            throws DocumentException, IOException {
        int sectionIndex = 0;
        int subSecIndex = 0;

        if (sections != null) {
            for (int i = 0; i < sections.length; i++) {                
                PlanRiskItem item = this.planRiskItemBS.retrievePlanRiskItembyId(Long.parseLong(sections[i]));
                if (item == null)
                    continue;

                subSecIndex = 0;

                List<PlanRiskItemField> itemFields = this.planRiskItemBS.listFieldsByPlanRiskItem(item).getList();
                List<PlanRiskSubItem> subItems = this.planRiskItemBS.listSubItemByItem(item, null).getList();
                List<PlanRiskSubItem> selectedSubItems = getSelectedSubitems(subItems, subsections);

                String sectionName = String.format("%d.%d %s", chapterIndex, ++sectionIndex, item.getName());
                document.add(reportGeneratorHelper.newSummarySection(sectionName));

                showItemFields(itemFields, document);

                for (PlanRiskSubItem subitem : selectedSubItems) {
                    String subSectionName = String.format("%d.%d.%d %s", chapterIndex, sectionIndex, ++subSecIndex, subitem.getName());
                    document.add(reportGeneratorHelper.newSummarySection(subSectionName));

                    List<PlanRiskSubItemField> subitemFields = this.planRiskItemBS.listSubFieldsBySubItem(subitem)
                            .getList();

                    showSubItemFields(subitemFields, document);
                }
            }
        }
    }

    private List<PlanRiskSubItem> getSelectedSubitems(List<PlanRiskSubItem> subitems, String[] subsections) {
        List<PlanRiskSubItem> selectedSubitems = new ArrayList<PlanRiskSubItem>();
        for (PlanRiskSubItem sub : subitems) {
            if (subsections != null) {
                for (int j = 0; j < subsections.length; j++) {
                    if (sub.getId() == Long.parseLong(subsections[j])) {
                        selectedSubitems.add(sub);
                    }
                }
            }
        }
        return selectedSubitems;
    }

    private void showItemFields(List<PlanRiskItemField> itemFieldList, Document document)
            throws DocumentException, IOException {

        if (itemFieldList.size() == 0) {
            document.add(reportGeneratorHelper.listItem("Sem informações adicionais."));
            document.add(reportGeneratorHelper.paragraphSpacing());
        }

        for (PlanRiskItemField itemField : itemFieldList) {
            if (itemField.isText()) {
                document.add(
                    reportGeneratorHelper
                        .richTextAttributeDisplay(itemField.getName(), itemField.getDescription(), document)
                );
            } else { // campo de arquivos
                document.add(reportGeneratorHelper.listItem(itemField.getName() + ": "));
                String description = itemField.getDescription();
                if (description != null) {
                	Element element = reportGeneratorHelper.generateItemFieldElement(itemField.getFileLink());
                	document.add(element);
                }
            }
            document.add(reportGeneratorHelper.paragraphSpacing());
        }
    }

    private void showSubItemFields(List<PlanRiskSubItemField> subItemFieldList, Document document)
            throws DocumentException, IOException {

        if (subItemFieldList.size() == 0) {
            document.add(reportGeneratorHelper.listItem("Sem informações adicionais."));
            document.add(reportGeneratorHelper.paragraphSpacing());
        }

        for (PlanRiskSubItemField subItemField : subItemFieldList) {
            if (subItemField.isText()) {
                document.add(
                    reportGeneratorHelper
                        .richTextAttributeDisplay(subItemField.getName(), subItemField.getDescription(), document)
                );
            } else { // campo de arquivos
                document.add(reportGeneratorHelper.listItem(subItemField.getName() + ": "));
                String description = subItemField.getDescription();
                if (description != null) {
                	Element element = reportGeneratorHelper.generateItemFieldElement(subItemField.getFileLink());
                	document.add(element);
                }
            }
            document.add(reportGeneratorHelper.paragraphSpacing());
        }
    }

    public static class Params implements ReportGeneratorParams {
        public String title;
        public Long planId;
        public String items;
        public String subitems;

        public Params(String title, Long planId, String items, String subitems) {
            super();
            this.title = title;
            this.planId = planId;
            this.items = items;
            this.subitems = subitems;
        }
    }
}
