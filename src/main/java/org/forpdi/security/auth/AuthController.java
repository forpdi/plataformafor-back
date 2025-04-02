package org.forpdi.security.auth;

import org.forpdi.core.common.AbstractController;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.company.CompanyUser;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.user.UserRepository;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.security.captcha.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController extends AbstractController {

	@Autowired
	private UserBS userBS;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private AuthService authService;
	@Autowired
	private CompanyDomainContext domain;

	@PostMapping("login")
	@Captcha
	public ResponseEntity<?> login(@RequestBody Credentials credentials) {
		try {
			boolean deleted = this.userBS.userIsDeleted(credentials.getEmail());
			if (deleted && this.userBS.existsByEmail(credentials.getEmail()) != null) {
				return this.fail("Usuário inexistente. Entre em contato com o administrador do sistema.");
			}
			
			try {
				authenticationManager.authenticate(
		                new UsernamePasswordAuthenticationToken(
		                        credentials.getEmail(),
		                        credentials.getPassword()));
			} catch (BadCredentialsException e) {
				throw new BadCredentialsException("E-mail e/ou senha inválido(s).");
			}

			User user = userRepository.findByEmail(credentials.getEmail()).orElseThrow();
			
			if (this.domain != null && user.getAccessLevel() < AccessLevels.SYSTEM_ADMIN.getLevel()) {
				CompanyUser companyUser = this.userBS.retrieveCompanyUser(user, this.domain.get().getCompany());
				if (companyUser != null && companyUser.isBlocked()) {
					return this.fail("Este usuário foi bloqueado. Entre em contato com o administrador do sistema.");
				} else if (companyUser == null) {
					return this.fail("Usuário não cadastrado nesse domínio.");
				} else {
			        return this.success(authService.createAuthData(user));
				}

			} else {
				if (this.domain == null && user.getAccessLevel() < AccessLevels.SYSTEM_ADMIN.getLevel()) {
					return this.fail(
							"Este endereço foi desativado. Entre em contato com o administrador da instituição para mais informações.");
				} else {
			        return this.success(authService.createAuthData(user));
				}
			}
		} catch (BadCredentialsException e) {
			return this.fail(e.getMessage());
		} catch (Throwable e) {
			LOGGER.error("Erro no login.", e);
			return this.fail(e.getMessage());
		}
	}

	@GetMapping("refresh-access-token")
	public ResponseEntity<?> refreshAccessToken() {
		try {
			return this.success(authService.refreshAccessToken());
		} catch (Throwable e) {
			LOGGER.error("Fail to refresh token", e);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}
	
	@GetMapping("logout")
	public ResponseEntity<?> logout() {
		try {
			authService.logout();
			return this.success();
		} catch (Throwable e) {
			LOGGER.error("Fail to logout", e);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}
}
