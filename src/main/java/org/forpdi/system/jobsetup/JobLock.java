package org.forpdi.system.jobsetup;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = JobLock.TABLE)
@Table(name = JobLock.TABLE)
public class JobLock {
	public static final String TABLE = "fpdi_job_lock";
	
	@Id
	@Column(name = "job_name")
	private String jobName;
	
	@Column(name = "locked_at")
	private LocalDateTime lockedAt;

	@Column(name = "expiry_at")
	private LocalDateTime expiryAt;

	public JobLock(String jobName, LocalDateTime lockedAt, LocalDateTime expiryAt) {
		this.jobName = jobName;
		this.lockedAt = lockedAt;
		this.expiryAt = expiryAt;
	}
	
	public JobLock() {
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public LocalDateTime getLockedAt() {
		return lockedAt;
	}

	public void setLockedAt(LocalDateTime lockedAt) {
		this.lockedAt = lockedAt;
	}

	public LocalDateTime getExpiryAt() {
		return expiryAt;
	}

	public void setExpiryAt(LocalDateTime expiryAt) {
		this.expiryAt = expiryAt;
	}
}
