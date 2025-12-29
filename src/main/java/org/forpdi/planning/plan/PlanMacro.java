package org.forpdi.planning.plan;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.company.Company;

/**
 * @author Renato R. R. de Oliveira
 * 
 */
@Entity(name = PlanMacro.TABLE)
@Table(
	name = PlanMacro.TABLE
)
public class PlanMacro extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_plan_macro";
	private static final long serialVersionUID = 1L;

	@Column(nullable = false, length=255)
	private String name;

	@Column(nullable = true, length=10000)
	private String description;
	
	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date begin;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date end;

	@SkipSerialization
	@ManyToOne(targetEntity=Company.class, optional=false, fetch=FetchType.EAGER)
	private Company company;

	private boolean archived = false;

	@Transient
	private List<PlanMacroField> fields;
	
	@Column(nullable=false)
	private boolean documented = true;
	
	@Transient
	private boolean haveSons;
	
	public boolean isHaveSons() {
		return haveSons;
	}

	public void setHaveSons(boolean haveSons) {
		this.haveSons = haveSons;
	}

	public boolean isDocumented() {
		return documented;
	}

	public void setDocumented(boolean documented) {
		this.documented = documented;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public List<PlanMacroField> getFields() {
		return fields;
	}

	public void setFields(List<PlanMacroField> fields) {
		this.fields = fields;
	}
	
}