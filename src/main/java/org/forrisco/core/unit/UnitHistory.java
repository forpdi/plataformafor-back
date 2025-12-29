package org.forrisco.core.unit;

import java.time.LocalDateTime;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "UnitHistory")
@Table(name = "frisco_unit_history")
public class UnitHistory extends SimpleLogicalDeletableEntity {
    private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = Unit.class, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToOne(targetEntity = User.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_user_id", nullable = false) 
    private User responsibleUser;

    @Column(nullable = false, name="changed_field")
    private String changedField;

    @Column(nullable = true, name="old_value")
    private String oldValue;

    @Column(nullable = true, name="new_value")
    private String newValue;

    @Column(nullable = false, name="change_date")
    private LocalDateTime changeDate;


    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
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