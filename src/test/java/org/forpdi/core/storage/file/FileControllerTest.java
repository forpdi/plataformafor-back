package org.forpdi.core.storage.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.storage.StorageService;
import org.forpdi.core.storage.dto.DownloadFileRequestDto;
import org.forpdi.security.auth.AuthData;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileControllerTest {

    @InjectMocks
    private FileController controller;

    @Mock private CompanyDomainContext domain;
    @Mock private ArchiveBS archiveBS;
    @Mock private StorageService storageService;
    @Mock private CompanyBS companyBS;

    private static Company company(long id, String name) {
        Company c = new Company();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private static UserSession userSession(long userId, boolean logged, int accessLevel) {
        UserSession us = mock(UserSession.class, Mockito.RETURNS_DEEP_STUBS);
        when(us.isLogged()).thenReturn(logged);
        when(us.getAccessLevel()).thenReturn(accessLevel);
        AuthData ad = mock(AuthData.class);
        when(ad.getId()).thenReturn(userId);
        when(ad.getName()).thenReturn("tester");
        when(us.getAuthData()).thenReturn(ad);
        return us;
    }

    private static void setIdViaReflection(Object entity, long id) {
        try {
            Field f = entity.getClass().getSuperclass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Não foi possível setar o id via reflexão", e);
        }
    }

    @BeforeEach
    void setup() {
        CompanyDomain mockCompanyDomain = mock(CompanyDomain.class);
        when(domain.get()).thenReturn(mockCompanyDomain);
        when(mockCompanyDomain.getCompany()).thenReturn(company(10L, "Comp-10"));
    }

    @Test
    @DisplayName("uploadFile: sucesso (admin) PNG com assinatura válida")
    void upload_success_admin_png() throws Exception {
        UserSession admin = userSession(1L, true, AccessLevels.SYSTEM_ADMIN.getLevel());
        byte[] pngHeader = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x11, 0x22, 0x33, 0x44};
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", pngHeader);

        Archive saved = new Archive();
        setIdViaReflection(saved, 123L);
        saved.setCompany(company(10L, "Comp-10"));
        saved.setName("logo.png");
        when(archiveBS.save(any(Archive.class))).thenReturn(saved);

        doNothing().when(storageService).uploadFile(any(InputStream.class), anyString(), anyString(), anyLong());

        ResponseEntity<?> resp = controller.uploadFile(file, admin);
        assertEquals(200, resp.getStatusCodeValue());

        verify(storageService).uploadFile(any(InputStream.class), eq("image/png"), eq("123.png"), anyLong());
    }

    @Test
    @DisplayName("uploadFile: rejeita arquivo vazio")
    void upload_reject_empty() {
        UserSession user = userSession(2L, true, AccessLevels.MANAGER.getLevel());
        MockMultipartFile file = new MockMultipartFile("file", "vazio.txt", "text/plain", new byte[0]);
        when(companyBS.findCompanyByUserId(2L)).thenReturn(company(10L, "Comp-10"));

        ResponseEntity<?> resp = controller.uploadFile(file, user);
        assertEquals(400, resp.getStatusCodeValue());
        verify(storageService, never()).uploadFile(any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("uploadFile: rejeita tamanho maior que 2MB")
    void upload_reject_tooLarge() {
        UserSession user = userSession(3L, true, AccessLevels.MANAGER.getLevel());
        byte[] big = new byte[2 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf", big);
        when(companyBS.findCompanyByUserId(3L)).thenReturn(company(10L, "Comp-10"));

        ResponseEntity<?> resp = controller.uploadFile(file, user);
        assertEquals(400, resp.getStatusCodeValue());
        verify(storageService, never()).uploadFile(any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("uploadFile: rejeita extensão não permitida")
    void upload_reject_disallowedExtension() {
        UserSession user = userSession(3L, true, AccessLevels.MANAGER.getLevel());
        MockMultipartFile file = new MockMultipartFile("file", "script.sh", "text/x-sh", "#!/bin/sh".getBytes(StandardCharsets.UTF_8));
        when(companyBS.findCompanyByUserId(3L)).thenReturn(company(10L, "Comp-10"));

        ResponseEntity<?> resp = controller.uploadFile(file, user);
        assertEquals(400, resp.getStatusCodeValue());
        verify(storageService, never()).uploadFile(any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("uploadFile: rejeita imagem com conteúdo inválido (MIME detectado ≠ extensão)")
    void upload_reject_image_badMime() {
        UserSession user = userSession(4L, true, AccessLevels.MANAGER.getLevel());
        MockMultipartFile file = new MockMultipartFile("file", "fake.png", "image/png", "not a png".getBytes(StandardCharsets.UTF_8));
        when(companyBS.findCompanyByUserId(4L)).thenReturn(company(10L, "Comp-10"));

        ResponseEntity<?> resp = controller.uploadFile(file, user);
        assertEquals(400, resp.getStatusCodeValue());
        verify(storageService, never()).uploadFile(any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("uploadFile: rejeita sessão inválida/deslogada")
    void upload_reject_notLogged() {
        UserSession anon = userSession(0L, false, 0);
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "x".getBytes());
        ResponseEntity<?> resp = controller.uploadFile(file, anon);
        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("uploadFile: rejeita quando empresa do usuário não é encontrada (não-admin)")
    void upload_reject_noCompanyForUser() {
        UserSession user = userSession(5L, true, AccessLevels.MANAGER.getLevel());
        when(companyBS.findCompanyByUserId(5L)).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "x".getBytes());
        ResponseEntity<?> resp = controller.uploadFile(file, user);
        assertEquals(400, resp.getStatusCodeValue());
        verify(storageService, never()).uploadFile(any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("downloadFile: sucesso (usuário admin) — inline + content-type coerente")
    void download_success_admin() throws Exception {
        Archive arc = new Archive();
        setIdViaReflection(arc, 321L);
        arc.setName("doc.pdf");
        arc.setCompany(company(10L, "Comp-10"));
        when(archiveBS.exists(321L, Archive.class)).thenReturn(arc);

        when(storageService.retrieveFile("321.pdf"))
                .thenReturn(new ByteArrayInputStream("PDF".getBytes(StandardCharsets.UTF_8)));

        DownloadFileRequestDto req = new DownloadFileRequestDto("321.pdf");

        UserSession admin = userSession(1L, true, AccessLevels.SYSTEM_ADMIN.getLevel());
        ResponseEntity<?> resp = controller.downloadFile(req, admin);

        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getHeaders().getFirst("Content-Disposition").toLowerCase().contains("inline"));
        assertEquals("application/octet-stream", resp.getHeaders().getContentType().toString());
        assertTrue(resp.getBody() instanceof InputStreamResource);
    }

    @Test
    @DisplayName("downloadFile: sucesso (usuário não-admin da mesma empresa)")
    void download_success_nonAdmin_sameCompany() throws Exception {
        Archive arc = new Archive();
        setIdViaReflection(arc, 1000L);
        arc.setName("img.png");
        arc.setCompany(company(77L, "Comp-77"));
        when(archiveBS.exists(1000L, Archive.class)).thenReturn(arc);

        when(companyBS.findCompanyByUserId(55L)).thenReturn(company(77L, "Comp-77"));
        when(storageService.retrieveFile("1000.png")).thenReturn(new ByteArrayInputStream(new byte[]{1,2,3}));

        DownloadFileRequestDto req = new DownloadFileRequestDto("1000.png");

        UserSession user = userSession(55L, true, AccessLevels.MANAGER.getLevel());
        ResponseEntity<?> resp = controller.downloadFile(req, user);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("image/png", resp.getHeaders().getContentType().toString());
    }

    @Test
    @DisplayName("downloadFile: 403 quando usuário não-admin de empresa diferente")
    void download_forbidden_nonAdmin_otherCompany() {
        Archive arc = new Archive();
        setIdViaReflection(arc, 99L);
        arc.setName("a.jpg");
        arc.setCompany(company(7L, "Comp-7"));
        when(archiveBS.exists(99L, Archive.class)).thenReturn(arc);

        when(companyBS.findCompanyByUserId(5L)).thenReturn(company(8L, "Comp-8"));

        DownloadFileRequestDto req = new DownloadFileRequestDto("99.jpg");

        UserSession user = userSession(5L, true, AccessLevels.MANAGER.getLevel());
        ResponseEntity<?> resp = controller.downloadFile(req, user);

        assertEquals(403, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("downloadFile: 403 anônimo quando empresa do arquivo ≠ empresa atual do tenant")
    void download_forbidden_anonymous_otherTenant() {
        Archive arc = new Archive();
        setIdViaReflection(arc, 77L);
        arc.setName("a.jpg");
        arc.setCompany(company(123L, "Outra-Comp"));
        when(archiveBS.exists(77L, Archive.class)).thenReturn(arc);

        DownloadFileRequestDto req = new DownloadFileRequestDto("77.jpg");

        UserSession anon = userSession(0L, false, 0);
        ResponseEntity<?> resp = controller.downloadFile(req, anon);

        assertEquals(403, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("downloadFile: 404 quando arquivo não encontrado")
    void download_notFound() {
        when(archiveBS.exists(555L, Archive.class)).thenReturn(null);

        DownloadFileRequestDto req = new DownloadFileRequestDto("555.pdf");

        UserSession admin = userSession(1L, true, AccessLevels.SYSTEM_ADMIN.getLevel());
        ResponseEntity<?> resp = controller.downloadFile(req, admin);

        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("downloadFile: 400 quando parâmetro file não é numérico")
    void download_badRequest_invalidId() {
        DownloadFileRequestDto req = new DownloadFileRequestDto("notanumber.png");

        UserSession admin = userSession(1L, true, AccessLevels.SYSTEM_ADMIN.getLevel());
        ResponseEntity<?> resp = controller.downloadFile(req, admin);

        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("downloadFile: 500 quando storageService lança IOException")
    void download_internalError_onStorage() throws Exception {
        Archive arc = new Archive();
        setIdViaReflection(arc, 42L);
        arc.setName("b.svg");
        arc.setCompany(company(10L, "Comp-10"));
        when(archiveBS.exists(42L, Archive.class)).thenReturn(arc);

        when(storageService.retrieveFile("42.svg")).thenThrow(new java.io.IOException("boom"));

        DownloadFileRequestDto req = new DownloadFileRequestDto("42.svg");

        UserSession admin = userSession(1L, true, AccessLevels.SYSTEM_ADMIN.getLevel());
        ResponseEntity<?> resp = controller.downloadFile(req, admin);

        assertEquals(500, resp.getStatusCodeValue());
    }
}