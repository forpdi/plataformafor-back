package org.forpdi.planning.fields.attachment;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;
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
	
	@Transient private Long exportStructureLevelInstanceId;
	
	@Transient private String exportAuthorMail;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFileLink() {
		return fileLink;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFileLink(String fileLink) {
		this.fileLink = fileLink;
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

	public void setLevelInstance(StructureLevelInstance levelInstance) {
		this.levelInstance = levelInstance;
	}

	public Long getExportStructureLevelInstanceId() {
		return exportStructureLevelInstanceId;
	}

	public void setExportStructureLevelInstanceId(Long exportStructureLevelInstanceId) {
		this.exportStructureLevelInstanceId = exportStructureLevelInstanceId;
	}

	public String getExportAuthorMail() {
		return exportAuthorMail;
	}

	public void setExportAuthorMail(String exportAuthorMail) {
		this.exportAuthorMail = exportAuthorMail;
	}
}
