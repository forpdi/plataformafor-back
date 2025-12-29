package org.forrisco.core.item.history;

import java.util.Date;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;

import com.google.gson.annotations.Expose;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "frisco_item_history")
public class ItemHistory extends SimpleLogicalDeletableEntity {
    private static final long serialVersionUID = 1L;

    @Column(name = "entity_type", nullable = false, length = 40)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "changed_field", nullable = false, length = 400)
    private String changedField;

    @Column(name = "old_value", columnDefinition = "longtext")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "longtext")
    private String newValue;

    @Expose(serialize = false, deserialize = false)
    @SkipSerialization
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
    @JoinColumn(name = "responsible_user_id", nullable = false)
    private User responsibleUser;

    @Column(name = "responsible_user_name", length = 400)
    private String responsibleUserName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "change_date", nullable = false)
    private Date changeDate;

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getChangedField() { return changedField; }
    public void setChangedField(String changedField) { this.changedField = changedField; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public User getResponsibleUser() { return responsibleUser; }
    public void setResponsibleUser(User responsibleUser) { this.responsibleUser = responsibleUser; }

    public String getResponsibleUserName() { return responsibleUserName; }
    public void setResponsibleUserName(String responsibleUserName) { this.responsibleUserName = responsibleUserName; }

    public Date getChangeDate() { return changeDate; }
    public void setChangeDate(Date changeDate) { this.changeDate = changeDate; }
}
