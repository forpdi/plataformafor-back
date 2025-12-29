package org.forpdi.planning.attribute;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.forpdi.core.user.User;
import org.forpdi.planning.fields.OptionsField;
import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.forpdi.planning.fields.budget.BudgetDTO;
import org.forpdi.planning.fields.schedule.Schedule;
import org.forpdi.planning.fields.table.TableFields;
import org.forpdi.planning.structure.StructureLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttributeTest {

	@Test
	@DisplayName("Testa o getter e setter de label")
	void testLabel() {
		Attribute attribute = new Attribute();
		String label = "Nome do Atributo";

		attribute.setLabel(label);
		assertEquals(label, attribute.getLabel());
	}

	@Test
	@DisplayName("Testa o getter e setter de description")
	void testDescription() {
		Attribute attribute = new Attribute();
		String description = "Descrição do atributo";

		attribute.setDescription(description);
		assertEquals(description, attribute.getDescription());
	}

	@Test
	@DisplayName("Testa o getter e setter de type")
	void testType() {
		Attribute attribute = new Attribute();
		String type = "Tipo do Atributo";

		attribute.setType(type);
		assertEquals(type, attribute.getType());
	}

	@Test
	@DisplayName("Testa o getter e setter de level")
	void testLevel() {
		Attribute attribute = new Attribute();
		StructureLevel level = new StructureLevel();
		level.setId(1L);

		attribute.setLevel(level);
		assertEquals(level, attribute.getLevel());
	}

	@Test
	@DisplayName("Testa o getter e setter de required")
	void testRequired() {
		Attribute attribute = new Attribute();
		attribute.setRequired(true);
		assertTrue(attribute.isRequired());

		attribute.setRequired(false);
		assertFalse(attribute.isRequired());
	}

	@Test
	@DisplayName("Testa os atributos booleanos específicos")
	void testBooleanFields() {
		Attribute attribute = new Attribute();

		attribute.setVisibleInTables(true);
		assertTrue(attribute.isVisibleInTables());

		attribute.setFinishDate(true);
		assertTrue(attribute.isFinishDate());

		attribute.setExpectedField(true);
		assertTrue(attribute.isExpectedField());

		attribute.setJustificationField(true);
		assertTrue(attribute.isJustificationField());

		attribute.setMinimumField(true);
		assertTrue(attribute.isMinimumField());

		attribute.setMaximumField(true);
		assertTrue(attribute.isMaximumField());

		attribute.setReachedField(true);
		assertTrue(attribute.isReachedField());

		attribute.setReferenceField(true);
		assertTrue(attribute.isReferenceField());

		attribute.setPolarityField(true);
		assertTrue(attribute.isPolarityField());

		attribute.setFormatField(true);
		assertTrue(attribute.isFormatField());

		attribute.setPeriodicityField(true);
		assertTrue(attribute.isPeriodicityField());

		attribute.setBeginField(true);
		assertTrue(attribute.isBeginField());

		attribute.setEndField(true);
		assertTrue(attribute.isEndField());

		attribute.setBscField(true);
		assertTrue(attribute.isBscField());
	}

	@Test
	@DisplayName("Testa o getter e setter de attributeInstance")
	void testAttributeInstance() {
		Attribute attribute = new Attribute();
		AttributeInstance instance = new AttributeInstance();

		attribute.setAttributeInstance(instance);
		assertEquals(instance, attribute.getAttributeInstance());
	}

	@Test
	@DisplayName("Testa o getter e setter de attributeInstances")
	void testAttributeInstances() {
		Attribute attribute = new Attribute();
		List<AttributeInstance> instances = new ArrayList<>();
		AttributeInstance instance = new AttributeInstance();
		instances.add(instance);

		attribute.setAttributeInstances(instances);
		assertEquals(instances, attribute.getAttributeInstances());
	}

	@Test
	@DisplayName("Testa o getter e setter de budgets")
	void testBudgets() {
		Attribute attribute = new Attribute();
		List<BudgetDTO> budgets = new ArrayList<>();
		BudgetDTO budget = new BudgetDTO();
		budgets.add(budget);

		attribute.setBudgets(budgets);
		assertEquals(budgets, attribute.getBudgets());
	}

	@Test
	@DisplayName("Testa o getter e setter de actionPlans")
	void testActionPlans() {
		Attribute attribute = new Attribute();
		List<ActionPlan> plans = new ArrayList<>();
		ActionPlan plan = new ActionPlan();
		plans.add(plan);

		attribute.setActionPlans(plans);
		assertEquals(plans, attribute.getActionPlans());
	}

	@Test
	@DisplayName("Testa o getter e setter de schedule")
	void testSchedule() {
		Attribute attribute = new Attribute();
		Schedule schedule = new Schedule();

		attribute.setSchedule(schedule);
		assertEquals(schedule, attribute.getSchedule());
	}

	@Test
	@DisplayName("Testa o getter e setter de tableFields")
	void testTableFields() {
		Attribute attribute = new Attribute();
		TableFields fields = new TableFields();

		attribute.setTableFields(fields);
		assertEquals(fields, attribute.getTableFields());
	}

	@Test
	@DisplayName("Testa o getter e setter de optionsField")
	void testOptionsField() {
		Attribute attribute = new Attribute();
		List<OptionsField> options = new ArrayList<>();
		OptionsField option = new OptionsField();
		options.add(option);

		attribute.setOptionsField(options);
		assertEquals(options, attribute.getOptionsField());
	}

	@Test
	@DisplayName("Testa o getter e setter de users")
	void testUsers() {
		Attribute attribute = new Attribute();
		List<User> users = new ArrayList<>();
		User user = new User();
		users.add(user);

		attribute.setUsers(users);
		assertEquals(users, attribute.getUsers());
	}

	@Test
	@DisplayName("Testa o getter e setter de exportStructureLevelId")
	void testExportStructureLevelId() {
		Attribute attribute = new Attribute();
		Long exportId = 123L;

		attribute.setExportStructureLevelId(exportId);
		assertEquals(exportId, attribute.getExportStructureLevelId());
	}

	@Test
	@DisplayName("Testa o método toString")
	void testToString() {
		Attribute attribute = new Attribute();
		attribute.setLabel("Atributo Teste");

		String result = attribute.toString();
		assertTrue(result.contains("Atributo Teste"));
	}

	@Test
	void test_set_option_labels_with_valid_list() {
		Attribute attribute = new Attribute();
		List<OptionsField> options = new ArrayList<>();
		OptionsField option1 = new OptionsField();
		option1.setLabel("Option 1");
		OptionsField option2 = new OptionsField();
		option2.setLabel("Option 2");
		options.add(option1);
		options.add(option2);

		attribute.setOptionLabels(options);

		assertEquals(options, attribute.getOptionsField());
		assertEquals(2, attribute.getOptionsField().size());
	}
}
