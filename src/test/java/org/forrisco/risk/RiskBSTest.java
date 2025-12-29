package org.forrisco.risk;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.user.User;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.policy.Policy;
import org.forrisco.core.process.Process;
import org.forrisco.core.process.ProcessBS;
import org.forrisco.core.process.ProcessObjective;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.history.RiskEditHistoryBS;
import org.forrisco.risk.links.RiskActivity;
import org.forrisco.risk.links.RiskAxis;
import org.forrisco.risk.links.RiskGoal;
import org.forrisco.risk.links.RiskIndicator;
import org.forrisco.risk.links.RiskProcessObjective;
import org.forrisco.risk.links.RiskStrategy;
import org.forrisco.risk.links.RiskStrategyBean;
import org.forrisco.risk.monitor.MonitorBS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.forpdi.core.company.CompanyDomain;
import org.apache.commons.mail.EmailException;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.auth.UserSession;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.notification.NotificationBS;
import org.forpdi.core.notification.NotificationType;
import org.forrisco.core.unit.UnitBS;
import org.forpdi.planning.plan.PlanMacro;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.forrisco.risk.monitor.Monitor;

@ExtendWith(MockitoExtension.class)
public class RiskBSTest {

    @Mock
    HibernateDAO hibernateDAO;

    @Mock
    ProcessBS processBS;

    @Mock
    MonitorBS monitorBS;

    @Mock
    CompanyDomainContext domain;

    @Mock
    NotificationBS notificationBS;

    @Mock
    UnitBS unitBS;

    @Spy
    @InjectMocks
    private RiskBS riskBS;

    private static Unit unit(Long id) {
        Unit u = new Unit();
        u.setId(id);
        return u;
    }

    private static User user(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private static Risk riskBase() {
        Risk r = new Risk();
        r.setUnit(unit(1L));
        r.setUser(user(2L));
        r.setManager(user(3L));
        r.setReason(" <b>motivo</b> ");
        r.setResult(" <script>resultado</script> ");
        r.setActivities(new PaginatedList<>());
        return r;
    }

    private static Process processComUnitEPlan(Long processId, Long unitId, Long planRiskId) {
        Process p = new Process();
        p.setId(processId);

        Unit u = new Unit();
        u.setId(unitId);

        PlanRisk pr = new PlanRisk();
        pr.setId(planRiskId);

        u.setPlanRisk(pr);
        p.setUnitCreator(u);
        return p;
    }

    private static ProcessObjective procObj(Long id, Process process) {
        ProcessObjective po = new ProcessObjective();
        po.setId(id);
        po.setProcess(process);
        return po;
    }

    @DisplayName("RiskBS.saveRisk deve marcar deleted=false e persistir")
    @Test
    public void testSaveRisk() {
        Risk validRisk = new Risk();
        validRisk.setDeleted(true);

        doNothing().when(hibernateDAO).persist(any(Risk.class));

        riskBS.saveRisk(validRisk);

        ArgumentCaptor<Risk> captor = ArgumentCaptor.forClass(Risk.class);
        verify(hibernateDAO, times(1)).persist(captor.capture());

        Risk persisted = captor.getValue();
        assertFalse(persisted.isDeleted(), "Risk.isDeleted() deveria ser false antes de persistir.");
    }

    @DisplayName("validateRisk - OK quando unit, user, manager e riskLevel existem")
    @Test
    void validateRisk_ok() {
        Risk risk = riskBase();
        RiskLevel rl = new RiskLevel();

        doReturn(unit(1L)).when(riskBS).exists(eq(1L), eq(Unit.class));
        doReturn(user(2L)).when(riskBS).exists(eq(2L), eq(User.class));
        doReturn(user(3L)).when(riskBS).exists(eq(3L), eq(User.class));

        assertDoesNotThrow(() -> riskBS.validateRisk(risk, rl));
    }

    @DisplayName("validateRisk - lança IllegalArgumentException quando Unit não existe")
    @Test
    void validateRisk_semUnit() {
        Risk risk = riskBase();
        doReturn(null).when(riskBS).exists(eq(1L), eq(Unit.class));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> riskBS.validateRisk(risk, new RiskLevel()));
        assertTrue(ex.getMessage().toLowerCase().contains("unidade"));
    }

