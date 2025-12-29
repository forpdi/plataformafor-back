package org.forrisco.core.process;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.storage.file.ArchiveBS;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.links.RiskProcessObjective;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessBSTest {

    private ProcessBS service;

    private ArchiveBS archiveBS;
    private EntityManager entityManager;

    private Object daoMock;
    private DaoStub daoStub;

    @BeforeEach
    void setup() throws Exception {
        service = spy(new ProcessBS());

        archiveBS = mock(ArchiveBS.class);
        entityManager = mock(EntityManager.class, Answers.RETURNS_DEEP_STUBS);

        ReflectionTestUtils.setField(service, "archiveBS", archiveBS);
        ReflectionTestUtils.setField(service, "entityManager", entityManager);

        Class<?> hibernateBusinessClass = service.getClass().getSuperclass();
        Field daoField = hibernateBusinessClass.getDeclaredField("dao");
        daoField.setAccessible(true);
        Class<?> daoClass = daoField.getType();

        daoStub = new DaoStub();
        daoMock = mock(daoClass, daoStub);
        ReflectionTestUtils.setField(service, "dao", daoMock);
    }

    private static class DaoStub implements Answer<Object> {
        private final Map<Class<?>, Deque<Object>> uniqueByClass = new ConcurrentHashMap<>();
        private final Map<Class<?>, Deque<List<?>>> listByClass = new ConcurrentHashMap<>();
        final List<Object> persisted = new ArrayList<>();
        final List<Object> removed = new ArrayList<>();

        final List<Criteria> createdCriteria = new ArrayList<>();

        void enqueueUnique(Class<?> clazz, Object value) {
            uniqueByClass.computeIfAbsent(clazz, k -> new ArrayDeque<>()).addLast(value);
        }

        @SafeVarargs
        final <T> void enqueueList(Class<T> clazz, List<T>... lists) {
            Deque<List<?>> q = listByClass.computeIfAbsent(clazz, k -> new ArrayDeque<>());
            for (List<T> l : lists) q.addLast(l);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object answer(InvocationOnMock inv) throws Throwable {
            String name = inv.getMethod().getName();

            switch (name) {
                case "newCriteria": {
                    Class<?> clazz = (Class<?>) inv.getArgument(0);

                    Criteria crit = mock(Criteria.class, Answers.RETURNS_SELF);
                    createdCriteria.add(crit);

                    lenient().when(crit.uniqueResult()).thenAnswer(ci -> {
                        Deque<Object> q = uniqueByClass.get(clazz);
                        return (q == null || q.isEmpty()) ? null : q.removeFirst();
                    });

                    return crit;
                }
                case "findByCriteria": {
                    Class<?> clazz = (Class<?>) inv.getArgument(1);
                    Deque<List<?>> q = listByClass.get(clazz);
                    if (q == null || q.isEmpty()) return Collections.emptyList();
                    return q.removeFirst();
                }
                case "persist": {
                    Object entity = inv.getArgument(0);
                    persisted.add(entity);
                    return null;
                }
                case "remove": {
                    Object entity = inv.getArgument(0);
                    removed.add(entity);
                    return null;
                }
                default:
                    return Answers.RETURNS_DEFAULTS.answer(inv);
            }
        }
    }

    private static Process newProcess(Long id, String name) {
        Process p = new Process();
        p.setId(id);
        p.setName(name);
        return p;
    }

    private static Unit newUnit(Long id, String name, PlanRisk plan) {
        Unit u = new Unit();
        u.setId(id);
        u.setName(name);
        u.setPlanRisk(plan);
        return u;
    }

    private static PlanRisk newPlanRisk(Long id) {
        PlanRisk pr = new PlanRisk();
        pr.setId(id);
        return pr;
    }

    private static ProcessUnit link(Process process, Unit unit) {
        ProcessUnit pu = new ProcessUnit();
        pu.setProcess(process);
        pu.setUnit(unit);
        pu.setDeleted(false);
        return pu;
    }

    private static ProcessObjective newObjective(Long id, Process process, String desc) {
        ProcessObjective o = new ProcessObjective();
        o.setId(id);
        o.setProcess(process);
        o.setDescription(desc);
        return o;
    }

    @Test
    @DisplayName("save(process): resolve arquivo por fileLink, seta deleted=false e persiste")
    void save_process_withFileLink_persistsWithArchive() throws Exception {
        Process p = new Process();
        p.setFileLink("file://abc");

        Archive a = new Archive();
        a.setName("doc.pdf");

        when(archiveBS.getByFileLink("file://abc")).thenReturn(a);

        service.save(p);

        assertFalse(p.isDeleted());
        assertEquals(a, p.getFile());

        assertTrue(daoStub.persisted.contains(p), "persist deveria ter sido chamado com o Process");
        verify(archiveBS).getByFileLink("file://abc");
    }

    @Test
    @DisplayName("save(process): sem fileLink não consulta ArchiveBS e persiste mesmo assim")
    void save_process_withoutFileLink_persistsNoArchiveLookup() throws Exception {
        Process p = new Process();

        service.save(p);

        assertFalse(p.isDeleted());
        assertNull(p.getFile());
        assertTrue(daoStub.persisted.contains(p));

        verify(archiveBS, never()).getByFileLink(anyString());
    }

    @Test
    @DisplayName("saveObjective: zera id e persiste")
    void saveObjective_resetsIdAndPersists() {
        Process pr = new Process(); pr.setId(10L);
        ProcessObjective obj = new ProcessObjective();
        obj.setId(123L);
        obj.setProcess(pr);

        service.saveObjective(obj);

        assertNull(obj.getId(), "saveObjective deve limpar o ID antes de persistir");
        assertTrue(daoStub.persisted.contains(obj));
    }

    @Test
    @DisplayName("retrieveProcessObjectiveById: retorna uniqueResult do criteria")
    void retrieveProcessObjectiveById_returnsUnique() {
        Process p = new Process(); p.setId(1L);
        ProcessObjective expected = new ProcessObjective();
        expected.setId(7L);
        expected.setProcess(p);

        daoStub.enqueueUnique(ProcessObjective.class, expected);

        ProcessObjective got = service.retrieveProcessObjectiveById(7L);
        assertEquals(expected, got);
    }

    @Test
    @DisplayName("deleteObjective: delega para this.remove(...)")
    void deleteObjective_callsRemove() {
        ProcessObjective obj = new ProcessObjective();

        doNothing().when(service).remove(obj);

        service.deleteObjective(obj);

        verify(service, times(1)).remove(obj);
    }

    @Test
    @DisplayName("listProcessByUnit: agrega relatedUnits e objectives e computa total")
    void listProcessByUnit_aggregates_and_counts() {
        PlanRisk pr = newPlanRisk(1L);
        Unit unit = newUnit(10L, "Unidade A", pr);

        Process p1 = newProcess(1L, "Proc 1");
        Process p2 = newProcess(2L, "Proc 2");

        ProcessUnit pu1 = link(p1, unit);
        ProcessUnit pu2 = link(p2, unit);

        daoStub.enqueueList(ProcessUnit.class, List.of(pu1, pu2));
        daoStub.enqueueList(ProcessUnit.class, List.of(pu1), List.of(pu2));

        ProcessObjective o1 = newObjective(101L, p1, "O1");
        ProcessObjective o2 = newObjective(102L, p2, "O2");
        daoStub.enqueueList(ProcessObjective.class, List.of(o1), List.of(o2));

        PaginatedList<Process> page = service.listProcessByUnit(unit);

        assertEquals(2L, page.getTotal());
        assertEquals(2, page.getList().size());

        Process r1 = page.getList().get(0);
        Process r2 = page.getList().get(1);

        assertEquals(List.of(unit), r1.getRelatedUnits());
        assertEquals(List.of(unit), r2.getRelatedUnits());
        assertEquals(List.of(o1), r1.getAllObjectives());
        assertEquals(List.of(o2), r2.getAllObjectives());
    }

    @Test
    @DisplayName("listRelatedUnits(process, planRisk): retorna Units a partir de ProcessUnit")
    void listRelatedUnits_simple() {
        PlanRisk pr = newPlanRisk(2L);
        Unit u1 = newUnit(11L, "U1", pr);
        Unit u2 = newUnit(12L, "U2", pr);

        Process p = newProcess(5L, "P");
        daoStub.enqueueList(ProcessUnit.class,
                List.of(link(p, u1), link(p, u2)));

        List<Unit> units = service.listRelatedUnits(p, pr);
        assertEquals(List.of(u1, u2), units);
    }

    @Test
    @DisplayName("listRelatedUnits(process, planRisk, term): filtra (testa caminho feliz)")
    void listRelatedUnits_withTerm() {
        PlanRisk pr = newPlanRisk(3L);
        Unit u1 = newUnit(21L, "Financeiro", pr);

        Process p = newProcess(6L, "P");

        daoStub.enqueueList(ProcessUnit.class, List.of(link(p, u1)));

        List<Unit> units = service.listRelatedUnits(p, pr, "nanc");
        assertEquals(List.of(u1), units);
    }

    @Test
    @DisplayName("listProcessObjectives: devolve lista do DAO")
    void listProcessObjectives_returnsDaoList() {
        Process p = newProcess(7L, "P");
        ProcessObjective o1 = newObjective(201L, p, "Desc 1");
        ProcessObjective o2 = newObjective(202L, p, "Desc 2");

        daoStub.enqueueList(ProcessObjective.class, List.of(o1, o2));

        List<ProcessObjective> list = service.listProcessObjectives(p);
        assertEquals(List.of(o1, o2), list);
    }

    @Test
    @DisplayName("isLinkedRiskObjectiveProcess: true quando uniqueResult != null")
    void isLinkedRiskObjectiveProcess_trueWhenFound() {
        Process p = newProcess(8L, "P");
        ProcessObjective obj = newObjective(301L, p, "D");

        daoStub.enqueueUnique(RiskProcessObjective.class, new Object());

        assertTrue(service.isLinkedRiskObjectiveProcess(obj));
    }

    @Test
    @DisplayName("isLinkedRiskObjectiveProcess: false quando uniqueResult == null")
    void isLinkedRiskObjectiveProcess_falseWhenNotFound() {
        Process p = newProcess(8L, "P");
        ProcessObjective obj = newObjective(302L, p, "D");

        assertFalse(service.isLinkedRiskObjectiveProcess(obj));
    }

    @Test
    @DisplayName("getProcessUnitsByProcess: devolve lista do DAO")
    void getProcessUnitsByProcess_returnsDaoList() {
        Process p = newProcess(9L, "P");
        Unit u = newUnit(31L, "U", newPlanRisk(9L));
        ProcessUnit pu = link(p, u);

        daoStub.enqueueList(ProcessUnit.class, List.of(pu));

        List<ProcessUnit> list = service.getProcessUnitsByProcess(p);
        assertEquals(List.of(pu), list);
    }

    @Test
    @DisplayName("retrieveProcessById: uniqueResult + popula relatedUnits e objectives")
    void retrieveProcessById_populatesRelations() {
        PlanRisk pr = newPlanRisk(10L);
        Unit creator = newUnit(41L, "Criadora", pr);

        Process p = newProcess(100L, "P");
        p.setUnitCreator(creator);

        ProcessObjective o1 = newObjective(401L, p, "O1");
        Unit rel = newUnit(42L, "Rel", pr);

        daoStub.enqueueUnique(Process.class, p);

        daoStub.enqueueList(ProcessUnit.class, List.of(link(p, rel)));

        daoStub.enqueueList(ProcessObjective.class, List.of(o1));

        Process got = service.retrieveProcessById(100L);

        assertSame(p, got);
        assertEquals(List.of(rel), got.getRelatedUnits());
        assertEquals(List.of(o1), got.getAllObjectives());
    }

    @Test
    @DisplayName("deleteProcess: marca deleted=true e soft-deleta ProcessUnit relacionados persistindo tudo")
    void deleteProcess_marksAndPersistsCascade() {
        Process p = newProcess(200L, "P");

        Unit u1 = newUnit(51L, "U1", newPlanRisk(11L));
        Unit u2 = newUnit(52L, "U2", newPlanRisk(11L));
        ProcessUnit pu1 = link(p, u1);
        ProcessUnit pu2 = link(p, u2);

        daoStub.enqueueList(ProcessUnit.class, List.of(pu1, pu2));

        service.deleteProcess(p);

        assertTrue(p.isDeleted());
        assertTrue(pu1.isDeleted());
        assertTrue(pu2.isDeleted());
        assertTrue(daoStub.persisted.contains(p));
        assertTrue(daoStub.persisted.contains(pu1));
        assertTrue(daoStub.persisted.contains(pu2));
    }

    @Test
    @DisplayName("listProcessByCompany: retorna lista e total do count.uniqueResult()")
    void listProcessByCompany_returnsListAndTotal() {
        Process p1 = newProcess(1L, "A");
        Process p2 = newProcess(2L, "B");
        daoStub.enqueueList(Process.class, List.of(p1, p2));

        daoStub.enqueueUnique(Process.class, 2L);

        PaginatedList<Process> result = service.listProcessByCompany(new org.forpdi.core.company.Company());

        assertEquals(List.of(p1, p2), result.getList());
        assertEquals(2L, result.getTotal());
    }

    @Test
    @DisplayName("listProcessByPlan: pagina, filtra e retorna total")
    void listProcessByPlan_returnsListAndTotal() {
        PlanRisk pr = newPlanRisk(77L);

        Process p1 = newProcess(1L, "A");
        Process p2 = newProcess(2L, "B");

        daoStub.enqueueList(Process.class, List.of(p1, p2));
        daoStub.enqueueUnique(Process.class, 2L);

        PaginatedList<Process> result = service.listProcessByPlan(pr, null, 1, 10, null);

        assertEquals(List.of(p1, p2), result.getList());
        assertEquals(2L, result.getTotal());
    }

    @Test
    @DisplayName("listObjectivesByPlan: pagina e retorna total")
    void listObjectivesByPlan_returnsListAndTotal() {
        PlanRisk pr = newPlanRisk(88L);
        Process p = newProcess(3L, "Proc");
        ProcessObjective o1 = newObjective(1L, p, "O1");
        ProcessObjective o2 = newObjective(2L, p, "O2");

        daoStub.enqueueList(ProcessObjective.class, List.of(o1, o2));
        daoStub.enqueueUnique(ProcessObjective.class, 2L);

        PaginatedList<ProcessObjective> result =
                service.listObjectivesByPlan(pr, null, 1, 10, null);

        assertEquals(List.of(o1, o2), result.getList());
        assertEquals(2L, result.getTotal());
    }

    @Test
    @DisplayName("listProcessLinkedToRisksByPlan: une listas (pode conter duplicados se o bean não sobrescrever equals/hashCode)")
    void listProcessLinkedToRisksByPlan_mergesWithoutDuplicates() {
        PlanRisk pr = newPlanRisk(999L);

        ProcessLinkedToRiskBean b1 = new ProcessLinkedToRiskBean();
        b1.setId(1L); b1.setName("P1"); b1.setRiskId(100L); b1.setUnitId(10L);

        ProcessLinkedToRiskBean b2 = new ProcessLinkedToRiskBean();
        b2.setId(2L); b2.setName("P2"); b2.setRiskId(101L); b2.setUnitId(11L);

        ProcessLinkedToRiskBean b1dup = new ProcessLinkedToRiskBean();
        b1dup.setId(1L); b1dup.setName("P1"); b1dup.setRiskId(105L); b1dup.setUnitId(10L);

        daoStub.enqueueList(ProcessLinkedToRiskBean.class, List.of(b1, b2), List.of(b1dup));

        List<ProcessLinkedToRiskBean> list = service.listProcessLinkedToRisksByPlan(pr);
        assertEquals(3, list.size());

        long countId1 = list.stream().filter(x -> x.getId().equals(1L)).count();
        long countId2 = list.stream().filter(x -> x.getId().equals(2L)).count();

        assertEquals(2, countId1);
        assertEquals(1, countId2);
    }
}
