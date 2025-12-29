package org.forrisco.risk.history;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.forpdi.core.user.User;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiskEditHistoryBSTest {

    private RiskEditHistoryBS service;
    private List<RiskEditHistory> saved;

    @BeforeEach
    void setup() {
        service = spy(new RiskEditHistoryBS());
        saved = Collections.synchronizedList(new ArrayList<>());

        doAnswer(inv -> { saved.add((RiskEditHistory) inv.getArgument(0)); return null; })
            .when(service).persist(any());
    }

    @Test
    @DisplayName("saveRiskChanges sem permissão ignora Nome, Código, Gestor e Responsável Técnico")
    void saveRiskChanges_withoutPermission_skipsPrivilegedFields() {
        User oldUser    = mockUser(1L, "Alice");
        User newUser    = mockUser(2L, "Bob");
        User oldManager = mockUser(3L, "Gerente A");
        User newManager = mockUser(4L, "Gerente B");
        Unit oldUnit    = mockUnit(10L, "Unidade A");
        Unit newUnit    = mockUnit(11L, "Unidade B");
        RiskLevel oldLevel = mockRiskLevel(100L, "BAIXO");
        RiskLevel newLevel = mockRiskLevel(101L, "ALTO");

        Date oldBegin = date(2024, 1, 10);
        Date newBegin = date(2025, 2, 1);

        Risk oldRisk = mockRisk(
            "Risco 1","COD-001",
            "<p>Causa A</p>","<i>Conseq A</i>",
            "Baixa","Médio","Mensal",
            "Financeiro","X","Estratégico",
            false,false,false,true,false,
            oldBegin,"http://a", oldUser, oldManager, oldUnit, oldLevel,
            null, null
        );
        Risk newRisk = mockRisk(
            "Risco 1 alterado","COD-002",
            "<b>Causa B</b>","Conseq B",
            "Alta","Alto","Semanal",
            "Operacional","Y","Operacional",
            true,true,true,false,true,
            newBegin,"http://b", oldUser, oldManager, oldUnit, oldLevel,
            null, null
        );
        User auditor = mockUser(99L, "Auditor");

        service.saveRiskChanges(oldRisk, newRisk, newUnit, newUser, newManager, newLevel, false, auditor);

        Set<String> fields = fieldNames(saved);
        Set<String> expected = Set.of(
            "Causa","Consequência","Probabilidade","Impacto","Periodicidade",
            "Tipologia","Outras Tipologias","Tipo",
            "Risco PDI","Risco por Eixo do PDI","Risco por Objetivo de Processo","Risco por Atividade de Processo",
            "Início","Link ForPDI","Unidade","Grau de Risco","Arquivado"
        );
        assertEquals(expected, fields,
            "Campos privilegiados não devem ser registrados quando hasPermission=false");

        RiskEditHistory causa = byField(saved, "Causa");
        assertEquals("Causa A", causa.getOldValue());
        assertEquals("Causa B", causa.getNewValue());

        RiskEditHistory riscoPdi = byField(saved, "Risco PDI");
        assertEquals("Não", riscoPdi.getOldValue());
        assertEquals("Sim", riscoPdi.getNewValue());

        RiskEditHistory inicio = byField(saved, "Início");
        assertEquals("10/01/2024", inicio.getOldValue());
        assertEquals("01/02/2025", inicio.getNewValue());

        RiskEditHistory unidade = byField(saved, "Unidade");
        assertEquals("Unidade A", unidade.getOldValue());
        assertEquals("Unidade B", unidade.getNewValue());

        RiskEditHistory grau = byField(saved, "Grau de Risco");
        assertEquals("BAIXO", grau.getOldValue());
        assertEquals("ALTO", grau.getNewValue());

        assertFalse(fields.contains("Nome"));
        assertFalse(fields.contains("Código"));
        assertFalse(fields.contains("Gestor"));
        assertFalse(fields.contains("Responsável Técnico"));
    }

    @Test
    @DisplayName("saveRiskChanges com permissão registra campos privilegiados")
    void saveRiskChanges_withPermission_logsPrivilegedFields() {
        User oldUser    = mockUser(1L, "Alice");
        User newUser    = mockUser(2L, "Bob");
        User oldManager = mockUser(3L, "Gerente A");
        User newManager = mockUser(4L, "Gerente B");
        Unit oldUnit    = mockUnit(10L, "Unidade A");
        Unit newUnit    = mockUnit(11L, "Unidade B");
        RiskLevel oldLevel = mockRiskLevel(100L, "BAIXO");
        RiskLevel newLevel = mockRiskLevel(101L, "ALTO");

        Date oldBegin = date(2024, 1, 10);
        Date newBegin = date(2025, 2, 1);

        Risk oldRisk = mockRisk(
            "Risco 1","COD-001",
            "<p>Causa A</p>","<i>Conseq A</i>",
            "Baixa","Médio","Mensal",
            "Financeiro","X","Estratégico",
            false,false,false,true,false,
            oldBegin,"http://a", oldUser, oldManager, oldUnit, oldLevel,
            null, null
        );
        Risk newRisk = mockRisk(
            "Risco 1 alterado","COD-002",
            "<b>Causa B</b>","Conseq B",
            "Alta","Alto","Semanal",
            "Operacional","Y","Operacional",
            true,true,true,false,true,
            newBegin,"http://b", oldUser, oldManager, oldUnit, oldLevel,
            null, null
        );
        User auditor = mockUser(99L, "Auditor");

        service.saveRiskChanges(oldRisk, newRisk, newUnit, newUser, newManager, newLevel, true, auditor);

        Set<String> fields = fieldNames(saved);
        assertTrue(fields.containsAll(Set.of("Nome","Código","Gestor","Responsável Técnico")),
            "Campos privilegiados devem ser registrados quando hasPermission=true");

        assertEquals("Risco 1", byField(saved, "Nome").getOldValue());
        assertEquals("Risco 1 alterado", byField(saved, "Nome").getNewValue());
        assertEquals("COD-001", byField(saved, "Código").getOldValue());
        assertEquals("COD-002", byField(saved, "Código").getNewValue());
        assertEquals("Gerente A", byField(saved, "Gestor").getOldValue());
        assertEquals("Gerente B", byField(saved, "Gestor").getNewValue());
        assertEquals("Alice", byField(saved, "Responsável Técnico").getOldValue());
        assertEquals("Bob", byField(saved, "Responsável Técnico").getNewValue());
    }

    @Test
    @DisplayName("saveRiskChanges não persiste nada quando não há mudanças")
    void saveRiskChanges_noChanges_noPersist() {
        User user    = mockUser(1L, "Alice");
        User manager = mockUser(3L, "Gerente A");
        Unit unit    = mockUnit(10L, "Unidade A");
        RiskLevel level = mockRiskLevel(100L, "BAIXO");
        Date begin = date(2024, 1, 10);

        Risk oldRisk = mockRisk(
            "Risco 1","COD-001",
            "<p>Causa</p>","<i>Conseq</i>",
            "Baixa","Médio","Mensal",
            "Financeiro","X","Estratégico",
            false,false,false,true,false,
            begin,"http://a", user, manager, unit, level,
            null, null
        );
        Risk newRisk = mockRisk(
            "Risco 1","COD-001",
            "<p>Causa</p>","<i>Conseq</i>",
            "Baixa","Médio","Mensal",
            "Financeiro","X","Estratégico",
            false,false,false,true,false,
            begin,"http://a", user, manager, unit, level,
            null, null
        );

        service.saveRiskChanges(oldRisk, newRisk, unit, user, manager, level, true, user);

        assertEquals(0, saved.size(), "Nenhuma entrada de histórico deveria ser persistida.");
    }

    @Test
    @DisplayName("save() sempre marca deleted=false antes de persistir")
    void save_marksDeletedFalse() throws Exception {
        RiskEditHistory h = new RiskEditHistory();
        RiskEditHistory.class.getMethod("setDeleted", boolean.class).invoke(h, true);

        service.save(h);

        assertEquals(1, saved.size(), "Deveria persistir exatamente um registro.");
        Object persisted = saved.get(0);
        boolean deleted = readBooleanFlag(persisted, "isDeleted", "getDeleted");
        assertFalse(deleted, "deleted deve ser false ao salvar.");
    }

    @Test
    @DisplayName("DTO formata a data como 'dd/MM/yyyy - HH:mm'")
    void dto_formats_date_correctly() {
        RiskEditHistory h = new RiskEditHistory();
        User resp = mockUser(42L, "Editor");

        h.setResponsibleUser(resp);
        h.setChangedField("Nome");
        h.setOldValue("A");
        h.setNewValue("B");
        h.setChangeDate(LocalDateTime.of(2025, 8, 1, 9, 5));

        RiskEditHistoryBS.RiskEditHistoryDTO dto = new RiskEditHistoryBS.RiskEditHistoryDTO(h);

        assertEquals("Editor", dto.getResponsibleUser());
        assertEquals("Nome", dto.getChangedField());
        assertEquals("A", dto.getOldValue());
        assertEquals("B", dto.getNewValue());
        assertEquals("01/08/2025 - 09:05", dto.getChangeDate());
    }

    private static Date date(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static User mockUser(Long id, String name) {
        User u = mock(User.class);
        when(u.getId()).thenReturn(id);
        when(u.getName()).thenReturn(name);
        return u;
    }

    private static Unit mockUnit(Long id, String name) {
        Unit u = mock(Unit.class);
        when(u.getId()).thenReturn(id);
        when(u.getName()).thenReturn(name);
        return u;
    }

    private static RiskLevel mockRiskLevel(Long id, String level) {
        RiskLevel rl = mock(RiskLevel.class);
        when(rl.getId()).thenReturn(id);
        when(rl.getLevel()).thenReturn(level);
        return rl;
    }

    private static Risk mockRisk(
        String name, String code,
        String reasonHtml, String resultHtml,
        String probability, String impact, String periodicity,
        String tipology, String otherTipologies, String type,
        boolean risk_pdi, boolean risk_pdi_axis, boolean risk_obj_process, boolean risk_act_process, boolean archived,
        Date begin, String linkFPDI, User user, User manager, Unit unit, RiskLevel riskLevel,
        Integer responseId, Integer orgLevelId
    ) {
        Risk r = mock(Risk.class);
        when(r.getName()).thenReturn(name);
        when(r.getCode()).thenReturn(code);
        when(r.getReason()).thenReturn(reasonHtml);
        when(r.getResult()).thenReturn(resultHtml);
        when(r.getProbability()).thenReturn(probability);
        when(r.getImpact()).thenReturn(impact);
        when(r.getPeriodicity()).thenReturn(periodicity);
        when(r.getTipology()).thenReturn(tipology);
        when(r.getOtherTipologies()).thenReturn(otherTipologies);
        when(r.getType()).thenReturn(type);
        when(r.isRisk_pdi()).thenReturn(risk_pdi);
        when(r.isRisk_pdi_axis()).thenReturn(risk_pdi_axis);
        when(r.isRisk_obj_process()).thenReturn(risk_obj_process);
        when(r.isRisk_act_process()).thenReturn(risk_act_process);
        when(r.isArchived()).thenReturn(archived);
        when(r.getBegin()).thenReturn(begin);
        when(r.getLinkFPDI()).thenReturn(linkFPDI);
        when(r.getUser()).thenReturn(user);
        when(r.getManager()).thenReturn(manager);
        when(r.getUnit()).thenReturn(unit);
        when(r.getRiskLevel()).thenReturn(riskLevel);
        when(r.getResponse()).thenReturn(responseId);
        when(r.getLevel()).thenReturn(orgLevelId);
        return r;
    }

    private static Set<String> fieldNames(List<RiskEditHistory> entries) {
        Set<String> set = new LinkedHashSet<>();
        for (RiskEditHistory h : entries) set.add(h.getChangedField());
        return set;
    }

    private static RiskEditHistory byField(List<RiskEditHistory> entries, String field) {
        return entries.stream()
            .filter(h -> field.equals(h.getChangedField()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Entrada não encontrada: " + field + "\n" + entries));
    }

    private static boolean readBooleanFlag(Object bean, String... candidateGetters) {
        for (String g : candidateGetters) {
            try {
                return (boolean) bean.getClass().getMethod(g).invoke(bean);
            } catch (Exception ignored) {}
        }
        throw new AssertionError("Nenhum getter boolean encontrado para " + Arrays.toString(candidateGetters));
    }
}
