package org.forpdi.planning.structure;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class FavoriteCleanupServiceTest {

    private FavoriteCleanupService service;

    private JdbcTemplate jdbc;
    private HibernateDAO dao;

    private Criteria crit;

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = FavoriteCleanupService.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @BeforeEach
    void setup() throws Exception {
        service = new FavoriteCleanupService();

        jdbc = mock(JdbcTemplate.class);
        dao  = mock(HibernateDAO.class);
        crit = mock(Criteria.class);

        inject(service, "jdbc", jdbc);
        inject(service, "dao",  dao);

        when(dao.newCriteria(eq(FavoriteLevelInstance.class))).thenReturn(crit);
        when(crit.createAlias(anyString(), anyString())).thenReturn(crit);
        when(crit.add(any())).thenReturn(crit);
    }

    @Test
    @DisplayName("runCleanup: lock não adquirido (GET_LOCK != 1) → não executa limpeza nem libera lock")
    void runCleanup_lockNotAcquired() {

        when(jdbc.queryForObject(eq("SELECT GET_LOCK(?, ?)"), eq(Integer.class), any(), any()))
            .thenReturn(0);

        service.runCleanup();
        verify(dao, never()).newCriteria(any());
        verify(dao, never()).delete(any());
        verify(jdbc, never()).queryForObject(eq("SELECT RELEASE_LOCK(?)"), eq(Integer.class), any());
    }

    @Test
    @DisplayName("runCleanup: lock adquirido e há órfãos → deleta cada um e libera lock")
    void runCleanup_withOrphans_deletesAll_andReleasesLock() {
        when(jdbc.queryForObject(eq("SELECT GET_LOCK(?, ?)"), eq(Integer.class), any(), any()))
            .thenReturn(1);

        FavoriteLevelInstance f1 = new FavoriteLevelInstance();
        FavoriteLevelInstance f2 = new FavoriteLevelInstance();
        when(crit.list()).thenReturn(List.of(f1, f2));

        when(jdbc.queryForObject(eq("SELECT RELEASE_LOCK(?)"), eq(Integer.class), any()))
            .thenReturn(1);

        service.runCleanup();

        verify(dao).newCriteria(eq(FavoriteLevelInstance.class));
        verify(crit).createAlias(eq("companyUser"), eq("cu"));
        verify(crit).createAlias(eq("cu.user"), eq("u"));
        verify(crit).add(any());
        verify(dao).delete(f1);
        verify(dao).delete(f2);

        verify(jdbc).queryForObject(eq("SELECT RELEASE_LOCK(?)"), eq(Integer.class), any());
    }

    @Test
    @DisplayName("runCleanup: lock adquirido e lista vazia → não deleta nada mas libera lock")
    void runCleanup_noOrphans_releasesLock() {
        when(jdbc.queryForObject(eq("SELECT GET_LOCK(?, ?)"), eq(Integer.class), any(), any()))
            .thenReturn(1);

        when(crit.list()).thenReturn(List.of());

        when(jdbc.queryForObject(eq("SELECT RELEASE_LOCK(?)"), eq(Integer.class), any()))
            .thenReturn(1);

        service.runCleanup();

        verify(dao).newCriteria(eq(FavoriteLevelInstance.class));
        verify(crit).list();
        verify(dao, never()).delete(any());
        verify(jdbc).queryForObject(eq("SELECT RELEASE_LOCK(?)"), eq(Integer.class), any());
    }
}
