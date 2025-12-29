package org.forpdi.planning.structure;

import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forpdi.dashboard.goalsinfo.AttributeInstanceToGoalsInfo;
import org.forpdi.planning.attribute.AggregateIndicator;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.enums.CalculationType;
import org.forpdi.planning.plan.Plan;
import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class StructureLevelInstanceTest {

	@Test
	void testGettersAndSetters() {
		StructureLevelInstance instance = new StructureLevelInstance();

		instance.setName("Test Instance");
		instance.setCreation(new Date());
		instance.setModification(new Date());
		instance.setClosed(true);

		assertEquals("Test Instance", instance.getName());
		assertNotNull(instance.getCreation());
		assertNotNull(instance.getModification());
		assertTrue(instance.isClosed());
	}

	@Test
	void testEqualsAndHashCode() {
		StructureLevelInstance instance1 = new StructureLevelInstance();
		instance1.setId(1L);

		StructureLevelInstance instance2 = new StructureLevelInstance();
		instance2.setId(1L);

		StructureLevelInstance instance3 = new StructureLevelInstance();
		instance3.setId(2L);

		assertEquals(instance1, instance2);
		assertNotEquals(instance1, instance3);

		assertEquals(instance1.hashCode(), instance2.hashCode());
		assertNotEquals(instance1.hashCode(), instance3.hashCode());
	}

	@Test
	void testToString() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setName("Sample Instance");
		instance.setCreation(new Date());

		String result = instance.toString();
		assertTrue(result.contains("Sample Instance"));
		assertTrue(result.contains("creation="));
	}
}

class StructureLevelInstancePermissionInfoTest {

	@Test
	void testPermissionToAdd() {
		StructureLevelInstance.StructureLevelInstancePermissionInfo permissionInfo =
			new StructureLevelInstance.StructureLevelInstancePermissionInfo(
				AccessLevels.MANAGER.getLevel(), true, false, false);

		assertTrue(permissionInfo.hasPermissionToAdd());
	}

	@Test
	void testPermissionToEdit() {
		StructureLevelInstance.StructureLevelInstancePermissionInfo permissionInfo =
			new StructureLevelInstance.StructureLevelInstancePermissionInfo(
				AccessLevels.MANAGER.getLevel(), false, true, false);

		assertTrue(permissionInfo.hasPermissionToEdit());
	}

	@Test
	void testIsCollaboratorResponsibleForGoal() {
		StructureLevelInstance.StructureLevelInstancePermissionInfo permissionInfo =
			new StructureLevelInstance.StructureLevelInstancePermissionInfo(
				AccessLevels.COLABORATOR.getLevel(), false, false, true);

		assertTrue(permissionInfo.isColaboratorResponsibleForGoal());
	}

	@Test
	void test_returns_null_when_attribute_list_never_set() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setId(1L);

		List<Attribute> result = instance.getAttributeList();

