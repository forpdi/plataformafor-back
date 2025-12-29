package org.forpdi.planning.structure.goals.history;

import java.util.ArrayList;
import java.util.List;

import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.Response;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.auth.AuthData;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GoalJustificationHistoryControllerTest {

    @Mock private GoalJustificationHistoryBS bs;

    @Mock private StructureBS structureBS;

    @Mock private CompanyDomainContext domain;

    @Mock private CompanyBS companyBS;
    
    @InjectMocks private GoalJustificationHistoryController controller;

    private UserSession userSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.userSession = mock(UserSession.class);
        AuthData mockAuthData = mock(AuthData.class);
        when(this.userSession.getAuthData()).thenReturn(mockAuthData);
        when(mockAuthData.getId()).thenReturn(1L);
    }
    
    @Test
    @DisplayName("Deve retornar histórico com sucesso para um usuário válido")
    void test_list_by_level_instance_returns_history() {
        long levelInstanceId = 1L;
        Long userId = 1L;

        Company company = new Company();
        company.setId(10L);

        PlanMacro fakePlanMacro = new PlanMacro();
        fakePlanMacro.setCompany(company);
        Plan fakePlan = new Plan();
        fakePlan.setParent(fakePlanMacro);
        StructureLevelInstance fakeLevelInstance = new StructureLevelInstance();
        fakeLevelInstance.setPlan(fakePlan);
        
        when(this.userSession.getAuthData().getId()).thenReturn(userId);
        when(this.userSession.getAccessLevel()).thenReturn(AccessLevels.COLABORATOR.getLevel());
        
        when(this.structureBS.retrieveLevelInstance(levelInstanceId)).thenReturn(fakeLevelInstance);
        when(this.companyBS.findCompanyByUserId(userId)).thenReturn(company);

        List<GoalJustificationHistory> historyList = new ArrayList<>();
        historyList.add(new GoalJustificationHistory());
        when(this.bs.listByLevelInstance(levelInstanceId)).thenReturn(historyList);
        
        ResponseEntity<?> response = controller.listByLevelInstance(levelInstanceId, this.userSession);
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Response<?> body = (Response<?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        ListWrapper<?> wrapper = (ListWrapper<?>) body.getData();
        assertNotNull(wrapper);
        assertEquals(1, wrapper.getList().size());
    }

    @Test
    @DisplayName("Deve falhar quando a instância de nível não for encontrada")
    void test_list_by_level_instance_fails_when_not_found() {
        long nonExistentId = 404L;
        
        when(this.structureBS.retrieveLevelInstance(nonExistentId)).thenReturn(null);
        
        ResponseEntity<?> response = controller.listByLevelInstance(nonExistentId, this.userSession);
        
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Response<?> body = (Response<?>) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("O nível solicitado não foi encontrado.", body.getMessage());
}
}