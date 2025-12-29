package org.forrisco.core.policy;

import java.time.LocalDateTime;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "PolicyHistory")
@Table(name = "frisco_policy_history")
public class PolicyHistory extends SimpleLogicalDeletableEntity {
	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = Policy.class, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "policy_id", nullable = false)
	private Policy policy;

	@ManyToOne(targetEntity = User.class, optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "responsible_user_id", nullable = false)
	private User responsibleUser;

	@Column(nullable = false, name = "changed_field")
	private String changedField;

	@Column(name = "old_value")
	private String oldValue;

	@Column(name = "new_value")
	private String newValue;

	@Column(nullable = false, name = "change_date")
	private LocalDateTime changeDate;

	public Policy getPolicy() {
		return policy;
	}

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	public User getResponsibleUser() {
		return responsibleUser;
	}

	public void setResponsibleUser(User responsibleUser) {
		this.responsibleUser = responsibleUser;
	}

	public String getChangedField() {
		return changedField;
	}

	public void setChangedField(String changedField) {
		this.changedField = changedField;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public LocalDateTime getChangeDate() {
		return changeDate;
	}

	public void setChangeDate(LocalDateTime changeDate) {
		this.changeDate = changeDate;
	}
}
