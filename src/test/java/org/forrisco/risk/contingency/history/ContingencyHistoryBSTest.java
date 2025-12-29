package org.forrisco.risk.contingency.history;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.user.User;
import org.forrisco.risk.contingency.Contingency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContingencyHistoryBSTest {

    private ContingencyHistoryBS service;
    private List<ContingencyHistory> persisted;

    @BeforeEach
    void setup() {
        service = spy(new ContingencyHistoryBS());
        persisted = new CopyOnWriteArrayList<>();

        doAnswer(inv -> {
            persisted.add((ContingencyHistory) inv.getArgument(0));
            return null;
        })
                .when(service).persist(any(ContingencyHistory.class));
    }

    private static Date date(int y, int m, int d) {
        return Date.from(LocalDate.of(y, m, d)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static User user(Long id, String name) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        return u;
    }

    private static Contingency contingency(Long id, String actionHtml, Date begin, Date end, User resp, User man) {
        Contingency c = new Contingency();
        c.setId(id);
        c.setAction(actionHtml);
        c.setValidityBegin(begin);
        c.setValidityEnd(end);
        c.setUser(resp);
        c.setManager(man);
        return c;
    }

    private static ContingencyHistory byField(List<ContingencyHistory> list, String field) {
        return list.stream().filter(h -> field.equals(h.getChangedField()))
                .findFirst().orElseThrow(() -> new AssertionError("Histórico não encontrado: " + field));
    }

    @Test
    @DisplayName("saveContingencyChanges: registra mudanças em ação (strip HTML), datas, responsável e gestor")
    void saveContingencyChanges_logsAllChangedFields() {
        User oldResp = user(1L, "Ana");
        User newResp = user(2L, "Beatriz");
        User oldMan = user(10L, "Carlos");
        User newMan = user(11L, "Daniel");

        Contingency oldC = contingency(100L, "<p>Antiga</p>", date(2024, 1, 10), date(2024, 12, 31), oldResp, oldMan);
        Contingency newC = contingency(100L, "<b>Nova</b>", date(2025, 2, 1), date(2025, 12, 31), newResp, newMan);

        User editor = user(99L, "Editor");

        service.saveContingencyChanges(oldC, newC, editor);

        assertEquals(5, persisted.size(), "Deveria ter 5 entradas de histórico.");

        ContingencyHistory acao = byField(persisted, "Ação");
        assertEquals("Antiga", acao.getOldValue());
        assertEquals("Nova", acao.getNewValue());

        ContingencyHistory inicio = byField(persisted, "Início Vigência");
        assertEquals("10/01/2024", inicio.getOldValue());
        assertEquals("01/02/2025", inicio.getNewValue());

        ContingencyHistory fim = byField(persisted, "Fim Vigência");
        assertEquals("31/12/2024", fim.getOldValue());
        assertEquals("31/12/2025", fim.getNewValue());

        ContingencyHistory resp = byField(persisted, "Responsável Técnico");
        assertEquals("Ana", resp.getOldValue());
        assertEquals("Beatriz", resp.getNewValue());

        ContingencyHistory gestor = byField(persisted, "Gestor");
        assertEquals("Carlos", gestor.getOldValue());
        assertEquals("Daniel", gestor.getNewValue());

        persisted.forEach(h -> {
            assertEquals(oldC, h.getContingency());
            assertEquals(editor, h.getResponsibleUser());
            assertFalse(h.isDeleted());
            assertNotNull(h.getChangeDate());
        });
    }

    @Test
    @DisplayName("saveContingencyChanges: sem mudanças → não persiste nada")
    void saveContingencyChanges_noChanges_noPersist() {
        User u = user(1L, "Ana");
        User m = user(10L, "Carlos");

        Contingency oldC = contingency(100L, "<p>Mesmo</p>", date(2024, 1, 10), date(2024, 12, 31), u, m);
        Contingency newC = contingency(100L, "<b>Mesmo</b>", date(2024, 1, 10), date(2024, 12, 31), u, m);

        User editor = user(99L, "Editor");

        service.saveContingencyChanges(oldC, newC, editor);

        assertEquals(0, persisted.size(), "Não deveria persistir histórico quando não há mudanças.");
    }

    @Test
    @DisplayName("save: força deleted=false e persiste")
    void save_marksDeletedFalse() {
        ContingencyHistory h = new ContingencyHistory();
        h.setDeleted(true);

        service.save(h);

        assertEquals(1, persisted.size());
        ContingencyHistory p = persisted.get(0);
        assertFalse(p.isDeleted(), "deleted deve ser false em save().");
    }

    @Test
    @DisplayName("DTO formata data como 'dd/MM/yyyy - HH:mm'")
    void dto_formats_changeDate() {
        ContingencyHistory h = new ContingencyHistory();
        User u = user(10L, "Editor");
        h.setId(7L);
        h.setResponsibleUser(u);
        h.setChangedField("Campo");
        h.setOldValue("A");
        h.setNewValue("B");
        h.setChangeDate(LocalDateTime.of(2025, 8, 1, 9, 5));

        ContingencyHistoryBS.ContingencyHistoryDTO dto = new ContingencyHistoryBS.ContingencyHistoryDTO(h);
        assertEquals(7L, dto.getId());
        assertEquals("Editor", dto.getResponsibleUser());
        assertEquals("Campo", dto.getChangedField());
        assertEquals("A", dto.getOldValue());
        assertEquals("B", dto.getNewValue());
        assertEquals("01/08/2025 - 09:05", dto.getChangeDate());
    }

    @Test
    @DisplayName("listHistoryByContingency: pagina e ordena por changeDate desc (sem termo)")
    @SuppressWarnings("unchecked")
    void listHistoryByContingency_minimalFlow() {
        DefaultParams params = new DefaultParams();
        params.setPage(1);
        params.setPageSize(5);

        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);

        CriteriaQuery<ContingencyHistory> cq = mock(CriteriaQuery.class);
        Root<ContingencyHistory> root = mock(Root.class);
        Predicate p1 = mock(Predicate.class), p2 = mock(Predicate.class);
        Order order = mock(Order.class);

        Join<ContingencyHistory, User> userJoin = mock(Join.class);
        doReturn(userJoin).when(root).join("responsibleUser");

        Path<Object> deletedPath = mock(Path.class);
        when(root.get("deleted")).thenReturn(deletedPath);

        Path<Object> contingencyPath = mock(Path.class);
        Path<Object> contingencyIdPath = mock(Path.class);
        when(root.get("contingency")).thenReturn(contingencyPath);
        when(contingencyPath.get("id")).thenReturn(contingencyIdPath);

        Path<Object> changeDatePath = mock(Path.class);
        when(root.get("changeDate")).thenReturn(changeDatePath);

        CriteriaQuery<Long> countCQ = mock(CriteriaQuery.class);
        Root<ContingencyHistory> countRoot = mock(Root.class);
        Expression<Long> countExpr = mock(Expression.class);

        TypedQuery<ContingencyHistory> listQuery = mock(TypedQuery.class);
        TypedQuery<Long> countQuery = mock(TypedQuery.class);

        try {
            var f = ContingencyHistoryBS.class.getDeclaredField("em");
            f.setAccessible(true);
            f.set(service, em);
        } catch (Exception e) {
            fail(e);
        }

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(ContingencyHistory.class)).thenReturn(cq);
        when(cq.from(ContingencyHistory.class)).thenReturn(root);
        when(cb.equal(any(), any())).thenReturn(p1, p2);
        when(cb.desc(any())).thenReturn(order);
        when(cq.select(root)).thenReturn(cq);
        when(cq.where(any(Predicate[].class))).thenReturn(cq);
        when(cq.orderBy(order)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(listQuery);
        when(listQuery.setFirstResult(anyInt())).thenReturn(listQuery);
        when(listQuery.setMaxResults(anyInt())).thenReturn(listQuery);

        ContingencyHistory h = new ContingencyHistory();
        User u = new User();
        u.setId(10L);
        u.setName("Editor");
        h.setId(1L);
        h.setResponsibleUser(u);
        h.setChangedField("Ação");
        h.setOldValue("x");
        h.setNewValue("y");
        h.setChangeDate(LocalDateTime.of(2025, 8, 1, 9, 0));
        when(listQuery.getResultList()).thenReturn(List.of(h));

        when(cb.createQuery(Long.class)).thenReturn(countCQ);
        when(countCQ.from(ContingencyHistory.class)).thenReturn(countRoot);

        Path<Object> countDeleted = mock(Path.class);
        when(countRoot.get("deleted")).thenReturn(countDeleted);
        Path<Object> countCont = mock(Path.class);
        Path<Object> countContId = mock(Path.class);
        when(countRoot.get("contingency")).thenReturn(countCont);
        when(countCont.get("id")).thenReturn(countContId);

        when(cb.countDistinct(countRoot.get("id"))).thenReturn(countExpr);
        when(countCQ.select(countExpr)).thenReturn(countCQ);
        when(countCQ.where(any(Predicate[].class))).thenReturn(countCQ);
        when(em.createQuery(countCQ)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        PaginatedList<ContingencyHistoryBS.ContingencyHistoryDTO> page = service.listHistoryByContingency(99L, params);

        assertNotNull(page);
        assertEquals(1L, page.getTotal());
        assertEquals(1, page.getList().size());
        assertEquals("Ação", page.getList().get(0).getChangedField());

        verify(listQuery).setFirstResult(0);
        verify(listQuery).setMaxResults(5);
        verify(cb).desc(root.get("changeDate"));
    }
}
