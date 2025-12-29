package org.forpdi.planning.structure;

import java.util.List;

import jakarta.transaction.Transactional;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class FavoriteCleanupService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteCleanupService.class);

    private static final String LOCK_NAME    = "fpdi_favorite_cleanup_lock";
    private static final int    LOCK_TIMEOUT = 0;

    @Autowired private JdbcTemplate    jdbc;
    @Autowired private HibernateDAO    dao;

    @Transactional
    public void runCleanup() {
        Integer gotLock = jdbc.queryForObject(
            "SELECT GET_LOCK(?, ?)", Integer.class, LOCK_NAME, LOCK_TIMEOUT);

        if (gotLock == null || gotLock != 1) {
            log.info("[CLEANUP] outra instância já executando – abortando.");
            return;
        }

        try {
            Criteria crit = dao.newCriteria(FavoriteLevelInstance.class)
                               .createAlias("companyUser", "cu")
                               .createAlias("cu.user",       "u")
                               .add(Restrictions.eq("u.deleted", true));

            @SuppressWarnings("unchecked")
            List<FavoriteLevelInstance> orphan = crit.list();
            log.info("[CLEANUP] {} favoritos órfãos encontrados", orphan.size());

            orphan.forEach(dao::delete);
            log.info("[CLEANUP] concluído com sucesso");

        } finally {
            jdbc.queryForObject(
                "SELECT RELEASE_LOCK(?)", Integer.class, LOCK_NAME);
        }
    }
}
