package org.forrisco.risk.links;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forrisco.core.process.Process;
import org.forrisco.risk.Risk;

/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = RiskProcess.TABLE)
@Table(name = RiskProcess.TABLE)
@Deprecated
public class RiskProcess extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_risk_process";
	private static final long serialVersionUID = 1L;

	@SkipSerialization
	@JoinColumn(name="risk_id") 
	@ManyToOne(targetEntity=Risk.class, optional=false, fetch=FetchType.EAGER)
	private Risk risk;

	//@SkipSerialization
	@JoinColumn(name="process_id") 
	@ManyToOne(targetEntity=Process.class, optional=false, fetch=FetchType.EAGER)
	private Process process;
	
	// Esse link é para um processo do próprio forrisco
	@Column(nullable=false, length=1000)
	private String linkFPDI;
	
	public RiskProcess() {}
	
	public RiskProcess(RiskProcess riskprocess) {
		this.linkFPDI =riskprocess.getLinkFPDI();
	}


	public Risk getRisk() {
		return risk;
	}

	public void setRisk(Risk risk) {
		this.risk = risk;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public String getLinkFPDI() {
		return linkFPDI;
	}

	public void setLinkFPDI(String linkFPDI) {
		this.linkFPDI = linkFPDI;
	}

	
}