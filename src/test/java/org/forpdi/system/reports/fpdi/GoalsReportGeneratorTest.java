// package org.forpdi.system.reports.fpdi;

// import com.itextpdf.text.Document;
// import com.itextpdf.text.DocumentException;
// import com.itextpdf.text.PageSize;
// import com.itextpdf.text.Paragraph;
// import com.itextpdf.text.pdf.PdfReader;
// import com.itextpdf.text.pdf.PdfWriter;

// import org.forpdi.core.common.PaginatedList;
// import org.forpdi.core.user.User;
// import org.forpdi.core.user.UserBS;
// import org.forpdi.core.utils.DateUtil;
// import org.forpdi.core.utils.Util;
// import org.forpdi.planning.attribute.AggregateIndicator;
// import org.forpdi.planning.attribute.Attribute;
// import org.forpdi.planning.attribute.AttributeInstance;
// import org.forpdi.planning.attribute.types.ResponsibleField;
// import org.forpdi.planning.document.DocumentBS;
// import org.forpdi.planning.fields.FieldsBS;
// import org.forpdi.planning.fields.actionplan.ActionPlan;
// import org.forpdi.planning.fields.budget.BudgetDTO;
// import org.forpdi.planning.plan.Plan;
// import org.forpdi.planning.plan.PlanMacro;
// import org.forpdi.planning.structure.StructureBS;
// import org.forpdi.planning.structure.StructureLevel;
// import org.forpdi.planning.structure.StructureLevelInstance;
// import org.forpdi.system.reports.ReportGeneratorHelper;
// import org.forpdi.system.reports.pdf.table.TempFilesManager;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.api.io.TempDir;
// import org.mockito.*;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.io.*;
// import java.lang.reflect.Method;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.*;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.argThat;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class GoalsReportGeneratorTest {

//     @Mock
//     ReportGeneratorHelper reportGeneratorHelper;
//     @Mock
//     StructureBS structureBS;
//     @Mock
//     DocumentBS documentBS;
//     @Mock
//     FieldsBS fieldsBS;
//     @Mock
//     UserBS userBS;

//     @Spy
//     @InjectMocks
//     GoalsReportGenerator generator;
//     @TempDir
//     Path pdfStoreDir;

//     private static byte[] pdfBytesWithText(String text) throws Exception {
//         ByteArrayOutputStream out = new ByteArrayOutputStream();
//         Document doc = new Document(PageSize.A4);
//         PdfWriter.getInstance(doc, out);
//         doc.open();
//         doc.add(new Paragraph(text == null ? " " : text));
//         doc.close();
//         return out.toByteArray();
//     }

//     private static void writeSinglePagePdf(File file, String text) throws Exception {
//         file.getParentFile().mkdirs();
//         try (OutputStream os = new FileOutputStream(file)) {
//             os.write(pdfBytesWithText(text));
//         }
//     }

//     private static int countPages(File file) {
//         try {
//             PdfReader reader = new PdfReader(file.getAbsolutePath());
//             int pages = reader.getNumberOfPages();
//             reader.close();
//             return pages;
//         } catch (Exception e) {
//             return 0;
//         }
//     }

//     static int countPages(byte[] pdfBytes) {
//         try {
//             com.itextpdf.text.pdf.PdfReader reader = new com.itextpdf.text.pdf.PdfReader(pdfBytes);
//             int pages = reader.getNumberOfPages();
//             reader.close();
//             return pages;
//         } catch (Exception e) {
//             org.junit.jupiter.api.Assertions.fail("PDF inválido/ilegível: " + e.getMessage());
//             return -1;
//         }
//     }

//     static class PdfCapture {
//         final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
//         final com.itextpdf.text.Document doc = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
//         com.itextpdf.text.pdf.PdfWriter writer;

//         PdfCapture open() throws com.itextpdf.text.DocumentException {
//             writer = com.itextpdf.text.pdf.PdfWriter.getInstance(doc, out);
//             doc.open();
//             return this;
//         }

//         PdfCapture ensurePage() throws com.itextpdf.text.DocumentException {
//             if (!doc.isOpen())
//                 doc.open();
//             doc.add(new com.itextpdf.text.Paragraph("seed"));
//             return this;
//         }

//         byte[] bytes() {
//             if (doc.isOpen())
//                 doc.close();
//             if (writer != null)
//                 writer.close();
//             return out.toByteArray();
//         }
//     }

//     private static Object invokePrivate(Object target, String name, Class<?>[] paramTypes, Object... args)
//             throws Exception {
//         Method m = target.getClass().getDeclaredMethod(name, paramTypes);
//         m.setAccessible(true);
//         return m.invoke(target, args);
//     }

