package org.forpdi.core.version;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;


/**
 * 
 * 
 */
@Entity(name = VersionHistory.TABLE)
@Table(name = VersionHistory.TABLE)
public class VersionHistory extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_version_history";
	private static final long serialVersionUID = 1L;
	
	@Column(nullable=false, length=255)
	private String numberVersion;
	
	@Temporal(TemporalType.DATE)
	@Column(nullable=false)
	private Date releaseDate = new Date();
	
	@Column(nullable=true, length=4000)
	private String infoFor;
	
	@Column(nullable=true, length=4000)
	private String infoPdi;
	
	@Column(nullable=true, length=4000)
	private String infoRisco;
	
	public String getnumberVersion() {
		return numberVersion;
	}

	public void setnumberVersion(String numberVersion) {
		this.numberVersion = numberVersion;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getInfoFor() {
		return infoFor;			
	}

	public void setInfoFor(String infoFor) {
		this.infoFor = infoFor;
	}
	
	public String getInfoPdi() {
		return infoPdi;			
	}

	public void setInfoPdi(String infoPdi) {
		this.infoPdi = infoPdi;
	}
	
	public String getInfoRisco() {
		return infoRisco;			
	}

	public void setInfoRisco(String infoRisco) {
		this.infoRisco = infoRisco;
	}
}
