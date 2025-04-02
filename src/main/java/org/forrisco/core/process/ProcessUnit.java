package org.forrisco.core.process;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forrisco.core.unit.Unit;

/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = ProcessUnit.TABLE)
@Table(name = ProcessUnit.TABLE)

public class ProcessUnit extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_process_unit";
	private static final long serialVersionUID = 1L;

	@SkipSerialization
	@JoinColumn(name="process_id")
	@ManyToOne(targetEntity=Process.class,  fetch=FetchType.EAGER)
	private Process process;
	
	@JoinColumn(name="unit_id") 
	@ManyToOne(targetEntity=Unit.class,  fetch=FetchType.EAGER)
	private Unit unit;
	
	public void setProcess(Process process) {
		this.process = process;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Process getProcess() {
		return process;
	}

	public Unit getUnit() {
		return unit;
	}
	
}
