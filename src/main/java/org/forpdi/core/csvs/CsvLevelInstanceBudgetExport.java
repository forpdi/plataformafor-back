package org.forpdi.core.csvs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.forpdi.planning.fields.budget.Budget;
import org.forpdi.planning.fields.budget.BudgetBS;
import org.forpdi.planning.fields.budget.BudgetElement;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CsvLevelInstanceBudgetExport {
	@Autowired private CsvExportHelper helper;
	@Autowired private BudgetBS budgetBS;

	public void exportLevelInstaceBudgets(StructureLevelInstance levelInstance, OutputStream outputStream) throws IOException {
		List<Budget> budgets = this.budgetBS.listBudgetByLevelInstance(levelInstance);
		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
		NumberFormat currencyFormater = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

		final String header = (
			helper.fromStringToCsvFieldFormat("Ação Orçamentária")
			+ helper.fromStringToCsvFieldFormat("Nome")
			+ helper.fromStringToCsvFieldFormat("Orçamento LOA")
			+ helper.fromStringToCsvFieldFormat("Saldo disponível")
			+ helper.fromStringToCsvFieldFormat("Empenhado")
			+ helper.fromStringToCsvFieldFormat("Realizado") + "\n"
		);

		writer.write(header);

		for (Budget budget : budgets) {
			BudgetElement budgetElement = budget.getBudgetElement();

			final String commited = currencyFormater.format(budget.getCommitted());
			final String realized = currencyFormater.format(budget.getRealized());
			final String budgetLoa = currencyFormater.format(budgetElement.getBudgetLoa());
			final String balanceAvailable = currencyFormater.format(budgetElement.getBalanceAvailable());

			final String row = (
				helper.fromStringToCsvFieldFormat(budget.getSubAction())
				+ helper.fromStringToCsvFieldFormat(budget.getName())
				+ helper.fromStringToCsvFieldFormat(budgetLoa)
				+ helper.fromStringToCsvFieldFormat(balanceAvailable)
				+ helper.fromStringToCsvFieldFormat(commited)
				+ helper.fromStringToCsvFieldFormat(realized)
			);

			writer.write(row + '\n');
		}

		writer.flush();
		writer.close();
		outputStream.close();
	}

}
