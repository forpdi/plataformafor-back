package org.forpdi.core.csvs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.Util;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.ActionPlanField;
import org.forpdi.planning.attribute.types.AttachmentField;
import org.forpdi.planning.attribute.types.BudgetField;
import org.forpdi.planning.attribute.types.ManagerField;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.forpdi.planning.fields.budget.Budget;
import org.forpdi.planning.fields.budget.BudgetDTO;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureHelper;
import org.forpdi.planning.structure.StructureLevel;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CsvGoalExport {
	@Autowired private CsvExportHelper helper;
	@Autowired private StructureBS structureBS;
	@Autowired private StructureHelper structureHelper;
	@Autowired	private FieldsBS fieldsBS;
	@Autowired private UserBS userBS;

	// Retorna uma String com os valores de um campo do tipo ActionPlan
	public void exportGoalsCSV(List<StructureLevelInstance> levelInstances, OutputStream outputStream) throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
		
		writeLevelInstancesCSV(
			levelInstances,
			Arrays.asList("Responsável Técnico", "Vencimento", "Esperado", "Alcançado", "Gestor"),
			false,
			writer
		);
		
		writer.flush();
		writer.close();
		outputStream.close();
	}
	
	public void writeLevelInstancesCSV(List<StructureLevelInstance> structureLevelInstanceList,
			List<String> attributesToExport, Boolean exportParentName, Writer writer) throws IOException {
		Map<StructureLevel, List<StructureLevelInstance>> structureLevelMap = getStructureLevelInstanceMap(
				structureLevelInstanceList);
		Map<Long, StructureLevelInstance> structureLevelInstanceMap = Util.generateMap(structureLevelInstanceList,
				sli -> sli.getId());

		for (Map.Entry<StructureLevel, List<StructureLevelInstance>> entry : structureLevelMap.entrySet()) {
			final StructureLevel currentStructureLevel = entry.getKey();
			final List<StructureLevelInstance> currentStructureLevelInstances = entry.getValue();
			final List<Attribute> attributesList = structureBS.retrieveLevelAttributes(currentStructureLevel,
					attributesToExport);

			final String levelInstanceHeader = getLevelInstanceHeaders(
					currentStructureLevel,
					attributesList,
					currentStructureLevelInstances.get(0),
					exportParentName);
			writer.write(levelInstanceHeader);

			Map<StructureLevelInstance, Map<Attribute, String>> levelInstancesAttributeValues = getLevelInstancesAttributesValues(
					currentStructureLevelInstances, attributesList);

			for (StructureLevelInstance structureLevelInstance : currentStructureLevelInstances) {
				Map<Attribute, String> levelInstanceAttributeValues = levelInstancesAttributeValues.get(structureLevelInstance);
				writeLevelInstanceValues(structureLevelInstance, levelInstanceAttributeValues, attributesList,
						structureLevelInstanceMap, exportParentName, writer);
			}
			writer.write("\n\n");
		}
	}
	
	private Map<StructureLevelInstance, Map<Attribute, String>> getLevelInstancesAttributesValues(
			List<StructureLevelInstance> levelInstances, List<Attribute> attributes) {
		List<AttributeInstance> attributeInstances = structureBS.getAttributeInstacesValues(levelInstances, attributes);
		
		Map<StructureLevelInstance, Map<Attribute, String>> attributeInstancesValues = new LinkedHashMap<>();

		
		attributeInstances.stream()
			.forEach(attributeInstance -> {
				StructureLevelInstance levelInstance = attributeInstance.getLevelInstance();
				Attribute attribute = attributeInstance.getAttribute();
				Map<Attribute, String> levelInstanceAttribute = attributeInstancesValues.get(levelInstance);
				
				if (levelInstanceAttribute == null) {
					levelInstanceAttribute = new LinkedHashMap<>();
				}
				
				levelInstanceAttribute.put(attribute, attributeInstance.getValue());
				attributeInstancesValues.put(levelInstance, levelInstanceAttribute);
			});
		
		
		return attributeInstancesValues;
	}
	

	private Map<StructureLevel, List<StructureLevelInstance>> getStructureLevelInstanceMap(
			List<StructureLevelInstance> structureLevelInstanceList) {
		Map<StructureLevel, List<StructureLevelInstance>> structureLevelInstanceMap = new LinkedHashMap<>();

		structureLevelInstanceList.stream().forEach(levelInstance -> {
			if(structureLevelInstanceMap.get(levelInstance.getLevel()) == null){
				structureLevelInstanceMap.put(levelInstance.getLevel(), new ArrayList<>(Arrays.asList(levelInstance)));
			} else {
				structureLevelInstanceMap.get(levelInstance.getLevel()).add(levelInstance);
			}
		});

		return structureLevelInstanceMap;
	}

	private String getLevelInstanceHeaders(StructureLevel structureLevel, List<Attribute> attributes, StructureLevelInstance structureLevelInstance, Boolean exportParentName){
		StringBuffer levelInstanceHeaders = new StringBuffer();
		levelInstanceHeaders.append(structureLevel.getName() + ":\n");

		levelInstanceHeaders.append(helper.fromStringToCsvFieldFormat("Nome"));

		if (exportParentName) {
			// Escreve o header que indica o parent
			final Long parentId = structureLevelInstance.getParent();
			if (parentId != null) {
				final StructureLevelInstance parent = structureHelper.retrieveLevelInstance(parentId);
				final StructureLevel parentLevel = parent != null ? parent.getLevel() : null;
				final String parentName = parentLevel != null ? parentLevel.getName() : "";
				levelInstanceHeaders.append(helper.fromStringToCsvFieldFormat(parentName));
			} else {
				levelInstanceHeaders.append(helper.fromStringToCsvFieldFormat("Plano de Ação"));
			}	
		}

		// Escreve os headers que indicam os campos
		attributes.stream().forEach(attribute -> {
			final Boolean isAttachmentField = attribute
				.getType()
				.equals(AttachmentField.class.getCanonicalName());

			// Ignora os campos de arquivo anexado
			if(!isAttachmentField) {
				final String attributeLabel = Optional.ofNullable(attribute.getLabel()).orElse("");
				levelInstanceHeaders.append(helper.fromStringToCsvFieldFormat(attributeLabel));
			}
		});
		levelInstanceHeaders.append(helper.fromStringToCsvFieldFormat("Rendimento") + '\n');

		return levelInstanceHeaders.toString();
	}
	
	private void writeLevelInstanceValues(StructureLevelInstance levelInstance, Map<Attribute, String> attributeValues,
			List<Attribute> attributes,
			Map<Long, StructureLevelInstance> structureLevelInstanceMap, Boolean exportParentName, Writer writer)
			throws IOException {
		writeInstanceName(levelInstance, writer);

		if (exportParentName) {
			writeParentOrPlanName(levelInstance, structureLevelInstanceMap, writer);
		}

		for (Attribute attribute : attributes) {
			String attributeValue = resolveAttributeValue(levelInstance, attribute, attributeValues);
			writer.write(attributeValue);
		}

		writePerformance(levelInstance, writer);
	}

	private void writeInstanceName(StructureLevelInstance levelInstance, Writer writer) throws IOException {
		final String name = Optional.ofNullable(levelInstance.getName()).orElse("");
		writer.write(helper.fromStringToCsvFieldFormat(name));
	}

	private void writeParentOrPlanName(StructureLevelInstance levelInstance,
			Map<Long, StructureLevelInstance> structureLevelInstanceMap, Writer writer) throws IOException {
		final Long parentId = levelInstance.getParent();
		if (parentId != null) {
			StructureLevelInstance parent = structureLevelInstanceMap.get(parentId);
			if (parent == null) {
				parent = structureHelper.retrieveLevelInstance(parentId);
			}
			final String parentName = Optional.ofNullable(parent.getName()).orElse("");
			writer.write(helper.fromStringToCsvFieldFormat(parentName));
		} else {
			final Plan plan = levelInstance.getPlan();
			final String planName = Optional.ofNullable(plan.getName()).orElse("");
			writer.write(helper.fromStringToCsvFieldFormat(planName));
		}
	}

	private String resolveAttributeValue(StructureLevelInstance levelInstance, Attribute attribute,
			Map<Attribute, String> attributeValues) {
		String type = attribute.getType();

		if (type.equals(ActionPlanField.class.getCanonicalName())) {
			return getActionPlanValues(fieldsBS.listAllActionPlansByInstance(levelInstance));
		} else if (type.equals(BudgetField.class.getCanonicalName())) {
			return getBudgetValues(fieldsBS.getBudgets(levelInstance));
		} else if (type.equals(ResponsibleField.class.getCanonicalName())
				|| type.equals(ManagerField.class.getCanonicalName())) {
			return getResponsibleOrManagerValue(attribute, attributeValues);
		} else if (type.equals(AttachmentField.class.getCanonicalName())) {
			return "";
		} else {
			return getDefaultAttributeValue(attribute, attributeValues);
		}
	}

	private String getResponsibleOrManagerValue(Attribute attribute, Map<Attribute, String> attributeValues) {
		try {
			String responsible = attributeValues != null ? attributeValues.get(attribute) : null;
			User user = this.userBS.existsByUser(Long.parseLong(responsible));
			return user != null ? helper.fromStringToCsvFieldFormat(user.getName())
					: String.valueOf(helper.getCsvDelimiter());
		} catch (NumberFormatException e) {
			return helper.fromStringToCsvFieldFormat("");
		}
	}

	private String getDefaultAttributeValue(Attribute attribute, Map<Attribute, String> attributeValues) {
		String value = attributeValues != null ? attributeValues.get(attribute) : null;
		return value != null ? helper.fromStringToCsvFieldFormat(value) : "-" + helper.getCsvDelimiter();
	}

	private void writePerformance(StructureLevelInstance levelInstance, Writer writer) throws IOException {
		String performance = helper.fromDoubleToCsvPerformanceFormat(levelInstance.getLevelValue());
		performance = performance != null ? helper.fromStringToCsvFieldFormat(performance) : "-";
		writer.write(performance + "\n");
	}

	// Mapeia os LevelInstance de acordo como StructureLevel correspondente
	private String getActionPlanValues(List<ActionPlan> actionPlans) {
		StringBuffer actionPlanValues = new StringBuffer();

		for(int i = 0; i < actionPlans.size(); i++) {
			final ActionPlan currentActionPlan = actionPlans.get(i);
			final Boolean isChecked = currentActionPlan.isChecked();
			final String accomplished = isChecked ? "Sim" : "Não";
			actionPlanValues.append("Concluído: " + accomplished + " - ");

			final String description = currentActionPlan.getDescription();
			actionPlanValues.append("Descrição: " + description + " - ");
			

			final String responsible = currentActionPlan.getResponsible();
			actionPlanValues.append("Responsável: " + responsible + " - ");

			final Date startDate = currentActionPlan.getBegin();
			actionPlanValues.append("Início: " + startDate.toString() + " - ");
			
			final Date endDate = currentActionPlan.getEnd();
			actionPlanValues.append("Término: " + endDate.toString());
			
			if (i < actionPlans.size() - 1) {
				actionPlanValues.append(" | ");
			}
		}
		return helper.fromStringToCsvFieldFormat(actionPlanValues.toString());
	}

	// Retorna uma String com os valores de um campo do tipo Orçamento
	private String getBudgetValues(List<BudgetDTO> budgets){
		final StringBuffer budgetValues = new StringBuffer();
		for (int i = 0; i < budgets.size(); i++) {
			NumberFormat currencyFormater = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

			final BudgetDTO budgetDto = budgets.get(i);
			final Budget budget = budgetDto.getBudget();

			final String subAction = budget.getSubAction();
			if(subAction != null) {
				budgetValues.append("Ação Orçamentária: " + subAction + " - ");
			}

			final String name = budget.getName();
			budgetValues.append("Nome: " + name + " - ");

			final String budgetLoa = currencyFormater.format(budgetDto.getBudgetLoa());
			budgetValues.append("Orçamento LOA: " + budgetLoa + " - ");

			final String balanceAvailable = currencyFormater.format(budgetDto.getBalanceAvailable());
			budgetValues.append("Saldo Disponível: " + balanceAvailable + " - ");

			final String commited = currencyFormater.format(budget.getCommitted());
			budgetValues.append("Empenhado: " + commited + " - ");

			final String realized = currencyFormater.format(budget.getRealized());
			budgetValues.append("Realizado: " + realized);

			if (i < budgets.size() - 1) {
				budgetValues.append(" | ");
			}
		}

		return helper.fromStringToCsvFieldFormat(budgetValues.toString());
	}
}
