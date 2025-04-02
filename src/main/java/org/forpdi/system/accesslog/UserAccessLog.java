package org.forpdi.system.accesslog;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = UserAccessLog.TABLE)
@Table(name = UserAccessLog.TABLE)
public class UserAccessLog {
	public static final String TABLE = "fpdi_user_access_log";
	
	@Id
	@Column(name="user_id")
    private Long userId;
	
	@Column(name="fpdi_last_access")
	private LocalDateTime fpdiLastAccess;
	
	@Column(name="frisco_last_access")
	private LocalDateTime  friscoLastAccess;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public LocalDateTime getFpdiLastAccess() {
		return fpdiLastAccess;
	}

	public void setFpdiLastAccess(LocalDateTime fpdiLastAccess) {
		this.fpdiLastAccess = fpdiLastAccess;
	}

	public LocalDateTime getFriscoLastAccess() {
		return friscoLastAccess;
	}

	public void setFriscoLastAccess(LocalDateTime friscoLastAccess) {
		this.friscoLastAccess = friscoLastAccess;
	}
}