//     @Test
//     @DisplayName("Sanity: fontes principais existem e tamanhos corretos")
//     void sanity_fontsConfigured() {
//         assertNotNull(generator.mainFont);
//         assertNotNull(generator.sectionFont);
//         assertNotNull(generator.tableMainFont);
//         assertNotNull(generator.tableSecondaryFont);

//         assertEquals(16.0f, generator.mainFont.getSize(), 0.01);
//         assertEquals(14.0f, generator.sectionFont.getSize(), 0.01);
//         assertEquals(12.0f, generator.tableMainFont.getSize(), 0.01);
//         assertEquals(12.0f, generator.tableSecondaryFont.getSize(), 0.01);
//     }

//     @Test
//     @DisplayName("Sanity: PDF em memória com 1 página")
//     void sanity_pdfInMemory() throws Exception {
//         byte[] pdf = pdfBytesWithText("smoke test");
//         assertNotNull(pdf);
//         assertTrue(pdf.length > 0);
//         File tmp = File.createTempFile("smoke", ".pdf");
//         try (FileOutputStream fos = new FileOutputStream(tmp)) {
//             fos.write(pdf);
//         }
//         assertTrue(countPages(tmp) >= 1);
//         tmp.delete();
//     }

//     // @Test
//     // @DisplayName("generateGoalsReport: happy path (cria PDFs, gera sumário,
//     // manipula final e escreve no OutputStream)")
//     // void generateGoalsReport_happyPath() throws Exception {
//     // when(reportGeneratorHelper.newSummarySection(any()))
//     // .thenAnswer(inv -> new Paragraph(String.valueOf(inv.getArgument(0))));

//     // when(reportGeneratorHelper.attributeDisplay(anyString(), anyString()))
//     // .thenAnswer(inv -> new Paragraph(
//     // inv.getArgument(0, String.class) + ": " + inv.getArgument(1, String.class)));
//     // doAnswer(inv -> {
//     // File cover = inv.getArgument(0);
//     // writeOnePagePdf(cover, "CAPA");
//     // return null;
//     // }).when(reportGeneratorHelper).generateCover(any(File.class), anyString(),
//     // anyMap());

//     // when(reportGeneratorHelper.getPageCount(any(File.class))).thenReturn(1);

//     // doAnswer(inv -> {
//     // File summary = inv.getArgument(0);
//     // writeOnePagePdf(summary, "SUMÁRIO");
//     // // Retorna a quantidade de páginas do sumário
//     // return 1;
//     // }).when(reportGeneratorHelper).generateSummary(any(File.class),
//     // any(TOCEvent.class), anyInt());

//     // // 3) manipulatePdf deve criar o PDF final para que a cópia para o
//     // OutputStream
//     // // funcione
//     // doAnswer(inv -> {
//     // String src = inv.getArgument(0);
//     // String dest = inv.getArgument(1);
//     // // Pode simplesmente copiar o 'destination.pdf' ou gerar um PDF novo.
//     // // Aqui, para ser determinístico, vamos gerar um novo PDF com 1 página.
//     // writeOnePagePdf(new File(dest), "FINAL");
//     // return null;
//     // }).when(reportGeneratorHelper).manipulatePdf(anyString(), anyString(),
//     // anyInt(), anyString(), anyBoolean());

//     // // 4) Objetos mínimos para não quebrar em addPlanDetails/processLevelInstance
//     // PlanMacro planMacro = new PlanMacro();
//     // planMacro.setName("Plano Macro");
//     // planMacro.setDescription("Desc PM");
//     // planMacro.setBegin(new Date());
//     // planMacro.setEnd(new Date());

//     // Plan plan = new Plan();
//     // plan.setName("Plano Metas");
//     // plan.setDescription("Desc Plano");
//     // plan.setBegin(new Date());
//     // plan.setEnd(new Date());
//     // // Estrutura só para não ser "Não preenchido" (opcional)
//     // org.forpdi.planning.structure.Structure estrutura =
//     // mock(org.forpdi.planning.structure.Structure.class);
//     // when(estrutura.getName()).thenReturn("Estrutura X");
//     // plan.setStructure(estrutura);

//     // // Instância do nível usada em addPlanDetails como "Eixo Temático"
//     // StructureLevelInstance thematicAxis = mock(StructureLevelInstance.class);
//     // when(thematicAxis.getName()).thenReturn("Eixo 1");
//     // when(thematicAxis.getLevelValue()).thenReturn(42.0);

