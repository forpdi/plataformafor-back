package org.forrisco.core.imports;

import org.forpdi.core.common.AbstractController;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImportController extends AbstractController {

    public static final String PATH = BASEPATH + "/forrisco/import";

    @Autowired
    private ImportBS importBS;

    @PostMapping(PATH + "/unit")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN_OR_SYSTEM_ADMIN)
    public ResponseEntity<?> importUnits(
            @RequestParam("planRiskId") Long planRiskId,
            @org.springframework.web.bind.annotation.RequestPart("file") MultipartFile file,
            UserSession userSession) {
        try {
            if (file == null || file.isEmpty()) {
                return this.fail("Arquivo CSV é obrigatório.");
            }
            if (planRiskId == null) {
                return this.fail("planRiskId é obrigatório.");
            }
            ImportResult result = importBS.importUnitsCsv(file, planRiskId, userSession);
            return this.success(result);
        } catch (IllegalArgumentException ex) {
            return this.fail(ex.getMessage());
        } catch (Throwable ex) {
            LOGGER.error("Erro inesperado ao importar unidades", ex);
            return this.fail("Erro inesperado ao importar unidades: " + ex.getMessage());
        }
    }

    @PostMapping(PATH + "/subunit")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN_OR_SYSTEM_ADMIN)
    public ResponseEntity<?> importSubunits(
            @RequestParam("planRiskId") Long planRiskId,
            @RequestParam("parentId") Long parentId,
            @org.springframework.web.bind.annotation.RequestPart("file") MultipartFile file,
            UserSession userSession) {
        try {
            if (file == null || file.isEmpty())
                return this.fail("Arquivo CSV é obrigatório.");
            if (planRiskId == null)
                return this.fail("planRiskId é obrigatório.");
            if (parentId == null)
                return this.fail("parentId é obrigatório.");

            ImportResult result = importBS.importSubunitsCsv(file, planRiskId, parentId, userSession);
            return this.success(result);
        } catch (IllegalArgumentException ex) {
            return this.fail(ex.getMessage());
        } catch (Throwable ex) {
            LOGGER.error("Erro inesperado ao importar subunidades", ex);
            return this.fail("Erro inesperado ao importar subunidades: " + ex.getMessage());
        }
    }

    @PostMapping(PATH + "/process")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN_OR_SYSTEM_ADMIN)
    public ResponseEntity<?> importProcesses(
            @RequestParam("planRiskId") Long planRiskId,
            @RequestParam("unitId") Long unitId,
            @org.springframework.web.bind.annotation.RequestPart("file") MultipartFile file,
            UserSession userSession) {
        try {
            if (file == null || file.isEmpty())
                return this.fail("Arquivo CSV é obrigatório.");
            if (planRiskId == null)
                return this.fail("planRiskId é obrigatório.");
            if (unitId == null)
                return this.fail("unitId é obrigatório.");

            ImportResult result = importBS.importProcessesCsv(file, planRiskId, unitId, userSession);
            return this.success(result);
        } catch (IllegalArgumentException ex) {
            return this.fail(ex.getMessage());
        } catch (Throwable ex) {
            LOGGER.error("Erro inesperado ao importar processos", ex);
            return this.fail("Erro inesperado ao importar processos: " + ex.getMessage());
        }
    }

    @PostMapping(PATH + "/risk")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN_OR_SYSTEM_ADMIN)
    public ResponseEntity<?> importRisks(
            @RequestParam("planRiskId") Long planRiskId,
            @RequestParam("unitId") Long unitId,
            @org.springframework.web.bind.annotation.RequestPart("file") MultipartFile file,
            UserSession userSession) {
        try {
            if (file == null || file.isEmpty())
                return this.fail("Arquivo CSV é obrigatório.");
            if (planRiskId == null)
                return this.fail("planRiskId é obrigatório.");
            if (unitId == null)
                return this.fail("unitId é obrigatório.");

            ImportResult result = importBS.importRisksCsv(file, planRiskId, unitId, userSession);
            return this.success(result);
        } catch (IllegalArgumentException ex) {
            return this.fail(ex.getMessage());
        } catch (Throwable ex) {
            LOGGER.error("Erro inesperado ao importar riscos", ex);
            return this.fail("Erro inesperado ao importar riscos: " + ex.getMessage());
        }
    }

    @PostMapping(PATH + "/preventive-action")
    @PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN_OR_SYSTEM_ADMIN)
    public ResponseEntity<?> importPreventiveActions(
            @RequestParam("planRiskId") Long planRiskId,
            @RequestParam("riskId") Long riskId,
            @RequestPart("file") MultipartFile file,
            UserSession userSession) {
        try {
            ImportResult result = importBS.importPreventiveActionsCsv(file, planRiskId, riskId, userSession);
            return this.success(result);
        } catch (Throwable ex) {
            LOGGER.error("Erro ao importar ações preventivas", ex);
            return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
        }
    }
}
