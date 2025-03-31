package org.forrisco.risk;

import java.util.Date;
import java.util.List;

import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.utils.JsonUtil;

public class RiskFilterParams {
	private List<Long> unitIds;
	private String term;
	private List<Long> processesId;
	private List<Long> linkedPdiIds;
	private String nameOrCode;
	private String type;
	private List<String> typologies;
	private List<Integer> responses;
	private List<Integer> levels;
	private Date startCreation;
	private Date endCreation;
	private Boolean archived;

	public static RiskFilterParams fromJson(String json) {
		return !GeneralUtils.isEmpty(json)
				? JsonUtil.fromJson(json, RiskFilterParams.class)
				: new RiskFilterParams();
	}
	
	public List<Long> getUnitIds() {
		return unitIds;
	}
	public void setUnitIds(List<Long> unitIds) {
		this.unitIds = unitIds;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public List<Long> getProcessesId() {
		return processesId;
	}
	public void setProcessesId(List<Long> processesId) {
		this.processesId = processesId;
	}
	public List<Long> getLinkedPdiIds() {
		return linkedPdiIds;
	}
	public void setLinkedPdiIds(List<Long> linkedPdiIds) {
		this.linkedPdiIds = linkedPdiIds;
	}
	public String getNameOrCode() {
		return nameOrCode;
	}
	public void setNameOrCode(String nameOrCode) {
		this.nameOrCode = nameOrCode;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getTypologies() {
		return typologies;
	}
	public void setTypologies(List<String> typologies) {
		this.typologies = typologies;
	}
	public List<Integer> getResponses() {
		return responses;
	}
	public void setResponses(List<Integer> responses) {
		this.responses = responses;
	}
	public List<Integer> getLevels() {
		return levels;
	}
	public void setLevels(List<Integer> levels) {
		this.levels = levels;
	}
	public Date getStartCreation() {
		return startCreation;
	}
	public void setStartCreation(Date startCreation) {
		this.startCreation = startCreation;
	}
	public Date getEndCreation() {
		return endCreation;
	}
	public void setEndCreation(Date endCreation) {
		this.endCreation = endCreation;
	}
	public Boolean getArchived() {
		return archived;
	}
	public void setArchived(Boolean archived) {
		this.archived = archived;
	}
	
	public boolean filteringByUnits() {
		return !GeneralUtils.isEmpty(unitIds);
	}
	public boolean filteringByTerm() {
		return !GeneralUtils.isEmpty(term);
	}
	public boolean filteringByProcesses() {
		return !GeneralUtils.isEmpty(processesId);
	}
	public boolean filteringByLinkedPdis() {
		return !GeneralUtils.isEmpty(linkedPdiIds);
	}
	public boolean filteringByNameOrCode() {
		return !GeneralUtils.isEmpty(nameOrCode);
	}
	public boolean filteringByType() {
		return !GeneralUtils.isEmpty(type);
	}
	public boolean filteringByTypologies() {
		return !GeneralUtils.isEmpty(typologies);
	}
	public boolean filteringByNoneResponse() {
		return filteringByResponses() && responses.contains(-1);
	}
	public boolean filteringByResponses() {
		return !GeneralUtils.isEmpty(responses);
	}
	public boolean filteringByNoneLevels() {
		return filteringByLevels() && levels.contains(-1);
	}
	public boolean filteringByLevels() {
		return !GeneralUtils.isEmpty(levels);
	}
	public boolean filteringByStartCreation() {
		return startCreation != null;
	}
	public boolean filteringByEndCreation() {
		return endCreation != null;
	}
	public boolean filteringByArchived() {
		return archived != null;
	}
}
