package org.forpdi.core.company;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.company.dto.CompanyDomainDto;
import org.forpdi.core.company.dto.CompanyDto;
import org.forpdi.core.company.dto.UpdateLogoDto;
import org.forpdi.core.storage.StorageService;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.storage.file.ArchiveBS;
import org.forpdi.core.user.UserBS;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    @Mock
    private CompanyBS bs;
    @Mock
    private UserBS userBS;
    @Mock
    private UserSession userSession;
    @Mock
    private CompanyDomainContext companyDomain;
    @Mock
    private ArchiveBS archiveBS;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private CompanyController controller;

    private Company company;
    private CompanyDomain domain;

    @BeforeEach
    void setup() {
        company = new Company();
        company.setId(1L);
        company.setName("Test Company");

        domain = new CompanyDomain();
        domain.setId(1L);
        domain.setCompany(company);
    }

    @Test
    void saveCompany_success() {
        CompanyDto dto = mock(CompanyDto.class);
        when(dto.company()).thenReturn(company);

        ResponseEntity<?> response = controller.saveCompany(dto);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void updateCompany_notFound() {
        CompanyDto dto = mock(CompanyDto.class);
        when(dto.company()).thenReturn(company);
        when(bs.exists(anyLong(), eq(Company.class))).thenReturn(null);

        ResponseEntity<?> resp = controller.updateCompany(dto);
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void retrieveCompany_systemAdmin_success() {
        when(bs.retrieveCompanyById(1L)).thenReturn(company);
        when(userSession.getAccessLevel()).thenReturn(AccessLevels.SYSTEM_ADMIN.getLevel());

        ResponseEntity<?> resp = controller.retrieveCompany(1L, userSession);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void updateLogo_success() {
        Archive archive = new Archive();
        archive.setCompany(company);
        archive.setId(1L);

        UpdateLogoDto dto = mock(UpdateLogoDto.class);
        when(dto.companyId()).thenReturn(1L);
        when(dto.fileLink()).thenReturn("file123");

        when(bs.retrieveCompanyById(1L)).thenReturn(company);
        when(userSession.getAccessLevel()).thenReturn(AccessLevels.SYSTEM_ADMIN.getLevel());
        when(archiveBS.getByFileLink("file123")).thenReturn(archive);

        ResponseEntity<?> resp = controller.updateLogo(dto, userSession);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void listCompanies_success() {
        when(bs.search(any(DefaultParams.class))).thenReturn(null);
        ResponseEntity<?> resp = controller.listCompanies(new DefaultParams());
        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    void deleteCompany_success() {
        when(bs.exists(1L, Company.class)).thenReturn(company);
        when(bs.retrieveCompanyByDomain(company)).thenReturn(Collections.emptyList());
        ResponseEntity<?> resp = controller.delete(1L);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void saveDomain_success() {
        CompanyDomainDto dto = mock(CompanyDomainDto.class);
        when(dto.domain()).thenReturn(domain);
        when(bs.retrieveCompanyById(1L)).thenReturn(company);
        when(bs.alreadyExistsURL(domain)).thenReturn(false);
        when(bs.retrieveByHost(anyString())).thenReturn(null);

        ResponseEntity<?> resp = controller.saveDomain(dto);
        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    void updateDomain_success() {
        CompanyDomainDto dto = mock(CompanyDomainDto.class);
        when(dto.domain()).thenReturn(domain);
        when(bs.exists(anyLong(), eq(CompanyDomain.class))).thenReturn(domain);
        when(bs.alreadyExistsURL(domain)).thenReturn(false);

        ResponseEntity<?> resp = controller.updateDomain(dto);
        assertEquals(200, resp.getStatusCodeValue());
    }
}
