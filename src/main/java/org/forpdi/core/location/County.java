package org.forpdi.core.location;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.forpdi.core.common.SimpleEntity;

@Entity(name = County.TABLE)
@Table(name = County.TABLE)
public class County extends SimpleEntity {
	public static final String TABLE = "fpdi_county";
	private static final long serialVersionUID = 1L;

	@Column(nullable=false, length=255)
	private String name;
	
	@ManyToOne(targetEntity=UF.class, fetch=FetchType.EAGER, optional=false)
	private UF uf;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UF getUf() {
		return uf;
	}

	public void setUf(UF uf) {
		this.uf = uf;
	}
}