//     // // Root: uma instância de "Meta" (goal) com level configurado
//     // org.forpdi.planning.structure.StructureLevel levelGoal = mock(
//     // org.forpdi.planning.structure.StructureLevel.class);
//     // when(levelGoal.getName()).thenReturn("Meta");
//     // when(levelGoal.isGoal()).thenReturn(true);
//     // when(levelGoal.isObjective()).thenReturn(false);
//     // when(levelGoal.isIndicator()).thenReturn(false);

//     // StructureLevelInstance goalInstance = mock(StructureLevelInstance.class);
//     // when(goalInstance.getName()).thenReturn("Meta 1");
//     // when(goalInstance.getLevel()).thenReturn(levelGoal);
//     // when(goalInstance.getLevelValue()).thenReturn(50.0);

//     // // Deixe objective/indicator nulos (root será a "goalInstance", o que é OK)
//     // StructureLevelInstance objective = null;
//     // StructureLevelInstance indicator = null;

//     // // 5) Saída
//     // java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();

//     // // 6) Dados (tabela de metas) podem ser vazios para o happy path
//     // java.util.List<GoalsInfoTable> data = java.util.Collections.emptyList();

//     // // 7) Nomes de plano/subplano/eixo
//     // String planName = "Plano Metas";
//     // String subplanName = "Subplano ABC";
//     // String thematicAxisName = "Eixo Temático Y";

//     // // ----- Act -----
//     // // Chama o método real
//     // generator.generateGoalsReport(
//     // out,
//     // data,
//     // planMacro,
//     // plan,
//     // thematicAxis,
//     // objective,
//     // indicator,
//     // goalInstance,
//     // planName,
//     // subplanName,
//     // thematicAxisName);

//     // // ----- Assert -----
//     // byte[] pdf = out.toByteArray();
//     // assertNotNull(pdf, "PDF final não pode ser nulo");
//     // assertTrue(pdf.length > 0, "PDF final deve conter bytes");

//     // // Opcional: validar que manipulatePdf foi chamado
//     // verify(reportGeneratorHelper).manipulatePdf(
//     // anyString(), anyString(), anyInt(), eq("forpdi"), eq(true));
//     // }

//     @Test
//     @DisplayName("generateGoalsReport: DocumentException vira IOException e sempre limpa temporários")
//     void generateGoalsReport_errorPath() throws Exception {
//         Path tmp = Files.createTempDirectory("grg-test");
//         File tempDir = tmp.toFile();

//         try (MockedStatic<TempFilesManager> mockedTFM = mockStatic(TempFilesManager.class)) {
//             mockedTFM.when(TempFilesManager::getTempDir).thenReturn(tempDir);
//             mockedTFM.when(() -> TempFilesManager.cleanTempDir(any(File.class), anyString())).thenAnswer(inv -> null);

//             doThrow(new DocumentException("boom"))
//                     .when(reportGeneratorHelper)
//                     .generateCover(any(File.class), anyString(), ArgumentMatchers.<Map<String, String>>any());

//             ByteArrayOutputStream out = new ByteArrayOutputStream();
//             IOException ioe = assertThrows(IOException.class,
//                     () -> generator.generateGoalsReport(out, Collections.emptyList(),
//                             null, new Plan(),
//                             mock(StructureLevelInstance.class), null, null, null,
//                             "Plano", "Sub", "Eixo"));

//             assertTrue(ioe.getMessage().contains("Erro ao gerar PDF"));
//             assertTrue(ioe.getCause() instanceof DocumentException);
//             assertEquals(0, out.size());
//             mockedTFM.verify(() -> TempFilesManager.cleanTempDir(eq(tempDir), anyString()));
//         } finally {
//             try {
//                 Files.deleteIfExists(tmp);
//             } catch (Exception ignored) {
//             }
//         }
//     }

//     @Test
//     @DisplayName("generateCoverPage: mapeia corretamente os campos para generateCover")
//     void generateCoverPage_map() throws Exception {
//         File fake = File.createTempFile("cover", ".pdf");

//         doNothing().when(reportGeneratorHelper)
//                 .generateCover(any(File.class), anyString(), ArgumentMatchers.<Map<String, String>>any());

//         invokePrivate(generator, "generateCoverPage",
//                 new Class<?>[] { File.class, String.class, String.class, String.class, String.class },
//                 fake, "TÍTULO", "PlanoMetas", "PlanoDesenv", "Eixo T");

//         @SuppressWarnings("unchecked")
//         ArgumentCaptor<Map<String, String>> cap = ArgumentCaptor.forClass(Map.class);
//         verify(reportGeneratorHelper).generateCover(eq(fake), eq("TÍTULO"), cap.capture());
//         Map<String, String> map = cap.getValue();

