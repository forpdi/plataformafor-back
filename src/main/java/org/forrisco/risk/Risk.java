package org.forrisco.risk;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.core.user.User;
import org.forpdi.core.utils.Util;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.links.RiskActivity;
import org.forrisco.risk.links.RiskAxis;
import org.forrisco.risk.links.RiskGoal;
import org.forrisco.risk.links.RiskIndicator;
import org.forrisco.risk.links.RiskProcessObjective;
import org.forrisco.risk.links.RiskStrategy;

/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = Risk.TABLE)
@Table(name = Risk.TABLE)
public class Risk extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_risk";
	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = User.class, optional = false, fetch = FetchType.EAGER)
	private User user;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
	private User manager;

	@ManyToOne(targetEntity = Unit.class, optional = false, fetch = FetchType.EAGER)
	private Unit unit;

	@ManyToOne(targetEntity = RiskLevel.class, optional = false, fetch = FetchType.EAGER)
	private RiskLevel riskLevel;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(nullable = false, length = 255)
	private String code;

	@Column(nullable = false, length = 4000)
	private String reason;

	@Column(nullable = false, length = 4000)
	private String result;

	@Column(nullable = false, length = 400)
	private String probability;

	@Column(nullable = false, length = 400)
	private String impact;

	@Column(nullable = false, length = 400)
	private String periodicity;

	@Column(nullable = false, length = 400)
	private String tipology;

	@Column(nullable = false, length = 255)
	private String otherTipologies;

	@Column(nullable = false, length = 4010)
	private String type;

	@Column(nullable = false)
	private boolean risk_pdi;
	
	@Column(nullable = false)
	private boolean risk_pdi_axis;
	
	@Column(nullable = false)
	private boolean risk_obj_process;

	@Column(nullable = false)
	private boolean risk_act_process;

	@Column(nullable = false)
	private Date begin;

	@Column(nullable = true)
	private String linkFPDI;

	@Column(nullable = true)
	private Integer response;

	@Column(nullable = true)
	private Integer level;

	@Column(nullable = false)
	private boolean archived = false;

	@Transient
	private PaginatedList<RiskActivity> activities;

	@Transient
	private PaginatedList<RiskStrategy> strategies;
	
	@Transient
	private PaginatedList<RiskAxis> axes;
	
	@Transient
	private PaginatedList<RiskIndicator> indicators;
	
	@Transient
	private PaginatedList<RiskGoal> goals;

	@Transient
	private List<RiskProcessObjective> processObjectives;
	
	@Transient
	private int monitoringState;

	@Transient
	private List<Unit> sharedUnits;
	
	public Risk() {
		// Construtor vazio para inicialização de objetos da classe e necessário para operações do JPA
	}

	public static Risk from(Risk risk) {
		Risk newRisk = new Risk();
		newRisk.begin = risk.getBegin();
		newRisk.code = risk.getCode();
		newRisk.impact = risk.getImpact();
		newRisk.linkFPDI = risk.getLinkFPDI();
		newRisk.name = risk.getName();
		newRisk.periodicity = risk.getPeriodicity();
		newRisk.probability = risk.getProbability();
		newRisk.reason = risk.getReason();
		newRisk.result = risk.getResult();
		newRisk.riskLevel = risk.getRiskLevel();
		newRisk.risk_act_process = risk.isRisk_act_process();
		newRisk.risk_obj_process = risk.isRisk_obj_process();
		newRisk.risk_pdi = risk.isRisk_pdi();
		newRisk.risk_pdi_axis = risk.isRisk_pdi_axis();
		newRisk.tipology = risk.getTipology();
		newRisk.type = risk.getType();
		newRisk.user = risk.getUser();
		newRisk.manager = risk.getManager();
		newRisk.response = risk.getResponse();
		newRisk.level = risk.getLevel();
		return newRisk;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getManager() {
		return manager;
	}

	public void setManager(User manager) {
		this.manager = manager;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getReason() {
		return reason;
	}
	
	public String getReasonWithoutTags() {
		return Util.htmlToString(reason);
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getResult() {
		return result;
	}
	
	public String getResultWithoutTags() {
		return Util.htmlToString(result);
	}

	public void setResult(String result) {
		this.result = result;
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

	public String getPeriodicity() {
		return periodicity;
	}

	public void setPeriodicity(String periodicity) {
		this.periodicity = periodicity;
	}

	public String getTipology() {
		return tipology;
	}

	public void setTipology(String tipology) {
		this.tipology = tipology;
	}

	public String getOtherTipologies() {
		return otherTipologies;
	}

	public void setOtherTipologies(String otherTipologies) {
		this.otherTipologies = otherTipologies;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRisk_pdi() {
		return risk_pdi;
	}

	public void setRisk_pdi(boolean risk_pdi) {
		this.risk_pdi = risk_pdi;
	}
	
	public boolean isRisk_pdi_axis() {
		return risk_pdi_axis;
	}

	public void setRisk_pdi_axis(boolean risk_pdi_axis) {
		this.risk_pdi_axis = risk_pdi_axis;
	}
	
	public boolean isRisk_obj_process() {
		return risk_obj_process;
	}

	public void setRisk_obj_process(boolean risk_obj_process) {
		this.risk_obj_process = risk_obj_process;
	}

	public boolean isRisk_act_process() {
		return risk_act_process;
	}

	public void setRisk_act_process(boolean risk_act_process) {
		this.risk_act_process = risk_act_process;
	}

	public Date getBegin() {
		return begin;
	}

	public void setBegin(Date begin) {
		this.begin = begin;
	}

	public RiskLevel getRiskLevel() {
		return riskLevel;
	}

	public void setRiskLevel(RiskLevel riskLevel) {
		this.riskLevel = riskLevel;
	}

	public String getLinkFPDI() {
		return linkFPDI;
	}

	public void setLinkFPDI(String linkFPDI) {
		this.linkFPDI = linkFPDI;
	}

	public Integer getResponse() {
		return response;
	}

	public void setResponse(Integer response) {
		this.response = response;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public PaginatedList<RiskActivity> getActivities() {
		return activities;
	}

	public void setActivities(PaginatedList<RiskActivity> activities) {
		this.activities = activities;
	}

	public PaginatedList<RiskStrategy> getStrategies() {
		return strategies;
	}

	public void setStrategies(PaginatedList<RiskStrategy> strategies) {
		this.strategies = strategies;
	}
	
	public PaginatedList<RiskAxis> getAxes() {
		return axes;
	}

	public void setAxes(PaginatedList<RiskAxis> axes) {
		this.axes = axes;
	}
	
	public PaginatedList<RiskIndicator> getIndicators() {
		return indicators;
	}

	public void setIndicators(PaginatedList<RiskIndicator> indicators) {
		this.indicators = indicators;
	}
	
	public PaginatedList<RiskGoal> getGoals() {
		return goals;
	}

	public void setGoals(PaginatedList<RiskGoal> goals) {
		this.goals = goals;
	}
 
	public int getMonitoringState() {
		return monitoringState;
	}

	public void setMonitoringState(int monitoringState) {
		this.monitoringState = monitoringState;
	}

	public List<Unit> getSharedUnits() {
		return sharedUnits;
	}

	public void setSharedUnits(List<Unit> sharedUnits) {
		this.sharedUnits = sharedUnits;
	}

	public List<String> getStrategiesDescriptions() {
		if (strategies == null) {
			return Collections.emptyList();
		}
		return strategies.getList()
				.stream()
				.map(strategy -> strategy.getStructure().getName())
				.collect(Collectors.toList());
	}
 
	public List<String> getActivitiesDescriptions() {
		if (activities == null) {
			return Collections.emptyList();
		}
		return activities.getList()
				.stream()
				.map(activity -> activity.getName() + " - " + activity.getProcess().getName())
				.collect(Collectors.toList());
	}

	public String getRiskResponseLabel() {
    Integer response = this.response; 
    if (response != null) {
        RiskResponse riskResponse = RiskResponse.GetById(response);
        return riskResponse.getLabel();
    } else {
        return "";
    }
	}
	
	public List<RiskProcessObjective> getProcessObjectives() {
		return processObjectives;
	}

	public void setProcessObjectives(List<RiskProcessObjective> processObjectives) {
		this.processObjectives = processObjectives;
	}

	public String getRiskLevelLabel() {
	    Integer level = this.level;
	    if (level != null) {
	        RiskOrganizationalLevel organizationalLevel = RiskOrganizationalLevel.getById(level);
	        return organizationalLevel.getLabel();
	    }
	    return "";
	}

	public String getManagerNameIfExists() {
		return manager != null ? manager.getName() : "";
	}
	
	public String getTipologiesFomatted() {
		return tipology.replace(";", ", ");
	}
	
	public String getFormattedTipologies() {
		return tipology.replace(";", "; ");
	}
}