package org.forrisco.core.process;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.user.User;
import org.forrisco.core.process.ProcessHistoryBS.ProcessHistoryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes para ProcessHistoryBS")
class ProcessHistoryBSTest {

    @Spy
    @InjectMocks
    private ProcessHistoryBS processHistoryBS;

    @Mock
    private EntityManager em;

    @Mock
    private Process oldProcess, newProcess;

    @Mock
    private User responsibleUser;

    @Test
    @DisplayName("save: Deve definir 'deleted' como false e persistir")
    void save_shouldSetDeletedToFalseAndPersist() {
        doNothing().when(processHistoryBS).persist(any(ProcessHistory.class));

        ProcessHistory history = new ProcessHistory();
        history.setDeleted(true);

        processHistoryBS.save(history);

        ArgumentCaptor<ProcessHistory> captor = ArgumentCaptor.forClass(ProcessHistory.class);
        verify(processHistoryBS).persist(captor.capture());

        assertFalse(captor.getValue().isDeleted(), "O campo 'deleted' deveria ter sido definido como false.");
    }

    @Test
    @DisplayName("saveProcessChanges: Deve salvar histórico quando o nome do processo muda")
    void saveProcessChanges_whenNameChanges_shouldPersistHistory() {
        doNothing().when(processHistoryBS).persist(any(ProcessHistory.class));

        when(oldProcess.getName()).thenReturn("Processo Antigo");
        when(newProcess.getName()).thenReturn("Processo Novo");

        processHistoryBS.saveProcessChanges(oldProcess, newProcess, responsibleUser, null, null, null, null);

        ArgumentCaptor<ProcessHistory> captor = ArgumentCaptor.forClass(ProcessHistory.class);
        verify(processHistoryBS, times(1)).persist(captor.capture());

        ProcessHistory savedHistory = captor.getValue();
        assertEquals("Nome", savedHistory.getChangedField());
        assertEquals("Processo Antigo", savedHistory.getOldValue());
        assertEquals("Processo Novo", savedHistory.getNewValue());
        assertEquals(responsibleUser, savedHistory.getResponsibleUser());
    }

    @Test
    @DisplayName("saveProcessChanges: Não deve salvar histórico quando os campos são iguais")
    void saveProcessChanges_whenFieldsAreEqual_shouldNotPersist() {
        when(oldProcess.getName()).thenReturn("Processo Igual");
        when(newProcess.getName()).thenReturn("Processo Igual");
        when(oldProcess.getFileLink()).thenReturn("link");
        when(newProcess.getFileLink()).thenReturn("link");

        List<String> objectives = List.of("Obj A");
        List<String> units = List.of("Unit B");

        processHistoryBS.saveProcessChanges(oldProcess, newProcess, responsibleUser, units, units, objectives,
                objectives);
        verify(processHistoryBS, never()).persist(any(ProcessHistory.class));
    }

    @Test
    @DisplayName("saveProcessChanges: Deve normalizar, ordenar e salvar mudança em lista de objetivos")
    void saveProcessChanges_whenObjectiveListChanges_shouldUseNormalizedString() {
        doNothing().when(processHistoryBS).persist(any(ProcessHistory.class));

        List<String> oldObjectives = List.of("<b>Objetivo B</b>", "   Objetivo A  ");
        List<String> newObjectives = List.of("Objetivo C", "Objetivo A");

        processHistoryBS.saveProcessChanges(oldProcess, newProcess, responsibleUser, null, null, oldObjectives,
                newObjectives);

        ArgumentCaptor<ProcessHistory> captor = ArgumentCaptor.forClass(ProcessHistory.class);
        verify(processHistoryBS, times(1)).persist(captor.capture());

        ProcessHistory savedHistory = captor.getValue();
        assertEquals("Objetivos do Processo", savedHistory.getChangedField());
        assertEquals("Objetivo A, Objetivo B", savedHistory.getOldValue());
        assertEquals("Objetivo A, Objetivo C", savedHistory.getNewValue());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @DisplayName("listHistoryByProcess: Deve construir a consulta e mapear para DTO corretamente")
    void listHistoryByProcess_shouldBuildQueryAndMapToDto() {
        Long processId = 1L;
        DefaultParams params = new DefaultParams();
        params.setPage(1);
        params.setPageSize(10);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<ProcessHistory> cq = mock(CriteriaQuery.class, RETURNS_DEEP_STUBS);
        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class, RETURNS_DEEP_STUBS);
        Root<ProcessHistory> root = mock(Root.class, RETURNS_DEEP_STUBS);
        Root<ProcessHistory> countRoot = mock(Root.class, RETURNS_DEEP_STUBS);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(ProcessHistory.class)).thenReturn(cq);
        when(cq.from(ProcessHistory.class)).thenReturn(root);

        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(countQuery.from(ProcessHistory.class)).thenReturn(countRoot);

        Path<Boolean> pathDeleted = mock(Path.class);
        Path<Object> pathChangeDate = mock(Path.class);
        Path<Long> pathProcessId = mock(Path.class);
        Path<Long> pathId = mock(Path.class);

        when(root.get("deleted")).thenReturn((Path) pathDeleted);
        when(root.get("changeDate")).thenReturn((Path) pathChangeDate);
        when(root.get("process").get("id")).thenReturn((Path) pathProcessId);
        when(countRoot.get("id")).thenReturn((Path) pathId);

        Predicate pNotDeleted = mock(Predicate.class);
        Predicate pProcessEq = mock(Predicate.class);
        Predicate pAnd = mock(Predicate.class);
        Order orderDesc = mock(Order.class);

        when(cb.isFalse(pathDeleted)).thenReturn(pNotDeleted);
        when(cb.equal(pathProcessId, processId)).thenReturn(pProcessEq);
        when(cb.and(pNotDeleted, pProcessEq)).thenReturn(pAnd);
        when(cb.desc(pathChangeDate)).thenReturn(orderDesc);

        when(cq.select(root)).thenReturn(cq);
        when(cq.where(pAnd)).thenReturn(cq);
        when(cq.orderBy(orderDesc)).thenReturn(cq);
        when(countQuery.select(any())).thenReturn(countQuery);
        when(countQuery.where(any(Predicate.class))).thenReturn(countQuery);

        TypedQuery<ProcessHistory> typedQuery = mock(TypedQuery.class);
        TypedQuery<Long> typedCountQuery = mock(TypedQuery.class);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(em.createQuery(countQuery)).thenReturn(typedCountQuery);
        when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);

        User user = new User();
        user.setName("Usuário Teste");
        ProcessHistory historyEntry = new ProcessHistory();
        historyEntry.setId(100L);
        historyEntry.setResponsibleUser(user);
        historyEntry.setChangedField("Teste");
        historyEntry.setOldValue("A");
        historyEntry.setNewValue("B");
        historyEntry.setChangeDate(java.time.LocalDateTime.of(2025, 9, 4, 10, 0));

        when(typedQuery.getResultList()).thenReturn(java.util.List.of(historyEntry));
        when(typedCountQuery.getSingleResult()).thenReturn(1L);

        PaginatedList<ProcessHistoryBS.ProcessHistoryDTO> result = processHistoryBS.listHistoryByProcess(1L, params);

        assertNotNull(result);
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getList().size());
        var dto = result.getList().get(0);
        assertEquals(100L, dto.getId());
        assertEquals("Usuário Teste", dto.getResponsibleUser());
        assertEquals("Teste", dto.getChangedField());
        assertEquals("04/09/2025 - 10:00", dto.getChangeDate());
    }
}