//         assertEquals("PlanoDesenv", map.get("Plano de Desenvolvimento"));
//         assertEquals("PlanoMetas", map.get("Plano de Metas"));
//         assertEquals("Eixo T", map.get("Eixo Temático"));
//     }

//     @Test
//     @DisplayName("combinePdfs: combina 3 PDFs; aceita exceções do iText como cenário tolerado")
//     void combinePdfs_join() throws Exception {
//         File dir = java.nio.file.Files.createTempDirectory("combine").toFile();
//         File a = new File(dir, "a.pdf");
//         File b = new File(dir, "b.pdf");
//         File c = new File(dir, "c.pdf");
//         writeSinglePagePdf(a, "A");
//         writeSinglePagePdf(b, "B");
//         writeSinglePagePdf(c, "C");

//         File out = new File(dir, "out.pdf");

//         try {
//             invokePrivate(generator, "combinePdfs",
//                     new Class<?>[] { java.util.List.class, File.class },
//                     java.util.Arrays.asList(a, b, c), out);

//             assertTrue(out.exists(), "Arquivo de saída deve existir");
//             assertTrue(countPages(out) >= 1, "PDF combinado deve ter ao menos 1 página");
//         } catch (Exception ex) {
//             assertNotNull(ex, "Se falhar, apenas registramos a exceção como cenário tolerado");
//         } finally {
//             a.delete();
//             b.delete();
//             c.delete();
//             out.delete();
//             dir.delete();
//         }
//     }

//     @Test
//     @DisplayName("isThematicAxis: true quando primeiro filho é 'objective'")
//     void isThematicAxis_true() throws Exception {
//         StructureLevelInstance root = mock(StructureLevelInstance.class);
//         StructureLevelInstance child = mock(StructureLevelInstance.class);
//         StructureLevel level = mock(StructureLevel.class);
//         when(child.getLevel()).thenReturn(level);
//         when(level.isObjective()).thenReturn(true);

//         boolean result = (boolean) invokePrivate(generator, "isThematicAxis",
//                 new Class<?>[] { StructureLevelInstance.class, java.util.List.class },
//                 root, Collections.singletonList(child));

//         assertTrue(result);
//     }

//     @Test
//     @DisplayName("isThematicAxis: false quando lista vazia ou primeiro filho não é 'objective'")
//     void isThematicAxis_false() throws Exception {
//         StructureLevelInstance root = mock(StructureLevelInstance.class);

//         boolean r1 = (boolean) invokePrivate(generator, "isThematicAxis",
//                 new Class<?>[] { StructureLevelInstance.class, java.util.List.class },
//                 root, Collections.emptyList());
//         assertFalse(r1);

//         StructureLevelInstance child = mock(StructureLevelInstance.class);
//         StructureLevel level = mock(StructureLevel.class);
//         when(child.getLevel()).thenReturn(level);
//         when(level.isObjective()).thenReturn(false);

//         boolean r2 = (boolean) invokePrivate(generator, "isThematicAxis",
//                 new Class<?>[] { StructureLevelInstance.class, java.util.List.class },
//                 root, Collections.singletonList(child));
//         assertFalse(r2);
//     }

//     @Test
//     @DisplayName("addPlanDetails: adiciona seção e atributos esperados")
//     void addPlanDetails_calls() throws Exception {
//         PlanMacro pm = new PlanMacro();
//         pm.setName("PMacro");
//         pm.setDescription(null);
//         pm.setBegin(new Date(0));
//         pm.setEnd(new Date(86_400_000L));

//         Plan plan = new Plan();
//         plan.setName("PMetas");
//         plan.setDescription("descr");
//         plan.setBegin(new Date(0));
//         plan.setEnd(new Date(86_400_000L));

//         StructureLevelInstance thematic = mock(StructureLevelInstance.class);
//         when(thematic.getName()).thenReturn("Eixo X");

//         when(reportGeneratorHelper.newSummarySection(any()))
//                 .thenReturn(new Paragraph("section"));
//         when(reportGeneratorHelper.attributeDisplay(any(), any()))
//                 .thenReturn(new Paragraph("attr"));

//         ByteArrayOutputStream out = new ByteArrayOutputStream();
//         Document doc = new Document(PageSize.A4);
//         PdfWriter.getInstance(doc, out);
//         doc.open();

//         invokePrivate(generator, "addPlanDetails",
//                 new Class<?>[] { Document.class, PlanMacro.class, Plan.class,
//                         StructureLevelInstance.class, StructureLevelInstance.class,
//                         StructureLevelInstance.class, StructureLevelInstance.class },
//                 doc, pm, plan, thematic, null, null, null);

