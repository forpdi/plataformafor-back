package org.forpdi.system.reports.fpdi;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;

import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.DateUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.dashboard.admin.GoalsInfoTable;
import org.forpdi.planning.attribute.AggregateIndicator;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.document.DocumentBS;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.forpdi.planning.fields.budget.BudgetDTO;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.system.reports.ReportGeneratorHelper;
import org.forpdi.system.reports.pdf.PDFSettings;
import org.forpdi.system.reports.pdf.PdfWriterFactory;
import org.forpdi.system.reports.pdf.TOCEvent;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class GoalsReportGenerator {

    @Autowired
    private ReportGeneratorHelper reportGeneratorHelper;

    @Autowired
    private StructureBS structureBS;
    @Autowired
    private DocumentBS documentBS;
    @Autowired
    private FieldsBS fieldsBS;
    @Autowired
    private UserBS userBS;

    protected final Font mainFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16.0f, BaseColor.BLACK);
    protected final Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14.0f, BaseColor.BLACK);
    protected final Font tableMainFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12.0f, BaseColor.BLACK);
    protected final Font tableSecondaryFont = FontFactory.getFont(FontFactory.HELVETICA, 12.0f, BaseColor.BLACK);

    /**
     * Gera o relatório de metas com capa, sumário e conteúdo.
     *
     * @param title        Título do relatório.
     * @param data         Lista de objetos GoalsInfoTable contendo os dados das
     *                     metas.
     * @param planMacro    Objeto PlanMacro contendo as informações do Plano de
     *                     Desenvolvimento.
     * @param plan         Objeto Plan contendo as informações do Plano.
     * @param rootInstance Objeto StructureLevelInstance raiz (Eixo Temático).
     * @param planName     Nome do Plano.
     * @param subplanName  Nome do Subplano.
     * @return InputStream do PDF gerado.
     * @throws DocumentException
     * @throws IOException
     */
    public InputStream generateGoalsReport(
            String title,
            List<GoalsInfoTable> data,
            PlanMacro planMacro,
            Plan plan,
            StructureLevelInstance thematicAxisInstance,
            StructureLevelInstance objectiveInstance,
            StructureLevelInstance indicatorInstance,
            StructureLevelInstance goalInstance,
            String planName,
            String subplanName,
            String thematicAxisName) throws DocumentException, IOException {
        String prefix = String.format("forpdi-report-export-%d", System.currentTimeMillis());
        File outputDir = TempFilesManager.getTempDir();

        File coverPdfFile = new File(outputDir, String.format("%s-cover.pdf", prefix));
        File contentPdfFile = new File(outputDir, String.format("%s-content.pdf", prefix));
        File summaryPdfFile = new File(outputDir, String.format("%s-summary.pdf", prefix));
        File finalPdfFile = new File(outputDir, String.format("%s-final.pdf", prefix));
        File destinationFile = new File(outputDir, String.format("%s-destination.pdf", prefix));

        generateCoverPage(coverPdfFile, title, planName, subplanName, thematicAxisName);

        TOCEvent event = new TOCEvent();
        generateContentPage(contentPdfFile, data, planMacro, plan, thematicAxisInstance, objectiveInstance,
                indicatorInstance, goalInstance, title, event);

        int coverPageCount = reportGeneratorHelper.getPageCount(coverPdfFile);
        int summaryPageCount = reportGeneratorHelper.generateSummary(summaryPdfFile, event, coverPageCount);

        combinePdfs(Arrays.asList(coverPdfFile, summaryPdfFile, contentPdfFile), destinationFile);

        reportGeneratorHelper.manipulatePdf(destinationFile.getPath(), finalPdfFile.getPath(),
                coverPageCount + summaryPageCount, "forpdi", true);

        InputStream inputStream = new FileInputStream(finalPdfFile);

        TempFilesManager.cleanTempDir(outputDir, prefix);

        return inputStream;
    }

    private void generateCoverPage(File coverPdfFile, String title, String planName, String subplanName,
            String thematicAxisName)
            throws DocumentException, IOException {
        Map<String, String> coverTextContent = new LinkedHashMap<>();
        coverTextContent.put("Plano de Desenvolvimento", subplanName);
        coverTextContent.put("Plano de Metas", planName);
        coverTextContent.put("Eixo Temático", thematicAxisName);
        reportGeneratorHelper.generateCover(coverPdfFile, title, coverTextContent);
    }

    private void generateContentPage(File contentPdfFile, List<GoalsInfoTable> data, PlanMacro planMacro, Plan plan,
            StructureLevelInstance thematicAxisInstance, StructureLevelInstance objectiveInstance,
            StructureLevelInstance indicatorInstance, StructureLevelInstance goalInstance, String title, TOCEvent event)
            throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 50, 36, 70, 65);
        PdfWriterFactory.create(document, contentPdfFile, event);

        document.open();

        String sectionIndex = "1";

        document.add(reportGeneratorHelper.newSummarySection(sectionIndex + ". " + title));

        addPlanDetails(document, planMacro, plan, thematicAxisInstance, objectiveInstance, indicatorInstance,
                goalInstance);

        processLevelInstance(document, thematicAxisInstance, plan, sectionIndex);

        document.close();
    }

    private void addPlanDetails(Document document, PlanMacro planMacro, Plan plan,
            StructureLevelInstance thematicAxisInstance, StructureLevelInstance objectiveInstance,
            StructureLevelInstance indicatorInstance, StructureLevelInstance goalInstance)
            throws DocumentException {
        String sectionIndex = "1.1";
        document.add(reportGeneratorHelper.newSummarySection(sectionIndex + ". Detalhes do Plano"));

        // Informações do Plano de Desenvolvimento
        document.add(reportGeneratorHelper.attributeDisplay("Plano de Desenvolvimento", planMacro.getName()));
        document.add(reportGeneratorHelper.attributeDisplay("Descrição",
                planMacro.getDescription() != null ? planMacro.getDescription() : "Não preenchido"));
        document.add(reportGeneratorHelper.attributeDisplay("Data de Início",
                planMacro.getBegin() != null ? DateUtil.dateToStringYYYY(planMacro.getBegin()) : "Não preenchido"));
        document.add(reportGeneratorHelper.attributeDisplay("Data de Término",
                planMacro.getEnd() != null ? DateUtil.dateToStringYYYY(planMacro.getEnd()) : "Não preenchido"));

        document.add(new Paragraph(" "));

        // Informações do Plano de Metas
        Paragraph planTitle = new Paragraph("Plano de Metas", PDFSettings.TITLE_FONT);
        planTitle.setSpacingBefore(10);
        planTitle.setSpacingAfter(5);
        document.add(planTitle);
        document.add(reportGeneratorHelper.attributeDisplay("Plano de Metas", plan.getName()));
        document.add(reportGeneratorHelper.attributeDisplay("Descrição",
                plan.getDescription() != null ? plan.getDescription() : "Não preenchido"));
        document.add(reportGeneratorHelper.attributeDisplay("Data de Início",
                plan.getBegin() != null ? DateUtil.dateToStringYYYY(plan.getBegin()) : "Não preenchido"));
        document.add(reportGeneratorHelper.attributeDisplay("Data de Término",
                plan.getEnd() != null ? DateUtil.dateToStringYYYY(plan.getEnd()) : "Não preenchido"));
        document.add(reportGeneratorHelper.attributeDisplay("Estrutura do Plano",
                plan.getStructure() != null ? plan.getStructure().getName() : "Não preenchido"));
        document.add(new Paragraph(" "));

        addLevelInfo(document, thematicAxisInstance, "Eixo Temático");
    }

    private void addLevelInfo(Document document, StructureLevelInstance levelInstance, String levelTitle)
            throws DocumentException {
        loadAttributeInstances(levelInstance);

        Paragraph levelTitleParagraph = new Paragraph(levelTitle, PDFSettings.TITLE_FONT);
        levelTitleParagraph.setSpacingBefore(10);
        levelTitleParagraph.setSpacingAfter(5);
        document.add(levelTitleParagraph);

        String name = levelInstance.getName() != null ? levelInstance.getName() : "Não preenchido";
        document.add(reportGeneratorHelper.attributeDisplay("Nome", name));

        String description = getAttributeValue(levelInstance, "Descrição");
        document.add(reportGeneratorHelper.attributeDisplay("Descrição", description));

        String responsibleName = getAttributeValue(levelInstance, "Responsável");
        document.add(reportGeneratorHelper.attributeDisplay("Responsável", responsibleName));

        String levelValueStr = (levelInstance.getLevelValue() != null)
                ? new DecimalFormat("#,##0.00").format(levelInstance.getLevelValue()) + "%"
                : "Não preenchido";
        document.add(reportGeneratorHelper.attributeDisplay("Rendimento Atual", levelValueStr));

        document.add(new Paragraph(" "));
    }

    private void processLevelInstance(Document document, StructureLevelInstance instance, Plan plan,
            String sectionIndex)
            throws DocumentException, IOException {
        loadAttributeInstances(instance);

        List<StructureLevelInstance> childInstances = getChildInstances(instance, plan);

        document.add(reportGeneratorHelper.newSummarySection(sectionIndex + ". " + instance.getName()));

        if (isThematicAxis(instance, childInstances)) {
            addEixoTematicoInfo(document, instance);
        } else {
            addLevelInfo(document, instance);
        }
        int childIndex = 1;
        for (StructureLevelInstance child : childInstances) {
            if (!child.getLevel().isGoal()) {
                String childSectionIndex = sectionIndex + "." + childIndex;
                processLevelInstance(document, child, plan, childSectionIndex);
            }
            childIndex++;
        }
    }

    private boolean isThematicAxis(StructureLevelInstance instance, List<StructureLevelInstance> childInstances) {
        return !childInstances.isEmpty() && childInstances.get(0).getLevel().isObjective();
    }

    private void addEixoTematicoInfo(Document document, StructureLevelInstance instance)
            throws DocumentException, IOException {
        document.add(reportGeneratorHelper.attributeDisplay("Nome do Eixo Temático", instance.getName()));

        String description = getAttributeValue(instance, "Descrição");
        document.add(reportGeneratorHelper.attributeDisplay("Descrição", description));

        String responsibleName = getAttributeValue(instance, "Responsável");
        document.add(reportGeneratorHelper.attributeDisplay("Responsável", responsibleName));

        String levelValueStr = (instance.getLevelValue() != null)
                ? new DecimalFormat(Util.DECIMAL_FORMAT_PATTERN).format(instance.getLevelValue()) + "%"
                : "Não preenchido";
        document.add(reportGeneratorHelper.attributeDisplay("Rendimento atual do nível", levelValueStr));

        addObjectivesTable(document, instance);
    }

    private void addLevelInfo(Document document, StructureLevelInstance instance)
            throws DocumentException, IOException {
        document.add(reportGeneratorHelper.attributeDisplay(instance.getLevel().getName(), instance.getName()));

        String description = getAttributeValue(instance, "Descrição");
        document.add(reportGeneratorHelper.attributeDisplay("Descrição", description));

        String responsibleName = getAttributeValue(instance, "Responsável");
        document.add(reportGeneratorHelper.attributeDisplay("Responsável", responsibleName));

        if (instance.getLevel().isIndicator()) {
            addIndicatorInfo(document, instance);
        }

        if (instance.getLevel().isObjective()) {
            addObjectiveInfo(document, instance);
        }

    }

    private void addObjectivesTable(Document document, StructureLevelInstance instance)
            throws DocumentException {
        List<StructureLevelInstance> objectivesList = getChildInstances(instance, instance.getPlan());

        if (objectivesList != null && !objectivesList.isEmpty()) {
            Paragraph objectivesTitle = new Paragraph("Objetivos", tableMainFont);
            objectivesTitle.setSpacingBefore(10);
            objectivesTitle.setSpacingAfter(5);
            document.add(objectivesTitle);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setWidths(new float[] { 3, 2, 2 });

            Stream.of("Nome", "Responsável", "Rendimento")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(1);
                        header.setPhrase(new Phrase(columnTitle, tableMainFont));
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(header);
                    });

            DecimalFormat decimalFormatDbl = new DecimalFormat(Util.DECIMAL_FORMAT_PATTERN);

            for (StructureLevelInstance objective : objectivesList) {
                table.addCell(new PdfPCell(new Phrase(objective.getName(), tableSecondaryFont)));

                loadAttributeInstances(objective);

                String responsibleName = getAttributeValue(objective, "Responsável Técnico");
                table.addCell(new PdfPCell(new Phrase(responsibleName, tableSecondaryFont)));

                String levelValueStr = (objective.getLevelValue() != null)
                        ? decimalFormatDbl.format(objective.getLevelValue()) + "%"
                        : "Não preenchido";
                table.addCell(new PdfPCell(new Phrase(levelValueStr, tableSecondaryFont)));
            }

            document.add(table);
        }
    }

    private void addObjectiveInfo(Document document, StructureLevelInstance instance)
            throws DocumentException {
        String levelValueStr = (instance.getLevelValue() != null)
                ? new DecimalFormat(Util.DECIMAL_FORMAT_PATTERN).format(instance.getLevelValue()) + "%"
                : "Não preenchido";
        document.add(reportGeneratorHelper.attributeDisplay("Rendimento atual do nível", levelValueStr));
        addIndicatorsTable(document, instance);

        addBudgetTableToObjective(document, instance);
    }

    private void addIndicatorsTable(Document document, StructureLevelInstance instance)
            throws DocumentException {
        List<StructureLevelInstance> indicatorsList = getChildInstances(instance, instance.getPlan());

        if (indicatorsList != null && !indicatorsList.isEmpty()) {
            Paragraph indicatorsTitle = new Paragraph("Indicadores", tableMainFont);
            indicatorsTitle.setSpacingBefore(10);
            indicatorsTitle.setSpacingAfter(5);
            document.add(indicatorsTitle);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setWidths(new float[] { 3, 2, 2, 2, 2 });

            Stream.of("Nome", "Responsável", "Início", "Vencimento", "Rendimento")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(1);
                        header.setPhrase(new Phrase(columnTitle, tableMainFont));
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(header);
                    });

            DecimalFormat decimalFormatDbl = new DecimalFormat(Util.DECIMAL_FORMAT_PATTERN);

            for (StructureLevelInstance indicator : indicatorsList) {
                table.addCell(new PdfPCell(new Phrase(indicator.getName(), tableSecondaryFont)));

                loadAttributeInstances(indicator);

                String responsibleName = getAttributeValue(indicator, "Responsável");
                table.addCell(new PdfPCell(new Phrase(responsibleName, tableSecondaryFont)));

                String beginDateStr = getAttributeValue(indicator, "Início");
                table.addCell(new PdfPCell(new Phrase(beginDateStr, tableSecondaryFont)));

                String endDateStr = getAttributeValue(indicator, "Vencimento");
                table.addCell(new PdfPCell(new Phrase(endDateStr, tableSecondaryFont)));

                String levelValueStr = (indicator.getLevelValue() != null)
                        ? decimalFormatDbl.format(indicator.getLevelValue()) + "%"
                        : "Não preenchido";
                table.addCell(new PdfPCell(new Phrase(levelValueStr, tableSecondaryFont)));
            }

            document.add(table);
        }
    }

    private void addIndicatorInfo(Document document, StructureLevelInstance instance)
            throws DocumentException {
        String levelValueStr = (instance.getLevelValue() != null)
                ? new DecimalFormat(Util.DECIMAL_FORMAT_PATTERN).format(instance.getLevelValue()) + "%"
                : "Não preenchido";
        document.add(reportGeneratorHelper.attributeDisplay("Rendimento atual do nível", levelValueStr));

        addActionPlanTable(document, instance);

        addGoalsTable(document, instance);

        if (instance.isAggregate()) {
            addAggregateIndicators(document, instance);
        }
    }

    private void addActionPlanTable(Document document, StructureLevelInstance instance)
            throws DocumentException {
        PaginatedList<ActionPlan> actionPlanList = fieldsBS.listActionPlansByInstance(instance);

        if (actionPlanList.getList() != null && !actionPlanList.getList().isEmpty()) {
            Paragraph actionPlanTitle = new Paragraph("Plano de Ação", tableMainFont);
            actionPlanTitle.setSpacingBefore(10);
            actionPlanTitle.setSpacingAfter(5);
            document.add(actionPlanTitle);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setWidths(new float[] { 4, 2, 2, 2 });

            Stream.of("Ação", "Responsável", "Início", "Fim")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(1);
                        header.setPhrase(new Phrase(columnTitle, tableMainFont));
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(header);
                    });

            for (ActionPlan actionPlan : actionPlanList.getList()) {
                table.addCell(new PdfPCell(new Phrase(actionPlan.getDescription(), tableSecondaryFont)));

                String responsibleName = actionPlan.getUserResponsibleName() != null
                        ? actionPlan.getUserResponsibleName()
                        : "Não preenchido";
                table.addCell(new PdfPCell(new Phrase(responsibleName, tableSecondaryFont)));

                String beginDateStr = actionPlan.getBegin() != null
                        ? DateUtil.dateToStringYYYY(actionPlan.getBegin())
                        : "Não preenchido";
                table.addCell(new PdfPCell(new Phrase(beginDateStr, tableSecondaryFont)));

                String endDateStr = actionPlan.getEnd() != null
                        ? DateUtil.dateToStringYYYY(actionPlan.getEnd())
                        : "Não preenchido";
                table.addCell(new PdfPCell(new Phrase(endDateStr, tableSecondaryFont)));
            }

            document.add(table);
        }
    }

    private void addGoalsTable(Document document, StructureLevelInstance instance) throws DocumentException {
        List<StructureLevelInstance> goalsList = getChildInstances(instance, instance.getPlan());

        if (goalsList != null && !goalsList.isEmpty()) {
            Paragraph goalsTitle = new Paragraph("Metas", tableMainFont);
            goalsTitle.setSpacingBefore(10);
            goalsTitle.setSpacingAfter(5);
            document.add(goalsTitle);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setWidths(new float[] { 3, 2, 2, 2, 2, 2, 2 });

            Stream.of("Nome", "Responsável Técnico", "Vencimento", "Esperado", "Alcançado", "Gestor", "Desempenho")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(1);
                        header.setPhrase(new Phrase(columnTitle, tableMainFont));
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(header);
                    });

            DecimalFormat decimalFormatDbl = new DecimalFormat(Util.DECIMAL_FORMAT_PATTERN);

            for (StructureLevelInstance goal : goalsList) {
                table.addCell(new PdfPCell(new Phrase(goal.getName(), tableSecondaryFont)));

                loadAttributeInstances(goal);

                String responsibleName = getAttributeValue(goal, "Responsável Técnico");
                table.addCell(new PdfPCell(new Phrase(responsibleName, tableSecondaryFont)));

                String endDateStr = getAttributeValue(goal, "Vencimento");
                table.addCell(new PdfPCell(new Phrase(endDateStr, tableSecondaryFont)));

                String expectedValue = getAttributeValue(goal, "Esperado");
                table.addCell(new PdfPCell(new Phrase(expectedValue, tableSecondaryFont)));

                String reachedValue = getAttributeValue(goal, "Alcançado");
                table.addCell(new PdfPCell(new Phrase(reachedValue, tableSecondaryFont)));

                String managerName = getAttributeValue(goal, "Gestor");
                table.addCell(new PdfPCell(new Phrase(managerName, tableSecondaryFont)));

                String performanceStr = (goal.getLevelValue() != null)
                        ? decimalFormatDbl.format(goal.getLevelValue()) + "%"
                        : "Não preenchido";
                table.addCell(new PdfPCell(new Phrase(performanceStr, tableSecondaryFont)));
            }

            document.add(table);
        }
    }

    private void addAggregateIndicators(Document document, StructureLevelInstance instance)
            throws DocumentException {
        List<AggregateIndicator> aggregateIndicators = this.documentBS.listAggIndbyLevelInstance(instance);

        if (aggregateIndicators != null && !aggregateIndicators.isEmpty()) {
            Paragraph aggParagraphLabel = new Paragraph();
            aggParagraphLabel.setSpacingBefore(10);
            aggParagraphLabel.add(new Phrase("Indicadores Agregados:", sectionFont));
            document.add(aggParagraphLabel);

            PdfPTable table = new PdfPTable(5);
            table.setSpacingBefore(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 3, 2, 2, 2, 2 });

            Stream.of("Nome", "Responsável", "Início", "Vencimento", "Rendimento")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(1);
                        header.setPhrase(new Phrase(columnTitle, tableMainFont));
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(header);
                    });

            DecimalFormat decimalFormatDbl = new DecimalFormat(Util.DECIMAL_FORMAT_PATTERN);

            for (AggregateIndicator aggInd : aggregateIndicators) {
                StructureLevelInstance aggInstance = aggInd.getAggregate();

                table.addCell(new PdfPCell(new Phrase(aggInstance.getName(), tableSecondaryFont)));

                String responsibleName = getAttributeValue(aggInstance, "Responsável");
                table.addCell(new PdfPCell(new Phrase(responsibleName, tableSecondaryFont)));

                String beginDateStr = getAttributeValue(aggInstance, "Início");
                table.addCell(new PdfPCell(new Phrase(beginDateStr, tableSecondaryFont)));

                String endDateStr = getAttributeValue(aggInstance, "Vencimento");
                table.addCell(new PdfPCell(new Phrase(endDateStr, tableSecondaryFont)));

                String levelValueStr = (aggInstance.getLevelValue() != null)
                        ? decimalFormatDbl.format(aggInstance.getLevelValue()) + "%"
                        : "Não preenchido";
                table.addCell(new PdfPCell(new Phrase(levelValueStr, tableSecondaryFont)));
            }

            document.add(table);
        }
    }

    private List<StructureLevelInstance> getChildInstances(StructureLevelInstance instance, Plan plan) {
        List<StructureLevelInstance> children = structureBS.retrieveLevelInstanceSons(instance.getId());

        List<StructureLevelInstance> filteredChildren = new ArrayList<>();
        for (StructureLevelInstance child : children) {
            if (child.getPlan().getId().equals(plan.getId())) {
                filteredChildren.add(child);
            }
        }
        return filteredChildren;
    }

    private String getAttributeValue(StructureLevelInstance instance, String attributeLabel) {
        if (instance.getAttributeInstanceList() != null) {
            for (AttributeInstance attrInstance : instance.getAttributeInstanceList()) {
                Attribute attribute = attrInstance.getAttribute();
                if (attribute != null && attribute.getLabel().equalsIgnoreCase(attributeLabel)) {
                    if (attributeLabel.equalsIgnoreCase("Gestor")) {
                        return getManagerName(attrInstance.getValue());
                    }

                    if (attribute.isBeginField() || attribute.isEndField()) {
                        Date dateValue = attrInstance.getValueAsDate();
                        return dateValue != null ? DateUtil.dateToStringYYYY(dateValue)
                                : "Não preenchido";
                    }
                    if (attribute.isReachedField() || attribute.isExpectedField()
                            || attribute.isMaximumField() || attribute.isMinimumField()) {
                        if (attrInstance.getValue() != null && !attrInstance.getValue().isEmpty()) {
                            String valueStr = attrInstance.getValue();
                            try {
                                valueStr = valueStr.replace(".", "").replace(",", ".");
                                double value = Double.parseDouble(valueStr);
                                return new DecimalFormat(Util.DECIMAL_FORMAT_PATTERN).format(value);
                            } catch (NumberFormatException e) {
                                return "Não preenchido";
                            }
                        } else {
                            return "Não preenchido";
                        }
                    }
                    if (attribute.getType().equals(ResponsibleField.class.getCanonicalName())) {
                        try {
                            Long userId = Long.parseLong(attrInstance.getValue());
                            User responsible = userBS.existsByUser(userId);
                            return responsible != null ? responsible.getName() : "Não preenchido";
                        } catch (NumberFormatException e) {
                            return "Não preenchido";
                        }
                    }
                    return attrInstance.getValue() != null && !attrInstance.getValue().isEmpty()
                            ? attrInstance.getValue()
                            : "Não preenchido";
                }
            }
        }
        return "Não preenchido";
    }

    private String getManagerName(String managerIdStr) {
        if (managerIdStr == null || managerIdStr.isEmpty()) {
            return "Não preenchido";
        }
        try {
            Long managerId = Long.parseLong(managerIdStr);
            User manager = userBS.existsByUser(managerId);
            return manager != null ? manager.getName() : "Não preenchido";
        } catch (NumberFormatException e) {
            return "Não preenchido";
        }
    }

    /**
     * Adiciona a tabela de Orçamento na seção de Objetivos.
     *
     * @param document  Documento PDF.
     * @param objective Instância de nível do Objetivo atual.
     * @throws DocumentException
     */
    private void addBudgetTableToObjective(Document document, StructureLevelInstance objective)
            throws DocumentException {
        List<BudgetDTO> budgetList = new ArrayList<BudgetDTO>();
        budgetList = this.fieldsBS.getBudgets(objective);

        if (budgetList != null && !budgetList.isEmpty()) {
            Paragraph budgetTitle = new Paragraph("Orçamento", tableMainFont);
            budgetTitle.setSpacingBefore(10);
            budgetTitle.setSpacingAfter(5);
            document.add(budgetTitle);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setWidths(new float[] { 3, 2, 2, 2 });

            Stream.of("Subação", "Planejado", "Empenhado", "Realizado")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(1);
                        header.setPhrase(new Phrase(columnTitle, PDFSettings.TEXT_FONT));
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(header);
                    });

            Locale ptBr = new Locale("pt", "BR");
            NumberFormat moedaFormat = NumberFormat.getCurrencyInstance(ptBr);

            for (BudgetDTO b : budgetList) {
                String subAction = (b.getBudget() != null && b.getBudget().getSubAction() != null)
                        ? b.getBudget().getSubAction()
                        : "Não preenchido";
                PdfPCell cell = new PdfPCell(new Phrase(subAction, PDFSettings.TEXT_FONT));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(cell);

                String planned = b.getBudgetLoa() != null ? moedaFormat.format(b.getBudgetLoa()) : "R$ 0,00";
                cell = new PdfPCell(new Phrase(planned, PDFSettings.TEXT_FONT));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);

                String committed = (b.getBudget() != null && b.getBudget().getCommitted() != null)
                        ? moedaFormat.format(b.getBudget().getCommitted())
                        : "R$ 0,00";
                cell = new PdfPCell(new Phrase(committed, PDFSettings.TEXT_FONT));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);

                String realized = (b.getBudget() != null && b.getBudget().getRealized() != null)
                        ? moedaFormat.format(b.getBudget().getRealized())
                        : "R$ 0,00";
                cell = new PdfPCell(new Phrase(realized, PDFSettings.TEXT_FONT));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);
            }

            document.add(table);
        }
    }

    private void loadAttributeInstances(StructureLevelInstance instance) {
        List<Attribute> attributes = structureBS.retrieveLevelSonsAttributes(instance);

        List<AttributeInstance> attributeInstances = new ArrayList<>();
        for (Attribute attribute : attributes) {
            AttributeInstance attrInstance = structureBS.getAttributeInstance(instance, attribute);
            if (attrInstance != null) {
                attrInstance.setAttribute(attribute);
                attributeInstances.add(attrInstance);
            }
        }

        instance.setAttributeList(attributes);
        instance.setAttributeInstanceList(attributeInstances);
    }

    private void combinePdfs(List<File> pdfFiles, File outputPdfFile) throws IOException, DocumentException {
        Document document = new Document();
        PdfCopy copy = new PdfCopy(document, new java.io.FileOutputStream(outputPdfFile));
        document.open();

        for (File pdfFile : pdfFiles) {
            PdfReader reader = new PdfReader(pdfFile.getPath());
            reportGeneratorHelper.copyDocumentContent(copy, reader);
            reader.close();
        }

        document.close();
    }
}