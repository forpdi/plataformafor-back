package org.forpdi.core.company;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import org.forpdi.core.common.AbstractController;
import org.forpdi.core.utils.Util;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.MultipartConfig;

/**
 * Controller único para Export/Import com processamento assíncrono do restore.
 */
@RestController
// Define limites de upload sem precisar mexer no application.yml (opcional)
@MultipartConfig(
    location = "/tmp",              // ajuste se preferir outro diretório
    fileSizeThreshold = 2 * 1024 * 1024, // 2MB
    maxFileSize = -1L,              // sem limite (ou defina ex.: 5L * 1024 * 1024 * 1024)
    maxRequestSize = -1L
)
public class BackupAndRestoreController extends AbstractController {

    @Autowired private BackupAndRestoreHelper dbbackup;
    @Autowired private CompanyDomainContext domain;
    @Autowired private PlanBS planBS;
    @Autowired private CompanyBS companyBS;

    // ==============================
    // Infra "local" de jobs (em memória)
    // ==============================

    private static final Map<UUID, ImportState> STATES = new ConcurrentHashMap<>();
    private static final Set<Long> RUNNING_BY_COMPANY = ConcurrentHashMap.newKeySet();
    private static ExecutorService IMPORT_EXEC;

    @PostConstruct
    void initExec() {
        if (IMPORT_EXEC == null) {
            IMPORT_EXEC = new ThreadPoolExecutor(
                2, 4, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(16),
                r -> {
                    Thread t = new Thread(r, "import-worker-" + UUID.randomUUID());
                    t.setDaemon(true);
                    return t;
                }
            );
        }
    }

    private static ImportState getState(UUID id) { return STATES.get(id); }
    private static void update(UUID id, Consumer<ImportState> mut) { var st = STATES.get(id); if (st != null) mut.accept(st); }
    private static boolean isRunningForCompany(Long companyId) { return RUNNING_BY_COMPANY.contains(companyId); }
    private static UUID createJob(Long companyId, Path storedPath, String originalName) {
        UUID id = UUID.randomUUID();
        STATES.put(id, new ImportState(id, companyId, storedPath.toString(), originalName));
        RUNNING_BY_COMPANY.add(companyId);
        return id;
    }
    private static void finish(UUID id) { var st = STATES.get(id); if (st != null) RUNNING_BY_COMPANY.remove(st.companyId); }

    // POJO simples de estado
    private static final class ImportState {
        final UUID jobId;
        final Long companyId;
        final String storedPath;
        final String originalName;
        volatile String stage = "ENFILEIRADO";
        volatile int progress = 0;
        volatile boolean success = false;
        volatile String message = null;
        final LocalDateTime createdAt = LocalDateTime.now();

        ImportState(UUID jobId, Long companyId, String storedPath, String originalName) {
            this.jobId = jobId; this.companyId = companyId; this.storedPath = storedPath; this.originalName = originalName;
        }
        Map<String,Object> view() {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("jobId", jobId.toString());
            m.put("companyId", companyId);
            m.put("file", originalName);
            m.put("stage", stage);
            m.put("progress", progress);
            m.put("success", success);
            m.put("message", message);
            m.put("createdAt", createdAt.toString());
            return m;
        }
    }

    // MultipartFile baseado em arquivo do disco (compatível com dbbackup.restore(MultipartFile))
    private static final class FileSystemMultipartFile implements MultipartFile {
        private final Path path;
        private final String originalName;
        private final String contentType;

        FileSystemMultipartFile(Path path, String originalName, String contentType) {
            this.path = path; this.originalName = originalName; this.contentType = contentType;
        }
        @Override public String getName() { return originalName != null ? originalName : path.getFileName().toString(); }
        @Override public String getOriginalFilename() { return originalName; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { try { return Files.size(path) == 0; } catch (IOException e) { return true; } }
        @Override public long getSize() { try { return Files.size(path); } catch (IOException e) { return -1; } }
        @Override public byte[] getBytes() throws IOException { return Files.readAllBytes(path); } // evite usar para arquivos gigantes
        @Override public InputStream getInputStream() throws IOException { return Files.newInputStream(path, StandardOpenOption.READ); }
        @Override public void transferTo(java.io.File dest) throws IOException, IllegalStateException { Files.copy(path, dest.toPath(), StandardCopyOption.REPLACE_EXISTING); }
    }

    // ==============================
    // EXPORT (seu código original)
    // ==============================

    /**
     * Valida export (sem baixar)
     */
    @GetMapping("api/company/export")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
    public ResponseEntity<?> validexport(UserSession userSession) {
        try {
            boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

            if (isSystemAdmin) {
                if (this.domain == null || this.domain.get().getCompany() == null) {
                    return this.fail("Não há instituição cadastrada para exportar");
                }
            } else {
                Long loggedInUserId = userSession.getAuthData().getId();
                Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
                if (userCompany == null) {
                    return this.fail("Não há instituição cadastrada para exportar");
                }
            }
            return this.success();

        } catch (Throwable ex) {
            LOGGER.error("Unexpected runtime error", ex);
            this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return this.nothing();
        }
    }

