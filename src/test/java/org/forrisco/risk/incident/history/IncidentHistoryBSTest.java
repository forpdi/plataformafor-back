package org.forrisco.risk.incident.history;

import org.forpdi.core.user.User;
import org.forrisco.risk.incident.Incident;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class IncidentHistoryBSTest {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    private static class Capture {
        final List<IncidentHistory> saved = new ArrayList<>();
        final IncidentHistoryBS bs;

        Capture() {
            bs = spy(new IncidentHistoryBS());
            lenient().doAnswer(inv -> {
                Object arg = inv.getArgument(0);
                if (arg instanceof IncidentHistory h) {
                    saved.add(h);
                }
                return null;
            }).when(bs).persist(any());
        }
    }

    private Incident buildIncident(Date begin, int type, String descHtml, String actionHtml,
            String userName, String managerName) {
        Incident inc = new Incident();
        inc.setBegin(begin);
        inc.setType(type);
        inc.setDescription(descHtml);
        inc.setAction(actionHtml);

        if (userName != null) {
            User u = new User();
            u.setName(userName);
            inc.setUser(u);
        }
        if (managerName != null) {
            User m = new User();
            m.setName(managerName);
            inc.setManager(m);
        }
        return inc;
    }

    @Test
    @DisplayName("save: marca deleted=false e persiste")
    void save_setsDeletedFalseAndPersists() {
        Capture cap = new Capture();

        IncidentHistory h = new IncidentHistory();
        h.setDeleted(true);
        cap.bs.save(h);

        assertEquals(1, cap.saved.size(), "Deveria persistir 1 registro");
        assertFalse(cap.saved.get(0).isDeleted(), "Registro salvo deve vir com deleted=false");
    }

    @Test
    @DisplayName("saveIncidentChanges: cria histórico para cada campo alterado (desc, ação, data, resp técnico, gestor)")
    void saveIncidentChanges_createsRecordsPerChangedField() {
        Capture cap = new Capture();

        Date oldDate = new Date(System.currentTimeMillis() - 2 * 60_000);
        Date newDate = new Date(System.currentTimeMillis());

        Incident oldInc = buildIncident(oldDate, 1, "<b>Alpha</b>", "<p>Act A</p>", "OldUser", "OldMgr");
        Incident newInc = buildIncident(newDate, 1, "Beta", "Act B", "NewUser", "NewMgr");

        User responsible = new User();
        responsible.setName("Resp");

        cap.bs.saveIncidentChanges(oldInc, newInc, responsible);

        assertTrue(cap.saved.size() >= 5, "Deveria registrar pelo menos 5 mudanças");

        Map<String, IncidentHistory> byField = new HashMap<>();
        for (IncidentHistory h : cap.saved) {
            byField.put(h.getChangedField(), h);
            assertSame(oldInc, h.getIncident(), "Incident associado deve ser o 'oldInc'");
            assertEquals("Resp", h.getResponsibleUser().getName(), "Usuário responsável deve ser o informado");
            assertNotNull(h.getChangeDate());
        }

        IncidentHistory hDesc = byField.get("Descrição");
        assertNotNull(hDesc, "Mudança em 'Descrição' deveria existir");
        assertEquals("Alpha", hDesc.getOldValue());
        assertEquals("Beta", hDesc.getNewValue());

        IncidentHistory hAct = byField.get("Ação Corretiva");
        assertNotNull(hAct, "Mudança em 'Ação Corretiva' deveria existir");
        assertEquals("Act A", hAct.getOldValue());
        assertEquals("Act B", hAct.getNewValue());

        String oldFmt = oldDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DF);
        String newFmt = newDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DF);
        IncidentHistory hDate = byField.get("Data/Hora");
        assertNotNull(hDate, "Mudança em 'Data/Hora' deveria existir");
        assertEquals(oldFmt, hDate.getOldValue());
        assertEquals(newFmt, hDate.getNewValue());

        IncidentHistory hResp = byField.get("Responsável Técnico");
        assertNotNull(hResp, "Mudança em 'Responsável Técnico' deveria existir");
        assertEquals("OldUser", hResp.getOldValue());
        assertEquals("NewUser", hResp.getNewValue());

        IncidentHistory hMgr = byField.get("Gestor");
        assertNotNull(hMgr, "Mudança em 'Gestor' deveria existir");
        assertEquals("OldMgr", hMgr.getOldValue());
        assertEquals("NewMgr", hMgr.getNewValue());
    }

    @Test
    @DisplayName("saveIncidentChanges: não cria histórico quando nada mudou (HTML diferente mas texto igual não conta)")
    void saveIncidentChanges_noChanges_createsNothing() {
        Capture cap = new Capture();

        Date d = new Date();

        Incident oldInc = buildIncident(d, 1, "<b>Igual</b>", "<i>Mesma</i>", "User", "Mgr");
        Incident newInc = buildIncident(d, 1, "Igual", "Mesma", "User", "Mgr");

        User responsible = new User();
        responsible.setName("Resp");

        cap.bs.saveIncidentChanges(oldInc, newInc, responsible);

        assertEquals(0, cap.saved.size(), "Nenhum histórico deveria ser criado quando não há mudanças");
    }

    @Test
    @DisplayName("IncidentHistoryDTO: mapeia e formata corretamente")
    void dto_mapsAndFormats() {
        IncidentHistory h = new IncidentHistory();
        h.setId(77L);
        User ru = new User();
        ru.setName("Maria");
        h.setResponsibleUser(ru);
        h.setChangedField("Campo X");
        h.setOldValue("Antes");
        h.setNewValue("Depois");
        LocalDateTime when = LocalDateTime.of(2025, 1, 2, 3, 4);
        h.setChangeDate(when);

        IncidentHistoryBS.IncidentHistoryDTO dto = new IncidentHistoryBS.IncidentHistoryDTO(h);

        assertEquals(77L, dto.getId());
        assertEquals("Maria", dto.getResponsibleUser());
        assertEquals("Campo X", dto.getChangedField());
        assertEquals("Antes", dto.getOldValue());
        assertEquals("Depois", dto.getNewValue());
        assertEquals(when.format(DF), dto.getChangeDate());
    }
}
