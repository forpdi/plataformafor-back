package org.forpdi.core.csvs;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.forpdi.core.common.PaginatedList;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.incident.Incident;
import org.forrisco.risk.incident.IncidentBS;
import org.forrisco.risk.incident.dto.FilterIncidentsDto;

@Service
public class CsvIncidentExport {

    @Autowired private CsvExportHelper helper;
    @Autowired private IncidentBS incidentBS;
    @Autowired private RiskBS riskBS;

    public void exportIncidentsCsv(PlanRisk plan, FilterIncidentsDto req, OutputStream out) throws IOException {
        List<Incident> incidents = findFilteredIncidents(plan, req);

        final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');

            writer.write(
                helper.fromStringToCsvFieldFormat("Unidade") +
                helper.fromStringToCsvFieldFormat("Subunidade") +
                helper.fromStringToCsvFieldFormat("Risco") +
                helper.fromStringToCsvFieldFormat("Código") +
                helper.fromStringToCsvFieldFormat("Tipo do Risco") +
                helper.fromStringToCsvFieldFormat("Incidente") +
                helper.fromStringToCsvFieldFormat("Ação corretiva") +
                helper.fromStringToCsvFieldFormat("Tipo do Incidente") +
                helper.fromStringToCsvFieldFormat("Data/Hora") +
                '\n'
            );

            for (Incident inc : incidents) {
                Risk r = inc.getRisk();
                Unit u  = (r != null) ? r.getUnit() : null;

                String unidade = "";
                String subunidade = "";
                if (u != null) {
                    if (u.getParent() != null) {
                        unidade = safe(u.getParent().getName());
                        subunidade = safe(u.getName());
                    } else {
                        unidade = safe(u.getName());
                    }
                }

                String riskName = (r != null) ? safe(r.getName()) : "";
                String riskCode = (r != null) ? safe(r.getCode()) : "";
                String riskType = (r != null) ? safe(r.getType()) : "";

                String incidentText = stripHtml(inc.getDescription());
                String corrective   = safe(inc.getAction());

                int t = inc.getType();
                String typeLabel = (t == 1) ? "Ameaça" : (t == 2) ? "Oportunidade" : "";

                String dateTime = (inc.getBegin() != null) ? df.format(inc.getBegin()) : "";

                writer.write(
                    helper.fromStringToCsvFieldFormat(unidade) +
                    helper.fromStringToCsvFieldFormat(subunidade) +
                    helper.fromStringToCsvFieldFormat(riskName) +
                    helper.fromStringToCsvFieldFormat(riskCode) +
                    helper.fromStringToCsvFieldFormat(riskType) +
                    helper.fromStringToCsvFieldFormat(incidentText) +
                    helper.fromStringToCsvFieldFormat(corrective) +
                    helper.fromStringToCsvFieldFormat(typeLabel) +
                    helper.fromStringToCsvFieldFormat(dateTime) +
                    '\n'
                );
            }
            writer.flush();
        }
    }

    // --------- mesma lógica de /incident/filter, sem paginação ---------
    private List<Incident> findFilteredIncidents(PlanRisk plan, FilterIncidentsDto dto) {
        final Set<Long> unitFilter = (dto.getUnitIds() == null) ? Collections.emptySet() : new HashSet<>(dto.getUnitIds());
        final String needle = (dto.getIncident() == null) ? null : dto.getIncident().trim().toLowerCase();
        final Integer type = dto.getType();
        final Date start = dto.getStartCreation();
        final Date end = dto.getEndCreation();

        List<Incident> all = new ArrayList<>();
        if (!unitFilter.isEmpty()) {
            for (Long unitId : unitFilter) {
                Unit unit = this.riskBS.exists(unitId, Unit.class);
                if (unit == null) continue;
                if (!unit.getPlanRisk().getId().equals(plan.getId())) continue;

                PaginatedList<Risk> risks = this.riskBS.listRiskByUnit(unit);
                for (Risk risk : risks.getList()) {
                    PaginatedList<Incident> incs = this.incidentBS.listIncidentsByRisk(risk);
                    all.addAll(incs.getList());
                }
            }
        }

        return all.stream().filter(inc -> {
            Risk risk = inc.getRisk();
            if (risk == null || risk.getUnit() == null || risk.getUnit().getPlanRisk() == null) return false;
            if (!risk.getUnit().getPlanRisk().getId().equals(plan.getId())) return false;

            // unidade/subunidade
            if (!unitFilter.isEmpty()) {
                Unit u = risk.getUnit();
                Long uid = u.getId();
                Long pid = (u.getParent() != null ? u.getParent().getId() : null);
                if (!unitFilter.contains(uid) && (pid == null || !unitFilter.contains(pid))) return false;
            }

            // texto
            if (needle != null && !needle.isEmpty()) {
                String desc = (inc.getDescription() != null ? inc.getDescription().toLowerCase() : "");
                String action = (inc.getAction() != null ? inc.getAction().toLowerCase() : "");
                if (!desc.contains(needle) && !action.contains(needle)) return false;
            }

            // tipo (Integer vs int)
            if (type != null) {
                if (inc.getType() != type.intValue()) return false;
            }

            // datas
            Date begin = inc.getBegin();
            if (start != null && (begin == null || begin.before(start))) return false;
            if (end != null && (begin == null || begin.after(end))) return false;

            return true;
        }).collect(Collectors.toList());
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").trim();
    }
}
