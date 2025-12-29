package org.forpdi.core.communication;

import java.util.Date;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.company.Company;
import org.forpdi.core.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * 
 * 
 */
@Entity(name = Communication.TABLE)
@Table(name = Communication.TABLE)
public class Communication extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_communication";
	private static final long serialVersionUID = 1L;

	@SkipSerialization
	@ManyToOne(targetEntity=Company.class, optional=false, fetch=FetchType.EAGER)
	private Company company;
	
	@Column(nullable=false, length=50)
	private String title;
	
	@Column(nullable=false, length=1000)
	private String message;
	
	@Temporal(TemporalType.DATE)
	@Column(nullable=false)
	private Date validityBegin = new Date();
	
	@Temporal(TemporalType.DATE)
	@Column(nullable=true)
	private Date validityEnd = new Date();

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	private Date lastModification = new Date();
	
	@Column(nullable=false)
	private Boolean showPopup;

	@ManyToOne(targetEntity = User.class, optional = false, fetch = FetchType.EAGER)
	private User responsible;
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getMessage() {
		return message;			
	}

	public void setMessage(String message) {
		this.message = message;
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

	public Date getlastModification() {
		return lastModification;
	}

	public void setLastModification(Date lastModification) {
		this.lastModification = lastModification;
	}

	public Boolean getShowPopup() {
		return showPopup;
	}

	public void setShowPopup(Boolean showPopup) {
		this.showPopup = showPopup;
	}
	
	public void setResponsible(User responsible) {
		this.responsible = responsible;
	}
	
	public User getResponsible() {
		return responsible;
	}

		public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

}
