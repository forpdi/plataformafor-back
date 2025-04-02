package org.forpdi.core.bean;

import java.io.Serializable;

public class ItemToSelect implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private String name;
	
	public ItemToSelect() {
	}

	public ItemToSelect(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
