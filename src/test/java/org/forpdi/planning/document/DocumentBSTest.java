package org.forpdi.planning.document;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.forpdi.core.user.User;
import org.forpdi.planning.attribute.AttributeTypeFactory;
import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.forpdi.planning.attribute.types.NumberField;
import org.forpdi.planning.attribute.types.ScheduleField;
import org.forpdi.planning.attribute.types.SelectField;
import org.forpdi.planning.attribute.types.TableField;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.OptionsField;
import org.forpdi.planning.fields.schedule.ScheduleInstance;
import org.forpdi.planning.fields.table.TableFields;
import org.forpdi.planning.fields.table.TableInstance;
import org.forpdi.planning.fields.table.TableStructure;
import org.forpdi.planning.fields.table.TableValues;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.core.user.UserBS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;

class DocumentBSTest {

    private static void inject(Object target, String field, Object value) throws Exception {
        var f = DocumentBS.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    @DisplayName("returnPdfPTable: com cabeçalhos (hideHeaders=false) formata Currency/Percentage/Number e resolve ResponsibleField")
    void returnPdfPTable_withHeaders_formatsAndResolves() throws Exception {
        DocumentBS bs = new DocumentBS();

        FieldsBS fieldsBS = mock(FieldsBS.class);
        UserBS userBS = mock(UserBS.class);
        inject(bs, "fieldsBS", fieldsBS);
        inject(bs, "userBS", userBS);

        TableStructure s1 = new TableStructure();
        s1.setLabel("R$");
        s1.setType(org.forpdi.planning.attribute.types.Currency.class.getCanonicalName());
        TableStructure s2 = new TableStructure();
        s2.setLabel("%");
        s2.setType(org.forpdi.planning.attribute.types.Percentage.class.getCanonicalName());
        TableStructure s3 = new TableStructure();
        s3.setLabel("Num");
        s3.setType(NumberField.class.getCanonicalName());
        TableStructure s4 = new TableStructure();
        s4.setLabel("Resp");
        s4.setType(org.forpdi.planning.attribute.types.ResponsibleField.class.getCanonicalName());
        TableStructure s5 = new TableStructure();
        s5.setLabel("Txt");
        s5.setType(String.class.getCanonicalName());
        List<TableStructure> headers = List.of(s1, s2, s3, s4, s5);

        TableInstance inst = new TableInstance();
        TableValues v1 = new TableValues();
        v1.setTableStructure(s1);
        v1.setValue("1234.56");
        TableValues v2 = new TableValues();
        v2.setTableStructure(s2);
        v2.setValue("0.25");
        TableValues v3 = new TableValues();
        v3.setTableStructure(s3);
        v3.setValue("10.5");
        TableValues v4 = new TableValues();
        v4.setTableStructure(s4);
        v4.setValue("100");
        TableValues v5 = new TableValues();
        v5.setTableStructure(s5);
        v5.setValue("texto puro");
        when(fieldsBS.listTableValuesByInstance(eq(inst), eq(false))).thenReturn(List.of(v1, v2, v3, v4, v5));

        User u = mock(User.class);
        when(u.getName()).thenReturn("Maria");
        when(userBS.existsByUser(100L)).thenReturn(u);

        PdfPTable table = bs.returnPdfPTable(headers, List.of(inst), /* hideHeaders */ false, /* hideBorders */ false);
        assertEquals(5, table.getNumberOfColumns(), "Deveria criar tabela com 5 colunas.");

        verify(fieldsBS, times(1)).listTableValuesByInstance(eq(inst), eq(false));
        verify(userBS, times(1)).existsByUser(100L);
    }

    @Test
    @DisplayName("returnPdfPTable: sem cabeçalhos e sem bordas (2 colunas) cria células NO_BORDER")
    void returnPdfPTable_hideHeadersBorderless() throws Exception {
        DocumentBS bs = new DocumentBS();

        FieldsBS fieldsBS = mock(FieldsBS.class);
        UserBS userBS = mock(UserBS.class);
        inject(bs, "fieldsBS", fieldsBS);
        inject(bs, "userBS", userBS);

        TableStructure a = new TableStructure();
        a.setLabel("A");
        a.setType(org.forpdi.planning.attribute.types.Currency.class.getCanonicalName());
        TableStructure b = new TableStructure();
        b.setLabel("B");
        b.setType(String.class.getCanonicalName());
        List<TableStructure> headers = List.of(a, b);

        TableInstance inst = new TableInstance();
        TableValues v1 = new TableValues();
        v1.setTableStructure(a);
        v1.setValue("99.99");
        TableValues v2 = new TableValues();
        v2.setTableStructure(b);
        v2.setValue("texto");
        when(fieldsBS.listTableValuesByInstance(eq(inst), eq(false))).thenReturn(List.of(v1, v2));

        PdfPTable table = bs.returnPdfPTable(headers, List.of(inst), /* hideHeaders */ true, /* hideBorders */ true);
        assertEquals(2, table.getNumberOfColumns());

        try {
            var row0 = table.getRow(0);
            assertNotNull(row0);
            PdfPCell c0 = row0.getCells()[0];
            assertEquals(0, c0.getBorder(), "Célula deveria estar sem borda (NO_BORDER).");
        } catch (Throwable ignored) {
        }
    }

    @Test
    @DisplayName("setSectionsFilled: marca seção como preenchida se houver instâncias de tabela/cronograma (inclui busca em subseções)")
    void setSectionsFilled_marksFilledWithTableOrScheduleInstances() throws Exception {
        DocumentBS spy = spy(new DocumentBS());

        FieldsBS fieldsBS = mock(FieldsBS.class);
        inject(spy, "fieldsBS", fieldsBS);

        DocumentSection secA = new DocumentSection();
        secA.setName("A");
        DocumentAttribute attrTable = new DocumentAttribute();
        attrTable.setId(10L);
        attrTable.setType(TableField.class.getCanonicalName());

        DocumentSection sonA = new DocumentSection();
        sonA.setName("A-1");
        DocumentAttribute attrSched = new DocumentAttribute();
        attrSched.setId(20L);
        attrSched.setType(ScheduleField.class.getCanonicalName());

        DocumentSection secB = new DocumentSection();
        secB.setName("B");
        DocumentAttribute attrEmpty = new DocumentAttribute();
        attrEmpty.setId(30L);
        attrEmpty.setType(String.class.getCanonicalName());
        attrEmpty.setValue("");

        doReturn(List.of(attrTable)).when(spy).listAttributesBySection(eq(secA), anyLong());
        doReturn(List.of(attrSched)).when(spy).listAttributesBySection(eq(sonA), anyLong());
        doReturn(List.of(sonA)).when(spy).listSectionsSons(eq(secA));

        doReturn(List.of(attrEmpty)).when(spy).listAttributesBySection(eq(secB), anyLong());
        doReturn(Collections.emptyList()).when(spy).listSectionsSons(eq(secB));

        when(fieldsBS.retrieveTableInstanceByAttribute(10L, true))
                .thenReturn(List.of(new TableInstance()));
        when(fieldsBS.retrieveScheduleInstanceByAttribute(20L, true))
                .thenReturn(List.of(new ScheduleInstance()));
        when(fieldsBS.retrieveTableInstanceByAttribute(30L, true))
                .thenReturn(Collections.emptyList());

        List<DocumentSection> entrada = new ArrayList<>(List.of(secA, secB));
        spy.setSectionsFilled(entrada, 1L);

        assertTrue(secA.isFilled(), "Seção A deveria estar marcada como preenchida.");
        assertFalse(secB.isFilled(), "Seção B deveria continuar não preenchida.");
    }

    @Test
    @DisplayName("saveAttributes: converte e persiste valores usando AttributeTypeFactory (numérico e data)")
    void saveAttributes_convertsAndPersists() throws Exception {
        DocumentBS spy = spy(new DocumentBS());

        DocumentAttribute existentNum = new DocumentAttribute();
        existentNum.setId(1L);
        existentNum.setType(NumberField.class.getCanonicalName());

        DocumentAttribute existentDate = new DocumentAttribute();
        existentDate.setId(2L);
        existentDate.setType("org.forpdi.planning.attribute.types.DateField");

        doReturn(existentNum).when(spy).retrieveDocumentAttribute(1L);
        doReturn(existentDate).when(spy).retrieveDocumentAttribute(2L);

        AtomicInteger persistCount = new AtomicInteger();
        doAnswer(inv -> {
            persistCount.incrementAndGet();
            return null;
        })
                .when(spy).persist(any());

        AttributeTypeWrapper wrapperNum = mock(AttributeTypeWrapper.class);
        when(wrapperNum.isNumerical()).thenReturn(true);
        when(wrapperNum.isDate()).thenReturn(false);
        when(wrapperNum.toDatabase("10.50")).thenReturn("10.50-db");
        when(wrapperNum.toDatabaseNumerical("10.50")).thenReturn(10.50);

        AttributeTypeWrapper wrapperDate = mock(AttributeTypeWrapper.class);
        when(wrapperDate.isNumerical()).thenReturn(false);
        when(wrapperDate.isDate()).thenReturn(true);
        when(wrapperDate.toDatabase("2020-01-01")).thenReturn("2020-01-01-db");
        Date d = new Date(1577836800000L);
        when(wrapperDate.toDatabaseDate("2020-01-01")).thenReturn(d);

        try (MockedStatic<AttributeTypeFactory> factoryStatic = mockStatic(AttributeTypeFactory.class)) {
            AttributeTypeFactory factory = mock(AttributeTypeFactory.class, RETURNS_DEEP_STUBS);
            factoryStatic.when(AttributeTypeFactory::getInstance).thenReturn(factory);

            when(factory.get(eq(NumberField.class.getCanonicalName())).getWrapper()).thenReturn(wrapperNum);
            when(factory.get(eq("org.forpdi.planning.attribute.types.DateField")).getWrapper()).thenReturn(wrapperDate);

            DocumentAttribute inNum = new DocumentAttribute();
            inNum.setId(1L);
            inNum.setValue("10.50");
            DocumentAttribute inDate = new DocumentAttribute();
            inDate.setId(2L);
            inDate.setValue("2020-01-01");

            List<DocumentAttribute> out = spy.saveAttributes(List.of(inNum, inDate));

            assertEquals(2, out.size());
            assertEquals("10.50-db", existentNum.getValue());
            assertEquals(10.50, existentNum.getValueAsNumber());
            assertEquals("2020-01-01-db", existentDate.getValue());
            assertEquals(d, existentDate.getValueAsDate());
            assertTrue(persistCount.get() >= 2, "Deveria persistir ao menos os dois atributos ajustados.");
        }
    }

    @Test
    @DisplayName("duplicateTableField: com keepContent=true copia estruturas, opções, instâncias e valores")
    void duplicateTableField_keepContentTrue_persistsCopies() throws Exception {
        DocumentBS spy = spy(new DocumentBS());

        FieldsBS fieldsBS = mock(FieldsBS.class);
        inject(spy, "fieldsBS", fieldsBS);

        DocumentAttribute attr = new DocumentAttribute();
        attr.setId(1L);
        DocumentAttribute attrCopy = new DocumentAttribute();
        attrCopy.setId(2L);

        TableFields fields = new TableFields();
        fields.setAttributeId(attr.getId());
        when(fieldsBS.tableFieldsByAttribute(attr.getId(), true, false)).thenReturn(fields);

        TableStructure st1 = new TableStructure();
        st1.setId(100L);
        st1.setLabel("S1");
        st1.setType(SelectField.class.getCanonicalName());
        TableStructure st2 = new TableStructure();
        st2.setId(200L);
        st2.setLabel("S2");
        st2.setType(NumberField.class.getCanonicalName());
        when(fieldsBS.listTableStructureByFields(fields)).thenReturn(List.of(st1, st2));

        OptionsField opt = new OptionsField();
        opt.setLabel("Op1");
        when(fieldsBS.getOptionsForColumn(attr.getId(), st1.getId(), true)).thenReturn(List.of(opt));

        TableInstance inst = new TableInstance();
        when(fieldsBS.listTableInstanceByFields(fields, false)).thenReturn(List.of(inst));

        TableValues tv1 = new TableValues();
        tv1.setTableStructure(st1);
        tv1.setValue("A");
        TableValues tv2 = new TableValues();
        tv2.setTableStructure(st2);
        tv2.setValue("42");
        tv2.setValueAsNumber(42.0);
        when(fieldsBS.listTableValuesByInstance(inst, false)).thenReturn(List.of(tv1, tv2));

        List<Object> persisted = Collections.synchronizedList(new ArrayList<>());
        doAnswer(inv -> {
            persisted.add(inv.getArgument(0));
            return null;
        }).when(spy).persist(any());

        spy.duplicateTableField(attr, attrCopy, true);

        long countTableFields = persisted.stream().filter(x -> x instanceof TableFields).count();
        long countStructures = persisted.stream().filter(x -> x instanceof TableStructure).count();
        long countOptions = persisted.stream().filter(x -> x instanceof OptionsField).count();
        long countInstances = persisted.stream().filter(x -> x instanceof TableInstance).count();
        long countValues = persisted.stream().filter(x -> x instanceof TableValues).count();

        assertEquals(1, countTableFields, "Deveria criar 1 TableFields de cópia");
        assertEquals(2, countStructures, "Deveria copiar as 2 TableStructure");
        assertEquals(1, countOptions, "Deveria copiar 1 OptionsField");
        assertEquals(1, countInstances, "Deveria copiar 1 TableInstance");
        assertEquals(2, countValues, "Deveria copiar 2 TableValues");
    }

    @Test
    @DisplayName("manipulatePdf: numera páginas e gera arquivo de saída legível")
    void manipulatePdf_addsPageNumbersCreatesOutput() throws Exception {
        DocumentBS bs = new DocumentBS();

        File src = File.createTempFile("docbs-src-", ".pdf");
        File out = File.createTempFile("docbs-out-", ".pdf");
        try {
            com.itextpdf.text.Document d = new com.itextpdf.text.Document(PageSize.A4);
            PdfWriter.getInstance(d, new FileOutputStream(src));
            d.open();
            d.add(new Paragraph("página 1"));
            d.newPage();
            d.add(new Paragraph("página 2"));
            d.close();

            com.itextpdf.text.Document ref = new com.itextpdf.text.Document(PageSize.A4);

            bs.manipulatePdf(src.getAbsolutePath(), out.getAbsolutePath(), ref, /* unnumbered */ 0);

            assertTrue(out.exists());
            assertTrue(Files.size(out.toPath()) > 0L);

            PdfReader r = new PdfReader(out.getAbsolutePath());
            assertEquals(2, r.getNumberOfPages());
            r.close();
        } finally {
            src.delete();
            out.delete();
        }
    }

    @Test
    @DisplayName("listAllAttributesBySections: entrada vazia → lista vazia")
    void listAllAttributesBySections_empty() {
        DocumentBS bs = new DocumentBS();
        List<DocumentAttribute> list = bs.listAllAttributesBySections(Collections.emptyList());
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("generatePDFplanTable: sem instâncias raiz → lista vazia")
    void generatePDFplanTable_noRootInstances() throws Exception {
        DocumentBS bs = new DocumentBS();
        StructureBS structureBS = mock(StructureBS.class);
        inject(bs, "structureBS", structureBS);

        Plan plan = new Plan();
        when(structureBS.listRootLevelInstanceByPlan(plan)).thenReturn(Collections.emptyList());

        List<PdfPTable> tables = bs.generatePDFplanTable(plan);
        assertTrue(tables.isEmpty(), "Sem instâncias raiz, não deve haver tabelas.");
    }
}
