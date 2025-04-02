package org.forrisco.risk.links;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forrisco.core.process.ProcessObjective;
import org.forrisco.risk.Risk;

/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = RiskProcessObjective.TABLE)
@Table(name = RiskProcessObjective.TABLE)

public class RiskProcessObjective extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_risk_process_objective";
	private static final long serialVersionUID = 1L;

	@SkipSerialization
	@JoinColumn(name="risk_id") 
	@ManyToOne(targetEntity=Risk.class, fetch=FetchType.EAGER)
	private Risk risk;
	
	@JoinColumn(name="objective_id") 
	@ManyToOne(targetEntity=ProcessObjective.class, fetch=FetchType.EAGER)
	private ProcessObjective processObjective;
	
	@Column(nullable=false, length=1000)
	private String linkFPDI;
	
	@Transient
	private Long processId;

	public Risk getRisk() {
		return risk;
	}

	public void setRisk(Risk risk) {
		this.risk = risk;
	}

	public ProcessObjective getProcessObjective() {
		return processObjective;
	}

	public void setProcessObjective(ProcessObjective processObjective) {
		this.processObjective = processObjective;
	}
	
	public String getLinkFPDI() {
		return linkFPDI;
	}

	public void setLinkFPDI(String linkFPDI) {
		this.linkFPDI = linkFPDI;
	}
	
	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}
	
}