package org.forpdi.system.reports.frisco;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

import com.itextpdf.text.Paragraph;
import org.forpdi.system.reports.ReportGeneratorHelper;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.risk.RiskLevel;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.Risk;
import org.forrisco.risk.monitor.MonitorBS;
import org.forrisco.risk.monitor.MonitorHistory;
import org.forrisco.risk.monitor.MonitoringState;
import org.forrisco.risk.typology.RiskTypologyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BoardReportGeneratorTest {

  private BoardReportGenerator sutWith(Object... fieldPairs) {
    BoardReportGenerator sut = new BoardReportGenerator();
    for (int i = 0; i < fieldPairs.length; i += 2) {
      String name = (String) fieldPairs[i];
      Object value = fieldPairs[i + 1];
      try {
        var f = BoardReportGenerator.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(sut, value);
      } catch (Exception e) { throw new RuntimeException(e); }
    }
    return sut;
  }

  private Object call(Object target, String name, Class<?>[] types, Object... args) {
    try {
      Method m = target.getClass().getDeclaredMethod(name, types);
      m.setAccessible(true);
      return m.invoke(target, args);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  private Risk risk(String type, String prob, String impact, RiskLevel level, String tipologyRaw) {
    Risk r = mock(Risk.class);
    when(r.getType()).thenReturn(type);
    when(r.getProbability()).thenReturn(prob);
    when(r.getImpact()).thenReturn(impact);
    when(r.getRiskLevel()).thenReturn(level);
    when(r.getTipology()).thenReturn(tipologyRaw);
    return r;
  }

  @Test
  @DisplayName("countRisksOccurrences: por probabilidade/impacto/tipo")
  void countRisksOccurrences_ok() {
    ReportGeneratorHelper helper = mock(ReportGeneratorHelper.class);
    BoardReportGenerator sut = sutWith("reportGeneratorHelper", helper);

    RiskLevel any = mock(RiskLevel.class);
    List<Risk> risks = List.of(
        risk("Ameaça", "P1", "I1", any, ""),
        risk("Ameaça", "P1", "I1", any, ""),
        risk("Ameaça", "P2", "I1", any, ""),
        risk("Oportunidade", "P1", "I1", any, "")
    );

    String c = (String) call(sut, "countRisksOccurrences",
        new Class[]{List.class, String.class, String.class, String.class},
        risks, "Ameaça", "P1", "I1");
    assertEquals("2", c);
  }

  @Test
  @DisplayName("countRiskQuantity: por nível e tipo")
  void countRiskQuantity_ok() {
    ReportGeneratorHelper helper = mock(ReportGeneratorHelper.class);
    BoardReportGenerator sut = sutWith("reportGeneratorHelper", helper);

    RiskLevel a = mock(RiskLevel.class);
    RiskLevel b = mock(RiskLevel.class);

    List<Risk> risks = List.of(
        risk("Oportunidade", "P", "I", a, ""),
        risk("Oportunidade", "P", "I", a, ""),
        risk("Oportunidade", "P", "I", b, ""),
        risk("Ameaça", "P", "I", a, "")
    );

    String c = (String) call(sut, "countRiskQuantity",
        new Class[]{List.class, String.class, RiskLevel.class},
        risks, "Oportunidade", a);
    assertEquals("2", c);
  }

  @Test
  @DisplayName("countRiskTipologies: mapeia strings para enums e conta")
  void countRiskTipologies_mapsAndCounts() {
    ReportGeneratorHelper helper = mock(ReportGeneratorHelper.class);
    BoardReportGenerator sut = sutWith("reportGeneratorHelper", helper);

    // usa valores reais do enum
    String tips = RiskTypologyType.OPERATIONAL.getValue() + ";" + RiskTypologyType.LEGAL.getValue();
    Risk r1 = risk("Ameaça", "P", "I", mock(RiskLevel.class), tips);
    Risk r2 = risk("Ameaça", "P", "I", mock(RiskLevel.class), RiskTypologyType.OPERATIONAL.getValue());

    @SuppressWarnings("unchecked")
    Map<String, Map<RiskTypologyType, org.forpdi.core.utils.Counter>> m =
        (Map<String, Map<RiskTypologyType, org.forpdi.core.utils.Counter>>) call(
            sut, "countRiskTipologies", new Class[]{List.class}, List.of(r1, r2)
        );

    var amea = m.get("Ameaça");
    assertNotNull(amea);
    assertEquals(2, amea.get(RiskTypologyType.OPERATIONAL).getCounter());
    assertEquals(1, amea.get(RiskTypologyType.LEGAL).getCounter());
  }

  @Test
  @DisplayName("generateEmptyIncidentCounterMap: 12 meses com zero")
  void emptyIncidentCounterMap_12() {
    BoardReportGenerator sut = sutWith();
    @SuppressWarnings("unchecked")
    Map<Integer, org.forpdi.core.utils.Counter> map =
        (Map<Integer, org.forpdi.core.utils.Counter>) call(sut, "generateEmptyIncidentCounterMap", new Class[]{});
    assertEquals(12, map.size());
    assertTrue(map.values().stream().allMatch(c -> c.getCounter() == 0));
    assertTrue(map.keySet().stream().allMatch(m -> m >= 1 && m <= 12));
  }

  @Test
  @DisplayName("getDateLimitToListIncidents: ~1 ano atrás")
  void dateLimit_aboutOneYear() {
    BoardReportGenerator sut = sutWith();
    Date now = new Date();
    Date limit = (Date) call(sut, "getDateLimitToListIncidents", new Class[]{});
    long days = Math.abs((now.getTime() - limit.getTime()) / (1000 * 60 * 60 * 24));
    assertTrue(days >= 360 && days <= 370, "dif = " + days + " dias");
  }

  @Test
  @DisplayName("getMonitorHistoryMapped: agrupa por estado e soma quantidades")
  void monitorHistoryMapped_groupsAndSums() {
    MonitorBS monitorBS = mock(MonitorBS.class);
    ReportGeneratorHelper helper = mock(ReportGeneratorHelper.class);
    BoardReportGenerator sut = sutWith("monitorBS", monitorBS, "reportGeneratorHelper", helper);

    Unit u = mock(Unit.class);
    MonitorHistory h1 = mock(MonitorHistory.class);
    when(h1.getEstado()).thenReturn("em dia");
    when(h1.getQuantity()).thenReturn(5);
    MonitorHistory h2 = mock(MonitorHistory.class);
    when(h2.getEstado()).thenReturn("atrasado");
    when(h2.getQuantity()).thenReturn(3);

    when(monitorBS.listLastMonitorHistoryByUnits(anyList())).thenReturn(List.of(h1, h2));

    @SuppressWarnings("unchecked")
    Map<MonitoringState, org.forpdi.core.utils.Counter> map =
        (Map<MonitoringState, org.forpdi.core.utils.Counter>) call(
            sut, "getMonitorHistoryMapped", new Class[]{List.class}, List.of(u)
        );

    assertEquals(5, map.get(MonitoringState.UP_TO_DATE).getCounter());
    assertEquals(3, map.get(MonitoringState.LATE).getCounter());
  }

  @Test
  @DisplayName("generatePreText: escreve PDF com plano e unidades")
  void generatePreText_writesPdf(@TempDir File tmp) throws Exception {
    ReportGeneratorHelper helper = mock(ReportGeneratorHelper.class);
    when(helper.newSectionTitle(anyString())).thenReturn(new Paragraph("title"));
    when(helper.listItem(anyString())).thenReturn(new Paragraph("item"));

    BoardReportGenerator sut = sutWith("reportGeneratorHelper", helper);

    File pre = new File(tmp, "pretext.pdf");
    PlanRisk plan = mock(PlanRisk.class); when(plan.getName()).thenReturn("Plano X");
    Unit u1 = mock(Unit.class); when(u1.getName()).thenReturn("U1");
    Unit u2 = mock(Unit.class); when(u2.getName()).thenReturn("U2");

    assertDoesNotThrow(() -> call(
        sut, "generatePreText", new Class[]{File.class, PlanRisk.class, List.class},
        pre, plan, List.of(u1, u2)
    ));

    assertTrue(pre.exists() && pre.length() > 0);
  }
}
