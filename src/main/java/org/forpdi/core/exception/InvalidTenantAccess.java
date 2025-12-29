package org.forpdi.core.exception;

public class InvalidTenantAccess extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidTenantAccess(String message) {
		super(message);
	}
}
