package org.forrisco.core.policy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ItemToSelect;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.common.AbstractController;
import org.forpdi.system.reports.frisco.PolicyReportGenerator;
import org.forpdi.system.reports.frisco.PolicyReportGenerator.Params;
import org.forrisco.core.item.FieldItem;
import org.forrisco.core.item.Item;
import org.forrisco.core.item.ItemBS;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskLevel;
import org.forrisco.core.policy.dto.PolicyDto;
import org.forrisco.core.policy.dto.PolicyExportRequestDto;
import org.forpdi.security.auth.AuthData;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.http.ResponseEntity;

class PolicyControllerTest {

    @InjectMocks
    private PolicyController controller;

    @Mock private CompanyDomainContext domain;
    @Mock private PolicyBS policyBS;
    @Mock private ItemBS itemBS;
    @Mock private RiskBS riskBS;
    @Mock private PolicyReportGenerator policyReportGenerator;
    @Mock private CompanyBS companyBS;

    private MockHttpServletResponse servletResp;

    private static Company company(long id, String name) {
        Company c = new Company();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private static UserSession userSession(long userId, int accessLevel) {
        UserSession us = mock(UserSession.class, Mockito.RETURNS_DEEP_STUBS);
        when(us.getAccessLevel()).thenReturn(accessLevel);
        AuthData ad = mock(AuthData.class);
        when(ad.getId()).thenReturn(userId);
        when(ad.getName()).thenReturn("tester");
        when(us.getAuthData()).thenReturn(ad);
        return us;
    }

    private static <T extends Serializable> PaginatedList<T> page(List<T> list) {
        PaginatedList<T> p = new PaginatedList<>();
        p.setList(list);
        p.setTotal(list == null ? 0L : (long) list.size());
        return p;
    }

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        servletResp = new MockHttpServletResponse();
        Field r = AbstractController.class.getDeclaredField("response");
        r.setAccessible(true);
        r.set(controller, servletResp);

        CompanyDomain tenant = mock(CompanyDomain.class);
        when(domain.get()).thenReturn(tenant);
        when(tenant.getCompany()).thenReturn(company(10L, "Comp-10"));
    }

    @Test
    @DisplayName("savePolicy: sucesso (admin) — valida, salva, cria níveis de risco e retorna 200")
    void savePolicy_success() {
        PolicyDto dto = mock(PolicyDto.class);
        Policy policy = new Policy();
        policy.setDescription("<b>desc</b>");
        when(dto.policy()).thenReturn(policy);

        ResponseEntity<?> resp = controller.savePolicy(dto);

        assertEquals(200, resp.getStatusCodeValue());
        verify(policyBS).validateDates(policy);
        verify(policyBS).save(policy);
        verify(riskBS).saveRiskLevel(policy);
        assertNotNull(policy.getCompany(), "Deveria setar a empresa do tenant");
    }

    @Test
    @DisplayName("listMacrosArchived: sucesso (200) com domain definido")
    void listMacrosArchived_success() {
        when(policyBS.listPolicies(any(Company.class), eq(true), any(DefaultParams.class)))
                .thenReturn(page(List.of(new Policy())));

        ResponseEntity<?> resp = controller.listMacrosArchived(new DefaultParams());

        assertEquals(200, resp.getStatusCodeValue());
        verify(policyBS).listPolicies(any(Company.class), eq(true), any(DefaultParams.class));
    }

    @Test
    @DisplayName("listPolicyUnarchived: sucesso (200) retorna listPoliciesHasPlans")
    void listPolicyUnarchived_success() {
        PaginatedList<Policy> pols = page(List.of(new Policy()));
        PaginatedList<Policy> withPlans = page(List.of(new Policy()));
        when(policyBS.listPolicies(any(Company.class), eq(false), any(DefaultParams.class))).thenReturn(pols);
        when(policyBS.listPoliciesHasPlans(pols)).thenReturn(withPlans);

        ResponseEntity<?> resp = controller.listPolicyUnarchived(new DefaultParams());

        assertEquals(200, resp.getStatusCodeValue());
        verify(policyBS).listPoliciesHasPlans(pols);
    }

