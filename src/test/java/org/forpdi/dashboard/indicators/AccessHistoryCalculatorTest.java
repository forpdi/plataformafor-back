package org.forpdi.dashboard.indicators;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.forpdi.dashboard.indicators.CompanyAccessHistory.FPDI_ACCESS_IDX;
import static org.forpdi.dashboard.indicators.CompanyAccessHistory.FRISCO_ACCESS_IDX;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import org.forpdi.core.company.Company;
import org.forpdi.system.accesslog.AccessLogHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class AccessHistoryCalculatorTest {

  // ---- helpers -------------------------------------------------------------

  private AccessLogHistory alh(long companyId, String isoLocalDate, int fpdi, int frisco) {
    AccessLogHistory mock = mock(AccessLogHistory.class);
    when(mock.getCompanyId()).thenReturn(companyId);
    when(mock.getDate()).thenReturn(LocalDate.parse(isoLocalDate));
    when(mock.getFpdiAccessCount()).thenReturn(fpdi);
    when(mock.getFriscoAccessCount()).thenReturn(frisco);
    return mock;
  }

  private Company company(long id) {
    Company c = mock(Company.class);
    when(c.getId()).thenReturn(id);
    return c;
  }

  private Date date(String yyyyMMdd) {
    try {
      return new SimpleDateFormat("yyyy-MM-dd").parse(yyyyMMdd);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  // ---- tests ---------------------------------------------------------------

  @Test
  @DisplayName("getAccessHistoryResult: agrupa por empresa e preenche datas faltantes")
  void getAccessHistoryResult_fillsMissingDatesAndGroups() {
    // company 1 tem registros em 01 e 03 -> deve preencher 02 com [0,0]
    var list = List.of(
        alh(1L, "2025-01-01", 2, 0),
        alh(1L, "2025-01-03", 0, 5),
        alh(2L, "2025-01-02", 7, 1)
    );

    var calc = new AccessHistoryCalculator(list);
    var result = calc.getAccessHistoryResult();

    assertNotNull(result);
    var histories = result.getCompaniesAccessHistory();
    assertEquals(2, histories.size(), "Deve haver 2 empresas");

    // empresa 1
    var c1 = histories.stream().filter(h -> h.getCompanyId() == 1L).findFirst().orElseThrow();
    Map<String, Integer[]> h1 = c1.getHistory();

    assertTrue(h1.containsKey("2025-01-01"));
    assertTrue(h1.containsKey("2025-01-02")); // preenchido automaticamente
    assertTrue(h1.containsKey("2025-01-03"));

    assertArrayEquals(new Integer[]{2,0}, h1.get("2025-01-01"));
    assertArrayEquals(new Integer[]{0,0}, h1.get("2025-01-02"));
    assertArrayEquals(new Integer[]{0,5}, h1.get("2025-01-03"));

    // empresa 2
    var c2 = histories.stream().filter(h -> h.getCompanyId() == 2L).findFirst().orElseThrow();
    Map<String, Integer[]> h2 = c2.getHistory();
    assertEquals(1, h2.size());
    assertArrayEquals(new Integer[]{7,1}, h2.get("2025-01-02"));
  }

  @Test
  @DisplayName("getAccessHistoryResultOneByMonth: marca 1 por mês e produto, sem duplicar dias")
  void getAccessHistoryResultOneByMonth_monthBins() {
    // Mesma empresa, mesmo mês:
    // - FPDI acessado em 05 e 12 -> deve registrar 1 apenas uma vez
    // - FRISCO acessado só em 20 -> 1 uma vez
    var list = List.of(
        alh(10L, "2025-03-05", 3, 0),
        alh(10L, "2025-03-12", 2, 0),
        alh(10L, "2025-03-20", 0, 4),
        // mês diferente: abril
        alh(10L, "2025-04-01", 1, 1),
        // empresa diferente
        alh(11L, "2025-03-02", 0, 2)
    );

    var calc = new AccessHistoryCalculator(list);
    var result = calc.getAccessHistoryResultOneByMonth();
    var histories = result.getCompaniesAccessHistory();

    var h10 = histories.stream().filter(h -> h.getCompanyId() == 10L).findFirst().orElseThrow().getHistory();
    // Em março deve existir apenas uma entrada por dia onde houve 1ª marcação,
    // mas o importante é que os valores sejam 1 para cada produto no mês.
    // Como a implementação grava por dia, vamos somar o mês para garantir que não duplicou a marcação.
    int marchFpdi = h10.entrySet().stream()
        .filter(e -> e.getKey().startsWith("2025-03"))
        .mapToInt(e -> e.getValue()[FPDI_ACCESS_IDX]).sum();
    int marchFrisco = h10.entrySet().stream()
        .filter(e -> e.getKey().startsWith("2025-03"))
        .mapToInt(e -> e.getValue()[FRISCO_ACCESS_IDX]).sum();

    assertEquals(1, marchFpdi, "FPDI deve marcar 1 vez em março");
    assertEquals(1, marchFrisco, "FRISCO deve marcar 1 vez em março");

    int aprilFpdi = h10.entrySet().stream()
        .filter(e -> e.getKey().startsWith("2025-04"))
        .mapToInt(e -> e.getValue()[FPDI_ACCESS_IDX]).sum();
    int aprilFrisco = h10.entrySet().stream()
        .filter(e -> e.getKey().startsWith("2025-04"))
        .mapToInt(e -> e.getValue()[FRISCO_ACCESS_IDX]).sum();

    assertEquals(1, aprilFpdi, "FPDI deve marcar 1 em abril");
    assertEquals(1, aprilFrisco, "FRISCO deve marcar 1 em abril");

    var h11 = histories.stream().filter(h -> h.getCompanyId() == 11L).findFirst().orElseThrow().getHistory();
    int marchFrisco_11 = h11.entrySet().stream()
        .filter(e -> e.getKey().startsWith("2025-03"))
        .mapToInt(e -> e.getValue()[FRISCO_ACCESS_IDX]).sum();
    assertEquals(1, marchFrisco_11, "FRISCO empresa 11 deve marcar 1 em março");
  }

  @Nested
  @DisplayName("AccessHistoryResult.getCompaniesAccessHistoryFilteredByPeriod")
  class FilteredByPeriodTests {

    @Test
    @DisplayName("Soma FPDI/FRISCO dentro da janela, ignora fora, inclui empresas sem histórico")
    void sumsInsideWindow_andKeepsEmptyCompanies() {
      // Históricos já montados via getAccessHistoryResult()
      var list = List.of(
          // empresa 1: dois dias dentro da janela
          alh(1L, "2025-01-10", 2, 1),
          alh(1L, "2025-01-12", 3, 4),
          // empresa 2: um dia fora da janela
          alh(2L, "2024-12-31", 9, 9)
      );
      var calc = new AccessHistoryCalculator(list);
      var base = calc.getAccessHistoryResult(); // history por dia

      // empresas consideradas
      var companies = List.of(company(1L), company(2L), company(3L)); // 3L sem histórico -> deve vir 0,0

      // janela [2025-01-01 .. 2025-01-31]
      Date begin = date("2025-01-01");
      Date end   = date("2025-01-31");

      Map<Long, Integer[]> filtered = base.getCompaniesAccessHistoryFilteredByPeriod(companies, begin, end);

      assertEquals(3, filtered.size());

      // empresa 1: soma 2+3 para FPDI e 1+4 para FRISCO
      assertArrayEquals(new Integer[]{5,5}, filtered.get(1L));

      // empresa 2: único registro é 2024-12-31 (fora da janela) -> 0,0
      assertArrayEquals(new Integer[]{0,0}, filtered.get(2L));

      // empresa 3: sem histórico -> 0,0
      assertArrayEquals(new Integer[]{0,0}, filtered.get(3L));
    }

    @Test
    @DisplayName("Limites são inclusivos (isAfterOrEqual / isBeforeOrEqual)")
    void boundariesInclusive() {
      var list = List.of(
          alh(5L, "2025-02-01", 1, 0), // limite inferior
          alh(5L, "2025-02-15", 2, 3),
          alh(5L, "2025-02-28", 0, 4)  // limite superior
      );
      var calc = new AccessHistoryCalculator(list);
      var base = calc.getAccessHistoryResult();

      var companies = List.of(company(5L));
      Map<Long, Integer[]> filtered = base.getCompaniesAccessHistoryFilteredByPeriod(
          companies, date("2025-02-01"), date("2025-02-28")
      );

      assertArrayEquals(new Integer[]{3,7}, filtered.get(5L),
          "Deve incluir os registros exatamente nos limites da janela");
    }
  }
}
