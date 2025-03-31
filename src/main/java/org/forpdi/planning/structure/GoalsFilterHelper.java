package org.forpdi.planning.structure;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.utils.Consts;
import org.forpdi.core.utils.JsonUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.NumberField;
import org.forpdi.system.CriteriaCompanyFilter;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

@Component
public class GoalsFilterHelper {
	@Autowired
	private HibernateDAO dao;
	
	@Autowired
	private StructureBS structureBS;
	
	@Autowired
	private CriteriaCompanyFilter filter;
	
	@Autowired
	private AttributeHelper attrHelper;

	/**
	 * Listar metas a partir de filtros de pesquisa.
	 * @param term
	 *            parentId da instância de um level pai.
	 *            name termo com o nome da meta
	 *            attributesToFilter string com os filtros que serão aplicados nos atributos
	 *            page número da página
	 *            pageSize quantidade de itens na página
	 * @return list Lista de instâncias
	 */
	public PaginatedList<StructureLevelInstance> retrieveFilteredGoals(Long parentId, String name,
			Integer progressStatusId, String attributesToFilter, Integer page, Integer pageSize) {

		if (page == null || page <= 0) {
			page = 1;
		}
		if (pageSize == null || pageSize <= 0) {
			pageSize = Consts.MIN_PAGE_SIZE;
		}
		
		Set<AttributeToFilter> attributeToFilterSet = getAttributeToFilterSet(attributesToFilter);

		boolean isFilteringOnlyByName = attributeToFilterSet.isEmpty() && progressStatusId == null;
		
		String polarity = getPolarity(parentId);
		
		PaginatedList<StructureLevelInstance> levelInstanceList = null;
		if (!isFilteringOnlyByName) {	
			levelInstanceList = progressStatusId != null && progressStatusId == GoalProgressStatus.NOT_FILLED.getId()
				? filterByNotFilled(parentId, name, progressStatusId, page, pageSize, attributeToFilterSet, polarity)
				: filterByAttributes(parentId, name, progressStatusId, page, pageSize, attributeToFilterSet, polarity);
		} else {
			levelInstanceList = filterOnlyByName(parentId, name, page, pageSize);
		}
		
		List<StructureLevelInstance> list = levelInstanceList.getList();
				
		setAttributes(list);

		setGoalStatus(list, polarity);
		
		return levelInstanceList;
	}

	private Set<AttributeToFilter> getAttributeToFilterSet(String attributesToFilter) {
		if (attributesToFilter == null) {
			return Collections.emptySet();
		}
		
		Type setType = new TypeToken<Set<AttributeToFilter>>() {}.getType();
		Set<AttributeToFilter> attributeToFilterSet = JsonUtil.fromJson(attributesToFilter, setType);
		return attributeToFilterSet;
	}

	private String getPolarity(Long parentId) {
    AttributeInstance attributeInstance = this.attrHelper.retrievePolarityAttributeInstance(parentId);
    if (attributeInstance == null || attributeInstance.getValue() == null) {
        throw new IllegalArgumentException("Necessário preencher as informações do indicador");
    }
    return attributeInstance.getValue();
}

	private PaginatedList<StructureLevelInstance> filterOnlyByName(Long parentId, String name, Integer page,
			Integer pageSize) {
		Criteria criteria = createFilterOnlyByNameCriteria(parentId, name);
		criteria.addOrder(Order.asc("visualized"))
			.addOrder(Order.desc("creation"))
			.setFirstResult(Util.getPaginationOffset(page, pageSize))
			.setMaxResults(pageSize);
		
		Criteria counting = createFilterOnlyByNameCriteria(parentId, name);
		counting.setProjection(Projections.countDistinct("id"));

		List<StructureLevelInstance> list = this.filter.filterAndList(
				criteria, StructureLevelInstance.class, "macro.company");
		long total = this.filter.filterAndFind(counting, "macro.company");
		
		return new PaginatedList<StructureLevelInstance>(list, total);
	}
	
	private PaginatedList<StructureLevelInstance> filterByNotFilled(Long parentId, String name, Integer progressStatusId,
			Integer page, Integer pageSize, Set<AttributeToFilter> attributeToFilterSet, String polarity) {
		int offset = Util.getPaginationOffset(page, pageSize);
		

		String sqlQuery = getFilterByProgressSql(parentId, name, progressStatusId, polarity, "fsli.id");

		String countingQuery = getFilterByProgressSql(parentId, name, progressStatusId, polarity, "count(fsli.id)");
		sqlQuery += "LIMIT " + pageSize + " OFFSET " + offset + ";";

		@SuppressWarnings("unchecked")
		List<BigInteger> levelInstanceIds = this.dao.newSQLQuery(sqlQuery).getResultList();
		BigInteger total = (BigInteger) this.dao.newSQLQuery(countingQuery).getSingleResult();
		
		List<Long> idsAsLong = Util.bigIntegerListToLongList(levelInstanceIds);
		
		List<StructureLevelInstance> list = this.structureBS.listStructureLevelInstancesByIds(idsAsLong);
		
		return new PaginatedList<StructureLevelInstance>(list, total.longValue());
	}

