package org.forpdi.dashboard.goalsinfo;

import java.io.Serializable;
import java.util.Optional;

public class GoalsInfoOverview implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long inDay;
	private Long late;
	private Long belowMininum;
	private Long belowExpected;
	private Long reached;
	private Long aboveExpected;
	private Long notStarted;
	private Long finished;
	private Long closeToMaturity;
	private Long total;

	public GoalsInfoOverview(Long inDay, Long late, Long belowMininum, Long belowExpected, Long reached,
			Long aboveExpected, Long notStarted, Long finished, Long closeToMaturity, Long total) {
		this.inDay = inDay;
		this.late = late;
		this.belowMininum = belowMininum;
		this.belowExpected = belowExpected;
		this.reached = reached;
		this.aboveExpected = aboveExpected;
		this.notStarted = notStarted;
		this.finished = finished;
		this.closeToMaturity = closeToMaturity;
		this.total = total;
	}

	public GoalsInfoOverview() {
	}

	public Long getInDay() {
		return zeroIfNull(inDay);
	}

	public void setInDay(Long inDay) {
		this.inDay = inDay;
	}

	public Long getLate() {
		return zeroIfNull(late);
	}

	public void setLate(Long late) {
		this.late = late;
	}

	public Long getBelowMininum() {
		return zeroIfNull(belowMininum);
	}

	public void setBelowMininum(Long belowMininum) {
		this.belowMininum = belowMininum;
	}

	public Long getBelowExpected() {
		return zeroIfNull(belowExpected);
	}

	public void setBelowExpected(Long belowExpected) {
		this.belowExpected = belowExpected;
	}

	public Long getReached() {
		return zeroIfNull(reached);
	}

	public void setReached(Long reached) {
		this.reached = reached;
	}

	public Long getAboveExpected() {
		return zeroIfNull(aboveExpected);
	}

	public void setAboveExpected(Long aboveExpected) {
		this.aboveExpected = aboveExpected;
	}

	public Long getNotStarted() {
		return zeroIfNull(notStarted);
	}

	public void setNotStarted(Long notStarted) {
		this.notStarted = notStarted;
	}

	public Long getFinished() {
		return zeroIfNull(finished);
	}

	public void setFinished(Long finished) {
		this.finished = finished;
	}

	public Long getCloseToMaturity() {
		return zeroIfNull(closeToMaturity);
	}

	public void setCloseToMaturity(Long closeToMaturity) {
		this.closeToMaturity = closeToMaturity;
	}

	public Long getTotal() {
		return zeroIfNull(total);
	}

	public void setTotal(Long total) {
		this.total = total;
	}
	
	public long getReachedOrAboveExpected() {
		return getReached() + getAboveExpected();
	}
	
	private long zeroIfNull(Long value) {
		return Optional.ofNullable(value).orElse(0L);
	}

}
