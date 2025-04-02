package org.forpdi.core.company;

import org.springframework.stereotype.Component;

@Component
public class CompanyDomainContext {

	private static ThreadLocal<CompanyDomain> currentDomain = new InheritableThreadLocal<>();
	
	public static void setCurrentDomain(CompanyDomain domain) {
		currentDomain.set(domain);
	}
	
	public static void clear() {
		currentDomain.set(null);
	}
	
	public CompanyDomain get() {
		return currentDomain.get();
	}
	
	public Company getCompany() {
		return get().getCompany();
	}
	
	public void validateTenant(Company resourceCompany) {
		Company currentCompany = getCompany();
		if (!resourceCompany.equals(currentCompany)) {
			throw new IllegalStateException("Você não tem acesso à este recurso");
		}
	}

}