	private Criteria createFilterOnlyByNameCriteria(Long parentId, String name) {
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
				.createAlias("level", "level", JoinType.INNER_JOIN)
				.createAlias("plan", "plan", JoinType.INNER_JOIN)
				.createAlias("plan.parent", "macro", JoinType.INNER_JOIN)
				.add(Restrictions.eq("level.goal", true))
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("parent", parentId));
		
		if (!GeneralUtils.isEmpty(name)) {
			criteria.add(Restrictions.like("name", "%" + name + "%").ignoreCase());
		}

		return criteria;
	}

	private PaginatedList<StructureLevelInstance> filterByAttributes(Long parentId, String name, Integer progressStatusId,
			Integer page, Integer pageSize, Set<AttributeToFilter> attributeToFilterSet, String polarity) {
		Map<StructureLevelInstance, List<AttributeInstance>> mappedByLevelInstance = new HashMap<>();
		Set<Long> totalSet = new HashSet<>();
		
		for (AttributeToFilter attributeToFilter : attributeToFilterSet) {
			Criteria criteria = createAttributeInstanceCriteria(parentId, name, attributeToFilter);
			criteria.setFirstResult(Util.getPaginationOffset(page, pageSize))
					.setMaxResults(pageSize);

			Criteria counting = createAttributeInstanceCriteria(parentId, name, attributeToFilter);
			counting.setProjection(Projections.countDistinct("id"));
			
			List<AttributeInstance> result = filter.filterAndList(criteria, AttributeInstance.class, "macro.company");
			long total = filter.filterAndFind(counting, "macro.company");
			totalSet.add(total);
			
			addAttributeInstancesToMap(mappedByLevelInstance, result);
		}
		
		if (progressStatusId != null) {
			PaginatedList<AttributeInstance> list = filterByProgressStatus(parentId, name, progressStatusId,
					page, pageSize, polarity);
			
			totalSet.add(list.getTotal());

			addAttributeInstancesToMap(mappedByLevelInstance, list.getList());
		}

		return getLevelInstancesFromAttributes(attributeToFilterSet, progressStatusId, mappedByLevelInstance, totalSet);
	}

	private Criteria createAttributeInstanceCriteria(Long parentId, String name, AttributeToFilter attributeToFilter) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class)
				.createAlias("attribute", "attribute", JoinType.INNER_JOIN)
				.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN)
				.createAlias("levelInstance.level", "level", JoinType.INNER_JOIN)
				.createAlias("levelInstance.plan", "plan", JoinType.INNER_JOIN)
				.createAlias("plan.parent", "macro", JoinType.INNER_JOIN)
				.add(Restrictions.eq("attribute.id", attributeToFilter.getAttributeId()))
				.add(Restrictions.eq("level.goal", true))
				.add(Restrictions.eq("levelInstance.deleted", false))
				.add(Restrictions.eq("levelInstance.parent", parentId));

		String field = "value" + (attributeToFilter.getType().equals(NumberField.class.getCanonicalName()) ? "AsNumber" : "");
		criteria.add(Restrictions.eq(field, attributeToFilter.getValue()));

		if (!GeneralUtils.isEmpty(name)) {
			criteria.add(Restrictions.like("levelInstance.name", "%" + name + "%").ignoreCase());
		}

		return criteria;
	}

	private void addAttributeInstancesToMap(Map<StructureLevelInstance, List<AttributeInstance>> mappedByLevelInstance,
			List<AttributeInstance> list) {
		for (AttributeInstance attributeInstance : list) {
			StructureLevelInstance levelInstance = attributeInstance.getLevelInstance();
			List<AttributeInstance> attributeInstanceList = mappedByLevelInstance.get(levelInstance);

			if (attributeInstanceList == null) {
				attributeInstanceList = new ArrayList<>();
				mappedByLevelInstance.put(levelInstance, attributeInstanceList);
			}
			
			attributeInstanceList.add(attributeInstance);
		}
	}

	private PaginatedList<AttributeInstance> filterByProgressStatus(Long parentId, String name, Integer progressStatusId,
			Integer page, Integer pageSize, String polarity) {
		int offset = Util.getPaginationOffset(page, pageSize);

		String sqlQuery = getFilterByProgressSql(parentId, name, progressStatusId, polarity, "fai.id");

		String countingQuery = getFilterByProgressSql(parentId, name, progressStatusId, polarity, "count(fai.id)");

		sqlQuery += "LIMIT " + pageSize + " OFFSET " + offset + ";";

		@SuppressWarnings("unchecked")
		List<BigInteger> attributeIds = this.dao.newSQLQuery(sqlQuery).getResultList();
		BigInteger total = (BigInteger) this.dao.newSQLQuery(countingQuery).getSingleResult();
		
		List<Long> idsAsLong = Util.bigIntegerListToLongList(attributeIds);
		
		List<AttributeInstance> list = this.structureBS.listAttributeInstancesByIds(idsAsLong);
		
		return new PaginatedList<>(list, total.longValue());
	}

	private String getFilterNotFIlledSql(Long parentId, String nameExpression, String projection) {
		return "SELECT " + projection + " FROM fpdi_structure_level_instance fsli\n"
				+ "LEFT JOIN fpdi_attribute_instance fai on fai.levelInstance_id = fsli.id\n"
				+ "LEFT JOIN fpdi_attribute fa ON fai.attribute_id = fa.id\n"
				+ "LEFT JOIN fpdi_attribute_instance fai2 ON fai2.levelInstance_id = fai.levelInstance_id\n"
				+ "LEFT JOIN fpdi_attribute fa2 ON fa2.id = fai2.attribute_id\n"
				+ "WHERE fsli.parent = " + parentId + " AND fsli.deleted = false " + nameExpression + "\n"
				+ "AND fai.creation IS NULL\n";
	}
	
	private String getFilterByProgressSql(Long parentId, String name, Integer progressStatusId,
			String polarity, String projection) {
		GoalProgressStatus progressStatus = GoalProgressStatus.getById(progressStatusId);
		
		String nameExpression = !GeneralUtils.isEmpty(name)
				? "AND fsli.name like '%" + name + "%'"
						: "";
		
		if (progressStatus.equals(GoalProgressStatus.NOT_FILLED)) {
			return getFilterNotFIlledSql(parentId, nameExpression, projection);
		}
		
		SqlQueryParams params = getQueryParams(progressStatus, polarity);
		
		return "SELECT " + projection + " FROM fpdi_attribute_instance fai\n" + 
				"INNER JOIN fpdi_structure_level_instance fsli on fsli.id = fai.levelInstance_id\n" + 
				"INNER JOIN fpdi_attribute fa ON fai.attribute_id = fa.id\n" + 
				"INNER JOIN fpdi_attribute_instance fai2 ON fai2.levelInstance_id = fai.levelInstance_id\n" + 
				"INNER JOIN fpdi_attribute fa2 ON fa2.id = fai2.attribute_id \n" + 
				"WHERE fsli.parent = " + parentId + " AND fsli.deleted = false " + nameExpression + "\n" +
				"AND fa.reachedField = true AND fa2." + params.targetField + " = true\n" +
				"AND fai.valueAsNumber IS NOT NULL AND fai.valueAsNumber " + params.operator + " fai2.valueAsNumber\n";
	}

	private SqlQueryParams getQueryParams(GoalProgressStatus progressStatus, String polarityValue) {
		SqlQueryParams params;
		Polarity polarity = Polarity.getByValue(polarityValue);
		String operator = polarity.getOperator();
		String reverseOperator = polarity.getReverseOperator();
		
		switch (progressStatus) {
			case MINIMUM_BELOW:
				params = new SqlQueryParams("minimumField", reverseOperator + "=");
				break;
			case MINIMUM:
				params = new SqlQueryParams("expectedField", reverseOperator);
				break;
			case ENOUGH_UP:
				params = new SqlQueryParams("expectedField", operator + "=");
				break;
			case MAXIMUM_UP:
				params = new SqlQueryParams("expectedField", operator);
				break;
			default:
				throw new IllegalArgumentException("Unkkown progress status: " + progressStatus.getLabel());
		}
		return params;
	}

	private PaginatedList<StructureLevelInstance> getLevelInstancesFromAttributes(Set<AttributeToFilter> attributeToFilterSet,
			Integer progressStatusId, Map<StructureLevelInstance, List<AttributeInstance>> mappedByLevelInstance, Set<Long> totalSet) {
		List<StructureLevelInstance> levelInstanceList = new ArrayList<>();
		
		int numberOfAttributesFiltered = attributeToFilterSet.size();
		if (progressStatusId != null) {
			numberOfAttributesFiltered += 1;
		}
		
		for (Entry<StructureLevelInstance, List<AttributeInstance>> entry : mappedByLevelInstance.entrySet()) {
			List<AttributeInstance> list = entry.getValue();
			if (list.size() == numberOfAttributesFiltered) {
				levelInstanceList.add(entry.getKey());
			}
		}
		
		Optional<Long> optionalMinTotal = totalSet.stream().min((a, b) -> a.compareTo(b));
		long minTotal = optionalMinTotal.isPresent() ? optionalMinTotal.get() : 0;

		return new PaginatedList<StructureLevelInstance>(levelInstanceList, minTotal);
	}
	
	private void setAttributes(List<StructureLevelInstance> levelInstanceList) {
		for (StructureLevelInstance sli : levelInstanceList) {
			List<Attribute> attributes = this.structureBS.retrieveLevelSonsAttributes(sli);
			sli.getLevel().setAttributes(attributes);
		}
	}

	private void setGoalStatus(List<StructureLevelInstance> list, String polarity) {
		try {
			structureBS.setGoalStatus(list, polarity);
		} catch (ParseException e) {
			throw new IllegalStateException(e);
		}
	}

	private class SqlQueryParams {
		String targetField;
		String operator;

		public SqlQueryParams(String targetField, String operator) {
			this.targetField = targetField;
			this.operator = operator;
		}
	}
}
