package org.forpdi.core.csvs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.forpdi.planning.fields.budget.BudgetElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CsvBudgetExport {
	@Autowired private CsvExportHelper helper;
	
	public void exportBudgetCSV(List<BudgetElement> budgets, OutputStream outputStream) throws IOException {
		char csvDelimiter = helper.getCsvDelimiter();
		
		Writer oos = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
		oos.write("Ação orçamentária" + csvDelimiter + "Orçamento LOA" + csvDelimiter + "Saldo disponível"
			+ csvDelimiter + "Quantidade de objetivos vinculados" + "\n");
		for (BudgetElement budget : budgets) {
			String row = getCSVrow(budget);
			if (!row.isEmpty()) {
				oos.write(row);
			}
		}
		oos.flush();
		oos.close();
		outputStream.close();
	}

	private String getCSVrow(BudgetElement budget) {
		StringBuilder sb = new StringBuilder();
		sb.append(toCSV(budget));
		return sb.toString();
	}

	private String toCSV(BudgetElement budget) {
		char csvDelimiter = helper.getCsvDelimiter();
		
		String budgetLoa = String.format("\"R$%,.2f\"", budget.getBudgetLoa());
		String balanceAvailable = String.format("\"R$%,.2f\"", budget.getBalanceAvailable());;
		
		return budget.getSubAction().toString().toUpperCase() + csvDelimiter + budgetLoa + csvDelimiter 
			+ balanceAvailable + csvDelimiter + budget.getLinkedObjects().toString() + "\n";
	}

}
