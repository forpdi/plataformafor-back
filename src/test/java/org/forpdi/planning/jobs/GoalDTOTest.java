package org.forpdi.planning.jobs;

import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.structure.StructureLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class GoalDTOTest {

	private GoalDTO goalDTO;

	@BeforeEach
	void setup() {
		goalDTO = new GoalDTO();
	}

	@Test
	@DisplayName("Deve criar GoalDTO com valores padrão")
	void testDefaultConstructor() {
		assertNull(goalDTO.getParent());
		assertNull(goalDTO.getPlan());
		assertNull(goalDTO.getLevel());
		assertNull(goalDTO.getName());
		assertNull(goalDTO.getResponsible());
		assertNull(goalDTO.getManager());
		assertNull(goalDTO.getDescription());
		assertEquals(0.0, goalDTO.getExpected());
		assertEquals(0.0, goalDTO.getMinimum());
		assertEquals(0.0, goalDTO.getMaximum());
		assertNull(goalDTO.getReached());
		assertNull(goalDTO.getPeriodicity());
		assertNull(goalDTO.getBeginDate());
		assertNull(goalDTO.getEndDate());
	}

	@Test
	@DisplayName("Deve criar GoalDTO com valores definidos pelo construtor")
	void testParameterizedConstructor() {
		Long parent = 1L;
		Plan plan = new Plan();
		StructureLevel level = new StructureLevel();
		String name = "Test Goal";
		String responsible = "John Doe";
		String manager = "Jane Smith";
		String description = "Goal description";
		double expected = 100.0;
		double minimum = 80.0;
		double maximum = 120.0;
		String periodicity = "Monthly";
		Date beginDate = new Date();
		Date endDate = new Date();

		GoalDTO goal = new GoalDTO(parent, plan, level, name, manager, responsible, description, expected, minimum, maximum, periodicity, beginDate, endDate);

		assertEquals(parent, goal.getParent());
		assertEquals(plan, goal.getPlan());
		assertEquals(level, goal.getLevel());
		assertEquals(name, goal.getName());
		assertEquals(responsible, goal.getResponsible());
		assertEquals(manager, goal.getManager());
		assertEquals(description, goal.getDescription());
		assertEquals(expected, goal.getExpected());
		assertEquals(minimum, goal.getMinimum());
		assertEquals(maximum, goal.getMaximum());
		assertEquals(periodicity, goal.getPeriodicity());
		assertEquals(beginDate, goal.getBeginDate());
		assertEquals(endDate, goal.getEndDate());
	}

	@Test
	@DisplayName("Deve definir e obter os valores corretamente")
	void testSettersAndGetters() {
		Long parent = 2L;
		Plan plan = new Plan();
		StructureLevel level = new StructureLevel();
		String name = "Updated Goal";
		String responsible = "Alice Johnson";
		String manager = "Bob Brown";
		String description = "Updated description";
		double expected = 90.0;
		double minimum = 70.0;
		double maximum = 110.0;
		Double reached = 95.0;
		String periodicity = "Weekly";
		Date beginDate = new Date();
		Date endDate = new Date();

		goalDTO.setParent(parent);
		goalDTO.setPlan(plan);
		goalDTO.setLevel(level);
		goalDTO.setName(name);
		goalDTO.setResponsible(responsible);
		goalDTO.setManager(manager);
		goalDTO.setDescription(description);
		goalDTO.setExpected(expected);
		goalDTO.setMinimum(minimum);
		goalDTO.setMaximum(maximum);
		goalDTO.setReached(reached);
		goalDTO.setPeriodicity(periodicity);
		goalDTO.setBeginDate(beginDate);
		goalDTO.setEndDate(endDate);

		assertEquals(parent, goalDTO.getParent());
		assertEquals(plan, goalDTO.getPlan());
		assertEquals(level, goalDTO.getLevel());
		assertEquals(name, goalDTO.getName());
		assertEquals(responsible, goalDTO.getResponsible());
		assertEquals(manager, goalDTO.getManager());
		assertEquals(description, goalDTO.getDescription());
		assertEquals(expected, goalDTO.getExpected());
		assertEquals(minimum, goalDTO.getMinimum());
		assertEquals(maximum, goalDTO.getMaximum());
		assertEquals(reached, goalDTO.getReached());
		assertEquals(periodicity, goalDTO.getPeriodicity());
		assertEquals(beginDate, goalDTO.getBeginDate());
		assertEquals(endDate, goalDTO.getEndDate());
	}
}
