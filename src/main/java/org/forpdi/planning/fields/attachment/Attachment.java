package org.forpdi.planning.fields.attachment;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.planning.structure.StructureLevelInstance;

@Entity(name = Attachment.TABLE)
@Table(name = Attachment.TABLE)
public class Attachment extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_attachment";
	private static final long serialVersionUID = 1L;

	@Column(nullable = false, length = 250)
	private String name;

	@Column(nullable = true, length = 250)
	private String description;

	@Column(nullable = false)
	private String fileLink;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Date creation = new Date();

	@ManyToOne(targetEntity = User.class, optional = true, fetch = FetchType.EAGER)
	private User author;

	@SkipSerialization
	@ManyToOne(targetEntity = StructureLevelInstance.class, optional = false, fetch = FetchType.EAGER)
	private StructureLevelInstance levelInstance;

	@Transient
	private Long exportStructureLevelInstanceId;

	@Transient
	private String exportAuthorMail;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String d) {
		this.description = d;
	}

	public String getFileLink() {
		return fileLink;
	}

	public void setFileLink(String f) {
		this.fileLink = f;
	}

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public StructureLevelInstance getLevelInstance() {
		return levelInstance;
	}

	public void setLevelInstance(StructureLevelInstance level) {
		this.levelInstance = level;
	}

	public Long getExportStructureLevelInstanceId() {
		return exportStructureLevelInstanceId;
	}

	public void setExportStructureLevelInstanceId(Long id) {
		this.exportStructureLevelInstanceId = id;
	}

	public String getExportAuthorMail() {
		return exportAuthorMail;
	}

	public void setExportAuthorMail(String mail) {
		this.exportAuthorMail = mail;
	}
}
