package org.forrisco.core.plan;

import java.time.LocalDateTime;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "frisco_plan_risk_history")
public class PlanRiskHistory extends SimpleLogicalDeletableEntity {
	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.LAZY, optional = false, targetEntity = PlanRisk.class)
	@JoinColumn(name = "plan_risk_id", nullable = false)
	private PlanRisk planRisk;

	@ManyToOne(fetch = FetchType.EAGER, optional = false, targetEntity = User.class)
	@JoinColumn(name = "responsible_user_id", nullable = false)
	private User responsibleUser;

	@Column(name = "changed_field", nullable = false, length = 255)
	private String changedField;

	@Column(name = "old_value")
	private String oldValue;

	@Column(name = "new_value")
	private String newValue;

	@Column(name = "change_date", nullable = false)
	private LocalDateTime changeDate = LocalDateTime.now();

	// getters/setters
	public PlanRisk getPlanRisk() { return planRisk; }
	public void setPlanRisk(PlanRisk planRisk) { this.planRisk = planRisk; }

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
