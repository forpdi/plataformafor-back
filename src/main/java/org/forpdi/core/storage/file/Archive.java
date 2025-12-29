package org.forpdi.core.storage.file;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import org.apache.commons.io.FilenameUtils;
import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.company.Company;
import org.forpdi.core.utils.SanitizeUtil;

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

	@PrePersist
	@PreUpdate
	private void sanitizeName() {
		this.name = SanitizeUtil.sanitizeFilename(this.name);
	}

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
		return String.format("%d.%s", id, getExtension());
	}

	public String getExtension() {
		return FilenameUtils.getExtension(this.name);
	}
}