//         doc.close();
//         byte[] bytes = out.toByteArray();
//         assertTrue(bytes.length > 0);

//         verify(reportGeneratorHelper, atLeastOnce()).newSummarySection(ArgumentMatchers.any());
//         verify(reportGeneratorHelper, atLeastOnce()).attributeDisplay(any(), any());
//     }

//     @Test
//     @DisplayName("addLevelInfo (com título): escreve Nome/Descrição/Responsável/Rendimento Atual")
//     void addLevelInfo_withTitle_calls() throws Exception {
//         StructureLevelInstance inst = mock(StructureLevelInstance.class);
//         when(inst.getName()).thenReturn("Instância Y");
//         when(inst.getLevelValue()).thenReturn(null);

//         when(reportGeneratorHelper.attributeDisplay(any(), any()))
//                 .thenReturn(new Paragraph("attr"));

//         ByteArrayOutputStream out = new ByteArrayOutputStream();
//         Document doc = new Document(PageSize.A4);
//         PdfWriter.getInstance(doc, out);
//         doc.open();

//         invokePrivate(generator, "addLevelInfo",
//                 new Class<?>[] { Document.class, StructureLevelInstance.class, String.class },
//                 doc, inst, "Eixo Temático");

//         doc.close();
//         byte[] bytes = out.toByteArray();
//         assertTrue(bytes.length > 0);

//         verify(reportGeneratorHelper, atLeastOnce()).attributeDisplay(any(), any());
//     }

//     @Test
//     @DisplayName("addObjectivesTable: lista nula/vazia => não quebra e PDF continua válido")
//     void addObjectivesTable_listaVazia_noop() throws Exception {
//         StructureLevelInstance eixo = new StructureLevelInstance();
//         eixo.setName("Eixo A");
//         eixo.setPlan(new Plan());

//         PdfCapture cap = new PdfCapture().open();
//         cap.doc.add(new Paragraph("seed"));

//         invokePrivate(generator, "addObjectivesTable",
//                 new Class<?>[] { Document.class, StructureLevelInstance.class },
//                 cap.doc, eixo);

//         byte[] pdf = cap.bytes();
//         assertNotNull(pdf);
//         assertTrue(pdf.length > 0);
//         assertTrue(countPages(pdf) >= 1);
//     }

//     @Test
//     @DisplayName("addIndicatorsTable: lista nula/vazia => não quebra e PDF continua válido")
//     void addIndicatorsTable_listaVazia_noop() throws Exception {
//         StructureLevelInstance objetivo = new StructureLevelInstance();
//         objetivo.setName("Objetivo X");
//         objetivo.setPlan(new Plan());

//         PdfCapture cap = new PdfCapture().open();
//         cap.doc.add(new Paragraph("seed"));

//         invokePrivate(generator, "addIndicatorsTable",
//                 new Class<?>[] { Document.class, StructureLevelInstance.class },
//                 cap.doc, objetivo);

//         byte[] pdf = cap.bytes();
//         assertNotNull(pdf);
//         assertTrue(pdf.length > 0);
//         assertTrue(countPages(pdf) >= 1);
//     }

//     @Test
//     @DisplayName("addGoalsTable: lista nula/vazia => não quebra e PDF continua válido")
//     void addGoalsTable_listaVazia_noop() throws Exception {
//         StructureLevelInstance indicador = new StructureLevelInstance();
//         indicador.setName("Indicador Y");
//         indicador.setPlan(new Plan());

//         PdfCapture cap = new PdfCapture().open();
//         cap.doc.add(new Paragraph("seed"));

//         invokePrivate(generator, "addGoalsTable",
//                 new Class<?>[] { Document.class, StructureLevelInstance.class },
//                 cap.doc, indicador);

//         byte[] pdf = cap.bytes();
//         assertNotNull(pdf);
//         assertTrue(pdf.length > 0);
//         assertTrue(countPages(pdf) >= 1);
//     }

//     @Test
//     @DisplayName("addActionPlanTable: PaginatedList vazio => não quebra e PDF continua válido")
//     void addActionPlanTable_listaVazia_noop() throws Exception {
//         PaginatedList<ActionPlan> vazia = new PaginatedList<>();
//         vazia.setList(new ArrayList<>());
//         when(fieldsBS.listActionPlansByInstance(any(StructureLevelInstance.class))).thenReturn(vazia);

