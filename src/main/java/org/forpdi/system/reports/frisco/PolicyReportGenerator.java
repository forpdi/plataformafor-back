package org.forpdi.system.reports.frisco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.system.reports.ReportGenerator;
import org.forpdi.system.reports.ReportGeneratorHelper;
import org.forpdi.system.reports.ReportGeneratorParams;
import org.forpdi.system.reports.pdf.PDFSettings;
import org.forpdi.system.reports.pdf.PdfWriterFactory;
import org.forpdi.system.reports.pdf.TOCEvent;
import org.forpdi.system.reports.pdf.table.CellBackground;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.forrisco.core.item.FieldItem;
import org.forrisco.core.item.FieldSubItem;
import org.forrisco.core.item.Item;
import org.forrisco.core.item.ItemBS;
import org.forrisco.core.item.SubItem;
import org.forrisco.core.policy.PIDescriptions;
import org.forrisco.core.policy.PIDescriptions.Description;
import org.forrisco.core.policy.Policy;
import org.forrisco.core.policy.PolicyBS;
import org.forrisco.core.policy.RiskMatrixColor;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.html.simpleparser.StyleSheet;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPRow;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;


@Component
public class PolicyReportGenerator implements ReportGenerator {
    
    @Autowired
    private ReportGeneratorHelper reportGeneratorHelper;

    @Autowired
    private ItemBS itemBS;

    @Autowired
    private PolicyBS policyBS;

    @Autowired
    private RiskBS riskBS;
    

