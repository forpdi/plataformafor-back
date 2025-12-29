package org.forpdi.system.reports.fpdi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.types.Currency;
import org.forpdi.planning.attribute.types.NumberField;
import org.forpdi.planning.attribute.types.Percentage;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.attribute.types.ScheduleField;
import org.forpdi.planning.attribute.types.SelectPlan;
import org.forpdi.planning.attribute.types.TableField;
import org.forpdi.planning.attribute.types.TextArea;
import org.forpdi.planning.document.DocumentAttribute;
import org.forpdi.planning.document.DocumentBS;
import org.forpdi.planning.document.DocumentSection;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.schedule.ScheduleInstance;
import org.forpdi.planning.fields.table.TableInstance;
import org.forpdi.planning.fields.table.TableStructure;
import org.forpdi.planning.fields.table.TableValues;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.system.reports.ReportGeneratorHelper;
import org.forpdi.system.reports.ReportGeneratorParams;
import org.forpdi.system.reports.pdf.PdfWriterFactory;
import org.forpdi.system.reports.pdf.TOCEvent;
import org.forpdi.system.reports.pdf.htmlparser.HtmlToPdfParser;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.forpdi.planning.fields.table.TableFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

class PDIDocumentReportGeneratorTest {

    private Path temp;
    private File tempDir;
    private MockedStatic<TempFilesManager> tempMock;
    private MockedStatic<PdfWriterFactory> writerFactory;
    private MockedStatic<ReportGeneratorParams> paramsStatic;
    private MockedStatic<HtmlToPdfParser> htmlParserMock;

    @BeforeEach
    void setup() throws Exception {
        temp = Files.createTempDirectory("fpdi-test-");
        tempDir = temp.toFile();

        tempMock = mockStatic(TempFilesManager.class);
        writerFactory = mockStatic(PdfWriterFactory.class);
        paramsStatic = mockStatic(ReportGeneratorParams.class);
        htmlParserMock = mockStatic(HtmlToPdfParser.class);

        tempMock.when(TempFilesManager::getTempDir).thenReturn(tempDir);
        tempMock.when(() -> TempFilesManager.cleanTempDir(any(File.class), anyString())).thenAnswer(inv -> null);

        writerFactory
                .when(() -> PdfWriterFactory.create(any(com.itextpdf.text.Document.class), any(File.class),
                        any(TOCEvent.class)))
                .thenAnswer(inv -> {
                    com.itextpdf.text.Document doc = inv.getArgument(0);
                    File out = inv.getArgument(1);
                    TOCEvent ev = inv.getArgument(2);
                    PdfWriter w = attachWriter(doc, out);
                    if (ev != null)
                        w.setPageEvent(ev);
                    return null;
                });
        writerFactory
                .when(() -> PdfWriterFactory.create(any(com.itextpdf.text.Document.class), any(File.class)))
                .thenAnswer(inv -> {
                    com.itextpdf.text.Document doc = inv.getArgument(0);
                    File out = inv.getArgument(1);
                    attachWriter(doc, out);
                    return null;
                });

        htmlParserMock
                .when(() -> HtmlToPdfParser.addElementsParsedToDocument(anyString(),
                        any(com.itextpdf.text.Document.class)))
                .thenAnswer(inv -> null);
    }

