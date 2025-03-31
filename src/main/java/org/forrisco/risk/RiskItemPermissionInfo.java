package org.forrisco.risk;

public class RiskItemPermissionInfo {
    private final boolean hasPermission;
    private final boolean isResponsible;
    private final boolean isRiskResponsible;
    
	public RiskItemPermissionInfo(boolean hasPermission, boolean isResponsible, boolean isRiskResponsible) {
		this.hasPermission = hasPermission;
		this.isResponsible = isResponsible;
		this.isRiskResponsible = isRiskResponsible;
	}

	public boolean isHasPermission() {
		return hasPermission;
	}

	public boolean isResponsible() {
		return isResponsible;
	}

	public boolean isRiskResponsible() {
		return isRiskResponsible;
	}
	
	public boolean isRiskResponsibleOrHasPermission() {
		return hasPermission || isRiskResponsible;
	}
}
