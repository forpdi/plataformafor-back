package org.forpdi.core.location;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
