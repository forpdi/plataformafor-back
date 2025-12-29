package org.forpdi.core.csvs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.forpdi.planning.fields.budget.BudgetElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CsvBudgetExportTest {

    @Mock
    private CsvExportHelper helper;

    @InjectMocks
    private CsvBudgetExport exporter;

    private static BudgetElement budget(String subAction, double loa, double balance, int linked) {
        BudgetElement b = org.mockito.Mockito.mock(BudgetElement.class);
        org.mockito.Mockito.when(b.getSubAction()).thenReturn(subAction);
        org.mockito.Mockito.when(b.getBudgetLoa()).thenReturn(loa);
        org.mockito.Mockito.when(b.getBalanceAvailable()).thenReturn(balance);
        org.mockito.Mockito.when(b.getLinkedObjects()).thenReturn((long) linked);
        return b;
    }

    @Test
    @DisplayName("exportBudgetCSV: gera cabeçalho e linhas com ';' e formato pt-BR (R$1.234,56)")
    void export_withSemicolonDelimiter_ptBRFormatting() throws IOException {
        when(helper.getCsvDelimiter()).thenReturn(';');

        var b1 = budget("subação 001", 1234.56, 78.9, 3);
        var b2 = budget("ação X", 0.0, 1000000.5, 7);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        exporter.exportBudgetCSV(List.of(b1, b2), out);

        String csv = out.toString(StandardCharsets.UTF_8);
        String expectedHeader = "Ação orçamentária;Orçamento LOA;Saldo disponível;Quantidade de objetivos vinculados\n";
        String expectedRow1 = "SUBAÇÃO 001;\"R$1.234,56\";\"R$78,90\";3\n";
        String expectedRow2 = "AÇÃO X;\"R$0,00\";\"R$1.000.000,50\";7\n";

        assertEquals(expectedHeader + expectedRow1 + expectedRow2, csv);
    }

    @Test
    @DisplayName("exportBudgetCSV: gera apenas cabeçalho quando lista está vazia")
    void export_emptyList_onlyHeader() throws IOException {
        when(helper.getCsvDelimiter()).thenReturn(',');

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.exportBudgetCSV(List.of(), out);

        String csv = out.toString(StandardCharsets.UTF_8);
        String expectedHeader = "Ação orçamentária,Orçamento LOA,Saldo disponível,Quantidade de objetivos vinculados\n";
        assertEquals(expectedHeader, csv);
    }
}
