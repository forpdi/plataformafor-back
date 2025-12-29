package org.forpdi.core.csvs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.Structure;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class CsvPlanExportTest {

    @InjectMocks
    private CsvPlanExport exporter;

    @Mock
    private CsvExportHelper helper;

    @Mock
    private CsvGoalExport csvGoalsExport;

    @Mock
    private StructureBS structureBS;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        lenient().when(helper.fromStringToCsvFieldFormat(anyString()))
                .thenAnswer(inv -> "\"" + inv.getArgument(0, String.class) + "\";");

        lenient().when(helper.fromDoubleToCsvPerformanceFormat(any()))
                .thenAnswer(inv -> {
                    Double d = inv.getArgument(0);
                    return d == null ? null : String.valueOf(d);
                });
    }

    private static PlanMacro planMacro(long id, String name, Date begin, Date end, String description) {
        PlanMacro pm = new PlanMacro();
        pm.setId(id);
        pm.setName(name);
        pm.setBegin(begin);
        pm.setEnd(end);
        pm.setDescription(description);
        return pm;
    }

    private static Plan plan(PlanMacro pm, String name, String description,
                             Date begin, Date end, String structureName, Double performance) {
        Plan p = new Plan();
        p.setParent(pm);
        p.setName(name);
        p.setDescription(description);
        p.setBegin(begin);
        p.setEnd(end);

        Structure st = new Structure();
        st.setName(structureName);
        p.setStructure(st);

        p.setPerformance(performance);
        return p;
    }

    @Test
    @DisplayName("exportCSV: escreve blocos de Plano Macro e Plano de Ação e delega goals para CsvGoalExport")
    void exportCSV_happyPath() throws Exception {
        Date now = new Date();

        PlanMacro pm = planMacro(100L, "Plano Macro X", now, now, "Descrição Macro");
        Plan p1 = plan(pm, "Plano A", "Desc A", now, now, "Estrutura 1", 75.5);
        Plan p2 = plan(pm, "Plano B", "Desc B", now, now, "Estrutura 2", 33.0);

        StructureLevelInstance sli1 = new StructureLevelInstance(); sli1.setId(10L);
        StructureLevelInstance sli2 = new StructureLevelInstance(); sli2.setId(20L);
        when(structureBS.listAllLevelInstanceByPlan(eq(p1), eq(false))).thenReturn(List.of(sli1));
        when(structureBS.listAllLevelInstanceByPlan(eq(p2), eq(false))).thenReturn(List.of(sli2));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        exporter.exportCSV(List.of(p1, p2), out);

        String csv = out.toString(StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"Id\";\"Plano Macro\";\"Data de Início\";\"Data de Término\";\"Descrição\";"),
                "Deveria conter cabeçalho do Plano Macro");
        assertTrue(csv.contains("\"100\";"), "Deveria conter o id do Plano Macro");
        assertTrue(csv.contains("\"Plano Macro X\";"), "Deveria conter o nome do Plano Macro");
        assertTrue(csv.contains("\"Descrição Macro\";"), "Deveria conter a descrição do Plano Macro");

        assertTrue(csv.contains("\"Plano de Ação:\";"), "Deveria conter o título do bloco de plano de ação");
        assertTrue(csv.contains("\"Nome\";\"Descrição do Plano\";\"Data de Início\";\"Data de Término\";\"Estrutura do Plano\";\"Rendimento\";"),
                "Cabeçalho do bloco de Planos");

        assertTrue(csv.contains("\"Plano A\";"));
        assertTrue(csv.contains("\"Desc A\";"));
        assertTrue(csv.contains("\"Estrutura 1\";"));
        assertTrue(csv.contains("\"75.5\";"));

        assertTrue(csv.contains("\"Plano B\";"));
        assertTrue(csv.contains("\"Desc B\";"));
        assertTrue(csv.contains("\"Estrutura 2\";"));
        assertTrue(csv.contains("\"33.0\";"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StructureLevelInstance>> cap = ArgumentCaptor.forClass(List.class);
        verify(csvGoalsExport, times(1)).writeLevelInstancesCSV(cap.capture(), isNull(), eq(true), any());
        List<StructureLevelInstance> passed = cap.getValue();
        assertEquals(2, passed.size(), "Deveria passar os SLI dos dois planos.");
        assertTrue(passed.stream().anyMatch(s -> Long.valueOf(10L).equals(s.getId())));
        assertTrue(passed.stream().anyMatch(s -> Long.valueOf(20L).equals(s.getId())));
    }

    @Test
    @DisplayName("exportCSV: roda com strings nulas (nome/descrição), desde que datas/estrutura não sejam nulos")
    void exportCSV_withNullStringsButRequiredFieldsPresent() throws Exception {
        Date now = new Date();

        PlanMacro pm = planMacro(200L, null, now, now, null);
        Plan p = plan(pm, null, null, now, now, "Estrutura Z", null);

        when(structureBS.listAllLevelInstanceByPlan(eq(p), eq(false))).thenReturn(List.of());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.exportCSV(List.of(p), out);
        String csv = out.toString(StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"Id\";\"Plano Macro\";"), "Cabeçalho do Plano Macro presente");
        assertTrue(csv.contains("\"200\";"), "Id do Plano Macro presente");
        assertTrue(csv.contains("\"Plano de Ação:\";"));
        assertTrue(csv.contains("\"Estrutura Z\";"));
    }
}
