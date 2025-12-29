package org.forpdi.core.csvs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.DateUtil;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.attribute.types.SelectField;
import org.forpdi.planning.attribute.types.TextArea;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.plan.PlanRiskBS;
import org.forrisco.core.policy.Policy;
import org.forrisco.core.policy.PolicyBS;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskFilterParams;
import org.forrisco.risk.links.RiskAxis;
import org.forrisco.risk.links.RiskStrategy;
import org.forrisco.risk.monitor.MonitoringState;
import org.forrisco.risk.preventiveaction.PreventiveAction;
import org.forrisco.risk.preventiveaction.PreventiveActionBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@Service
public class CsvRiskExport {
	@Autowired private CsvExportHelper helper;
	@Autowired private PreventiveActionBS preventiveActionBS;
	@Autowired private RiskBS riskBS;
	@Autowired private StructureBS structureBS;
	@Autowired private UserBS userBS;
	@Autowired private AttributeHelper attrHelper;
    @Autowired private PolicyBS policyBS;
    @Autowired private UnitBS unitBS;
    @Autowired private PlanRiskBS planRiskBS;
	@PersistenceContext
    private EntityManager em; 
    
	public void exportAxesRiskCSV(Long planId, Long selectedAxis, Integer selectedYear, OutputStream outputStream)
			throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

		List<Risk> allRisksForPlan = getFilteredRisks(planId, -1L, -1);

		List<Risk> risksToWrite = new ArrayList<>();
		List<PreventiveAction> actionsToWrite = new ArrayList<>();
		List<RiskStrategy> strategiesToWrite = new ArrayList<>();
		List<RiskAxis> axesToWrite = new ArrayList<>();

		for (Risk risk : allRisksForPlan) {

			List<RiskAxis> currentRiskAxes = this.riskBS.listRiskAxis(risk).getList();
			boolean axisMatches = (selectedAxis == -1L) || currentRiskAxes.stream()
					.anyMatch(axis -> axis.getStructure().getId().equals(selectedAxis));

			if (!axisMatches) {
				continue;
			}

			List<PreventiveAction> currentRiskActions = this.preventiveActionBS.listPreventiveActionByRisk(risk)
					.getList();
			List<PreventiveAction> filteredActions = currentRiskActions.stream()
					.filter(action -> (selectedYear == -1)
							|| DateUtil.isSameYear(action.getValidityBegin(), selectedYear))
					.collect(Collectors.toList());

			risksToWrite.add(risk);
			axesToWrite.addAll(currentRiskAxes);
			actionsToWrite.addAll(filteredActions);
			strategiesToWrite.addAll(this.riskBS.listRiskStrategy(risk).getList());
		}

		String headerRisks = (helper.fromStringToCsvFieldFormat("Riscos") + '\n'
				+ helper.fromStringToCsvFieldFormat("Nome")
				+ helper.fromStringToCsvFieldFormat("Gestor")
				+ helper.fromStringToCsvFieldFormat("Responsável Técnico")
				+ helper.fromStringToCsvFieldFormat("Código")
				+ helper.fromStringToCsvFieldFormat("Causa")
				+ helper.fromStringToCsvFieldFormat("Consequência")
				+ helper.fromStringToCsvFieldFormat("Probabilidade")
				+ helper.fromStringToCsvFieldFormat("Impacto")
				+ helper.fromStringToCsvFieldFormat("Periodicidade")
				+ helper.fromStringToCsvFieldFormat("Tipo")
				+ helper.fromStringToCsvFieldFormat("Início")
				+ helper.fromStringToCsvFieldFormat("Resposta")
				+ helper.fromStringToCsvFieldFormat("Nível Organizacional"));
		writer.write(headerRisks + '\n');

		for (Risk risk : risksToWrite) {
			String manager = risk.getManagerNameIfExists();
			String rowRisks = (helper.fromStringToCsvFieldFormat(risk.getName())
					+ helper.fromStringToCsvFieldFormat(manager)
					+ helper.fromStringToCsvFieldFormat(risk.getUser().getName())
					+ helper.fromStringToCsvFieldFormat(risk.getCode())
					+ helper.fromStringToCsvFieldFormat(risk.getReasonWithoutTags())
					+ helper.fromStringToCsvFieldFormat(risk.getResultWithoutTags())
					+ helper.fromStringToCsvFieldFormat(risk.getProbability())
					+ helper.fromStringToCsvFieldFormat(risk.getImpact())
					+ helper.fromStringToCsvFieldFormat(risk.getPeriodicity())
					+ helper.fromStringToCsvFieldFormat(risk.getType())
					+ helper.fromStringToCsvFieldFormat(formatter.format(risk.getBegin()))
					+ helper.fromStringToCsvFieldFormat(risk.getRiskResponseLabel())
					+ helper.fromStringToCsvFieldFormat(risk.getRiskLevelLabel()));
			writer.write(rowRisks + '\n');
		}

