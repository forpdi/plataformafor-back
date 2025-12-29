package org.forpdi.dashboard.goalsinfo;

import java.util.Date;

import org.forpdi.core.common.SimpleIdentifiable;
import org.forpdi.planning.plan.PlanMacro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

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

	    @Transient
    private double inDayPercentage;
    @Transient
    private double latePercentage;
    @Transient
    private double belowMinimumPercentage;
    @Transient
    private double belowExpectedPercentage;
    @Transient
    private double reachedPercentage;
    @Transient
    private double aboveExpectedPercentage;
    @Transient
    private double notStartedPercentage;
    @Transient
    private double finishedPercentage;
    @Transient
    private double closeToMaturityPercentage;

    public double getInDayPercentage() {
        return inDayPercentage;
    }

    public void setInDayPercentage(double inDayPercentage) {
        this.inDayPercentage = inDayPercentage;
    }

    public double getLatePercentage() {
        return latePercentage;
    }

    public void setLatePercentage(double latePercentage) {
        this.latePercentage = latePercentage;
    }

    public double getBelowMinimumPercentage() {
        return belowMinimumPercentage;
    }

    public void setBelowMinimumPercentage(double belowMinimumPercentage) {
        this.belowMinimumPercentage = belowMinimumPercentage;
    }

    public double getBelowExpectedPercentage() {
        return belowExpectedPercentage;
    }

    public void setBelowExpectedPercentage(double belowExpectedPercentage) {
        this.belowExpectedPercentage = belowExpectedPercentage;
    }

    public double getReachedPercentage() {
        return reachedPercentage;
    }

    public void setReachedPercentage(double reachedPercentage) {
        this.reachedPercentage = reachedPercentage;
    }

    public double getAboveExpectedPercentage() {
        return aboveExpectedPercentage;
    }

    public void setAboveExpectedPercentage(double aboveExpectedPercentage) {
        this.aboveExpectedPercentage = aboveExpectedPercentage;
    }

    public double getNotStartedPercentage() {
        return notStartedPercentage;
    }

    public void setNotStartedPercentage(double notStartedPercentage) {
        this.notStartedPercentage = notStartedPercentage;
    }

    public double getFinishedPercentage() {
        return finishedPercentage;
    }

    public void setFinishedPercentage(double finishedPercentage) {
        this.finishedPercentage = finishedPercentage;
    }

    public double getCloseToMaturityPercentage() {
        return closeToMaturityPercentage;
    }

    public void setCloseToMaturityPercentage(double closeToMaturityPercentage) {
        this.closeToMaturityPercentage = closeToMaturityPercentage;
    }
}
	
