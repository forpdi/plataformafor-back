package org.forrisco.core.process;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.forpdi.core.common.SimpleEntity;

/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = ProcessObjective.TABLE)
@Table(name = ProcessObjective.TABLE)

public class ProcessObjective extends SimpleEntity {
	public static final String TABLE = "frisco_process_objective";
	private static final long serialVersionUID = 1L;

	@JoinColumn(name="process_id") 
	@ManyToOne(targetEntity=Process.class, optional=false, fetch=FetchType.EAGER)
	private Process process;
	
	@Column(nullable=true, length=4000)
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}
	
}