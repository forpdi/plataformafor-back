package org.forpdi.security.authz;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Configuration
public class AuthzConfig {
	@Bean
    public RoleHierarchy roleHierarchy(){
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        String hierarchy = this.getHierarchyAsString();
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }

	public String getHierarchyAsString() {
        List<AccessLevels> accessLevels = AccessLevels.getOrderedByLevel();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < accessLevels.size(); i++) {
        	String accessLevelName = accessLevels.get(i).asRoleName();
        	builder.append(accessLevelName);
        	if (i < accessLevels.size() - 1) {
        		builder.append(" > ");
        	}
		}
        return builder.toString();
	}
}
