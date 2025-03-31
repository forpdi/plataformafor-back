package org.forpdi.core.storage.file;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.io.FilenameUtils;
import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.company.Company;


@Entity(name = Archive.TABLE)
@Table(name = Archive.TABLE)

public class Archive extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_archive";
	private static final long serialVersionUID = 1L;

	@Column(nullable = false, length = 255)
	private String name;
	
	@ManyToOne(optional = false)
	@SkipSerialization
	private Company company;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}
	
	public String getFileLink() {
		String extension = getExtension();
		return String.format("%d.%s", id, extension);
	}
	
	public String getExtension() {
		return FilenameUtils.getExtension(getName());
	}
}
