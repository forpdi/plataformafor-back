package org.forpdi.core.csvs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CsvPlanExport {

	@Autowired private CsvExportHelper helper;
	@Autowired CsvGoalExport csvGoalsExport;
	@Autowired private StructureBS structureBS;

	public void exportCSV(List<Plan> plans, OutputStream outputStream) throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
		
		String planMacroString = getPlanMacroCSV(plans.get(0).getParent());
		writer.write(planMacroString + "\n");

		String actionPlansString = getActionPlansCSV(plans);
		writer.write(actionPlansString + "\n");

		List<StructureLevelInstance> levelInstances = plans
			.stream()
			.flatMap(plan -> structureBS.listAllLevelInstanceByPlan(plan, false).stream())
			.collect(Collectors.toList());

		csvGoalsExport.writeLevelInstancesCSV(levelInstances, null, true, writer);

		writer.flush();
		writer.close();
		outputStream.close();
	}

	private String getPlanMacroCSV(PlanMacro planMacro) {
		final String header = (
			helper.fromStringToCsvFieldFormat("Id")
			+ helper.fromStringToCsvFieldFormat("Plano Macro")
			+ helper.fromStringToCsvFieldFormat("Data de Início")
			+ helper.fromStringToCsvFieldFormat("Data de Término")
			+ helper.fromStringToCsvFieldFormat("Descrição")
			+ "\n"
		);

		final String data = (
			helper.fromStringToCsvFieldFormat(Long.toString(planMacro.getId()))
			+ helper.fromStringToCsvFieldFormat(Optional.ofNullable(planMacro.getName()).orElse(""))
			+ helper.fromStringToCsvFieldFormat(Optional.ofNullable(planMacro.getBegin().toString()).orElse(""))
			+ helper.fromStringToCsvFieldFormat(Optional.ofNullable(planMacro.getEnd().toString()).orElse(""))
			+ helper.fromStringToCsvFieldFormat(Optional.ofNullable(planMacro.getDescription()).orElse(""))
		);

		return header + data + "\n";
	}

	private String getActionPlansCSV(List<Plan> plans) {
		final String header = (
			helper.fromStringToCsvFieldFormat("Plano de Ação:") + "\n"
			+ helper.fromStringToCsvFieldFormat("Nome")
			+ helper.fromStringToCsvFieldFormat("Descrição do Plano")
			+ helper.fromStringToCsvFieldFormat("Data de Início")
			+ helper.fromStringToCsvFieldFormat("Data de Término")
			+ helper.fromStringToCsvFieldFormat("Estrutura do Plano")
			+ helper.fromStringToCsvFieldFormat("Rendimento") + "\n"
		);

		final StringBuffer actionPlansFinalString = new StringBuffer();

		actionPlansFinalString.append(header);

		for(Plan actionPlan: plans) {
			final String name = helper.fromStringToCsvFieldFormat(Optional.ofNullable(actionPlan.getName()).orElse(""));
			final String description = helper.fromStringToCsvFieldFormat(Optional.ofNullable(actionPlan.getDescription()).orElse(""));
			final String startDate = helper.fromStringToCsvFieldFormat(Optional.ofNullable(actionPlan.getBegin().toString()).orElse(""));
			final String endDate = helper.fromStringToCsvFieldFormat(Optional.ofNullable(actionPlan.getEnd().toString()).orElse(""));
			final String planStructure = helper.fromStringToCsvFieldFormat(Optional.ofNullable(actionPlan.getStructure().getName()).orElse(""));
			final String performance = helper.fromStringToCsvFieldFormat(helper.fromDoubleToCsvPerformanceFormat(actionPlan.getPerformance()));
			final String actionPlansValues = (name + description + startDate + endDate + planStructure + performance + "\n");
			actionPlansFinalString.append(actionPlansValues);
		}
		return actionPlansFinalString.toString();
	}
}
