package org.forpdi.planning.document;

import org.forpdi.planning.plan.PlanMacro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

	private Document document;
	private PlanMacro mockPlan;

	@BeforeEach
	void setUp() {
		document = new Document();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -2);
		document.setCreation(calendar.getTime());
		mockPlan = Mockito.mock(PlanMacro.class);
	}

	@Test
	@DisplayName("Dado um Document, quando o título é definido, então o título deve ser corretamente armazenado")
	void testSetAndGetTitle() {
		String title = "Plano de Ação";
		document.setTitle(title);

		assertEquals(title, document.getTitle(), "O título deve ser corretamente retornado");
	}

	@Test
	@DisplayName("Dado um Document, quando a descrição é definida, então a descrição deve ser corretamente armazenada")
	void testSetAndGetDescription() {
		String description = "Descrição detalhada do documento";
		document.setDescription(description);

		assertEquals(description, document.getDescription(), "A descrição deve ser corretamente retornada");
	}

	@Test
	@DisplayName("Dado um Document, quando o PlanMacro é associado, então o PlanMacro deve ser corretamente retornado")
	void testSetAndGetPlan() {
		document.setPlan(mockPlan);

		assertEquals(mockPlan, document.getPlan(), "O PlanMacro associado deve ser corretamente retornado");
	}

	@Test
	@DisplayName("Dado um Document, quando a data de criação não for configurada, então a data de criação deve ser a data atual")
	void testGetCreation_Default() {
		Date creationDate = document.getCreation();

		assertNotNull(creationDate, "A data de criação não deve ser nula");
		assertTrue(creationDate.before(new Date()), "A data de criação deve ser uma data no passado");
	}

	@Test
	@DisplayName("Dado um Document, quando a data de criação é alterada, então a data de criação deve ser a data fornecida")
	void testSetCreation() {
		Date newCreationDate = new Date(1672531199000L);

		document.setCreation(newCreationDate);

		assertEquals(newCreationDate, document.getCreation(), "A data de criação deve ser corretamente alterada");
	}

	@Test
	@DisplayName("Dado um Document, quando o exportPlanMacroId é definido, então o valor deve ser corretamente armazenado e retornado")
	void testSetAndGetExportPlanMacroId() {
		Long exportPlanMacroId = 123L;
		document.setExportPlanMacroId(exportPlanMacroId);

		assertEquals(exportPlanMacroId, document.getExportPlanMacroId(), "O exportPlanMacroId deve ser corretamente retornado");
	}
}
