package org.forpdi.dashboard.goalsinfo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.forpdi.core.common.SimpleIdentifiable;
import org.forpdi.planning.plan.PlanMacro;

@Entity(name = GoalsInfo.TABLE)
@Table(name = GoalsInfo.TABLE)
public class GoalsInfo  implements SimpleIdentifiable {
	public static final String TABLE = "fpdi_goals_info";
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column
	private Long inDay;
	@Column
	private Long late;
	@Column
	private Long belowMininum;
	@Column
	private Long belowExpected;
	@Column
	private Long reached;
	@Column
	private Long aboveExpected;
	@Column
	private Long notStarted;
	@Column
	private Long finished;
	@Column
	private Long closeToMaturity;
	@Column
	private Long total;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	private Date updatedAt;
	@OneToOne(targetEntity=PlanMacro.class, optional=false, fetch=FetchType.EAGER)
	private PlanMacro planMacro;
	
	@Override
	public Long getId() {
		return id;
	}
	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getInDay() {
		return inDay;
	}
	public void setInDay(Long inDay) {
		this.inDay = inDay;
	}
	public Long getLate() {
		return late;
	}
	public void setLate(Long late) {
		this.late = late;
	}
	public Long getBelowMininum() {
		return belowMininum;
	}
	public void setBelowMininum(Long belowMininum) {
		this.belowMininum = belowMininum;
	}
	public Long getBelowExpected() {
		return belowExpected;
	}
	public void setBelowExpected(Long belowExpected) {
		this.belowExpected = belowExpected;
	}
	public Long getReached() {
		return reached;
	}
	public void setReached(Long reached) {
		this.reached = reached;
	}
	public Long getAboveExpected() {
		return aboveExpected;
	}
	public void setAboveExpected(Long aboveExpected) {
		this.aboveExpected = aboveExpected;
	}
	public Long getNotStarted() {
		return notStarted;
	}
	public void setNotStarted(Long notStarted) {
		this.notStarted = notStarted;
	}
	public Long getFinished() {
		return finished;
	}
	public void setFinished(Long finished) {
		this.finished = finished;
	}
	public Long getCloseToMaturity() {
		return closeToMaturity;
	}
	public void setCloseToMaturity(Long closeToMaturity) {
		this.closeToMaturity = closeToMaturity;
	}
	public Long getTotal() {
		return total;
	}
	public void setTotal(Long total) {
		this.total = total;
	}
	public Date getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	public PlanMacro getPlanMacro() {
		return planMacro;
	}
	public void setPlanMacro(PlanMacro plan) {
		this.planMacro = plan;
	}
	
	public void updateInfos(GoalsInfo goalsInfo) {
		inDay = goalsInfo.inDay;
		late = goalsInfo.late;
		belowMininum = goalsInfo.belowMininum;
		belowExpected = goalsInfo.belowExpected;
		reached = goalsInfo.reached;
		aboveExpected = goalsInfo.aboveExpected;
		notStarted = goalsInfo.notStarted;
		finished = goalsInfo.finished;
		closeToMaturity = goalsInfo.closeToMaturity;
		total = goalsInfo.total;
	}
}
