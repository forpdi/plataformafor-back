package org.forpdi.core.common;

import java.io.Serializable;
import java.util.List;


/**
 * Bean to serialize responses for the clients with list payloads.
 * @author Renato R. R. de Oliveira
 *
 */
public class ListResponse<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The request was successful? */
	private boolean success;
	/** Error specific/technical info. */
	private String error;
	/** Message to the client side. */
	private String message;
	/** Data list payload. */
	private List<T> data;
	/** Total size of the list, if the result is paginated. */
	private Long total;
	
	public ListResponse(boolean success, String message, String error, List<T> data, Long total) {
		this.success = success;
		this.error = error;
		this.message = message;
		this.data = data;
		this.total = total;
	}
	
	public ListResponse(boolean success, String message, String error, PaginatedList<T> list) {
		this(success, message, error, list.getList(), list.getTotal());
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}
	
}
