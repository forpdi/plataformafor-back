package org.forpdi.planning.structure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Executa a faxina logo após a aplicação subir,
 * se a flag cleanup.favorites.enabled=true.
 */
@Component
public class FavoriteCleanupRunner implements ApplicationRunner {

    @Autowired private FavoriteCleanupService    service;
    @Autowired private FavoriteCleanupProperties props;

    @Override
    public void run(ApplicationArguments args) {
        if (props.isEnabled()) {
            service.runCleanup();
        }
    }
}
