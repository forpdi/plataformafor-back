package org.forpdi.core.location;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.forpdi.core.common.SimpleEntity;

@Entity(name = Region.TABLE)
@Table(name = Region.TABLE)
public class Region extends SimpleEntity {
	public static final String TABLE = "fpdi_region";
	private static final long serialVersionUID = 1L;
	
	@Column(nullable=false, length=255)
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
