package org.forpdi.core.communication;

import java.util.Date;

import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.communication.dto.CommunicationDto;
import org.forpdi.core.communication.dto.ListComminicationRequestDto;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.utils.Consts;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Gabriel Oliveira
 */
@RestController
public class CommunicationController extends AbstractController {

	@Autowired
	private CommunicationBS bs;

	@Autowired
	private CompanyDomainContext companyDomainContext;

	@Autowired
	private CompanyBS companyBS;


	/**
	 * Salvar novo comunicado
	 * 
	 * @param title
	 *                      titulo do comunicado.
	 * @param message
	 *                      mensagem do comunicado.
	 * @param validityBegin
	 *                      Data de inicio do comunicado.
	 * @param validityEnd
	 *                      Data de término do comunicado.
	 * 
	 */
	@PostMapping("/api/communication/new")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> newCommunication(@RequestBody CommunicationDto dto) {
		try {
			Communication communicationHistory = new Communication();
			communicationHistory.setTitle(dto.title());
			communicationHistory.setMessage(dto.message());
			communicationHistory.setValidityBegin(dto.validityBegin());
			communicationHistory.setValidityEnd(dto.validityEnd());
			communicationHistory.setResponsible(this.userSession.getUser());
			communicationHistory.setCompany(this.companyDomainContext.getCompany());
			this.bs.saveNewCommunication(communicationHistory);
			return this.success(communicationHistory);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}

	/**
	 * Retorna um comunicado de acordo com seu id.
	 * 
	 * @param id do comunicado desejado.
	 * @return communicationHistory, versão desejada que possui o dado id.
	 */
	@GetMapping("/api/communication/{id}")
	public ResponseEntity<?> retrieveCommunication(@PathVariable Long id, UserSession userSession) {
		try {
			Communication communicationHistory = this.bs.retrieveCommunicationById(id);
			if (communicationHistory == null) {
				return this.fail("O comunicado solicitado não foi encontrado.");
			}

			if (userSession.isLogged()) {
				boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

				if (!isSystemAdmin) {
					Long loggedInUserId = userSession.getAuthData().getId();
					Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

					if (userCompany == null || !communicationHistory.getCompany().getId().equals(userCompany.getId())) {
						throw new AccessDeniedException("Acesso negado");
					}
				}
			} else {
				this.companyDomainContext.validateTenant(communicationHistory.getCompany());
			}

			return this.success(communicationHistory);

		} catch (AccessDeniedException | IllegalStateException ex) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("Você não tem permissão para acessar este comunicado.");
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna comunicados salvos.
	 * 
	 * 
	 * @return <PaginatedList> Communication
	 */
	@PostMapping("/api/communication/list")
	public ResponseEntity<?> listCommunications(
		UserSession userSession,
		@RequestBody ListComminicationRequestDto requestDto) {
		try {
			Integer page = requestDto.page();
			Integer pageSize = requestDto.pageSize();
			Company targetCompany;
			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (isSystemAdmin) {
				targetCompany = this.companyDomainContext.getCompany();
			} else {
				Long loggedInUserId = userSession.getAuthData().getId();
				targetCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
			}

			if (targetCompany == null) {
				LOGGER.error("Falha de segurança: Usuário com ID {} tentando listar comunicados sem empresa associada.", userSession.getAuthData().getId());
				return this.fail("Acesso negado.");
			}
			
			if (page == null || page < 1) {
				page = 1;
			}
			if (pageSize == null) {
				pageSize = Consts.MED_PAGE_SIZE;
			}

			PaginatedList<Communication> communicationHistory = this.bs.listByCompany(targetCompany, page, pageSize);
			return this.success(communicationHistory.getList(), communicationHistory.getTotal());

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna comunicados salvos.
	 * 
	 * 
	 * @return Communication
	 */
	@GetMapping("/api/communication/showActiveCommunication")
	public ResponseEntity<?> showActiveCommunication(UserSession userSession) {
		try {
			Company targetCompany;
			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (isSystemAdmin) {
				targetCompany = this.companyDomainContext.getCompany();
			} else {
				Long loggedInUserId = userSession.getAuthData().getId();
				targetCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
			}

			if (targetCompany == null) {
				return this.success((Communication) null);
			}

			Communication communication = this.bs.retrieveCommunicationByValidity(targetCompany);
			return this.success(communication);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna comunicados salvos de uma validade especifica.
	 * 
	 * 
	 * @return Communication
	 */
	@GetMapping("/api/communication/retrieveActiveCommunication")
	public ResponseEntity<?> retrieveActiveCommunication(
			UserSession userSession,
			@RequestParam(required = false) Date validityBegin) {
		try {
			Company targetCompany;
			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (isSystemAdmin) {
				targetCompany = this.companyDomainContext.getCompany();
			} else {
				Long loggedInUserId = userSession.getAuthData().getId();
				targetCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
			}

			if (targetCompany == null) {
				return this.success((Communication) null);
			}

			Communication communication = this.bs.retrieveCommunicationByValidity(targetCompany, validityBegin);
			return this.success(communication);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Atualiza um comunicado.
	 * 
	 * @param comunicado, comunicado a ser atualizado.
	 * @return existent, comunicado atualizado.
	 */
	@PutMapping("/api/communication/update")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> updateCommunication(@RequestBody CommunicationDto dto) {
		try {
			Communication existent = this.bs.exists(dto.id(), Communication.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}

			this.companyDomainContext.validateTenant(existent.getCompany());

			existent.setTitle(dto.title());
			existent.setMessage(dto.message());
			existent.setValidityBegin(dto.validityBegin());
			existent.setValidityEnd(dto.validityEnd());
			existent.setResponsible(dto.responsible());
			existent.setShowPopup(dto.showPopup());
			existent.setLastModification(new Date());
			this.bs.persist(existent);
			return this.success(existent);
		} catch (AccessDeniedException | IllegalStateException e) {
			LOGGER.warn("BOLA detectada: usuário {} tentou atualizar o comunicado {}",
					this.userSession.getUser().getId(), dto.id());
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("Você não tem permissão para alterar este comunicado.");
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Desativa comunicado.
	 * 
	 * @param id
	 *           Id do comunicado a ser desativado.
	 */
	@PutMapping("/api/communication/{id}")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> endCommunication(@PathVariable Long id) {
		try {
			Communication existent = this.bs.exists(id, Communication.class);
			if (existent == null) {
				return this.fail("O comunicado solicitado não foi encontrado.");
			}

			this.companyDomainContext.validateTenant(existent.getCompany());

			bs.endCommunication(existent);
			return this.success(id);

		} catch (AccessDeniedException | IllegalStateException ex) {
			LOGGER.warn("BOLA detectada: usuário {} tentou desativar o comunicado {}",
					this.userSession.getUser().getId(), id);
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("Você não tem permissão para desativar este comunicado.");
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

}
