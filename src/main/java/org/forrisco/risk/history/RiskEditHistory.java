package org.forrisco.risk.history;

import java.time.LocalDateTime;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;
import org.forrisco.risk.Risk;
import jakarta.persistence.*;

@Entity(name = "RiskEditHistory")
@Table(name = "frisco_risk_edit_history")
public class RiskEditHistory extends SimpleLogicalDeletableEntity {
    private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = Risk.class, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_id", nullable = false)
    private Risk risk;

    @ManyToOne(targetEntity = User.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_user_id", nullable = false)
    private User responsibleUser;

    @Column(name = "changed_field", nullable = false)
    private String changedField;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    // Getters/Setters
    public Risk getRisk() { return risk; }
    public void setRisk(Risk risk) { this.risk = risk; }
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
