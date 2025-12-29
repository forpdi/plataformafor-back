package org.forrisco.core.export;

import java.util.List;

import org.forpdi.core.common.AbstractController;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("api/forrisco/export")
public class ExportController extends AbstractController {

	@Autowired
	private ExportBS exportBS;

	@PostMapping("/unit")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN_OR_SYSTEM_ADMIN)
	public ResponseEntity<?> exportUnits(@RequestBody(required = false) List<Long> idsList, HttpServletResponse response, UserSession userSession) {
		try {
			exportBS.exportUnitsCSV(idsList, response, userSession);
			return this.nothing();
		} catch (IllegalArgumentException e) {
			return this.fail(e.getMessage());
		} catch (Exception e) {
			return this.fail("Erro inesperado ao exportar unidades.");
		}
	}

	@PostMapping("/process")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN_OR_SYSTEM_ADMIN)
	public ResponseEntity<?> exportProcesses(@RequestBody(required = false) List<Long> idsList, HttpServletResponse response, UserSession userSession) {
		try {
			exportBS.exportProcessesCSV(idsList, response, userSession);
			return this.nothing();
		} catch (IllegalArgumentException e) {
			return this.fail(e.getMessage());
		} catch (Exception e) {
			return this.fail("Erro inesperado ao exportar processos.");
		}
	}

	@PostMapping("/risk")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN_OR_SYSTEM_ADMIN)
	public ResponseEntity<?> exportRisks(@RequestBody(required = false) List<Long> idsList, HttpServletResponse response, UserSession userSession) {
		try {
			exportBS.exportRisksCSV(idsList, response, userSession);
			return this.nothing();
		} catch (IllegalArgumentException e) {
			return this.fail(e.getMessage());
		} catch (Exception e) {
			return this.fail("Erro inesperado ao exportar riscos.");
		}
	}

	@PostMapping("/prevenctiveAction")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN_OR_SYSTEM_ADMIN)
	public ResponseEntity<?> exportPrevenctiveAction(@RequestBody(required = false) List<Long> idsList, HttpServletResponse response, UserSession userSession) {
		try {
			exportBS.exportPrevenctiveActionsCSV(idsList, response, userSession);
			return this.nothing();
		} catch (IllegalArgumentException e) {
			return this.fail(e.getMessage());
		} catch (Exception e) {
			return this.fail("Erro inesperado ao exportar ações de prevenção.");
		}
	}
}
