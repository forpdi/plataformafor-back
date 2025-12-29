package org.forrisco.core.process;

import jakarta.persistence.EntityManager;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.ListResponse;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.notification.NotificationBS;
import org.forpdi.core.storage.file.ArchiveBS;
import org.forpdi.core.user.User;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.policy.Policy;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.RiskBS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.forpdi.core.common.Response;
import org.forpdi.security.auth.AuthData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes para ProcessController")
class ProcessControllerTest {

    @Spy
    @InjectMocks
    private ProcessController controller;

    @Mock
    private CompanyDomainContext domain;
    @Mock
    private ProcessBS processBS;
    @Mock
    private RiskBS riskBS;
    @Mock
    private NotificationBS notificationBS;
    @Mock
    private ArchiveBS archiveBS;
    @Mock
    private CompanyBS companyBS;
    @Mock
    private ProcessHistoryBS processHistoryBS;

    private UserSession mockUserSession;
    private Company mockCompany;
    private Unit mockUnit;
    private Process baseProcess;
    private User mockUser;
    private PlanRisk mockPlanRisk;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Usuário Teste");

        mockUserSession = mock(UserSession.class);

        AuthData mockAuthData = mock(AuthData.class);
        when(mockAuthData.getId()).thenReturn(mockUser.getId());
        when(mockAuthData.getName()).thenReturn(mockUser.getName());

        when(mockUserSession.getAuthData()).thenReturn(mockAuthData);

        when(mockUserSession.getAccessLevel()).thenReturn(AccessLevels.MANAGER.getLevel());

        mockCompany = new Company();
        mockCompany.setId(10L);

        Policy mockPolicy = mock(Policy.class);
        when(mockPolicy.getCompany()).thenReturn(mockCompany);
        mockPlanRisk = mock(PlanRisk.class);
        when(mockPlanRisk.getId()).thenReturn(20L);
        when(mockPlanRisk.getPolicy()).thenReturn(mockPolicy);
        mockUnit = mock(Unit.class);
        when(mockUnit.getId()).thenReturn(30L);
        when(mockUnit.getPlanRisk()).thenReturn(mockPlanRisk);

        baseProcess = new Process();
        baseProcess.setId(100L);
        baseProcess.setName("Processo Base");
        baseProcess.setCompany(mockCompany);
        baseProcess.setUnitCreator(mockUnit);
        baseProcess.setAllObjectives(new ArrayList<>());
        baseProcess.setRelatedUnits(new ArrayList<>());

        CompanyDomain mockCompanyDomain = mock(CompanyDomain.class);
        when(domain.get()).thenReturn(mockCompanyDomain);
        when(mockCompanyDomain.getCompany()).thenReturn(mockCompany);

