package org.forpdi.planning.attribute;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.planning.structure.StructureLevelInstance;


@Entity(name = AggregateIndicator.TABLE)
@Table(name = AggregateIndicator.TABLE, uniqueConstraints = @UniqueConstraint(columnNames = { "id" }), indexes = {
		@Index(columnList = "aggregate_id,indicator_id")})
public class AggregateIndicator extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_aggregate_indicator";
	private static final long serialVersionUID = 1L;

	@SkipSerialization
	@ManyToOne(targetEntity = StructureLevelInstance.class, optional = false, fetch = FetchType.EAGER)
	private StructureLevelInstance indicator;

	@ManyToOne(targetEntity = StructureLevelInstance.class, optional = false, fetch = FetchType.EAGER)
	private StructureLevelInstance aggregate;

	@Column(nullable = false)
	private Double percentage;
	
	@Transient private Long exportAggregateId;
	
	@Transient private Long exportIndicatorId;

	public StructureLevelInstance getIndicator() {
		return indicator;
	}

	public void setIndicator(StructureLevelInstance indicator) {
		this.indicator = indicator;
	}

	public StructureLevelInstance getAggregate() {
		return aggregate;
	}

	public void setAggregate(StructureLevelInstance aggregate) {
		this.aggregate = aggregate;
	}

	public Double getPercentage() {
		return percentage;
	}

	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}

	@Override
	public String toString() {
		return "AggregateIndicator [indicator=" + indicator.toString() + ", aggregate=" + aggregate.toString() + ", percentage=" + percentage
				+ "]";
	}
	
	public Long getExportAggregateId() {
		return exportAggregateId;
	}

	public void setExportAggregateId(Long exportAggregateId) {
		this.exportAggregateId = exportAggregateId;
	}

	public Long getExportIndicatorId() {
		return exportIndicatorId;
	}

	public void setExportIndicatorId(Long exportIndicatorId) {
		this.exportIndicatorId = exportIndicatorId;
	}

}