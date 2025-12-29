package org.forpdi.core.jobs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.forpdi.security.auth.RefreshTokenBlacklist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class CleanRefreshTokenBlacklistTaskTest {

    private CleanRefreshTokenBlacklistTask task;
    private EntityManager em;

    @BeforeEach
    void setup() throws Exception {
        task = new CleanRefreshTokenBlacklistTask();
        em = mock(EntityManager.class);

        var f = CleanRefreshTokenBlacklistTask.class.getDeclaredField("entityManager");
        f.setAccessible(true);
        f.set(task, em);
    }

    @Test
    @DisplayName("execute: remove tokens expirados retornados pela query")
    void execute_removesExpiredTokens() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);
        CriteriaQuery<RefreshTokenBlacklist> cq = mock(CriteriaQuery.class);
        Root<RefreshTokenBlacklist> root = mock(Root.class);
        TypedQuery<RefreshTokenBlacklist> tq = mock(TypedQuery.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(RefreshTokenBlacklist.class)).thenReturn(cq);
        when(cq.from(RefreshTokenBlacklist.class)).thenReturn(root);
        when(em.createQuery(cq)).thenReturn(tq);

        RefreshTokenBlacklist token1 = new RefreshTokenBlacklist();
        RefreshTokenBlacklist token2 = new RefreshTokenBlacklist();
        when(tq.getResultList()).thenReturn(List.of(token1, token2));

        task.execute();

        verify(em).remove(token1);
        verify(em).remove(token2);
    }

    @Test
    @DisplayName("execute: não remove nada se lista vazia")
    void execute_noTokens_noRemove() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);
        CriteriaQuery<RefreshTokenBlacklist> cq = mock(CriteriaQuery.class);
        Root<RefreshTokenBlacklist> root = mock(Root.class);
        TypedQuery<RefreshTokenBlacklist> tq = mock(TypedQuery.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(RefreshTokenBlacklist.class)).thenReturn(cq);
        when(cq.from(RefreshTokenBlacklist.class)).thenReturn(root);
        when(em.createQuery(cq)).thenReturn(tq);

        when(tq.getResultList()).thenReturn(List.of());

        task.execute();

        verify(em, never()).remove(any());
    }
}
