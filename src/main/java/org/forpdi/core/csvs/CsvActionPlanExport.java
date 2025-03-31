package org.forpdi.core.csvs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CsvActionPlanExport {
	@Autowired private CsvExportHelper helper;
	
	public void exportLevelInstaceActionPlanCSV(List<ActionPlan> actionPlans, OutputStream outputStream) throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
		String header = (
			helper.fromStringToCsvFieldFormat("Planos de Ação") + '\n'
			+ helper.fromStringToCsvFieldFormat("Status")
			+ helper.fromStringToCsvFieldFormat("Descrição")
			+ helper.fromStringToCsvFieldFormat("Responsável Técnico")
			+ helper.fromStringToCsvFieldFormat("Início")
			+ helper.fromStringToCsvFieldFormat("Término")
		);
		writer.write(header + '\n');
		
		for(ActionPlan actionPlan : actionPlans) {
			String status = "";
			
			Boolean checked = Optional.ofNullable(actionPlan.isChecked()).orElse(false);
			Boolean notChecked = Optional.ofNullable(actionPlan.isNotChecked()).orElse(false);
			
			if (checked && !notChecked) {
				status = "Concluído";
			} else if (!checked && notChecked) {
				status = "Não concluído";
			} else if (!checked && !notChecked) {
				status= "Sem status";
			}
			
			String user = actionPlan.getUserResponsibleName();

			String row = (
				helper.fromStringToCsvFieldFormat(status)
				+ helper.fromStringToCsvFieldFormat(actionPlan.getDescription())
				+ helper.fromStringToCsvFieldFormat(user)
				+ helper.fromStringToCsvFieldFormat(formatter.format(actionPlan.getBegin()))
				+ helper.fromStringToCsvFieldFormat(formatter.format(actionPlan.getEnd()))
			);
			
			writer.write(row + '\n');
		}
		
		writer.flush();
		writer.close();
		outputStream.close();
	}

}
