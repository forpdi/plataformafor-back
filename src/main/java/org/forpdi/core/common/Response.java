package org.forpdi.core.common;

import java.io.Serializable;


/**
 * Bean to serialize responses for the clients.
 * @author Renato R. R. de Oliveira
 *
 */
public class Response<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The request was successful? */
	private boolean success;
	/** Error specific/technical info. */
	private String error;
	/** Message to the client side. */
	private String message;
	/** Data payload. */
	private T data;
	
	public Response(boolean success, String message, String error, T data) {
		this.setSuccess(success);
		this.error = error;
		this.message = message;
		this.data = data;
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
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
}
