package org.forpdi.core.csvs;

import static org.mockito.Mockito.*;

import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Teste da classe CsvActionPlanExport")
class CsvActionPlanExportTest {

	@Mock
	private CsvExportHelper helper;

	@InjectMocks
	private CsvActionPlanExport csvExporter;

	public CsvActionPlanExportTest() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Teste de exportação de planos de ação para CSV")
	void testExportLevelInstanceActionPlanCSV() throws IOException {
		ActionPlan actionPlan = mock(ActionPlan.class);
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date beginDate = new Date();
		Date endDate = new Date();

		when(actionPlan.isChecked()).thenReturn(true);
		when(actionPlan.isNotChecked()).thenReturn(false);
		when(actionPlan.getDescription()).thenReturn("Descrição do Plano de Ação");
		when(actionPlan.getUserResponsibleName()).thenReturn("João Silva");
		when(actionPlan.getBegin()).thenReturn(beginDate);
		when(actionPlan.getEnd()).thenReturn(endDate);

		when(helper.fromStringToCsvFieldFormat(anyString())).thenAnswer(invocation -> invocation.getArgument(0) + ";");

		List<ActionPlan> actionPlans = List.of(actionPlan);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		csvExporter.exportLevelInstaceActionPlanCSV(actionPlans, outputStream);

		String expectedHeader = "Planos de Ação;\nStatus;Descrição;Responsável Técnico;Início;Término;\n";
		String expectedRow = "Concluído;Descrição do Plano de Ação;João Silva;"
			+ formatter.format(beginDate) + ";"
			+ formatter.format(endDate) + ";\n";

		String csvOutput = outputStream.toString("UTF-8");

		assertTrue(csvOutput.startsWith(expectedHeader), "O CSV deve começar com o cabeçalho correto.");
		assertTrue(csvOutput.contains(expectedRow), "O CSV deve conter a linha formatada corretamente.");
	}

	@Test
	@DisplayName("Teste de exportação com lista de planos de ação vazia")
	void testExportLevelInstanceActionPlanCSVEmptyList() throws IOException {
		List<ActionPlan> actionPlans = List.of();

		when(helper.fromStringToCsvFieldFormat(anyString())).thenAnswer(invocation -> invocation.getArgument(0) + ";");

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		csvExporter.exportLevelInstaceActionPlanCSV(actionPlans, outputStream);

		String expectedHeader = "Planos de Ação;\nStatus;Descrição;Responsável Técnico;Início;Término;\n";
		String csvOutput = outputStream.toString("UTF-8");

		assertEquals(expectedHeader, csvOutput, "O CSV deve conter apenas o cabeçalho quando a lista está vazia.");
	}

	@Test
	@DisplayName("Teste de exportação com status 'Sem status'")
	void testExportLevelInstanceActionPlanCSVWithoutStatus() throws IOException {
		ActionPlan actionPlan = mock(ActionPlan.class);
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date beginDate = new Date();
		Date endDate = new Date();

		when(actionPlan.isChecked()).thenReturn(false);
		when(actionPlan.isNotChecked()).thenReturn(false);
		when(actionPlan.getDescription()).thenReturn("Descrição Sem Status");
		when(actionPlan.getUserResponsibleName()).thenReturn("Maria Oliveira");
		when(actionPlan.getBegin()).thenReturn(beginDate);
		when(actionPlan.getEnd()).thenReturn(endDate);

		when(helper.fromStringToCsvFieldFormat(anyString())).thenAnswer(invocation -> invocation.getArgument(0) + ";");

		List<ActionPlan> actionPlans = List.of(actionPlan);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		csvExporter.exportLevelInstaceActionPlanCSV(actionPlans, outputStream);

		String expectedRow = "Sem status;Descrição Sem Status;Maria Oliveira;"
			+ formatter.format(beginDate) + ";"
			+ formatter.format(endDate) + ";\n";

		String csvOutput = outputStream.toString("UTF-8");

		assertTrue(csvOutput.contains(expectedRow), "O CSV deve conter a linha com status 'Sem status'.");
	}
}