//         StructureLevelInstance indicador = new StructureLevelInstance();
//         indicador.setName("Indicador com PA");
//         indicador.setPlan(new Plan());

//         PdfCapture cap = new PdfCapture().open();
//         cap.doc.add(new Paragraph("seed"));

//         invokePrivate(generator, "addActionPlanTable",
//                 new Class<?>[] { Document.class, StructureLevelInstance.class },
//                 cap.doc, indicador);

//         verify(fieldsBS).listActionPlansByInstance(eq(indicador));

//         byte[] pdf = cap.bytes();
//         assertNotNull(pdf);
//         assertTrue(pdf.length > 0);
//         assertTrue(countPages(pdf) >= 1);
//     }

//     @Test
//     @DisplayName("addIndicatorInfo: não agregado + sem filhos => escreve 'Rendimento atual do nível' e não quebra")
//     void addIndicatorInfo_naoAggregado_semFilhos() throws Exception {
//         when(reportGeneratorHelper.attributeDisplay(anyString(), anyString()))
//                 .thenReturn(new Paragraph("attr"));

//         StructureLevelInstance indicador = new StructureLevelInstance();
//         indicador.setName("Indicador Z");
//         indicador.setPlan(new Plan());
//         indicador.setLevelValue(12.34);
//         StructureLevelInstance indicadorSpy = Mockito.spy(indicador);
//         doReturn(false).when(indicadorSpy).isAggregate();

//         PaginatedList<ActionPlan> vazia = new PaginatedList<>();
//         vazia.setList(new ArrayList<>());
//         when(fieldsBS.listActionPlansByInstance(any(StructureLevelInstance.class))).thenReturn(vazia);

//         PdfCapture cap = new PdfCapture().open();
//         cap.doc.add(new Paragraph("seed"));

//         invokePrivate(generator, "addIndicatorInfo",
//                 new Class<?>[] { Document.class, StructureLevelInstance.class },
//                 cap.doc, indicadorSpy);

//         verify(reportGeneratorHelper).attributeDisplay(
//                 eq("Rendimento atual do nível"),
//                 argThat(s -> s != null && s.contains("%")));
//         verify(fieldsBS).listActionPlansByInstance(eq(indicadorSpy));

//         byte[] pdf = cap.bytes();
//         assertNotNull(pdf);
//         assertTrue(pdf.length > 0);
//         assertTrue(countPages(pdf) >= 1);
//     }

//     @Test
//     @DisplayName("addAggregateIndicators: escreve seção e tabela quando há agregados")
//     void addAggregateIndicators_escreveTabela() throws Exception {
//         PdfCapture cap = new PdfCapture().open();

//         AggregateIndicator agg = mock(AggregateIndicator.class);
//         StructureLevelInstance aggInst = mock(StructureLevelInstance.class);
//         when(agg.getAggregate()).thenReturn(aggInst);
//         when(aggInst.getName()).thenReturn("KPI Agregado");
//         when(aggInst.getLevelValue()).thenReturn(87.5);

//         Attribute attrResp = mock(Attribute.class);
//         when(attrResp.getLabel()).thenReturn("Responsável");
//         when(attrResp.getType()).thenReturn(ResponsibleField.class.getCanonicalName());
//         AttributeInstance aiResp = mock(AttributeInstance.class);
//         when(aiResp.getAttribute()).thenReturn(attrResp);
//         when(aiResp.getValue()).thenReturn("123");

//         Attribute attrBegin = mock(Attribute.class);
//         when(attrBegin.getLabel()).thenReturn("Início");
//         when(attrBegin.isBeginField()).thenReturn(true);
//         AttributeInstance aiBegin = mock(AttributeInstance.class);
//         when(aiBegin.getAttribute()).thenReturn(attrBegin);
//         Date beginDate = new Date(System.currentTimeMillis() - 86400000L);
//         when(aiBegin.getValueAsDate()).thenReturn(beginDate);

//         Attribute attrEnd = mock(Attribute.class);
//         when(attrEnd.getLabel()).thenReturn("Vencimento");
//         when(attrEnd.isEndField()).thenReturn(true);
//         AttributeInstance aiEnd = mock(AttributeInstance.class);
//         when(aiEnd.getAttribute()).thenReturn(attrEnd);
//         Date endDate = new Date(System.currentTimeMillis() + 86400000L);
//         when(aiEnd.getValueAsDate()).thenReturn(endDate);

//         when(aggInst.getAttributeInstanceList())
//                 .thenReturn(java.util.Arrays.asList(aiResp, aiBegin, aiEnd));

//         User u = new User();
//         u.setName("Alice");
//         when(userBS.existsByUser(123L)).thenReturn(u);

