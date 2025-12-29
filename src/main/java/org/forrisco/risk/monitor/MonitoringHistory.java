package org.forrisco.risk.monitor;

import java.time.LocalDateTime;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "MonitoringHistory")
@Table(name = "frisco_monitoring_history")
@AttributeOverride(name = "deleted", column = @Column(name = "deleted", nullable = false))
public class MonitoringHistory extends SimpleLogicalDeletableEntity {
	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = Monitor.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "monitoring_id", nullable = false)
	private Monitor monitoring;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "responsible_user_id", nullable = false)
	private User responsibleUser;

	@Column(name = "changed_field", nullable = false, length = 255)
	private String changedField;

	@Column(name = "old_value", columnDefinition = "LONGTEXT")
	private String oldValue;

	@Column(name = "new_value", columnDefinition = "LONGTEXT")
	private String newValue;

	@Column(name = "change_date", nullable = false)
	private LocalDateTime changeDate;

	/* getters/setters */
	public Monitor getMonitoring() { return monitoring; }
	public void setMonitoring(Monitor monitoring) { this.monitoring = monitoring; }

	public User getResponsibleUser() { return responsibleUser; }
	public void setResponsibleUser(User responsibleUser) { this.responsibleUser = responsibleUser; }

	public String getChangedField() { return changedField; }
	public void setChangedField(String changedField) { this.changedField = changedField; }

	public String getOldValue() { return oldValue; }
	public void setOldValue(String oldValue) { this.oldValue = oldValue; }

	public String getNewValue() { return newValue; }
	public void setNewValue(String newValue) { this.newValue = newValue; }

	public LocalDateTime getChangeDate() { return changeDate; }
	public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }
}