    @Test
    @DisplayName("listToSelect: sucesso (200) retorna itens")
    void listToSelect_success() {
        when(policyBS.listPoliciesToSelect(any(Company.class)))
                .thenReturn(List.of(new ItemToSelect(1L, "P1"), new ItemToSelect(2L, "P2")));

        ResponseEntity<?> resp = controller.listToSelect();

        assertEquals(200, resp.getStatusCodeValue());
        verify(policyBS).listPoliciesToSelect(any(Company.class));
    }

    @Test
    @DisplayName("retrievePolicy: 400 quando não encontra a política")
    void retrievePolicy_notFound() {
        when(policyBS.exists(999L, Policy.class)).thenReturn(null);

        ResponseEntity<?> resp = controller.retrievePolicy(999L,
                userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("retrievePolicy: 400 (Acesso negado) quando não-admin de outra empresa")
    void retrievePolicy_forbidden_nonAdmin() {
        Policy pol = new Policy();
        pol.setId(50L);
        pol.setCompany(company(7L, "C7"));
        when(policyBS.exists(50L, Policy.class)).thenReturn(pol);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(8L, "C8"));

        ResponseEntity<?> resp = controller.retrievePolicy(50L,
                userSession(123L, AccessLevels.MANAGER.getLevel()));

        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("retrievePolicy: sucesso (admin) seta hasLinkedPlans e retorna 200")
    void retrievePolicy_success_admin() {
        Policy pol = new Policy();
        pol.setId(10L);
        pol.setCompany(company(10L, "Comp-10"));
        when(policyBS.exists(10L, Policy.class)).thenReturn(pol);
        when(policyBS.hasLinkedPlans(pol)).thenReturn(true);

        ResponseEntity<?> resp = controller.retrievePolicy(10L,
                userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(200, resp.getStatusCodeValue());
        verify(policyBS).hasLinkedPlans(pol);
    }

    @Test
    @DisplayName("retrieveRiskLevel: sucesso (200) com lista paginada")
    void retrieveRiskLevel_success() {
        Policy pol = new Policy();
        pol.setId(7L);
        pol.setCompany(company(10L, "Comp-10"));
        when(riskBS.exists(7L, Policy.class)).thenReturn(pol);
        when(riskBS.listRiskLevelByPolicy(pol)).thenReturn(page(List.of(new RiskLevel(), new RiskLevel())));

        ResponseEntity<?> resp = controller.retrieveRiskLevel(7L);

        assertEquals(200, resp.getStatusCodeValue());
        verify(riskBS).listRiskLevelByPolicy(pol);
    }

    @Test
    @DisplayName("retrieveRiskLevel: 400 quando política não encontrada")
    void retrieveRiskLevel_notFound() {
        when(riskBS.exists(77L, Policy.class)).thenReturn(null);

        ResponseEntity<?> resp = controller.retrieveRiskLevel(77L);

        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("deletePolicy: 404 quando política inexistente")
    void deletePolicy_notFound() {
        when(policyBS.exists(5L, Policy.class)).thenReturn(null);

        ResponseEntity<?> resp = controller.deletePolicy(5L,
                userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("deletePolicy: 400 (Acesso negado) quando não-admin de outra empresa")
    void deletePolicy_forbidden_nonAdmin() {
        Policy pol = new Policy();
        pol.setId(5L);
        pol.setCompany(company(1L, "C1"));
        when(policyBS.exists(5L, Policy.class)).thenReturn(pol);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(2L, "C2"));

        ResponseEntity<?> resp = controller.deletePolicy(5L,
                userSession(123L, AccessLevels.MANAGER.getLevel()));

        assertEquals(400, resp.getStatusCodeValue());
        verify(policyBS, never()).delete(any());
    }

    @Test
    @DisplayName("deletePolicy: 400 quando existem planos de risco vinculados")
    void deletePolicy_hasLinkedPlans() {
        Policy pol = new Policy();
        pol.setId(5L);
        pol.setCompany(company(10L, "Comp-10"));
        when(policyBS.exists(5L, Policy.class)).thenReturn(pol);
        when(policyBS.listPlanbyPolicy(pol)).thenReturn(page(List.of(new PlanRisk())));

        ResponseEntity<?> resp = controller.deletePolicy(5L,
                userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(400, resp.getStatusCodeValue());
        verify(policyBS, never()).delete(pol);
    }

    @Test
    @DisplayName("deletePolicy: sucesso (200) remove campos, subitens, itens, níveis de risco e a política")
    void deletePolicy_success() {
        Policy pol = new Policy();
        pol.setId(9L);
        pol.setCompany(company(10L, "Comp-10"));
        when(policyBS.exists(9L, Policy.class)).thenReturn(pol);

        when(policyBS.listPlanbyPolicy(pol)).thenReturn(page(List.of()));
        RiskLevel rl1 = new RiskLevel();
        RiskLevel rl2 = new RiskLevel();
        when(policyBS.listRiskLevelbyPolicy(pol)).thenReturn(page(List.of(rl1, rl2)));
        Item it1 = new Item();
        Item it2 = new Item();
        when(itemBS.listItensByPolicy(pol)).thenReturn(page(List.of(it1, it2)));
        FieldItem f1 = new FieldItem();
        FieldItem f2 = new FieldItem();
        when(itemBS.listFieldsByItem(it1)).thenReturn(page(List.of(f1)));
        when(itemBS.listFieldsByItem(it2)).thenReturn(page(List.of(f2)));

        ResponseEntity<?> resp = controller.deletePolicy(9L,
                userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(200, resp.getStatusCodeValue());
        verify(itemBS).delete(f1);
        verify(itemBS).delete(f2);
        verify(itemBS).deleteSubitens(it1);
        verify(itemBS).deleteSubitens(it2);
        verify(itemBS).delete(it1);
        verify(itemBS).delete(it2);
        verify(riskBS).delete(rl1);
        verify(riskBS).delete(rl2);
        verify(policyBS).delete(pol);
    }

    @Test
    @DisplayName("policy(update): 400 quando política inexistente")
    void updatePolicy_notFound() {
        PolicyDto dto = mock(PolicyDto.class);
        Policy p = new Policy();
        p.setId(99L);
        when(dto.policy()).thenReturn(p);
        when(policyBS.exists(99L, Policy.class)).thenReturn(null);

        ResponseEntity<?> resp = controller.policy(dto,
                userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("policy(update): 400 (Acesso negado) quando não-admin de outra empresa")
    void updatePolicy_forbidden_nonAdmin() {
        Policy existent = new Policy();
        existent.setId(77L);
        existent.setCompany(company(1L, "C1"));
        when(policyBS.exists(77L, Policy.class)).thenReturn(existent);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(2L, "C2"));

        PolicyDto dto = mock(PolicyDto.class);
        Policy incoming = new Policy();
        incoming.setId(77L);
        when(dto.policy()).thenReturn(incoming);

        ResponseEntity<?> resp = controller.policy(dto,
                userSession(123L, AccessLevels.MANAGER.getLevel()));

        assertEquals(400, resp.getStatusCodeValue());
        verify(policyBS, never()).persist(any());
    }

    @Test
    @DisplayName("policy(update): 400 quando possui planos vinculados")
    void updatePolicy_hasLinkedPlans() {
        Policy existent = new Policy();
        existent.setId(70L);
        existent.setCompany(company(10L, "Comp-10"));
        when(policyBS.exists(70L, Policy.class)).thenReturn(existent);
        when(policyBS.hasLinkedPlans(existent)).thenReturn(true);

        PolicyDto dto = mock(PolicyDto.class);
        Policy incoming = new Policy();
        incoming.setId(70L);
        when(dto.policy()).thenReturn(incoming);

        ResponseEntity<?> resp = controller.policy(dto,
                userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(400, resp.getStatusCodeValue());
        verify(policyBS, never()).persist(any());
    }

    // @Test #Correção no teste abaixo
    // @DisplayName("policy(update): sucesso (200) apaga níveis antigos, recria, e persiste alterações")
    // void updatePolicy_success() {
    //     // ## ARRANGE: Prepara o cenário ##

    //     // 1. Simula a política que já existe no banco
    //     Policy existent = new Policy();
    //     existent.setId(70L);
    //     existent.setCompany(company(10L, "Comp-10"));
    //     when(policyBS.exists(70L, Policy.class)).thenReturn(existent);
    //     when(policyBS.hasLinkedPlans(existent)).thenReturn(false);

    //     // 2. Simula os níveis de risco antigos que serão removidos
    //     RiskLevel rl1 = new RiskLevel();
    //     RiskLevel rl2 = new RiskLevel();
    //     when(riskBS.listRiskLevelByPolicy(existent)).thenReturn(page(List.of(rl1, rl2)));

    //     // 3. Cria a entidade 'Policy' com os dados da requisição de atualização
    //     // Não adicionamos nenhuma lista aqui, pois a classe Policy não possui uma.
    //     Policy incoming = new Policy();
    //     incoming.setId(70L);
    //     incoming.setName("Novo Nome");
    //     incoming.setDescription("Nova Desc");

    //     // 4. (CORREÇÃO) Cria um DTO real que encapsula a entidade 'Policy'
    //     // Isso substitui o mock e simula uma requisição válida.
    //     PolicyDto dto = new PolicyDto(incoming);

    //     // ## ACT: Executa a ação ##
    //     ResponseEntity<?> resp = controller.policy(dto,
    //             userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

    //     // ## ASSERT: Verifica o resultado ##
    //     assertEquals(200, resp.getStatusCodeValue(), "A resposta deveria ser 200 OK");

    //     // Verifica se os níveis antigos foram deletados
    //     verify(riskBS).delete(rl1);
    //     verify(riskBS).delete(rl2);

    //     // Verifica se o método para salvar os novos níveis foi chamado,
    //     // passando o objeto 'incoming' que contém os dados para a criação.
    //     verify(riskBS).saveRiskLevel(incoming);

    //     // Garante que a política existente foi atualizada ANTES de persistir
    //     ArgumentCaptor<Policy> policyCaptor = ArgumentCaptor.forClass(Policy.class);
    //     verify(policyBS).persist(policyCaptor.capture());
    //     assertEquals("Novo Nome", policyCaptor.getValue().getName());
    //     assertEquals("Nova Desc", policyCaptor.getValue().getDescription());
    // }

    @Test
    @DisplayName("exportreport: sucesso (204) streama PDF no HttpServletResponse")
    void exportReport_success() throws Exception {
        Policy pol = new Policy();
        pol.setId(5L);
        pol.setCompany(company(10L, "Comp-10"));
        when(policyBS.exists(5L, Policy.class)).thenReturn(pol);

        PolicyExportRequestDto req = mock(PolicyExportRequestDto.class);
        when(req.policyId()).thenReturn(5L);
        when(req.title()).thenReturn("Relatório");
        when(req.itens()).thenReturn("1,2,3");
        when(req.subitens()).thenReturn("4,5");

        byte[] pdf = "PDFDATA".getBytes(StandardCharsets.UTF_8);
        when(policyReportGenerator.generateReport(any(Params.class)))
                .thenReturn(new ByteArrayInputStream(pdf));

        ResponseEntity<?> resp = controller.exportreport(req,
                userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(204, resp.getStatusCodeValue());
        assertEquals("application/pdf", servletResp.getHeader("Content-Type"));
        assertTrue(servletResp.getContentAsString().contains("PDFDATA"));
    }

    @Test
    @DisplayName("exportreport: 400 quando política não encontrada")
    void exportReport_notFound() {
        when(policyBS.exists(55L, Policy.class)).thenReturn(null);

        PolicyExportRequestDto req = mock(PolicyExportRequestDto.class);
        when(req.policyId()).thenReturn(55L);

        ResponseEntity<?> resp = controller.exportreport(req,
                userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("exportreport: 400 (Acesso negado) quando não-admin de outra empresa")
    void exportReport_forbidden_nonAdmin() {
        Policy pol = new Policy();
        pol.setId(60L);
        pol.setCompany(company(7L, "C7"));
        when(policyBS.exists(60L, Policy.class)).thenReturn(pol);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(8L, "C8"));

        PolicyExportRequestDto req = mock(PolicyExportRequestDto.class);
        when(req.policyId()).thenReturn(60L);

        ResponseEntity<?> resp = controller.exportreport(req,
                userSession(123L, AccessLevels.MANAGER.getLevel()));

        assertEquals(400, resp.getStatusCodeValue());
    }
}