    @DisplayName("validateRisk - lança IllegalArgumentException quando User não existe")
    @Test
    void validateRisk_semUser() {
        Risk risk = riskBase();
        doReturn(unit(1L)).when(riskBS).exists(eq(1L), eq(Unit.class));
        doReturn(null).when(riskBS).exists(eq(2L), eq(User.class));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> riskBS.validateRisk(risk, new RiskLevel()));
        assertTrue(ex.getMessage().toLowerCase().contains("responsável"));
    }

    @DisplayName("validateRisk - repropaga IllegalStateException quando manager inválido")
    @Test
    void validateRisk_managerInvalido() {
        Risk risk = riskBase();
        doReturn(unit(1L)).when(riskBS).exists(eq(1L), eq(Unit.class));
        doReturn(user(2L)).when(riskBS).exists(eq(2L), eq(User.class));
        doReturn(null).when(riskBS).exists(eq(3L), eq(User.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> riskBS.validateRisk(risk, new RiskLevel()));
        assertTrue(ex.getMessage().toLowerCase().contains("gestor"));
    }

    @DisplayName("validateRisk - lança IllegalArgumentException quando RiskLevel é nulo")
    @Test
    void validateRisk_riskLevelNulo() {
        Risk risk = riskBase();
        doReturn(unit(1L)).when(riskBS).exists(eq(1L), eq(Unit.class));
        doReturn(user(2L)).when(riskBS).exists(eq(2L), eq(User.class));
        doReturn(user(3L)).when(riskBS).exists(eq(3L), eq(User.class));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> riskBS.validateRisk(risk, null));
        assertTrue(ex.getMessage().toLowerCase().contains("grau de risco"));
    }

    @DisplayName("saveRiskLevel(Policy) - cria RiskLevel para cada coluna com cor não-nula")
    @Test
    void saveRiskLevel_policyMatriz() {
        Policy policy = new Policy();
        String[][] matrix = new String[][] {
                { "Baixo", "Médio", "Alto", "Crítico" },
                { "16711680", "65280", null, "255" }
        };
        policy.setRisk_level(matrix);

        doNothing().when(riskBS).persist(any(RiskLevel.class));

        riskBS.saveRiskLevel(policy);

        verify(riskBS, times(3)).persist(any(RiskLevel.class));
    }

    @DisplayName("saveRiskLevel(RiskLevel) - seta deleted=false e persiste")
    @Test
    void saveRiskLevel_unitario() {
        RiskLevel rl = new RiskLevel();
        rl.setDeleted(true);

        doNothing().when(riskBS).persist(any(RiskLevel.class));

        riskBS.saveRiskLevel(rl);

        ArgumentCaptor<RiskLevel> captor = ArgumentCaptor.forClass(RiskLevel.class);
        verify(riskBS).persist(captor.capture());
        assertFalse(captor.getValue().isDeleted(), "RiskLevel deve ser salvo com deleted=false");
    }

    @DisplayName("saveActivities - não faz nada quando a lista é nula")
    @Test
    void saveActivities_listaNula() {
        Risk r = riskBase();
        PaginatedList<RiskActivity> page = new PaginatedList<>();
        page.setList(null);
        r.setActivities(page);

        riskBS.saveActivities(r);
        verify(hibernateDAO, never()).persist(any());
    }

    @DisplayName("saveRiskProcessObjective - cria vínculo e monta link correto")
    @Test
    void saveRiskProcessObjective_ok() {
        Risk r = riskBase();

        List<RiskProcessObjective> rpos = new ArrayList<>();
        RiskProcessObjective input = new RiskProcessObjective();
        input.setId(123L);
        rpos.add(input);
        r.setProcessObjectives(rpos);

        Process proc = processComUnitEPlan(33L, 44L, 55L);
        ProcessObjective existentObj = procObj(123L, proc);

        when(processBS.retrieveProcessObjectiveById(123L)).thenReturn(existentObj);
        doNothing().when(riskBS).persist(any(RiskProcessObjective.class));

        riskBS.saveRiskProcessObjective(r);

        ArgumentCaptor<RiskProcessObjective> captor = ArgumentCaptor.forClass(RiskProcessObjective.class);
        verify(riskBS, times(1)).persist(captor.capture());
        RiskProcessObjective persisted = captor.getValue();

        assertEquals(r, persisted.getRisk(), "Deve referenciar o Risk");
        assertEquals(existentObj, persisted.getProcessObjective(), "Deve referenciar o ProcessObjective existente");
        assertEquals(123L, persisted.getProcessId(), "Deve copiar o processId de entrada");
        assertEquals("#/forrisco/plan-risk/55/unit/44/info", persisted.getLinkFPDI(),
                "Link FPDI gerado incorretamente");
    }

    private Risk novoRiscoComCompartilhamento() {
        Risk risk = new Risk();
        risk.setId(999L);
        risk.setName("Risco A");
        risk.setReason("<b>Razão</b><script>alert(1)</script>");
        risk.setResult("<i>Resultado</i><img src=x onerror=1 />");
        risk.setUnit(unit(1L));

        Unit shared = unit(2L);
        shared.setUser(user(10L));
        List<Unit> sharedUnits = new ArrayList<>();
        sharedUnits.add(shared);
        risk.setSharedUnits(sharedUnits);

        return risk;
    }

    @Test
    @DisplayName("createNewRisk: seta begin/id/riskLevel, sanitiza textos e chama persistências/notify")
    void createNewRisk_camposESalvamentos() throws EmailException {
        Risk risk = novoRiscoComCompartilhamento();

        RiskLevel level = new RiskLevel();
        level.setId(7L);

        doReturn(level).when(riskBS).getRiskLevelByRisk(eq(risk), isNull());
        doNothing().when(riskBS).validateRisk(any(Risk.class), any(RiskLevel.class));
        doNothing().when(riskBS).saveRisk(any(Risk.class));
        doNothing().when(riskBS).saveActivities(any(Risk.class));
        doNothing().when(riskBS).saveRiskProcessObjective(any(Risk.class));
        doNothing().when(riskBS).saveStrategies(any(Risk.class));
        doNothing().when(riskBS).saveAxes(any(Risk.class));
        doNothing().when(riskBS).saveIndicators(any(Risk.class));
        doNothing().when(riskBS).saveGoals(any(Risk.class));
        doNothing().when(riskBS).saveResponse(any(Risk.class));
        doNothing().when(riskBS).notifySharedRisk(any(Risk.class), any(Unit.class), any());

        riskBS.createNewRisk(risk);

        assertNotNull(risk.getBegin(), "Begin deve ser definido ao criar novo risco");
        assertNull(risk.getId(), "ID deve ser resetado para null");
        assertNotNull(risk.getRiskLevel(), "RiskLevel deve ser definido");
        assertEquals(7L, risk.getRiskLevel().getId());

        assertEquals(SanitizeUtil.sanitize("<b>Razão</b><script>alert(1)</script>"), risk.getReason());
        assertEquals(SanitizeUtil.sanitize("<i>Resultado</i><img src=x onerror=1 />"), risk.getResult());

        verify(riskBS).saveRisk(risk);
        verify(riskBS).saveActivities(risk);
        verify(riskBS).saveRiskProcessObjective(risk);
        verify(riskBS).saveStrategies(risk);
        verify(riskBS).saveAxes(risk);
        verify(riskBS).saveIndicators(risk);
        verify(riskBS).saveGoals(risk);
        verify(riskBS).saveResponse(risk);

        verify(riskBS, times(1)).notifySharedRisk(eq(risk), any(Unit.class), any());
    }

    @Test
    @DisplayName("createNewRisk: não notifica quando não há unidades compartilhadas")
    void createNewRisk_semCompartilhamento() throws EmailException {
        Risk risk = novoRiscoComCompartilhamento();
        risk.setSharedUnits(null);

        RiskLevel level = new RiskLevel();
        doReturn(level).when(riskBS).getRiskLevelByRisk(eq(risk), isNull());

        doNothing().when(riskBS).validateRisk(any(Risk.class), any(RiskLevel.class));
        doNothing().when(riskBS).saveRisk(any(Risk.class));
        doNothing().when(riskBS).saveActivities(any(Risk.class));
        doNothing().when(riskBS).saveRiskProcessObjective(any(Risk.class));
        doNothing().when(riskBS).saveStrategies(any(Risk.class));
        doNothing().when(riskBS).saveAxes(any(Risk.class));
        doNothing().when(riskBS).saveIndicators(any(Risk.class));
        doNothing().when(riskBS).saveGoals(any(Risk.class));
        doNothing().when(riskBS).saveResponse(any(Risk.class));

        riskBS.createNewRisk(risk);

        verify(riskBS, never()).notifySharedRisk(any(), any(), any());
        verify(riskBS).saveRisk(risk);
    }

    @Test
    @DisplayName("notifySharedRisk: retorna sem ação quando risk ou unit são nulos")
    void notifySharedRisk_parametrosNulos_noop() throws EmailException {
        riskBS.notifySharedRisk(null, unit(1L), user(2L));
        riskBS.notifySharedRisk(new Risk(), null, user(2L));

        verifyNoInteractions(notificationBS);
        verify(unitBS, never()).retrieveUnitById(anyLong());
        verify(domain, never()).get();
    }

    @Test
    @DisplayName("notifySharedRisk: usa unitBS.retrieveUnitById e retorna quando não há responsável (unit.user/responsible nulos)")
    void notifySharedRisk_semResponsavel_noop() throws EmailException {
        Risk risk = new Risk();
        risk.setId(99L);
        risk.setName("Risco X");

        Unit inputUnit = unit(5L);
        Unit loaded = unit(5L);
        when(unitBS.retrieveUnitById(5L)).thenReturn(loaded);

        riskBS.notifySharedRisk(risk, inputUnit, null);

        verify(unitBS).retrieveUnitById(5L);
        verifyNoInteractions(notificationBS);
    }

    @Test
    @DisplayName("notifySharedRisk: retorna quando responsável não tem ID")
    void notifySharedRisk_responsavelSemId_noop() throws EmailException {
        Risk risk = new Risk();
        risk.setId(99L);
        risk.setName("Risco X");

        Unit inputUnit = unit(5L);
        Unit loaded = unit(5L);
        User u = new User();
        loaded.setUser(u);

        when(unitBS.retrieveUnitById(5L)).thenReturn(loaded);

        riskBS.notifySharedRisk(risk, inputUnit, null);

        verify(unitBS).retrieveUnitById(5L);
        verifyNoInteractions(notificationBS);
    }

    @DisplayName("saveActivities: seta defaults, resolve process e link, e persiste cada atividade")
    @Test
    void saveActivities_ok_resolveProcessELink_persisteCada() {
        Risk r = riskBase();
        PaginatedList<RiskActivity> page = new PaginatedList<>();
        List<RiskActivity> list = new ArrayList<>();

        RiskActivity a1 = new RiskActivity();
        a1.setId(111L);
        a1.setName(null);
        Process p1Ref = new Process();
        p1Ref.setId(33L);
        a1.setProcess(p1Ref);
        list.add(a1);

        RiskActivity a2 = new RiskActivity();
        a2.setId(222L);
        a2.setName("Ativ 2");
        Process p2Ref = new Process();
        p2Ref.setId(99L);
        a2.setProcess(p2Ref);
        list.add(a2);

        page.setList(list);
        r.setActivities(page);

        Process p1 = processComUnitEPlan(33L, 44L, 55L);
        p1.setDeleted(false);

        when(hibernateDAO.exists(33L, Process.class)).thenReturn(p1);
        when(hibernateDAO.exists(99L, Process.class)).thenReturn(null);

        riskBS.saveActivities(r);

        assertNull(a1.getId(), "Ao salvar, id da atividade deve ser null");
        assertSame(r, a1.getRisk(), "Atividade deve referenciar o Risk");
        assertFalse(a1.isDeleted(), "Atividade deve ser salva com deleted=false");
        assertSame(p1, a1.getProcess(), "Processo deve ser resolvido via dao.exists");
        assertEquals("", a1.getName(), "Nome null deve virar string vazia");
        assertEquals("#/forrisco/plan-risk/55/unit/44/info", a1.getLinkFPDI(), "Link FPDI incorreto");

        assertNull(a2.getId(), "Ao salvar, id da atividade deve ser null");
        assertSame(r, a2.getRisk(), "Atividade deve referenciar o Risk");
        assertFalse(a2.isDeleted(), "Atividade deve ser salva com deleted=false");
        assertNull(a2.getProcess(), "Process inexistente deve resultar em process=null");
        assertNull(a2.getLinkFPDI(), "Sem process válido, não deve montar link");
        assertEquals("Ativ 2", a2.getName(), "Nome existente deve ser preservado");

        verify(hibernateDAO, times(1)).persist(a1);
        verify(hibernateDAO, times(1)).persist(a2);
    }

    @DisplayName("saveActivities: sem unitCreator no process -> não monta link, mas persiste")
    @Test
    void saveActivities_processSemUnitCreator_naoMontaLink_persiste() {
        Risk r = riskBase();
        PaginatedList<RiskActivity> page = new PaginatedList<>();
        List<RiskActivity> list = new ArrayList<>();
        page.setList(list);
        r.setActivities(page);

        RiskActivity a = new RiskActivity();
        a.setId(10L);
        a.setName(null);
        Process pRef = new Process();
        pRef.setId(123L);
        a.setProcess(pRef);
        list.add(a);

        Process p = new Process();
        p.setId(123L);
        p.setDeleted(false);
        p.setUnitCreator(null);
        when(hibernateDAO.exists(123L, Process.class)).thenReturn(p);

        riskBS.saveActivities(r);

        assertNull(a.getId());
        assertSame(r, a.getRisk());
        assertFalse(a.isDeleted());
        assertSame(p, a.getProcess());
        assertEquals("", a.getName());
        assertNull(a.getLinkFPDI(), "Sem unitCreator, não deve montar link");

        verify(hibernateDAO).persist(a);
    }

    @DisplayName("saveStrategies: seta defaults, resolve estrutura/plano, gera link e persiste cada estratégia")
    @Test
    void saveStrategies_ok_geraLink_persiste() {
        Risk r = riskBase();
        PaginatedList<RiskStrategy> page = new PaginatedList<>();
        List<RiskStrategy> list = new ArrayList<>();
        page.setList(list);
        r.setStrategies(page);

        RiskStrategy s1 = new RiskStrategy();
        s1.setId(123L);
        StructureLevelInstance structureRef = new StructureLevelInstance();
        structureRef.setId(77L);
        s1.setStructure(structureRef);
        list.add(s1);

        StructureLevelInstance structureFull = new StructureLevelInstance();
        structureFull.setId(77L);
        structureFull.setName("Objetivo XPTO");

        Plan plan = new Plan();
        plan.setId(200L);
        PlanMacro parent = new PlanMacro();
        parent.setId(999L);
        plan.setParent(parent);
        structureFull.setPlan(plan);

        when(hibernateDAO.exists(77L, StructureLevelInstance.class)).thenReturn(structureFull);
        when(hibernateDAO.exists(200L, Plan.class)).thenReturn(plan);

        riskBS.saveStrategies(r);

        assertNull(s1.getId());
        assertSame(r, s1.getRisk());
        assertFalse(s1.isDeleted());
        assertSame(structureFull, s1.getStructure());
        assertEquals("Objetivo XPTO", s1.getName());
        assertEquals("#/plan/999/details/subplan/level/77", s1.getLinkFPDI());

        verify(hibernateDAO).persist(s1);
    }

    @DisplayName("saveStrategies: lança IllegalArgumentException quando estrutura não existe")
    @Test
    void saveStrategies_estruturaNaoEncontrada_excecao() {
        Risk r = riskBase();
        PaginatedList<RiskStrategy> page = new PaginatedList<>();
        List<RiskStrategy> list = new ArrayList<>();
        page.setList(list);
        r.setStrategies(page);

        RiskStrategy s = new RiskStrategy();
        StructureLevelInstance structureRef = new StructureLevelInstance();
        structureRef.setId(123L);
        s.setStructure(structureRef);
        list.add(s);

        when(hibernateDAO.exists(123L, StructureLevelInstance.class)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> riskBS.saveStrategies(r));
        assertTrue(ex.getMessage().toLowerCase().contains("objetivo do pdi"),
                "Mensagem deve indicar ausência do objetivo");
        verify(hibernateDAO, never()).persist(any());
    }

    @DisplayName("saveStrategies: lança IllegalArgumentException quando plano do PDI não existe")
    @Test
    void saveStrategies_planoNaoEncontrado_excecao() {
        Risk r = riskBase();
        PaginatedList<RiskStrategy> page = new PaginatedList<>();
        List<RiskStrategy> list = new ArrayList<>();
        page.setList(list);
        r.setStrategies(page);

        RiskStrategy s = new RiskStrategy();
        StructureLevelInstance structureRef = new StructureLevelInstance();
        structureRef.setId(77L);
        s.setStructure(structureRef);
        list.add(s);

        StructureLevelInstance structureFull = new StructureLevelInstance();
        structureFull.setId(77L);
        structureFull.setName("Objetivo ABC");

        Plan planRef = new Plan();
        planRef.setId(200L);

        structureFull.setPlan(planRef);

        when(hibernateDAO.exists(77L, StructureLevelInstance.class)).thenReturn(structureFull);
        when(hibernateDAO.exists(200L, Plan.class)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> riskBS.saveStrategies(r));
        assertTrue(ex.getMessage().toLowerCase().contains("plano do pdi"), "Mensagem deve indicar ausência do plano");
        verify(hibernateDAO, never()).persist(any());
    }

    @DisplayName("saveAxes: seta defaults, resolve estrutura/plano, gera link e persiste cada eixo")
    @Test
    void saveAxes_ok_geraLink_persiste() {
        Risk r = riskBase();
        PaginatedList<RiskAxis> page = new PaginatedList<>();
        List<RiskAxis> list = new ArrayList<>();
        page.setList(list);
        r.setAxes(page);

        RiskAxis axis = new RiskAxis();
        axis.setId(321L);
        StructureLevelInstance structureRef = new StructureLevelInstance();
        structureRef.setId(77L);
        axis.setStructure(structureRef);
        list.add(axis);

        StructureLevelInstance structureFull = new StructureLevelInstance();
        structureFull.setId(77L);
        structureFull.setName("Eixo Temático XYZ");

        Plan plan = new Plan();
        plan.setId(200L);
        PlanMacro parent = new PlanMacro();
        parent.setId(999L);
        plan.setParent(parent);
        structureFull.setPlan(plan);

        when(hibernateDAO.exists(77L, StructureLevelInstance.class)).thenReturn(structureFull);
        when(hibernateDAO.exists(200L, Plan.class)).thenReturn(plan);

        riskBS.saveAxes(r);

        assertNull(axis.getId(), "Ao salvar, id do eixo deve ser null");
        assertSame(r, axis.getRisk(), "Eixo deve referenciar o Risk");
        assertFalse(axis.isDeleted(), "Eixo deve ser salvo com deleted=false");
        assertSame(structureFull, axis.getStructure(), "Deve setar a Structure resolvida");
        assertEquals("Eixo Temático XYZ", axis.getName(), "Nome deve ser o da estrutura");
        assertEquals("#/plan/999/details/subplan/level/77", axis.getLinkFPDI(), "Link FPDI incorreto");

        verify(hibernateDAO).persist(axis);
    }

    @DisplayName("saveAxes: lança IllegalArgumentException quando estrutura não existe")
    @Test
    void saveAxes_estruturaNaoEncontrada_excecao() {
        Risk r = riskBase();
        PaginatedList<RiskAxis> page = new PaginatedList<>();
        List<RiskAxis> list = new ArrayList<>();
        page.setList(list);
        r.setAxes(page);

        RiskAxis axis = new RiskAxis();
        StructureLevelInstance structureRef = new StructureLevelInstance();
        structureRef.setId(123L);
        axis.setStructure(structureRef);
        list.add(axis);

        when(hibernateDAO.exists(123L, StructureLevelInstance.class)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> riskBS.saveAxes(r));
        assertTrue(ex.getMessage().toLowerCase().contains("eixo do pdi"), "Mensagem deve indicar ausência do eixo");
        verify(hibernateDAO, never()).persist(any());
    }

    @DisplayName("saveAxes: lança IllegalArgumentException quando plano do PDI não existe")
    @Test
    void saveAxes_planoNaoEncontrado_excecao() {
        Risk r = riskBase();
        PaginatedList<RiskAxis> page = new PaginatedList<>();
        List<RiskAxis> list = new ArrayList<>();
        page.setList(list);
        r.setAxes(page);

        RiskAxis axis = new RiskAxis();
        StructureLevelInstance structureRef = new StructureLevelInstance();
        structureRef.setId(77L);
        axis.setStructure(structureRef);
        list.add(axis);

        StructureLevelInstance structureFull = new StructureLevelInstance();
        structureFull.setId(77L);
        structureFull.setName("Eixo ABC");

        Plan planRef = new Plan();
        planRef.setId(200L);
        planRef.setParent(new PlanMacro());
        structureFull.setPlan(planRef);

        when(hibernateDAO.exists(77L, StructureLevelInstance.class)).thenReturn(structureFull);
        when(hibernateDAO.exists(200L, Plan.class)).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> riskBS.saveAxes(r));
        assertTrue(ex.getMessage().toLowerCase().contains("plano do pdi"), "Mensagem deve indicar ausência do plano");
        verify(hibernateDAO, never()).persist(any());
    }

    @DisplayName("saveIndicators: seta defaults, resolve estrutura/plano, gera link e persiste cada indicador")
    @Test
    void saveIndicators_ok_geraLink_persiste() {
        Risk r = riskBase();
        PaginatedList<RiskIndicator> page = new PaginatedList<>();
        List<RiskIndicator> list = new ArrayList<>();
        page.setList(list);
        r.setIndicators(page);

        RiskIndicator ind = new RiskIndicator();
        ind.setId(321L);
        StructureLevelInstance structureRef = new StructureLevelInstance();
        structureRef.setId(77L);
        ind.setStructure(structureRef);
        list.add(ind);

        StructureLevelInstance structureFull = new StructureLevelInstance();
        structureFull.setId(77L);
        structureFull.setName("Indicador ABC");

        Plan plan = new Plan();
        plan.setId(200L);
        PlanMacro parent = new PlanMacro();
        parent.setId(999L);
        plan.setParent(parent);
        structureFull.setPlan(plan);

        when(hibernateDAO.exists(77L, StructureLevelInstance.class)).thenReturn(structureFull);
        when(hibernateDAO.exists(200L, Plan.class)).thenReturn(plan);

        riskBS.saveIndicators(r);

        assertNull(ind.getId(), "Ao salvar, id do indicador deve ser null");
        assertSame(r, ind.getRisk(), "Indicador deve referenciar o Risk");
        assertFalse(ind.isDeleted(), "Indicador deve ser salvo com deleted=false");
        assertSame(structureFull, ind.getStructure(), "Deve setar a Structure resolvida");
        assertEquals("Indicador ABC", ind.getName(), "Nome deve ser o da estrutura");
        assertEquals("#/plan/999/details/subplan/level/77", ind.getLinkFPDI(), "Link FPDI incorreto");

        verify(hibernateDAO).persist(ind);
    }

    @DisplayName("saveIndicators: lança IllegalArgumentException quando estrutura não existe")
    @Test
    void saveIndicators_estruturaNaoEncontrada_excecao() {
        Risk r = riskBase();
        PaginatedList<RiskIndicator> page = new PaginatedList<>();
        List<RiskIndicator> list = new ArrayList<>();
        page.setList(list);
        r.setIndicators(page);

        RiskIndicator ind = new RiskIndicator();
        StructureLevelInstance structureRef = new StructureLevelInstance();
        structureRef.setId(123L);
        ind.setStructure(structureRef);
        list.add(ind);

        when(hibernateDAO.exists(123L, StructureLevelInstance.class)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> riskBS.saveIndicators(r));
        assertTrue(ex.getMessage().toLowerCase().contains("eixo do pdi"), "Mensagem deve indicar ausência do eixo");
        verify(hibernateDAO, never()).persist(any());
    }

    @DisplayName("saveIndicators: lança IllegalArgumentException quando plano do PDI não existe")
    @Test
    void saveIndicators_planoNaoEncontrado_excecao() {
        Risk r = riskBase();
        PaginatedList<RiskIndicator> page = new PaginatedList<>();
        List<RiskIndicator> list = new ArrayList<>();
        page.setList(list);
        r.setIndicators(page);

        RiskIndicator ind = new RiskIndicator();
        StructureLevelInstance structureRef = new StructureLevelInstance();
        structureRef.setId(77L);
        ind.setStructure(structureRef);
        list.add(ind);

        StructureLevelInstance structureFull = new StructureLevelInstance();
        structureFull.setId(77L);
        structureFull.setName("Indicador XYZ");

        Plan planRef = new Plan();
        planRef.setId(200L);
        planRef.setParent(new PlanMacro());
        structureFull.setPlan(planRef);

        when(hibernateDAO.exists(77L, StructureLevelInstance.class)).thenReturn(structureFull);
        when(hibernateDAO.exists(200L, Plan.class)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> riskBS.saveIndicators(r));
        assertTrue(ex.getMessage().toLowerCase().contains("plano do pdi"), "Mensagem deve indicar ausência do plano");
        verify(hibernateDAO, never()).persist(any());
    }

    @DisplayName("saveGoals: seta defaults, resolve estrutura/plano, gera link e persiste cada meta")
    @Test
    void saveGoals_ok_geraLink_persiste() {
        Risk r = riskBase();
        PaginatedList<RiskGoal> page = new PaginatedList<>();
        List<RiskGoal> list = new ArrayList<>();
        page.setList(list);
        r.setGoals(page);

        RiskGoal goal = new RiskGoal();
        goal.setId(321L);
        StructureLevelInstance structureRef = new StructureLevelInstance();
        structureRef.setId(77L);
        goal.setStructure(structureRef);
        list.add(goal);

        StructureLevelInstance structureFull = new StructureLevelInstance();
        structureFull.setId(77L);
        structureFull.setName("Meta XYZ");

        Plan plan = new Plan();
        plan.setId(200L);
        PlanMacro parent = new PlanMacro();
        parent.setId(999L);
        plan.setParent(parent);
        structureFull.setPlan(plan);

        when(hibernateDAO.exists(77L, StructureLevelInstance.class)).thenReturn(structureFull);
        when(hibernateDAO.exists(200L, Plan.class)).thenReturn(plan);
        riskBS.saveGoals(r);

        assertNull(goal.getId(), "Ao salvar, id da meta deve ser null");
        assertSame(r, goal.getRisk(), "Meta deve referenciar o Risk");
        assertFalse(goal.isDeleted(), "Meta deve ser salva com deleted=false");
        assertSame(structureFull, goal.getStructure(), "Deve setar a Structure resolvida");
        assertEquals("Meta XYZ", goal.getName(), "Nome deve ser o da estrutura");
        assertEquals("#/plan/999/details/subplan/level/77", goal.getLinkFPDI(), "Link FPDI incorreto");

        verify(hibernateDAO).persist(goal);
    }

    @DisplayName("saveGoals: lança IllegalArgumentException quando estrutura não existe")
    @Test
    void saveGoals_estruturaNaoEncontrada_excecao() {
        Risk r = riskBase();
        PaginatedList<RiskGoal> page = new PaginatedList<>();
        List<RiskGoal> list = new ArrayList<>();
        page.setList(list);
        r.setGoals(page);

        RiskGoal goal = new RiskGoal();
        StructureLevelInstance structureRef = new StructureLevelInstance();
        structureRef.setId(123L);
        goal.setStructure(structureRef);
        list.add(goal);

        when(hibernateDAO.exists(123L, StructureLevelInstance.class)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> riskBS.saveGoals(r));
        assertTrue(ex.getMessage().toLowerCase().contains("eixo do pdi"), "Mensagem deve indicar ausência do eixo");
        verify(hibernateDAO, never()).persist(any());
    }

    @DisplayName("saveGoals: lança IllegalArgumentException quando plano do PDI não existe")
    @Test
    void saveGoals_planoNaoEncontrado_excecao() {
        Risk r = riskBase();
        PaginatedList<RiskGoal> page = new PaginatedList<>();
        List<RiskGoal> list = new ArrayList<>();
        page.setList(list);
        r.setGoals(page);

        RiskGoal goal = new RiskGoal();
        StructureLevelInstance structureRef = new StructureLevelInstance();
        structureRef.setId(77L);
        goal.setStructure(structureRef);
        list.add(goal);

        StructureLevelInstance structureFull = new StructureLevelInstance();
        structureFull.setId(77L);
        structureFull.setName("Meta ABC");

        Plan planRef = new Plan();
        planRef.setId(200L);
        planRef.setParent(new PlanMacro());
        structureFull.setPlan(planRef);

        when(hibernateDAO.exists(77L, StructureLevelInstance.class)).thenReturn(structureFull);
        when(hibernateDAO.exists(200L, Plan.class)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> riskBS.saveGoals(r));
        assertTrue(ex.getMessage().toLowerCase().contains("plano do pdi"), "Mensagem deve indicar ausência do plano");
        verify(hibernateDAO, never()).persist(any());
    }

    @DisplayName("saveResponse: não faz nada quando response é null")
    @Test
    void saveResponse_responseNull_noop() {
        Risk r = riskBase();
        r.setSharedUnits(new ArrayList<>());
        r.setResponse(null);

        riskBS.saveResponse(r);

        verifyNoInteractions(unitBS);
        verify(riskBS, never()).persist(any(RiskSharedUnit.class));
    }

    @DisplayName("saveResponse: quando RiskResponse=SHARE, persiste um RiskSharedUnit por unidade compartilhada")
    @Test
    void saveResponse_share_persisteParaCadaUnidade() {
        Risk r = riskBase();

        Unit u1 = unit(21L);
        Unit u2 = unit(22L);
        r.setSharedUnits(new ArrayList<>(List.of(u1, u2)));

        r.setResponse(1);

        Unit loaded1 = unit(21L);
        loaded1.setName("U1");
        Unit loaded2 = unit(22L);
        loaded2.setName("U2");
        when(unitBS.retrieveUnitById(21L)).thenReturn(loaded1);
        when(unitBS.retrieveUnitById(22L)).thenReturn(loaded2);

        ArgumentCaptor<RiskSharedUnit> captor = ArgumentCaptor.forClass(RiskSharedUnit.class);
        doNothing().when(riskBS).persist(any(RiskSharedUnit.class));

        try (MockedStatic<RiskResponse> mocked = mockStatic(RiskResponse.class)) {
            mocked.when(() -> RiskResponse.GetById(1)).thenReturn(RiskResponse.SHARE);

            riskBS.saveResponse(r);
        }

        verify(unitBS).retrieveUnitById(21L);
        verify(unitBS).retrieveUnitById(22L);

        verify(riskBS, times(2)).persist(captor.capture());
        List<RiskSharedUnit> saved = captor.getAllValues();

        assertEquals(2, saved.size(), "Devem ser persistidas duas RiskSharedUnit");
        assertSame(r, saved.get(0).getRisk(), "Risk deve ser associado");
        assertSame(loaded1, saved.get(0).getUnit(), "Unit carregada deve ser associada");
        assertSame(r, saved.get(1).getRisk(), "Risk deve ser associado");
        assertSame(loaded2, saved.get(1).getUnit(), "Unit carregada deve ser associada");
    }

    @DisplayName("delete(RiskLevel): marca deleted=true e persiste")
    @Test
    void delete_riskLevel_marcaDeletedEPersiste() {
        RiskLevel rl = new RiskLevel();
        rl.setDeleted(false);

        doNothing().when(riskBS).persist(any(RiskLevel.class));

        riskBS.delete(rl);

        assertTrue(rl.isDeleted(), "Após delete, deleted deve ser true");
        verify(riskBS, times(1)).persist(same(rl));
    }

    @DisplayName("delete(RiskLevel): mantém deleted=true e persiste quando já estava deletado (idempotente)")
    @Test
    void delete_riskLevel_jaDeletado_aindaPersiste() {
        RiskLevel rl = new RiskLevel();
        rl.setDeleted(true);

        doNothing().when(riskBS).persist(any(RiskLevel.class));

        riskBS.delete(rl);

        assertTrue(rl.isDeleted(), "Deve continuar true");
        verify(riskBS, times(1)).persist(same(rl));
    }

    @DisplayName("delete(RiskActivity): marca deleted=true e persiste")
    @Test
    void delete_riskActivity_marcaDeletedEPersiste() throws Exception {
        RiskActivity act = new RiskActivity();
        act.setDeleted(false);

        doNothing().when(riskBS).persist(any(RiskActivity.class));

        Method m = RiskBS.class.getDeclaredMethod("delete", RiskActivity.class);
        m.setAccessible(true);
        m.invoke(riskBS, act);

        assertTrue(act.isDeleted(), "Após delete, deleted deve ser true");
        verify(riskBS, times(1)).persist(same(act));
    }

    @DisplayName("delete(RiskActivity): mantém deleted=true e persiste quando já estava deletada (idempotente)")
    @Test
    void delete_riskActivity_jaDeletada_aindaPersiste() throws Exception {
        RiskActivity act = new RiskActivity();
        act.setDeleted(true);

        doNothing().when(riskBS).persist(any(RiskActivity.class));

        Method m = RiskBS.class.getDeclaredMethod("delete", RiskActivity.class);
        m.setAccessible(true);
        m.invoke(riskBS, act);

        assertTrue(act.isDeleted(), "Deve continuar true");
        verify(riskBS, times(1)).persist(same(act));
    }

    @DisplayName("delete(RiskStrategy): marca deleted=true e persiste")
    @Test
    void delete_riskStrategy_marcaDeletedEPersiste() throws Exception {
        RiskStrategy str = new RiskStrategy();
        str.setDeleted(false);

        doNothing().when(riskBS).persist(any(RiskStrategy.class));

        Method m = RiskBS.class.getDeclaredMethod("delete", RiskStrategy.class);
        m.setAccessible(true);
        m.invoke(riskBS, str);

        assertTrue(str.isDeleted(), "Após delete, deleted deve ser true");
        verify(riskBS, times(1)).persist(same(str));
    }

    @DisplayName("delete(RiskStrategy): mantém deleted=true e persiste quando já estava deletada (idempotente)")
    @Test
    void delete_riskStrategy_jaDeletada_aindaPersiste() throws Exception {
        RiskStrategy str = new RiskStrategy();
        str.setDeleted(true);

        doNothing().when(riskBS).persist(any(RiskStrategy.class));

        Method m = RiskBS.class.getDeclaredMethod("delete", RiskStrategy.class);
        m.setAccessible(true);
        m.invoke(riskBS, str);

        assertTrue(str.isDeleted(), "Deve continuar true");
        verify(riskBS, times(1)).persist(same(str));
    }

    @DisplayName("delete(RiskAxis): marca deleted=true e persiste")
    @Test
    void delete_riskAxis_marcaDeletedEPersiste() throws Exception {
        RiskAxis axi = new RiskAxis();
        axi.setDeleted(false);

        doNothing().when(riskBS).persist(any(RiskAxis.class));

        Method m = RiskBS.class.getDeclaredMethod("delete", RiskAxis.class);
        m.setAccessible(true);
        m.invoke(riskBS, axi);

        assertTrue(axi.isDeleted(), "Após delete, deleted deve ser true");
        verify(riskBS, times(1)).persist(same(axi));
    }

    @DisplayName("delete(RiskAxis): mantém deleted=true e persiste quando já estava deletado (idempotente)")
    @Test
    void delete_riskAxis_jaDeletado_aindaPersiste() throws Exception {
        RiskAxis axi = new RiskAxis();
        axi.setDeleted(true);

        doNothing().when(riskBS).persist(any(RiskAxis.class));

        Method m = RiskBS.class.getDeclaredMethod("delete", RiskAxis.class);
        m.setAccessible(true);
        m.invoke(riskBS, axi);

        assertTrue(axi.isDeleted(), "Deve continuar true");
        verify(riskBS, times(1)).persist(same(axi));
    }

    @DisplayName("deleteAPS: marca deleted em Activity/Strategy/Axis e deleta Indicator/Goal/SharedUnit/ProcessObjective")
    @Test
    void deleteAPS_deletaTudoRelacionado() {
        Risk old = new Risk();
        old.setId(1L);

        Risk dbRisk = new Risk();
        dbRisk.setId(1L);
        when(hibernateDAO.exists(1L, Risk.class)).thenReturn(dbRisk);

        RiskActivity act = new RiskActivity();
        act.setDeleted(false);
        RiskStrategy str = new RiskStrategy();
        str.setDeleted(false);
        RiskAxis axi = new RiskAxis();
        axi.setDeleted(false);

        RiskIndicator ind = new RiskIndicator();
        RiskGoal goal = new RiskGoal();
        RiskSharedUnit rsu = new RiskSharedUnit();
        RiskProcessObjective rpo = new RiskProcessObjective();

        PaginatedList<RiskActivity> activities = new PaginatedList<>();
        activities.setList(new ArrayList<>(List.of(act)));

        PaginatedList<RiskStrategy> strategies = new PaginatedList<>();
        strategies.setList(new ArrayList<>(List.of(str)));

        PaginatedList<RiskAxis> axes = new PaginatedList<>();
        axes.setList(new ArrayList<>(List.of(axi)));

        PaginatedList<RiskIndicator> indicators = new PaginatedList<>();
        indicators.setList(new ArrayList<>(List.of(ind)));

        PaginatedList<RiskGoal> goals = new PaginatedList<>();
        goals.setList(new ArrayList<>(List.of(goal)));

        List<RiskSharedUnit> sharedUnits = new ArrayList<>(List.of(rsu));
        List<RiskProcessObjective> rpos = new ArrayList<>(List.of(rpo));

        doReturn(activities).when(riskBS).listRiskActivity(dbRisk);
        doReturn(strategies).when(riskBS).listRiskStrategy(dbRisk);
        doReturn(axes).when(riskBS).listRiskAxis(dbRisk);
        doReturn(indicators).when(riskBS).listRiskIndicators(dbRisk);
        doReturn(goals).when(riskBS).listRiskGoals(dbRisk);
        doReturn(rpos).when(riskBS).listRiskProcessObjectives(dbRisk);
        doReturn(sharedUnits).when(riskBS).listRiskSharedUnits(eq(dbRisk), eq(true));

        doNothing().when(riskBS).persist(any(RiskActivity.class));
        doNothing().when(riskBS).persist(any(RiskStrategy.class));
        doNothing().when(riskBS).persist(any(RiskAxis.class));

        riskBS.deleteAPS(old);

        assertTrue(act.isDeleted(), "Activity deve ser marcada como deletada");
        assertTrue(str.isDeleted(), "Strategy deve ser marcada como deletada");
        assertTrue(axi.isDeleted(), "Axis deve ser marcado como deletado");

        verify(riskBS, times(1)).persist(same(act));
        verify(riskBS, times(1)).persist(same(str));
        verify(riskBS, times(1)).persist(same(axi));

        verify(hibernateDAO, times(1)).delete(same(ind));
        verify(hibernateDAO, times(1)).delete(same(goal));
        verify(hibernateDAO, times(1)).delete(same(rsu));
        verify(hibernateDAO, times(1)).delete(same(rpo));
    }

    @DisplayName("deleteAPS: retorna sem ação quando Risk não existe")
    @Test
    void deleteAPS_riskNaoExiste_noop() {
        Risk old = new Risk();
        old.setId(999L);

        when(hibernateDAO.exists(999L, Risk.class)).thenReturn(null);

        riskBS.deleteAPS(old);

        verify(riskBS, never()).listRiskActivity(any());
        verify(riskBS, never()).listRiskStrategy(any());
        verify(riskBS, never()).listRiskAxis(any());
        verify(riskBS, never()).listRiskIndicators(any());
        verify(riskBS, never()).listRiskGoals(any());
        verify(riskBS, never()).listRiskProcessObjectives(any());
        verify(riskBS, never()).listRiskSharedUnits(any(), anyBoolean());

        verify(riskBS, never()).persist(any());
        verify(hibernateDAO, never()).delete(any());
    }

    @DisplayName("delete(Risk): marca deleted=true e persiste")
    @Test
    void delete_risk_marcaDeletedEPersiste() {
        Risk r = new Risk();
        r.setDeleted(false);

        doNothing().when(riskBS).persist(any(Risk.class));

        riskBS.delete(r);

        assertTrue(r.isDeleted(), "Após delete, deleted deve ser true");
        verify(riskBS, times(1)).persist(same(r));
    }

    @DisplayName("delete(Risk): mantém deleted=true e persiste quando já estava deletado (idempotente)")
    @Test
    void delete_risk_jaDeletado_aindaPersiste() {
        Risk r = new Risk();
        r.setDeleted(true);

        doNothing().when(riskBS).persist(any(Risk.class));

        riskBS.delete(r);

        assertTrue(r.isDeleted(), "Deve continuar true");
        verify(riskBS, times(1)).persist(same(r));
    }

    @DisplayName("listRiskById: retorna o risco quando encontrado")
    @Test
    void listRiskById_encontrado_retornaRisco() throws Exception {
        Long id = 42L;
        Risk expected = new Risk();
        expected.setId(id);

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaQuery<Risk> cq = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<Risk> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Risk> typedQuery = mock(TypedQuery.class);
        @SuppressWarnings("unchecked")
        Path<Object> deletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> idPath = mock(Path.class);
        Predicate pDeleted = mock(Predicate.class);
        Predicate pId = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Risk.class)).thenReturn(cq);
        when(cq.from(Risk.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);

        when(root.get("deleted")).thenReturn(deletedPath);
        when(root.get("id")).thenReturn(idPath);
        when(cb.equal(deletedPath, false)).thenReturn(pDeleted);
        when(cb.equal(idPath, id)).thenReturn(pId);
        when(cq.where(pDeleted, pId)).thenReturn(cq);

        when(emMock.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(1)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(expected));

        Risk got = riskBS.listRiskById(id);

        assertNotNull(got, "Deve retornar um Risk quando encontrado");
        assertEquals(id, got.getId());

        verify(typedQuery).setMaxResults(1);
    }

    @DisplayName("listRiskById: retorna null quando não encontrado")
    @Test
    void listRiskById_naoEncontrado_retornaNull() throws Exception {
        Long id = 999L;

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaQuery<Risk> cq = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<Risk> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Risk> typedQuery = mock(TypedQuery.class);
        @SuppressWarnings("unchecked")
        Path<Object> deletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> idPath = mock(Path.class);
        Predicate pDeleted = mock(Predicate.class);
        Predicate pId = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Risk.class)).thenReturn(cq);
        when(cq.from(Risk.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);

        when(root.get("deleted")).thenReturn(deletedPath);
        when(root.get("id")).thenReturn(idPath);
        when(cb.equal(deletedPath, false)).thenReturn(pDeleted);
        when(cb.equal(idPath, id)).thenReturn(pId);
        when(cq.where(pDeleted, pId)).thenReturn(cq);

        when(emMock.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(1)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        Risk got = riskBS.listRiskById(id);

        assertNull(got, "Deve retornar null quando nenhum Risk é encontrado");
        verify(typedQuery).setMaxResults(1);
    }

    @DisplayName("listRiskLevel(Policy): cria RiskLevels para entradas com cor não-nula")
    @Test
    void listRiskLevel_ok_filtraNulos() {
        Policy policy = new Policy();
        String[][] matrix = new String[][] {
                { "Baixo", "Médio", "Alto" },
                { "16711680", null, "255" } // só 2 cores válidas
        };
        policy.setRisk_level(matrix);

        PaginatedList<RiskLevel> result = riskBS.listRiskLevel(policy);

        assertNotNull(result);
        assertNotNull(result.getList());
        assertEquals(2, result.getList().size(), "Deve criar RiskLevel apenas para cores não-nulas");
        assertEquals(2L, result.getTotal(), "Total deve refletir quantidade criada");

        RiskLevel r0 = result.getList().get(0);
        assertNull(r0.getId(), "ID deve ser null (não salvo)");
        assertEquals(16711680, r0.getColor());
        assertEquals("Baixo", r0.getLevel());
        assertSame(policy, r0.getPolicy());

        RiskLevel r1 = result.getList().get(1);
        assertNull(r1.getId());
        assertEquals(255, r1.getColor());
        assertEquals("Alto", r1.getLevel());
        assertSame(policy, r1.getPolicy());
    }

    @DisplayName("listRiskLevel(Policy): retorna lista vazia e total=0 quando todas as cores são nulas")
    @Test
    void listRiskLevel_todasCoresNulas() {
        Policy policy = new Policy();
        String[][] matrix = new String[][] {
                { "Baixo", "Médio" },
                { null, null }
        };
        policy.setRisk_level(matrix);

        PaginatedList<RiskLevel> result = riskBS.listRiskLevel(policy);

        assertNotNull(result);
        assertNotNull(result.getList());
        assertTrue(result.getList().isEmpty(), "Lista deve estar vazia");
        assertEquals(0L, result.getTotal(), "Total deve ser 0");
    }

    @DisplayName("listRiskLevel(Policy): lança NumberFormatException quando a cor não é numérica")
    @Test
    void listRiskLevel_corInvalida_lancaNumberFormatException() {
        Policy policy = new Policy();
        String[][] matrix = new String[][] {
                { "Crítico" },
                { "not-a-number" }
        };
        policy.setRisk_level(matrix);

        assertThrows(NumberFormatException.class, () -> riskBS.listRiskLevel(policy),
                "Deve lançar NumberFormatException para cor inválida");
    }

    @DisplayName("listRiskLevelByPolicy: retorna lista e total quando há registros")
    @Test
    void listRiskLevelByPolicy_comResultados() throws Exception {
        Policy policy = new Policy();

        // Mocks do Criteria para COUNT
        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cbCount = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskLevel> countRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        Path<Object> countDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countPolicyPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> idPath = mock(Path.class);
        Predicate predDelCount = mock(Predicate.class);
        Predicate predPolCount = mock(Predicate.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTQ = mock(TypedQuery.class);

        // Mocks do Criteria para LIST
        CriteriaBuilder cbList = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskLevel> listCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskLevel> listRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        Path<Object> listDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> listPolicyPath = mock(Path.class);
        Predicate predDelList = mock(Predicate.class);
        Predicate predPolList = mock(Predicate.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskLevel> listTQ = mock(TypedQuery.class);

        // Injeta o EntityManager no RiskBS
        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        // em.getCriteriaBuilder() é chamado duas vezes: count e list
        when(emMock.getCriteriaBuilder()).thenReturn(cbCount, cbList);

        // ----- COUNT -----
        when(cbCount.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(RiskLevel.class)).thenReturn(countRoot);
        when(countCQ.select(any())).thenReturn(countCQ); // select(countDistinct(...))
        when(countRoot.get("id")).thenReturn(idPath);
        when(countRoot.get("deleted")).thenReturn(countDeletedPath);
        when(countRoot.get("policy")).thenReturn(countPolicyPath);
        when(cbCount.equal(countDeletedPath, false)).thenReturn(predDelCount);
        when(cbCount.equal(countPolicyPath, policy)).thenReturn(predPolCount);
        when(countCQ.where(predDelCount, predPolCount)).thenReturn(countCQ);
        when(emMock.createQuery(countCQ)).thenReturn(countTQ);
        when(countTQ.getSingleResult()).thenReturn(3L);

        // ----- LIST -----
        when(cbList.createQuery(RiskLevel.class)).thenReturn(listCQ);
        when(listCQ.from(RiskLevel.class)).thenReturn(listRoot);
        when(listCQ.select(listRoot)).thenReturn(listCQ);
        when(listRoot.get("deleted")).thenReturn(listDeletedPath);
        when(listRoot.get("policy")).thenReturn(listPolicyPath);
        when(cbList.equal(listDeletedPath, false)).thenReturn(predDelList);
        when(cbList.equal(listPolicyPath, policy)).thenReturn(predPolList);
        when(listCQ.where(predDelList, predPolList)).thenReturn(listCQ);
        when(emMock.createQuery(listCQ)).thenReturn(listTQ);

        RiskLevel rl1 = new RiskLevel();
        rl1.setId(10L);
        RiskLevel rl2 = new RiskLevel();
        rl2.setId(20L);
        when(listTQ.getResultList()).thenReturn(List.of(rl1, rl2));

        // Execução
        PaginatedList<RiskLevel> result = riskBS.listRiskLevelByPolicy(policy);

        // Asserções
        assertNotNull(result);
        assertEquals(3L, result.getTotal(), "Total deve vir do COUNT");
        assertNotNull(result.getList());
        assertEquals(2, result.getList().size(), "Lista deve vir da query de listagem");
        assertSame(rl1, result.getList().get(0));
        assertSame(rl2, result.getList().get(1));
    }

    @DisplayName("listRiskLevelByPolicy: retorna total=0 e lista vazia quando não há registros")
    @Test
    void listRiskLevelByPolicy_semResultados() throws Exception {
        Policy policy = new Policy();

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cbCount = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskLevel> countRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        Path<Object> countDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countPolicyPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> idPath = mock(Path.class);
        Predicate predDelCount = mock(Predicate.class);
        Predicate predPolCount = mock(Predicate.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTQ = mock(TypedQuery.class);

        CriteriaBuilder cbList = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskLevel> listCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskLevel> listRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        Path<Object> listDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> listPolicyPath = mock(Path.class);
        Predicate predDelList = mock(Predicate.class);
        Predicate predPolList = mock(Predicate.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskLevel> listTQ = mock(TypedQuery.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cbCount, cbList);

        when(cbCount.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(RiskLevel.class)).thenReturn(countRoot);
        when(countCQ.select(any())).thenReturn(countCQ);
        when(countRoot.get("id")).thenReturn(idPath);
        when(countRoot.get("deleted")).thenReturn(countDeletedPath);
        when(countRoot.get("policy")).thenReturn(countPolicyPath);
        when(cbCount.equal(countDeletedPath, false)).thenReturn(predDelCount);
        when(cbCount.equal(countPolicyPath, policy)).thenReturn(predPolCount);
        when(countCQ.where(predDelCount, predPolCount)).thenReturn(countCQ);
        when(emMock.createQuery(countCQ)).thenReturn(countTQ);
        when(countTQ.getSingleResult()).thenReturn(0L);

        when(cbList.createQuery(RiskLevel.class)).thenReturn(listCQ);
        when(listCQ.from(RiskLevel.class)).thenReturn(listRoot);
        when(listCQ.select(listRoot)).thenReturn(listCQ);
        when(listRoot.get("deleted")).thenReturn(listDeletedPath);
        when(listRoot.get("policy")).thenReturn(listPolicyPath);
        when(cbList.equal(listDeletedPath, false)).thenReturn(predDelList);
        when(cbList.equal(listPolicyPath, policy)).thenReturn(predPolList);
        when(listCQ.where(predDelList, predPolList)).thenReturn(listCQ);
        when(emMock.createQuery(listCQ)).thenReturn(listTQ);
        when(listTQ.getResultList()).thenReturn(List.of());

        PaginatedList<RiskLevel> result = riskBS.listRiskLevelByPolicy(policy);

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertNotNull(result.getList());
        assertTrue(result.getList().isEmpty(), "Lista deve estar vazia quando não há registros");
    }

    @DisplayName("listRiskReducedByUnits: com lista vazia retorna Collections.emptyList()")
    @Test
    void listRiskReducedByUnits_listaVazia() {
        List<RiskBean> result = riskBS.listRiskReducedByUnits(new ArrayList<>());
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Deve retornar lista vazia quando units está vazio");
    }

    @DisplayName("filterRisks(planRiskId, filters): usa DefaultParams.createWithMaxPageSize e retorna a lista da versão paginada")
    @Test
    void filterRisks_delegadorUsaMaxPageSizeERetornaList() {
        Long planRiskId = 55L;
        RiskFilterParams filters = new RiskFilterParams();
        PaginatedList<Risk> page = new PaginatedList<>();
        List<Risk> expected = new ArrayList<>();
        expected.add(new Risk());
        page.setList(expected);

        try (MockedStatic<DefaultParams> mocked = mockStatic(DefaultParams.class)) {
            DefaultParams maxParams = mock(DefaultParams.class);
            mocked.when(DefaultParams::createWithMaxPageSize).thenReturn(maxParams);

            doReturn(page).when(riskBS).filterRisks(eq(planRiskId), same(filters), same(maxParams));

            List<Risk> result = riskBS.filterRisks(planRiskId, filters);

            assertSame(expected, result, "Deve retornar exatamente a lista da versão paginada");

            verify(riskBS, times(1)).filterRisks(eq(planRiskId), same(filters), same(maxParams));
        }
    }

    @DisplayName("filterFullRisks: quando lista está vazia não lista coleções e retorna vazio")
    @Test
    void filterFullRisks_listaVazia_noop() {
        Long planRiskId = 88L;
        RiskFilterParams filters = new RiskFilterParams();

        PaginatedList<Risk> page = new PaginatedList<>();
        page.setList(new ArrayList<>());

        try (MockedStatic<DefaultParams> mocked = mockStatic(DefaultParams.class)) {
            DefaultParams maxParams = mock(DefaultParams.class);
            mocked.when(DefaultParams::createWithMaxPageSize).thenReturn(maxParams);

            doReturn(page).when(riskBS).filterRisks(eq(planRiskId), same(filters), same(maxParams));

            List<Risk> result = riskBS.filterFullRisks(planRiskId, filters);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(riskBS, times(1)).filterRisks(eq(planRiskId), same(filters), same(maxParams));
            verify(riskBS, never()).listRiskStrategy(any(Risk.class));
            verify(riskBS, never()).listRiskAxis(any(Risk.class));
            verify(riskBS, never()).listRiskIndicators(any(Risk.class));
            verify(riskBS, never()).listRiskGoals(any(Risk.class));
            verify(riskBS, never()).listRiskActivity(any(Risk.class));
        }
    }

    @DisplayName("listRiskByUnitList: units nulo ou vazio -> retorna imediatamente (sem usar EntityManager)")
    @Test
    void listRiskByUnitList_unidadesVazias_retornaImediato() throws Exception {
        EntityManager emMock = mock(EntityManager.class);
        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        PaginatedList<Risk> resNull = riskBS.listRiskByUnitList(null);
        assertNotNull(resNull);
        assertTrue(resNull.getList() == null || resNull.getList().isEmpty(),
                "Com units nulo, a lista deve ser nula ou vazia");
        assertTrue(resNull.getTotal() == null || resNull.getTotal() == 0L,
                "Com units nulo, total deve ser nulo ou 0");

        PaginatedList<Risk> resEmpty = riskBS.listRiskByUnitList(new ArrayList<>());
        assertNotNull(resEmpty);
        assertTrue(resEmpty.getList() == null || resEmpty.getList().isEmpty(),
                "Com units vazio, a lista deve ser nula ou vazia");
        assertTrue(resEmpty.getTotal() == null || resEmpty.getTotal() == 0L,
                "Com units vazio, total deve ser nulo ou 0");

        verifyNoInteractions(emMock);
    }

    @DisplayName("listRiskByUnitList: monta criteria com deleted=false e unit IN (units) e retorna lista+total")
    @Test
    void listRiskByUnitList_ok_retornaListaETotal() throws Exception {
        Unit u1 = unit(10L);
        Unit u2 = unit(20L);
        List<Unit> units = new ArrayList<>(List.of(u1, u2));

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaQuery<Risk> cq = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<Risk> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Risk> tq = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        Path<Object> deletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> unitPath = mock(Path.class);
        Predicate notDeleted = mock(Predicate.class);
        Predicate inUnits = mock(Predicate.class);
        Predicate andPred = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Risk.class)).thenReturn(cq);
        when(cq.from(Risk.class)).thenReturn(root);

        when(root.get("deleted")).thenReturn(deletedPath);
        when(root.get("unit")).thenReturn(unitPath);

        when(cb.equal(deletedPath, false)).thenReturn(notDeleted);
        when(unitPath.in(units)).thenReturn(inUnits);
        when(cb.and(notDeleted, inUnits)).thenReturn(andPred);

        when(cq.select(root)).thenReturn(cq);
        when(cq.where(andPred)).thenReturn(cq);

        when(emMock.createQuery(cq)).thenReturn(tq);

        Risk r1 = new Risk();
        r1.setId(1L);
        Risk r2 = new Risk();
        r2.setId(2L);
        List<Risk> found = new ArrayList<>(List.of(r1, r2));
        when(tq.getResultList()).thenReturn(found);

        PaginatedList<Risk> results = riskBS.listRiskByUnitList(units);

        assertNotNull(results);
        assertSame(found, results.getList(), "Deve devolver exatamente a lista da query");
        assertEquals(2L, results.getTotal(), "Total deve ser o tamanho da lista");

        verify(cb).equal(deletedPath, false);
        verify(unitPath).in(units);
        verify(cb).and(notDeleted, inUnits);
        verify(cq).select(root);
        verify(cq).where(andPred);
        verify(emMock).createQuery(cq);
        verify(tq).getResultList();
    }

    @DisplayName("resolvePolicy: quando policy é fornecida, retorna a mesma e não consulta o banco")
    @Test
    void resolvePolicy_policyNaoNula_shortCircuit() throws Exception {
        Policy provided = new Policy();
        provided.setId(999L);

        Risk risk = new Risk();
        risk.setUnit(unit(10L));

        Method m = RiskBS.class.getDeclaredMethod("resolvePolicy", Risk.class, Policy.class);
        m.setAccessible(true);
        Policy result = (Policy) m.invoke(riskBS, risk, provided);

        assertSame(provided, result, "Deveria retornar exatamente a Policy fornecida");
        verify(riskBS, never()).exists(anyLong(), any());
    }

    @DisplayName("resolvePolicy: unit inexistente -> retorna null")
    @Test
    void resolvePolicy_unitInexistente_retornaNull() throws Exception {
        Risk risk = new Risk();
        risk.setUnit(unit(10L));

        doReturn(null).when(riskBS).exists(eq(10L), eq(Unit.class));

        Method m = RiskBS.class.getDeclaredMethod("resolvePolicy", Risk.class, Policy.class);
        m.setAccessible(true);
        Policy result = (Policy) m.invoke(riskBS, risk, (Policy) null);

        assertNull(result, "Quando a unidade não existe, deve retornar null");
        verify(riskBS, times(1)).exists(eq(10L), eq(Unit.class));
        verify(riskBS, never()).exists(anyLong(), eq(PlanRisk.class));
        verify(riskBS, never()).exists(anyLong(), eq(Policy.class));
    }

    @DisplayName("resolvePolicy: planRisk inexistente -> retorna null")
    @Test
    void resolvePolicy_planRiskInexistente_retornaNull() throws Exception {
        Risk risk = new Risk();
        risk.setUnit(unit(10L));

        Unit loadedUnit = unit(10L);
        PlanRisk prRef = new PlanRisk();
        prRef.setId(20L);
        loadedUnit.setPlanRisk(prRef);

        doReturn(loadedUnit).when(riskBS).exists(eq(10L), eq(Unit.class));
        doReturn(null).when(riskBS).exists(eq(20L), eq(PlanRisk.class));

        Method m = RiskBS.class.getDeclaredMethod("resolvePolicy", Risk.class, Policy.class);
        m.setAccessible(true);
        Policy result = (Policy) m.invoke(riskBS, risk, (Policy) null);

        assertNull(result, "Quando o PlanRisk não existe, deve retornar null");
        verify(riskBS, times(1)).exists(eq(10L), eq(Unit.class));
        verify(riskBS, times(1)).exists(eq(20L), eq(PlanRisk.class));
        verify(riskBS, never()).exists(anyLong(), eq(Policy.class));
    }

    @DisplayName("resolvePolicy: policy encontrada -> retorna a Policy carregada")
    @Test
    void resolvePolicy_policyEncontrada_ok() throws Exception {
        Risk risk = new Risk();
        risk.setUnit(unit(10L));

        Unit loadedUnit = unit(10L);
        PlanRisk prRef = new PlanRisk();
        prRef.setId(20L);
        loadedUnit.setPlanRisk(prRef);
        doReturn(loadedUnit).when(riskBS).exists(eq(10L), eq(Unit.class));

        PlanRisk loadedPR = new PlanRisk();
        Policy polRef = new Policy();
        polRef.setId(30L);
        loadedPR.setPolicy(polRef);
        doReturn(loadedPR).when(riskBS).exists(eq(20L), eq(PlanRisk.class));

        Policy loadedPolicy = new Policy();
        loadedPolicy.setId(30L);
        doReturn(loadedPolicy).when(riskBS).exists(eq(30L), eq(Policy.class));

        Method m = RiskBS.class.getDeclaredMethod("resolvePolicy", Risk.class, Policy.class);
        m.setAccessible(true);
        Policy result = (Policy) m.invoke(riskBS, risk, (Policy) null);

        assertSame(loadedPolicy, result, "Deve retornar a Policy obtida via exists");
        verify(riskBS).exists(eq(10L), eq(Unit.class));
        verify(riskBS).exists(eq(20L), eq(PlanRisk.class));
        verify(riskBS).exists(eq(30L), eq(Policy.class));
    }

    @DisplayName("resolvePolicy: policy não encontrada -> lança IllegalStateException('Policy not found')")
    @Test
    void resolvePolicy_policyNaoEncontrada_excecao() throws Exception {
        Risk risk = new Risk();
        risk.setUnit(unit(10L));

        Unit loadedUnit = unit(10L);
        PlanRisk prRef = new PlanRisk();
        prRef.setId(20L);
        loadedUnit.setPlanRisk(prRef);
        doReturn(loadedUnit).when(riskBS).exists(eq(10L), eq(Unit.class));

        PlanRisk loadedPR = new PlanRisk();
        Policy polRef = new Policy();
        polRef.setId(30L);
        loadedPR.setPolicy(polRef);
        doReturn(loadedPR).when(riskBS).exists(eq(20L), eq(PlanRisk.class));

        doReturn(null).when(riskBS).exists(eq(30L), eq(Policy.class));

        Method m = RiskBS.class.getDeclaredMethod("resolvePolicy", Risk.class, Policy.class);
        m.setAccessible(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            try {
                m.invoke(riskBS, risk, (Policy) null);
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getTargetException();
                if (cause instanceof RuntimeException re)
                    throw re;
                throw new RuntimeException(cause);
            }
        });

        assertTrue(ex.getMessage().contains("Policy not found"));
        verify(riskBS).exists(eq(10L), eq(Unit.class));
        verify(riskBS).exists(eq(20L), eq(PlanRisk.class));
        verify(riskBS).exists(eq(30L), eq(Policy.class));
    }

    @DisplayName("listRiskByLevel: pagina, cria aliases, aplica restrições/ordem e retorna lista + total")
    @Test
    void listRiskByLevel_ok() {
        PlanRisk planRisk = new PlanRisk();
        planRisk.setId(5L);
        String level = "ALTO";
        Integer page = 3, pageSize = 25;

        org.forpdi.core.common.Criteria criteria = mock(org.forpdi.core.common.Criteria.class);
        org.forpdi.core.common.Criteria counting = mock(org.forpdi.core.common.Criteria.class);

        when(hibernateDAO.newCriteria(Risk.class)).thenReturn(criteria, counting);
        when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
        when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
        when(criteria.addOrder(any(org.forpdi.core.common.Order.class))).thenReturn(criteria);
        when(criteria.createAlias(anyString(), anyString())).thenReturn(criteria);
        when(criteria.add(any())).thenReturn(criteria);

        when(counting.createAlias(anyString(), anyString())).thenReturn(counting);
        when(counting.add(any())).thenReturn(counting);
        when(counting.setProjection(any())).thenReturn(counting);

        List<Risk> expected = new ArrayList<>(List.of(new Risk(), new Risk()));
        when(hibernateDAO.findByCriteria(criteria, Risk.class)).thenReturn(expected);
        when(counting.uniqueResult()).thenReturn(42L);

        PaginatedList<Risk> result = riskBS.listRiskByLevel(planRisk, level, page, pageSize);

        assertNotNull(result);
        assertSame(expected, result.getList());
        assertEquals(42L, result.getTotal());

        verify(criteria).setFirstResult((page - 1) * pageSize);
        verify(criteria).setMaxResults(pageSize);
        verify(criteria).addOrder(any(org.forpdi.core.common.Order.class));

        verify(criteria).createAlias("unit", "unit");
        verify(criteria).createAlias("riskLevel", "riskLevel");
        verify(criteria, times(3)).add(any());

        verify(counting).createAlias("unit", "unit");
        verify(counting).createAlias("riskLevel", "riskLevel");
        verify(counting, times(3)).add(any());
        verify(counting).setProjection(any());

        verify(hibernateDAO).findByCriteria(criteria, Risk.class);
        verify(counting).uniqueResult();
    }

    @DisplayName("listRiskByLevel: quando page e pageSize são nulos usa defaults (page=1, pageSize=10)")
    @Test
    void listRiskByLevel_defaultsQuandoNulos() {
        PlanRisk planRisk = new PlanRisk();
        planRisk.setId(9L);
        String level = "BAIXO";

        org.forpdi.core.common.Criteria criteria = mock(org.forpdi.core.common.Criteria.class);
        org.forpdi.core.common.Criteria counting = mock(org.forpdi.core.common.Criteria.class);

        when(hibernateDAO.newCriteria(Risk.class)).thenReturn(criteria, counting);
        when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
        when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
        when(criteria.addOrder(any(org.forpdi.core.common.Order.class))).thenReturn(criteria);
        when(criteria.createAlias(anyString(), anyString())).thenReturn(criteria);
        when(criteria.add(any())).thenReturn(criteria);

        when(counting.createAlias(anyString(), anyString())).thenReturn(counting);
        when(counting.add(any())).thenReturn(counting);
        when(counting.setProjection(any())).thenReturn(counting);

        when(hibernateDAO.findByCriteria(criteria, Risk.class)).thenReturn(new ArrayList<>());
        when(counting.uniqueResult()).thenReturn(0L);

        PaginatedList<Risk> result = riskBS.listRiskByLevel(planRisk, level, null, null);

        assertNotNull(result);
        assertTrue(result.getList().isEmpty());
        assertEquals(0L, result.getTotal());

        verify(criteria).setFirstResult(0);
        verify(criteria).setMaxResults(10);
        verify(criteria).addOrder(any(org.forpdi.core.common.Order.class));
        verify(criteria).createAlias("unit", "unit");
        verify(criteria).createAlias("riskLevel", "riskLevel");
        verify(criteria, times(3)).add(any());

        verify(counting).createAlias("unit", "unit");
        verify(counting).createAlias("riskLevel", "riskLevel");
        verify(counting, times(3)).add(any());
        verify(counting).setProjection(any());
    }

    @DisplayName("listRiskByUser: pagina, aplica restrições, usa sort custom quando params.isSorting=true e retorna lista+total")
    @Test
    void listRiskByUser_sortingTrue_usaSortCustom() {
        Long userId = 123L;

        DefaultParams params = mock(DefaultParams.class);
        when(params.getPage()).thenReturn(3);
        when(params.getPageSize()).thenReturn(12);
        when(params.isSorting()).thenReturn(true);
        org.forpdi.core.common.Order sortOrder = mock(org.forpdi.core.common.Order.class);
        when(params.getSortOrder()).thenReturn(sortOrder);

        org.forpdi.core.common.Criteria criteria = mock(org.forpdi.core.common.Criteria.class);
        org.forpdi.core.common.Criteria counting = mock(org.forpdi.core.common.Criteria.class);
        when(hibernateDAO.newCriteria(Risk.class)).thenReturn(criteria, counting);

        when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
        when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
        when(criteria.add(any())).thenReturn(criteria);
        when(criteria.addOrder(any(org.forpdi.core.common.Order.class))).thenReturn(criteria);

        when(counting.add(any())).thenReturn(counting);
        when(counting.setProjection(any())).thenReturn(counting);

        List<Risk> expected = new ArrayList<>(List.of(new Risk(), new Risk(), new Risk()));
        when(hibernateDAO.findByCriteria(criteria, Risk.class)).thenReturn(expected);
        when(counting.uniqueResult()).thenReturn(17L);

        PaginatedList<Risk> result = riskBS.listRiskByUser(userId, params);

        assertNotNull(result);
        assertSame(expected, result.getList(), "Deve retornar a lista do findByCriteria");
        assertEquals(17L, result.getTotal(), "Total deve vir do counting.uniqueResult");

        verify(criteria).setFirstResult((3 - 1) * 12);
        verify(criteria).setMaxResults(12);

        verify(criteria, times(2)).add(any());
        verify(counting, times(2)).add(any());
        verify(counting).setProjection(any());

        verify(criteria).addOrder(same(sortOrder));
        verify(hibernateDAO).findByCriteria(same(criteria), eq(Risk.class));
        verify(counting).uniqueResult();
    }

    @DisplayName("listRiskByUser: quando params.isSorting=false usa Order.asc(\"name\") por padrão")
    @Test
    void listRiskByUser_sortingFalse_usaAscPorNome() {
        Long userId = 456L;

        DefaultParams params = mock(DefaultParams.class);
        when(params.getPage()).thenReturn(1);
        when(params.getPageSize()).thenReturn(10);
        when(params.isSorting()).thenReturn(false);

        org.forpdi.core.common.Criteria criteria = mock(org.forpdi.core.common.Criteria.class);
        org.forpdi.core.common.Criteria counting = mock(org.forpdi.core.common.Criteria.class);
        when(hibernateDAO.newCriteria(Risk.class)).thenReturn(criteria, counting);

        when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
        when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
        when(criteria.add(any())).thenReturn(criteria);
        when(criteria.addOrder(any(org.forpdi.core.common.Order.class))).thenReturn(criteria);

        when(counting.add(any())).thenReturn(counting);
        when(counting.setProjection(any())).thenReturn(counting);

        when(hibernateDAO.findByCriteria(criteria, Risk.class)).thenReturn(new ArrayList<>());
        when(counting.uniqueResult()).thenReturn(0L);

        PaginatedList<Risk> result = riskBS.listRiskByUser(userId, params);

        assertNotNull(result);
        assertTrue(result.getList().isEmpty());
        assertEquals(0L, result.getTotal());

        verify(criteria).setFirstResult(0);
        verify(criteria).setMaxResults(10);

        verify(criteria, times(2)).add(any());
        verify(counting, times(2)).add(any());
        verify(counting).setProjection(any());

        verify(params, never()).getSortOrder();
        verify(criteria).addOrder(any(org.forpdi.core.common.Order.class));

        verify(hibernateDAO).findByCriteria(same(criteria), eq(Risk.class));
        verify(counting).uniqueResult();
    }

    @DisplayName("listRiskStrategy: retorna lista e total quando há vínculos")
    @Test
    void listRiskStrategy_comResultados() throws Exception {
        Risk risk = new Risk();
        risk.setId(1L);

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskStrategy> listCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskStrategy> listRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskStrategy> listTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskStrategy> countRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        Path<Object> listDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> listRiskPath = mock(Path.class);
        Predicate predListDeleted = mock(Predicate.class);
        Predicate predListRisk = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        Path<Object> countDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countRiskPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countIdPath = mock(Path.class);
        Predicate predCountDeleted = mock(Predicate.class);
        Predicate predCountRisk = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);

        when(cb.createQuery(RiskStrategy.class)).thenReturn(listCQ);
        when(listCQ.from(RiskStrategy.class)).thenReturn(listRoot);
        when(listCQ.select(listRoot)).thenReturn(listCQ);

        when(listRoot.get("deleted")).thenReturn(listDeletedPath);
        when(listRoot.get("risk")).thenReturn(listRiskPath);

        when(cb.equal(listDeletedPath, false)).thenReturn(predListDeleted);
        when(cb.equal(listRiskPath, risk)).thenReturn(predListRisk);
        when(listCQ.where(predListDeleted, predListRisk)).thenReturn(listCQ);

        when(emMock.createQuery(listCQ)).thenReturn(listTQ);

        RiskStrategy rs1 = new RiskStrategy();
        rs1.setId(10L);
        RiskStrategy rs2 = new RiskStrategy();
        rs2.setId(20L);
        when(listTQ.getResultList()).thenReturn(List.of(rs1, rs2));

        when(cb.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(RiskStrategy.class)).thenReturn(countRoot);

        when(countRoot.get("id")).thenReturn(countIdPath);
        when(countRoot.get("deleted")).thenReturn(countDeletedPath);
        when(countRoot.get("risk")).thenReturn(countRiskPath);

        when(cb.equal(countDeletedPath, false)).thenReturn(predCountDeleted);
        when(cb.equal(countRiskPath, risk)).thenReturn(predCountRisk);
        when(countCQ.select(any())).thenReturn(countCQ);
        when(countCQ.where(predCountDeleted, predCountRisk)).thenReturn(countCQ);

        when(emMock.createQuery(countCQ)).thenReturn(countTQ);
        when(countTQ.getSingleResult()).thenReturn(2L);

        PaginatedList<RiskStrategy> result = riskBS.listRiskStrategy(risk);

        assertNotNull(result);
        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getList().size());
        assertSame(rs1, result.getList().get(0));
        assertSame(rs2, result.getList().get(1));
        verify(emMock).getCriteriaBuilder();
        verify(emMock).createQuery(listCQ);
        verify(emMock).createQuery(countCQ);
        verify(listTQ).getResultList();
        verify(countTQ).getSingleResult();
    }

    @DisplayName("listRiskStrategy: retorna total=0 e lista vazia quando não há vínculos")
    @Test
    void listRiskStrategy_semResultados() throws Exception {
        Risk risk = new Risk();
        risk.setId(2L);

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskStrategy> listCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskStrategy> listRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskStrategy> listTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskStrategy> countRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        Path<Object> listDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> listRiskPath = mock(Path.class);
        Predicate predListDeleted = mock(Predicate.class);
        Predicate predListRisk = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        Path<Object> countDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countRiskPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countIdPath = mock(Path.class);
        Predicate predCountDeleted = mock(Predicate.class);
        Predicate predCountRisk = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);

        when(cb.createQuery(RiskStrategy.class)).thenReturn(listCQ);
        when(listCQ.from(RiskStrategy.class)).thenReturn(listRoot);
        when(listCQ.select(listRoot)).thenReturn(listCQ);
        when(listRoot.get("deleted")).thenReturn(listDeletedPath);
        when(listRoot.get("risk")).thenReturn(listRiskPath);
        when(cb.equal(listDeletedPath, false)).thenReturn(predListDeleted);
        when(cb.equal(listRiskPath, risk)).thenReturn(predListRisk);
        when(listCQ.where(predListDeleted, predListRisk)).thenReturn(listCQ);
        when(emMock.createQuery(listCQ)).thenReturn(listTQ);
        when(listTQ.getResultList()).thenReturn(List.of());

        when(cb.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(RiskStrategy.class)).thenReturn(countRoot);
        when(countRoot.get("id")).thenReturn(countIdPath);
        when(countRoot.get("deleted")).thenReturn(countDeletedPath);
        when(countRoot.get("risk")).thenReturn(countRiskPath);
        when(cb.equal(countDeletedPath, false)).thenReturn(predCountDeleted);
        when(cb.equal(countRiskPath, risk)).thenReturn(predCountRisk);
        when(countCQ.select(any())).thenReturn(countCQ);
        when(countCQ.where(predCountDeleted, predCountRisk)).thenReturn(countCQ);
        when(emMock.createQuery(countCQ)).thenReturn(countTQ);
        when(countTQ.getSingleResult()).thenReturn(0L);

        PaginatedList<RiskStrategy> result = riskBS.listRiskStrategy(risk);

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertNotNull(result.getList());
        assertTrue(result.getList().isEmpty());

        verify(listTQ).getResultList();
        verify(countTQ).getSingleResult();
    }

    @DisplayName("listReducedRiskStrategy: retorna lista preenchida")
    @Test
    void listReducedRiskStrategy_comResultados() throws Exception {
        Risk risk = new Risk();
        risk.setId(1L);

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskStrategyBean> cq = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskStrategy> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskStrategyBean> tq = mock(TypedQuery.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(RiskStrategyBean.class)).thenReturn(cq);
        when(cq.from(RiskStrategy.class)).thenReturn(root);

        lenient().when(cq.select(any())).thenReturn(cq);

        @SuppressWarnings("unchecked")
        Path<Object> genericPath = mock(Path.class);
        lenient().when(root.get(anyString())).thenReturn(genericPath);
        lenient().when(genericPath.get(anyString())).thenReturn(genericPath);

        @SuppressWarnings("unchecked")
        Path<Object> deletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> riskPath = mock(Path.class);
        when(root.get("deleted")).thenReturn(deletedPath);
        when(root.get("risk")).thenReturn(riskPath);

        Predicate predDeleted = mock(Predicate.class);
        Predicate predRisk = mock(Predicate.class);
        when(cb.equal(deletedPath, false)).thenReturn(predDeleted);
        when(cb.equal(riskPath, risk)).thenReturn(predRisk);
        when(cq.where(predDeleted, predRisk)).thenReturn(cq);

        when(emMock.createQuery(cq)).thenReturn(tq);

        RiskStrategyBean b1 = new RiskStrategyBean();
        RiskStrategyBean b2 = new RiskStrategyBean();
        when(tq.getResultList()).thenReturn(List.of(b1, b2));

        PaginatedList<RiskStrategyBean> result = riskBS.listReducedRiskStrategy(risk);

        assertNotNull(result);
        assertNotNull(result.getList());
        assertEquals(2, result.getList().size());
        assertSame(b1, result.getList().get(0));
        assertSame(b2, result.getList().get(1));

        verify(emMock).getCriteriaBuilder();
        verify(emMock).createQuery(cq);
        verify(tq).getResultList();
    }

    @DisplayName("listReducedRiskStrategy: retorna lista vazia quando não há vínculos")
    @Test
    void listReducedRiskStrategy_semResultados() throws Exception {
        Risk risk = new Risk();
        risk.setId(2L);

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskStrategyBean> cq = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskStrategy> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskStrategyBean> tq = mock(TypedQuery.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(RiskStrategyBean.class)).thenReturn(cq);
        when(cq.from(RiskStrategy.class)).thenReturn(root);
        lenient().when(cq.select(any())).thenReturn(cq);

        @SuppressWarnings("unchecked")
        Path<Object> genericPath = mock(Path.class);
        lenient().when(root.get(anyString())).thenReturn(genericPath);
        lenient().when(genericPath.get(anyString())).thenReturn(genericPath);

        @SuppressWarnings("unchecked")
        Path<Object> deletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> riskPath = mock(Path.class);
        when(root.get("deleted")).thenReturn(deletedPath);
        when(root.get("risk")).thenReturn(riskPath);

        Predicate predDeleted = mock(Predicate.class);
        Predicate predRisk = mock(Predicate.class);
        when(cb.equal(deletedPath, false)).thenReturn(predDeleted);
        when(cb.equal(riskPath, risk)).thenReturn(predRisk);
        when(cq.where(predDeleted, predRisk)).thenReturn(cq);

        when(emMock.createQuery(cq)).thenReturn(tq);
        when(tq.getResultList()).thenReturn(List.of()); // vazio

        PaginatedList<RiskStrategyBean> result = riskBS.listReducedRiskStrategy(risk);

        assertNotNull(result);
        assertNotNull(result.getList());
        assertTrue(result.getList().isEmpty());

        verify(emMock).createQuery(cq);
        verify(tq).getResultList();
    }

    @DisplayName("listRiskAxis: retorna lista e total quando há vínculos")
    @Test
    void listRiskAxis_comResultados() throws Exception {
        Risk risk = new Risk();
        risk.setId(1L);

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskAxis> listCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskAxis> listRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskAxis> listTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskAxis> countRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        Path<Object> listDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> listRiskPath = mock(Path.class);
        Predicate predListDeleted = mock(Predicate.class);
        Predicate predListRisk = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        Path<Object> countIdPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countRiskPath = mock(Path.class);
        Predicate predCountDeleted = mock(Predicate.class);
        Predicate predCountRisk = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);

        when(cb.createQuery(RiskAxis.class)).thenReturn(listCQ);
        when(listCQ.from(RiskAxis.class)).thenReturn(listRoot);
        when(listCQ.select(listRoot)).thenReturn(listCQ);

        when(listRoot.get("deleted")).thenReturn(listDeletedPath);
        when(listRoot.get("risk")).thenReturn(listRiskPath);

        when(cb.equal(listDeletedPath, false)).thenReturn(predListDeleted);
        when(cb.equal(listRiskPath, risk)).thenReturn(predListRisk);
        when(listCQ.where(predListDeleted, predListRisk)).thenReturn(listCQ);

        when(emMock.createQuery(listCQ)).thenReturn(listTQ);

        RiskAxis a1 = new RiskAxis();
        a1.setId(10L);
        RiskAxis a2 = new RiskAxis();
        a2.setId(20L);
        when(listTQ.getResultList()).thenReturn(List.of(a1, a2));

        when(cb.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(RiskAxis.class)).thenReturn(countRoot);

        when(countRoot.get("id")).thenReturn(countIdPath);
        when(countRoot.get("deleted")).thenReturn(countDeletedPath);
        when(countRoot.get("risk")).thenReturn(countRiskPath);

        when(cb.equal(countDeletedPath, false)).thenReturn(predCountDeleted);
        when(cb.equal(countRiskPath, risk)).thenReturn(predCountRisk);

        when(countCQ.select(any())).thenReturn(countCQ);
        when(countCQ.where(predCountDeleted, predCountRisk)).thenReturn(countCQ);

        when(emMock.createQuery(countCQ)).thenReturn(countTQ);
        when(countTQ.getSingleResult()).thenReturn(2L);

        PaginatedList<RiskAxis> result = riskBS.listRiskAxis(risk);

        assertNotNull(result);
        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getList().size());
        assertSame(a1, result.getList().get(0));
        assertSame(a2, result.getList().get(1));

        verify(emMock).getCriteriaBuilder();
        verify(emMock).createQuery(listCQ);
        verify(emMock).createQuery(countCQ);
        verify(listTQ).getResultList();
        verify(countTQ).getSingleResult();
    }

    @DisplayName("listRiskAxis: retorna total=0 e lista vazia quando não há vínculos")
    @Test
    void listRiskAxis_semResultados() throws Exception {
        Risk risk = new Risk();
        risk.setId(2L);

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskAxis> listCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskAxis> listRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskAxis> listTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskAxis> countRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        Path<Object> listDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> listRiskPath = mock(Path.class);
        Predicate predListDeleted = mock(Predicate.class);
        Predicate predListRisk = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        Path<Object> countIdPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countRiskPath = mock(Path.class);
        Predicate predCountDeleted = mock(Predicate.class);
        Predicate predCountRisk = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);

        when(cb.createQuery(RiskAxis.class)).thenReturn(listCQ);
        when(listCQ.from(RiskAxis.class)).thenReturn(listRoot);
        when(listCQ.select(listRoot)).thenReturn(listCQ);
        when(listRoot.get("deleted")).thenReturn(listDeletedPath);
        when(listRoot.get("risk")).thenReturn(listRiskPath);
        when(cb.equal(listDeletedPath, false)).thenReturn(predListDeleted);
        when(cb.equal(listRiskPath, risk)).thenReturn(predListRisk);
        when(listCQ.where(predListDeleted, predListRisk)).thenReturn(listCQ);
        when(emMock.createQuery(listCQ)).thenReturn(listTQ);
        when(listTQ.getResultList()).thenReturn(List.of());

        when(cb.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(RiskAxis.class)).thenReturn(countRoot);
        when(countRoot.get("id")).thenReturn(countIdPath);
        when(countRoot.get("deleted")).thenReturn(countDeletedPath);
        when(countRoot.get("risk")).thenReturn(countRiskPath);
        when(cb.equal(countDeletedPath, false)).thenReturn(predCountDeleted);
        when(cb.equal(countRiskPath, risk)).thenReturn(predCountRisk);
        when(countCQ.select(any())).thenReturn(countCQ);
        when(countCQ.where(predCountDeleted, predCountRisk)).thenReturn(countCQ);
        when(emMock.createQuery(countCQ)).thenReturn(countTQ);
        when(countTQ.getSingleResult()).thenReturn(0L);

        PaginatedList<RiskAxis> result = riskBS.listRiskAxis(risk);

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertNotNull(result.getList());
        assertTrue(result.getList().isEmpty());

        verify(listTQ).getResultList();
        verify(countTQ).getSingleResult();
    }

    @DisplayName("listRiskIndicators: retorna lista e total quando há vínculos")
    @Test
    void listRiskIndicators_comResultados() throws Exception {
        Risk risk = new Risk();
        risk.setId(1L);

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskIndicator> listCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskIndicator> listRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskIndicator> listTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskIndicator> countRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        Path<Object> listDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> listRiskPath = mock(Path.class);
        Predicate predListDeleted = mock(Predicate.class);
        Predicate predListRisk = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        Path<Object> countIdPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countRiskPath = mock(Path.class);
        Predicate predCountDeleted = mock(Predicate.class);
        Predicate predCountRisk = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);

        when(cb.createQuery(RiskIndicator.class)).thenReturn(listCQ);
        when(listCQ.from(RiskIndicator.class)).thenReturn(listRoot);
        when(listCQ.select(listRoot)).thenReturn(listCQ);

        when(listRoot.get("deleted")).thenReturn(listDeletedPath);
        when(listRoot.get("risk")).thenReturn(listRiskPath);

        when(cb.equal(listDeletedPath, false)).thenReturn(predListDeleted);
        when(cb.equal(listRiskPath, risk)).thenReturn(predListRisk);
        when(listCQ.where(predListDeleted, predListRisk)).thenReturn(listCQ);

        when(emMock.createQuery(listCQ)).thenReturn(listTQ);

        RiskIndicator i1 = new RiskIndicator();
        i1.setId(10L);
        RiskIndicator i2 = new RiskIndicator();
        i2.setId(20L);
        when(listTQ.getResultList()).thenReturn(List.of(i1, i2));

        when(cb.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(RiskIndicator.class)).thenReturn(countRoot);

        when(countRoot.get("id")).thenReturn(countIdPath);
        when(countRoot.get("deleted")).thenReturn(countDeletedPath);
        when(countRoot.get("risk")).thenReturn(countRiskPath);

        when(cb.equal(countDeletedPath, false)).thenReturn(predCountDeleted);
        when(cb.equal(countRiskPath, risk)).thenReturn(predCountRisk);

        when(countCQ.select(any())).thenReturn(countCQ);
        when(countCQ.where(predCountDeleted, predCountRisk)).thenReturn(countCQ);

        when(emMock.createQuery(countCQ)).thenReturn(countTQ);
        when(countTQ.getSingleResult()).thenReturn(2L);

        PaginatedList<RiskIndicator> result = riskBS.listRiskIndicators(risk);

        assertNotNull(result);
        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getList().size());
        assertSame(i1, result.getList().get(0));
        assertSame(i2, result.getList().get(1));

        verify(emMock).getCriteriaBuilder();
        verify(emMock).createQuery(listCQ);
        verify(emMock).createQuery(countCQ);
        verify(listTQ).getResultList();
        verify(countTQ).getSingleResult();
    }

    @DisplayName("listRiskIndicators: retorna total=0 e lista vazia quando não há vínculos")
    @Test
    void listRiskIndicators_semResultados() throws Exception {
        Risk risk = new Risk();
        risk.setId(2L);

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskIndicator> listCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskIndicator> listRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskIndicator> listTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskIndicator> countRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        Path<Object> listDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> listRiskPath = mock(Path.class);
        Predicate predListDeleted = mock(Predicate.class);
        Predicate predListRisk = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        Path<Object> countIdPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countRiskPath = mock(Path.class);
        Predicate predCountDeleted = mock(Predicate.class);
        Predicate predCountRisk = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);

        when(cb.createQuery(RiskIndicator.class)).thenReturn(listCQ);
        when(listCQ.from(RiskIndicator.class)).thenReturn(listRoot);
        when(listCQ.select(listRoot)).thenReturn(listCQ);
        when(listRoot.get("deleted")).thenReturn(listDeletedPath);
        when(listRoot.get("risk")).thenReturn(listRiskPath);
        when(cb.equal(listDeletedPath, false)).thenReturn(predListDeleted);
        when(cb.equal(listRiskPath, risk)).thenReturn(predListRisk);
        when(listCQ.where(predListDeleted, predListRisk)).thenReturn(listCQ);
        when(emMock.createQuery(listCQ)).thenReturn(listTQ);
        when(listTQ.getResultList()).thenReturn(List.of());
        when(cb.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(RiskIndicator.class)).thenReturn(countRoot);
        when(countRoot.get("id")).thenReturn(countIdPath);
        when(countRoot.get("deleted")).thenReturn(countDeletedPath);
        when(countRoot.get("risk")).thenReturn(countRiskPath);
        when(cb.equal(countDeletedPath, false)).thenReturn(predCountDeleted);
        when(cb.equal(countRiskPath, risk)).thenReturn(predCountRisk);
        when(countCQ.select(any())).thenReturn(countCQ);
        when(countCQ.where(predCountDeleted, predCountRisk)).thenReturn(countCQ);
        when(emMock.createQuery(countCQ)).thenReturn(countTQ);
        when(countTQ.getSingleResult()).thenReturn(0L);

        PaginatedList<RiskIndicator> result = riskBS.listRiskIndicators(risk);

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertNotNull(result.getList());
        assertTrue(result.getList().isEmpty());

        verify(listTQ).getResultList();
        verify(countTQ).getSingleResult();
    }

    @DisplayName("listRiskGoals: retorna lista e total quando há vínculos")
    @Test
    void listRiskGoals_comResultados() throws Exception {
        Risk risk = new Risk();
        risk.setId(1L);

        // --- Mocks principais ---
        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskGoal> listCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskGoal> listRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskGoal> listTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskGoal> countRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        Path<Object> listDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> listRiskPath = mock(Path.class);
        Predicate predListDeleted = mock(Predicate.class);
        Predicate predListRisk = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        Path<Object> countIdPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countRiskPath = mock(Path.class);
        Predicate predCountDeleted = mock(Predicate.class);
        Predicate predCountRisk = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);

        when(cb.createQuery(RiskGoal.class)).thenReturn(listCQ);
        when(listCQ.from(RiskGoal.class)).thenReturn(listRoot);
        when(listCQ.select(listRoot)).thenReturn(listCQ);

        when(listRoot.get("deleted")).thenReturn(listDeletedPath);
        when(listRoot.get("risk")).thenReturn(listRiskPath);

        when(cb.equal(listDeletedPath, false)).thenReturn(predListDeleted);
        when(cb.equal(listRiskPath, risk)).thenReturn(predListRisk);
        when(listCQ.where(predListDeleted, predListRisk)).thenReturn(listCQ);

        when(emMock.createQuery(listCQ)).thenReturn(listTQ);

        RiskGoal g1 = new RiskGoal();
        g1.setId(10L);
        RiskGoal g2 = new RiskGoal();
        g2.setId(20L);
        when(listTQ.getResultList()).thenReturn(List.of(g1, g2));

        when(cb.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(RiskGoal.class)).thenReturn(countRoot);

        when(countRoot.get("id")).thenReturn(countIdPath);
        when(countRoot.get("deleted")).thenReturn(countDeletedPath);
        when(countRoot.get("risk")).thenReturn(countRiskPath);

        when(cb.equal(countDeletedPath, false)).thenReturn(predCountDeleted);
        when(cb.equal(countRiskPath, risk)).thenReturn(predCountRisk);

        when(countCQ.select(any())).thenReturn(countCQ);
        when(countCQ.where(predCountDeleted, predCountRisk)).thenReturn(countCQ);

        when(emMock.createQuery(countCQ)).thenReturn(countTQ);
        when(countTQ.getSingleResult()).thenReturn(2L);

        PaginatedList<RiskGoal> result = riskBS.listRiskGoals(risk);

        assertNotNull(result);
        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getList().size());
        assertSame(g1, result.getList().get(0));
        assertSame(g2, result.getList().get(1));

        verify(emMock).getCriteriaBuilder();
        verify(emMock).createQuery(listCQ);
        verify(emMock).createQuery(countCQ);
        verify(listTQ).getResultList();
        verify(countTQ).getSingleResult();
    }

    @DisplayName("listRiskGoals: retorna total=0 e lista vazia quando não há vínculos")
    @Test
    void listRiskGoals_semResultados() throws Exception {
        Risk risk = new Risk();
        risk.setId(2L);

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskGoal> listCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskGoal> listRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskGoal> listTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskGoal> countRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        Path<Object> listDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> listRiskPath = mock(Path.class);
        Predicate predListDeleted = mock(Predicate.class);
        Predicate predListRisk = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        Path<Object> countIdPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countRiskPath = mock(Path.class);
        Predicate predCountDeleted = mock(Predicate.class);
        Predicate predCountRisk = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);

        when(cb.createQuery(RiskGoal.class)).thenReturn(listCQ);
        when(listCQ.from(RiskGoal.class)).thenReturn(listRoot);
        when(listCQ.select(listRoot)).thenReturn(listCQ);
        when(listRoot.get("deleted")).thenReturn(listDeletedPath);
        when(listRoot.get("risk")).thenReturn(listRiskPath);
        when(cb.equal(listDeletedPath, false)).thenReturn(predListDeleted);
        when(cb.equal(listRiskPath, risk)).thenReturn(predListRisk);
        when(listCQ.where(predListDeleted, predListRisk)).thenReturn(listCQ);
        when(emMock.createQuery(listCQ)).thenReturn(listTQ);
        when(listTQ.getResultList()).thenReturn(List.of());

        when(cb.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(RiskGoal.class)).thenReturn(countRoot);
        when(countRoot.get("id")).thenReturn(countIdPath);
        when(countRoot.get("deleted")).thenReturn(countDeletedPath);
        when(countRoot.get("risk")).thenReturn(countRiskPath);
        when(cb.equal(countDeletedPath, false)).thenReturn(predCountDeleted);
        when(cb.equal(countRiskPath, risk)).thenReturn(predCountRisk);
        when(countCQ.select(any())).thenReturn(countCQ);
        when(countCQ.where(predCountDeleted, predCountRisk)).thenReturn(countCQ);
        when(emMock.createQuery(countCQ)).thenReturn(countTQ);
        when(countTQ.getSingleResult()).thenReturn(0L);

        PaginatedList<RiskGoal> result = riskBS.listRiskGoals(risk);

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertNotNull(result.getList());
        assertTrue(result.getList().isEmpty());

        verify(listTQ).getResultList();
        verify(countTQ).getSingleResult();
    }

    @DisplayName("listRiskActivity: retorna lista e total quando há vínculos")
    @Test
    void listRiskActivity_comResultados() throws Exception {
        Risk risk = new Risk();
        risk.setId(1L);

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskActivity> listCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskActivity> listRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskActivity> listTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskActivity> countRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        Path<Object> listDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> listRiskPath = mock(Path.class);
        Predicate predListDeleted = mock(Predicate.class);
        Predicate predListRisk = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        Path<Object> countIdPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countRiskPath = mock(Path.class);
        Predicate predCountDeleted = mock(Predicate.class);
        Predicate predCountRisk = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);

        when(cb.createQuery(RiskActivity.class)).thenReturn(listCQ);
        when(listCQ.from(RiskActivity.class)).thenReturn(listRoot);
        when(listCQ.select(listRoot)).thenReturn(listCQ);

        when(listRoot.get("deleted")).thenReturn(listDeletedPath);
        when(listRoot.get("risk")).thenReturn(listRiskPath);

        when(cb.equal(listDeletedPath, false)).thenReturn(predListDeleted);
        when(cb.equal(listRiskPath, risk)).thenReturn(predListRisk);
        when(listCQ.where(predListDeleted, predListRisk)).thenReturn(listCQ);

        when(emMock.createQuery(listCQ)).thenReturn(listTQ);

        RiskActivity a1 = new RiskActivity();
        a1.setId(10L);
        RiskActivity a2 = new RiskActivity();
        a2.setId(20L);
        when(listTQ.getResultList()).thenReturn(List.of(a1, a2));

        when(cb.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(RiskActivity.class)).thenReturn(countRoot);

        when(countRoot.get("id")).thenReturn(countIdPath);
        when(countRoot.get("deleted")).thenReturn(countDeletedPath);
        when(countRoot.get("risk")).thenReturn(countRiskPath);

        when(cb.equal(countDeletedPath, false)).thenReturn(predCountDeleted);
        when(cb.equal(countRiskPath, risk)).thenReturn(predCountRisk);

        when(countCQ.select(any())).thenReturn(countCQ);
        when(countCQ.where(predCountDeleted, predCountRisk)).thenReturn(countCQ);

        when(emMock.createQuery(countCQ)).thenReturn(countTQ);
        when(countTQ.getSingleResult()).thenReturn(2L);

        PaginatedList<RiskActivity> result = riskBS.listRiskActivity(risk);

        assertNotNull(result);
        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getList().size());
        assertSame(a1, result.getList().get(0));
        assertSame(a2, result.getList().get(1));

        verify(emMock).getCriteriaBuilder();
        verify(emMock).createQuery(listCQ);
        verify(emMock).createQuery(countCQ);
        verify(listTQ).getResultList();
        verify(countTQ).getSingleResult();
    }

    @DisplayName("listRiskActivity: retorna total=0 e lista vazia quando não há vínculos")
    @Test
    void listRiskActivity_semResultados() throws Exception {
        Risk risk = new Risk();
        risk.setId(2L);

        EntityManager emMock = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<RiskActivity> listCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskActivity> listRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<RiskActivity> listTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<RiskActivity> countRoot = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> countTQ = mock(TypedQuery.class);

        @SuppressWarnings("unchecked")
        Path<Object> listDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> listRiskPath = mock(Path.class);
        Predicate predListDeleted = mock(Predicate.class);
        Predicate predListRisk = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        Path<Object> countIdPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countDeletedPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Object> countRiskPath = mock(Path.class);
        Predicate predCountDeleted = mock(Predicate.class);
        Predicate predCountRisk = mock(Predicate.class);

        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        when(emMock.getCriteriaBuilder()).thenReturn(cb);

        when(cb.createQuery(RiskActivity.class)).thenReturn(listCQ);
        when(listCQ.from(RiskActivity.class)).thenReturn(listRoot);
        when(listCQ.select(listRoot)).thenReturn(listCQ);
        when(listRoot.get("deleted")).thenReturn(listDeletedPath);
        when(listRoot.get("risk")).thenReturn(listRiskPath);
        when(cb.equal(listDeletedPath, false)).thenReturn(predListDeleted);
        when(cb.equal(listRiskPath, risk)).thenReturn(predListRisk);
        when(listCQ.where(predListDeleted, predListRisk)).thenReturn(listCQ);
        when(emMock.createQuery(listCQ)).thenReturn(listTQ);
        when(listTQ.getResultList()).thenReturn(List.of());

        when(cb.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(RiskActivity.class)).thenReturn(countRoot);
        when(countRoot.get("id")).thenReturn(countIdPath);
        when(countRoot.get("deleted")).thenReturn(countDeletedPath);
        when(countRoot.get("risk")).thenReturn(countRiskPath);
        when(cb.equal(countDeletedPath, false)).thenReturn(predCountDeleted);
        when(cb.equal(countRiskPath, risk)).thenReturn(predCountRisk);
        when(countCQ.select(any())).thenReturn(countCQ);
        when(countCQ.where(predCountDeleted, predCountRisk)).thenReturn(countCQ);
        when(emMock.createQuery(countCQ)).thenReturn(countTQ);
        when(countTQ.getSingleResult()).thenReturn(0L);

        PaginatedList<RiskActivity> result = riskBS.listRiskActivity(risk);

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertNotNull(result.getList());
        assertTrue(result.getList().isEmpty());

        verify(listTQ).getResultList();
        verify(countTQ).getSingleResult();
    }

    @DisplayName("listSharedUnits: retorna apenas as Units de RiskSharedUnit (ordem preservada)")
    @Test
    void listSharedUnits_ok_ordemPreservada() {
        Risk risk = new Risk();
        risk.setId(1L);

        Unit u1 = unit(10L);
        Unit u2 = unit(20L);

        RiskSharedUnit rsu1 = new RiskSharedUnit();
        rsu1.setUnit(u1);
        RiskSharedUnit rsu2 = new RiskSharedUnit();
        rsu2.setUnit(u2);

        List<RiskSharedUnit> shared = new ArrayList<>();
        shared.add(rsu1);
        shared.add(rsu2);

        doReturn(shared).when(riskBS).listRiskSharedUnits(same(risk), eq(false));

        List<Unit> result = riskBS.listSharedUnits(risk);

        assertNotNull(result);
        assertEquals(2, result.size(), "Deve retornar duas unidades");
        assertSame(u1, result.get(0), "A ordem deve ser preservada (primeiro u1)");
        assertSame(u2, result.get(1), "A ordem deve ser preservada (depois u2)");

        verify(riskBS, times(1)).listRiskSharedUnits(same(risk), eq(false));
    }

    @DisplayName("listSharedUnits: quando não há compartilhamentos retorna lista vazia")
    @Test
    void listSharedUnits_semCompartilhamentos_retornaVazio() {
        Risk risk = new Risk();
        risk.setId(2L);

        doReturn(new ArrayList<RiskSharedUnit>())
                .when(riskBS).listRiskSharedUnits(same(risk), eq(false));

        List<Unit> result = riskBS.listSharedUnits(risk);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Sem compartilhamentos, a lista deve ser vazia");
        verify(riskBS).listRiskSharedUnits(same(risk), eq(false));
    }

    @DisplayName("listHistoryByUnits: quando units.list está vazia retorna Collections.emptyList e não usa DAO")
    @Test
    void listHistoryByUnits_listaVazia_noop() {
        PaginatedList<Unit> units = new PaginatedList<>();

        List<RiskHistoryBean> result = riskBS.listHistoryByUnits(units);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Com lista de unidades vazia deve retornar lista vazia");
        verifyNoInteractions(hibernateDAO);
    }

    @DisplayName("listHistoryByUnits: monta criteria com IN(unit.id), deleted=false, order asc(id), projeta e transforma em RiskHistoryBean")
    @Test
    void listHistoryByUnits_ok() throws Exception {
        Unit u1 = unit(10L);
        Unit u2 = unit(20L);
        PaginatedList<Unit> units = new PaginatedList<>();
        units.setList(new ArrayList<>(List.of(u1, u2)));

        List<Long> unitIds = List.of(10L, 20L);
        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.mapEntityIds(units.getList())).thenReturn(unitIds);

            org.forpdi.core.common.Criteria criteria = mock(org.forpdi.core.common.Criteria.class);
            when(hibernateDAO.newCriteria(RiskHistory.class)).thenReturn(criteria);

            when(criteria.add(any())).thenReturn(criteria);
            when(criteria.addOrder(any(org.forpdi.core.common.Order.class))).thenReturn(criteria);
            when(criteria.setProjection(any())).thenReturn(criteria);
            when(criteria.setResultTransformer(eq(RiskHistoryBean.class))).thenReturn(criteria);

            RiskHistoryBean b1 = new RiskHistoryBean();
            RiskHistoryBean b2 = new RiskHistoryBean();
            List<RiskHistoryBean> expected = List.of(b1, b2);
            when(hibernateDAO.findByCriteria(criteria, RiskHistoryBean.class)).thenReturn(expected);

            List<RiskHistoryBean> result = riskBS.listHistoryByUnits(units);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertSame(b1, result.get(0));
            assertSame(b2, result.get(1));

            verify(hibernateDAO).newCriteria(RiskHistory.class);
            verify(criteria, atLeast(2)).add(any());
            verify(criteria).addOrder(any(org.forpdi.core.common.Order.class));
            verify(criteria).setProjection(any());
            verify(criteria).setResultTransformer(eq(RiskHistoryBean.class));

            verify(hibernateDAO).findByCriteria(criteria, RiskHistoryBean.class);
        }
    }

    @DisplayName("getMatrixVector: parse 2x2 gera total=8 linhas e extrai [label, v1, v2] corretamente")
    @Test
    void getMatrixVector_parse2x2_ok() {
        Policy policy = new Policy();
        policy.setNline(2);
        policy.setNcolumn(2);

        String matrixStr = String.join(";",
                "[P1,0]P1",
                "[P1,I1]L11",
                "[P1,I2]L12",
                "[P2,0]P2",
                "[P2,I1]L21",
                "[P2,I2]L22",
                "[0,I1]I1",
                "[0,I2]I2");
        policy.setMatrix(matrixStr);

        String[][] parsed = riskBS.getMatrixVector(policy);

        assertNotNull(parsed);
        assertEquals(8, parsed.length, "Deve ter 8 linhas para 2x2");

        assertArrayEquals(new String[] { "P1", "P1", "0" }, parsed[0]);
        assertArrayEquals(new String[] { "L11", "P1", "I1" }, parsed[1]);
        assertArrayEquals(new String[] { "L12", "P1", "I2" }, parsed[2]);
        assertArrayEquals(new String[] { "P2", "P2", "0" }, parsed[3]);
        assertArrayEquals(new String[] { "L21", "P2", "I1" }, parsed[4]);
        assertArrayEquals(new String[] { "L22", "P2", "I2" }, parsed[5]);
        assertArrayEquals(new String[] { "I1", "0", "I1" }, parsed[6]);
        assertArrayEquals(new String[] { "I2", "0", "I2" }, parsed[7]);
    }

    @DisplayName("getMatrixVector: parse 1x1 gera total=3 linhas e extrai corretamente")
    @Test
    void getMatrixVector_parse1x1_ok() {
        Policy policy = new Policy();
        policy.setNline(1);
        policy.setNcolumn(1);

        String matrixStr = String.join(";",
                "[P1,0]P1",
                "[P1,I1]L11",
                "[0,I1]I1");
        policy.setMatrix(matrixStr);

        String[][] parsed = riskBS.getMatrixVector(policy);

        assertNotNull(parsed);
        assertEquals(3, parsed.length, "Deve ter 3 linhas para 1x1");

        assertArrayEquals(new String[] { "P1", "P1", "0" }, parsed[0]);
        assertArrayEquals(new String[] { "L11", "P1", "I1" }, parsed[1]);
        assertArrayEquals(new String[] { "I1", "0", "I1" }, parsed[2]);
    }

    @DisplayName("hasLinkedRiskObjectiveProcess: retorna true quando algum objetivo possui vínculos de risco")
    @Test
    void hasLinkedRiskObjectiveProcess_algumObjetivoComVinculo_true() {
        Process proc = new Process();
        proc.setId(99L);
        ProcessObjective o1 = procObj(100L, proc);
        ProcessObjective o2 = procObj(200L, proc);

        when(processBS.listProcessObjectives(proc)).thenReturn(List.of(o1, o2));

        org.forpdi.core.common.Criteria c1 = mock(org.forpdi.core.common.Criteria.class);
        org.forpdi.core.common.Criteria c2 = mock(org.forpdi.core.common.Criteria.class);
        when(hibernateDAO.newCriteria(RiskProcessObjective.class)).thenReturn(c1, c2);

        when(c1.createAlias(anyString(), anyString(), any(org.forpdi.core.common.JoinType.class))).thenReturn(c1);
        when(c1.add(any())).thenReturn(c1);
        when(c2.createAlias(anyString(), anyString(), any(org.forpdi.core.common.JoinType.class))).thenReturn(c2);
        when(c2.add(any())).thenReturn(c2);

        List<RiskProcessObjective> empty = new ArrayList<>();
        List<RiskProcessObjective> some = new ArrayList<>();
        RiskProcessObjective rpo = new RiskProcessObjective();
        rpo.setId(555L);
        some.add(rpo);

        when(hibernateDAO.findByCriteria(c1, RiskProcessObjective.class)).thenReturn(empty);
        when(hibernateDAO.findByCriteria(c2, RiskProcessObjective.class)).thenReturn(some);

        boolean result = riskBS.hasLinkedRiskObjectiveProcess(proc);

        assertTrue(result, "Deve retornar true quando ao menos um objetivo possui vínculos.");

        verify(hibernateDAO, times(2)).newCriteria(RiskProcessObjective.class);
        verify(c1).createAlias(eq("processObjective"), eq("processObjective"),
                eq(org.forpdi.core.common.JoinType.INNER_JOIN));
        verify(c2).createAlias(eq("processObjective"), eq("processObjective"),
                eq(org.forpdi.core.common.JoinType.INNER_JOIN));
        verify(c1, times(1)).add(any());
        verify(c2, times(1)).add(any());
        verify(hibernateDAO).findByCriteria(c1, RiskProcessObjective.class);
        verify(hibernateDAO).findByCriteria(c2, RiskProcessObjective.class);
    }

    @DisplayName("hasLinkedRiskObjectiveProcess: retorna false quando o processo não possui objetivos")
    @Test
    void hasLinkedRiskObjectiveProcess_semObjetivos_false() {
        Process proc = new Process();
        proc.setId(7L);
        when(processBS.listProcessObjectives(proc)).thenReturn(new ArrayList<>());

        boolean result = riskBS.hasLinkedRiskObjectiveProcess(proc);

        assertFalse(result, "Sem objetivos, não há como haver vínculos.");
        verify(processBS).listProcessObjectives(proc);
        verify(hibernateDAO, never()).newCriteria(RiskProcessObjective.class);
    }

    @DisplayName("hasLinkedRiskActivity: retorna true quando existe atividade vinculada (deleted=false)")
    @Test
    void hasLinkedRiskActivity_existeAtividade_true() {
        Process proc = new Process();
        proc.setId(99L);

        org.forpdi.core.common.Criteria criteria = mock(org.forpdi.core.common.Criteria.class);
        when(hibernateDAO.newCriteria(RiskActivity.class)).thenReturn(criteria);

        when(criteria.add(any())).thenReturn(criteria);
        when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
        when(criteria.uniqueResult()).thenReturn(new RiskActivity());

        boolean result = riskBS.hasLinkedRiskActivity(proc);

        assertTrue(result, "Deve retornar true quando uniqueResult() != null");

        verify(hibernateDAO).newCriteria(RiskActivity.class);
        verify(criteria, times(2)).add(any());
        verify(criteria).setMaxResults(1);
        verify(criteria).uniqueResult();
    }

    @DisplayName("hasLinkedRiskActivity: retorna false quando não existe atividade vinculada")
    @Test
    void hasLinkedRiskActivity_semAtividade_false() {
        Process proc = new Process();
        proc.setId(100L);

        org.forpdi.core.common.Criteria criteria = mock(org.forpdi.core.common.Criteria.class);
        when(hibernateDAO.newCriteria(RiskActivity.class)).thenReturn(criteria);

        when(criteria.add(any())).thenReturn(criteria);
        when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
        when(criteria.uniqueResult()).thenReturn(null);

        boolean result = riskBS.hasLinkedRiskActivity(proc);

        assertFalse(result, "Deve retornar false quando uniqueResult() == null");

        verify(hibernateDAO).newCriteria(RiskActivity.class);
        verify(criteria, times(2)).add(any());
        verify(criteria).setMaxResults(1);
        verify(criteria).uniqueResult();
    }

    // ---------- isResposible ----------
    @DisplayName("isResposible: true quando IDs do usuário corrente e responsável são iguais")
    @Test
    void isResposible_true_quandoIdsIguais() {
        UserSession session = mock(UserSession.class);
        User current = user(10L);
        when(session.getUser()).thenReturn(current);

        User responsible = user(10L);

        assertTrue(riskBS.isResposible(session, responsible));
    }

    @DisplayName("isResposible: false quando IDs são diferentes")
    @Test
    void isResposible_false_quandoIdsDiferentes() {
        UserSession session = mock(UserSession.class);
        when(session.getUser()).thenReturn(user(10L));

        assertFalse(riskBS.isResposible(session, user(99L)));
    }

    private void injectUserSession(UserSession session) throws Exception {
        Field f = RiskBS.class.getDeclaredField("userSession");
        f.setAccessible(true);
        f.set(riskBS, session);
    }

    @DisplayName("validateRiskItemPermissions: lança quando sem permissão e não é responsável do item nem do risco")
    @Test
    void validateRiskItemPermissions_semPermissaoNemResponsabilidade_lanca() throws Exception {
        UserSession session = mock(UserSession.class);
        when(session.getUser()).thenReturn(user(1L));
        injectUserSession(session);

        Risk risk = new Risk();
        risk.setUser(user(3L));
        User itemResponsible = user(2L);

        doReturn(false).when(riskBS).hasPermissionToEditRiskItems(same(session));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> riskBS.validateRiskItemPermissions(risk, itemResponsible));
        assertTrue(ex.getMessage().toLowerCase().contains("permissão"));

        verify(riskBS, times(1)).isResposible(same(session), same(itemResponsible));
        verify(riskBS, times(1)).isResposible(same(session), same(risk.getUser()));
    }

    @DisplayName("validateRiskItemPermissions: não lança quando tem permissão (mesmo não sendo responsável)")
    @Test
    void validateRiskItemPermissions_comPermissao_ok() throws Exception {
        UserSession session = mock(UserSession.class);
        when(session.getUser()).thenReturn(user(1L));
        injectUserSession(session);

        Risk risk = new Risk();
        risk.setUser(user(3L));
        User itemResponsible = user(2L);

        doReturn(true).when(riskBS).hasPermissionToEditRiskItems(same(session));

        RiskItemPermissionInfo info = riskBS.validateRiskItemPermissions(risk, itemResponsible);
        assertNotNull(info);
    }

    @DisplayName("validateRiskItemPermissions: não lança quando usuário é responsável pelo item")
    @Test
    void validateRiskItemPermissions_responsavelPeloItem_ok() throws Exception {
        UserSession session = mock(UserSession.class);
        when(session.getUser()).thenReturn(user(10L));
        injectUserSession(session);

        Risk risk = new Risk();
        risk.setUser(user(3L));
        User itemResponsible = user(10L);

        doReturn(false).when(riskBS).hasPermissionToEditRiskItems(same(session));

        RiskItemPermissionInfo info = riskBS.validateRiskItemPermissions(risk, itemResponsible);
        assertNotNull(info);
    }

    @DisplayName("validateRiskItemPermissions: não lança quando usuário é responsável pelo risco")
    @Test
    void validateRiskItemPermissions_responsavelDoRisco_ok() throws Exception {
        UserSession session = mock(UserSession.class);
        when(session.getUser()).thenReturn(user(10L));
        injectUserSession(session);

        Risk risk = new Risk();
        risk.setUser(user(10L));
        User itemResponsible = user(2L);

        doReturn(false).when(riskBS).hasPermissionToEditRiskItems(same(session));

        RiskItemPermissionInfo info = riskBS.validateRiskItemPermissions(risk, itemResponsible);
        assertNotNull(info);
    }

    @DisplayName("sendUserLinkedToRiskNotification: envia notificação e e-mail com URL e texto corretos")
    @Test
    void sendUserLinkedToRiskNotification_ok() throws EmailException {
        Risk risk = new Risk();
        risk.setId(5L);
        risk.setName("Risco A");

        Unit unit = unit(10L);
        User user = user(9L);

        String baseUrl = "https://srv";
        String expectedUrl = "https://srv/#/forrisco/risk/5/details/info";
        String expectedText = "Você foi vinculado como responsável pelo risco Risco A no ForRisco";

        riskBS.sendUserLinkedToRiskNotification(risk, unit, user, baseUrl);

        verify(notificationBS).sendNotification(
                eq(NotificationType.FORRISCO_USER_LINKED_TO_RISK),
                eq(expectedText),
                isNull(),
                eq(9L),
                eq(expectedUrl));
        verify(notificationBS).sendNotificationEmail(
                eq(NotificationType.FORRISCO_USER_LINKED_TO_RISK),
                eq(expectedText),
                eq(""),
                eq(user),
                eq(expectedUrl));
    }

    @DisplayName("sendRiskChangedNotification: envia notificação e e-mail com URL e texto corretos")
    @Test
    void sendRiskChangedNotification_ok() throws EmailException {
        Risk risk = new Risk();
        risk.setId(11L);
        risk.setName("Risco B");

        User user = user(22L);
        String baseUrl = "https://base";
        String expectedUrl = "https://base/#/forrisco/risk/11/details/info";
        String expectedText = "O risco Risco B foi alterado.";

        riskBS.sendRiskChangedNotification(risk, user, baseUrl);

        verify(notificationBS).sendNotification(
                eq(NotificationType.FORRISCO_MANAGER_RISK_UPDATED),
                eq(expectedText),
                isNull(),
                eq(22L),
                eq(expectedUrl));
        verify(notificationBS).sendNotificationEmail(
                eq(NotificationType.FORRISCO_MANAGER_RISK_UPDATED),
                eq(expectedText),
                eq(""),
                eq(user),
                eq(expectedUrl));
    }

    @DisplayName("sendNotificationToRiskItemManager: usa risk.getName() como 'aux' (3º parâmetro) em ambas as chamadas")
    @Test
    void sendNotificationToRiskItemManager_ok() throws EmailException {
        Risk risk = new Risk();
        risk.setName("Risco Z");

        User manager = user(44L);
        String url = "https://u";
        String text = "Texto X";
        NotificationType type = NotificationType.FORRISCO_MANAGER_RISK_UPDATED;

        riskBS.sendNotificationToRiskItemManager(risk, manager, url, text, type);

        verify(notificationBS).sendNotification(
                eq(type),
                eq(text),
                eq("Risco Z"),
                eq(44L),
                eq(url));
        verify(notificationBS).sendNotificationEmail(
                eq(type),
                eq(text),
                eq("Risco Z"),
                eq(manager),
                eq(url));
    }

    // ======================= validateVigency =======================

    @DisplayName("validateVigency: OK quando ambas as datas são nulas")
    @Test
    void validateVigency_ambasNulas_ok() {
        assertDoesNotThrow(() -> riskBS.validateVigency(null, null));
    }

    @DisplayName("validateVigency: lança quando só uma das datas é informada (apenas início)")
    @Test
    void validateVigency_soInicio_lanca() {
        Date begin = new Date();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> riskBS.validateVigency(begin, null));
        assertTrue(ex.getMessage().toLowerCase().contains("vigência"));
    }

    @DisplayName("validateVigency: lança quando só uma das datas é informada (apenas término)")
    @Test
    void validateVigency_soTermino_lanca() {
        Date end = new Date();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> riskBS.validateVigency(null, end));
        assertTrue(ex.getMessage().toLowerCase().contains("vigência"));
    }

    @DisplayName("validateVigency: lança quando término é antes do início")
    @Test
    void validateVigency_terminoAntesDoInicio_lanca() {
        Date begin = new Date(System.currentTimeMillis());
        Date end = new Date(System.currentTimeMillis() - 1000);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> riskBS.validateVigency(begin, end));
        assertTrue(ex.getMessage().toLowerCase().contains("início"));
    }

    @DisplayName("validateVigency: OK quando início e término são iguais")
    @Test
    void validateVigency_datasIguais_ok() {
        Date d = new Date();
        assertDoesNotThrow(() -> riskBS.validateVigency(d, d));
    }

    @DisplayName("validateVigency: OK quando término é depois do início")
    @Test
    void validateVigency_terminoDepois_ok() {
        Date begin = new Date(System.currentTimeMillis());
        Date end = new Date(System.currentTimeMillis() + 1000);
        assertDoesNotThrow(() -> riskBS.validateVigency(begin, end));
    }

    @DisplayName("listRiskSharedUnits: includesDeleted=false aplica filtro unit.deleted=false e retorna lista")
    @Test
    void listRiskSharedUnits_excluiUnidadesDeletadas() {
        Risk risk = new Risk();
        risk.setId(77L);

        org.forpdi.core.common.Criteria criteria = mock(org.forpdi.core.common.Criteria.class);
        when(hibernateDAO.newCriteria(RiskSharedUnit.class)).thenReturn(criteria);

        when(criteria.createAlias(anyString(), anyString(), any(org.forpdi.core.common.JoinType.class)))
                .thenReturn(criteria);
        when(criteria.add(any())).thenReturn(criteria);

        RiskSharedUnit rsu1 = new RiskSharedUnit();
        rsu1.setId(1L);
        RiskSharedUnit rsu2 = new RiskSharedUnit();
        rsu2.setId(2L);
        List<RiskSharedUnit> expected = List.of(rsu1, rsu2);
        when(hibernateDAO.findByCriteria(criteria, RiskSharedUnit.class)).thenReturn(expected);

        List<RiskSharedUnit> got = riskBS.listRiskSharedUnits(risk, false);

        assertNotNull(got);
        assertEquals(2, got.size());
        assertSame(expected, got);

        verify(hibernateDAO).newCriteria(RiskSharedUnit.class);
        verify(criteria).createAlias(eq("risk"), eq("risk"), eq(org.forpdi.core.common.JoinType.INNER_JOIN));
        verify(criteria).createAlias(eq("unit"), eq("unit"), eq(org.forpdi.core.common.JoinType.INNER_JOIN));
        verify(criteria, times(2)).add(any());
        verify(hibernateDAO).findByCriteria(criteria, RiskSharedUnit.class);
    }

    @DisplayName("listRiskSharedUnits: includesDeleted=true NÃO aplica filtro unit.deleted=false")
    @Test
    void listRiskSharedUnits_incluiDeletadas_naoFiltraDeleted() {
        Risk risk = new Risk();
        risk.setId(88L);

        org.forpdi.core.common.Criteria criteria = mock(org.forpdi.core.common.Criteria.class);
        when(hibernateDAO.newCriteria(RiskSharedUnit.class)).thenReturn(criteria);
        when(criteria.createAlias(anyString(), anyString(), any(org.forpdi.core.common.JoinType.class)))
                .thenReturn(criteria);
        when(criteria.add(any())).thenReturn(criteria);

        when(hibernateDAO.findByCriteria(criteria, RiskSharedUnit.class)).thenReturn(new ArrayList<>());

        List<RiskSharedUnit> got = riskBS.listRiskSharedUnits(risk, true);

        assertNotNull(got);
        assertTrue(got.isEmpty());

        verify(criteria, times(1)).add(any());
        verify(hibernateDAO).findByCriteria(criteria, RiskSharedUnit.class);
    }

    @DisplayName("listRiskProcessObjectives: cria aliases, filtra por risk.id e retorna lista")
    @Test
    void listRiskProcessObjectives_ok() {
        Risk risk = new Risk();
        risk.setId(55L);

        org.forpdi.core.common.Criteria criteria = mock(org.forpdi.core.common.Criteria.class);
        when(hibernateDAO.newCriteria(RiskProcessObjective.class)).thenReturn(criteria);
        when(criteria.createAlias(anyString(), anyString(), any(org.forpdi.core.common.JoinType.class)))
                .thenReturn(criteria);
        when(criteria.add(any())).thenReturn(criteria);

        RiskProcessObjective r1 = new RiskProcessObjective();
        r1.setId(10L);
        RiskProcessObjective r2 = new RiskProcessObjective();
        r2.setId(20L);
        List<RiskProcessObjective> expected = List.of(r1, r2);
        when(hibernateDAO.findByCriteria(criteria, RiskProcessObjective.class)).thenReturn(expected);

        List<RiskProcessObjective> got = riskBS.listRiskProcessObjectives(risk);

        assertNotNull(got);
        assertEquals(2, got.size());
        assertSame(expected, got);

        verify(hibernateDAO).newCriteria(RiskProcessObjective.class);
        verify(criteria).createAlias(eq("risk"), eq("risk"), eq(org.forpdi.core.common.JoinType.INNER_JOIN));
        verify(criteria).createAlias(eq("processObjective"), eq("processObjective"),
                eq(org.forpdi.core.common.JoinType.INNER_JOIN));
        verify(criteria, times(1)).add(any());
        verify(hibernateDAO).findByCriteria(criteria, RiskProcessObjective.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T invokePrivateExtract(String method, Risk arg) throws Exception {
        Method m = RiskBS.class.getDeclaredMethod(method, Risk.class);
        m.setAccessible(true);
        return (T) m.invoke(this.riskBS, arg);
    }

    private static StructureLevelInstance sli(Long id, String name) {
        StructureLevelInstance s = new StructureLevelInstance();
        s.setId(id);
        s.setName(name);
        return s;
    }

    private static RiskAxis axisWithSli(Long sliId) {
        RiskAxis a = new RiskAxis();
        StructureLevelInstance s = new StructureLevelInstance();
        s.setId(sliId);
        a.setStructure(s);
        return a;
    }

    private static RiskStrategy strategyWithSli(Long sliId) {
        RiskStrategy a = new RiskStrategy();
        StructureLevelInstance s = new StructureLevelInstance();
        s.setId(sliId);
        a.setStructure(s);
        return a;
    }

    private static RiskIndicator indicatorWithSli(Long sliId) {
        RiskIndicator a = new RiskIndicator();
        StructureLevelInstance s = new StructureLevelInstance();
        s.setId(sliId);
        a.setStructure(s);
        return a;
    }

    private static RiskGoal goalWithSli(Long sliId) {
        RiskGoal a = new RiskGoal();
        StructureLevelInstance s = new StructureLevelInstance();
        s.setId(sliId);
        a.setStructure(s);
        return a;
    }

    private static PlanRisk planRisk(Date validityEnd) {
        PlanRisk pr = new PlanRisk();
        pr.setValidityEnd(validityEnd);
        return pr;
    }

    private static Risk riskWithUnitPlan(Long id, Date validityEnd) {
        Risk r = new Risk();
        r.setId(id);
        Unit u = new Unit();
        u.setPlanRisk(planRisk(validityEnd));
        r.setUnit(u);
        r.setBegin(new Date());
        r.setPeriodicity("1");
        return r;
    }

    @DisplayName("setRisksMonitoringState: plano expirado => EXPIRED_PLAN; plano vigente usa monitor + RiskStateChecker")
    @Test
    void setRisksMonitoringState_expirado_e_vigente() {
        Date now = new Date();
        Risk expirado = riskWithUnitPlan(1L, new Date(now.getTime() - 86_400_000L));
        Risk vigente = riskWithUnitPlan(2L, new Date(now.getTime() + 86_400_000L));

        // monitor para o vigente
        Monitor mon = new Monitor();
        Date monBegin = new Date(now.getTime() - 5 * 86_400_000L);
        mon.setBegin(monBegin);
        when(monitorBS.lastMonitorbyRisk(2L)).thenReturn(mon);

        try (MockedStatic<RiskStateChecker> mocked = mockStatic(RiskStateChecker.class)) {
            mocked.when(() -> RiskStateChecker.riskState(eq(vigente.getPeriodicity()), eq(monBegin)))
                    .thenReturn(7);

            riskBS.setRisksMonitoringState(new ArrayList<>(List.of(expirado, vigente)));

            assertEquals(RiskState.EXPIRED_PLAN.getId(), expirado.getMonitoringState());
            assertEquals(7, vigente.getMonitoringState());

            verify(monitorBS, never()).lastMonitorbyRisk(1L);
            verify(monitorBS).lastMonitorbyRisk(2L);
        }
    }

    @DisplayName("setRisksMonitoringState: plano vigente sem monitor => usa begin do risco")
    @Test
    void setRisksMonitoringState_vigente_semMonitor_usaBeginDoRisco() {
        Date now = new Date();
        Risk vigente = riskWithUnitPlan(10L, new Date(now.getTime() + 86_400_000L));
        vigente.setBegin(new Date(now.getTime() - 3 * 86_400_000L));

        when(monitorBS.lastMonitorbyRisk(10L)).thenReturn(null);
        try (MockedStatic<RiskStateChecker> mocked = mockStatic(RiskStateChecker.class)) {
            mocked.when(() -> RiskStateChecker.riskState(eq("1"), eq(vigente.getBegin())))
                    .thenReturn(4);

            riskBS.setRisksMonitoringState(new ArrayList<>(List.of(vigente)));
            assertEquals(4, vigente.getMonitoringState());
            verify(monitorBS).lastMonitorbyRisk(10L);
        }
    }

    @DisplayName("extractAxisNames: coleta nomes (trim) de SLIs existentes; ignora ids nulos/nomes nulos")
    @Test
    void extractAxisNames_ok() throws Exception {
        Risk r = new Risk();
        PaginatedList<RiskAxis> page = new PaginatedList<>();
        List<RiskAxis> list = new ArrayList<>();
        list.add(axisWithSli(10L));
        list.add(axisWithSli(null));
        list.add(axisWithSli(20L));
        list.add(axisWithSli(30L));
        page.setList(list);
        r.setAxes(page);

        when(hibernateDAO.exists(10L, StructureLevelInstance.class)).thenReturn(sli(10L, "  Eixo A  "));
        when(hibernateDAO.exists(20L, StructureLevelInstance.class)).thenReturn(sli(20L, "Eixo B"));
        when(hibernateDAO.exists(30L, StructureLevelInstance.class)).thenReturn(null);

        List<String> names = invokePrivateExtract("extractAxisNames", r);
        assertEquals(List.of("Eixo A", "Eixo B"), names);
    }

    @DisplayName("extractStrategyNames: coleta nomes (trim)")
    @Test
    void extractStrategyNames_ok() throws Exception {
        Risk r = new Risk();
        PaginatedList<RiskStrategy> page = new PaginatedList<>();
        List<RiskStrategy> list = new ArrayList<>();
        list.add(strategyWithSli(100L));
        list.add(strategyWithSli(200L));
        page.setList(list);
        r.setStrategies(page);

        when(hibernateDAO.exists(100L, StructureLevelInstance.class)).thenReturn(sli(100L, "  Obj X  "));
        when(hibernateDAO.exists(200L, StructureLevelInstance.class)).thenReturn(sli(200L, "Obj Y"));

        List<String> names = invokePrivateExtract("extractStrategyNames", r);
        assertEquals(List.of("Obj X", "Obj Y"), names);
    }

    @DisplayName("extractIndicatorNames: coleta nomes (trim)")
    @Test
    void extractIndicatorNames_ok() throws Exception {
        Risk r = new Risk();
        PaginatedList<RiskIndicator> page = new PaginatedList<>();
        List<RiskIndicator> list = new ArrayList<>();
        list.add(indicatorWithSli(1L));
        list.add(indicatorWithSli(2L));
        page.setList(list);
        r.setIndicators(page);

        when(hibernateDAO.exists(1L, StructureLevelInstance.class)).thenReturn(sli(1L, "  Ind A "));
        when(hibernateDAO.exists(2L, StructureLevelInstance.class)).thenReturn(sli(2L, "Ind B"));

        List<String> names = invokePrivateExtract("extractIndicatorNames", r);
        assertEquals(List.of("Ind A", "Ind B"), names);
    }

    @DisplayName("extractGoalNames: coleta nomes (trim)")
    @Test
    void extractGoalNames_ok() throws Exception {
        Risk r = new Risk();
        PaginatedList<RiskGoal> page = new PaginatedList<>();
        List<RiskGoal> list = new ArrayList<>();
        list.add(goalWithSli(7L));
        list.add(goalWithSli(9L));
        page.setList(list);
        r.setGoals(page);

        when(hibernateDAO.exists(7L, StructureLevelInstance.class)).thenReturn(sli(7L, "  Meta X "));
        when(hibernateDAO.exists(9L, StructureLevelInstance.class)).thenReturn(sli(9L, "Meta Y"));

        List<String> names = invokePrivateExtract("extractGoalNames", r);
        assertEquals(List.of("Meta X", "Meta Y"), names);
    }

    @DisplayName("extractProcessObjectiveDescriptions: coleta descriptions (trim) de ProcessObjective")
    @Test
    void extractProcessObjectiveDescriptions_ok() throws Exception {
        Risk r = new Risk();
        List<RiskProcessObjective> rpos = new ArrayList<>();
        RiskProcessObjective a = new RiskProcessObjective();
        a.setId(1L);
        RiskProcessObjective b = new RiskProcessObjective();
        b.setId(2L);
        RiskProcessObjective c = new RiskProcessObjective();
        c.setId(null);
        rpos.add(a);
        rpos.add(b);
        rpos.add(c);
        r.setProcessObjectives(rpos);

        ProcessObjective po1 = new ProcessObjective();
        po1.setId(1L);
        po1.setDescription("  Desc 1 ");
        ProcessObjective po2 = new ProcessObjective();
        po2.setId(2L);
        po2.setDescription(null);

        when(processBS.retrieveProcessObjectiveById(1L)).thenReturn(po1);
        when(processBS.retrieveProcessObjectiveById(2L)).thenReturn(po2);

        List<String> desc = invokePrivateExtract("extractProcessObjectiveDescriptions", r);
        assertEquals(List.of("Desc 1"), desc);
    }

    @DisplayName("getLastMonitoringDate: executa JPQL, injeta parâmetro 'risk' e retorna MAX(begin)")
    @Test
    void getLastMonitoringDate_ok() throws Exception {
        EntityManager emMock = mock(EntityManager.class);
        Field f = RiskBS.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(riskBS, emMock);

        Risk risk = new Risk();
        risk.setId(55L);
        TypedQuery<Date> tq = mock(TypedQuery.class);

        when(emMock.createQuery(anyString(), eq(Date.class))).thenReturn(tq);
        Date expected = new Date();
        when(tq.setParameter(eq("risk"), same(risk))).thenReturn(tq);
        when(tq.getSingleResult()).thenReturn(expected);

        Date got = riskBS.getLastMonitoringDate(risk);

        assertEquals(expected, got);
        verify(emMock).createQuery(anyString(), eq(Date.class));
        verify(tq).setParameter("risk", risk);
        verify(tq).getSingleResult();
    }

    @DisplayName("getRiskLevelByRisk: calcula posição na matriz e retorna RiskLevel do criteria")
    @Test
    void getRiskLevelByRisk_ok() {
        Risk risk = new Risk();
        risk.setProbability("P2");
        risk.setImpact("I1");

        Policy policy = new Policy();
        policy.setNline(2);
        policy.setNcolumn(2);

        String[][] matrix = new String[8][3];
        matrix[3][0] = "P2";
        matrix[6][0] = "I1";
        matrix[7][0] = "HIGH";

        doReturn(matrix).when(riskBS).getMatrixVector(same(policy));

        org.forpdi.core.common.Criteria criteria = mock(org.forpdi.core.common.Criteria.class);
        when(hibernateDAO.newCriteria(RiskLevel.class)).thenReturn(criteria);
        when(criteria.add(any())).thenReturn(criteria);

        RiskLevel expected = new RiskLevel();
        expected.setLevel("HIGH");
        when(criteria.uniqueResult()).thenReturn(expected);

        RiskLevel got = riskBS.getRiskLevelByRisk(risk, policy);

        assertNotNull(got);
        assertEquals("HIGH", got.getLevel());

        verify(hibernateDAO).newCriteria(RiskLevel.class);
        verify(criteria, times(3)).add(any());
        verify(criteria).uniqueResult();
    }

    @DisplayName("getRiskLevelByRisk: quando posição calculada >= total => lança IllegalArgumentException")
    @Test
    void getRiskLevelByRisk_posicaoInvalida_lanca() {
        Risk risk = new Risk();
        risk.setProbability("P");
        risk.setImpact("I");

        Policy policy = new Policy();
        policy.setNline(1);
        policy.setNcolumn(1);

        String[][] matrix = new String[3][3];
        matrix[2][0] = "P";
        matrix[2][0] = "P";
        matrix[2][0] = "I";

        risk.setProbability("I");
        risk.setImpact("I");

        doReturn(matrix).when(riskBS).getMatrixVector(same(policy));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> riskBS.getRiskLevelByRisk(risk, policy));
        assertTrue(ex.getMessage().toLowerCase().contains("grau de risco"));

        verify(hibernateDAO, never()).newCriteria(RiskLevel.class);
    }
}
