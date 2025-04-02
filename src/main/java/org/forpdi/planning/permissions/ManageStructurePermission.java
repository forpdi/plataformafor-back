package org.forpdi.planning.permissions;

import org.forpdi.core.user.authz.Permission;
import org.forpdi.security.authz.AccessLevels;

public class ManageStructurePermission extends Permission {

	@Override
	public String getDisplayName() {
		return "Gerenciar Estruturas de Planos de Metas";
	}

	@Override
	public int getRequiredAccessLevel() {
		return AccessLevels.SYSTEM_ADMIN.getLevel();
	}

	@Override
	public String getDescription() {
		return "Importar estruturas de Plano de Metas, Excluir estruturas de Plano de Metas, Visualizar estruturas de Plano de Metas";
	}
}