        when(companyBS.findCompanyByUserId(mockUser.getId())).thenReturn(mockCompany);
    }

    @Nested
    @DisplayName("Endpoint: saveProcess (POST /new)")
    class SaveProcessTests {
        @Test
        @DisplayName("Deve salvar um processo com sucesso")
        void saveProcess_success() throws Exception {
            Process processoParaSalvar = new Process();
            processoParaSalvar.setUnit(mockUnit);
            processoParaSalvar.setAllObjectives(new ArrayList<>());

            ProcessObjective objective = new ProcessObjective();
            objective.setDescription("Test Objective");
            processoParaSalvar.getAllObjectives().add(objective);

            when(processBS.exists(any(), eq(Unit.class))).thenReturn(mockUnit);
            when(companyBS.findCompanyByUserId(anyLong())).thenReturn(mockCompany);

            ResponseEntity<?> response = controller.saveProcess(processoParaSalvar, mockUserSession);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(processBS).save(any(Process.class));
            verify(processBS).saveObjective(any(ProcessObjective.class));
        }

        @Test
        @DisplayName("Deve falhar se a unidade não for encontrada")
        void saveProcess_whenUnitNotFound_shouldFail() {
            when(processBS.exists(any(), eq(Unit.class))).thenReturn(null);
            Process process = new Process();
            process.setUnit(new Unit());
            ResponseEntity<?> response = controller.saveProcess(process, mockUserSession);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            Response body = (Response) response.getBody();
            assertNotNull(body, "O corpo da resposta não deveria ser nulo");
            assertTrue(body.getMessage().contains("Unidade não encontrada"));
        }

        @Test
        @DisplayName("Deve falhar por segurança se o usuário pertence a outra empresa")
        void saveProcess_whenUserInDifferentCompany_shouldFail() {
            Company otherCompany = new Company();
            otherCompany.setId(99L);
            when(companyBS.findCompanyByUserId(anyLong())).thenReturn(otherCompany);
            when(processBS.exists(any(), eq(Unit.class))).thenReturn(mockUnit);

            Process process = new Process();
            process.setUnit(mockUnit);
            ResponseEntity<?> response = controller.saveProcess(process, mockUserSession);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            Response body = (Response) response.getBody();
            assertNotNull(body, "O corpo da resposta não deveria ser nulo");
            assertTrue(body.getMessage().contains("Acesso negado"));
        }
    }

    @Nested
    @DisplayName("Endpoint: deleteProcesses (DELETE /{id})")
    class DeleteProcessTests {
        @Test
        @DisplayName("Deve deletar um processo com sucesso")
        void deleteProcesses_success() {
            when(processBS.exists(100L, Process.class)).thenReturn(baseProcess);
            when(companyBS.findCompanyByUserId(anyLong())).thenReturn(mockCompany);
            when(riskBS.hasLinkedRiskActivity(baseProcess)).thenReturn(false);
            when(riskBS.hasLinkedRiskObjectiveProcess(baseProcess)).thenReturn(false);

            ResponseEntity<?> response = controller.deleteProcesses(100L, mockUserSession);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(processBS).deleteProcess(baseProcess);
        }

        @Test
        @DisplayName("Deve falhar se o processo estiver vinculado a um risco")
        void deleteProcesses_whenLinkedToRisk_shouldFail() {
            when(processBS.exists(100L, Process.class)).thenReturn(baseProcess);
            when(companyBS.findCompanyByUserId(anyLong())).thenReturn(mockCompany);
            when(riskBS.hasLinkedRiskActivity(baseProcess)).thenReturn(true);

            ResponseEntity<?> response = controller.deleteProcesses(100L, mockUserSession);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            Response body = (Response) response.getBody();
            assertNotNull(body, "O corpo da resposta não deveria ser nulo");
            assertTrue(body.getMessage().contains("Processo vinculado a um Risco"));
        }
    }

    @Nested
    @DisplayName("Endpoint: updateProcess (PUT /)")
    class UpdateProcessTests {
        @Test
        @DisplayName("Deve atualizar um processo com sucesso")
        void updateProcess_success() {
            Process incomingProcess = mock(Process.class);

            when(incomingProcess.getId()).thenReturn(100L);
            when(incomingProcess.getName()).thenReturn("Nome Atualizado");
            when(incomingProcess.getAllObjectives()).thenReturn(Collections.emptyList());
            when(incomingProcess.getRelatedUnits()).thenReturn(Collections.emptyList());
            when(processBS.exists(100L, Process.class)).thenReturn(baseProcess);
            when(companyBS.findCompanyByUserId(anyLong())).thenReturn(mockCompany);
            when(processBS.exists(mockUser.getId(), User.class)).thenReturn(mockUser);
            when(processBS.listRelatedUnits(any(), any())).thenReturn(Collections.emptyList());
            when(processBS.listProcessObjectives(any())).thenReturn(Collections.emptyList());
            when(processBS.getProcessUnitsByProcess(any())).thenReturn(Collections.emptyList());

            ResponseEntity<?> response = controller.updateProcess(incomingProcess, mockUserSession);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(processHistoryBS).saveProcessChanges(eq(baseProcess), eq(incomingProcess), any(), any(), any(),
                    any(), any());
            verify(processBS, atLeastOnce()).persist(any());
        }
    }

    @Nested
    @DisplayName("Endpoint: getProcessHistory (GET /{id}/history)")
    class GetProcessHistoryTests {
       @Test
        @DisplayName("Deve retornar o histórico de um processo com sucesso")
        void getProcessHistory_success() {
            when(processBS.exists(100L, Process.class)).thenReturn(baseProcess);
            when(companyBS.findCompanyByUserId(anyLong())).thenReturn(mockCompany);

            PaginatedList<ProcessHistoryBS.ProcessHistoryDTO> mockHistoryList = new PaginatedList<>();
            List<ProcessHistoryBS.ProcessHistoryDTO> dtoList = new ArrayList<>();
            dtoList.add(mock(ProcessHistoryBS.ProcessHistoryDTO.class));
            mockHistoryList.setList(dtoList);
            mockHistoryList.setTotal(1L);

            when(processHistoryBS.listHistoryByProcess(eq(100L), any(DefaultParams.class))).thenReturn(mockHistoryList);

            ResponseEntity<?> response = controller.getProcessHistory(100L, new DefaultParams(), mockUserSession);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody(), "O corpo da resposta não deveria ser nulo");
            assertTrue(response.getBody() instanceof ListResponse, "O corpo deveria ser um ListResponse");

            ListResponse<?> listResponse = (ListResponse<?>) response.getBody();

            assertSame(dtoList, listResponse.getData(), "A lista dentro da resposta deve ser a mesma");
            assertEquals(1L, listResponse.getTotal(), "O total de itens deve ser 1");
        }


        @Test
        @DisplayName("Deve retornar Not Found se o processo não existir")
        void getProcessHistory_whenProcessNotFound_shouldReturnNotFound() {
            when(processBS.exists(999L, Process.class)).thenReturn(null);

            ResponseEntity<?> response = controller.getProcessHistory(999L, new DefaultParams(), mockUserSession);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }
}