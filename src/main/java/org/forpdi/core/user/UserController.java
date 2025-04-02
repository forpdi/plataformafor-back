package org.forpdi.core.user;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.IdDto;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.bean.UserToSelect;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.company.CompanyUser;
import org.forpdi.core.notification.NotificationBS;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.user.authz.UserPermission;
import org.forpdi.core.user.dto.EditAccessLevelDto;
import org.forpdi.core.user.dto.ImportUsersDto;
import org.forpdi.core.user.dto.InviteUserDto;
import org.forpdi.core.user.dto.RecoverPasswordDto;
import org.forpdi.core.user.dto.RegisterInvitedUserDto;
import org.forpdi.core.user.dto.RegisterUserDto;
import org.forpdi.core.user.dto.ResetPasswordDto;
import org.forpdi.core.user.dto.UpdateNotificationSettingsDto;
import org.forpdi.core.user.dto.UpdateUserDto;
import org.forpdi.core.user.dto.UserDto;
import org.forpdi.core.utils.Util;
import org.forpdi.planning.permissions.PermissionDTO;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Renato R. R. de Oliveira
 */
@RestController
public class UserController extends AbstractController {

	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private UserBS bs;
	@Autowired
	private StructureBS sbs;
	@Autowired
	private NotificationBS notificationBS;
	@Autowired
	private UserPasswordUsedBS userPasswordUsedBS;
	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Salvar um usuário enviando um convite
	 * 
	 * @param name        Nome do usuário.
	 * @param email       Email do usuário.
	 * @param accessLevel Nível de acesso do usuário.
	 * @return User Usuário salvo
	 */
	@PostMapping("/api/user/invite")
	@PreAuthorize(AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> invite(@RequestBody InviteUserDto dto) {
		try {
			if (dto.accessLevel() > this.userSession.getAccessLevel()) {
				return this.forbidden();
			}

			User user = this.bs.inviteUser(dto.name(), dto.email(), dto.accessLevel());

			return this.success(user);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail(e.getMessage());
		}
	}

	/**
	 * Cadastra um usuário sem enviar convite
	 * 
	 * @param name        Nome do usuário.
	 * @param email       Email do usuário.
	 * @param password    Senha do usuário.
	 * @param accessLevel Nível de acesso do usuário.
	 * @return User Usuário cadastrado
	 */
	@PostMapping("/api/user/register")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> register(@RequestBody RegisterUserDto dto) {
		try {
			User user = this.bs.existsByEmail(dto.email());
			if (user != null && !user.isDeleted()) {
				return this.fail("E-mail do usuário " + dto.email() + " já foi cadastrado!");
			} else {
				user = new User();
			}
			user.setName(dto.name());
			user.setEmail(dto.email());
			user.setPassword(passwordEncoder.encode(dto.password()));
			user.setDeleted(false);
			user.setActive(true);
			user.setAccessLevel(dto.accessLevel());
			this.bs.persist(user);

			CompanyUser companyUser = new CompanyUser();
			companyUser.setCompany(this.domain.get().getCompany());
			companyUser.setUser(user);
			CompanyUser existent = this.bs.exists(companyUser, CompanyUser.class);
			if (existent == null) {
				companyUser.setAccessLevel(dto.accessLevel());
				this.bs.persist(companyUser);
			}

			return this.success(user);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail(e.getMessage());
		}
	}

	/**
	 * Atualizar o termo de privacidade do usuário.
	 *
	 * @param user Id do usuário que se deseja atualizar o campo.
	 *
	 * @return ResponseEntity<?>
	 */
	@PostMapping("/api/user/update/terms")
	public ResponseEntity<?> updateTermField(@RequestBody IdDto dto) {
		try {

			User user = this.bs.exists(dto.id(), User.class);
			if (user == null) {
				return this.notFound();
			} else if (!this.userSession.getUser().getId().equals(user.getId())) {
				return this.fail("Termo de privacidade deve ser aceito por quem está logado");
			} else if (user.getTermsAcceptance() != null) {
				return this.fail("Termo de privacidade já aceito");
			}

			user.setTermsAcceptance(new Date());
			this.bs.persist(user);
			return this.success();

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}


	/**
	 * Reenviar convite para usuário acessar o sistema.
	 * 
	 * @param id Id do usuário.
	 * @return Mensagem de feedback.
	 */
	@PostMapping("/api/user/{id}/reinvite")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> resendInvitation(@PathVariable Long id) {
		try {
			User user = this.bs.exists(id, User.class);
			if (user == null) {
				return this.notFound();
			}
			this.bs.sendInvitationEmail(user);
			return this.success("Convite reenviado com sucesso.");
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("E-mail do usuário já foi cadastrado!");
		}
	}

	/**
	 * Atualiza os campos do perfil do usuário.
	 * 
	 * @param user            Usuário que terá seus campos atualizados.
	 * @param currentPassword Password atual do usuário.
	 * @param newPassword     Novo passdoword do usuário.
	 * @return User Usuário com todos os campos do perfil atualizado.
	 */
	@PostMapping("/api/user/updateUserProfile")
	public ResponseEntity<?> updateProfile(@RequestBody UpdateUserDto dto) {
		try {
			User user = dto.user();
			if (!this.userSession.getUser().getId().equals(user.getId())) {
				return this.forbidden();
			}
			
			if (dto.currentPassword() != null) {
				User existent = this.bs.exists(user.getId(), User.class);
				boolean currentPasswordIsValid = passwordEncoder.matches(dto.currentPassword(), existent.getPassword());
				if (!currentPasswordIsValid) {
					throw new IllegalArgumentException("Senha atual inválida!");
				}
			}

			return this.success(this.bs.updateUser(user, dto.newPassword()));
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail(e.getMessage());
		}
	}

	/**
	 * Atualiza os campos de um usuário
	 * 
	 * @param user        Usuário que terá seus campos atualizados.
	 * @param passwordNew Novo password do usuário.
	 * @return User Usuário com todos os campos atualizados.
	 */
	@PostMapping("/api/user/updateUser")
	@PreAuthorize(AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> editUser(@RequestBody UpdateUserDto dto) {
		try {
			return this.success(this.bs.updateUser(dto.user(), dto.newPassword()));
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail(e.getMessage());
		}

		
	}

	/**
	 * Usuário do tipo administrador realiza update no campo de accessLevel
	 * 
	 * @param userId      Id do usuário que terá seus campo atualizado.
	 * @param accessLevel novo nível que será atribuído ao usuário.
	 */
	@PostMapping("/api/user/updateAccessLevel")
	@PreAuthorize(AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> editUserAccessLevel(@RequestBody EditAccessLevelDto dto) {
		try {
			User existent = this.bs.existsByUser(dto.userId());
			if (existent.getAccessLevel() > this.userSession.getAccessLevel()) {
				return this.forbidden();
			}
			
			CompanyUser companyUser = this.bs.retrieveCompanyUser(existent, this.domain.get().getCompany());

			existent.setAccessLevel(dto.accessLevel());

			if (companyUser != null) {
				companyUser.setAccessLevel(dto.accessLevel());
			}

			this.bs.notifyOnAccessLevelChange(companyUser, dto.userId(), existent);

			this.bs.persist(existent);
			this.bs.persist(companyUser);

			return this.success(existent);
		} catch (Throwable e) {
			LOGGER.error("Erro no login.", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}

	}

	/**
	 * Recuperar password do usuário.
	 * 
	 * @param email E-mail do usuário para recuperar a senha.
	 * @throws EmailException
	 * @throws UnknownHostException
	 */
	@PostMapping("/api/user/recover")
	public ResponseEntity<?> requestRecover(@RequestBody RecoverPasswordDto dto) throws EmailException, UnknownHostException {
		try {

			User user = this.bs.existsByEmail(dto.email());

			if (this.domain == null || this.domain.get().getCompany() == null) {
				return this.fail("Não foi possível recuperar a senha. Entre em contato com o administrador do sistema.");
			}

			if (user != null) {
				boolean userDeleted = this.bs.userIsDeleted(dto.email());
				CompanyUser companyUser = this.bs.retrieveCompanyUser(user, this.domain.get().getCompany());

				boolean userCanRecover = !userDeleted && companyUser != null && !companyUser.isBlocked()
						&& companyUser.getUser().isActive();
				
				if (userCanRecover) {
					UserRecoverRequest req = this.bs.requestRecover(user);

					String url = request.getRequestURL().toString().replaceAll("forpdi/api/user/recover",
							"#/reset-password/") + req.getToken();

					this.notificationBS.sendNotificationEmail(NotificationType.RECOVER_PASSWORD, url, "", user, null);
				}
			}
			return this.success("Você receberá uma mensagem caso o e-mail informado seja válido");
		} catch (Throwable ex) {
			LOGGER.error("Unexpected error occurred.", ex);
			return this.fail("Unexpected error occurred: " + ex.getMessage());
		}
	}

	/**
	 * Alterar password.
	 * 
	 * @param password Password novo.
	 * @param token    Token de identificação do usuário para trocar o password.
	 */
	@PostMapping("/api/user/reset/{token}")
	public ResponseEntity<?> resetUserPassword(@PathVariable String token, @RequestBody ResetPasswordDto dto) {
		try {
			if (GeneralUtils.isEmpty(dto.password())) {
				return this.fail("A senha não pode ser vazia.");
			} else {
				this.bs.resetPassword(dto.password(), token);
				return this.success();
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected error occurred.", ex);
			return this.fail(ex.getMessage());
		}
	}

	/**
	 * Registrar usuário.
	 * 
	 * @param user      Usuário que será registrado.
	 * @param birthdate Data de nascimento do usuário.
	 * @param token     Token de identificação do usuário.
	 */
	@PostMapping("/api/user/register/{token}")
	public ResponseEntity<?> registerUser(@PathVariable String token, @RequestBody RegisterInvitedUserDto dto) {
		try {
			User user = dto.user();
			User existent = this.bs.existsByInviteToken(token);
			if (GeneralUtils.isInvalid(existent)) {
				return this.fail("Token de registro inválida.");
			} else if (dto.termsAccepted() == null || !dto.termsAccepted()) {
				return this.fail("Você deve aceitar os termos de privacidade.");
			} else {
				User userRetrieveCpf = null;
				User userRetrieveCellphone = null;

				userRetrieveCpf = bs.existsByCpf(user.getCpf());
				userRetrieveCellphone = bs.existsByCellphone(user.getCellphone());

				boolean exists = false;
				String msgError = "";

				if (userRetrieveCpf != null) {
					exists = true;
					msgError = "CPF ";
				}
				if (userRetrieveCellphone != null) {
					exists = true;
					msgError += "CELULAR";
				}

				if (!exists) {
					this.bs.validatePassword(user.getPassword(), null);

					existent.setName(user.getName());
					existent.setCpf(user.getCpf());
					existent.setCellphone(user.getCellphone());
					existent.setPhone(user.getPhone());
					existent.setDepartment(user.getDepartment());
					if (dto.birthdate() != null) {
						existent.setBirthdate(GeneralUtils.parseDate(dto.birthdate()));
					}
					existent.setPassword(passwordEncoder.encode(user.getPassword()));
					existent.setActive(true);
					existent.setInviteToken(null);
					existent.setTermsAcceptance(new Date());

					this.userPasswordUsedBS.persistUsedPassword(user.getPassword(), existent);
					this.bs.persist(existent);
//						CompanyUser companyUser = this.bs.retrieveCompanyUser(existent, this.domain.get().getCompany());
					this.notificationBS.sendNotificationEmail(NotificationType.WELCOME, "", "", existent, null);
					return this.success(existent);
				} else {
					return this.fail(msgError);
				}
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected error occurred.", ex);
			return this.fail(ex.getMessage());
		}
	}

	/**
	 * Verificar validade de token de acesso do usuário.
	 * 
	 * @param token Token para validação.
	 * @return Mensagem feedback do usuário.
	 */
	@GetMapping("/api/user/register/{token}")
	public ResponseEntity<?> canRegister(@PathVariable String token) {
		try {
			User user = this.bs.existsByInviteToken(token);
			if (user == null) {
				return this.fail("Token de registro inválida.");
			} else {
				return this.success();
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected error occurred.", ex);
			return this.fail(ex.getMessage());
		}
	}

	/**
	 * Requisição para recuperar usuário.
	 * 
	 * @param token Token de identificação do usuário a ser recuperado.
	 * @return UserRecoverRequest Requisição realizada.
	 * 
	 */
	@GetMapping("/api/user/reset/{token}")
	public ResponseEntity<?> canReset(@PathVariable String token) {
		try {
			UserRecoverRequest req = this.bs.retrieveRecoverRequest(token);
			if (req == null) {
				return this.fail("Token de recuperação inválida ou expirada.");
			} else {
				return this.success();
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected error occurred.", ex);
			return this.fail(ex.getMessage());
		}
	}

	/**
	 * Buscar usuário da instituição do domínio acessado.
	 * 
	 * @param id Id do usuário.
	 * @return User usuário da instituição.
	 */
	@GetMapping("/api/user/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> retrieveUsers(@PathVariable Long id) {
		try {
			User user = this.bs.exists(id, User.class);
			if (user == null)
				return this.notFound();
			else {
				CompanyUser companyUser = this.bs.retrieveCompanyUser(user, this.domain.get().getCompany());
				if (companyUser == null) {
					return this.fail(
							"Este usuário não está cadastrado nesse instituição. Convide-o para poder ver suas informações.");
				}
				user.setAccessLevel(companyUser.getAccessLevel());
				user.setCpf(Util.censorCPF(user.getCpf()));
				return this.success(user);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Buscar uma página de usuários da instituição atual
	 * 
	 * @param page Número da página que se deseja recuperar os usuários.
	 * @param pageSize Tamanho da página a ser recuperada
	 * @return PaginatedList Lista dos usuários.
	 */
	@GetMapping("/api/user")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> listUsers(@ModelAttribute DefaultParams params) {
		try {
			PaginatedList<UserDto> users = this.bs.listFromCurrentCompany(params);
			return this.success(users.getList(), users.getTotal());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna uma lista resumida dos usuarios filtrados para seleção.
	 * 
	 * @param minAccessLevel
	 * @param maxAccessLevel
	 */
	@GetMapping("/api/user/toSelect")
	public ResponseEntity<?> listToSelect(@RequestParam(required = false) Integer minAccessLevel,
			@RequestParam(required = false) Integer maxAccessLevel) {

		try {
			List<UserToSelect> users = this.bs.listUsersToSelect(this.domain.get().getCompany(), minAccessLevel, maxAccessLevel);
			ListWrapper<UserToSelect> wrapper = new ListWrapper<>(users);
			return this.success(wrapper);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Buscar uma página com todos os usuários de todas as instituições
	 * 
	 * @param page Número da página que se deseja recuperar os usuários.
	 * @param term Termo de pesquisa dos usuários
	 * @return PaginatedList Lista dos usuários.
	 */
	@GetMapping("/api/user/listSystemUsers")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> listAllUsers(@ModelAttribute DefaultParams params) {
		try {
			PaginatedList<UserDto> users = this.bs.listSystemUsers(params);
			return this.success(users.getList(), users.getTotal());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Deletar usuário.
	 * 
	 * @param id Id do usuário que será deletado.
	 * @return boolean true - usuário deletado com sucesso. false - usuário não
	 *         deletado.
	 */
	@DeleteMapping("/api/user/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> removeUser(@PathVariable Long id) {
		try {
			User user = this.bs.exists(id, User.class);

			if (user == null) {
				return this.notFound();
			}
			if (user.getAccessLevel() > this.userSession.getAccessLevel()) {
				return this.forbidden();
			}

			
			if (!sbs.isUserResponsibleForSomeLevel(id, this.domain.get().getCompany())) {
				CompanyUser companyUser = this.bs.retrieveCompanyUser(user, this.domain.get().getCompany());
				if (userSession.getAccessLevel() < user.getAccessLevel()
						|| (userSession.getAccessLevel() == user.getAccessLevel() && user.isActive())) {
					return this.fail("Não é possível excluir um usuário com nível de acesso igual ou superior.");
				}
				User deletedUser = null;
				if (companyUser != null) {
					deletedUser = this.bs.deleteCompanyUser(companyUser);
				}
				return this.success(deletedUser);
			} else {
				return this.fail("Impossível excluir esse usuário pois ele é responsável por algum nível do plano de metas.");
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Bloquear usuário.
	 * 
	 * @param id Id do usuário que será bloquado.
	 * @return CompanyUser Instituição que o usuário está bloquado.
	 */
	@PostMapping("/api/user/{id}/block")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> blockUser(@PathVariable Long id) {
		if (id == null) {
			return this.notFound();
		}
		if (this.domain == null) {
			return this.fail("Você só pode bloquear usuários estando em um domínio de uma instituição.");
		}
		try {
			User user = this.bs.exists(id, User.class);
			if (user == null) {
				return notFound();
			}
			if (user.getAccessLevel() > this.userSession.getAccessLevel()) {
				return this.forbidden();
			}

			CompanyUser companyUser = this.bs.retrieveCompanyUser(user, this.domain.get().getCompany());
			if (companyUser != null) {
				companyUser.setBlocked(true);
				this.bs.persist(companyUser);
			}
			return this.success(companyUser);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Desbloquear usuário.
	 * 
	 * @param id Id do usuário que será desbloqueado.
	 * @return companyUser Instituição que o usuário está desbloqueado.
	 */
	@PostMapping("/api/user/{id}/unblock")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> unblockUser(@PathVariable Long id) {
		if (id == null) {
			return notFound();
		}
		if (this.domain == null) {
			return this.fail("Você só pode desbloquear usuários estando em um domínio de uma instituição.");
		}
		try {
			User user = this.bs.exists(id, User.class);
			if (user == null) {
				return notFound();
			}
			if (user.getAccessLevel() > this.userSession.getAccessLevel()) {
				return this.forbidden();
			}

			CompanyUser companyUser = this.bs.retrieveCompanyUser(user, this.domain.get().getCompany());
			if (companyUser != null) {
				companyUser.setBlocked(false);
				this.bs.persist(companyUser);
			}
			return this.success(companyUser);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Buscar todos os campos do usuário.
	 * 
	 * @param id Id do usuário para buscar todos os seus campos.
	 * @return User Usuário com todos os seus campos.
	 */
	@GetMapping("/api/user/profileUser")
	public ResponseEntity<?> retrieveUserProfile() {
		try {
			User user = this.bs.exists(this.userSession.getUser().getId(), User.class);
			if (user == null) {
				return this.fail("A empresa solicitada não foi encontrada.");
			} else {
				user.setCpf(Util.censorCPF(user.getCpf()));
				UserDto dto;
				if (user.getAccessLevel() != AccessLevels.SYSTEM_ADMIN.getLevel()) {
					CompanyUser compUser = this.bs.retrieveCompanyUser(user, this.domain.get().getCompany());
					dto = UserDto.from(compUser, true);
				} else {
					dto = UserDto.from(user, true);
				}
				
				return this.success(dto);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Salvar as permissões do usuário.
	 * 
	 * @param permissions Lista com as permissões a serem salvas.
	 * @return UserPermission Lista de permissões do usuário.
	 */
	@PostMapping("/api/user/permissions")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> saveUserPermissions(@RequestBody PaginatedList<UserPermission> permissions) {
		List<UserPermission> list = permissions.getList();
		if (!list.isEmpty()) {
			for (UserPermission permission : list) {
				User user = permission.getUser();
				if (user.getAccessLevel() > this.userSession.getAccessLevel()) {
					return this.forbidden();
				}
			}
		}

		try {
			for (UserPermission permission : permissions.getList()) {
				permission.setCompany(this.domain.get().getCompany());
				String url = domain.get().getBaseUrl() + "/#/users/profilerUser/" + permission.getUser().getId();
				this.bs.saveUserPermission(permission, url);
			}
			return this.success(permissions);
		} catch (Throwable ex) {
			LOGGER.error("Error while proxying the file upload.", ex);
			return this.fail();
		}
	}

	/**
	 * Listar as permições do sistema, se o usuário possuir essa permição local, o
	 * atributo "granted" vira como true
	 * 
	 * @param userId id do usuário
	 * @return lista com as permições desse usuário
	 */
	@GetMapping("/api/user/permissions")
	public ResponseEntity<?> listPermissions(@RequestParam(required = false) Long userId) {
		try {
			User user;
			if (userId == null) {
				user = userSession.getUser();
			} else {
				user = this.bs.existsByUser(userId);
			}
			PaginatedList<PermissionDTO> list = this.bs.listPermissionsByUser(user);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Error while proxying the file upload.", ex);
			return this.fail();
		}
	}

	/**
	 * Atualizar configuração de notificação.
	 * 
	 * @param id                  Id do usuário que atualizará configurações de
	 *                            notificação.
	 * @param notificationSetting Nova configuração de notificação.
	 * @return companyUser Instituição que o usuário está desbloqueado.
	 */
	@PostMapping("/api/user/updateNotificationSettings")
	public ResponseEntity<?> updateNotificationSettings(@RequestBody UpdateNotificationSettingsDto dto) {
		if (dto.id() == null) {
			return this.notFound();
		}
		if (this.domain == null) {
			return this.fail("Você só pode atualizar configuração de notificação estando em um domínio de uma instituição.");
		}
		try {
			User user = this.bs.exists(dto.id(), User.class);
			if (user == null) {
				return this.notFound();
			}

			CompanyUser companyUser = this.bs.retrieveCompanyUser(user, this.domain.get().getCompany());
			if (companyUser != null) {
				companyUser.setNotificationSetting(dto.notificationSetting());
				this.bs.persist(companyUser);
			}
			return this.success(companyUser);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Importar usuários.
	 * 
	 * @param nameList[]   Vetor de nomes.
	 * @param emailList[]  Vetor de emails.
	 * @param accessList[] Vetor de nível de acesso.
	 */
	@PostMapping("/api/user/importUsers")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> importUsers(@RequestBody ImportUsersDto dto) {
		try {
			this.bs.inviteUsers(dto.nameList(), dto.emailList(), dto.accessList());
			return this.success();
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail(e.getMessage());
		}
	}

}
