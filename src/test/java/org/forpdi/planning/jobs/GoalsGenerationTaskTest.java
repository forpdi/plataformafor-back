package org.forpdi.planning.jobs;

import org.forpdi.core.common.HibernateDAO;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.structure.StructureLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoalsGenerationTaskTest {
	@Mock
	private HibernateDAO dao;

	@Mock
	private Logger logger;

	@Spy
	@InjectMocks
	private GoalsGenerationTask goalsGenerationTask;

	GoalDTO goal;

	@BeforeEach
	void setUp() {
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
		String periodicity = "Mensal";
		Date beginDate = new Date();
		Date endDate = new Date();
		goal = new GoalDTO(parent, plan, level, name, manager, responsible, description, expected, minimum, maximum, periodicity, beginDate, endDate);
	}

	@Test
	public void testAddGoalToQueue() {
		GoalDTO goal = mock(GoalDTO.class);
		goalsGenerationTask.add(goal);

		verify(goalsGenerationTask, times(1)).add(goal);
	}

	@Test
	public void testExecuteWithEmptyQueue() {
		goalsGenerationTask.execute();
		verify(dao, never()).execute(any());
	}

	@Test
	public void testErrorLogging() {
		List<Attribute> attr = new ArrayList<>();
		Attribute attr1 = new Attribute();
		attr.add(attr1);
		attr.add(attr1);

		goalsGenerationTask.add(goal);
		doCallRealMethod().when(dao).execute(any());
		goalsGenerationTask.execute();
	}

	@Test
	public void testExecuteWithValidQueueAndAttibuteIsResponsibleField() {

		List<Attribute> attr = new ArrayList<>();
		Attribute attr1 = new Attribute();
		attr1.setType("org.forpdi.planning.attribute.types.ResponsibleField");
		attr.add(attr1);
		attr.add(attr1);
		goal.getLevel().setAttributes(attr);

		doCallRealMethod().when(dao).execute(any());
		goalsGenerationTask.add(goal);

		goalsGenerationTask.execute();
	}

	@Test
	public void testExecuteWithValidQueueAndAttibuteIsManagerField() {

		List<Attribute> attr = new ArrayList<>();
		Attribute attr1 = new Attribute();
		attr1.setType("org.forpdi.planning.attribute.types.ManagerField");
		attr.add(attr1);
		attr.add(attr1);
		goal.getLevel().setAttributes(attr);

		doCallRealMethod().when(dao).execute(any());
		goalsGenerationTask.add(goal);

		goalsGenerationTask.execute();
	}

	@Test
	public void testExecuteWithValidQueueAndAttibuteIsFinishDate() {

		List<Attribute> attr = new ArrayList<>();
		Attribute attr1 = new Attribute();
		attr1.setType("org.forpdi.planning.attribute.types.DateField");
		attr1.setFinishDate(true);
		attr.add(attr1);
		attr.add(attr1);
		goal.getLevel().setAttributes(attr);

		doCallRealMethod().when(dao).execute(any());
		goalsGenerationTask.add(goal);

		goalsGenerationTask.execute();
	}

	@Test
	public void testExecuteWithValidQueueAndAttibuteisExpectedField() {

		List<Attribute> attr = new ArrayList<>();
		Attribute attr1 = new Attribute();
		attr1.setType("org.forpdi.planning.attribute.types.NumberField");
		attr1.setExpectedField(true);
		attr.add(attr1);
		attr.add(attr1);
		goal.getLevel().setAttributes(attr);

		doCallRealMethod().when(dao).execute(any());
		goalsGenerationTask.add(goal);

		goalsGenerationTask.execute();
	}

	@Test
	public void testExecuteWithValidQueueAndAttributeIsMinimumField() {

		List<Attribute> attr = new ArrayList<>();
		Attribute attr1 = new Attribute();
		attr1.setType("org.forpdi.planning.attribute.types.NumberField");
		attr1.setMinimumField(true);
		attr.add(attr1);
		attr.add(attr1);
		goal.getLevel().setAttributes(attr);

		doCallRealMethod().when(dao).execute(any());
		goalsGenerationTask.add(goal);

		goalsGenerationTask.execute();
	}

	@Test
	public void testExecuteWithValidQueueAndAttributeIsMaximumField() {

		List<Attribute> attr = new ArrayList<>();
		Attribute attr1 = new Attribute();
		attr1.setType("org.forpdi.planning.attribute.types.NumberField");
		attr1.setMaximumField(true);
		attr.add(attr1);
		attr.add(attr1);
		goal.getLevel().setAttributes(attr);

		doCallRealMethod().when(dao).execute(any());
		goalsGenerationTask.add(goal);

		goalsGenerationTask.execute();
	}

	@Test
	public void testExecuteWithValidQueueAndAttributeIsReachedField() {

		List<Attribute> attr = new ArrayList<>();
		Attribute attr1 = new Attribute();
		attr1.setType("org.forpdi.planning.attribute.types.NumberField");
		attr1.setReachedField(true);
		attr.add(attr1);
		attr.add(attr1);
		goal.getLevel().setAttributes(attr);
		goal.setReached(75.5);
		doCallRealMethod().when(dao).execute(any());
		goalsGenerationTask.add(goal);

		goalsGenerationTask.execute();
	}

	@Test
	public void testExecuteWithValidQueueAndAttributeIsReachedFieldAndReachedValueNull() {

		List<Attribute> attr = new ArrayList<>();
		Attribute attr1 = new Attribute();
		attr1.setType("org.forpdi.planning.attribute.types.NumberField");
		attr1.setReachedField(true);
		attr.add(attr1);
		attr.add(attr1);
		goal.getLevel().setAttributes(attr);
		goal.setReached(null);
		doCallRealMethod().when(dao).execute(any());
		goalsGenerationTask.add(goal);

		goalsGenerationTask.execute();
	}

	@Test
	public void testExecuteWithValidQueueAndNonexistentFieldType() {

		List<Attribute> attr = new ArrayList<>();
		Attribute attr1 = new Attribute();
		attr1.setType(null);
		attr1.setType("org.forpdi.planning.attribute.types.NonexistentField");
		attr.add(attr1);
		attr.add(attr1);
		goal.getLevel().setAttributes(attr);
		goal.setReached(75.5);
		doCallRealMethod().when(dao).execute(any());
		goalsGenerationTask.add(goal);

		goalsGenerationTask.execute();
	}
}
