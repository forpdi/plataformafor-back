package org.forpdi.planning.structure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.planning.plan.PDIFilterParams;
import org.forpdi.planning.structure.xml.StructureBeans;
import org.forpdi.planning.structure.xml.StructureImporter;
import org.forpdi.security.auth.UserSession;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

// Estes testes focam no comportamento do controller, sem contexto completo do Spring.
// Evitamos Security/DB/I.O. reais usando mocks e o MockMvc "standalone".
class StructureControllerTest {

    // ===== util de injeção por reflexão =====
    private static void inject(Object target, String field, Object value) throws Exception {
        var f = StructureController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ---------------------------------------------------------------------------------------
    // 2) POST /api/structure/import
    // ---------------------------------------------------------------------------------------
    @Test
    @DisplayName("importStructure: XML válido → importa, persiste e vincula à empresa do tenant")
    void importStructure_success() throws Exception {
        StructureController controller = new StructureController();

        StructureBS bs = mock(StructureBS.class);
        DuplicatedStructureCheckHelper dupHelper = mock(DuplicatedStructureCheckHelper.class);
        CompanyDomainContext domain = mock(CompanyDomainContext.class, RETURNS_DEEP_STUBS);

        inject(controller, "bs", bs);
        inject(controller, "duplicatedStructureCheckHelper", dupHelper);
        inject(controller, "domain", domain);

        Company company = new Company();
        company.setId(1L);
        when(domain.get().getCompany()).thenReturn(company);

        StructureImporter importer = mock(StructureImporter.class);
        StructureBeans.Structure readed = new StructureBeans.Structure();
        readed.name = "Estrutura XYZ";
        when(importer.getReadedStructure()).thenReturn(readed);

        Structure structure = new Structure();
        when(bs.importStructure(importer)).thenReturn(structure);

        when(bs.getStructureImporter(any(File.class))).thenReturn(importer);

        MockMultipartFile file = new MockMultipartFile(
            "file", "estrutura.xml", "application/xml",
            "<estrutura/>".getBytes(StandardCharsets.UTF_8)
        );

        File tmpBase = java.nio.file.Files.createTempDirectory("fpdi-tmp-").toFile();

        try (MockedStatic<TempFilesManager> tm = mockStatic(TempFilesManager.class);
             MockedStatic<Files> nioFiles = mockStatic(Files.class)) {
            tm.when(TempFilesManager::getTempDir).thenReturn(tmpBase);
            nioFiles.when(() -> Files.probeContentType(any(Path.class))).thenReturn("application/xml");

            ResponseEntity<?> resp = controller.importStructure(file);

            assertEquals(200, resp.getStatusCodeValue());
        } finally {
            tmpBase.delete();
        }
    }

    @Test
    @DisplayName("importStructure: estrutura duplicada (mesmo nome) → 400 com mensagem específica")
    void importStructure_duplicateSameName() throws Exception {
        StructureController controller = new StructureController();

        StructureBS bs = mock(StructureBS.class);
        DuplicatedStructureCheckHelper dupHelper = mock(DuplicatedStructureCheckHelper.class);
        CompanyDomainContext domain = mock(CompanyDomainContext.class, RETURNS_DEEP_STUBS);
        inject(controller, "bs", bs);
        inject(controller, "duplicatedStructureCheckHelper", dupHelper);
        inject(controller, "domain", domain);

        Company company = new Company(); company.setId(1L);
        when(domain.get().getCompany()).thenReturn(company);

        StructureImporter importer = mock(StructureImporter.class);
        StructureBeans.Structure readed = new StructureBeans.Structure();
        readed.name = "Estrutura XYZ";
        when(importer.getReadedStructure()).thenReturn(readed);
        when(bs.getStructureImporter(any(File.class))).thenReturn(importer);

        Structure duplicated = new Structure();
        duplicated.setName("Estrutura XYZ");
        when(dupHelper.retrieveDuplicatedStructure(readed)).thenReturn(duplicated);

        MockMultipartFile file = new MockMultipartFile("file","estrutura.xml","application/xml","<e/>".getBytes());

        File tmpBase = java.nio.file.Files.createTempDirectory("fpdi-tmp-").toFile();
        try (MockedStatic<TempFilesManager> tm = mockStatic(TempFilesManager.class);
             MockedStatic<Files> nioFiles = mockStatic(Files.class)) {
            tm.when(TempFilesManager::getTempDir).thenReturn(tmpBase);
            nioFiles.when(() -> Files.probeContentType(any(Path.class))).thenReturn("application/xml");

            ResponseEntity<?> resp = controller.importStructure(file);
            assertEquals(400, resp.getStatusCodeValue());
        } finally {
            tmpBase.delete();
        }
    }

    @Test
    @DisplayName("importStructure: MIME inválido → 400 (bloqueio de upload)")
    void importStructure_invalidMime() throws Exception {
        StructureController controller = new StructureController();

        StructureBS bs = mock(StructureBS.class);
        DuplicatedStructureCheckHelper dupHelper = mock(DuplicatedStructureCheckHelper.class);
        inject(controller, "bs", bs);
        inject(controller, "duplicatedStructureCheckHelper", dupHelper);
        inject(controller, "domain", mock(CompanyDomainContext.class, RETURNS_DEEP_STUBS));

        when(bs.getStructureImporter(any(File.class))).thenReturn(mock(StructureImporter.class));

        MockMultipartFile file = new MockMultipartFile("file","estrutura.xml","application/xml","<e/>".getBytes());

        File tmpBase = java.nio.file.Files.createTempDirectory("fpdi-tmp-").toFile();
        try (MockedStatic<TempFilesManager> tm = mockStatic(TempFilesManager.class);
             MockedStatic<Files> nioFiles = mockStatic(Files.class)) {
            tm.when(TempFilesManager::getTempDir).thenReturn(tmpBase);
            // MIME errado
            nioFiles.when(() -> Files.probeContentType(any(Path.class))).thenReturn("application/pdf");

            ResponseEntity<?> resp = controller.importStructure(file);
            assertEquals(400, resp.getStatusCodeValue());
        } finally {
            tmpBase.delete();
        }
    }

    // ---------------------------------------------------------------------------------------
    // 3) POST /api/structure/exportPDF (async)
    // ---------------------------------------------------------------------------------------
    @Test
    @DisplayName("exportPDF: async=false → 400 Bad Request")
    void exportPDF_syncNotAllowed() {
        StructureController controller = new StructureController();
        ResponseEntity<Object> resp = controller.exportPDF(new PDIFilterParams(), false, null, new MockHttpServletRequest());
        assertEquals(400, resp.getStatusCodeValue());
        assertTrue(String.valueOf(resp.getBody()).contains("Modo síncrono não suportado"));
    }

    @Test
    @DisplayName("reportStatus: job não encontrado → 404")
    void reportStatus_notFound() {
        StructureController controller = new StructureController();
        StructureBS structureBS = mock(StructureBS.class);
        injectNoThrow(controller, "structureBS", structureBS);

        when(structureBS.getJobStatusEntity(any(UUID.class))).thenReturn(null);

        ResponseEntity<Object> resp = controller.reportStatus(UUID.randomUUID().toString(), mock(UserSession.class));
        assertEquals(404, resp.getStatusCodeValue());
    }

    // ---------------------------------------------------------------------------------------
    // 6) parseGoalStatusCompat (privado, via reflexão)
    // ---------------------------------------------------------------------------------------
    @Test
    @DisplayName("parseGoalStatusCompat: mapeia null/\"-1\"/vazio para null; 1/true→true; 0/false→false; outros→null")
    void parseGoalStatusCompat_cases() throws Exception {
        Method m = StructureController.class.getDeclaredMethod("parseGoalStatusCompat", String.class);
        m.setAccessible(true);

        assertNull(m.invoke(null, new Object[]{ null }));
        assertNull(m.invoke(null, ""));
        assertNull(m.invoke(null, "-1"));
        assertEquals(Boolean.TRUE,  m.invoke(null, "true"));
        assertEquals(Boolean.TRUE,  m.invoke(null, "1"));
        assertEquals(Boolean.FALSE, m.invoke(null, "false"));
        assertEquals(Boolean.FALSE, m.invoke(null, "0"));
        assertNull(m.invoke(null, "qualquer"));
    }

    private static void injectNoThrow(Object target, String field, Object value) {
        try { inject(target, field, value); } catch (Exception ignored) {}
    }
}
