package org.forpdi.planning.fields.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;


@Entity(name = ScheduleStructure.TABLE)
@Table(name = ScheduleStructure.TABLE)
public class ScheduleStructure extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_schedule_structure";
	private static final long serialVersionUID = 1L;

	@Column(nullable = false, length=255)
	private String label;
	
	@Column(nullable = false, length=255)
	private String type;
	
	@SkipSerialization
	@ManyToOne(targetEntity=Schedule.class, optional=false, fetch=FetchType.EAGER)
	private Schedule schedule;

	@Transient private Long exportScheduleId;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
	
	public Long getExportScheduleId() {
		return exportScheduleId;
	}

	public void setExportScheduleId(Long exportScheduleId) {
		this.exportScheduleId = exportScheduleId;
	}
	
}
