
package org.forpdi.core.csvs;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.forpdi.planning.fields.budget.BudgetElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CsvBudgetExportTest {

	@InjectMocks
	private CsvBudgetExport csvBudgetExport;
	@Mock
	StringBuilder stringBuilder;
	@Mock
	private CsvExportHelper helper;

	@Mock
	private BudgetElement budgetElement;

	private OutputStream outputStream;

	@BeforeEach
	void setUp() {
		outputStream = new ByteArrayOutputStream();
	}

	@Test
	@DisplayName("Dado uma lista de orçamentos e um OutputStream, quando exportamos o CSV, então o conteúdo gerado deve ser correto")
	void testExportBudgetCSVListOfBudgetsNotNull() throws IOException {
		List<BudgetElement> budgets = List.of(budgetElement);

		when(helper.getCsvDelimiter()).thenReturn(';');
		when(budgetElement.getSubAction()).thenReturn("Ação 1");
		when(budgetElement.getBudgetLoa()).thenReturn(10000.0);
		when(budgetElement.getBalanceAvailable()).thenReturn(5000.0);
		when(budgetElement.getLinkedObjects()).thenReturn(5L);

		csvBudgetExport.exportBudgetCSV(budgets, outputStream);

		String expectedCsv = "Ação orçamentária;Orçamento LOA;Saldo disponível;Quantidade de objetivos vinculados\n" +
			"AÇÃO 1;\"R$10.000,00\";\"R$5.000,00\";5\n";
		assertEquals(expectedCsv, outputStream.toString());
	}

	@Test
	void testExportBudgetCSVNullListOfBudgets() throws IOException {
		List<BudgetElement> budgets = List.of(budgetElement);

		when(helper.getCsvDelimiter()).thenReturn(';');
		when(budgetElement.getSubAction()).thenReturn("");
		when(budgetElement.getBudgetLoa()).thenReturn(0D);
		when(budgetElement.getBalanceAvailable()).thenReturn(0D);
		when(budgetElement.getLinkedObjects()).thenReturn(0L);

		csvBudgetExport.exportBudgetCSV(budgets, outputStream);

		String expectedCsv = "Ação orçamentária;Orçamento LOA;Saldo disponível;Quantidade de objetivos vinculados\n" +
			";\"R$0,00\";\"R$0,00\";0\n";
		assertEquals(expectedCsv, outputStream.toString());
	}
}
