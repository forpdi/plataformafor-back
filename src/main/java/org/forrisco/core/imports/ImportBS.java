package org.forrisco.core.imports;

import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.user.User;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;

import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.process.Process;
import org.forrisco.core.process.ProcessBS;
import org.forrisco.core.process.ProcessObjective;
import org.forrisco.core.process.ProcessUnit;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.ExecutionControllerEnum;
import org.forrisco.risk.ExistentControllerEnum;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskLevel;
import org.forrisco.risk.RiskOrganizationalLevel;
import org.forrisco.risk.RiskResponse;
import org.forrisco.risk.preventiveaction.PreventiveAction;
import org.forrisco.risk.preventiveaction.PreventiveActionBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImportBS {

    @Autowired
    private UnitBS unitBS;
    @Autowired
    private CompanyBS companyBS;
    @Autowired
    private CompanyDomainContext domain;
    @Autowired
    private ProcessBS processBS;
    @Autowired
    private RiskBS riskBS;
    @Autowired
    private PreventiveActionBS preventiveActionBS;

    private static final char DELIM = ';';

    private static final String H_NOME = "Nome da Unidade";
    private static final String H_SIGLA = "Sigla";
    private static final String H_DESC = "Descrição";

    private List<Map<String, String>> normalizeAndRemapHeaders(
            List<Map<String, String>> rows,
            List<String> headerWarningsCollector
    ) {
        if (rows == null || rows.isEmpty())
            return rows;

        Map<String, String> canonicalByAlias = new LinkedHashMap<>();
        canonicalByAlias.put(normHeader("Nome da Unidade"), H_NOME);
        canonicalByAlias.put(normHeader("Sigla"), H_SIGLA);
        canonicalByAlias.put(normHeader("Descrição"), H_DESC);

        Set<String> originalHeaders = rows.get(0).keySet();
        Map<String, String> remap = new LinkedHashMap<>();
        List<String> mappedPairs = new ArrayList<>();

        for (String original : originalHeaders) {
            String normalized = normHeader(original);
            String canonical = canonicalByAlias.get(normalized);
            if (canonical != null) {
                remap.put(original, canonical);
                if (!original.equals(canonical)) {
                    mappedPairs.add("'" + stripInvisibles(original) + "' → '" + canonical + "'");
                }
            } else {
                remap.put(original, original);
            }
        }

        List<Map<String, String>> fixed = new ArrayList<>(rows.size());
        for (Map<String, String> line : rows) {
            Map<String, String> newLine = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : remap.entrySet()) {
                String srcKey = e.getKey();
                String dstKey = e.getValue();
                newLine.put(dstKey, line.get(srcKey));
            }
            fixed.add(newLine);
        }

        if (headerWarningsCollector != null) {
            StringBuilder sb = new StringBuilder("Cabeçalhos mapeados: ");
            sb.append("'Nome da Unidade'=\"").append(H_NOME).append("\"; ");
            sb.append("'Sigla'=\"").append(H_SIGLA).append("\"");
            if (fixed.get(0).containsKey(H_DESC)) {
                sb.append("; 'Descrição'=\"").append(H_DESC).append("\"");
            }
            sb.append(".");
            headerWarningsCollector.add(sb.toString());

            if (!mappedPairs.isEmpty()) {
                headerWarningsCollector.add(
                        "Ajustes automáticos de cabeçalho: " + String.join("; ", mappedPairs) + ".");
            }
        }

        return fixed;
    }

    private static String normHeader(String s) {
        if (s == null)
            return "";
        String out = stripInvisibles(s);
        out = out.trim().replaceAll("\\s+", " ");
        return out.toLowerCase(Locale.ROOT);
    }

    private static String stripInvisibles(String s) {
        if (s == null)
            return "";
        String out = s.replace("\uFEFF", "");
        out = out.replace('\u00A0', ' ');
        return out;
    }

    private static String normalizeCell(String s) {
        if (s == null)
            return "";
        String out = stripInvisibles(s);
        out = out.replaceAll("[\\t\\r\\n]+", " ").replaceAll("\\s+", " ").trim();
        return out;
    }

    private static String truncateWithWarn(String s, int max, int rowNumber, String field, List<String> warnings) {
        if (s == null)
            return null;
        if (s.length() <= max)
            return s;
        if (warnings != null) {
            warnings.add(String.format(
                    "Campo '%s' na linha %d excedeu %d caracteres e foi truncado.",
                    field, rowNumber, max));
        }
        return s.substring(0, max);
    }

    @Transactional
    public ImportResult importUnitsCsv(MultipartFile file, Long planRiskId, UserSession userSession) throws Exception {
        PlanRisk planRisk = unitBS.exists(planRiskId, PlanRisk.class);
        if (planRisk == null)
            throw new IllegalArgumentException("Plano de Risco inválido.");

        boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
        if (!isSystemAdmin) {
            Long loggedInUserId = userSession.getAuthData().getId();
            Company userCompany = companyBS.findCompanyByUserId(loggedInUserId);
            if (userCompany == null || !planRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
                throw new IllegalArgumentException("Acesso negado.");
            }
        }
        domain.validateTenant(planRisk.getPolicy().getCompany());

        List<Map<String, String>> rows = parseCsv(file);

        List<String> warnings = new ArrayList<>();
        rows = normalizeAndRemapHeaders(rows, warnings);

        User currentUser = unitBS.exists(userSession.getAuthData().getId(), User.class);

        return importUnits(rows, planRisk, null, currentUser, warnings);
    }

    // ===================== IMPORT SUBUNITS =====================
    @Transactional
    public ImportResult importSubunitsCsv(MultipartFile file, Long planRiskId, Long parentId, UserSession userSession)
            throws Exception {
        PlanRisk planRisk = unitBS.exists(planRiskId, PlanRisk.class);
        if (planRisk == null)
            throw new IllegalArgumentException("Plano de Risco inválido.");

        boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
        if (!isSystemAdmin) {
            Long loggedInUserId = userSession.getAuthData().getId();
            Company userCompany = companyBS.findCompanyByUserId(loggedInUserId);
            if (userCompany == null || !planRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
                throw new IllegalArgumentException("Acesso negado.");
            }
        }
        domain.validateTenant(planRisk.getPolicy().getCompany());

        Unit parent = unitBS.exists(parentId, Unit.class);
        if (parent == null)
            throw new IllegalArgumentException("Unidade pai inválida.");
        if (parent.getPlanRisk() == null || !Objects.equals(parent.getPlanRisk().getId(), planRiskId)) {
            throw new IllegalArgumentException("Unidade pai não pertence ao plano informado.");
        }

        List<Map<String, String>> rows = parseCsv(file);

        List<String> warnings = new ArrayList<>();
        rows = normalizeAndRemapHeaders(rows, warnings);

        User currentUser = unitBS.exists(userSession.getAuthData().getId(), User.class);

        return importUnits(rows, planRisk, parent, currentUser, warnings);
    }

    private ImportResult importUnits(
            List<Map<String, String>> rows,
            PlanRisk planRisk,
            Unit parent,
            User currentUser,
            List<String> warnings
    ) {
        ensureHeaders(rows, H_NOME, H_SIGLA);

        int imported = 0, skipped = 0;
        int emptyRows = 0, requiredInvalid = 0;
        List<Integer> emptyRowNumbers = new ArrayList<>();
        List<Integer> requiredInvalidRows = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        final int MAX_NAME = 255;
        final int MAX_SIGLA = 64;
        final int MAX_DESC = 2000;

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> line = rows.get(i);
            int rowNumber = i + 2;

            boolean allEmpty = line.values().stream()
                    .map(ImportBS::stripInvisibles)
                    .allMatch(v -> v == null || v.trim().isEmpty());
            if (allEmpty) {
                errors.add(new ImportError(rowNumber, "Geral", "Linha vazia."));
                skipped++;
                emptyRows++;
                emptyRowNumbers.add(rowNumber);
                continue;
            }

            String nome = normalizeCell(line.get(H_NOME));
            String sigla = normalizeCell(line.get(H_SIGLA));
            String desc = normalizeCell(line.getOrDefault(H_DESC, ""));

            if (nome.isEmpty() || sigla.isEmpty()) {
                errors.add(new ImportError(rowNumber, "Nome/Sigla", "Obrigatórios"));
                skipped++;
                requiredInvalid++;
                requiredInvalidRows.add(rowNumber);
                continue;
            }

            nome = truncateWithWarn(nome, MAX_NAME, rowNumber, "Nome da Unidade", warnings);
            sigla = truncateWithWarn(sigla, MAX_SIGLA, rowNumber, "Sigla", warnings);
            desc = truncateWithWarn(desc, MAX_DESC, rowNumber, "Descrição", warnings);

            try {
                Unit unit = new Unit();
                unit.setId(null);
                unit.setName(nome);
                unit.setAbbreviation(sigla);
                unit.setDescription(SanitizeUtil.sanitize(desc));
                unit.setParent(parent);
                unit.setPlanRisk(planRisk);
                unit.setDeleted(false);
                unit.setUser(currentUser);
                unitBS.save(unit);
                imported++;
            } catch (Throwable e) {
                errors.add(new ImportError(rowNumber, "Geral", e.getMessage()));
                skipped++;
            }
        }

        final int total = rows.size();
        warnings.add(String.format(
                "Importação concluída: %d linha(s) processada(s); %d importada(s) com sucesso; %d ignorada(s).",
                total, imported, skipped));
        if (emptyRows > 0)
            warnings.add(
                    String.format("%d linha(s) vazia(s) (linhas: %s).", emptyRows, sampleRows(emptyRowNumbers, 10)));
        if (requiredInvalid > 0)
            warnings.add(String.format("%d linha(s) com obrigatórios inválidos/vazios (linhas: %s).",
                    requiredInvalid, sampleRows(requiredInvalidRows, 10)));

        return new ImportResult(imported, skipped, errors, warnings);
    }

    @Transactional
    public ImportResult importProcessesCsv(MultipartFile file, Long planRiskId, Long unitId, UserSession userSession)
            throws Exception {
        PlanRisk planRisk = unitBS.exists(planRiskId, PlanRisk.class);
        if (planRisk == null)
            throw new IllegalArgumentException("Plano de Risco inválido.");

        boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
        if (!isSystemAdmin) {
            Long loggedInUserId = userSession.getAuthData().getId();
            Company userCompany = companyBS.findCompanyByUserId(loggedInUserId);
            if (userCompany == null || !planRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
                throw new IllegalArgumentException("Acesso negado.");
            }
        }
        domain.validateTenant(planRisk.getPolicy().getCompany());

        Unit targetUnit = unitBS.exists(unitId, Unit.class);
        if (targetUnit == null)
            throw new IllegalArgumentException("Unidade informada não existe.");
        if (targetUnit.getPlanRisk() == null || !Objects.equals(targetUnit.getPlanRisk().getId(), planRiskId)) {
            throw new IllegalArgumentException("A unidade informada não pertence ao plano selecionado.");
        }

        List<Map<String, String>> rows = parseCsv(file);
        if (rows.isEmpty())
            return new ImportResult(0, 0, Collections.emptyList(), Collections.singletonList("CSV sem registros: nenhuma linha foi processada."));

        Set<String> header = rows.get(0).keySet();
        String colName = pickHeader(header, "Nome do Processo", "Nome", "Processo", "Nome Processo");
        String colObj = pickHeader(header, "Objetivo", "Descrição do objetivo", "Descricao do objetivo", "Objetivos");

        if (colName == null || colObj == null) {
            throw new IllegalArgumentException("Cabeçalhos obrigatórios ausentes: 'Nome' e 'Objetivo'. Encontrados: " + header);
        }

        Map<String, LinkedHashSet<String>> objectivesByName = new LinkedHashMap<>();
        Map<String, Integer> firstRowByName = new HashMap<>();
        List<ImportError> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int skipped = 0, imported = 0;

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> r = rows.get(i);
            int rowNumber = i + 2;

            String name = safe(r.get(colName)).trim();
            String obj = safe(r.get(colObj)).trim();

            if (name.isEmpty()) {
                errors.add(new ImportError(rowNumber, colName, "Nome do processo é obrigatório"));
                skipped++;
                continue;
            }
            if (obj.isEmpty()) {
                errors.add(new ImportError(rowNumber, colObj, "Objetivo é obrigatório"));
                skipped++;
                continue;
            }

            objectivesByName.computeIfAbsent(name, k -> new LinkedHashSet<>()).add(obj);
            firstRowByName.putIfAbsent(name, rowNumber);
        }

        for (Map.Entry<String, LinkedHashSet<String>> entry : objectivesByName.entrySet()) {
            final String processName = entry.getKey();
            final LinkedHashSet<String> objectives = entry.getValue();
            final int representativeRow = firstRowByName.getOrDefault(processName, 2);

            if (objectives.isEmpty()) {
                errors.add(new ImportError(representativeRow, "Objetivo", "O processo deve possuir ao menos um objetivo"));
                skipped++;
                continue;
            }

            try {
                Process proc = new Process();
                proc.setName(processName);
                proc.setCompany(domain.get().getCompany());
                proc.setUnitCreator(targetUnit);
                processBS.save(proc);

                ProcessUnit pu = new ProcessUnit();
                pu.setProcess(proc);
                pu.setUnit(targetUnit);
                processBS.save(pu);

                for (String objectiveDesc : objectives) {
                    ProcessObjective po = new ProcessObjective();
                    po.setProcess(proc);
                    po.setDescription(objectiveDesc);
                    processBS.saveObjective(po);
                }

                imported++;
            } catch (Throwable e) {
                errors.add(new ImportError(representativeRow, "Geral", e.getMessage()));
                skipped++;
            }
        }

        final int total = rows.size();
        warnings.add(String.format("Importação concluída: %d linha(s) processada(s); %d processo(s) importado(s); %d ignorado(s).",
                total, imported, skipped));
        warnings.add("Cabeçalhos mapeados: 'Nome'=\"" + colName + "\"; 'Objetivo'=\"" + colObj + "\".");

        return new ImportResult(imported, skipped, errors, warnings);
    }

    @Transactional
    public ImportResult importRisksCsv(MultipartFile file, Long planRiskId, Long unitId, UserSession userSession)
            throws Exception {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Arquivo CSV vazio ou inválido.");

        PlanRisk planRisk = unitBS.exists(planRiskId, PlanRisk.class);
        if (planRisk == null)
            throw new IllegalArgumentException("Plano de Risco inválido.");

        boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
        if (!isSystemAdmin) {
            Long userId = userSession.getAuthData().getId();
            Company userCompany = companyBS.findCompanyByUserId(userId);
            if (userCompany == null || !planRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
                throw new IllegalArgumentException("Acesso negado.");
            }
        }
        domain.validateTenant(planRisk.getPolicy().getCompany());

        Unit targetUnit = unitBS.exists(unitId, Unit.class);
        if (targetUnit == null || targetUnit.isDeleted()) {
            throw new IllegalArgumentException("A unidade informada não foi encontrada.");
        }
        if (!Objects.equals(targetUnit.getPlanRisk().getId(), planRiskId)) {
            throw new IllegalArgumentException("A unidade informada não pertence ao plano selecionado.");
        }

        List<Map<String, String>> rows = parseCsv(file);
        if (rows.isEmpty()) {
            return new ImportResult(0, 0, Collections.emptyList(),
                    Collections.singletonList("CSV sem registros: nenhuma linha foi processada."));
        }

        Set<String> headers = rows.get(0).keySet();
        String colRiskName = pickHeader(headers, "Nome do risco", "Nome", "Risco", "Nome do Risco");
        String colCause = pickHeader(headers, "Causa", "Causas", "Causa(s)");
        String colPeriodicity = pickHeader(headers, "Periodicidade do monitoramento", "Periodicidade");
        String colType = pickHeader(headers, "Tipo", "Tipo do risco");

        String colExistingControls = pickHeader(headers, "Controles existentes", "Controles Existentes");
        String colControlsExecution = pickHeader(headers, "Execução dos controles", "Execucao dos controles");
        String colRiskResponse = pickHeader(headers, "Resposta ao risco", "Resposta");
        String colOrgLevel = pickHeader(headers, "Nível Organizacional", "Nivel Organizacional");
        String colCode = pickHeader(headers, "Código", "Codigo", "Code", "Código de indentificação do risco");
        String colResult = pickHeader(headers, "Resultado", "Consequência", "Consequencias", "Consequencia",
                "Consequências", "Consequência(s)");

        if (colRiskName == null || colCause == null || colPeriodicity == null || colType == null) {
            throw new IllegalArgumentException(
                "Cabeçalhos obrigatórios ausentes: 'Nome do risco', 'Causa', 'Periodicidade do monitoramento' e 'Tipo'. " +
                "Encontrados: " + headers);
        }

        final Set<String> VALID_PERIODICITIES = new LinkedHashSet<>(Arrays.asList(
                "Diária", "Semanal", "Quinzenal", "Mensal", "Bimestral", "Trimestral", "Semestral", "Anual"));
        final Set<String> VALID_TYPES = new LinkedHashSet<>(Arrays.asList("Ameaça", "Oportunidade"));

        User defaultUser = targetUnit.getUser();
        if (defaultUser == null && userSession != null && userSession.getAuthData() != null) {
            defaultUser = unitBS.exists(userSession.getAuthData().getId(), User.class);
        }

        PaginatedList<RiskLevel> policyLevels = riskBS.listRiskLevelByPolicy(planRisk.getPolicy());
        if (policyLevels == null || policyLevels.getList() == null || policyLevels.getList().isEmpty()) {
            throw new IllegalStateException("A política não possui níveis de risco cadastrados.");
        }
        RiskLevel defaultLevel = policyLevels.getList().get(0);

        final int totalProcessed = rows.size();
        int imported = 0, skipped = 0, skippedByRequired = 0;
        int blanksExistingControls = 0, blanksControlsExecution = 0, blanksRiskResponse = 0, blanksOrgLevel = 0;
        int codeReplacedCount = 0, nameTruncatedCount = 0, codeTruncatedCount = 0;

        final Map<String,Integer> invalidPeriodicity = new LinkedHashMap<>();
        final Map<String,Integer> invalidType        = new LinkedHashMap<>();
        final Map<String,Integer> invalidRiskResponse= new LinkedHashMap<>();
        final Map<String,Integer> invalidOrgLevel    = new LinkedHashMap<>();
        final List<Integer> emptyRowNumbers          = new ArrayList<>();
        final List<Integer> requiredInvalidRows      = new ArrayList<>();
        final List<Integer> duplicateCodeRows        = new ArrayList<>();

        Set<String> seenCodes = new HashSet<>();
        List<ImportError> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        final int MAX_NAME_LEN = 255;
        final int MAX_CODE_LEN = 255;

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            int rowNumber = i + 2;

            boolean allEmpty = row.values().stream().allMatch(v -> v == null || v.trim().isEmpty());
            if (allEmpty) {
                errors.add(new ImportError(rowNumber, "Geral", "Linha vazia."));
                skipped++; skippedByRequired++;
                emptyRowNumbers.add(rowNumber);
                continue;
            }

            String riskName = safe(row.get(colRiskName)).trim();
            String cause = safe(row.get(colCause)).trim();
            String peri = safe(row.get(colPeriodicity)).trim();
            String type = safe(row.get(colType)).trim();

            List<String> requiredProblems = new ArrayList<>();
            if (riskName.isEmpty()) requiredProblems.add("Nome do risco (valor recebido: \"\")");
            if (cause.isEmpty())    requiredProblems.add("Causa (valor recebido: \"\")");
            if (!VALID_PERIODICITIES.contains(peri)) {
                requiredProblems.add("Periodicidade do monitoramento (valor recebido: \"" + peri + "\")");
                if (!peri.isEmpty()) invalidPeriodicity.merge(peri, 1, Integer::sum);
            }
            if (!VALID_TYPES.contains(type)) {
                requiredProblems.add("Tipo (valor recebido: \"" + type + "\")");
                if (!type.isEmpty()) invalidType.merge(type, 1, Integer::sum);
            }

            if (!requiredProblems.isEmpty()) {
                errors.add(new ImportError(rowNumber, "Obrigatórios",
                        "Campos inválidos/ausentes: " + String.join(", ", requiredProblems)));
                skipped++; skippedByRequired++;
                requiredInvalidRows.add(rowNumber);
                continue;
            }

            String existingControlsTxt = (colExistingControls != null ? safe(row.get(colExistingControls)).trim() : "");
            String controlsExecutionTxt = (colControlsExecution != null ? safe(row.get(colControlsExecution)).trim() : "");
            String riskResponseTxt = (colRiskResponse != null ? safe(row.get(colRiskResponse)).trim() : "");
            String orgLevelTxt = (colOrgLevel != null ? safe(row.get(colOrgLevel)).trim() : "");

            Integer existentControllerId = null;
            if (!existingControlsTxt.isEmpty()) {
                try { existentControllerId = ExistentControllerEnum.GetByLabel(existingControlsTxt).getId(); }
                catch (Exception ignore) { blanksExistingControls++; }
            }

            Integer executionControllerId = null;
            if (!controlsExecutionTxt.isEmpty()) {
                try { executionControllerId = ExecutionControllerEnum.GetByLabel(controlsExecutionTxt).getId(); }
                catch (Exception ignore) { blanksControlsExecution++; }
            }

            Integer responseId = null;
            if (!riskResponseTxt.isEmpty()) {
                boolean allowed = ("Ameaça".equals(type)
                    && Arrays.asList("Evitar", "Mitigar", "Transferir", "Aceitar").contains(riskResponseTxt))
                    || ("Oportunidade".equals(type)
                    && Arrays.asList("Explorar", "Melhorar", "Compartilhar", "Aceitar").contains(riskResponseTxt));
                if (allowed) {
                    for (RiskResponse rr : RiskResponse.values()) {
                        if (rr.getLabel().equals(riskResponseTxt)) {
                            responseId = rr.getId();
                            break;
                        }
                    }
                    if (responseId == null) blanksRiskResponse++;
                } else {
                    blanksRiskResponse++;
                    invalidRiskResponse.merge(riskResponseTxt, 1, Integer::sum);
                }
            }

            Integer orgLevelId = null;
            if (!orgLevelTxt.isEmpty()) {
                try {
                    orgLevelId = RiskOrganizationalLevel.getByLabel(orgLevelTxt).getId();
                } catch (Exception ignore) {
                    blanksOrgLevel++;
                    invalidOrgLevel.merge(orgLevelTxt, 1, Integer::sum);
                }
            }

            String code = (colCode != null ? safe(row.get(colCode)).trim() : "");
            if (code.isEmpty()) {
                code = "RISK-" + targetUnit.getId() + "-" + System.currentTimeMillis() + "-" + (i + 1);
            } else {
                if (seenCodes.contains(code)) {
                    code = code + "-" + (i + 1);
                    codeReplacedCount++;
                    duplicateCodeRows.add(rowNumber);
                }
            }
            seenCodes.add(code);

            String result = (colResult != null ? safe(row.get(colResult)).trim() : "");
            String reasonSan = SanitizeUtil.sanitize(cause);
            String resultSan = SanitizeUtil.sanitize(result);

            if (riskName.length() > MAX_NAME_LEN) {
                riskName = riskName.substring(0, MAX_NAME_LEN);
                nameTruncatedCount++;
            }
            if (code.length() > MAX_CODE_LEN) {
                code = code.substring(0, MAX_CODE_LEN);
                codeTruncatedCount++;
            }

            if (defaultUser == null) {
                errors.add(new ImportError(rowNumber, "Responsável",
                        "Unidade não possui responsável e usuário logado indisponível."));
                skipped++;
                continue;
            }

            try {
                Risk risk = new Risk();
                risk.setId(null);
                risk.setUnit(targetUnit);
                risk.setUser(defaultUser);
                risk.setManager(null);
                risk.setImported(true);
                risk.setArchived(false);
                risk.setBegin(new Date());
                risk.setName(riskName);
                risk.setCode(code);
                risk.setReason(reasonSan);
                risk.setResult(resultSan);
                risk.setPeriodicity(peri);
                risk.setType(type);
                risk.setProbability("");
                risk.setImpact("");
                risk.setTipology("");
                risk.setOtherTipologies("");
                risk.setExistentControllerId(existentControllerId);
                risk.setExecutionControllerId(executionControllerId);
                risk.setResponse(responseId);
                risk.setLevel(orgLevelId);
                risk.setRisk_pdi(false);
                risk.setRisk_pdi_axis(false);
                risk.setRisk_obj_process(false);
                risk.setRisk_act_process(false);
                risk.setRiskLevel(defaultLevel);
                riskBS.saveRisk(risk);
                imported++;
            } catch (Throwable e) {
                errors.add(new ImportError(rowNumber, "Geral", e.getMessage()));
                skipped++;
            }
        }

        warnings.add(String.format(
            "Cabeçalhos mapeados: 'Nome do risco'=\"%s\"; 'Causa'=\"%s\"; 'Periodicidade do monitoramento'=\"%s\"; 'Tipo'=\"%s\"%s%s%s%s.",
            colRiskName, colCause, colPeriodicity, colType,
            colExistingControls != null ? "; 'Controles existentes'=\"" + colExistingControls + "\"" : "",
            colControlsExecution != null ? "; 'Execução dos controles'=\"" + colControlsExecution + "\"" : "",
            colRiskResponse != null ? "; 'Resposta ao risco'=\"" + colRiskResponse + "\"" : "",
            colOrgLevel != null ? "; 'Nível Organizacional'=\"" + colOrgLevel + "\"" : ""
        ));

        if (skipped > 0 || imported >= 0) {
            warnings.add(String.format(
                "Importação concluída: %d linha(s) processada(s); %d importada(s) com sucesso (estado: \"Importado\"); %d ignorada(s) por obrigatórios inválidos (Nome do risco, Causa, Periodicidade do monitoramento ou Tipo).",
                totalProcessed, imported, skippedByRequired));
        }
        if (!emptyRowNumbers.isEmpty())
            warnings.add(String.format("%d linha(s) vazia(s) (linhas: %s).",
                    emptyRowNumbers.size(), sampleRows(emptyRowNumbers, 10)));
        if (!requiredInvalidRows.isEmpty())
            warnings.add(String.format("%d linha(s) com obrigatórios inválidos (linhas: %s).",
                    requiredInvalidRows.size(), sampleRows(requiredInvalidRows, 10)));

        if (!invalidPeriodicity.isEmpty())
            warnings.add("Valores inválidos — 'Periodicidade do monitoramento': " + formatCounterMap(invalidPeriodicity) + ".");
        if (!invalidType.isEmpty())
            warnings.add("Valores inválidos — 'Tipo': " + formatCounterMap(invalidType) + ".");
        if (!invalidRiskResponse.isEmpty())
            warnings.add("Valores inválidos — 'Resposta ao risco': " + formatCounterMap(invalidRiskResponse) + " (campo gravado em branco).");
        if (!invalidOrgLevel.isEmpty())
            warnings.add("Valores inválidos — 'Nível Organizacional': " + formatCounterMap(invalidOrgLevel) + " (campo gravado em branco).");

        if (blanksExistingControls > 0)
            warnings.add(String.format("%d linha(s): valor de \"Controles existentes\" não reconhecido → campo gravado em branco.", blanksExistingControls));
        if (blanksControlsExecution > 0)
            warnings.add(String.format("%d linha(s): valor de \"Execução dos controles\" não reconhecido → campo gravado em branco.", blanksControlsExecution));
        if (blanksRiskResponse > 0)
            warnings.add(String.format("%d linha(s): valor de \"Resposta ao risco\" não reconhecido para o Tipo informado → campo gravado em branco.", blanksRiskResponse));
        if (blanksOrgLevel > 0)
            warnings.add(String.format("%d linha(s): valor de \"Nível Organizacional\" não reconhecido → campo gravado em branco.", blanksOrgLevel));

        if (codeReplacedCount > 0)
            warnings.add(String.format("%d linha(s): código duplicado no CSV — gerado código alternativo automaticamente (linhas: %s).",
                    codeReplacedCount, sampleRows(duplicateCodeRows, 10)));
        if (nameTruncatedCount > 0)
            warnings.add(String.format("%d linha(s): nome excedia o limite — valor truncado para %d caracteres.", nameTruncatedCount, 255));
        if (codeTruncatedCount > 0)
            warnings.add(String.format("%d linha(s): código excedia o limite — valor truncado para %d caracteres.", codeTruncatedCount, 255));

        return new ImportResult(imported, skipped, errors, warnings);
    }

    @Transactional
    public ImportResult importPreventiveActionsCsv(
            MultipartFile file,
            Long planRiskId,
            Long riskId,
            UserSession userSession) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo CSV vazio ou inválido.");
        }
        if (file.getSize() > (3L * 1024 * 1024)) {
            throw new IllegalArgumentException("Tamanho máximo permitido para upload é 3MB.");
        }
        String originalName = file.getOriginalFilename() == null ? ""
                : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!originalName.endsWith(".csv")) {
            throw new IllegalArgumentException("Somente arquivos .csv são aceitos.");
        }

        PlanRisk planRisk = unitBS.exists(planRiskId, PlanRisk.class);
        if (planRisk == null)
            throw new IllegalArgumentException("Plano de Risco inválido.");
        boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
        if (!isSystemAdmin) {
            Long uid = userSession.getAuthData().getId();
            Company userCompany = companyBS.findCompanyByUserId(uid);
            if (userCompany == null || !planRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
                throw new IllegalArgumentException("Acesso negado.");
            }
        }
        domain.validateTenant(planRisk.getPolicy().getCompany());

        Risk risk = riskBS.exists(riskId, Risk.class);
        if (risk == null || risk.isDeleted()) {
            throw new IllegalArgumentException("Risco informado não foi encontrado.");
        }
        Unit riskUnit = risk.getUnit();
        if (riskUnit == null || riskUnit.getPlanRisk() == null
                || !Objects.equals(riskUnit.getPlanRisk().getId(), planRiskId)) {
            throw new IllegalArgumentException("O risco informado não pertence ao plano selecionado.");
        }

        User riskUser = risk.getUser();
        if (riskUser == null) {
            throw new IllegalStateException("O risco informado não possui 'Responsável técnico' definido.");
        }
        User riskManager = risk.getManager();

        List<Map<String, String>> rows = parseCsv(file);
        if (rows.isEmpty()) {
            return new ImportResult(0, 0, Collections.emptyList(),
                    Collections.singletonList("CSV sem registros: nenhuma linha foi processada."));
        }

        Set<String> headers = rows.get(0).keySet();
        String colAction = pickHeader(headers, "Ação", "Acao", "Ação de prevenção", "Ação/Descrição", "Descricao", "Descrição");
        String colBeginDate = pickHeader(headers, "Data início", "Data inicio", "Data de início", "Início", "Validade início", "Validade Início", "validity_begin");
        String colEndDate = pickHeader(headers, "Data fim", "Data de fim", "Fim", "Validade fim", "Validade Fim", "validity_end");
        String colFileLink = pickHeader(headers, "Arquivo", "Anexo", "Link do arquivo", "file", "fileLink", "Arquivo (link)");

        if (colAction == null) {
            throw new IllegalArgumentException("Cabeçalho obrigatório ausente: 'Ação'. Encontrados: " + headers);
        }

        final int totalProcessed = rows.size();
        int imported = 0, skipped = 0, skippedEmptyLine = 0, ignoredInvalidDates = 0;
        int truncatedAction = 0;
        final int MAX_ACTION_LEN = 4000;

        List<ImportError> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> r = rows.get(i);
            int rowNumber = i + 2;

            boolean allEmpty = true;
            for (String v : r.values()) {
                if (v != null && !v.trim().isEmpty()) { allEmpty = false; break; }
            }
            if (allEmpty) {
                errors.add(new ImportError(rowNumber, "Geral", "Linha vazia."));
                skipped++; skippedEmptyLine++;
                continue;
            }

            String actionTextRaw = safe(r.get(colAction)).trim();
            if (actionTextRaw.isEmpty()) {
                errors.add(new ImportError(rowNumber, colAction, "Ação é obrigatória."));
                skipped++;
                continue;
            }

            String actionText = SanitizeUtil.sanitize(actionTextRaw);
            if (actionText.length() > MAX_ACTION_LEN) {
                actionText = actionText.substring(0, MAX_ACTION_LEN);
                truncatedAction++;
            }

            Date validityBegin = parseFlexibleDate(safe(colBeginDate == null ? "" : r.get(colBeginDate)).trim());
            Date validityEnd   = parseFlexibleDate(safe(colEndDate   == null ? "" : r.get(colEndDate)).trim());

            if (validityBegin != null && validityEnd != null && validityEnd.before(validityBegin)) {
                validityBegin = null;
                validityEnd = null;
                ignoredInvalidDates++;
            }

            String fileLink = safe(colFileLink == null ? "" : r.get(colFileLink)).trim();

            try {
                PreventiveAction pa = new PreventiveAction();
                pa.setId(null);
                pa.setRisk(risk);
                pa.setUser(riskUser);
                pa.setManager(riskManager);
                pa.setAction(actionText);
                pa.setAccomplished(false);
                pa.setValidityBegin(validityBegin);
                pa.setValidityEnd(validityEnd);
                if (!fileLink.isEmpty()) pa.setFileLink(fileLink);

                preventiveActionBS.saveAction(pa);
                imported++;

            } catch (Throwable e) {
                errors.add(new ImportError(rowNumber, "Geral", e.getMessage()));
                skipped++;
            }
        }

        warnings.add(String.format("Importação concluída: %d linha(s) processada(s); %d importada(s) com sucesso; %d ignorada(s).",
                totalProcessed, imported, skipped));
        if (skippedEmptyLine > 0) {
            warnings.add(String.format("%d linha(s) ignorada(s) por estarem vazias.", skippedEmptyLine));
        }
        if (ignoredInvalidDates > 0) {
            warnings.add(String.format(
                    "%d linha(s): \"Data fim\" anterior à \"Data início\" — vigência ignorada (datas em branco).",
                    ignoredInvalidDates));
        }
        if (truncatedAction > 0) {
            warnings.add(String.format(
                    "%d linha(s): texto de \"Ação\" excedia o limite — valor truncado para %d caracteres.",
                    truncatedAction, MAX_ACTION_LEN));
        }

        return new ImportResult(imported, skipped, errors, warnings);
    }

    /**
     * Procura por um cabeçalho em um conjunto, permitindo múltiplos nomes (candidatos).
     * Esta versão modificada usa stripInvisibles() para remover BOM (Byte Order Mark)
     * e outros caracteres invisíveis antes da comparação.
     *
     * @param header     O conjunto de cabeçalhos lidos do arquivo (ex: pode conter "﻿Nome do risco")
     * @param candidates Os nomes de cabeçalho válidos que procuramos (ex: "Nome do risco", "Causa(s)")
     * @return O nome do cabeçalho *original* encontrado no conjunto, ou null se nenhum candidato for encontrado.
     */
    private String pickHeader(Set<String> header, String... candidates) {
        if (header == null || header.isEmpty())
            return null;

        for (String candidate : candidates) {
            if (candidate == null) continue;

            String cleanedCandidate = stripInvisibles(candidate).trim();

            for (String h : header) {
                if (h == null) continue;
                String cleanedHeader = stripInvisibles(h).trim();

                if (cleanedHeader.equalsIgnoreCase(cleanedCandidate)) {
                    return h;
                }
            }
        }
        return null;
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .withDelimiter(DELIM)
                     .withFirstRecordAsHeader()
                     .withIgnoreEmptyLines()
                     .withTrim()
                     .parse(reader)) {
            List<Map<String, String>> rows = new ArrayList<>();
            for (CSVRecord rec : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                for (String h : parser.getHeaderMap().keySet()) {
                    row.put(h, rec.get(h));
                }
                rows.add(row);
            }
            return rows;
        }
    }

    private void ensureHeaders(List<Map<String, String>> rows, String... required) {
        if (rows.isEmpty())
            throw new IllegalArgumentException("CSV sem registros.");
        Set<String> header = rows.get(0).keySet().stream()
                .map(s -> s == null ? "" : s.trim())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<String> missing = new ArrayList<>();
        for (String r : required) {
            if (!header.contains(r)) missing.add(r);
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cabeçalhos obrigatórios ausentes: " + String.join(", ", missing) + ". Encontrados: " + header);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static final String[] DATE_PATTERNS = {
            "yyyy-MM-dd",
            "dd/MM/yyyy"
    };

    private static Date parseFlexibleDate(String text) throws ParseException {
        if (text == null || text.trim().isEmpty()) return null;
        String t = text.trim();
        ParseException last = null;
        for (String p : DATE_PATTERNS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p);
                sdf.setLenient(false);
                return sdf.parse(t);
            } catch (ParseException e) {
                last = e;
            }
        }
        throw last != null ? last : new ParseException("Formato de data inválido: " + text, 0);
    }

    private static String sampleRows(List<Integer> rows, int limit) {
        if (rows == null || rows.isEmpty()) return "";
        String head = rows.stream().limit(limit).map(String::valueOf).collect(Collectors.joining(", "));
        return head + (rows.size() > limit ? " … (+" + (rows.size() - limit) + ")" : "");
    }

    private static String formatCounterMap(Map<String,Integer> map) {
        if (map == null || map.isEmpty()) return "{}";
        return map.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\": " + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
