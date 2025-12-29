package org.forpdi.core.csvs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;

import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.*;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.forpdi.planning.fields.budget.Budget;
import org.forpdi.planning.fields.budget.BudgetDTO;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureHelper;
import org.forpdi.planning.structure.StructureLevel;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CsvGoalExportTest {

    @Mock
    private CsvExportHelper helper;
    @Mock
    private StructureBS structureBS;
    @Mock
    private StructureHelper structureHelper;
    @Mock
    private FieldsBS fieldsBS;
    @Mock
    private UserBS userBS;

    @InjectMocks
    private CsvGoalExport exporter;

    private StructureLevel level(String name) {
        StructureLevel l = new StructureLevel();
        l.setName(name);
        return l;
    }

    private StructureLevelInstance sli(long id, String name, StructureLevel level, Plan plan, Long parentId,
            Double levelValue) {
        StructureLevelInstance s = new StructureLevelInstance();
        s.setId(id);
        s.setName(name);
        s.setLevel(level);
        s.setPlan(plan);
        s.setParent(parentId);
        s.setLevelValue(levelValue);
        return s;
    }

    private Attribute attr(String label, String type) {
        Attribute a = new Attribute();
        a.setLabel(label);
        a.setType(type);
        return a;
    }

    private AttributeInstance ai(StructureLevelInstance s, Attribute a, String v) {
        AttributeInstance i = new AttributeInstance();
        i.setLevelInstance(s);
        i.setAttribute(a);
        i.setValue(v);
        return i;
    }

    private static String read(WriterAndBuffer w) throws IOException {
        w.flush();
        return new String(w.baos.toByteArray(), StandardCharsets.UTF_8);
    }

    static class WriterAndBuffer extends Writer {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final OutputStreamWriter buffer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            buffer.write(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
            buffer.flush();
        }

        @Override
        public void close() throws IOException {
            buffer.close();
        }

        String toStringUtf8() throws IOException {
            flush();
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }

    @BeforeEach
    void setUpCommon() {
        lenient().when(helper.getCsvDelimiter()).thenReturn(';');
        lenient().when(helper.fromStringToCsvFieldFormat(anyString()))
                .thenAnswer(inv -> "\"" + inv.getArgument(0, String.class) + "\";");
        lenient().when(helper.fromDoubleToCsvPerformanceFormat(any()))
                .thenAnswer(inv -> {
                    Double d = inv.getArgument(0);
                    return (d == null) ? null : NumberFormat.getInstance(new Locale("pt", "BR")).format(d);
                });
    }

    @Test
    @DisplayName("exportGoalsCSV: escreve cabeçalho default e fecha streams")
    void exportGoalsCSV_writesHeaderAndCloses() throws Exception {
        StructureLevel lvl = level("Objetivo");
        Plan plan = new Plan();
        plan.setName("Meu Plano");
        StructureLevelInstance s = sli(1L, "Meta A", lvl, plan, null, 75.0);

        ByteArrayOutputStream out = spy(new ByteArrayOutputStream());
        doNothing().when(out).close();

        CsvGoalExport spyExporter = spy(exporter);
        doNothing().when(spyExporter).writeLevelInstancesCSV(anyList(), anyList(), eq(false), any(Writer.class));

        spyExporter.exportGoalsCSV(List.of(s), out);

        verify(spyExporter).writeLevelInstancesCSV(anyList(), anyList(), eq(false), any(Writer.class));
        verify(out, atLeastOnce()).close();
    }

    @Test
    @DisplayName("writeLevelInstancesCSV: header por nível, ignora Attachment, parent/plano e escreve valores")
    void writeLevelInstancesCSV_fullFlow() throws Exception {
        StructureLevel lvl = level("Indicador");
        StructureLevel lvlParent = level("Objetivo");
        Plan plan = new Plan();
        plan.setName("Plano X");

        StructureLevelInstance parent = sli(10L, "Obj 1", lvlParent, plan, null, 60.0);
        StructureLevelInstance a = sli(11L, "Ind 1", lvl, plan, 10L, 80.5);
        StructureLevelInstance b = sli(12L, "Ind 2", lvl, plan, null, null);

        Attribute attrResp = attr("Resp", ResponsibleField.class.getCanonicalName());
        Attribute attrMan = attr("Gest", ManagerField.class.getCanonicalName());
        Attribute attrAP = attr("PAções", ActionPlanField.class.getCanonicalName());
        Attribute attrBud = attr("Orçamento", BudgetField.class.getCanonicalName());
        Attribute attrAtt = attr("Anexo", AttachmentField.class.getCanonicalName());
        Attribute attrTxt = attr("Texto", String.class.getCanonicalName());

        List<Attribute> attrs = List.of(attrResp, attrMan, attrAP, attrBud, attrAtt, attrTxt);

        when(structureBS.retrieveLevelAttributes(eq(lvl), anyList())).thenReturn(attrs);

        List<AttributeInstance> instValues = new ArrayList<>();
        instValues.add(ai(a, attrResp, "100"));
        instValues.add(ai(a, attrMan, "200"));
        instValues.add(ai(a, attrTxt, "Texto A"));
        instValues.add(ai(b, attrResp, "abc"));
        instValues.add(ai(b, attrTxt, null));
        when(structureBS.getAttributeInstacesValues(eq(List.of(a, b)), eq(attrs))).thenReturn(instValues);

        User u100 = new User();
        u100.setId(100L);
        u100.setName("Alice");
        User u200 = new User();
        u200.setId(200L);
        u200.setName("Bob");
        when(userBS.existsByUser(100L)).thenReturn(u100);
        when(userBS.existsByUser(200L)).thenReturn(u200);

        ActionPlan ap1 = new ActionPlan();
        ap1.setChecked(true);
        ap1.setDescription("Desc 1");
        ap1.setResponsible("R1");
        ap1.setBegin(new Date(0));
        ap1.setEnd(new Date(86_400_000L));
        ActionPlan ap2 = new ActionPlan();
        ap2.setChecked(false);
        ap2.setDescription("Desc 2");
        ap2.setResponsible("R2");
        ap2.setBegin(new Date(0));
        ap2.setEnd(new Date(86_400_000L));
        when(fieldsBS.listAllActionPlansByInstance(a)).thenReturn(List.of(ap1, ap2));
        when(fieldsBS.listAllActionPlansByInstance(b)).thenReturn(Collections.emptyList());

        Budget bud = new Budget();
        bud.setSubAction("1234");
        bud.setName("Dotação");
        bud.setCommitted(500.0);
        bud.setRealized(250.0);
        BudgetDTO dto = new BudgetDTO();
        dto.setBudget(bud);
        dto.setBudgetLoa(1000.5);
        dto.setBalanceAvailable(749.5);
        when(fieldsBS.getBudgets(a)).thenReturn(List.of(dto));
        when(fieldsBS.getBudgets(b)).thenReturn(Collections.emptyList());

        when(structureHelper.retrieveLevelInstance(10L)).thenReturn(parent);

        StringWriter writer = new StringWriter();

        exporter.writeLevelInstancesCSV(List.of(a, b), List.of("Resp", "Vencimento", "Esperado", "Alcançado", "Gestor"),
                true, writer);

        String csv = writer.toString();

        assertTrue(csv.startsWith("Indicador:\n"));
        assertTrue(csv.contains("\"Nome\";"));
        assertTrue(csv.contains("\"Objetivo\";"));
        assertTrue(csv.contains("\"Resp\";"));
        assertTrue(csv.contains("\"Gest\";"));
        assertTrue(csv.contains("\"PAções\";"));
        assertTrue(csv.contains("\"Orçamento\";"));
        assertFalse(csv.contains("Anexo"));
        assertTrue(csv.contains("\"Texto\";"));
        assertTrue(csv.contains("\"Rendimento\";"));
        assertTrue(csv.contains("\"Ind 1\";"));
        assertTrue(csv.contains("\"Obj 1\";"));
        assertTrue(csv.contains("\"Alice\";"));
        assertTrue(csv.contains("\"Bob\";"));
        assertTrue(csv.contains("Concluído: Sim"));
        assertTrue(csv.contains("Concluído: Não"));
        assertTrue(csv.contains("Ação Orçamentária: 1234"));
        assertTrue(csv.contains("\"80,5\";"));

        assertTrue(csv.contains("\"Ind 2\";"));
        assertTrue(csv.contains("\"Plano X\";"));
        assertTrue(csv.contains("\"\";"));
        assertTrue(csv.contains("-;"));
        assertTrue(csv.contains("-\n"));
    }

    @Test
    @DisplayName("resolveAttributeValue: Attachment → vazio; Default → helper; Responsible/Manager → via userBS; ActionPlan/Budget delegam a FieldsBS")
    void resolveAttributeValue_branches() throws Exception {
        StructureLevel lvl = level("Meta");
        Plan plan = new Plan();
        StructureLevelInstance s = sli(1L, "M1", lvl, plan, null, 10.0);

        Attribute att = attr("Anexo", AttachmentField.class.getCanonicalName());
        Attribute txt = attr("Texto", String.class.getCanonicalName());
        Attribute resp = attr("Resp", ResponsibleField.class.getCanonicalName());
        Attribute man = attr("Gest", ManagerField.class.getCanonicalName());
        Attribute ap = attr("PA", ActionPlanField.class.getCanonicalName());
        Attribute bud = attr("Orç", BudgetField.class.getCanonicalName());

        Map<Attribute, String> values = new LinkedHashMap<>();
        values.put(txt, "VAL");
        values.put(resp, "300");
        values.put(man, "400");

        User u300 = new User();
        u300.setId(300L);
        u300.setName("Carol");
        User u400 = new User();
        u400.setId(400L);
        u400.setName("Diego");
        when(userBS.existsByUser(300L)).thenReturn(u300);
        when(userBS.existsByUser(400L)).thenReturn(u400);

        when(fieldsBS.listAllActionPlansByInstance(s)).thenReturn(Collections.emptyList());
        when(fieldsBS.getBudgets(s)).thenReturn(Collections.emptyList());

        StringWriter w = new StringWriter();

        var m = CsvGoalExport.class.getDeclaredMethod("writeLevelInstanceValues",
                StructureLevelInstance.class, Map.class, List.class, Map.class, Boolean.class, Writer.class);
        m.setAccessible(true);
        List<Attribute> attrs = List.of(att, txt, resp, man, ap, bud);
        m.invoke(exporter, s, values, attrs, Collections.emptyMap(), false, w);

        String csv = w.toString();
        assertTrue(csv.contains("\"M1\";"));
        assertTrue(csv.contains("\"VAL\";"));
        assertTrue(csv.contains("\"Carol\";"));
        assertTrue(csv.contains("\"Diego\";"));
        assertTrue(csv.contains("\"\";\"\";"));
    }
}
