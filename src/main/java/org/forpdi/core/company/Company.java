package org.forpdi.core.company;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.location.County;
import org.forpdi.core.storage.file.Archive;

@Entity(name = Company.TABLE)
@Table(name = Company.TABLE)
public class Company extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_company";
	private static final long serialVersionUID = 1L;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(nullable = false, length = 255)
	private String logo;

	@Column(nullable = true, length = 11000)
	private String description;

	@Column(nullable = true, length = 255)
	private String initials;

	@Column(nullable = true)
	private Integer type;

	private boolean showDashboard = true;

	private boolean showMaturity = true;

	private boolean showBudgetElement = true;

	private boolean enableForrisco = true;
	
	@Column(nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date creation = new Date();

	@ManyToOne(optional = true)
	private Archive logoArchive;
	
	@ManyToOne(optional = false)
	private County county;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isShowDashboard() {
		return showDashboard;
	}

	public void setShowDashboard(boolean showDashboard) {
		this.showDashboard = showDashboard;
	}

	public boolean isShowMaturity() {
		return showMaturity;
	}

	public void setShowMaturity(boolean showMaturity) {
		this.showMaturity = showMaturity;
	}

	public boolean isShowBudgetElement() {
		return showBudgetElement;
	}

	public void setShowBudgetElement(boolean showBudgetElement) {
		this.showBudgetElement = showBudgetElement;
	}

	public String getLocalization() {
		return county.getName() + "/" + county.getUf().getAcronym();
	}

	public boolean isEnableForrisco() {
		return enableForrisco;
	}

	public void setEnableForrisco(boolean enableForrisco) {
		this.enableForrisco = enableForrisco;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Archive getLogoArchive() {
		return logoArchive;
	}

	public void setLogoArchive(Archive logoArchive) {
		this.logoArchive = logoArchive;
	}
	
	public County getCounty() {
		return county;
	}

	public void setCounty(County county) {
		this.county = county;
	}

	public boolean hasLogo() {
		return logoArchive != null;
	}
	
	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Company other = (Company) obj;
		return Objects.equals(id, other.id);
	}
}
