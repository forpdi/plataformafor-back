package org.forpdi.planning.plan;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.forpdi.planning.structure.Structure;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlanTest {

	@Test
	@DisplayName("Testa o getter e setter de creation")
	void testCreation() {
		Plan plan = new Plan();
		Date creationDate = new Date();

		plan.setCreation(creationDate);
		assertEquals(creationDate, plan.getCreation());
	}

	@Test
	@DisplayName("Testa o getter e setter de name")
	void testName() {
		Plan plan = new Plan();
		String name = "Plano de Teste";

		plan.setName(name);
		assertEquals(name, plan.getName());
	}

	@Test
	@DisplayName("Testa o getter e setter de description")
	void testDescription() {
		Plan plan = new Plan();
		String description = "Descrição do plano de teste.";

		plan.setDescription(description);
		assertEquals(description, plan.getDescription());
	}

	@Test
	@DisplayName("Testa o getter e setter de begin")
	void testBegin() {
		Plan plan = new Plan();
		Date beginDate = new Date();

		plan.setBegin(beginDate);
		assertEquals(beginDate, plan.getBegin());
	}

	@Test
	@DisplayName("Testa o getter e setter de end")
	void testEnd() {
		Plan plan = new Plan();
		Date endDate = new Date();

		plan.setEnd(endDate);
		assertEquals(endDate, plan.getEnd());
	}

	@Test
	@DisplayName("Testa o getter e setter de structure")
	void testStructure() {
		Plan plan = new Plan();
		Structure structure = new Structure();
		structure.setId(1L);

		plan.setStructure(structure);
		assertEquals(structure, plan.getStructure());
	}

	@Test
	@DisplayName("Testa o getter e setter de parent")
	void testParent() {
		Plan plan = new Plan();
		PlanMacro parent = new PlanMacro();
		parent.setId(1L);

		plan.setParent(parent);
		assertEquals(parent, plan.getParent());
	}

	@Test
	@DisplayName("Testa o getter e setter de archived")
	void testArchived() {
		Plan plan = new Plan();
		plan.setArchived(true);
		assertTrue(plan.isArchived());

		plan.setArchived(false);
		assertFalse(plan.isArchived());
	}

	@Test
	@DisplayName("Testa o getter e setter de performance")
	void testPerformance() {
		Plan plan = new Plan();
		Double performance = 90.5;

		plan.setPerformance(performance);
		assertEquals(performance, plan.getPerformance());
	}

	@Test
	@DisplayName("Testa o getter e setter de minimumAverage")
	void testMinimumAverage() {
		Plan plan = new Plan();
		Double minimumAverage = 75.0;

		plan.setMinimumAverage(minimumAverage);
		assertEquals(minimumAverage, plan.getMinimumAverage());
	}

	@Test
	@DisplayName("Testa o getter e setter de maximumAverage")
	void testMaximumAverage() {
		Plan plan = new Plan();
		Double maximumAverage = 100.0;

		plan.setMaximumAverage(maximumAverage);
		assertEquals(maximumAverage, plan.getMaximumAverage());
	}

	@Test
	@DisplayName("Testa o getter e setter de updated")
	void testUpdated() {
		Plan plan = new Plan();
		plan.setUpdated(true);
		assertTrue(plan.isUpdated());

		plan.setUpdated(false);
		assertFalse(plan.isUpdated());
	}

	@Test
	@DisplayName("Testa o getter e setter de levelInstances")
	void testLevelInstances() {
		Plan plan = new Plan();
		List<StructureLevelInstance> instances = new ArrayList<>();
		StructureLevelInstance instance = new StructureLevelInstance();
		instances.add(instance);

		plan.setLevelInstances(instances);
		assertEquals(instances, plan.getLevelInstances());
	}

	@Test
	@DisplayName("Testa o getter e setter de auxValue")
	void testAuxValue() {
		Plan plan = new Plan();
		Double auxValue = 15.0;

		plan.setAuxValue(auxValue);
		assertEquals(auxValue, plan.getAuxValue());
	}

	@Test
	@DisplayName("Testa o getter e setter de planDetailedList")
	void testPlanDetailedList() {
		Plan plan = new Plan();
		List<PlanDetailed> detailedList = new ArrayList<>();
		PlanDetailed detailed = new PlanDetailed();
		detailedList.add(detailed);

		plan.setPlanDetailedList(detailedList);
		assertEquals(detailedList, plan.getPlanDetailedList());
	}

	@Test
	@DisplayName("Testa o getter e setter de haveSons")
	void testHaveSons() {
		Plan plan = new Plan();
		plan.setHaveSons(true);
		assertTrue(plan.isHaveSons());

		plan.setHaveSons(false);
		assertFalse(plan.isHaveSons());
	}

	@Test
	@DisplayName("Testa o getter e setter de exportStructureId")
	void testExportStructureId() {
		Plan plan = new Plan();
		Long exportStructureId = 123L;

		plan.setExportStructureId(exportStructureId);
		assertEquals(exportStructureId, plan.getExportStructureId());
	}

	@Test
	@DisplayName("Testa o getter e setter de exportPlanMacroId")
	void testExportPlanMacroId() {
		Plan plan = new Plan();
		Long exportPlanMacroId = 456L;

		plan.setExportPlanMacroId(exportPlanMacroId);
		assertEquals(exportPlanMacroId, plan.getExportPlanMacroId());
	}

}
