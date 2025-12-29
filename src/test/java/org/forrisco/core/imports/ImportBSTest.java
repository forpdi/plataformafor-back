package org.forrisco.core.imports;

import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.user.User;
import org.forpdi.security.auth.AuthData;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.policy.Policy;
import org.forrisco.core.process.ProcessBS;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.preventiveaction.PreventiveActionBS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportBSTest {

    @Mock
    private UnitBS unitBS;
    @Mock
    private CompanyBS companyBS;
    @Mock
    private CompanyDomainContext domain;
    @Mock
    private ProcessBS processBS;
    @Mock
    private RiskBS riskBS;
    @Mock
    private PreventiveActionBS preventiveActionBS;
    @Mock
    private UserSession userSession;

    @InjectMocks
    private ImportBS importBS;

    private PlanRisk planRisk;
    private User user;
    private AuthData authData;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(1L);
        Policy policy = new Policy();
        policy.setCompany(company);

        planRisk = new PlanRisk();
        planRisk.setId(10L);
        planRisk.setPolicy(policy);

        user = new User();
        user.setId(99L);
        user.setName("Tester");
        user.setEmail("tester@example.com");
        user.setAccessLevel(AccessLevels.SYSTEM_ADMIN.getLevel());

        authData = new AuthData(user);

        when(userSession.getAccessLevel()).thenReturn(AccessLevels.SYSTEM_ADMIN.getLevel());
        when(userSession.getAuthData()).thenReturn(authData);
        when(unitBS.exists(eq(planRisk.getId()), eq(PlanRisk.class))).thenReturn(planRisk);
        when(unitBS.exists(eq(user.getId()), eq(User.class))).thenReturn(user);
        doNothing().when(domain).validateTenant(any(Company.class));
    }

    @Test
    void importUnitsCsv_shouldImportRootUnitsAndKeepWarnings() throws Exception {
        MockMultipartFile csv = new MockMultipartFile(
                "file",
                "units.csv",
                "text/csv",
                ("Nome da Unidade;Sigla;Descrição\n" +
                        "Unidade 1;U1;Descricao 1\n" +
                        "Unidade 2;U2; ").getBytes(StandardCharsets.UTF_8)
        );

        ImportResult result = importBS.importUnitsCsv(csv, planRisk.getId(), userSession);

        assertEquals(2, result.getImported());
        assertEquals(0, result.getSkipped());
        assertFalse(result.getWarnings().isEmpty(), "Warnings should include import summary");

        ArgumentCaptor<Unit> captor = ArgumentCaptor.forClass(Unit.class);
        verify(unitBS, times(2)).save(captor.capture());
        List<Unit> saved = captor.getAllValues();

        assertTrue(saved.stream().allMatch(u -> u.getParent() == null), "Root import must not set parent");
        assertTrue(saved.stream().allMatch(u -> planRisk.equals(u.getPlanRisk())), "PlanRisk must be kept");
        assertTrue(saved.stream().anyMatch(u -> "U1".equals(u.getAbbreviation())));
    }

    @Test
    void importSubunitsCsv_shouldAttachParent() throws Exception {
        Unit parent = new Unit();
        parent.setId(20L);
        parent.setPlanRisk(planRisk);

        when(unitBS.exists(eq(parent.getId()), eq(Unit.class))).thenReturn(parent);

        MockMultipartFile csv = new MockMultipartFile(
                "file",
                "subs.csv",
                "text/csv",
                ("Nome da Unidade;Sigla;Descrição\n" +
                        "Sub;SUB;Descricao").getBytes(StandardCharsets.UTF_8)
        );

        ImportResult result = importBS.importSubunitsCsv(csv, planRisk.getId(), parent.getId(), userSession);

        assertEquals(1, result.getImported());
        verify(unitBS).save(argThat(u ->
                parent.equals(u.getParent()) && planRisk.equals(u.getPlanRisk())));
    }

    @Test
    void importUnitsCsv_shouldFailWhenRequiredHeaderMissing() throws Exception {
        MockMultipartFile csv = new MockMultipartFile(
                "file",
                "broken.csv",
                "text/csv",
                ("Nome da Unidade;Descrição\n" +
                        "Unidade 1;Descricao").getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(IllegalArgumentException.class,
                () -> importBS.importUnitsCsv(csv, planRisk.getId(), userSession));
    }
}
