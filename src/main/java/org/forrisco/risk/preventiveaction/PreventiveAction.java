package org.forrisco.risk.preventiveaction;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.user.User;
import org.forrisco.risk.Risk;

/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = PreventiveAction.TABLE)
@Table(name = PreventiveAction.TABLE)

public class PreventiveAction extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_preventive_action";
	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = Risk.class, optional = false, fetch = FetchType.EAGER)
	private Risk risk;

	@ManyToOne(targetEntity = User.class, optional = false, fetch = FetchType.EAGER)
	private User user;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
	private User manager;

	@ManyToOne(targetEntity=Archive.class,  fetch=FetchType.EAGER)
	private Archive file;

	@Column(nullable = false, length = 4000)
	private String action;

	@Column(nullable = false)
	private boolean accomplished;

	@Temporal(TemporalType.DATE)
	@Column(nullable = true, name="validity_begin")
	private Date validityBegin;
	
	@Temporal(TemporalType.DATE)
	@Column(nullable = true, name="validity_end")
	private Date validityEnd;

	@Transient
	private String fileLink;

	public PreventiveAction() {
	}

	public PreventiveAction(PreventiveAction action) {
		this.user = action.getUser();
		this.action = action.getAction();
		this.accomplished = action.isAccomplished();
		this.validityBegin = action.getValidityBegin();
		this.validityEnd = action.getValidityEnd();
		this.fileLink = action.getFileLink();
		this.file = action.getFile();
	}

	public Risk getRisk() {
		return risk;
	}

	public void setRisk(Risk risk) {
		this.risk = risk;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public User getManager() {
		return manager;
	}

	public void setManager(User manager) {
		this.manager = manager;
	}

	public Archive getFile() {
		return file;
	}

	public void setFile(Archive file) {
		this.file = file;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isAccomplished() {
		return accomplished;
	}
 
	public void setAccomplished(boolean accomplished) {
		this.accomplished = accomplished;
	}

	public String isAccomplishedDescription() {
		return accomplished ? "Sim" : "Não";
	}

	public Date getValidityBegin() {
		return validityBegin;
	}

	public void setValidityBegin(Date validityBegin) {
		this.validityBegin = validityBegin;
	}

	public Date getValidityEnd() {
		return validityEnd;
	}

	public void setValidityEnd(Date validityEnd) {
		this.validityEnd = validityEnd;
	}

	public String getFileLink() {
		return fileLink;
	}

	public void setFileLink(String fileLink) {
		this.fileLink = fileLink;
	}
}