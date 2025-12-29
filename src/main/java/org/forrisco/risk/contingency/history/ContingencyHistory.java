package org.forrisco.risk.contingency.history;

import java.time.LocalDateTime;
import jakarta.persistence.*;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;
import org.forrisco.risk.contingency.Contingency;

@Entity(name = "ContingencyHistory")
@Table(name = "frisco_contingency_history")
public class ContingencyHistory extends SimpleLogicalDeletableEntity {
    private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = Contingency.class, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "contingency_id", nullable = false)
    private Contingency contingency;

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

    public Contingency getContingency() { return contingency; }
    public void setContingency(Contingency contingency) { this.contingency = contingency; }
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
