package org.forpdi.core.common;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.forpdi.security.auth.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public abstract class AbstractController {
	protected final Logger LOGGER;

	/** Prefixo dos end points REST. */
	protected static final String BASEPATH = "/api";
	
	/** Objecto da sessão do usuário. Injetado automaticamente. */
	@Autowired
	protected UserSession userSession;
	
	@Autowired
	protected HttpServletResponse response;
	
	@Autowired
	protected HttpServletRequest request;

	public AbstractController() {
		LOGGER = LoggerFactory.getLogger(this.getClass());
	}
	
	protected <T extends Serializable> ResponseEntity<Response<T>> forbidden() {
		return this.fail("Forbidden", "You do not have permission for this action.", HttpServletResponse.SC_FORBIDDEN);
	}

	protected <T extends Serializable> ResponseEntity<Response<T>> notFound() {
		return this.fail("Not found", null, HttpServletResponse.SC_NOT_FOUND);
	}

	protected <T extends Serializable> ResponseEntity<Response<T>> fail() {
		return this.fail(null, null);
	}

	protected <T extends Serializable> ResponseEntity<Response<T>> fail(String message) {
		return this.fail(null, message);
	}
	
	protected <T extends Serializable> ResponseEntity<Response<T>> fail(String error, String message) {
		return this.fail(error, message, HttpServletResponse.SC_BAD_REQUEST);
	}
	
	protected <T extends Serializable> ResponseEntity<Response<T>> fail(String error, String message, int httpStatusCode)  {
		Response<T> response = new Response<T>(false, message, error, null);
		return ResponseEntity.status(httpStatusCode).body(response);
	}
	
	protected <T extends Serializable> ResponseEntity<Response<T>> success() {
		return this.success(null, "");
	}

	protected <T extends Serializable> ResponseEntity<Response<T>> success(String message) {
		return this.success(null, message);
	}

	protected <T extends Serializable> ResponseEntity<Response<T>> success(T data) {
		return this.success(data, null);
	}
	
	protected <T extends Serializable> ResponseEntity<Response<T>> success(T data, String message) {
		Response<T> response = new Response<T>(true, message, null, data);
		return ResponseEntity.ok(response);
	}
	
	protected <T extends Serializable> ResponseEntity<ListResponse<T>> success(List<T> list, Long total) {
		return this.success(list, total, null);
	}
	
	protected <T extends Serializable> ResponseEntity<ListResponse<T>> success(PaginatedList<T> list) {
		return this.success(list.getList(), list.getTotal(), null);
	}
	
	protected <T extends Serializable> ResponseEntity<ListResponse<T>> success(List<T> list, Long total, String message) {
		ListResponse<T> response = new ListResponse<T>(true, message, null, list, total);
		return ResponseEntity.ok(response);
	}
	
	protected ResponseEntity<?> nothing() {
		return ResponseEntity.noContent().build();
	}
}
