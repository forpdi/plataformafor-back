package org.forpdi.planning.document;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.forpdi.planning.fields.schedule.Schedule;
import org.forpdi.planning.fields.table.TableFields;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DocumentAttributeTest {

	@Test
	@DisplayName("Test setting and getting name")
	void testSetAndGetName() {
		DocumentAttribute attribute = new DocumentAttribute();
		attribute.setName("Test Name");
		assertEquals("Test Name", attribute.getName());
	}

	@Test
	@DisplayName("Test setting and getting section")
	void testSetAndGetSection() {
		DocumentAttribute attribute = new DocumentAttribute();
		DocumentSection section = new DocumentSection();
		attribute.setSection(section);
		assertEquals(section, attribute.getSection());
	}

	@Test
	@DisplayName("Test setting and getting type")
	void testSetAndGetType() {
		DocumentAttribute attribute = new DocumentAttribute();
		attribute.setType("Text");
		assertEquals("Text", attribute.getType());
	}

	@Test
	@DisplayName("Test setting and getting value")
	void testSetAndGetValue() {
		DocumentAttribute attribute = new DocumentAttribute();
		attribute.setValue("Sample Value");
		assertEquals("Sample Value", attribute.getValue());
	}

	@Test
	@DisplayName("Test setting and getting value as number")
	void testSetAndGetValueAsNumber() {
		DocumentAttribute attribute = new DocumentAttribute();
		attribute.setValueAsNumber(42.0);
		assertEquals(42.0, attribute.getValueAsNumber());
	}

	@Test
	@DisplayName("Test setting and getting value as date")
	void testSetAndGetValueAsDate() {
		DocumentAttribute attribute = new DocumentAttribute();
		Date date = new Date();
		attribute.setValueAsDate(date);
		assertEquals(date, attribute.getValueAsDate());
	}

	@Test
	@DisplayName("Test setting and getting sequence")
	void testSetAndGetSequence() {
		DocumentAttribute attribute = new DocumentAttribute();
		attribute.setSequence(10);
		assertEquals(10, attribute.getSequence());
	}

	@Test
	@DisplayName("Test setting and checking required flag")
	void testSetAndIsRequired() {
		DocumentAttribute attribute = new DocumentAttribute();
		attribute.setRequired(true);
		assertTrue(attribute.isRequired());
	}

	@Test
	@DisplayName("Test setting and getting schedule")
	void testSetAndGetSchedule() {
		DocumentAttribute attribute = new DocumentAttribute();
		Schedule schedule = new Schedule();
		attribute.setSchedule(schedule);
		assertEquals(schedule, attribute.getSchedule());
	}

	@Test
	@DisplayName("Test setting and getting table fields")
	void testSetAndGetTableFields() {
		DocumentAttribute attribute = new DocumentAttribute();
		TableFields tableFields = new TableFields();
		attribute.setTableFields(tableFields);
		assertEquals(tableFields, attribute.getTableFields());
	}

	@Test
	@DisplayName("Test setting and getting select plans")
	void testSetAndGetSelectPlans() {
		DocumentAttribute attribute = new DocumentAttribute();
		List<Plan> plans = new ArrayList<>();
		attribute.setSelectPlans(plans);
		assertEquals(plans, attribute.getSelectPlans());
	}

	@Test
	@DisplayName("Test setting and getting strategic objectives")
	void testSetAndGetStrategicObjectives() {
		DocumentAttribute attribute = new DocumentAttribute();
		List<StructureLevelInstance> objectives = new ArrayList<>();
		attribute.setStrategicObjectives(objectives);
		assertEquals(objectives, attribute.getStrategicObjectives());
	}

	@Test
	@DisplayName("Test setting and getting export document section ID")
	void testSetAndGetExportDocumentSectionId() {
		DocumentAttribute attribute = new DocumentAttribute();
		attribute.setExportDocumentSectionId(100L);
		assertEquals(100L, attribute.getExportDocumentSectionId());
	}
}