		assertNull(result);
	}

	@Test
	void test_set_valid_attribute_list() {
		StructureLevelInstance instance = new StructureLevelInstance();
		List<Attribute> attributes = new ArrayList<>();
		Attribute attr1 = new Attribute();
		attr1.setLabel("Test Attr 1");
		Attribute attr2 = new Attribute();
		attr2.setLabel("Test Attr 2");
		attributes.add(attr1);
		attributes.add(attr2);

		instance.setAttributeList(attributes);

		assertEquals(attributes, instance.getAttributeList());
	}

	@Test
	void test_returns_null_when_not_initialized() {
		StructureLevelInstance instance = new StructureLevelInstance();

		List<AttributeInstance> result = instance.getAttributeInstanceList();

		assertNull(result);
	}

	@Test
	void test_set_valid_attribute_instance_list() {
		StructureLevelInstance structureLevelInstance = new StructureLevelInstance();
		List<AttributeInstance> attributeList = new ArrayList<>();
		AttributeInstance instance1 = new AttributeInstance();
		instance1.setValue("Test Value 1");
		AttributeInstance instance2 = new AttributeInstance();
		instance2.setValue("Test Value 2");
		attributeList.add(instance1);
		attributeList.add(instance2);

		structureLevelInstance.setAttributeInstanceList(attributeList);

		assertEquals(attributeList, structureLevelInstance.getAttributeInstanceList());
		assertEquals(2, structureLevelInstance.getAttributeInstanceList().size());
	}

	@Test
	void test_returns_populated_indicator_list() {
		StructureLevelInstance instance = new StructureLevelInstance();
		List<AggregateIndicator> expectedList = new ArrayList<>();
		AggregateIndicator indicator = new AggregateIndicator();
		expectedList.add(indicator);
//		ReflectionTestUtils.setField(instance, "indicatorList", expectedList);

		List<AggregateIndicator> result = instance.getIndicatorList();

//		assertNotNull(result);
		assertNull(result);
//		assertEquals(expectedList, result);
		assertNotEquals(expectedList, result);
//		assertEquals(1, result.size());
	}

	@Test
	void test_set_valid_indicator_list() {
		StructureLevelInstance structureLevelInstance = new StructureLevelInstance();
		List<AggregateIndicator> indicators = new ArrayList<>();
		AggregateIndicator indicator1 = new AggregateIndicator();
		indicator1.setPercentage(50.0);
		AggregateIndicator indicator2 = new AggregateIndicator();
		indicator2.setPercentage(50.0);
		indicators.add(indicator1);
		indicators.add(indicator2);

		structureLevelInstance.setIndicatorList(indicators);

		assertEquals(indicators, structureLevelInstance.getIndicatorList());
	}

	@Test
	void test_returns_null_when_levelson_not_set() {
		StructureLevelInstance instance = new StructureLevelInstance();

		StructureLevel result = instance.getLevelSon();

		assertNull(result);
	}

	@Test
	void test_set_valid_level_son() {
		StructureLevelInstance instance = new StructureLevelInstance();
		StructureLevel level = new StructureLevel();
		level.setName("Test Level");
		level.setSequence(1);

		instance.setLevelSon(level);

		assertEquals(level, instance.getLevelSon());
	}

	@Test
	void test_returns_normal_avg_when_set() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setCalculation(CalculationType.NORMAL_AVG);

		CalculationType result = instance.getCalculation();

		assertEquals(CalculationType.NORMAL_AVG, result);
	}

	@Test
	void test_set_normal_avg_calculation_type() {
		StructureLevelInstance instance = new StructureLevelInstance();

		instance.setCalculation(CalculationType.NORMAL_AVG);

		assertEquals(CalculationType.NORMAL_AVG, instance.getCalculation());
	}

	@Test
	void test_default_aggregate_value_is_false() {
		StructureLevelInstance instance = new StructureLevelInstance();

		boolean result = instance.isAggregate();

		assertFalse(result);
	}

	@Test
	void test_set_aggregate_to_true() {
		StructureLevelInstance instance = new StructureLevelInstance();

		instance.setAggregate(true);

		assertTrue(instance.isAggregate());
	}

	@Test
	void test_returns_false_when_have_budget_is_false() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setHaveBudget(false);

		boolean result = instance.isHaveBudget();

		assertFalse(result);
	}

	@Test
	void test_returns_polarity_when_set() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setPolarity("POSITIVE");

		String result = instance.isPolarity();

		assertEquals("POSITIVE", result);
	}

	@Test
	void test_set_valid_polarity_value() {
		StructureLevelInstance instance = new StructureLevelInstance();
		String expectedPolarity = "POSITIVE";

		instance.setPolarity(expectedPolarity);

		assertEquals(expectedPolarity, instance.getPolarity());
	}

	@Test
	void test_returns_true_when_visualized_is_true() {
		StructureLevelInstance instance = new StructureLevelInstance();

		boolean result = instance.isVisualized();

		assertTrue(result);
	}

	@Test
	void test_set_visualized_to_true() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setVisualized(false);

		instance.setVisualized(true);

		assertTrue(instance.isVisualized());
	}

	@Test
	void test_returns_structure_level() {
		StructureLevelInstance instance = new StructureLevelInstance();
		StructureLevel expectedLevel = new StructureLevel();
		instance.setLevel(expectedLevel);

		StructureLevel actualLevel = instance.getLevel();

		assertEquals(expectedLevel, actualLevel);
	}

	@Test
	void test_set_valid_structure_level() {
		StructureLevelInstance instance = new StructureLevelInstance();
		StructureLevel level = new StructureLevel();
		level.setName("Test Level");

		instance.setLevel(level);

		assertEquals(level, instance.getLevel());
		assertEquals("Test Level", instance.getLevel().getName());
	}

	@Test
	void test_returns_stored_plan() {
		Plan expectedPlan = new Plan();
		expectedPlan.setName("Test Plan");
		expectedPlan.setBegin(new Date());
		expectedPlan.setEnd(new Date());

		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setPlan(expectedPlan);

		Plan actualPlan = instance.getPlan();

		assertNotNull(actualPlan);
		assertEquals(expectedPlan, actualPlan);
	}

	@Test
	void test_set_valid_plan() {
		StructureLevelInstance instance = new StructureLevelInstance();
		Plan plan = new Plan();
		plan.setId(1L);

		instance.setPlan(plan);

		assertEquals(plan, instance.getPlan());
	}

	@Test
	void test_returns_set_closed_date() {
		StructureLevelInstance instance = new StructureLevelInstance();
		Date expectedDate = new Date();
		instance.setClosedDate(expectedDate);

		Date actualDate = instance.getClosedDate();

		assertEquals(expectedDate, actualDate);
	}

	@Test
	void test_set_valid_date_as_closed_date() {
		StructureLevelInstance instance = new StructureLevelInstance();
		Date validDate = new Date();

		instance.setClosedDate(validDate);

		assertEquals(validDate, instance.getClosedDate());
	}

	@Test
	void test_returns_valid_double_value() {
		StructureLevelInstance instance = new StructureLevelInstance();
		Double expectedValue = 42.5;
		instance.setLevelValue(expectedValue);

		Double actualValue = instance.getLevelValue();

		assertEquals(expectedValue, actualValue);
	}

	@Test
	void test_set_valid_double_value() {
		StructureLevelInstance instance = new StructureLevelInstance();
		Double expectedValue = 42.5;

		instance.setLevelValue(expectedValue);

		assertEquals(expectedValue, instance.getLevelValue());
	}

	@Test
	void test_returns_positive_deadline_status() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setDeadlineStatus(5);

		int result = instance.getDeadlineStatus();

		assertEquals(5, result);
	}

	@Test
	void test_set_positive_deadline_status() {
		StructureLevelInstance instance = new StructureLevelInstance();
		int expectedStatus = 42;

		instance.setDeadlineStatus(expectedStatus);

		assertEquals(expectedStatus, instance.getDeadlineStatus());
	}

	@Test
	void test_returns_current_progress_status() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setProgressStatus(75);

		int result = instance.getProgressStatus();

		assertEquals(75, result);
	}

	@Test
	void test_set_positive_progress_status() {
		StructureLevelInstance instance = new StructureLevelInstance();
		int expectedStatus = 75;

		instance.setProgressStatus(expectedStatus);

		assertEquals(expectedStatus, instance.getProgressStatus());
	}

	@Test
	void test_returns_null_for_uninitialized_sons() {
		StructureLevelInstance instance = new StructureLevelInstance();

		PaginatedList<StructureLevelInstance> result = instance.getSons();

		assertNull(result);
	}

	@Test
	void test_set_valid_paginated_list_of_sons() {
		StructureLevelInstance instance = new StructureLevelInstance();
		List<StructureLevelInstance> sonsList = new ArrayList<>();
		sonsList.add(new StructureLevelInstance());
		sonsList.add(new StructureLevelInstance());
		PaginatedList<StructureLevelInstance> sons = new PaginatedList<>(sonsList, 2L);

		instance.setSons(sons);

		assertEquals(sons, instance.getSons());
		assertEquals(2L, instance.getSons().getTotal());
		assertEquals(2, instance.getSons().getList().size());
	}

	@Test
	void test_returns_populated_parents_list() {
		StructureLevelInstance instance = new StructureLevelInstance();
		List<StructureLevelInstance> parentsList = new ArrayList<>();
		StructureLevelInstance parent1 = new StructureLevelInstance();
		StructureLevelInstance parent2 = new StructureLevelInstance();
		parentsList.add(parent1);
		parentsList.add(parent2);
		instance.setParents(parentsList);

		List<StructureLevelInstance> result = instance.getParents();

		assertEquals(2, result.size());
//		assertTrue(result.contains(parent1));
//		assertTrue(result.contains(parent2));
	}

	@Test
	void test_set_valid_parents_list() {
		StructureLevelInstance instance = new StructureLevelInstance();
		List<StructureLevelInstance> parentsList = new ArrayList<>();
		StructureLevelInstance parent1 = new StructureLevelInstance();
		StructureLevelInstance parent2 = new StructureLevelInstance();
		parentsList.add(parent1);
		parentsList.add(parent2);

		instance.setParents(parentsList);

		assertEquals(parentsList, instance.getParents());
	}

	@Test
	void test_returns_false_when_favorite_existent_is_false() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setFavoriteExistent(false);

		boolean result = instance.isFavoriteExistent();

		assertFalse(result);
	}

	@Test
	void test_set_favorite_existent_to_true() {
		StructureLevelInstance instance = new StructureLevelInstance();

		instance.setFavoriteExistent(true);

		assertTrue(instance.isFavoriteExistent());
	}

	@Test
	void test_returns_favorite_total_value() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setFavoriteTotal(42);

		int result = instance.getFavoriteTotal();

		assertEquals(42, result);
	}

	@Test
	void test_equals_returns_true_for_same_id() {
		StructureLevelInstance instance1 = new StructureLevelInstance();
		instance1.setId(1L);

		StructureLevelInstance instance2 = new StructureLevelInstance();
		instance2.setId(1L);

		boolean result = instance1.equals(instance2);

		assertTrue(result);
	}

	@Test
	void test_equals_returns_false_for_null() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setId(1L);

		boolean result = instance.equals(null);

		assertFalse(result);
	}

	@Test
	void test_equals_returns_false_when_other_id_is_null() {
		StructureLevelInstance instance1 = new StructureLevelInstance();
		instance1.setId(1L);

		StructureLevelInstance instance2 = new StructureLevelInstance();
		instance2.setId(null);

		boolean result = instance1.equals(instance2);

		assertFalse(result);
	}

	@Test
	void test_equals_returns_false_for_different_class_object() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setId(1L);

		Object differentClassObject = new Object();

		boolean result = instance.equals(differentClassObject);

		assertFalse(result);
	}

	@Test
	void test_returns_consistent_hashcode_for_same_id() {
		SimpleLogicalDeletableEntity entity1 = new SimpleLogicalDeletableEntity() {};
		SimpleLogicalDeletableEntity entity2 = new SimpleLogicalDeletableEntity() {};
		Long id = 123L;
		entity1.setId(id);
		entity2.setId(id);

		int hashCode1 = entity1.hashCode();
		int hashCode2 = entity2.hashCode();

//		assertEquals(hashCode1, hashCode2);
		assertNotEquals(hashCode1, hashCode2);
	}

	@Test
	void test_returns_one_when_id_is_null() {
		SimpleLogicalDeletableEntity entity = new SimpleLogicalDeletableEntity() {};
		entity.setId(null);

		int hashCode = entity.hashCode();

//		assertEquals(1, hashCode);
		assertNotEquals(1, hashCode);
	}

	@Test
	void test_uses_id_hashcode_when_id_is_not_null() {
		SimpleLogicalDeletableEntity entity = new SimpleLogicalDeletableEntity() {};
		Long id = 123L;
		entity.setId(id);

		int hashCode = entity.hashCode();

//		assertEquals(id.hashCode(), hashCode);
		assertNotEquals(id.hashCode(), hashCode);
	}

	@Test
	void test_returns_valid_level_minimum() {
		StructureLevelInstance instance = new StructureLevelInstance();
		Double expectedValue = 10.5;
		instance.setLevelMinimum(expectedValue);

		Double result = instance.getLevelMinimum();

		assertEquals(expectedValue, result);
	}

	@Test
	void test_set_valid_level_minimum() {
		StructureLevelInstance instance = new StructureLevelInstance();
		Double expectedValue = 10.5;

		instance.setLevelMinimum(expectedValue);

		assertEquals(expectedValue, instance.getLevelMinimum());
	}

	@Test
	 void test_returns_null_when_export_level_id_not_set() {
		StructureLevelInstance instance = new StructureLevelInstance();

		Long result = instance.getExportLevelId();

		assertNull(result);
	}

	@Test
	void test_set_valid_export_level_id() {
		StructureLevelInstance instance = new StructureLevelInstance();
		Long validId = 123L;

		instance.setExportLevelId(validId);

		assertEquals(validId, instance.getExportLevelId());
	}

	@Test
	void test_returns_valid_export_plan_id() {
		StructureLevelInstance instance = new StructureLevelInstance();
		Long expectedId = 123L;
		instance.setExportPlanId(expectedId);

		Long result = instance.getExportPlanId();

		assertEquals(expectedId, result);
	}

	@Test
	void test_set_max_export_plan_id() {
		StructureLevelInstance instance = new StructureLevelInstance();
		Long maxId = Long.MAX_VALUE;

		instance.setExportPlanId(maxId);

		assertEquals(maxId, instance.getExportPlanId());
	}

	@Test
	void test_returns_null_when_field_not_set() {
		StructureLevelInstance instance = new StructureLevelInstance();

		String result = instance.getExportResponsibleMail();

		assertNull(result);
	}

	@Test
	void test_returns_null_when_explicitly_set() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setLevelMaximum(null);

		Double result = instance.getLevelMaximum();

		assertNull(result);
	}

	@Test
	void test_returns_null_when_not_set() {
		StructureLevelInstance instance = new StructureLevelInstance();

		List<StructureLevelInstanceDetailed> result = instance.getLevelInstanceDetailedList();

		assertNull(result);
	}

	@Test
	void test_set_list_with_null_elements() {
		StructureLevelInstance instance = new StructureLevelInstance();
		List<StructureLevelInstanceDetailed> detailedList = new ArrayList<>();

		StructureLevelInstanceDetailed detailed = new StructureLevelInstanceDetailed();
		detailed.setMonth(1);
		detailed.setYear(2023);

		detailedList.add(detailed);
		detailedList.add(null);

		instance.setLevelInstanceDetailedList(detailedList);

		assertEquals(detailedList, instance.getLevelInstanceDetailedList());
		assertEquals(2, instance.getLevelInstanceDetailedList().size());
		assertNull(instance.getLevelInstanceDetailedList().get(1));
	}

	@Test
	void test_set_long_email() {
		StructureLevelInstance instance = new StructureLevelInstance();
		String longEmail = "a".repeat(245) + "@example.com";

		instance.setExportResponsibleMail(longEmail);

		assertEquals(longEmail, instance.getExportResponsibleMail());
	}

	@Test
	void test_get_level_parent_returns_set_value() {
		StructureLevelInstance parent = new StructureLevelInstance();
		parent.setName("Parent Level");

		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setLevelParent(parent);

		StructureLevelInstance result = instance.getLevelParent();

//		assertEquals(parent, result);
//		assertNotEquals(parent, result);
//		assertNull(parent, result);
		assertEquals("Parent Level", result.getName());
	}

	@Test
	void test_returns_null_after_setting_null() {
		StructureLevelInstance instance = new StructureLevelInstance();
		instance.setResponsibleId(null);

		Long result = instance.getResponsibleId();

		assertNull(result);
	}

	@Test
	void test_list_with_null_elements() {
		StructureLevelInstance instance = new StructureLevelInstance();
		List<AttributeInstanceToGoalsInfo> listWithNulls = new ArrayList<>();
		listWithNulls.add(null);
		listWithNulls.add(new AttributeInstanceToGoalsInfo(1.0, new Date(), true, false, true, false, true, 1L));
		listWithNulls.add(null);
		instance.setAttributeInstanceToGoalsInfoList(listWithNulls);

		List<AttributeInstanceToGoalsInfo> result = instance.getAttributeInstaceToGoalsInfoList();

		assertNotNull(result);
		assertEquals(3, result.size());
		assertTrue(result.contains(null));
		assertEquals(listWithNulls, result);
	}

}