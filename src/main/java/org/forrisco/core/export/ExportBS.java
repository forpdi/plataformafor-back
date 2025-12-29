package org.forrisco.core.export;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forrisco.core.process.Process;
import org.forrisco.core.process.ProcessBS;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.ExecutionControllerEnum;
import org.forrisco.risk.ExistentControllerEnum;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskOrganizationalLevel;
import org.forrisco.risk.preventiveaction.PreventiveAction;
import org.forrisco.risk.preventiveaction.PreventiveActionBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class ExportBS {
    @Autowired
    private UnitBS unitBS;
    @Autowired
    private CompanyBS companyBS;
    @Autowired
    private ProcessBS processBS;
    @Autowired
    private RiskBS riskBS;
    @Autowired
    private PreventiveActionBS preventiveActionBS;

    private static void writeBom(Writer writer) throws IOException {
        writer.write('\uFEFF'); // BOM
    }

    private static String csvSafe(String s) {
        if (s == null)
            return "";
        String out = s
                .replace("\uFEFF", "")
                .replace('\u00A0', ' ');
        out = out.replace("\r\n", "\n").replace("\r", "\n");
        return out;
    }

    private static CSVFormat csvFormatWithHeader(String... header) {
        return CSVFormat.DEFAULT
                .withHeader(header)
                .withDelimiter(';')
                .withQuoteMode(QuoteMode.MINIMAL)
                .withRecordSeparator("\r\n");
    }

    public void exportUnitsCSV(List<Long> idList, HttpServletResponse response, UserSession userSession) {
        List<Unit> unitList = new ArrayList<>();

        if (idList != null && !idList.isEmpty()) {
            int level = userSession.getAccessLevel();
            boolean isSystemAdmin = (level == AccessLevels.SYSTEM_ADMIN.getLevel());
            boolean isCompanyAdmin = (level == AccessLevels.COMPANY_ADMIN.getLevel());

            if (!isSystemAdmin && !isCompanyAdmin) {
                throw new IllegalArgumentException("Apenas administradores podem exportar dados.");
            }

            Long loggedInUserId = userSession.getAuthData().getId();
            Company userCompany = companyBS.findCompanyByUserId(loggedInUserId);
            if (userCompany == null) {
                throw new IllegalArgumentException("Acesso negado.");
            }

            Unit firstUnit = unitBS.exists(idList.get(0), Unit.class);
            if (firstUnit == null) {
                throw new IllegalArgumentException("Unidade inválida.");
            }
            if (!firstUnit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
                throw new IllegalArgumentException("Acesso negado.");
            }

            unitList = unitBS.retrieveUnitsByIds(idList);

            for (Unit u : unitList) {
                if (!u.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
                    throw new IllegalArgumentException("Acesso negado.");
                }
            }

            for (Unit u : unitList) {
                if (u.getName() == null || u.getName().isEmpty()) {
                    throw new IllegalArgumentException("A unidade não possui nome, não é possível exportá-la");
                } else if (u.getAbbreviation() == null || u.getAbbreviation().isEmpty()) {
                    throw new IllegalArgumentException(
                            "A unidade " + u.getName() + " não possui sigla, e não pode ser exportada");
                }
            }
        }

        response.setContentType("text/csv; charset=UTF-8");
        if (idList != null && !idList.isEmpty() && unitList.get(0).getParent() != null) {
            response.setHeader("Content-Disposition", "attachment; filename=\"subunidades.csv\"");
        } else if (idList != null && !idList.isEmpty() && unitList.get(0).getParent() == null) {
            response.setHeader("Content-Disposition", "attachment; filename=\"unidades.csv\"");
        } else {
            response.setHeader("Content-Disposition", "attachment; filename=\"unidades-subunidades-template.csv\"");
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {

            writeBom(writer);

            try (CSVPrinter csvPrinter = new CSVPrinter(
                    writer, csvFormatWithHeader("Nome da Unidade", "Sigla", "Descrição"))) {
                if (idList != null && !idList.isEmpty()) {
                    for (Unit u : unitList) {
                        String descricao = (u.getDescription() != null) ? u.getDescription() : "";
                        csvPrinter.printRecord(
                                csvSafe(u.getName()),
                                csvSafe(u.getAbbreviation()),
                                csvSafe(descricao));
                    }
                }
                csvPrinter.flush();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void exportProcessesCSV(List<Long> idList, HttpServletResponse response, UserSession userSession) {
        List<Process> processList = new ArrayList<>();

        if (idList != null && !idList.isEmpty()) {
            Process process = unitBS.exists(idList.get(0), Process.class);
            boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
            if (!isSystemAdmin) {
                Long loggedInUserId = userSession.getAuthData().getId();
                Company userCompany = companyBS.findCompanyByUserId(loggedInUserId);
                if (userCompany == null || !process.getUnitCreator().getPlanRisk().getPolicy().getCompany().getId()
                        .equals(userCompany.getId())) {
                    throw new IllegalArgumentException("Acesso negado.");
                }
            }
            processList = processBS.retrieveProcessesByIds(idList);
            for (Process p : processList) {
                if (p.getName() == null || p.getName().isEmpty()) {
                    throw new IllegalArgumentException(
                            "O processo ou um dos processos não possui nome, não é possível exportá-lo");
                } else if (p.getProcessObjectivesDescriptionsString() == null
                        || p.getProcessObjectivesDescriptionsString().isEmpty()) {
                    throw new IllegalArgumentException(
                            "O processo " + p.getName() + " não possui objetivos, e não pode ser exportado");
                }
            }
        }

        response.setContentType("text/csv; charset=UTF-8");
        if (idList != null && !idList.isEmpty()) {
            response.setHeader("Content-Disposition", "attachment; filename=\"processos.csv\"");
        } else {
            response.setHeader("Content-Disposition", "attachment; filename=\"processos-template.csv\"");
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {

            writeBom(writer);

            try (CSVPrinter csvPrinter = new CSVPrinter(
                    writer, csvFormatWithHeader("Nome", "Objetivo"))) {
                if (idList != null && !idList.isEmpty()) {
                    for (Process p : processList) {
                        for (int i = 0; p.getAllObjectives().size() > i; i++) {
                            csvPrinter.printRecord(
                                    csvSafe(p.getName()),
                                    csvSafe(p.getAllObjectives().get(i).getDescription()));
                        }
                    }
                }
                csvPrinter.flush();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void exportRisksCSV(List<Long> idList, HttpServletResponse response, UserSession userSession) {
        List<Risk> risksList = new ArrayList<>();

        if (idList != null && !idList.isEmpty()) {
            int level = userSession.getAccessLevel();
            boolean isSystemAdmin = (level == AccessLevels.SYSTEM_ADMIN.getLevel());
            boolean isCompanyAdmin = (level == AccessLevels.COMPANY_ADMIN.getLevel());

            if (!isSystemAdmin && !isCompanyAdmin) {
                throw new IllegalArgumentException("Apenas administradores podem exportar dados.");
            }

            Long loggedInUserId = userSession.getAuthData().getId();
            Company userCompany = companyBS.findCompanyByUserId(loggedInUserId);
            if (userCompany == null) {
                throw new IllegalArgumentException("Acesso negado.");
            }

            Risk risk = unitBS.exists(idList.get(0), Risk.class);
            if (risk == null) {
                throw new IllegalArgumentException("Risco inválido.");
            }
            if (!risk.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
                throw new IllegalArgumentException("Acesso negado.");
            }

            risksList = riskBS.listRisksByIds(idList);

            for (Risk r : risksList) {
                if (!r.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
                    throw new IllegalArgumentException("Acesso negado.");
                }
            }

            for (Risk r : risksList) {
                if (r.getName() == null || r.getName().isEmpty()) {
                    throw new IllegalArgumentException(
                            "O risco ou um dos riscos não possui nome, não é possível exportá-lo");
                } else if (r.getReason() == null || r.getReason().isEmpty()) {
                    throw new IllegalArgumentException(
                            "O risco " + r.getName() + " não possui causa, e não pode ser exportado");
                } else if (r.getPeriodicity() == null || r.getPeriodicity().isEmpty()) {
                    throw new IllegalArgumentException("O risco " + r.getName()
                            + " não possui periodicidade de monitoramento, e não pode ser exportado");
                } else if (r.getType() == null || r.getType().isEmpty()) {
                    throw new IllegalArgumentException(
                            "O risco " + r.getName() + " não possui tipo, e não pode ser exportado");
                }
            }
        }

        response.setContentType("text/csv; charset=UTF-8");
        if (idList != null && !idList.isEmpty()) {
            response.setHeader("Content-Disposition", "attachment; filename=\"riscos.csv\"");
        } else {
            response.setHeader("Content-Disposition", "attachment; filename=\"riscos-template.csv\"");
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {

            writeBom(writer);

            try (CSVPrinter csvPrinter = new CSVPrinter(
                    writer,
                    csvFormatWithHeader(
                            "Nome do risco",
                            "Código de identificação do risco",
                            "Causa(s)",
                            "Consequência(s)",
                            "Controles Existentes",
                            "Execução dos controles",
                            "Periodicidade do monitoramento",
                            "Tipo",
                            "Resposta ao risco",
                            "Nível Organizacional"))) {
                if (idList != null && !idList.isEmpty()) {
                    for (Risk r : risksList) {
                        String riskCode = (r.getCode() != null) ? r.getCode() : "";
                        String riskConsequences = (r.getResult() != null) ? r.getResult() : "";
                        String riskExistentControllers = (r.getExistentControllerId() != null)
                                ? ExistentControllerEnum.GetById(r.getExistentControllerId()).getLabel()
                                : "";
                        String riskExecutionControllers = (r.getExecutionControllerId() != null)
                                ? ExecutionControllerEnum.GetById(r.getExecutionControllerId()).getLabel()
                                : "";
                        String riskResponse = (r.getRiskResponseLabel() != null) ? r.getRiskResponseLabel() : "";
                        String riskOrganizationalLevel = (r.getLevel() != null)
                                ? RiskOrganizationalLevel.getById(r.getLevel()).getLabel()
                                : "";

                        csvPrinter.printRecord(
                                csvSafe(r.getName()),
                                csvSafe(riskCode),
                                csvSafe(r.getReason()),
                                csvSafe(riskConsequences),
                                csvSafe(riskExistentControllers),
                                csvSafe(riskExecutionControllers),
                                csvSafe(r.getPeriodicity()),
                                csvSafe(r.getType()),
                                csvSafe(riskResponse),
                                csvSafe(riskOrganizationalLevel));
                    }
                }
                csvPrinter.flush();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void exportPrevenctiveActionsCSV(List<Long> idList, HttpServletResponse response, UserSession userSession) {
        List<PreventiveAction> prevenctiveActionList = new ArrayList<>();

        if (idList != null && !idList.isEmpty()) {
            PreventiveAction preventiveAction = unitBS.exists(idList.get(0), PreventiveAction.class);
            boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
            if (!isSystemAdmin) {
                Long loggedInUserId = userSession.getAuthData().getId();
                Company userCompany = companyBS.findCompanyByUserId(loggedInUserId);
                if (userCompany == null || !preventiveAction.getRisk().getUnit().getPlanRisk().getPolicy().getCompany()
                        .getId().equals(userCompany.getId())) {
                    throw new IllegalArgumentException("Acesso negado.");
                }
            }
            prevenctiveActionList = preventiveActionBS.listByActionsIds(idList);
            for (PreventiveAction pA : prevenctiveActionList) {
                if (pA.getAction() == null || pA.getAction().isEmpty()) {
                    throw new IllegalArgumentException(
                            "A ação de prevenção ou uma das ações de prevenção não possui descrição, não é possível exportá-la");
                }
            }
        }

        response.setContentType("text/csv; charset=UTF-8");
        if (idList != null && !idList.isEmpty()) {
            response.setHeader("Content-Disposition", "attachment; filename=\"acoesdeprevencao.csv\"");
        } else {
            response.setHeader("Content-Disposition", "attachment; filename=\"acoesdeprevencao-template.csv\"");
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {

            writeBom(writer);

            try (CSVPrinter csvPrinter = new CSVPrinter(
                    writer, csvFormatWithHeader("Ação", "Data início", "Data fim"))) {
                if (idList != null && !idList.isEmpty()) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    for (PreventiveAction pA : prevenctiveActionList) {
                        String actionBeginDate = (pA.getValidityBegin() != null)
                                ? formatter.format(pA.getValidityBegin())
                                : "";
                        String actionEndDate = (pA.getValidityEnd() != null) ? formatter.format(pA.getValidityEnd())
                                : "";

                        csvPrinter.printRecord(
                                csvSafe(pA.getAction()),
                                csvSafe(actionBeginDate),
                                csvSafe(actionEndDate));
                    }
                }
                csvPrinter.flush();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