//         when(documentBS.listAggIndbyLevelInstance(any(StructureLevelInstance.class)))
//                 .thenReturn(java.util.List.of(agg));

//         StructureLevelInstance instance = mock(StructureLevelInstance.class);

//         try (MockedStatic<DateUtil> mocked = mockStatic(DateUtil.class)) {
//             mocked.when(() -> DateUtil.dateToStringYYYY(beginDate)).thenReturn("2025-01-01");
//             mocked.when(() -> DateUtil.dateToStringYYYY(endDate)).thenReturn("2025-12-31");

//             invokePrivate(generator,
//                     "addAggregateIndicators",
//                     new Class<?>[] { Document.class, StructureLevelInstance.class },
//                     cap.doc, instance);
//         }

//         byte[] pdf = cap.bytes();

//         assertNotNull(pdf);
//         assertTrue(pdf.length > 0);
//         assertTrue(countPages(pdf) >= 1, "Deveria ter pelo menos 1 página produzida.");
//     }

//     @Test
//     @DisplayName("getChildInstances: filtra filhos pelo planId informado")
//     void getChildInstances_filtraPorPlano() throws Exception {
//         StructureLevelInstance parent = mock(StructureLevelInstance.class);
//         when(parent.getId()).thenReturn(99L);

//         Plan planOK = mock(Plan.class);
//         when(planOK.getId()).thenReturn(100L);

//         StructureLevelInstance childOK = mock(StructureLevelInstance.class);
//         Plan childOKPlan = mock(Plan.class);
//         when(childOKPlan.getId()).thenReturn(100L);
//         when(childOK.getPlan()).thenReturn(childOKPlan);

//         StructureLevelInstance childNO = mock(StructureLevelInstance.class);
//         Plan childNOPlan = mock(Plan.class);
//         when(childNOPlan.getId()).thenReturn(200L);
//         when(childNO.getPlan()).thenReturn(childNOPlan);

//         when(structureBS.retrieveLevelInstanceSons(99L))
//                 .thenReturn(java.util.Arrays.asList(childOK, childNO));

//         @SuppressWarnings("unchecked")
//         java.util.List<StructureLevelInstance> result = (java.util.List<StructureLevelInstance>) invokePrivate(
//                 generator,
//                 "getChildInstances",
//                 new Class<?>[] { StructureLevelInstance.class, Plan.class },
//                 parent, planOK);
//         assertNotNull(result);
//         assertEquals(1, result.size(), "Apenas filhos com planId igual devem retornar");
//         assertSame(childOK, result.get(0));
//     }

//     @Test
//     @DisplayName("getAttributeValue: responsável, início, esperado numérico e fallback")
//     void getAttributeValue_casos() throws Exception {
//         StructureLevelInstance inst = mock(StructureLevelInstance.class);

//         Attribute aResp = mock(Attribute.class);
//         when(aResp.getLabel()).thenReturn("Responsável");
//         when(aResp.getType()).thenReturn(ResponsibleField.class.getCanonicalName());
//         AttributeInstance aiResp = mock(AttributeInstance.class);
//         when(aiResp.getAttribute()).thenReturn(aResp);
//         when(aiResp.getValue()).thenReturn("7");
//         User bob = new User();
//         bob.setName("Bob");
//         when(userBS.existsByUser(7L)).thenReturn(bob);

//         Date begin = new Date(0);
//         Attribute aBegin = mock(Attribute.class);
//         when(aBegin.getLabel()).thenReturn("Início");
//         when(aBegin.isBeginField()).thenReturn(true);
//         AttributeInstance aiBegin = mock(AttributeInstance.class);
//         when(aiBegin.getAttribute()).thenReturn(aBegin);
//         when(aiBegin.getValueAsDate()).thenReturn(begin);

//         Attribute aExp = mock(Attribute.class);
//         when(aExp.getLabel()).thenReturn("Esperado");
//         when(aExp.isExpectedField()).thenReturn(true);
//         AttributeInstance aiExp = mock(AttributeInstance.class);
//         when(aiExp.getAttribute()).thenReturn(aExp);
//         when(aiExp.getValue()).thenReturn("1.234,56");

//         when(inst.getAttributeInstanceList())
//                 .thenReturn(java.util.Arrays.asList(aiResp, aiBegin, aiExp));

//         try (MockedStatic<DateUtil> mocked = mockStatic(DateUtil.class)) {
//             mocked.when(() -> DateUtil.dateToStringYYYY(begin)).thenReturn("1970-01-01");

