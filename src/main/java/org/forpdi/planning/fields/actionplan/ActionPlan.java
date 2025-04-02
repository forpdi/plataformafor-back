package org.forpdi.planning.fields.actionplan;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;
import org.forpdi.planning.structure.StructureLevelInstance;

@Entity(name = ActionPlan.TABLE)
@Table(name = ActionPlan.TABLE)
public class ActionPlan extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_action_plan";
	private static final long serialVersionUID = 1L;
	
	@Column(nullable=false)
	private Boolean checked;
	
	@Column
	private Boolean notChecked;

	@Column(nullable=true, length=4000)
	private String description;
	
	@SkipSerialization
	@ManyToOne(targetEntity=StructureLevelInstance.class, optional=false, fetch=FetchType.EAGER)
	private StructureLevelInstance levelInstance;
	
	@ManyToOne
	private User user;
	
	@Column(nullable = false, length=4000)
	private String responsible;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date begin;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date end;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Date creation = new Date();
	
	@ManyToOne(targetEntity=StructureLevelInstance.class, optional=true, fetch=FetchType.EAGER)
	private StructureLevelInstance linkedGoal;
	
	@Transient
	private Long exportStructureLevelInstanceId;

	@Transient
	private String exportResponsibleMail;
	
	public Long getExportStructureLevelInstanceId() {
		return exportStructureLevelInstanceId;
	}

	public void setExportStructureLevelInstanceId(Long exportStructureLevelInstanceId) {
		this.exportStructureLevelInstanceId = exportStructureLevelInstanceId;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public Boolean isNotChecked() {
		return notChecked;
	}

	public void setNotChecked(Boolean notChecked) {
		this.notChecked = notChecked;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public StructureLevelInstance getLevelInstance() {
		return levelInstance;
	}

	public void setLevelInstance(StructureLevelInstance levelInstance) {
		this.levelInstance = levelInstance;
	}

	public StructureLevelInstance getLinkedGoal() {
		return linkedGoal;
	}

	public void setLinkedGoal(StructureLevelInstance linkedGoal) {
		this.linkedGoal = linkedGoal;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getResponsible() {
		return responsible;
	}

	public void setResponsible(String responsible) {
		this.responsible = responsible;
	}

	public String getUserResponsibleName() {
		return user != null ? user.getName() : responsible;
	}
	
	public Date getBegin() {
		return begin;
	}

	public void setBegin(Date begin) {
		this.begin = begin;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public String getExportResponsibleMail() {
		return exportResponsibleMail;
	}

	public void setExportResponsibleMail(String exportResponsibleMail) {
		this.exportResponsibleMail = exportResponsibleMail;
	}
	
	public static class ActionPlanPermissionInfo {
		private final boolean hasAccessLevel;
		private final boolean isResponsible;
		private final boolean isResponsibleForIndicator;
		
		public ActionPlanPermissionInfo(boolean hasAccessLevel, boolean isResponsible,
				boolean isResponsibleForIndicator) {
			this.hasAccessLevel = hasAccessLevel;
			this.isResponsible = isResponsible;
			this.isResponsibleForIndicator = isResponsibleForIndicator;
		}

		public boolean hasAccessLevel() {
			return hasAccessLevel;
		}

		public boolean isResponsible() {
			return isResponsible;
		}

		public boolean isResponsibleForIndicator() {
			return isResponsibleForIndicator;
		}
		
		public boolean hasPermission() {
			return hasAccessLevel || isResponsible || isResponsibleForIndicator;
		}
		
		public boolean hasPermissionToUpdateResponsible() {
			return hasAccessLevel || isResponsibleForIndicator;
		}
	}
	
}