package org.forpdi.core.communication;

import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.communication.dto.CommunicationDto;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunicationControllerTest {

    @Mock
    private CommunicationBS bs;
    @Mock
    private CompanyDomainContext companyDomainContext;
    @Mock
    private CompanyBS companyBS;
    @Mock
    private UserSession userSession;

    @InjectMocks
    private CommunicationController controller;

    private Company company;
    private Communication communication;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1L);

        communication = new Communication();
        communication.setId(1L);
        communication.setCompany(company);
    }

    @Test
    void newCommunication_success() {
        CommunicationDto dto = mock(CommunicationDto.class);
        when(dto.title()).thenReturn("Titulo");
        when(dto.message()).thenReturn("Mensagem");
        when(dto.validityBegin()).thenReturn(new Date());
        when(dto.validityEnd()).thenReturn(new Date());
        when(companyDomainContext.getCompany()).thenReturn(company);
        when(userSession.getUser()).thenReturn(null);

        ResponseEntity<?> resp = controller.newCommunication(dto);
        assertEquals(200, resp.getStatusCodeValue());
        verify(bs).saveNewCommunication(any(Communication.class));
    }

    @Test
    void retrieveCommunication_systemAdmin_success() {
        when(bs.retrieveCommunicationById(1L)).thenReturn(communication);
        when(userSession.isLogged()).thenReturn(true);
        when(userSession.getAccessLevel()).thenReturn(AccessLevels.SYSTEM_ADMIN.getLevel());

        ResponseEntity<?> resp = controller.retrieveCommunication(1L, userSession);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void showActiveCommunication_success() {
        when(userSession.getAccessLevel()).thenReturn(AccessLevels.SYSTEM_ADMIN.getLevel());
        when(companyDomainContext.getCompany()).thenReturn(company);
        when(bs.retrieveCommunicationByValidity(company)).thenReturn(communication);

        ResponseEntity<?> resp = controller.showActiveCommunication(userSession);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void retrieveActiveCommunication_success() {
        when(userSession.getAccessLevel()).thenReturn(AccessLevels.SYSTEM_ADMIN.getLevel());
        when(companyDomainContext.getCompany()).thenReturn(company);
        when(bs.retrieveCommunicationByValidity(eq(company), any(Date.class))).thenReturn(communication);

        ResponseEntity<?> resp = controller.retrieveActiveCommunication(userSession, new Date());
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void updateCommunication_notFound() {
        CommunicationDto dto = mock(CommunicationDto.class);
        when(dto.id()).thenReturn(1L);
        when(bs.exists(1L, Communication.class)).thenReturn(null);

        ResponseEntity<?> resp = controller.updateCommunication(dto);
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void endCommunication_success() {
        when(bs.exists(1L, Communication.class)).thenReturn(communication);

        ResponseEntity<?> resp = controller.endCommunication(1L);
        assertEquals(200, resp.getStatusCodeValue());
    }
}
