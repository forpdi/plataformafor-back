package org.forrisco.risk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiskFilterParamsTest {

	@Test
	@DisplayName("Teste de getters e setters")
	void testRiskFilterParamsGettersAndSetters() {
		RiskFilterParams filterParams = new RiskFilterParams();

		List<Long> unitIds = Arrays.asList(1L, 2L, 3L);
		String term = "Test Term";
		List<Long> processesId = Arrays.asList(10L, 20L);
		List<Long> linkedPdiIds = Arrays.asList(100L, 200L);
		String nameOrCode = "Code123";
		String type = "TypeA";
		List<String> typologies = Arrays.asList("Typology1", "Typology2");
		List<Integer> responses = Arrays.asList(1, 2, 3);
		List<Integer> levels = Arrays.asList(4, 5, 6);
		Date startCreation = new Date();
		Date endCreation = new Date(System.currentTimeMillis() + 86400000);
		Boolean archived = true;

		filterParams.setUnitIds(unitIds);
		filterParams.setTerm(term);
		filterParams.setProcessesId(processesId);
		filterParams.setLinkedPdiIds(linkedPdiIds);
		filterParams.setNameOrCode(nameOrCode);
		filterParams.setType(type);
		filterParams.setTypologies(typologies);
		filterParams.setResponses(responses);
		filterParams.setLevels(levels);
		filterParams.setStartCreation(startCreation);
		filterParams.setEndCreation(endCreation);
		filterParams.setArchived(archived);

		assertEquals(unitIds, filterParams.getUnitIds());
		assertEquals(term, filterParams.getTerm());
		assertEquals(processesId, filterParams.getProcessesId());
		assertEquals(linkedPdiIds, filterParams.getLinkedPdiIds());
		assertEquals(nameOrCode, filterParams.getNameOrCode());
		assertEquals(type, filterParams.getType());
		assertEquals(typologies, filterParams.getTypologies());
		assertEquals(responses, filterParams.getResponses());
		assertEquals(levels, filterParams.getLevels());
		assertEquals(startCreation, filterParams.getStartCreation());
		assertEquals(endCreation, filterParams.getEndCreation());
		assertEquals(archived, filterParams.getArchived());
	}

	@Test
	@DisplayName("Teste de deserialização com JSON válido")
	public void testFromJson_validJson() {
		String json = "{\"unitIds\":[1,2,3],\"term\":\"Test\",\"responses\":[1,2]}";
		RiskFilterParams params = RiskFilterParams.fromJson(json);

		assertNotNull(params);
		assertEquals(Arrays.asList(1L, 2L, 3L), params.getUnitIds());
		assertEquals("Test", params.getTerm());
		assertEquals(Arrays.asList(1, 2), params.getResponses());
	}

	@Test
	@DisplayName("Teste de deserialização com JSON vazio")
	public void testFromJson_emptyJson() {
		String json = "";
		RiskFilterParams params = RiskFilterParams.fromJson(json);

		assertNotNull(params);
		assertNull(params.getUnitIds());
		assertNull(params.getTerm());
	}

	@Test
	@DisplayName("Teste de filtragem por unidades")
	public void testFilteringByUnits() {
		RiskFilterParams params = new RiskFilterParams();
		params.setUnitIds(Arrays.asList(1L, 2L));

		assertTrue(params.filteringByUnits());

		params.setUnitIds(null);
		assertFalse(params.filteringByUnits());
	}

	@Test
	@DisplayName("Teste de filtragem por termo")
	public void testFilteringByTerm() {
		RiskFilterParams params = new RiskFilterParams();
		params.setTerm("Test Term");

		assertTrue(params.filteringByTerm());

		params.setTerm(null);
		assertFalse(params.filteringByTerm());
	}

	@Test
	@DisplayName("Teste de filtragem por processos")
	public void testFilteringByProcesses() {
		RiskFilterParams params = new RiskFilterParams();
		params.setProcessesId(Arrays.asList(10L, 20L));

		assertTrue(params.filteringByProcesses());

		params.setProcessesId(null);
		assertFalse(params.filteringByProcesses());
	}

	@Test
	@DisplayName("Teste de filtragem por PDIs vinculados")
	public void testFilteringByLinkedPdis() {
		RiskFilterParams params = new RiskFilterParams();
		params.setLinkedPdiIds(Arrays.asList(5L, 15L));

		assertTrue(params.filteringByLinkedPdis());

		params.setLinkedPdiIds(null);
		assertFalse(params.filteringByLinkedPdis());
	}

	@Test
	@DisplayName("Teste de filtragem por nome ou código")
	public void testFilteringByNameOrCode() {
		RiskFilterParams params = new RiskFilterParams();
		params.setNameOrCode("Code123");

		assertTrue(params.filteringByNameOrCode());

		params.setNameOrCode(null);
		assertFalse(params.filteringByNameOrCode());
	}

	@Test
	@DisplayName("Teste de filtragem por tipo")
	public void testFilteringByType() {
		RiskFilterParams params = new RiskFilterParams();
		params.setType("RiskType");

		assertTrue(params.filteringByType());

		params.setType(null);
		assertFalse(params.filteringByType());
	}

	@Test
	@DisplayName("Teste de filtragem por tipologias")
	public void testFilteringByTypologies() {
		RiskFilterParams params = new RiskFilterParams();
		params.setTypologies(Arrays.asList("Type1", "Type2"));

		assertTrue(params.filteringByTypologies());

		params.setTypologies(null);
		assertFalse(params.filteringByTypologies());
	}

	@Test
	@DisplayName("Teste de filtragem por respostas")
	public void testFilteringByResponses() {
		RiskFilterParams params = new RiskFilterParams();
		params.setResponses(Arrays.asList(1, 2));

		assertTrue(params.filteringByResponses());

		params.setResponses(null);
		assertFalse(params.filteringByResponses());
	}

	@Test
	@DisplayName("Teste de filtragem por respostas inexistentes")
	public void testFilteringByNoneResponse() {
		RiskFilterParams params = new RiskFilterParams();
		params.setResponses(Arrays.asList(-1, 2));

		assertTrue(params.filteringByNoneResponse());

		params.setResponses(Arrays.asList(1, 2));
		assertFalse(params.filteringByNoneResponse());

		params.setResponses(null);
		assertFalse(params.filteringByNoneResponse());
	}

	@Test
	@DisplayName("Teste de filtragem por níveis")
	public void testFilteringByLevels() {
		RiskFilterParams params = new RiskFilterParams();
		params.setLevels(Arrays.asList(1, 2));

		assertTrue(params.filteringByLevels());

		params.setLevels(null);
		assertFalse(params.filteringByLevels());
	}

	@Test
	@DisplayName("Teste de filtragem por níveis inexistentes")
	public void testFilteringByNoneLevels() {
		RiskFilterParams params = new RiskFilterParams();
		params.setLevels(Arrays.asList(-1, 3));

		assertTrue(params.filteringByNoneLevels());

		params.setLevels(Arrays.asList(1, 2));
		assertFalse(params.filteringByNoneLevels());

		params.setLevels(null);
		assertFalse(params.filteringByNoneLevels());
	}

	@Test
	@DisplayName("Teste de filtragem por data de criação inicial")
	public void testFilteringByStartCreation() {
		RiskFilterParams params = new RiskFilterParams();
		params.setStartCreation(new Date());

		assertTrue(params.filteringByStartCreation());

		params.setStartCreation(null);
		assertFalse(params.filteringByStartCreation());
	}

	@Test
	@DisplayName("Teste de filtragem por data de criação final")
	public void testFilteringByEndCreation() {
		RiskFilterParams params = new RiskFilterParams();
		params.setEndCreation(new Date());

		assertTrue(params.filteringByEndCreation());

		params.setEndCreation(null);
		assertFalse(params.filteringByEndCreation());
	}

	@Test
	@DisplayName("Teste de filtragem por arquivamento")
	public void testFilteringByArchived() {
		RiskFilterParams params = new RiskFilterParams();
		params.setArchived(true);

		assertTrue(params.filteringByArchived());

		params.setArchived(null);
		assertFalse(params.filteringByArchived());
	}
}
