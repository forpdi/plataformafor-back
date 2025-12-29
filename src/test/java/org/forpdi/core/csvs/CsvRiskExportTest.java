package org.forpdi.core.csvs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.user.UserBS;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevel;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.plan.PlanRiskBS;
import org.forrisco.core.policy.Policy;
import org.forrisco.core.policy.PolicyBS;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskFilterParams;
import org.forrisco.risk.links.RiskAxis;
import org.forrisco.risk.links.RiskStrategy;
import org.forrisco.risk.monitor.MonitoringState;
import org.forrisco.risk.preventiveaction.PreventiveAction;
import org.forrisco.risk.preventiveaction.PreventiveActionBS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CsvRiskExportTest {

  private CsvRiskExport export;
  private CsvExportHelper helper;
  private PreventiveActionBS preventiveActionBS;
  private RiskBS riskBS;
  private StructureBS structureBS;
  private UserBS userBS;
  private AttributeHelper attrHelper;
  private PolicyBS policyBS;
  private UnitBS unitBS;
  private PlanRiskBS planRiskBS;

  private static void inject(Object target, String field, Object value) {
    try {
      Field f = target.getClass().getDeclaredField(field);
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  void setup() {
    export = new CsvRiskExport();
    helper = mock(CsvExportHelper.class);
    preventiveActionBS = mock(PreventiveActionBS.class);
    riskBS = mock(RiskBS.class);
    structureBS = mock(StructureBS.class);
    userBS = mock(UserBS.class);
    attrHelper = mock(AttributeHelper.class);
    policyBS = mock(PolicyBS.class);
    unitBS = mock(UnitBS.class);
    planRiskBS = mock(PlanRiskBS.class);

    inject(export, "helper", helper);
    inject(export, "preventiveActionBS", preventiveActionBS);
    inject(export, "riskBS", riskBS);
    inject(export, "structureBS", structureBS);
    inject(export, "userBS", userBS);
    inject(export, "attrHelper", attrHelper);
    inject(export, "policyBS", policyBS);
    inject(export, "unitBS", unitBS);
    inject(export, "planRiskBS", planRiskBS);

    when(helper.fromStringToCsvFieldFormat(anyString()))
        .thenAnswer(inv -> "\"" + (String) inv.getArgument(0) + "\",");
  }

  @Test
  @DisplayName("exportAxesRiskCSV: gera CSV com riscos, eixos, ações e estratégias (sem filtros)")
  void exportAxesRiskCSV_ok_no_filters() throws Exception {
    Long planId = 1L;
    Long selectedAxis = -1L;
    Integer selectedYear = -1;

    PlanRisk planRisk = mock(PlanRisk.class);
    when(policyBS.exists(planId, PlanRisk.class)).thenReturn(planRisk);

    Unit unit = mock(Unit.class);
    PaginatedList<Unit> unitsPage = new PaginatedList<>();
    unitsPage.setList(List.of(unit));
    when(unitBS.listUnitsbyPlanRisk(planRisk)).thenReturn(unitsPage);

    Risk risk = mock(Risk.class);
    when(risk.getName()).thenReturn("Risco X");
    when(risk.getManagerNameIfExists()).thenReturn("Gestor Y");
    var user = mock(org.forpdi.core.user.User.class);
    when(user.getName()).thenReturn("Tecnico Z");
    when(risk.getUser()).thenReturn(user);
    when(risk.getCode()).thenReturn("COD-1");
    when(risk.getReasonWithoutTags()).thenReturn("Causa A");
    when(risk.getResultWithoutTags()).thenReturn("Consequência B");
    when(risk.getProbability()).thenReturn("Alta");
    when(risk.getImpact()).thenReturn("Médio");
    when(risk.getPeriodicity()).thenReturn("Mensal");
    when(risk.getType()).thenReturn("Ameaça");
    Date begin = new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-15");
    when(risk.getBegin()).thenReturn(begin);
    when(risk.getRiskResponseLabel()).thenReturn("Mitigar");
    when(risk.getRiskLevelLabel()).thenReturn("ALTO");

    PaginatedList<Risk> risksPage = new PaginatedList<>();
    risksPage.setList(List.of(risk));
    when(riskBS.listRiskByUnit(unit)).thenReturn(risksPage);

    StructureLevelInstance axisInstance = mock(StructureLevelInstance.class);
    RiskAxis axis = mock(RiskAxis.class);
    when(axis.getName()).thenReturn("Eixo 1");
    when(axis.getRisk()).thenReturn(risk);
    when(axis.getStructure()).thenReturn(axisInstance);
    PaginatedList<RiskAxis> axesPage = new PaginatedList<>();
    axesPage.setList(List.of(axis));
    when(riskBS.listRiskAxis(risk)).thenReturn(axesPage);

    PreventiveAction action = mock(PreventiveAction.class);
    when(action.getRisk()).thenReturn(risk);
    when(action.getAction()).thenReturn("Treinar equipe");
    when(action.getManager()).thenReturn(user);
    when(action.getUser()).thenReturn(user);
    when(action.isAccomplishedDescription()).thenReturn("Não");
    when(action.getValidityBegin()).thenReturn(begin);
    when(action.getValidityEnd()).thenReturn(begin);

    PaginatedList<PreventiveAction> actionsPage = new PaginatedList<>();
    actionsPage.setList(List.of(action));
    when(preventiveActionBS.listPreventiveActionByRisk(risk)).thenReturn(actionsPage);

    StructureLevelInstance strategyInstance = mock(StructureLevelInstance.class);
    RiskStrategy strategy = mock(RiskStrategy.class);
    when(strategy.getRisk()).thenReturn(risk);
    when(strategy.getStructure()).thenReturn(strategyInstance);
    when(strategy.getName()).thenReturn("Objetivo 1");
    PaginatedList<RiskStrategy> strategiesPage = new PaginatedList<>();
    strategiesPage.setList(List.of(strategy));
    when(riskBS.listRiskStrategy(risk)).thenReturn(strategiesPage);

    PaginatedList<Attribute> emptyAttrs = new PaginatedList<>();
    emptyAttrs.setList(List.of());
    StructureLevel level = mock(StructureLevel.class);
    when(axisInstance.getLevel()).thenReturn(level);
    when(strategyInstance.getLevel()).thenReturn(level);
    when(structureBS.listAttributes(level)).thenReturn(emptyAttrs);

    OutputStream os = new ByteArrayOutputStream();
    export.exportAxesRiskCSV(planId, selectedAxis, selectedYear, os);
    String csv = os.toString();

    assertTrue(csv.contains("Riscos"));
    assertTrue(csv.contains("\"Risco X\","));
    assertTrue(csv.contains("Eixos temáticos"));
    assertTrue(csv.contains("\"Eixo 1\","));
    assertTrue(csv.contains("Ações de prevenção"));
    assertTrue(csv.contains("\"Treinar equipe\","));
    assertTrue(csv.contains("Objetivos estratégicos"));
    assertTrue(csv.contains("\"Objetivo 1\","));
  }

  @Test
  @DisplayName("exportAxesRiskCSV: aplica filtros selectedAxis e selectedYear")
  void exportAxesRiskCSV_filters() throws Exception {
    Long planId = 1L;
    Long selectedAxis = 999L;
    Integer selectedYear = 2024;

    PlanRisk planRisk = mock(PlanRisk.class);
    when(policyBS.exists(planId, PlanRisk.class)).thenReturn(planRisk);
    Unit unit = mock(Unit.class);
    PaginatedList<Unit> unitsPage = new PaginatedList<>();
    unitsPage.setList(List.of(unit));
    when(unitBS.listUnitsbyPlanRisk(planRisk)).thenReturn(unitsPage);

    Risk risk = mock(Risk.class);
    when(risk.getName()).thenReturn("Risco Filtrado");
    when(risk.getManagerNameIfExists()).thenReturn("");
    var user = mock(org.forpdi.core.user.User.class);
    when(user.getName()).thenReturn("Tec");
    when(risk.getUser()).thenReturn(user);
    when(risk.getCode()).thenReturn("C-2");
    when(risk.getReasonWithoutTags()).thenReturn("x");
    when(risk.getResultWithoutTags()).thenReturn("y");
    when(risk.getProbability()).thenReturn("Baixa");
    when(risk.getImpact()).thenReturn("Alto");
    when(risk.getPeriodicity()).thenReturn("Trimestral");
    when(risk.getType()).thenReturn("Oportunidade");
    Date begin = new SimpleDateFormat("yyyy-MM-dd").parse("2024-05-10");
    when(risk.getBegin()).thenReturn(begin);
    when(risk.getRiskResponseLabel()).thenReturn("Aceitar");
    when(risk.getRiskLevelLabel()).thenReturn("MÉDIO");

    PaginatedList<Risk> risksPage = new PaginatedList<>();
    risksPage.setList(List.of(risk));
    when(riskBS.listRiskByUnit(unit)).thenReturn(risksPage);

    StructureLevelInstance axis1 = mock(StructureLevelInstance.class);
    when(axis1.getId()).thenReturn(999L);
    StructureLevelInstance axis2 = mock(StructureLevelInstance.class);
    when(axis2.getId()).thenReturn(100L);

    RiskAxis a1 = mock(RiskAxis.class);
    when(a1.getName()).thenReturn("Eixo OK");
    when(a1.getRisk()).thenReturn(risk);
    when(a1.getStructure()).thenReturn(axis1);

    RiskAxis a2 = mock(RiskAxis.class);
    when(a2.getName()).thenReturn("Eixo Fora");
    when(a2.getRisk()).thenReturn(risk);
    when(a2.getStructure()).thenReturn(axis2);

    PaginatedList<RiskAxis> axesPage = new PaginatedList<>();
    axesPage.setList(List.of(a1, a2));
    when(riskBS.listRiskAxis(risk)).thenReturn(axesPage);

    PreventiveAction acOK = mock(PreventiveAction.class);
    when(acOK.getRisk()).thenReturn(risk);
    when(acOK.getAction()).thenReturn("Ação 2024");
    when(acOK.getUser()).thenReturn(user);
    when(acOK.isAccomplishedDescription()).thenReturn("Não");
    Date begin2024 = new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-01");
    when(acOK.getValidityBegin()).thenReturn(begin2024);
    when(acOK.getValidityEnd()).thenReturn(begin2024);

    PreventiveAction acOut = mock(PreventiveAction.class);
    when(acOut.getRisk()).thenReturn(risk);
    when(acOut.getAction()).thenReturn("Ação 2023");
    when(acOut.getUser()).thenReturn(user);
    when(acOut.isAccomplishedDescription()).thenReturn("Não");
    Date begin2023 = new SimpleDateFormat("yyyy-MM-dd").parse("2023-01-01");
    when(acOut.getValidityBegin()).thenReturn(begin2023);
    when(acOut.getValidityEnd()).thenReturn(begin2023);

    PaginatedList<PreventiveAction> acts = new PaginatedList<>();
    acts.setList(List.of(acOK, acOut));
    when(preventiveActionBS.listPreventiveActionByRisk(risk)).thenReturn(acts);

    RiskStrategy st = mock(RiskStrategy.class);
    StructureLevelInstance stInst = mock(StructureLevelInstance.class);
    when(st.getStructure()).thenReturn(stInst);
    when(st.getRisk()).thenReturn(risk);
    when(st.getName()).thenReturn("Estratégia X");
    PaginatedList<RiskStrategy> stPage = new PaginatedList<>();
    stPage.setList(List.of(st));
    when(riskBS.listRiskStrategy(risk)).thenReturn(stPage);

    PaginatedList<Attribute> emptyAttrs = new PaginatedList<>();
    emptyAttrs.setList(List.of());
    StructureLevel level = mock(StructureLevel.class);
    when(axis1.getLevel()).thenReturn(level);
    when(axis2.getLevel()).thenReturn(level);
    when(stInst.getLevel()).thenReturn(level);
    when(structureBS.listAttributes(level)).thenReturn(emptyAttrs);

    OutputStream os = new ByteArrayOutputStream();
    export.exportAxesRiskCSV(planId, selectedAxis, selectedYear, os);
    String csv = os.toString();
    assertTrue(csv.contains("\"Eixo OK\","), "Eixo com id=999 precisa aparecer");
    assertFalse(csv.contains("\"Eixo Fora\","), "Eixo com id!=999 deve ser filtrado");

    assertTrue(csv.contains("\"Ação 2024\","), "Ação do ano filtrado precisa aparecer");
    assertFalse(csv.contains("\"Ação 2023\","), "Ação fora do ano não deve aparecer");
  }

  @Test
  @DisplayName("exportRiskCsv: OK gera cabeçalhos e uma linha de risco")
  void exportRiskCsv_ok() throws Exception {
    Long planRiskId = 5L;
    PlanRisk planRisk = mock(PlanRisk.class);
    when(planRiskBS.exists(planRiskId, PlanRisk.class)).thenReturn(planRisk);

    Policy policy = mock(Policy.class);
    when(policy.getName()).thenReturn("Política ABC");
    when(planRisk.getPolicy()).thenReturn(policy);
    when(planRisk.getName()).thenReturn("Plano DEF");

    Unit parent = mock(Unit.class);
    when(parent.getName()).thenReturn("Unidade Mãe");

    Unit subunit = mock(Unit.class);
    when(subunit.isSubunit()).thenReturn(true);
    when(subunit.getParent()).thenReturn(parent);
    when(subunit.getName()).thenReturn("Sub A");

    Risk r = mock(Risk.class);
    when(r.getName()).thenReturn("Risco 1");
    when(r.getCode()).thenReturn("RX-1");
    when(r.getType()).thenReturn("Ameaça");
    when(r.getRiskResponseLabel()).thenReturn("Mitigar");
    when(r.getTipologiesFomatted()).thenReturn("Operacional;Legal");
    when(r.getUnit()).thenReturn(subunit);
    Date begin = new SimpleDateFormat("yyyy-MM-dd").parse("2025-02-02");
    when(r.getBegin()).thenReturn(begin);
    when(r.getMonitoringState()).thenReturn(MonitoringState.UP_TO_DATE.ordinal());

    when(riskBS.filterRisks(eq(planRiskId), any(RiskFilterParams.class))).thenReturn(List.of(r));

    doAnswer(inv -> null).when(riskBS).setRisksMonitoringState(anyList());

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    export.exportRiskCsv(planRiskId, new RiskFilterParams(), os);
    String csv = os.toString();

    assertTrue(csv.contains("Política"));
    assertTrue(csv.contains("\"Política ABC\","));
    assertTrue(csv.contains("\"Plano DEF\","));

    assertTrue(csv.contains("Nome do risco"));
    assertTrue(csv.contains("\"Risco 1\","));
    assertTrue(csv.contains("\"Unidade Mãe\","));
    assertTrue(csv.contains("\"Sub A\","));
    assertTrue(csv.contains(MonitoringState.UP_TO_DATE.getReportLabel()));
    verify(riskBS).setRisksMonitoringState(anyList());
  }

  @Test
  @DisplayName("exportRiskCsv: lança IllegalArgumentException quando plano não existe")
  void exportRiskCsv_plan_not_found() throws Exception {
    when(planRiskBS.exists(99L, PlanRisk.class)).thenReturn(null);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    assertThrows(IllegalArgumentException.class,
        () -> export.exportRiskCsv(99L, new RiskFilterParams(), os));
  }
}
