package org.forrisco.core.policy;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.company.Company;
import org.forpdi.core.utils.JsonUtil;
import org.forrisco.core.policy.PIDescriptions.PIDescriptionsWrapper;

/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = Policy.TABLE)
@Table(name = Policy.TABLE)

public class Policy extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_policy";
	private static final long serialVersionUID = 1L;

	@SkipSerialization
	@ManyToOne(targetEntity=Company.class, optional=false, fetch=FetchType.EAGER)
	private Company company;
	
	@Column(nullable=false, length=400)
	private String name;

	@Column(nullable = true, columnDefinition="longtext")
	private String description;
	
	@Temporal(TemporalType.DATE)
	@Column(nullable = true, name="validity_begin")
	private Date validityBegin;
	
	@Temporal(TemporalType.DATE)
	@Column(nullable = true, name="validity_end")
	private Date validityEnd;

	@Column(nullable=false)
	private int nline;
	
	@Column(nullable=false)
	private int ncolumn;

	@Column(nullable=false, length=4000)
	private String probability;

	@Column(nullable=false, length=4000)
	private String impact;
	
	@Column(nullable=false, length=4000)
	private String matrix;
	
	@Column(nullable = true, columnDefinition="text")
	private String PIDescriptions;

	private boolean archived = false;
	
	@Transient
	private String risk_level[][];
	
	@Transient
	private boolean hasLinkedPlans;
	

	public boolean getHasLinkedPlans() {
		return hasLinkedPlans;
	}
	
	public void setHasLinkedPlans(boolean hasLinkedPlans) {
		this.hasLinkedPlans = hasLinkedPlans;
	}
	
	public String[][] getRisk_level() {
		return risk_level;
	}
	
	public void setRisk_level(String[][] risk_level) {
		this.risk_level = risk_level;
	}
	
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
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

	public Date getValidityBegin() {
		return validityBegin;
	}

	public void setValidityBegin(Date validityBegin) {
		this.validityBegin = validityBegin;
	}

	public Date getValidityEnd() {
		return validityEnd;
	}

	public void setValidityEnd(Date validityEnd) {
		this.validityEnd = validityEnd;
	}

	public int getNline() {
		return nline;
	}

	public void setNline(int nline) {
		this.nline = nline;
	}

	public int getNcolumn() {
		return ncolumn;
	}

	public void setNcolumn(int ncolumn) {
		this.ncolumn = ncolumn;
	}

	public String getProbability() {
		return probability;
	}

	public void setProbability(String probability) {
		this.probability = probability;
	}

	public String getImpact() {
		return impact;
	}

	public void setImpact(String impact) {
		this.impact = impact;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}
	
	public String getMatrix() {
		return matrix;
	}

	public void setMatrix(String matrix) {
		this.matrix = matrix;
	}

	public String getPIDescriptions() {
		return PIDescriptions;
	}

	public void setPIDescriptions(String pIDescriptions) {
		PIDescriptions = pIDescriptions;
	}
	
	public PIDescriptions getPIDescriptionAsObject() {
		PIDescriptionsWrapper wrapper = JsonUtil.fromJson(this.PIDescriptions, PIDescriptionsWrapper.class);
		return wrapper.PIDescriptions;
	}
	
	public String[][] getMatrixLevels() {
		String[][] matrixLevels = new String[nline + 1][];
		for (int i = 0; i < matrixLevels.length; i++) {
			matrixLevels[i] = new String[ncolumn + 1];
		}
		try {
			for (String str : matrix.split(";")) {
				Matcher matcher = Pattern.compile("\\[.*\\]").matcher(str);
				if (!matcher.find()) {
					throw new RuntimeException();
				}
				String rowAndColumnBetweenBrackets = matcher.group();
				String rowAndColumn = rowAndColumnBetweenBrackets.substring(1, rowAndColumnBetweenBrackets.length() - 1);
				String[] rowAndColumnSplit = rowAndColumn.split(",", 2);
				int row = Integer.parseInt(rowAndColumnSplit[0]);
				int column = Integer.parseInt(rowAndColumnSplit[1]);
				String level = str.replace(rowAndColumnBetweenBrackets, "");
				matrixLevels[row][column] = level;
			}
		} catch (RuntimeException e) {
			throw new IllegalStateException("Invalid risk matrix format", e);
		}

		return matrixLevels;
	}
}