    @Override
    public InputStream generateReport(ReportGeneratorParams params) {
        try {
            Params parsedParams = ReportGeneratorParams.extractParams(params, Params.class);
            return exportPolicyReport(parsedParams.policyId, parsedParams.title,
                    parsedParams.items, parsedParams.subitems);
        } catch (IOException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream exportPolicyReport(Long policyId, String title, String items, String subitems)
            throws IOException, DocumentException {

        File outputDir = TempFilesManager.getTempDir();

        final String prefix = String.format("frisco-report-export-%d", System.currentTimeMillis());

        File finalSummaryPdfFile = new File(outputDir, String.format("%s-final-summary.pdf", prefix));
        File destinationFile = new File(outputDir, String.format("%s-mounted.pdf", prefix));
        File finalPdfFile = new File(outputDir, String.format("final-%s.pdf", prefix));
        File coverPdfFile = new File(outputDir, String.format("%s-cover.pdf", prefix));
        File contentFile = new File(outputDir, String.format("%s-content.pdf", prefix));

        Map<String, String> coverTextContent = new HashMap<>();
        coverTextContent.put("Relatório de uma política", "");
        reportGeneratorHelper.generateCover(coverPdfFile, title, coverTextContent);

        TOCEvent event = new TOCEvent();
        PdfReader cover = new PdfReader(coverPdfFile.getPath());

        generatePolicyContent(contentFile, policyId, items, subitems, event, prefix);

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

    private void generatePolicyContent(File contentFile, Long policyId, String items, String subitems, TOCEvent event,
            String prefix)
            throws DocumentException, IOException, MalformedURLException {
        Document document = new Document();
        PdfWriterFactory.create(document, contentFile, event);
        File outputDir = contentFile.getParentFile();

        String[] sections = null;
        int chapterIndex  = 1;

        if (items != null)
            sections = items.split(",");

        String[] subsections = null;
        if (subitems != null)
            subsections = subitems.split(",");

        reportGeneratorHelper.setDimensions(document);
        document.open();

       
        if (Arrays.stream(sections).anyMatch("0"::equals)){
        	generatePolicyGeneralInformation(policyId, document, outputDir, prefix);  
        	chapterIndex++;
        }

        document.newPage();
        generatePolicyItems(document, sections, subsections, chapterIndex);

        document.close();
    }

    private void generatePolicyGeneralInformation(Long policyId, Document document, File outputDir, String prefix)
            throws DocumentException, IOException {
        Policy policy = this.policyBS.exists(policyId, Policy.class);

        document.add(reportGeneratorHelper.newSummarySection("1. Política"));
        document.add(reportGeneratorHelper.newSummarySection("1.1. Informações Gerais"));

        document.add(reportGeneratorHelper.attributeDisplay("Nome da política", policy.getName()));
        document.add(
            reportGeneratorHelper
                .richTextAttributeDisplay("Descrição", policy.getDescription(), document, PDFSettings.TITLE_FONT)
        );

        if (policy.getValidityBegin() != null && policy.getValidityEnd() != null) {
            String validity = String.format("%s à %s",
                    GeneralUtils.DATE_FORMAT.format(policy.getValidityBegin()),
                    GeneralUtils.DATE_FORMAT.format(policy.getValidityEnd()));
            document.add(reportGeneratorHelper.attributeDisplay("Prazo de vigência", validity));
        }

        // matrix de risco
        String[][] matrix = this.riskBS.getMatrixVector(policy);
        String table = "";
        for (int x = 0; x <= policy.getNline(); x++) {
            String children = "";
            for (int y = 0; y <= policy.getNcolumn(); y++) {
                children += "\t\t\t\t" + "<td id=\"x-" + x + "-y" + y + "\">"
                        + this.getMatrixValue(policy, matrix, x, y) + "</td>\n";
            }
            table += "\t\t\t" + "<tr id=\"x-" + x + "\">\n" + children + "\t\t\t" + "</tr>\n";
        }

        String html = "<html>\n" + "<head>\n" + "</head>\n" + "<body>" + "<div>\n" + "<table style=\"width:100%\">\n"
                + table + "</table>" + "</div>\n" + "</body>\n" + "</html>";

        document.add(reportGeneratorHelper.paragraphSpacing());
        document.add(reportGeneratorHelper.newSummarySection("1.2 Matriz de Risco"));

        StyleSheet styles = new StyleSheet();

        File htmlFile = new File(outputDir, String.format("%s-0.html", prefix));
        FileOutputStream out = new FileOutputStream(htmlFile);
        out.write(html.getBytes());
        out.close();

        FileReader fr = new FileReader(htmlFile.getPath());
        List<Element> p = HTMLWorker.parseToList(fr, styles);

        fr.close();

        for (int k = 0; k < p.size(); ++k) {
            if (p.get(k) instanceof PdfPTable) {
                PdfPTable att = (PdfPTable) p.get(k);

                ArrayList<PdfPRow> rows = att.getRows();

                for (int y = 0; y < rows.size(); y++) {

                    PdfPCell[] cells = rows.get(y).getCells();

                    for (int x = 0; x < cells.length; x++) {
                        if (cells[x] != null) {

                            BaseColor bc = this.getMatrixWebColor(policy, matrix, y, x);
                            CellBackground cellBackground = new CellBackground(bc);

                            if (bc != null) {
                                cells[x].setPaddingBottom(10);
                                cells[x].setVerticalAlignment(Element.ALIGN_MIDDLE);
                                cells[x].setPaddingLeft(10);
                                cells[x].setPaddingRight(10);
                                cells[x].setCellEvent(cellBackground);
                                cells[x].setBorder(0);
                            } else {
                                if (x == 0 && y == policy.getNline() + 1) {
                                    cells[x].setRotation(90);
                                }

                                if (y == policy.getNline() + 1) {
                                    cells[x].setBottom((float) 100);
                                }
                            }
                        }
                    }
                }
                document.add(att);
            }
        }
        for (int k = 0; k < p.size(); ++k) {

            if (p.get(k) instanceof Paragraph) {
                Paragraph att = (Paragraph) p.get(k);
                document.add(att);
            }
        }

        generateTypeDescriptions(document, policy);
    }

    public void generatePolicyItems(Document document, String[] sections, String[] subsections, int chapterIndex)
            throws DocumentException, FileNotFoundException, IOException {
    	int secIndex = 0;
    	int subSecIndex = 0;

        document.add(reportGeneratorHelper.newSummarySection(String.format("%d. Estrutura", chapterIndex)));

        if (sections != null) {
            for (int i = 0; i < sections.length; i++) {
                Item item = this.itemBS.retrieveItembyId(Long.parseLong(sections[i]));
                if (item == null)
                    continue;

                List<FieldItem> itemFields = this.itemBS.listFieldsByItem(item).getList();
                List<SubItem> subItems = this.itemBS.listSubItensByItem(item).getList();
                List<SubItem> selectedSubItems = getSelectedSubItems(subItems, subsections);

                String sectionName = String.format("%d.%d %s", chapterIndex, ++secIndex, item.getName());
                document.add(reportGeneratorHelper.newSummarySection(sectionName));

                showItemFields(itemFields, document);

                subSecIndex = 0;
                for (SubItem subitem : selectedSubItems) {
                    String subSectionName = String.format("%d.%d.%d %s", chapterIndex, secIndex, ++subSecIndex, subitem.getName());
                    document.add(reportGeneratorHelper.newSummarySection(subSectionName));

                    List<FieldSubItem> subitemFields = this.itemBS.listFieldsBySubItem(subitem).getList();

                    showSubItemFields(subitemFields, document);
                }
            }
        }
    }

    private BaseColor getMatrixWebColor(Policy policy, String[][] matrix, int line, int column) {

        for (int i = 0; i < matrix.length; i++) {
            if (Integer.parseInt(matrix[i][1]) == line) {
                if (Integer.parseInt(matrix[i][2]) == column) {
                    if (Integer.parseInt(matrix[i][2]) == 0) {
                        return null;
                    } else if (Integer.parseInt(matrix[i][1]) == policy.getNline()) {
                        return null;
                    } else {

                        PaginatedList<RiskLevel> risklevel = this.policyBS.listRiskLevelbyPolicy(policy);
                        int currentColor = -1;
                        if (risklevel != null) {
                            for (int k = 0; k < risklevel.getTotal(); k++) {
                                if (risklevel.getList().get(k).getLevel().equals(matrix[i][0])) {
                                    currentColor = risklevel.getList().get(k).getColor();
                                    break;
                                }
                            }
                        }

                        String colorCode = RiskMatrixColor.getCodeById(currentColor);
                        return WebColors.getRGBColor(colorCode);
                    }
                }
            }
        }
        return null;
    }

    private String getMatrixValue(Policy policy, String[][] matrix, int line, int column) {

        for (int i = 0; i < matrix.length; i++) {
            if (Integer.parseInt(matrix[i][1]) == line) {
                if (Integer.parseInt(matrix[i][2]) == column) {
                    if (Integer.parseInt(matrix[i][2]) == 0) {
                        return "<div style=\"text-align:center;\">" + matrix[i][0] + "&nbsp;&nbsp;&nbsp;&nbsp;</div>";
                    } else if (Integer.parseInt(matrix[i][1]) == policy.getNline()) {
                        return "<div style=\"text-align:center;\">" + matrix[i][0] + "</div>";
                    } else {

                        int currentColor = -1;
                        String cor = "";
                        PaginatedList<RiskLevel> risklevel = this.policyBS.listRiskLevelbyPolicy(policy);
                        if (risklevel != null) {
                            for (int k = 0; k < risklevel.getTotal(); k++) {
                                if (risklevel.getList().get(k).getLevel().equals(matrix[i][0])) {
                                    currentColor = risklevel.getList().get(k).getColor();
                                    break;
                                }
                            }
                        }

                        cor = RiskMatrixColor.getNameById(currentColor);

                        return "<div style=\"text-align:center; font-weight: bold;color:#fff;\" "
                                + "class=\"Cor " + cor + "\">" + matrix[i][0] + "</div>";

                    }
                }
            }
        }
        return "";
    }

    private void generateTypeDescriptions(Document document, Policy policy) throws DocumentException {
        PIDescriptions piDescription = policy.getPIDescriptionAsObject();

        document.add(reportGeneratorHelper.newSectionTitle("Probabilidades"));
        for (Description description : piDescription.getPdescriptions())
            document.add(reportGeneratorHelper.attributeDisplay(
                    description.getValue(),
                    description.getDescription()));

        document.add(reportGeneratorHelper.newSectionTitle("Impactos"));
        for (Description description : piDescription.getIdescriptions())
            document.add(reportGeneratorHelper.attributeDisplay(
                    description.getValue(),
                    description.getDescription()));
    }

    private List<SubItem> getSelectedSubItems(List<SubItem> subItems, String[] subsections) {
        List<SubItem> selectedSubItems = new ArrayList<SubItem>();

        for (SubItem sub : subItems) {
            if (subsections != null) {
                for (int j = 0; j < subsections.length; j++) {
                    if (sub.getId() == Long.parseLong(subsections[j])) {
                        selectedSubItems.add(sub);
                    }
                }
            }
        }

        return selectedSubItems;
    }

    private void showItemFields(List<FieldItem> itemFieldList, Document document)
            throws DocumentException, FileNotFoundException, IOException {

        if (itemFieldList.size() == 0) {
            document.add(reportGeneratorHelper.listItem("Sem informações adicionais."));
            document.add(reportGeneratorHelper.paragraphSpacing());
        }

        for (FieldItem itemField : itemFieldList) {
            if (itemField.isText()) {
                document.add(
                    reportGeneratorHelper
                        .richTextAttributeDisplay(itemField.getName(), itemField.getDescription(), document)
                );
            } else { // campo de arquivos
                document.add(reportGeneratorHelper.listItem(itemField.getName()));
                String description = itemField.getDescription();
                if (description != null) {
                	Element element = reportGeneratorHelper.generateItemFieldElement(itemField.getFileLink());
                    document.add(element);
                }
            }
            document.add(reportGeneratorHelper.paragraphSpacing());
        }
    }

    private void showSubItemFields(List<FieldSubItem> subItemFieldList, Document document)
            throws DocumentException, IOException {

        if (subItemFieldList.size() == 0) {
            document.add(reportGeneratorHelper.listItem("Sem informações adicionais."));
            document.add(reportGeneratorHelper.paragraphSpacing());
        }

        for (FieldSubItem subItemField : subItemFieldList) {
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
        public Long policyId;
        public String title;
        public String items;
        public String subitems;

        public Params(Long policyId, String title, String items, String subitems) {
            super();
            this.policyId = policyId;
            this.title = title;
            this.items = items;
            this.subitems = subitems;
        }
    }
}