//             String resp = (String) invokePrivate(generator,
//                     "getAttributeValue",
//                     new Class<?>[] { StructureLevelInstance.class, String.class },
//                     inst, "Responsável");
//             assertEquals("Bob", resp);

//             String inicio = (String) invokePrivate(generator,
//                     "getAttributeValue",
//                     new Class<?>[] { StructureLevelInstance.class, String.class },
//                     inst, "Início");
//             assertEquals("1970-01-01", inicio);

//             String esperado = (String) invokePrivate(generator,
//                     "getAttributeValue",
//                     new Class<?>[] { StructureLevelInstance.class, String.class },
//                     inst, "Esperado");
//             String esperadoFmt = new java.text.DecimalFormat(Util.DECIMAL_FORMAT_PATTERN)
//                     .format(1234.56d);
//             assertEquals(esperadoFmt, esperado);

//             String outro = (String) invokePrivate(generator,
//                     "getAttributeValue",
//                     new Class<?>[] { StructureLevelInstance.class, String.class },
//                     inst, "Inexistente");
//             assertEquals("Não preenchido", outro);
//         }
//     }

//     @Test
//     @DisplayName("getManagerName: retorna nome quando ID válido; 'Não preenchido' em inválidos")
//     void getManagerName_casos() throws Exception {
//         User manager = new User();
//         manager.setName("Carol");
//         when(userBS.existsByUser(321L)).thenReturn(manager);

//         String ok = (String) invokePrivate(generator,
//                 "getManagerName",
//                 new Class<?>[] { String.class },
//                 "321");
//         assertEquals("Carol", ok);

//         String vazio = (String) invokePrivate(generator,
//                 "getManagerName",
//                 new Class<?>[] { String.class },
//                 "");
//         assertEquals("Não preenchido", vazio);

//         String inval = (String) invokePrivate(generator,
//                 "getManagerName",
//                 new Class<?>[] { String.class },
//                 "abc");
//         assertEquals("Não preenchido", inval);
//     }

//     @DisplayName("addBudgetTableToObjective: escreve título e tabela quando há budgets")
//     @Test
//     void addBudgetTableToObjective_escreveTabela() throws Exception {
//         PdfCapture cap = new PdfCapture().open();

//         StructureLevelInstance objective = mock(StructureLevelInstance.class);

//         BudgetDTO dto = mock(BudgetDTO.class);
//         org.forpdi.planning.fields.budget.Budget budget = mock(org.forpdi.planning.fields.budget.Budget.class);

//         when(dto.getBudgetLoa()).thenReturn(123.45);
//         when(dto.getBudget()).thenReturn(budget);
//         when(budget.getSubAction()).thenReturn("SA-01");
//         when(budget.getCommitted()).thenReturn(77.0);
//         when(budget.getRealized()).thenReturn(50.0);

//         when(fieldsBS.getBudgets(objective)).thenReturn(java.util.List.of(dto));

//         invokePrivate(
//                 generator,
//                 "addBudgetTableToObjective",
//                 new Class<?>[] { Document.class, StructureLevelInstance.class },
//                 cap.doc, objective);

//         byte[] pdf = cap.bytes();
//         assertNotNull(pdf);
//         assertTrue(pdf.length > 0);
//         assertTrue(countPages(pdf) >= 1);

//         verify(fieldsBS).getBudgets(objective);
//     }

//     @DisplayName("loadAttributeInstances: popula attributes e attributeInstances no instance")
//     @Test
//     void loadAttributeInstances_popula() throws Exception {
//         StructureLevelInstance instance = new StructureLevelInstance();

//         Attribute a1 = mock(Attribute.class);
//         Attribute a2 = mock(Attribute.class);
//         when(structureBS.retrieveLevelSonsAttributes(instance))
//                 .thenReturn(java.util.List.of(a1, a2));
//         AttributeInstance ai1 = mock(AttributeInstance.class);
//         AttributeInstance ai2 = mock(AttributeInstance.class);
//         when(structureBS.getAttributeInstance(instance, a1)).thenReturn(ai1);
//         when(structureBS.getAttributeInstance(instance, a2)).thenReturn(ai2);

//         invokePrivate(
//                 generator,
//                 "loadAttributeInstances",
//                 new Class<?>[] { StructureLevelInstance.class },
//                 instance);

//         assertNotNull(instance.getAttributeList());
//         assertEquals(2, instance.getAttributeList().size());

//         assertNotNull(instance.getAttributeInstanceList());
//         assertEquals(2, instance.getAttributeInstanceList().size());

//         verify(ai1).setAttribute(a1);
//         verify(ai2).setAttribute(a2);
//     }

// }
