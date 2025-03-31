package org.forrisco.risk.contingency;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;
import org.forrisco.risk.Risk;

/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = Contingency.TABLE)
@Table(name = Contingency.TABLE)

public class Contingency extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_contingency";
	private static final long serialVersionUID = 1L;


	@ManyToOne(targetEntity=User.class, optional=false, fetch=FetchType.EAGER)
	private User user;

	@ManyToOne(targetEntity=User.class, fetch=FetchType.EAGER)
	private User manager;

	@ManyToOne(targetEntity=Risk.class, optional=false, fetch=FetchType.EAGER)
	private Risk risk;

	@Column(nullable=false, length=4000)
	private String action;

	@Temporal(TemporalType.DATE)
	@Column(nullable = true, name="validity_begin")
	private Date validityBegin;
	
	@Temporal(TemporalType.DATE)
	@Column(nullable = true, name="validity_end")
	private Date validityEnd;

 
	public Contingency() {
	}
 
	public Contingency(Contingency contigency) {
		this.user = contigency.getUser();
		this.action =contigency.getAction();
		validityBegin = contigency.getValidityBegin();
		validityEnd = contigency.getValidityEnd();
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Risk getRisk() {
		return risk;
	}

	public void setRisk(Risk risk) {
		this.risk = risk;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	public User getManager() {
		return manager;
	}

	public void setManager(User manager) {
		this.manager = manager;
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

}