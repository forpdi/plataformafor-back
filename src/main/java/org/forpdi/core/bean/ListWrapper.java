package org.forpdi.core.bean;

import java.io.Serializable;
import java.util.List;

public class ListWrapper<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<T> list;
	
	public ListWrapper() {
	}

	public ListWrapper(List<T> list) {
		this.list = list;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}
}
