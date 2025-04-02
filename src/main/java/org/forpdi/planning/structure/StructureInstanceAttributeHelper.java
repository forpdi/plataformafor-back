package org.forpdi.planning.structure;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.ProjectionList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.dashboard.goalsinfo.AttributeInstanceToGoalsInfo;
import org.forpdi.planning.attribute.AttributeInstance;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class StructureInstanceAttributeHelper {
	@Autowired
	private HibernateDAO dao;
	
	/**
	 * Recebe uma lista de StructureLevelInstance e seta os atributos
	 * 
	 * @param structureLevelInstance 
	 */
	public void setAttributes(final List<StructureLevelInstance> structureLevelInstance) {
		List<AttributeInstance> attrInstances = listAllAttributeInstanceByLevelInstances(structureLevelInstance);
		
		Map<Long, List<AttributeInstance>> strucAttrInstanceMap = new HashMap<>();
		for (AttributeInstance attrInstance : attrInstances) {
			List<AttributeInstance> attrInstanceList = strucAttrInstanceMap.get(attrInstance.getLevelInstance().getId());
			if (attrInstanceList == null) {
				attrInstanceList = new LinkedList<>();
				strucAttrInstanceMap.put(attrInstance.getLevelInstance().getId(), attrInstanceList);
			}
			attrInstanceList.add(attrInstance);
		}
		for (StructureLevelInstance goal : structureLevelInstance) {
			goal.setAttributeInstanceList(strucAttrInstanceMap.get(goal.getId()));			
		}
	}

	/**
	 * Recebe uma lista de StructureLevelInstance e seta os atributos
	 * 
	 * @param structureLevelInstance 
	 */
	public void setAttributesToGoalsInfoCalc(final List<StructureLevelInstance> structureLevelInstance) {
		List<AttributeInstanceToGoalsInfo> attrInstances = listAllAttributeInstanceByLevelInstancesToGoalsInfoCalc(structureLevelInstance);
		
		Map<Long, List<AttributeInstanceToGoalsInfo>> strucAttrInstanceMap = new HashMap<>();
		for (AttributeInstanceToGoalsInfo attrInstance : attrInstances) {
			List<AttributeInstanceToGoalsInfo> attrInstanceList = strucAttrInstanceMap.get(attrInstance.getLevelInstanceId());
			if (attrInstanceList == null) {
				attrInstanceList = new LinkedList<>();
				strucAttrInstanceMap.put(attrInstance.getLevelInstanceId(), attrInstanceList);
			}
			attrInstanceList.add(attrInstance);
		}
		for (StructureLevelInstance goal : structureLevelInstance) {
			goal.setAttributeInstanceToGoalsInfoList(strucAttrInstanceMap.get(goal.getId()));			
		}
	}

	/**
	 * Busca os atributos relacionados com os elementos de uma lista de StructureLevelInstance
	 *
	 * @param slis lista de StructureLevelInstance
	 * @return lista de AttributeInstance relacionados com lista de StructureLevelInstance
	 */
	public List<AttributeInstance> listAllAttributeInstanceByLevelInstances(List<StructureLevelInstance> slis) {
		if (GeneralUtils.isEmpty(slis)) {
			return Collections.emptyList();
		}
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.add(Restrictions.in("levelInstance", slis));
		return this.dao.findByCriteria(criteria, AttributeInstance.class);
	}

	private List<AttributeInstanceToGoalsInfo> listAllAttributeInstanceByLevelInstancesToGoalsInfoCalc(List<StructureLevelInstance> slis) {
		if (GeneralUtils.isEmpty(slis)) {
			return Collections.emptyList();
		}

		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.add(Restrictions.in("levelInstance", slis));
		criteria.createAlias("attribute", "attribute", JoinType.INNER_JOIN);
		criteria.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN);

		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("valueAsNumber"), "valueAsNumber")
				.add(Projections.property("valueAsDate"), "valueAsDate")
				.add(Projections.property("attribute.finishDate"), "finishDate")
				.add(Projections.property("attribute.expectedField"), "expectedField")
				.add(Projections.property("attribute.reachedField"), "reachedField")
				.add(Projections.property("attribute.minimumField"), "minimumField")
				.add(Projections.property("attribute.maximumField"), "maximumField")
				.add(Projections.property("levelInstance.id"), "levelInstanceId");

		
		criteria.setProjection(projList)
				.setResultTransformer(AttributeInstanceToGoalsInfo.class);
		
		return this.dao.findByCriteria(criteria, AttributeInstanceToGoalsInfo.class);
	}

}
