package org.forpdi.planning.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.Restrictions;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PolarityMapHelper {

	@Autowired
	private HibernateDAO dao;
	
	public PolarityMapHelper() {
	}

	/**
	 * Gera um Map com o id de uma StructureLevelInstance que representa uma meta (goal) e um 
	 * AttributeInstance que representa uma polaridade (polarity) 
	 * 
	 * @param goals lista de metas
	 * @return mapa id da meta / polaridade
	 */
	public Map<Long, AttributeInstance> generatePolarityMap(List<StructureLevelInstance> goals) {
		// cria uma lista com as instancias pai de goals (istancias metas) de onde eh possivel recuperar a polaridade
		// cria um map com os ids de goals e dos pais para facilitar o aceeso posterior 
		List<Long> goalParentIds = new ArrayList<>(goals.size());
		Map<Long, Long> idParentMap = new HashMap<>();
		for (StructureLevelInstance goal : goals) {
			if (goal.getParent() != null) {
				goalParentIds.add(goal.getParent());
				idParentMap.put(goal.getParent(), goal.getId());
			}
		}
		// recupera todas AttributeInstance em que levelInstance possui o campo de polaridade
		 List<AttributeInstance> polarities = this.retrievePolaritiesByLevelInstanceIds(goalParentIds);
		// cria um map para acessar a polaridade atraves do id do goal (meta)
		Map<Long, AttributeInstance> polarityMap = new HashMap<>();
		for (AttributeInstance polarity : polarities) {
			long structureLevelInstanceId = idParentMap.get(polarity.getLevelInstance().getId());
			polarityMap.put(structureLevelInstanceId, polarity);
		}
		return polarityMap;
	}

	/**
	 * Busca as instancias de atributos referentes a polaridade relacionados com os ids de StructureLevelInstance passados
	 * 
	 * @param levelInstanceIds lista de ids de StructureLevelInstance
	 * @return lista de atributos que se refere a polaridade
	 */
	public List<AttributeInstance> retrievePolaritiesByLevelInstanceIds(List<Long> levelInstanceIds) {
		if (GeneralUtils.isEmpty(levelInstanceIds)) {
			return Collections.emptyList();
		}
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class)
			.createAlias("attribute", "attribute", JoinType.INNER_JOIN)
			.add(Restrictions.in("levelInstance.id", levelInstanceIds))
			.add(Restrictions.eq("attribute.polarityField", true));
		return this.dao.findByCriteria(criteria, AttributeInstance.class);
	}
}
