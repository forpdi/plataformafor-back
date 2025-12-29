package org.forpdi.core.user;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * 
 * @author Renato R. R. de Oliveira
 *
 */
@Entity(name = UserRecoverRequest.TABLE)
@Table(name = UserRecoverRequest.TABLE)
public class UserRecoverRequest implements Serializable {
	public static final String TABLE = "fpdi_user_recover_req";
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(nullable=false, length=128)
	private String token;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=true)
	private Date recover;
	
	@Column(nullable=true, length=64)
	private String recoverIp;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	private Date creation;
	
	@Column(nullable=false, length=64)
	private String creationIp;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	private Date expiration;
	
	private boolean used = false;

	@ManyToOne(targetEntity=User.class, fetch=FetchType.EAGER, optional=false)
	private User user;
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public String getCreationIp() {
		return creationIp;
	}

	public void setCreationIp(String creationIp) {
		this.creationIp = creationIp;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public Date getRecover() {
		return recover;
	}

	public void setRecover(Date recover) {
		this.recover = recover;
	}

	public String getRecoverIp() {
		return recoverIp;
	}

	public void setRecoverIp(String recoverIp) {
		this.recoverIp = recoverIp;
	}
	
}
