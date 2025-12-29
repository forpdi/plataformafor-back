package org.forpdi.planning.fields.schedule;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


@Entity(name = ScheduleInstance.TABLE)
@Table(name = ScheduleInstance.TABLE)
public class ScheduleInstance extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_schedule_instance";
	private static final long serialVersionUID = 1L;
	
	@Column(nullable = false)
	private Long number;
	
	@Column(nullable = false, length=4000)
	private String description;
	
	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date begin;
	
	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date end;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Date creation = new Date();
	
	@Column(nullable = true, length=255)
	private String periodicity;
	
	@SkipSerialization
	@ManyToOne(targetEntity=Schedule.class, optional=false, fetch=FetchType.EAGER)
	private Schedule schedule;
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="scheduleInstance", targetEntity=ScheduleValues.class)
	@Cascade(CascadeType.ALL)
	private List<ScheduleValues> scheduleValues;
	
	@Transient private Long exportScheduleId;

	public Long getNumber() {
		return number;
	}

	public void setNumber(Long number) {
		this.number = number;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getPeriodicity() {
		return periodicity;
	}

	public void setPeriodicity(String periodicity) {
		this.periodicity = periodicity;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public List<ScheduleValues> getScheduleValues() {
		return scheduleValues;
	}

	public void setScheduleValues(List<ScheduleValues> scheduleValues) {
		this.scheduleValues = scheduleValues;
	}
	
	public Long getExportScheduleId() {
		return exportScheduleId;
	}

	public void setExportScheduleId(Long exportScheduleId) {
		this.exportScheduleId = exportScheduleId;
	}
	
}
