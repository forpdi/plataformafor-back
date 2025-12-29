package org.forpdi.planning.structure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Feature-flag simples controlada no YAML (profile prod). */
@Component
@ConfigurationProperties(prefix = "cleanup.favorites")
public class FavoriteCleanupProperties {
    private boolean enabled = true;
    public boolean isEnabled()             { return enabled; }
    public void    setEnabled(boolean val) { this.enabled = val; }
}