    @GetMapping("company/export")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
    public ResponseEntity<?> export(@RequestParam(required = false) String ids, UserSession userSession) {
        try {
            Company targetCompany;
            boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

            if (isSystemAdmin) {
                targetCompany = this.domain.get().getCompany();
            } else {
                Long loggedInUserId = userSession.getAuthData().getId();
                targetCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
            }

            if (targetCompany == null) {
                return this.fail("Acesso negado. Empresa não identificada.");
            }

            List<Long> planMacroIds = Util.stringListToLongList(ids);
            if (planMacroIds.isEmpty()) {
                return this.fail("Não há planos selecionados");
            }

            for (Long planMacroId : planMacroIds) {
                PlanMacro planMacro = this.planBS.retrievePlanMacroById(planMacroId);
                if (planMacro == null) {
                    return this.fail("Plano macro com ID " + planMacroId + " não encontrado.");
                }
                if (!planMacro.getCompany().getId().equals(targetCompany.getId())) {
                    LOGGER.warn("Tentativa de EXPORTAÇÃO indevida do Plano Macro ID {} pelo usuário ID {}.", planMacroId, userSession.getAuthData().getId());
                    return this.fail("Acesso negado.");
                }
            }

            LOGGER.info("Starting export company '{}'...", targetCompany.getName());
            this.response.setHeader("Content-Disposition", String.format("attachment; filename=plans-%d-%s.fbk",
                    targetCompany.getId(), LocalDateTime.now().toString()));
            this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

            this.dbbackup.export(targetCompany.getId(), this.response.getOutputStream(), planMacroIds);

        } catch (Throwable ex) {
            LOGGER.error("Unexpected runtime error", ex);
            this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return this.fail("Unexpected runtime error" + ex.getMessage());
        }
        return this.nothing();
    }

    // ==============================
    // IMPORT (assíncrono para evitar 504)
    // ==============================

    /**
     * Inicia upload + agenda restore (ASSÍNCRONO). Responde 202 rapidamente.
     */
    @PostMapping(value = "api/company/fbkupload", consumes = MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(value = AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
    public ResponseEntity<?> fbkupload(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return this.fail("upload falhou");
            }
            if (this.domain == null || this.domain.get().getCompany() == null) {
                return this.fail("Instituição não definida");
            }
            Long companyId = this.domain.get().getCompany().getId();

            if (isRunningForCompany(companyId)) {
                return this.fail("processo de importação já em andamento");
            }

            Path tmpDir = Paths.get(Optional.ofNullable(System.getProperty("java.io.tmpdir")).orElse("/tmp"));
            String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.fbk");
            String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path stored = tmpDir.resolve(UUID.randomUUID() + "-" + safeName);
            Files.copy(file.getInputStream(), stored, StandardCopyOption.REPLACE_EXISTING);

            UUID jobId = createJob(companyId, stored, originalName);
            submitRestoreJob(jobId, stored, originalName);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("jobId", jobId.toString());
            body.put("statusUrl", "/forpdi/api/company/fbkupload/status/" + jobId);
            return ResponseEntity.accepted().body(body);

        } catch (Throwable ex) {
            LOGGER.error("Erro no upload/agenda de importação", ex);
            return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
        }
    }

    /**
     * Status do job de importação (para polling no front)
     */
    @GetMapping("api/company/fbkupload/status/{jobId}")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
    public ResponseEntity<?> status(@PathVariable String jobId) {
        ImportState st;
        try {
            st = getState(UUID.fromString(jobId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
        if (st == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(st.view());
    }

    // ==============================
    // Worker interno (mesmo arquivo)
    // ==============================

    private void submitRestoreJob(UUID jobId, Path stored, String originalName) {
        IMPORT_EXEC.submit(() -> {
            update(jobId, s -> { s.stage = "GERANDO"; s.progress = 10; });

            try {
                // cria um MultipartFile baseado no arquivo do disco (mantém compatível com seu helper)
                MultipartFile diskFile = new FileSystemMultipartFile(stored, originalName, "application/octet-stream");

                // Chama o helper original
                dbbackup.restore(diskFile);

                update(jobId, s -> { s.stage = "PRONTO"; s.progress = 100; s.success = true; s.message = "Importação concluída."; });
            } catch (IllegalArgumentException e) {
                update(jobId, s -> { s.stage = "ERRO"; s.message = e.getMessage(); s.success = false; });
            } catch (Throwable t) {
                update(jobId, s -> { s.stage = "ERRO"; s.message = "Falha inesperada: " + t.getMessage(); s.success = false; });
            } finally {
                // limpeza do arquivo em disco
                try { Files.deleteIfExists(stored); } catch (Exception ignore) {}
                finish(jobId);
            }
        });
    }

    // ==============================
    // (Opcional) Manter o endpoint antigo /restore para compatibilidade
    // ==============================

    /**
     * Mantido apenas para compatibilidade: agora não faz nada
     * pois o restore roda em background quando o upload é aceito.
     */
    @PostMapping("api/company/restore")
    @PreAuthorize(value=AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
    public ResponseEntity<?> restore() {
        return this.fail("Fluxo alterado: use /api/company/fbkupload (202) e acompanhe por /api/company/fbkupload/status/{jobId}.");
    }
}
