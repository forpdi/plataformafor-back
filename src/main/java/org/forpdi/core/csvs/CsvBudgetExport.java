package org.forpdi.core.csvs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import org.forpdi.planning.fields.budget.BudgetElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CsvBudgetExport {

    @Autowired private CsvExportHelper helper;

    private static final DecimalFormat NUMBER_FORMATTER;

    static {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        NUMBER_FORMATTER = new DecimalFormat(pattern, symbols);
    }

    public void exportBudgetCSV(List<BudgetElement> budgets, OutputStream outputStream) throws IOException {
        char csvDelimiter = helper.getCsvDelimiter();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {
            writer.write("Ação orçamentária" + csvDelimiter + "Orçamento LOA" + csvDelimiter + "Saldo disponível"
                + csvDelimiter + "Quantidade de objetivos vinculados" + "\n");
            
            for (BudgetElement budget : budgets) {
                String row = toCSV(budget, csvDelimiter);
                if (!row.isEmpty()) {
                    writer.write(row);
                }
            }
            writer.flush();
        }
    }

    private String toCSV(BudgetElement budget, char csvDelimiter) {
        String budgetLoa = NUMBER_FORMATTER.format(budget.getBudgetLoa());
        String balanceAvailable = NUMBER_FORMATTER.format(budget.getBalanceAvailable());

        String formattedBudgetLoa = "\"R$" + budgetLoa + "\"";
        String formattedBalanceAvailable = "\"R$" + balanceAvailable + "\"";

        return budget.getSubAction().toUpperCase() + csvDelimiter + formattedBudgetLoa + csvDelimiter
            + formattedBalanceAvailable + csvDelimiter + budget.getLinkedObjects() + "\n";
    }
}