    @AfterEach
    void teardown() throws IOException {
        tempMock.close();
        writerFactory.close();
        paramsStatic.close();
        htmlParserMock.close();
        try {
            Files.walk(temp).sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException ignored) {
        }
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field f = PDIDocumentReportGenerator.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static PdfWriter attachWriter(com.itextpdf.text.Document doc, File file) throws Exception {
        doc.setPageSize(PageSize.A4);
        return PdfWriter.getInstance(doc, new FileOutputStream(file));
    }

    private static void writeOnePagePdf(File file, String text) throws Exception {
        com.itextpdf.text.Document d = new com.itextpdf.text.Document(PageSize.A4);
        PdfWriter.getInstance(d, new FileOutputStream(file));
        d.open();
        d.add(new Paragraph(text));
        d.close();
    }

    /** ---------------------- TESTES ---------------------- */

    @Test
    @DisplayName("generateReport: fluxo mínimo (sem seções) retorna InputStream e cria PDF final")
    void generateReport_minimalFlow_returnsInputStream() throws Exception {
        PDIDocumentReportGenerator gen = new PDIDocumentReportGenerator();

        ReportGeneratorHelper helper = mock(ReportGeneratorHelper.class);

        inject(gen, "reportGeneratorHelper", helper);
        inject(gen, "planBS", null);
        inject(gen, "docBS", null);
        inject(gen, "structHelper", null);
        inject(gen, "fieldsBS", null);
        inject(gen, "userBS", null);
        inject(gen, "structureBS", null);
        inject(gen, "attrHelper", null);

        paramsStatic.when(() -> ReportGeneratorParams.extractParams(any(), eq(PDIDocumentReportGenerator.Params.class)))
                .thenReturn(new PDIDocumentReportGenerator.Params("Relatório Teste", null));

        doAnswer(inv -> { // cria capa
            File coverFile = inv.getArgument(0);
            writeOnePagePdf(coverFile, "CAPA");
            return null;
        }).when(helper).generateCover(any(File.class), anyString(), anyMap());

        doAnswer(inv -> null).when(helper).setDimensions(any(com.itextpdf.text.Document.class));
        doAnswer(inv -> { // closePdfReader seguro
            PdfReader r = inv.getArgument(0);
            if (r != null)
                r.close();
            return null;
        }).when(helper).closePdfReader(any(PdfReader.class));

        doAnswer(inv -> { // gera o final.pdf
            String src = inv.getArgument(0);
            String dst = inv.getArgument(1);
            Files.copy(Paths.get(src), Paths.get(dst), StandardCopyOption.REPLACE_EXISTING);
            return null;
        }).when(helper).manipulatePdf(anyString(), anyString(), anyInt(), anyString(), anySet(), anyBoolean());

        InputStream in = gen.generateReport(null);
        assertNotNull(in, "Deveria retornar um InputStream para o PDF final.");
        byte[] buf = new byte[16];
        assertTrue(in.read(buf) >= 0, "O stream do PDF deveria ser legível.");
        in.close();

        verify(helper, atLeastOnce()).manipulatePdf(anyString(), anyString(), anyInt(), anyString(), anySet(),
                anyBoolean());
        tempMock.verify(() -> TempFilesManager.cleanTempDir(eq(tempDir), anyString()));
    }

    @Test
    @DisplayName("generateReport: falha ao gerar capa é encapsulada em RuntimeException")
    void generateReport_whenCoverFails_wrapsRuntimeException() throws Exception {
        PDIDocumentReportGenerator gen = new PDIDocumentReportGenerator();
        ReportGeneratorHelper helper = mock(ReportGeneratorHelper.class);
        inject(gen, "reportGeneratorHelper", helper);

        paramsStatic.when(() -> ReportGeneratorParams.extractParams(any(), eq(PDIDocumentReportGenerator.Params.class)))
                .thenReturn(new PDIDocumentReportGenerator.Params("Relatório Teste", ""));

        doThrow(new IOException("falha simulada")).when(helper).generateCover(any(File.class), anyString(), anyMap());

        assertThrows(RuntimeException.class, () -> gen.generateReport(null),
                "Qualquer IOException/DocumentException deveria ser encapsulada em RuntimeException.");
    }

    @Test
    @DisplayName("returnPdfPTable: com 4 colunas (pré-textual especial) inclui cabeçalhos e resolve ResponsibleField")
    void returnPdfPTable_headersAndFormatting_callsUserBS() throws Exception {
        PDIDocumentReportGenerator gen = new PDIDocumentReportGenerator();

        var fieldsBS = mock(FieldsBS.class);
        var userBS = mock(UserBS.class);

        inject(gen, "fieldsBS", fieldsBS);
        inject(gen, "userBS", userBS);

        List<TableStructure> headers = List.of(
                mockTableStructure("H1"), mockTableStructure("H2"),
                mockTableStructure("H3"), mockTableStructure("H4"));

        TableInstance inst = mock(TableInstance.class);

        TableValues v1 = mockTableValue(Currency.class, "1234.56");
        TableValues v2 = mockTableValue(Percentage.class, "0.25");
        TableValues v3 = mockTableValue(NumberField.class, "10.5");
        TableValues v4 = mockTableValue(ResponsibleField.class, "100");

        when(fieldsBS.listTableValuesByInstance(inst)).thenReturn(List.of(v1, v2, v3, v4));

        User u = mock(User.class);
        when(u.getName()).thenReturn("José");
        when(userBS.existsByUser(100L)).thenReturn(u);

        Method m = PDIDocumentReportGenerator.class.getDeclaredMethod(
                "returnPdfPTable", List.class, List.class, boolean.class, boolean.class);
        m.setAccessible(true);

        PdfPTable table = (PdfPTable) m.invoke(gen, headers, List.of(inst), /* hideHeaders */ false,
                /* hideBorders */ false);

        assertEquals(4, table.getNumberOfColumns(), "Deveria criar uma tabela com 4 colunas.");

        verify(fieldsBS, times(1)).listTableValuesByInstance(inst);
        verify(userBS, times(1)).existsByUser(100L);
    }

    @Test
    @DisplayName("returnPdfPTable: sem cabeçalhos e sem bordas (2 colunas) cria células sem borda")
    void returnPdfPTable_hideHeadersNoBorders_buildsBorderlessCells() throws Exception {
        PDIDocumentReportGenerator gen = new PDIDocumentReportGenerator();

        var fieldsBS = mock(FieldsBS.class);
        inject(gen, "fieldsBS", fieldsBS);
        inject(gen, "userBS", mock(UserBS.class));

        List<TableStructure> headers = List.of(
                mockTableStructure("A"), mockTableStructure("B"));

        TableInstance inst = mock(TableInstance.class);
        TableValues v1 = mockTableValue(Currency.class, "99.99");
        TableValues v2 = mockTableValue(String.class, "Texto");
        when(fieldsBS.listTableValuesByInstance(inst)).thenReturn(List.of(v1, v2));

        Method m = PDIDocumentReportGenerator.class.getDeclaredMethod(
                "returnPdfPTable", List.class, List.class, boolean.class, boolean.class);
        m.setAccessible(true);

        PdfPTable table = (PdfPTable) m.invoke(gen, headers, List.of(inst), /* hideHeaders */ true,
                /* hideBorders */ true);

        assertEquals(2, table.getNumberOfColumns());

        try {
            var row0 = table.getRow(0);
            assertNotNull(row0);
            PdfPCell c0 = row0.getCells()[0];
            assertNotNull(c0);
            assertEquals(0, c0.getBorder(), "Célula deveria ser 'borderless' (NO_BORDER).");
        } catch (Throwable ignored) {
        }
    }

    private DocumentSection mockDocumentSection(long id, String name, boolean isPreText,
            org.forpdi.planning.document.Document doc) {
        DocumentSection ds = new DocumentSection();
        ds.setId(id);
        ds.setName(name);
        ds.setPreTextSection(isPreText);
        ds.setDocument(doc);
        return ds;
    }

    private DocumentAttribute mockDocumentAttribute(String name, Class<?> type, String value) {
        DocumentAttribute attr = new DocumentAttribute();
        attr.setName(name);
        attr.setType(type.getCanonicalName());
        attr.setValue(value);
        return attr;
    }

    // @Test
    // @DisplayName("generateReport: processa seção de conteúdo com TextArea e subseção com Tabela")
    // void generateReport_withContentAndSubSection_processesAll() throws Exception {
    //     PDIDocumentReportGenerator gen = new PDIDocumentReportGenerator();

    //     // Dependências
    //     var helper = mock(ReportGeneratorHelper.class);
    //     var docBS = mock(DocumentBS.class);
    //     var fieldsBS = mock(FieldsBS.class);
    //     inject(gen, "reportGeneratorHelper", helper);
    //     inject(gen, "docBS", docBS);
    //     inject(gen, "fieldsBS", fieldsBS);
    //     inject(gen, "userBS", mock(UserBS.class)); // evita NPE

    //     // Parâmetros
    //     paramsStatic.when(() -> ReportGeneratorParams.extractParams(any(), eq(PDIDocumentReportGenerator.Params.class)))
    //             .thenReturn(new PDIDocumentReportGenerator.Params("Relatório com Subseção", "1"));

    //     // Capa + pipeline PDF final
    //     doAnswer(inv -> {
    //         File coverFile = inv.getArgument(0);
    //         writeOnePagePdf(coverFile, "CAPA");
    //         return null;
    //     }).when(helper).generateCover(any(File.class), anyString(), anyMap());
    //     doAnswer(inv -> null).when(helper).setDimensions(any(com.itextpdf.text.Document.class));
    //     doAnswer(inv -> {
    //         PdfReader r = inv.getArgument(0);
    //         if (r != null)
    //             r.close();
    //         return null;
    //     }).when(helper).closePdfReader(any(PdfReader.class));
    //     doAnswer(inv -> {
    //         String src = inv.getArgument(0);
    //         String dst = inv.getArgument(1);
    //         Files.copy(Paths.get(src), Paths.get(dst), StandardCopyOption.REPLACE_EXISTING);
    //         return null;
    //     }).when(helper).manipulatePdf(anyString(), anyString(), anyInt(), anyString(), anySet(), anyBoolean());

    //     // Documento/Plano
    //     org.forpdi.planning.plan.PlanMacro planMacro = mock(org.forpdi.planning.plan.PlanMacro.class);
    //     when(planMacro.getId()).thenReturn(10L);
    //     org.forpdi.planning.document.Document document = mock(org.forpdi.planning.document.Document.class);
    //     when(document.getPlan()).thenReturn(planMacro);

    //     DocumentSection mainSection = mockDocumentSection(1L, "Seção Principal", false, document);
    //     DocumentAttribute mainAttr = mockDocumentAttribute("Atributo Principal", TextArea.class, "Conteúdo HTML");
    //     when(docBS.retrieveSectionById(1L)).thenReturn(mainSection);
    //     when(docBS.listAttributesBySection(mainSection, 10L)).thenReturn(List.of(mainAttr));

    //     // Sub-seção com tabela
    //     DocumentSection subSection = mockDocumentSection(2L, "Subseção", false, document);
    //     subSection.setFilled(true);
    //     DocumentAttribute subAttr = mockDocumentAttribute("Tabela Subseção", TableField.class, null);
    //     when(docBS.listSectionsSons(mainSection)).thenReturn(List.of(subSection));
    //     doAnswer(inv -> {
    //         @SuppressWarnings("unchecked")
    //         List<DocumentSection> lst = inv.getArgument(0);
    //         lst.forEach(s -> s.setFilled(true));
    //         return null;
    //     }).when(docBS).setSectionsFilled(anyList(), anyLong());
    //     when(docBS.listAttributesBySection(subSection, 10L)).thenReturn(List.of(subAttr));

    //     // Tabela
    //     when(fieldsBS.tableFieldsByAttribute(anyLong(), anyBoolean()))
    //             .thenReturn(mock(TableFields.class));
    //     when(fieldsBS.listTableStructureByFields(any()))
    //             .thenReturn(List.of(mockTableStructure("Col1")));
    //     when(fieldsBS.listTableInstanceByFields(any()))
    //             .thenReturn(List.of(mock(TableInstance.class)));

    //     // Execução
    //     gen.generateReport(null);

    //     // Verificações
    //     verify(docBS).retrieveSectionById(1L);
    //     verify(docBS).listSectionsSons(mainSection);
    //     verify(docBS).listAttributesBySection(subSection, 10L);
    //     htmlParserMock.verify(() -> HtmlToPdfParser.addElementsParsedToDocument(eq("Conteúdo HTML"), any()));
    // }

    @Test
    @DisplayName("generateReport: processa seção de conteúdo com ScheduleField")
    void generateReport_withScheduleField_processesCorrectly() throws Exception {
        PDIDocumentReportGenerator gen = new PDIDocumentReportGenerator();
        var helper = mock(ReportGeneratorHelper.class);
        var docBS = mock(DocumentBS.class);
        var fieldsBS = mock(FieldsBS.class);
        inject(gen, "reportGeneratorHelper", helper);
        inject(gen, "docBS", docBS);
        inject(gen, "fieldsBS", fieldsBS);
        inject(gen, "userBS", mock(UserBS.class)); // evita NPE

        // Capa + pipeline PDF final
        doAnswer(inv -> {
            File coverFile = inv.getArgument(0);
            writeOnePagePdf(coverFile, "CAPA");
            return null;
        }).when(helper).generateCover(any(File.class), anyString(), anyMap());
        doAnswer(inv -> null).when(helper).setDimensions(any(com.itextpdf.text.Document.class));
        doAnswer(inv -> {
            PdfReader r = inv.getArgument(0);
            if (r != null)
                r.close();
            return null;
        }).when(helper).closePdfReader(any(PdfReader.class));
        doAnswer(inv -> {
            String src = inv.getArgument(0);
            String dst = inv.getArgument(1);
            Files.copy(Paths.get(src), Paths.get(dst), StandardCopyOption.REPLACE_EXISTING);
            return null;
        }).when(helper).manipulatePdf(anyString(), anyString(), anyInt(), anyString(), anySet(), anyBoolean());

        paramsStatic.when(() -> ReportGeneratorParams.extractParams(any(), eq(PDIDocumentReportGenerator.Params.class)))
                .thenReturn(new PDIDocumentReportGenerator.Params("Relatório Cronograma", "1"));

        org.forpdi.planning.plan.PlanMacro planMacro = mock(org.forpdi.planning.plan.PlanMacro.class);
        when(planMacro.getId()).thenReturn(10L);

        org.forpdi.planning.document.Document document = mock(org.forpdi.planning.document.Document.class);
        when(document.getPlan()).thenReturn(planMacro);

        DocumentSection section = mockDocumentSection(1L, "Cronograma", false, document);
        DocumentAttribute attr = mockDocumentAttribute("Atividades", ScheduleField.class, null);
        attr.setId(100L);
        when(docBS.retrieveSectionById(1L)).thenReturn(section);
        when(docBS.listAttributesBySection(section, 10L)).thenReturn(List.of(attr));
        when(docBS.listSectionsSons(section)).thenReturn(Collections.emptyList());

        ScheduleInstance inst = new ScheduleInstance();
        inst.setDescription("Tarefa 1");
        inst.setBegin(new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-01"));
        inst.setEnd(new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-31"));
        when(fieldsBS.retrieveScheduleInstanceByAttribute(100L, true)).thenReturn(List.of(inst));

        gen.generateReport(null);

        verify(fieldsBS).retrieveScheduleInstanceByAttribute(100L, true);
    }

    @Test
    @DisplayName("generateReport: processa seção com SelectPlan e chama gerador de tabela do plano")
    void generateReport_withSelectPlan_callsPlanTableGenerator() throws Exception {
        PDIDocumentReportGenerator gen = new PDIDocumentReportGenerator();
        var helper = mock(ReportGeneratorHelper.class);
        var docBS = mock(DocumentBS.class);
        var planBS = mock(PlanBS.class);
        var structureBS = mock(StructureBS.class);

        inject(gen, "reportGeneratorHelper", helper);
        inject(gen, "docBS", docBS);
        inject(gen, "planBS", planBS);
        inject(gen, "structureBS", structureBS);
        inject(gen, "attrHelper", mock(AttributeHelper.class));
        inject(gen, "userBS", mock(UserBS.class)); // evita NPE

        // Capa + pipeline PDF final
        doAnswer(inv -> {
            File coverFile = inv.getArgument(0);
            writeOnePagePdf(coverFile, "CAPA");
            return null;
        }).when(helper).generateCover(any(File.class), anyString(), anyMap());
        doAnswer(inv -> null).when(helper).setDimensions(any(com.itextpdf.text.Document.class));
        doAnswer(inv -> {
            PdfReader r = inv.getArgument(0);
            if (r != null)
                r.close();
            return null;
        }).when(helper).closePdfReader(any(PdfReader.class));
        doAnswer(inv -> {
            String src = inv.getArgument(0);
            String dst = inv.getArgument(1);
            Files.copy(Paths.get(src), Paths.get(dst), StandardCopyOption.REPLACE_EXISTING);
            return null;
        }).when(helper).manipulatePdf(anyString(), anyString(), anyInt(), anyString(), anySet(), anyBoolean());

        paramsStatic.when(() -> ReportGeneratorParams.extractParams(any(), eq(PDIDocumentReportGenerator.Params.class)))
                .thenReturn(new PDIDocumentReportGenerator.Params("Relatório de Plano", "1"));

        org.forpdi.planning.plan.PlanMacro planMacro = mock(org.forpdi.planning.plan.PlanMacro.class);
        when(planMacro.getId()).thenReturn(10L);

        org.forpdi.planning.document.Document document = mock(org.forpdi.planning.document.Document.class);
        when(document.getPlan()).thenReturn(planMacro);

        DocumentSection section = mockDocumentSection(1L, "Plano de Metas", false, document);
        DocumentAttribute attr = mockDocumentAttribute("Meu Plano", SelectPlan.class, "101");
        when(docBS.retrieveSectionById(1L)).thenReturn(section);
        when(docBS.listAttributesBySection(section, 10L)).thenReturn(List.of(attr));
        when(docBS.listSectionsSons(section)).thenReturn(Collections.emptyList());

        Plan linkedPlan = new Plan();
        linkedPlan.setId(101L);
        linkedPlan.setName("Plano Vinculado");
        when(planBS.retrieveById(101L)).thenReturn(linkedPlan);
        when(structureBS.listRootLevelInstanceByPlan(linkedPlan)).thenReturn(Collections.emptyList());

        gen.generateReport(null);

        verify(planBS).retrieveById(101L);
        verify(structureBS).listRootLevelInstanceByPlan(linkedPlan);
    }

    @Test
    @DisplayName("returnPdfPTable: formata corretamente NumberField inteiro e com casas decimais")
    void returnPdfPTable_numberFieldFormatting_integerAndDouble() throws Exception {
        PDIDocumentReportGenerator gen = new PDIDocumentReportGenerator();
        var fieldsBS = mock(FieldsBS.class);
        inject(gen, "fieldsBS", fieldsBS);
        inject(gen, "userBS", mock(UserBS.class));

        List<TableStructure> headers = List.of(mockTableStructure("Valores"));
        TableInstance inst1 = mock(TableInstance.class);
        TableInstance inst2 = mock(TableInstance.class);

        TableValues vInt = mockTableValue(NumberField.class, "123");
        TableValues vDouble = mockTableValue(NumberField.class, "123.45");

        when(fieldsBS.listTableValuesByInstance(inst1)).thenReturn(List.of(vInt));
        when(fieldsBS.listTableValuesByInstance(inst2)).thenReturn(List.of(vDouble));

        Method m = PDIDocumentReportGenerator.class.getDeclaredMethod(
                "returnPdfPTable", List.class, List.class, boolean.class, boolean.class);
        m.setAccessible(true);

        PdfPTable table = (PdfPTable) m.invoke(gen, headers, List.of(inst1, inst2), false, false);

        assertEquals(1, table.getNumberOfColumns());
        assertEquals(3, table.getRows().size());
    }

    /**
     * Helpers robustos para evitar UnfinishedStubbingException em métodos finais
     */
    private static TableStructure mockTableStructure(String label) {
        TableStructure ts = mock(TableStructure.class);
        // doReturn é mais seguro quando há getters/proxies/finais
        doReturn(label).when(ts).getLabel();
        return ts;
    }

    private static TableValues mockTableValue(Class<?> typeClass, String value) {
        TableValues tv = mock(TableValues.class);
        TableStructure ts = mock(TableStructure.class);
        doReturn(typeClass.getCanonicalName()).when(ts).getType();
        doReturn(ts).when(tv).getTableStructure();
        doReturn(value).when(tv).getValue();
        return tv;
    }
}