		writeRiskAxisCSV(axesToWrite, writer);
		writePreventiveActionsCSV(actionsToWrite, writer);
		writeStrategyObjectivesCSV(strategiesToWrite, writer);

		writer.flush();
		writer.close();
		outputStream.close();
	}
    
    
    private List<Risk> getFilteredRisks(Long planId, Long selectedAxis, Integer selectedYear){
    	
    	PlanRisk planRisk = this.policyBS.exists(planId, PlanRisk.class);
		
		List<Risk> filteredRisks = new ArrayList<>();
		List<Risk> filteredRisksByAxis = new ArrayList<>();
		List<Risk> filteredRisksByYear = new ArrayList<>();
		List<PreventiveAction> actionsFiltered = new ArrayList<>();
		List<RiskAxis> axesFiltered = new ArrayList<>();
        
        List<Unit> planRiskUnits = this.unitBS.listUnitsbyPlanRisk(planRisk).getList();
        if (planRisk == null || GeneralUtils.isEmpty(planRiskUnits)) {
            return null;
        }

        List<Unit> units = planRiskUnits
                .stream()
                .collect(Collectors.toList());
		
		List<Risk> risks = new ArrayList<>();
			for (Unit unit : units) {
				risks.addAll(this.riskBS.listRiskByUnit(unit).getList());
			}
		
		for (Risk risk: risks) {			
			axesFiltered.addAll(this.riskBS.listRiskAxis(risk).getList());
			actionsFiltered.addAll(this.preventiveActionBS.listPreventiveActionByRisk(risk).getList());
		}
		
		for (RiskAxis axis: axesFiltered) {
			
			if(selectedAxis == -1 
					|| axis.getStructure().getId().equals(selectedAxis)) {
				filteredRisksByAxis.add(axis.getRisk());
			}
		}
		
		for (PreventiveAction action: actionsFiltered) {
			if(selectedYear == -1 || DateUtil.isSameYear(action.getValidityBegin(), selectedYear)) {
					filteredRisksByYear.add(action.getRisk());						
			}
		}
		
		Set<Risk> combinedRisks = new LinkedHashSet<Risk>();
		combinedRisks.addAll(filteredRisksByAxis);
		combinedRisks.addAll(filteredRisksByYear);
		filteredRisks.addAll(combinedRisks);
		
		return filteredRisks;
    }

		private static final String risk = "Risco";
    
		private void writeRiskAxisCSV(List<RiskAxis> axes, Writer writer) throws IOException {
			writeRiskAxisHeader(writer);

			for (RiskAxis axe : axes) {
				StructureLevelInstance axisInstance = axe.getStructure();
				List<Attribute> attributeList = structureBS.listAttributes(axisInstance.getLevel()).getList();

				String responsible = retrieveResponsible(axisInstance, attributeList);
				String description = retrieveDescription(axisInstance, attributeList);

				writeRiskAxisRow(writer, axe, responsible, description);
			}
		}

		private void writeRiskAxisHeader(Writer writer) throws IOException {
			String headerAxis = (helper.fromStringToCsvFieldFormat("Eixos temáticos") + '\n'
					+ helper.fromStringToCsvFieldFormat("Nome")
					+ helper.fromStringToCsvFieldFormat(risk)
					+ helper.fromStringToCsvFieldFormat("Responsável")
					+ helper.fromStringToCsvFieldFormat("Descrição"));
			writer.write('\n' + headerAxis + '\n');
		}

		private String retrieveResponsible(StructureLevelInstance axisInstance, List<Attribute> attributeList) {
			for (Attribute att : attributeList) {
				if (isAttributeInstanceValid(axisInstance, att)
						&& att.getType().equals(ResponsibleField.class.getCanonicalName())) {

					String responsibleId = attrHelper.retrieveAttributeInstance(axisInstance, att).getValue();

					User responsibleUser = this.userBS.existsByUser(Long.parseLong(responsibleId));

					if (responsibleUser != null) {
						return responsibleUser.getName();
					}
				}
			}
			return " ";
		}

		private String retrieveDescription(StructureLevelInstance axisInstance, List<Attribute> attributeList) {
			for (Attribute att : attributeList) {
				if (isAttributeInstanceValid(axisInstance, att)
						&& att.getType().equals(TextArea.class.getCanonicalName())) {
					return attrHelper.retrieveAttributeInstance(axisInstance, att).getValue();
				}
			}
			return " ";
		}

		private boolean isAttributeInstanceValid(StructureLevelInstance axisInstance, Attribute att) {
			return attrHelper.retrieveAttributeInstance(axisInstance, att) != null;
		}

		private void writeRiskAxisRow(Writer writer, RiskAxis axe, String responsible, String description)
				throws IOException {
			String rowAxis = (helper.fromStringToCsvFieldFormat(axe.getName())
					+ helper.fromStringToCsvFieldFormat(axe.getRisk().getName())
					+ helper.fromStringToCsvFieldFormat(responsible)
					+ helper.fromStringToCsvFieldFormat(description));
			writer.write(rowAxis + '\n');
		}
	
	private void writePreventiveActionsCSV(List<PreventiveAction> actions, Writer writer) throws IOException {
		String headerActions = (
				helper.fromStringToCsvFieldFormat("Ações de prevenção") + '\n'
				+ helper.fromStringToCsvFieldFormat(risk)
				+ helper.fromStringToCsvFieldFormat("Ação")
				+ helper.fromStringToCsvFieldFormat("Gestor")
				+ helper.fromStringToCsvFieldFormat("Responsável técnico")
				+ helper.fromStringToCsvFieldFormat("Concluído")
				+ helper.fromStringToCsvFieldFormat("Início")
				+ helper.fromStringToCsvFieldFormat("Término")
			);
		
		writer.write('\n' + headerActions + '\n');
		
		for(PreventiveAction action : actions) {
	
			String managerAction = action.getManager() != null ?  action.getManager().getName() : " ";
			String validityBegin = action.getValidityBegin() != null 
					? DateUtil.dateToString(action.getValidityBegin()) : " ";
			String validityEnd = action.getValidityEnd() != null 
					? DateUtil.dateToString(action.getValidityEnd()) : " ";
			
			String activityRow = (
				helper.fromStringToCsvFieldFormat(action.getRisk().getName())
				+ helper.fromStringToCsvFieldFormat(action.getAction())
				+ helper.fromStringToCsvFieldFormat(managerAction)	
				+ helper.fromStringToCsvFieldFormat(action.getUser().getName())	
				+ helper.fromStringToCsvFieldFormat(action.isAccomplishedDescription())	
				+ helper.fromStringToCsvFieldFormat(validityBegin)	
				+ helper.fromStringToCsvFieldFormat(validityEnd)	
			);
			writer.write(activityRow  + '\n');
		}
		
	}
	
	private void writeStrategyObjectivesCSV(List<RiskStrategy> strategies, Writer writer) throws IOException {
		writeStrategyObjectivesHeader(writer);

		for (RiskStrategy strategy : strategies) {
			StructureLevelInstance strategyInstance = strategy.getStructure();
			List<Attribute> attributeList = structureBS.listAttributes(strategyInstance.getLevel()).getList();

			String responsible = retrieveAttributeValue(strategyInstance, attributeList,
					ResponsibleField.class.getCanonicalName());
			String perspective = retrieveAttributeValue(strategyInstance, attributeList,
					SelectField.class.getCanonicalName());
			String description = retrieveAttributeValue(strategyInstance, attributeList, TextArea.class.getCanonicalName());

			writeStrategyObjectivesRow(writer, strategy, responsible, description, perspective);
		}
	}

	private void writeStrategyObjectivesHeader(Writer writer) throws IOException {
		String headerStrategies = (helper.fromStringToCsvFieldFormat("Objetivos estratégicos") + '\n'
				+ helper.fromStringToCsvFieldFormat(risk)
				+ helper.fromStringToCsvFieldFormat("Nome")
				+ helper.fromStringToCsvFieldFormat("Responsável")
				+ helper.fromStringToCsvFieldFormat("Descrição")
				+ helper.fromStringToCsvFieldFormat("Perspectiva do BSC"));
		writer.write('\n' + headerStrategies + '\n');
	}

	private String retrieveAttributeValue(StructureLevelInstance instance, List<Attribute> attributes,
			String attributeType) {
		for (Attribute att : attributes) {
			if (attrHelper.retrieveAttributeInstance(instance, att) != null
					&& att.getType().equals(attributeType)) {
				return attrHelper.retrieveAttributeInstance(instance, att).getValue();
			}
		}
		return "";
	}

	private void writeStrategyObjectivesRow(Writer writer, RiskStrategy strategy, String responsible, String description,
			String perspective) throws IOException {
		String strategyRow = (helper.fromStringToCsvFieldFormat(strategy.getRisk().getName())
				+ helper.fromStringToCsvFieldFormat(strategy.getName())
				+ helper.fromStringToCsvFieldFormat(responsible)
				+ helper.fromStringToCsvFieldFormat(description)
				+ helper.fromStringToCsvFieldFormat(perspective));
		writer.write(strategyRow + '\n');
	}

	public void exportRiskCsv(Long planRiskId, RiskFilterParams filterParams, OutputStream outputStream)
			throws IOException {
		StringBuilder csvContent = new StringBuilder();

		PlanRisk planRisk = planRiskBS.exists(planRiskId, PlanRisk.class);
		if (planRisk == null) {
			throw new IllegalArgumentException("Plano de risco não encontrado");
		}
		Policy policy = planRisk.getPolicy();

		String policyAndPlanRiskHeader = helper.fromStringToCsvFieldFormat("Política")
				+ helper.fromStringToCsvFieldFormat("Plano de riscos");

		csvContent.append(policyAndPlanRiskHeader).append('\n')
				.append(helper.fromStringToCsvFieldFormat(policy.getName()))
				.append(helper.fromStringToCsvFieldFormat(planRisk.getName()))
				.append("\n\n");

		String risksHeader = helper.fromStringToCsvFieldFormat("Nome do risco")
				+ helper.fromStringToCsvFieldFormat("Código")
				+ helper.fromStringToCsvFieldFormat("Tipo de risco")
				+ helper.fromStringToCsvFieldFormat("Resposta ao risco")
				+ helper.fromStringToCsvFieldFormat("Tipologia")
				+ helper.fromStringToCsvFieldFormat("Unidade")
				+ helper.fromStringToCsvFieldFormat("Subunidade")
				+ helper.fromStringToCsvFieldFormat("Data de criação")
				+ helper.fromStringToCsvFieldFormat("Monitoramento")
				+ helper.fromStringToCsvFieldFormat("Último monitoramento");

		csvContent.append(risksHeader).append('\n');

		List<Risk> risks = this.riskBS.filterRisks(planRiskId, filterParams);
		this.riskBS.setRisksMonitoringState(risks);

		for (Risk r : risks) {
			Unit unit = r.getUnit();
			String subunitName = unit.isSubunit() ? unit.getName() : "";
			String unitName = unit.isSubunit() ? unit.getParent().getName() : unit.getName();
			MonitoringState monitoringState = MonitoringState.values()[r.getMonitoringState()];

			java.util.Date lastMonitoringDate = this.riskBS.getLastMonitoringDate(r);
			String lastMonitoringStr = (lastMonitoringDate != null)
					? DateUtil.dateToString(lastMonitoringDate)
					: "—";

			csvContent.append(helper.fromStringToCsvFieldFormat(r.getName()))
					.append(helper.fromStringToCsvFieldFormat(r.getCode()))
					.append(helper.fromStringToCsvFieldFormat(r.getType()))
					.append(helper.fromStringToCsvFieldFormat(r.getRiskResponseLabel()))
					.append(helper.fromStringToCsvFieldFormat(r.getTipologiesFomatted()))
					.append(helper.fromStringToCsvFieldFormat(unitName))
					.append(helper.fromStringToCsvFieldFormat(subunitName))
					.append(helper.fromStringToCsvFieldFormat(DateUtil.dateToString(r.getBegin())))
					.append(helper.fromStringToCsvFieldFormat(monitoringState.getReportLabel()))
					.append(helper.fromStringToCsvFieldFormat(lastMonitoringStr)) // << NOVA CÉLULA
					.append('\n');
		}

		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
		writer.write(csvContent.toString());

		writer.flush();
		writer.close();
		outputStream.close();
	}
}
