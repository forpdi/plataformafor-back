package org.forpdi.core.common;

import java.io.Serializable;
import java.util.List;

public class PaginatedList<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<T> list;
	private Long total;
	
	public PaginatedList() {
		this(null, null);
	}
	public PaginatedList(List<T> list, Long total) {
		this.list = list;
		this.total = total;
	}
	
	public List<T> getList() {
		return list;
	}
	public void setList(List<T> list) {
		this.list = list;
	}
	public Long getTotal() {
		return total;
	}
	public void setTotal(Long total) {
		this.total = total;
	}

}
