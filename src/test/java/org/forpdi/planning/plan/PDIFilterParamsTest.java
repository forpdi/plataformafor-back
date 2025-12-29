package org.forpdi.planning.plan;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PDIFilterParamsTest {

	private PDIFilterParams params;

	@BeforeEach
	public void setUp() {
		params = new PDIFilterParams();
	}

	@Test
	@DisplayName("Testa o parse correto da data inicial como objeto Date")
	void testGetStartDateAsDate() throws ParseException {
		String validDate = "19/12/2024 15:30:00";
		params.setStartDate(validDate);

		Date date = params.getStartDateAsDate();
		assertNotNull(date);
		assertEquals(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(validDate), date);
	}

	@Test
	@DisplayName("Testa o parse correto da data final como objeto Date")
	void testGetEndDateAsDate() throws ParseException {
		String validDate = "20/12/2024 15:30:00";
		params.setEndDate(validDate);

		Date date = params.getEndDateAsDate();
		assertNotNull(date);
		assertEquals(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(validDate), date);
	}

	@Test
	@DisplayName("Testa se o método retorna null para data inicial inválida")
	void testGetStartDateAsDateInvalid() throws ParseException {
		params.setStartDate("invalid-date");
		assertThrows(ParseException.class, params::getStartDateAsDate);
	}

	@Test
	@DisplayName("Testa se o método retorna null para data final inválida")
	void testGetEndDateAsDateInvalid() throws ParseException {
		params.setEndDate("invalid-date");
		assertThrows(ParseException.class, params::getEndDateAsDate);
	}

	@Test
	@DisplayName("Testa se o filtro por macro está ativo quando o macro está definido")
	void testFilteringByMacro() {
		params.setMacro(1L);
		assertTrue(params.filteringByMacro());
	}

	@Test
	@DisplayName("Testa se o filtro por macro está inativo quando o macro não está definido")
	void testFilteringByMacroInactive() {
		params.setMacro(null);
		assertFalse(params.filteringByMacro());
	}

	@Test
	@DisplayName("Testa se o filtro por plano está ativo quando o plano está definido")
	void testFilteringByPlan() {
		params.setPlan(1L);
		assertTrue(params.filteringByPlan());
	}

	@Test
	@DisplayName("Testa se o filtro por plano está inativo quando o plano não está definido")
	void testFilteringByPlanInactive() {
		params.setPlan(null);
		assertFalse(params.filteringByPlan());
	}

	@Test
	@DisplayName("Testa se o filtro por nível de instância está ativo quando o nível está definido")
	void testFilteringByLevelInstance() {
		params.setLevelInstance(1L);
		assertTrue(params.filteringByLevelInstance());
	}

	@Test
	@DisplayName("Testa se o filtro por nível de instância está inativo quando o nível não está definido")
	void testFilteringByLevelInstanceInactive() {
		params.setLevelInstance(null);
		assertFalse(params.filteringByLevelInstance());
	}

	@Test
	@DisplayName("Testa se o filtro por ID de meta está ativo quando a meta está definida")
	void testFilteringByGoalId() {
		params.setGoalId(1L);
		assertTrue(params.filteringByGoalId());
	}

	@Test
	@DisplayName("Testa se o filtro por ID de meta está inativo quando a meta não está definida")
	void testFilteringByGoalIdInactive() {
		params.setGoalId(null);
		assertFalse(params.filteringByGoalId());
	}

	@Test
	@DisplayName("Testa se o filtro por data inicial está ativo quando a data está definida")
	void testFilteringByStartDate() {
		params.setStartDate("19/12/2024 15:30:00");
		assertTrue(params.filteringByStartDate());
	}

	@Test
	@DisplayName("Testa se o filtro por data inicial está inativo quando a data não está definida")
	void testFilteringByStartDateInactive() {
		params.setStartDate(null);
		assertFalse(params.filteringByStartDate());
	}

	@Test
	@DisplayName("Testa se o filtro por data final está ativo quando a data está definida")
	void testFilteringByEndDate() {
		params.setEndDate("20/12/2024 15:30:00");
		assertTrue(params.filteringByEndDate());
	}

	@Test
	@DisplayName("Testa se o filtro por data final está inativo quando a data não está definida")
	void testFilteringByEndDateInactive() {
		params.setEndDate(null);
		assertFalse(params.filteringByEndDate());
	}

	@Test
	@DisplayName("Testa se o filtro por status da meta está ativo quando o status está definido")
	void testFilteringByGoalStatus() {
		params.setGoalStatus(true);
		assertTrue(params.filteringByGoalStatus());
	}

	@Test
	@DisplayName("Testa se o filtro por status da meta está inativo quando o status não está definido")
	void testFilteringByGoalStatusInactive() {
		params.setGoalStatus(null);
		assertFalse(params.filteringByGoalStatus());
	}

	@Test
	@DisplayName("Testa validação de parâmetros com exceção lançada")
	void testValidateParamsException() {
		params.setMacro(null);
		params.setPlan(null);
		params.setLevelInstance(null);

		Exception exception = assertThrows(IllegalArgumentException.class, params::validateParams);
		assertEquals("Você deve selecionar um plano", exception.getMessage());
	}

	@Test
	@DisplayName("Testa validação de parâmetros com todos os valores definidos")
	void testValidateParamsValid() {
		params.setMacro(1L);
		params.setPlan(1L);
		params.setLevelInstance(1L);

		assertDoesNotThrow(params::validateParams);
	}

	@Test
	void test_get_macro_returns_set_value() {
		PDIFilterParams params = new PDIFilterParams();
		Long expectedMacro = 123L;
		params.setMacro(expectedMacro);

		Long actualMacro = params.getMacro();

		assertEquals(expectedMacro, actualMacro);
	}

	@Test
	void test_returns_set_plan_value() {
		PDIFilterParams params = new PDIFilterParams();
		Long expectedPlan = 123L;
		params.setPlan(expectedPlan);

		Long actualPlan = params.getPlan();

		assertEquals(expectedPlan, actualPlan);
	}

	@Test
	void test_returns_level_instance_when_set() {
		PDIFilterParams params = new PDIFilterParams();
		Long expectedLevelInstance = 123L;
		params.setLevelInstance(expectedLevelInstance);

		Long actualLevelInstance = params.getLevelInstance();

		assertEquals(expectedLevelInstance, actualLevelInstance);
	}

	@Test
	void test_returns_valid_goal_id() {
		PDIFilterParams params = new PDIFilterParams();
		Long expectedGoalId = 123L;
		params.setGoalId(expectedGoalId);

		Long actualGoalId = params.getGoalId();

		assertEquals(expectedGoalId, actualGoalId);
	}

	@Test
	void test_returns_valid_date_string() {
		PDIFilterParams params = new PDIFilterParams();
		String validDate = "01/01/2023 10:30:00";
		params.setStartDate(validDate);

		String result = params.getStartDate();

		assertEquals(validDate, result);
	}

	@Test
	void test_returns_valid_end_date_string() {
		PDIFilterParams params = new PDIFilterParams();
		String expectedDate = "01/01/2024 10:30:00";
		params.setEndDate(expectedDate);

		String actualDate = params.getEndDate();

		assertEquals(expectedDate, actualDate);
	}

	@Test
	public void test_returns_true_when_goal_status_set_to_true() {
		PDIFilterParams params = new PDIFilterParams();
		params.setGoalStatus(true);

		Boolean result = params.getGoalStatus();

		assertTrue(result);
	}

	@Test
	public void test_returns_current_page_value_for_positive_integer() {
		PDIFilterParams params = new PDIFilterParams();
		Integer expectedPage = 5;
		params.setPage(expectedPage);

		Integer actualPage = params.getPage();

		assertEquals(expectedPage, actualPage);
	}

	@Test
	void test_set_positive_page_value() {
		PDIFilterParams params = new PDIFilterParams();
		Integer expectedPage = 5;

		params.setPage(expectedPage);

		assertEquals(expectedPage, params.getPage());
	}

	@Test
	void test_returns_set_positive_pagesize() {
		PDIFilterParams params = new PDIFilterParams();
		Integer expectedPageSize = 10;
		params.setPageSize(expectedPageSize);

		Integer actualPageSize = params.getPageSize();

		assertEquals(expectedPageSize, actualPageSize);
	}

	@Test
	void test_returns_positive_progress_status() {
		PDIFilterParams params = new PDIFilterParams();
		Integer expectedStatus = 75;
		params.setProgressStatus(expectedStatus);

		Integer actualStatus = params.getProgressStatus();

		assertEquals(expectedStatus, actualStatus);
	}

	@Test
	void test_set_positive_progress_status() {
		PDIFilterParams params = new PDIFilterParams();
		Integer expectedStatus = 50;

		params.setProgressStatus(expectedStatus);

		assertEquals(expectedStatus, params.getProgressStatus());
	}

	@Test
	void test_set_null_progress_status() {
		PDIFilterParams params = new PDIFilterParams();
		Integer nullStatus = null;

		params.setProgressStatus(nullStatus);

		assertNull(params.getProgressStatus());
	}


}
