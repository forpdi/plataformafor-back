package org.forpdi.core.user.authz;

import org.forpdi.security.authz.AccessLevels;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PermissionTest {

    @Test
    @DisplayName("Deve retornar nível de acesso AUTHENTICATED")
    void test_required_access_level_returns_authenticated() {
        Permission permission = new Permission() {
			@Override
			public String getDisplayName() {
				return "";
			}

			@Override
            public String getDescription() {
                return "Test Permission";
            }
        };

        int accessLevel = permission.getRequiredAccessLevel();

        assertEquals(AccessLevels.AUTHENTICATED.getLevel(), accessLevel,
            "O nível de acesso deve ser AUTHENTICATED");
    }
}
