package org.forrisco.risk.links;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forrisco.risk.Risk;

/**
 * @author Gabriel Oliveira
 * 
 */
@Entity(name = RiskIndicator.TABLE)
@Table(name = RiskIndicator.TABLE)

public class RiskIndicator extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_risk_indicators";
	private static final long serialVersionUID = 1L;

	@SkipSerialization
	@JoinColumn(name="risk_id") 
	@ManyToOne(targetEntity=Risk.class, optional=false, fetch=FetchType.EAGER)
	private Risk risk;

	//@SkipSerialization
	@JoinColumn(name="structure_id") 
	@ManyToOne(targetEntity=StructureLevelInstance.class, optional=false, fetch=FetchType.EAGER)
	private StructureLevelInstance structure;
	
	@Column(nullable=false, length=1000)
	private String linkFPDI;
	
	@Column(nullable=false, length=1000)
	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Risk getRisk() {
		return risk;
	}

	public void setRisk(Risk risk) {
		this.risk = risk;
	}

	public String getLinkFPDI() {
		return linkFPDI;
	}

	public void setLinkFPDI(String linkFPDI) {
		this.linkFPDI = linkFPDI;
	}


	public StructureLevelInstance getStructure() {
		return structure;
	}

	public void setStructure(StructureLevelInstance indicator) {
		this.structure = indicator;
	}
	
	
}