package org.forpdi.core.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;

/**
 * @author Erick Alves
 * 
 */
@Entity(name = UserPasswordUsed.TABLE)
@Table(name = UserPasswordUsed.TABLE)
public class UserPasswordUsed extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_user_password_used";
	private static final long serialVersionUID = 1L;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	private Date creation = new Date();

	@Column(nullable=false, length=255)
	@SkipSerialization
	private String password;

	@ManyToOne(optional=false)
	private User user;

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
