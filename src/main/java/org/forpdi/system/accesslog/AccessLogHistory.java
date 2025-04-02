package org.forpdi.system.accesslog;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = AccessLogHistory.TABLE)
@Table(name = AccessLogHistory.TABLE)
public class AccessLogHistory {
	public static final String TABLE = "fpdi_access_log_history";
	
	@EmbeddedId
	private AccessLogHistoryId id;
	
	@Column(name = "fpdi_access_count", nullable = false)
	private int fpdiAccessCount;
	
	@Column(name = "frisco_access_count", nullable = false)
	private int friscoAccessCount;

	public AccessLogHistory() {
	}

	public AccessLogHistory(Long companyId, LocalDate date, int fpdiAccessCount, int friscoAccessCount) {
		this.id = new AccessLogHistoryId(companyId, date);
		this.fpdiAccessCount = fpdiAccessCount;
		this.friscoAccessCount = friscoAccessCount;
	}

	public Long getCompanyId() {
		return id.companyId;
	}

	public LocalDate getDate() {
		return id.date;
	}

	public int getFpdiAccessCount() {
		return fpdiAccessCount;
	}

	public void setFpdiAccessCount(int fpdiAccessCount) {
		this.fpdiAccessCount = fpdiAccessCount;
	}

	public int getFriscoAccessCount() {
		return friscoAccessCount;
	}

	public void setFriscoAccessCount(int friscoAccessCount) {
		this.friscoAccessCount = friscoAccessCount;
	}
	
	public void incrementFpdiAccess() {
		this.fpdiAccessCount += 1;
	}
	
	public void incrementFriscoAccess() {
		this.friscoAccessCount += 1;
	}
	
	@Embeddable
	public static class AccessLogHistoryId implements Serializable {
		private static final long serialVersionUID = 1L;

		@Column(name = "company_id")
		private Long companyId;
		
		@Column(nullable = false)
		private LocalDate date;

		public AccessLogHistoryId() {
		}

		public AccessLogHistoryId(Long companyId, LocalDate date) {
			this.companyId = companyId;
			this.date = date;
		}
	}
}
