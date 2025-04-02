package org.forpdi.core.user.authz.permission;

import org.forpdi.core.user.authz.Permission;
import org.forpdi.security.authz.AccessLevels;

public class EditMessagesPermission extends Permission {

	@Override
	public String getDisplayName() {
		return "Editar Textos do Sistema";
	}

	@Override
	public int getRequiredAccessLevel() {
		return AccessLevels.COMPANY_ADMIN.getLevel();
	}

	@Override
	public String getDescription() {
		
		return "Editar textos e mensagens do sistema para esta instituição.";
	}
}
