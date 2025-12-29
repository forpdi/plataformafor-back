package org.forpdi.core.user;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.UserToSelect;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.Disjunction;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.ProjectionList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.company.CompanyUser;
import org.forpdi.core.notification.NotificationBS;
import org.forpdi.core.notification.NotificationSetting;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.user.authz.Permission;
import org.forpdi.core.user.authz.PermissionFactory;
import org.forpdi.core.user.authz.UserPermission;
import org.forpdi.core.user.dto.EditAccessLevelDto;
import org.forpdi.core.user.dto.UserDto;
import org.forpdi.core.utils.Util;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.ManagerField;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.permissions.PermissionDTO;
import org.forpdi.planning.structure.FavoriteLevelInstance;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.CryptManager;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.core.common.Order;
import org.forpdi.core.common.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.forpdi.system.EmailUtilsPlugin;


/**
 * @author Renato R. R. de Oliveira
 */
@Service
public class UserBS extends HibernateBusiness {

	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private NotificationBS notificationBS;
	@Autowired
	private UserPasswordUsedBS userPasswordUsedBS;
	@Autowired
	private UserSession userSession;
	@Autowired
	private PasswordEncoder passwordEncoder;

    private SecureRandom numberGenerator = new SecureRandom();

	private static final Logger LOG = LoggerFactory.getLogger(UserBS.class);


	private static final String ALERT_TO_EMAIL = "eduardo.yasuda@sinerji.com.br";
	private static final String ALERT_TO_NAME  = "Suporte";
	private static final String ALERT_SUBJECT  = "[ALERTA] Healthcheck do domínio falhou";
	private static final long MIN_ALERT_INTERVAL_MS = 30 * 60 * 1000L;
	private volatile long lastAlertAt = 0L;

	private static final String PASSWORD_POLICY_MESSAGE = "Verifique o nível de força de sua senha. Para tornar sua senha mais forte, crie uma senha entre 8 e 15 caracteres, letras maiúsculas e minúsculas, adicione números e caracteres especiais.";
	private static final Pattern PASSWORD_POLICY_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,15}$");

	@org.springframework.beans.factory.annotation.Autowired
	private EmailUtilsPlugin emailPlugin;


	private String resolveHealthUrlFromCurrentDomain() {
		final String actuatorPath = "actuator/health";

		String base = null;
		try {
			base = domain.get().getBaseUrl();
		} catch (Exception e) {
			LOG.warn("[health-url] Não consegui obter baseUrl do domínio.", e);
		}

		if (base == null || base.trim().isEmpty()) {
			String url = "https://example.invalid/" + actuatorPath;
			LOG.warn("[health-url] baseUrl vazio/nulo. Usando URL inválida para disparar alerta: {}", url);
			return url;
		}

		base = base.trim();

		try {
			java.net.URI u = java.net.URI.create(base);
			String scheme = (u.getScheme() == null || u.getScheme().isBlank()) ? "https" : u.getScheme();
			String host = u.getHost();
			int port = u.getPort();
			String path = u.getPath();
			if (path == null)
				path = "";
			if (!path.endsWith("/"))
				path += "/";

			if (host == null || host.isBlank()) {
				String url = "https://example.invalid/" + actuatorPath;
				LOG.warn("[health-url] Host não pôde ser extraído de '{}'. Usando URL inválida: {}", base, url);
				return url;
			}

			String authority = (port > -1) ? (host + ":" + port) : host;
			String url = scheme + "://" + authority + path + actuatorPath;
			LOG.info("[health-url] Usando URL pública para health: {}", url);
			return url;

		} catch (Exception e) {
			String url = "https://example.invalid/" + actuatorPath;
			LOG.warn("[health-url] baseUrl inválido '{}'. Usando URL inválida para disparar alerta: {}", base, url, e);
			return url;
		}
	}
		/**
	 * Salvar o usuário no banco de dados.
	 * 
	 * @param user Usuário a ser salvo.
	 * @return void.
	 */
	public void save(User user) {
		if (this.existsByEmail(user.getEmail()) != null) {
			throw new IllegalArgumentException("Já existe um usuários cadastrado com este e-mail.");
		}
		validatePassword(user.getPassword(), null);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setActive(true);
		user.setDeleted(false);
		user.setCreation(new Date());
		this.persist(user);
	}

	/**
	 * Deletar o usuário de uma instituição
	 * 
	 * @param companyUser Usuário a ser deletado.
	 * @return user Usuário deletado.
	 */
	@Transactional
	public User deleteCompanyUser(CompanyUser companyUser) {
		deleteUserFavorites(companyUser);
		this.dao.flush();

		User user = companyUser.getUser();
		setUserDeleted(user);
		this.dao.merge(user);
		this.remove(companyUser);
		return user;
	}

	private void deleteUserFavorites(CompanyUser companyUser) {
		Criteria criteria = this.dao.newCriteria(FavoriteLevelInstance.class)
				.add(Restrictions.eq("companyUser", companyUser));

		List<FavoriteLevelInstance> favorites = this.dao.findByCriteria(criteria, FavoriteLevelInstance.class);

		favorites.forEach(this.dao::delete); // troque por soft-delete se quiser
	}

	/**
	 * Deletar todos usuários de uma instituição
	 * 
	 * @return void.
	 */
	public void deleteAllCompanyUsers(Company company) {
		List<CompanyUser> companyUsers = listCompanyUsers(company);

		this.dao.execute(dao -> {
			for (CompanyUser companyUser : companyUsers) {
				deleteUserFavorites(companyUser);
				setUserDeleted(companyUser.getUser());
				dao.delete(companyUser);
			}
		});
	}

	public User updateUser(User user, String newPassword) {
		User existent = this.exists(user.getId(), User.class);
		CompanyUser companyUser = retrieveCompanyUser(existent, domain.get().getCompany());
		
		this.validateUpdateUser(user, existent, companyUser);
		
		boolean accessLevelChanged = existent.getAccessLevel() != user.getAccessLevel();
		
		existent.setName(user.getName());
		existent.setBirthdate(user.getBirthdate());
		existent.setCellphone(user.getCellphone());
		existent.setPhone(user.getPhone());
		existent.setDepartment(user.getDepartment());
		
		boolean hasHighLevelAccess = this.userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel()
				|| this.userSession.getUser().getId().equals(existent.getId());

		if (hasHighLevelAccess) {
			existent.setEmail(user.getEmail());
			if (user.getCpf() != null) {
				if (!Util.hasOnlyNumbers(user.getCpf()) || !GeneralUtils.validateCpf(user.getCpf())) {
					throw new IllegalArgumentException("CPF inválido!");
				}
				existent.setCpf(user.getCpf());
			}
		}

		companyUser.setAccessLevel(user.getAccessLevel());
		
		boolean passwordChanged = newPassword != null;
		if (passwordChanged && !hasHighLevelAccess) {
			throw new IllegalStateException("Você não tem permissão para alterar a senha deste usuário!");
		}
		if (passwordChanged) {
			this.validatePassword(newPassword, existent);
			existent.setPassword(passwordEncoder.encode(newPassword));
		}

		this.dao.execute(dao -> {
			dao.merge(existent);
			dao.merge(companyUser);
		});
		
		if (passwordChanged) {
			this.userPasswordUsedBS.persistUsedPassword(newPassword, existent);
		}

		if (accessLevelChanged && !this.userSession.getUser().getId().equals(user.getId())) {
			this.notifyOnAccessLevelChange(companyUser, user.getId(), existent);
		}

		return existent;
	}

	
	private void validateUpdateUser(User user, User existent, CompanyUser companyUser) {
		if (companyUser == null) {
			throw new IllegalArgumentException("Usuário não cadastrado nesta instituição");
		}
		
		if (user.getAccessLevel() > this.userSession.getAccessLevel()) {
			throw new IllegalArgumentException("Você não tem autorização para alterar o tipo de usuário para um nível acima");
		}
		if (existent.getAccessLevel() > this.userSession.getAccessLevel()) {
			throw new IllegalArgumentException("Você não tem autorização para alterar um usuário com nível acima");
		}
		if (user.getEmail() != null && !user.getEmail().equals(existent.getEmail()) && this.existsByEmail(user.getEmail()) != null) {
			throw new IllegalArgumentException("Já existe um usuário cadastrado com este email");
		}
		if (user.getCpf() != null && !user.getCpf().equals(existent.getCpf()) && this.existsByCpf(user.getCpf()) != null) {
			throw new IllegalArgumentException("Já existe um usuário cadastrado com este CPF");
		}
		if (user.getCellphone() != null && !user.getCellphone().equals(existent.getCellphone())
				&& this.existsByCellphone(user.getCellphone()) != null) {
			throw new IllegalArgumentException("Já existe um usuário cadastrado com este celular");
		}
	}
	
	private void setUserDeleted(User user) {
		user.setDeleted(true);
		user.setName("");
		String randStr = RandomStringUtils.random(5, 0, 0, true, false, null, new SecureRandom());
		user.setEmail(randStr);
		user.setInviteToken(null);
		user.setCellphone(null);
		user.setPhone(null);
		user.setCpf(null);
	}


	/**
	 * Verifica se o usuário está deletado.
	 * 
	 * @param email E-mail do usuário.
	 * @return boolean false Usuário está deletado. true Usuário não está deletado.
	 */
	public boolean userIsDeleted(String email) {
		Criteria criteria = this.dao.newCriteria(User.class)
				.add(Restrictions.eq("email", email))
				.add(Restrictions.eq("deleted", true));
	
		User user = (User) criteria.uniqueResult();
	
		// Deve retornar TRUE quando encontrou um usuário deletado
		return user != null;
	}

	public void validateInvites(String nameList[], String emailList[], Integer accessList[]) throws EmailException {
		for (int i = 0; i < nameList.length; i++) {
			User user = this.existsByEmail(emailList[i]);
			if (user != null && !user.isDeleted()) {
				throw new IllegalStateException(
						"O convite para o email " + emailList[i] + " já foi enviado, utilize a opção de reenviar na tabela Usuários do Sistema");
			}
			if (this.userSession.getAccessLevel() < AccessLevels.SYSTEM_ADMIN.getLevel()
					&& accessList[i] >= AccessLevels.SYSTEM_ADMIN.getLevel()) {
				throw new IllegalArgumentException("Você não tem autorização para conceder essa permissão ao usuário");
			}
		}
	}
	
	/**
	 * Envia convite para usuário acessar o sistema.
	 * 
	 * @param name        Nome do usuário.
	 * @param email       E-mail do usuário.
	 * @param accessLevel Nível de acesso do usuário.
	 * @return User Usuário que foi convidado.
	 * @throws EmailException Exceção usuário convidado já tem e-mail no banco de
	 *                        dados.
	 */
	@Transactional
	public User inviteUser(String name, String email, int accessLevel) throws EmailException {
		User user = this.existsByEmail(email);

		CompanyUser companyUser = retrieveCompanyUser(user, this.domain.get().getCompany());
		if (user != null && !user.isDeleted() && user.isActive() && companyUser != null) {
			throw new IllegalStateException(
					"O convite para o email " + email + " já foi enviado, utilize a opção de reenviar na tabela Usuários do Sistema");
		}

		if (user != null && companyUser == null){
			setUserDeleted(user);
			this.dao.persist(user);
		}

		User user1 = this.existsByEmail(email);
		return this.sendUserInvite(user1, name, email, accessLevel);
	}
	
	/**
	 * Persiste convite do usuário no sistema.
	 * 
	 * @param user		  Usuário sendo convidado.
	 * @param name        Nome do usuário.
	 * @param email       E-mail do usuário.
	 * @param accessLevel Nível de acesso do usuário.
	 * @return User Usuário que foi convidado.
	 * @throws EmailException Exceção usuário convidado já tem e-mail no banco de
	 *                        dados.
	 */
	public User sendUserInvite(User user, String name, String email, int accessLevel) throws EmailException {
		if (user == null) {
			user = new User();
		}
		
		user.setName(name);
		user.setEmail(email);
		user.setInviteToken(CryptManager.token("inv-" + user.getId() + "-" + domain.get().getCompany().getId()));
		user.setName(name);
		user.setAccessLevel(accessLevel);
		user.setDeleted(false);
		user.setActive(false);
		this.dao.persist(user);

		CompanyUser companyUser = new CompanyUser();
		companyUser.setCompany(domain.get().getCompany());
		companyUser.setUser(user);
		CompanyUser existent = this.exists(companyUser, CompanyUser.class);
		if (existent == null) {
			companyUser.setAccessLevel(accessLevel);
			this.dao.persist(companyUser);
		}

		if (GeneralUtils.isEmpty(user.getCpf())) {
			this.sendInvitationEmail(user);
		}
		return user;
	}

	/**
	 * Envia convite para usuários acessarem o sistema.
	 * 
	 * @param nameList        	Lista de nomes dos usuários.
	 * @param emailList       	Lista de e-mails dos usuários.
	 * @param accessList		Lista de níveis de acesso dos usuários.
	 * @throws EmailException 	Exceção usuário convidado já tem e-mail no banco de
	 *                        dados.
	 */
	public void inviteUsers(String[] nameList, String[] emailList, Integer[] accessList) throws EmailException {
		this.validateInvites(nameList, emailList, accessList);

		java.util.List<Integer> indicesUnicos = new java.util.ArrayList<>();
		java.util.List<String> vistos = new java.util.ArrayList<>();

		for (int i = 0; i < emailList.length; i++) {
			String emailNorm = (emailList[i] == null) ? null : emailList[i].trim().toLowerCase();
			emailList[i] = emailNorm;
			if (emailNorm == null || emailNorm.isEmpty()) {
				continue;
			}
			boolean jaVisto = false;
			for (String v : vistos) {
				if (v.equals(emailNorm)) {
					jaVisto = true;
					break;
				}
			}
			if (!jaVisto) {
				vistos.add(emailNorm);
				indicesUnicos.add(i);
			}
		}

		for (Integer idx : indicesUnicos) {
			this.inviteUser(nameList[idx], emailList[idx], accessList[idx]);
		}
	}

	private static boolean isReachableStatus(int code) {
		return (code >= 200 && code < 400) || code == 401 || code == 403 || code == 405;
	}

	public boolean isUp(String urlString, int connectTimeoutMs, int readTimeoutMs) {
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
				.setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
				.setResponseTimeout(Timeout.ofMilliseconds(readTimeoutMs))
				.build();

		try (CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.build()) {

			final HttpHead head = new HttpHead(urlString);
			return httpClient.execute(head, response -> {
				int status = response.getCode();
				EntityUtils.consume(response.getEntity());
				if (isReachableStatus(status)) {
					LOG.info("Health check (HEAD) {} => {} (considerando alcançável).", urlString, status);
					return true;
				}
				LOG.warn("Health check (HEAD) {} => {}. Tentando GET…", urlString, status);

				final HttpGet get = new HttpGet(urlString);
				return httpClient.execute(get, r -> {
					int s = r.getCode();
					EntityUtils.consume(r.getEntity());
					if (isReachableStatus(s)) {
						LOG.info("Health check (GET) {} => {} (considerando alcançável).", urlString, s);
						return true;
					}
					LOG.warn("Health check (GET) {} => {} (falhou).", urlString, s);
					return false;
				});
			});

		} catch (javax.net.ssl.SSLException ssl) {
			if (urlString.startsWith("https://")) {
				String httpUrl = urlString.replaceFirst("^https://", "http://");
				LOG.warn("Health check em {} falhou por SSL; tentando {}", urlString, httpUrl);
				return isUp(httpUrl, connectTimeoutMs, readTimeoutMs);
			}
			LOG.error("Falha SSL no health check: {}", urlString, ssl);
			return false;

		} catch (IOException e) {
			LOG.error("Falha I/O no health check: {}", urlString, e);
			return false;
		}
	}

	
	/**
	 * Envia e-mail para usuário que foi convidado.
	 * 
	 * @param User Usuário que foi convidado.
	 * @throws EmailException Exceção e-mail já está cadastrado no sistema.
	 */
	public void sendInvitationEmail(User user) throws EmailException {
		String healthUrl = resolveHealthUrlFromCurrentDomain();
		String baseUrl = domain.get().getBaseUrl();

		boolean up = isUp(healthUrl, 3000, 3000);
		if (!up) {
			long now = System.currentTimeMillis();
			if (now - lastAlertAt >= MIN_ALERT_INTERVAL_MS) {
				lastAlertAt = now;
				try {
					String body = "Falha ao acessar " + baseUrl + " em " + new Date(now) + ".\n"
							+ "Verifique se o domínio está correto.";
					emailPlugin.sendHtmlEmail(ALERT_TO_EMAIL, ALERT_TO_NAME, ALERT_SUBJECT, body, null);
					LOG.warn("Alerta de healthcheck enviado para {}", ALERT_TO_EMAIL);
				} catch (EmailException ex) {
					LOG.error("Não foi possível enviar o alerta de healthcheck.", ex);
				}
			}
			throw new RuntimeException("O domínio/health da instituição está inacessível: " + baseUrl);
		}

		String cleanedBaseUrl = (baseUrl == null) ? "" : baseUrl.trim();
		String fullCleanUrl = cleanedBaseUrl + "#/register/" + user.getInviteToken();
		this.notificationBS.sendNotificationEmail(NotificationType.INVITE_USER, null, "", user, fullCleanUrl);
	}

	/**
	 * Busca usuário pelo e-mail.
	 * 
	 * @param email E-mail para buscar o usuário.
	 * @return User Usuário que contém o e-mail.
	 */
	public User existsByEmail(String email) {
		Criteria criteria = this.dao.newCriteria(User.class).add(Restrictions.eq("email", email));
		return (User) criteria.uniqueResult();
	}

	/**
	 * Busca usuário pelo cpf.
	 * 
	 * @param cpf Cpf para buscar o usuário.
	 * @return User Usuário que contém o cpf.
	 */
	public User existsByCpf(String cpf) {
		Criteria criteria = this.dao.newCriteria(User.class).add(Restrictions.eq("cpf", cpf));
		return (User) criteria.uniqueResult();
	}

	/**
	 * Buscar usuário pelo celular.
	 * 
	 * @param cellphone celular para buscar o usuário.
	 * @return User Usuário que contém o celular.
	 */
	public User existsByCellphone(String cellphone) {
		Criteria criteria = this.dao.newCriteria(User.class).add(Restrictions.eq("cellphone", cellphone));
		return (User) criteria.uniqueResult();
	}

	/**
	 * Buscar usuário pelo id.
	 * 
	 * @param idUser Id do usuário.
	 * @return User Usuário que contém o id.
	 */
	public User existsByUser(Long idUser) {
		Criteria criteria = this.dao.newCriteria(User.class).add(Restrictions.eq("id", idUser));
		return (User) criteria.uniqueResult();
	}

	/**
	 * Buscar usuário pelo token de acesso.
	 * 
	 * @param token token para buscar usuário.
	 * @return User Usuário que contém o token.
	 */
	public User existsByInviteToken(String token) {
		Criteria criteria = this.dao.newCriteria(User.class).add(Restrictions.eq("inviteToken", token));
		return (User) criteria.uniqueResult();
	}

	/**
	 * Verificar se usuário é de uma determinada instituição.
	 * 
	 * @param user    Usuário para verificação.
	 * @param company Instituição para verificação.
	 * @return CompanyUser Instituição que pertence a determinado usuário.
	 */
	public CompanyUser retrieveCompanyUser(User user, Company company) {
		Criteria criteria = this.dao.newCriteria(CompanyUser.class);
		criteria.add(Restrictions.eq("company", company));
		criteria.add(Restrictions.eq("user", user));
		return (CompanyUser) criteria.uniqueResult();
	}


	/**
	 * Recupera o nível de acesso do usuário para a company do domínio atual.
	 * 
	 * @param user Usuário para recuperar o nível de acesso.
	 * @return Nível de acesso para a instituição atual.
	 */
	public int retrieveAccessLevel(User user) {
		if (domain == null) {
			return user.getAccessLevel();
		}
		Criteria criteria = this.dao.newCriteria(CompanyUser.class).add(Restrictions.eq("user", user))
				.add(Restrictions.eq("company", domain.get().getCompany()));
		CompanyUser companyUser = (CompanyUser) criteria.uniqueResult();
		if (companyUser == null) {
			return user.getAccessLevel();
		}
		return Math.max(user.getAccessLevel(), companyUser.getAccessLevel());
	}

	/**
	 * Lista todos usuários do sistema.
	 * 
	 * @param page Número da página.
	 * 
	 * @return PaginatedList<User> Lista de usuários.
	 */
	public PaginatedList<UserDto> listSystemUsers(DefaultParams params) {
		return listUsers(null, params);
	}

	/**
	 * Listar usuários da instituição do domínio acessado.
	 * 
	 * @param page Número da página.
	 * @param term Termo de pesquisa dos usuários
	 * @return PaginatedList<User> Lista de usuários.
	 */
	public PaginatedList<UserDto> listFromCurrentCompany(DefaultParams params) {
		return listUsers(domain.get().getCompany(), params);
	}

	public PaginatedList<UserDto> listFromAuthenticatedCompany(Company company, DefaultParams params) {
		return listUsers(company, params);
	}

	/**
	 * Listar usuários da instituição
	 * 
	 * @param page Número da página.
	 * 
	 * @return PaginatedList<User> Lista de usuários.
	 */
	public PaginatedList<UserDto> listUsers(Company company, DefaultParams params) {
		PaginatedList<UserDto> results = new PaginatedList<>();
		int page = params.getPage();
		int pageSize = params.getPageSize();

		Criteria criteria = this.dao.newCriteria(CompanyUser.class)
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("company", "company", JoinType.INNER_JOIN);

		Criteria counting = this.dao.newCriteria(CompanyUser.class)
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("company", "company", JoinType.INNER_JOIN)
				.setProjection(Projections.countDistinct("user.id"));

		if (params.hasTerm()) {
			String term = "%" + params.getTerm() + "%";
			Criterion companyCriterion = Restrictions.like("company.name", term).ignoreCase();
			Criterion nameCriterion = Restrictions.like("user.name", term).ignoreCase();
			Disjunction expression = Restrictions.or(nameCriterion, companyCriterion);
			criteria.add(expression);
			counting.add(expression);
		}

		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		} else {
			criteria.addOrder(Order.asc("company"))
					.addOrder(Order.asc("user.name"));
		}

		criteria.add(Restrictions.eq("company", company));
		counting.add(Restrictions.eq("company", company));

		@SuppressWarnings("unchecked")
		List<CompanyUser> companyUsers = this.dao.findByCriteria(criteria, CompanyUser.class);

		Pattern emailPattern = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
		List<UserDto> users = new ArrayList<>(companyUsers.size());
		for (CompanyUser cu : companyUsers) {
			User user = cu.getUser();

			if (user.getEmail() == null || !emailPattern.matcher(user.getEmail()).matches()) {
				continue;
			}

			int effectiveLevel = Math.max(user.getAccessLevel(), cu.getAccessLevel());
			user.setAccessLevel(effectiveLevel);
			if (effectiveLevel > userSession.getAccessLevel()) {
				continue;
			}
			boolean censorCpf = userSession.getAccessLevel() < AccessLevels.COMPANY_ADMIN.getLevel();
			users.add(UserDto.from(cu, censorCpf));
		}

		results.setList(users);
		results.setTotal((Long) counting.uniqueResult());
		return results;
	}

	/**
	 * Listar TODOS os usuários do sistema (visão do System Admin).
	 */
	public PaginatedList<UserDto> listAllSystemUsers(DefaultParams params) {
		PaginatedList<UserDto> results = new PaginatedList<>();

		int page = params.getPage();
		int pageSize = params.getPageSize();

		Criteria criteria = this.dao.newCriteria(CompanyUser.class)
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("company", "company", JoinType.LEFT_OUTER_JOIN);

		Criteria counting = this.dao.newCriteria(CompanyUser.class)
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.setProjection(Projections.countDistinct("user.id"));

		if (params.hasTerm()) {
			String like = "%" + params.getTerm() + "%";
			Disjunction expr = Restrictions.or(
					Restrictions.like("user.name", like).ignoreCase(),
					Restrictions.like("user.email", like).ignoreCase(),
					Restrictions.like("company.name", like).ignoreCase());
			criteria.add(expr);
			counting.add(expr);
		}

		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		} else {
			criteria.addOrder(Order.asc("user.name"));
		}

		List<CompanyUser> companyUsers = this.dao.findByCriteria(criteria, CompanyUser.class);

		List<UserDto> dtos = new ArrayList<>(companyUsers.size());
		for (CompanyUser cu : companyUsers) {
			User user = cu.getUser();
			int accessLevel = Math.max(user.getAccessLevel(), cu.getAccessLevel());
			user.setAccessLevel(accessLevel);
			dtos.add(UserDto.from(cu, false));
		}

		results.setList(dtos);
		results.setTotal((Long) counting.uniqueResult());
		return results;
	}

	/**
	* Listar usuários filtrados da instituição
	* 
	* @param minAcessLevel nivel de acesso minimo dos usuarios listados.
	* @param maxAcessLevel nivel de acesso maximo dos usuarios listados.
	* 
	* @return List<ItemToSelect> Lista de usuários.
	*/
	public List<UserToSelect> listUsersToSelect(Company company, Integer minAccessLevel, Integer maxAccessLevel) {
	 
		Criteria criteria = this.dao.newCriteria(CompanyUser.class)
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.add(Restrictions.eq("company", company));
		if (minAccessLevel != null) {
				criteria.add(Restrictions.ge("user.accessLevel", minAccessLevel));
		}			
		if (maxAccessLevel != null) {
				criteria.add(Restrictions.le("user.accessLevel", maxAccessLevel));
		}

		criteria.addOrder(Order.asc("user.name"));

		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("user.id"), "id");
		projList.add(Projections.property("user.name"), "name");
		projList.add(Projections.property("user.accessLevel"), "accessLevel");
		
		criteria.setProjection(projList);
		criteria.setResultTransformer(UserToSelect.class);

		List<UserToSelect> result = this.dao.findByCriteria(criteria, UserToSelect.class);
		
		return result;
	}
		
	/**
	 * Requisição do usuário para recuperar a senha.
	 * 
	 * @param user Usuário para recuperar a senha.
	 * @return UserRecoverRequest Requisição relizada.
	 */
	public UserRecoverRequest requestRecover(User user) {
		if (user == null)
			return null;
		UserRecoverRequest req = new UserRecoverRequest();
		req.setUser(user);
		req.setCreation(new Date());
		req.setCreationIp(this.request.getRemoteAddr());
		req.setExpiration(new Date(System.currentTimeMillis() + 1800000L));
		req.setToken(CryptManager.digest(numberGenerator.nextDouble() + "@" + System.currentTimeMillis() + "#" + user.getEmail()));
		this.dao.persist(req);
		// TODO Send the recovering email.
		return req;
	}

	/**
	 * Trocar o password do usuário.
	 * 
	 * @param password Novo password
	 * @param token    Token de identificação do usuário.
	 * @return boolean true Troca de password do usuário ocorreu com sucesso.
	 */
	public void resetPassword(String password, String token) {
		UserRecoverRequest req = this.retrieveRecoverRequest(token);
		if (req == null) {
			throw new IllegalStateException("Token de recuperação inválida ou expirada.");
		}

		User user = req.getUser();

		validatePassword(password, user);

		user.setPassword(passwordEncoder.encode(password));
		this.dao.persist(user);
		req.setRecover(new Date());
		req.setRecoverIp(this.request.getRemoteAddr());
		req.setUsed(true);
		this.dao.merge(req);
	}


	/**
	 * Requisição para recuperar usuário.
	 * 
	 * @param token Token de identificação do usuário a ser recuperado.
	 * @return UserRecoverRequest Requisição realizada.
	 * 
	 */
	public UserRecoverRequest retrieveRecoverRequest(String token) {
		UserRecoverRequest req = this.dao.exists(token, UserRecoverRequest.class);
		if (req == null)
			return null;
		if (req.getExpiration().getTime() < System.currentTimeMillis())
			return null;
		if (req.isUsed())
			return null;
		return req;
	}

	/**
	 * Listar usuários pela instituição.
	 * 
	 * @param void.
	 * @return PaginatedList<User> Lista contendo os usuários.
	 */
	public PaginatedList<User> listUsersByCompany() {
		PaginatedList<User> results = new PaginatedList<User>();
		Criteria criteria = this.dao.newCriteria(CompanyUser.class);

		criteria.createAlias("user", "user", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("company", domain.get().getCompany()));
		criteria.add(Restrictions.eq("blocked", false));
		// criteria.add(Restrictions.eq("user.active",false));
		criteria.addOrder(Order.asc("user.name"));

		List<CompanyUser> companyUsers = this.dao.findByCriteria(criteria, CompanyUser.class);
		ArrayList<User> users = new ArrayList<User>(companyUsers.size());
		for (CompanyUser companyUser : companyUsers) {
			users.add(companyUser.getUser());
		}
		results.setList(users);
		return results;
	}

	/**
	 * Listar usuários da instituição.
	 * 
	 * @param void.
	 * @return List<CompanyUser> Lista contendo os usuários da instituição.
	 */
	public List<CompanyUser> listCompanyUsers(Company company) {
		Criteria criteria = this.dao.newCriteria(CompanyUser.class);

		criteria.add(Restrictions.eq("company", company));

		return this.dao.findByCriteria(criteria, CompanyUser.class);
	}

	/**
	 * Buscar usuários de acordo com um nome.
	 * 
	 * @param terms Nome buscado.
	 * @return PaginatedList<User> Lista de usuários que contém o nome buscado em
	 *         alguma parte do nome.
	 */
	public PaginatedList<User> listUsersBySearch(String terms, Company company) {
		PaginatedList<User> results = new PaginatedList<User>();
		
		Criteria criteria = this.dao.newCriteria(CompanyUser.class);
		
		criteria.createAlias("user", "user")
			.add(Restrictions.eq("user.deleted", false))
			.add(Restrictions.eq("company", company));
		
		Criteria count = this.dao.newCriteria(CompanyUser.class)
				.createAlias("user", "user")
				.add(Restrictions.eq("company", company))
				.add(Restrictions.eq("user.deleted", false))
				.setProjection(Projections.countDistinct("user.id"));

		if (terms != null && !terms.isEmpty()) {
			Disjunction or = Restrictions.disjunction();			
			or.add(Restrictions.like("user.name", "%" + terms + "%").ignoreCase());

			criteria.add(or);
			count.add(or);
		}

		criteria.setProjection(Projections.property("user"));
		
		results.setList(this.dao.findByCriteria(criteria, User.class));
		results.setTotal((Long) count.uniqueResult());
		
		return results;

	}

	/**
	 * Listas as permissões do usuário.
	 * 
	 * @param user Usuário que será listado suas permissões.
	 * @return PaginatedList<PermissionDTO> Lista de permissões do usuário.
	 */
	public PaginatedList<PermissionDTO> listPermissionsByUser(User user) {
		PaginatedList<PermissionDTO> results = new PaginatedList<PermissionDTO>();

		if (domain.get() == null || domain.get().getCompany() == null) {
			results.setTotal((long) 0);
			results.setList(new ArrayList<>());
			return results;
		}

		Criteria criteria = this.dao.newCriteria(UserPermission.class);
		criteria.setProjection(Projections.projectionList().add(Projections.property("permission"), "permission"));
		criteria.add(Restrictions.eq("user", user));
		criteria.add(Restrictions.eq("company", domain.get().getCompany()));
		criteria.add(Restrictions.eq("revoked", false));
		criteria.setResultTransformer(String.class);
		List<String> userPermissions = this.dao.findByCriteria(criteria, String.class);

		PermissionFactory factory = PermissionFactory.getInstance();
		List<PermissionDTO> dtoList = new ArrayList<>();
		factory.each(new Consumer<Permission>() {

			@Override
			public void accept(Permission t) {
				PermissionDTO dto = new PermissionDTO();
				dto.setPermission(t.getDisplayName());
				dto.setAccessLevel(t.getRequiredAccessLevel());
				dto.setDescription(t.getDescription());
				dto.setType(t.getClass().getCanonicalName());
				if (userPermissions.contains(t.getClass().getCanonicalName())) {
					dto.setGranted(true);
				} else {
					dto.setGranted(false);
				}
				dtoList.add(dto);
			}
		});

		results.setList(dtoList);
		results.setTotal((long) dtoList.size());
		return results;
	}

	/**
	 * Retornar as permissões do usuário pelo seu nível de acesso.
	 * 
	 * @param user       Usuário no qual será retornando suas permissões.
	 * @param permission Nível de acesso do usuário.
	 * @return UserPermission Permissões do usuário.
	 */
	public UserPermission retrieveUserPermissionByUserAndPermission(User user, String permission) {
		Criteria criteria = this.dao.newCriteria(UserPermission.class);
		criteria.add(Restrictions.eq("user", user));
		criteria.add(Restrictions.eq("company", domain.get().getCompany()));
		criteria.add(Restrictions.eq("permission", permission));

		return (UserPermission) criteria.uniqueResult();
	}

	/**
	 * Salvar a permissão de um usuário.
	 * 
	 * @param permission Permissão a ser salva.
	 * @throws EmailException
	 * 
	 * @return void.
	 */
	public void saveUserPermission(UserPermission permission, String url) throws EmailException {
		boolean changePermission = false;
		UserPermission existent = this.retrieveUserPermissionByUserAndPermission(permission.getUser(),
				permission.getPermission());
		if ((existent == null || existent.isRevoked() != permission.isRevoked()) && !permission.isRevoked())
			changePermission = true;
		if (existent == null) {
			existent = new UserPermission();
		}
		existent.setCompany(permission.getCompany());
		existent.setPermission(permission.getPermission());
		existent.setUser(permission.getUser());
		existent.setRevoked(permission.isRevoked());
		if (changePermission) {
			CompanyUser companyUser = this.retrieveCompanyUser(permission.getUser(), domain.get().getCompany());
			if (companyUser.getNotificationSetting() == NotificationSetting.DEFAULT.getSetting()
					|| companyUser.getNotificationSetting() == NotificationSetting.RECEIVE_ALL_BY_EMAIL.getSetting()) {
				this.notificationBS.sendNotification(NotificationType.PERMISSION_CHANGED,
						this.notificationBS.getPermissionText(permission), null, permission.getUser().getId(), url);
				this.notificationBS.sendNotificationEmail(NotificationType.PERMISSION_CHANGED,
						this.notificationBS.getPermissionText(permission), "",
						this.existsByUser(permission.getUser().getId()), url);
			} else if (companyUser.getNotificationSetting() == NotificationSetting.DO_NOT_RECEIVE_EMAIL.getSetting()) {
				this.notificationBS.sendNotification(NotificationType.PERMISSION_CHANGED,
						this.notificationBS.getPermissionText(permission), null, permission.getUser().getId(), url);
			}
		}
		this.dao.persist(existent);
	}
	
	/**
	 * Buscar responsável de um determinado nível do Plano de Metas.
	 * 
	 * @param levelInstance Nível do Plano de Metas.
	 * @return User Responsável pelo nível do Plano de Metas.
	 */
	public User retrieveResponsible(StructureLevelInstance levelInstance) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.createAlias("attribute", "attribute");
		criteria.add(Restrictions.eq("levelInstance", levelInstance));
		criteria.add(Restrictions.eq("attribute.type", ResponsibleField.class.getCanonicalName()));

		AttributeInstance instance = (AttributeInstance) criteria.uniqueResult();
		User user;
		if (instance != null) {
			user = this.existsByUser(Long.valueOf(instance.getValue()));
		} else {
			user = null;
		}
		return user;
	}
	
	/**
	 * Buscar o gestor de um determinado nível do Plano de Metas.
	 * 
	 * @param levelInstance Nível do Plano de Metas.
	 * @return User Gestor responsável pelo nível do Plano de Metas.
	 */
	public User retrieveManager(StructureLevelInstance levelInstance) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.createAlias("attribute", "attribute");
		criteria.add(Restrictions.eq("levelInstance", levelInstance));
		criteria.add(Restrictions.eq("attribute.type", ManagerField.class.getCanonicalName()));

		AttributeInstance instance = (AttributeInstance) criteria.uniqueResult();
		User manager;
		if (instance != null) {
			manager = this.existsByUser(Long.valueOf(instance.getValue()));
		} else {
			manager = null;
		}
		return manager;
	}


	public PaginatedList<User> listByPermissionLevel(AccessLevels accessLevel) {
		PaginatedList<User> results = new PaginatedList<User>();

		Criteria criteria = this.dao.newCriteria(User.class);
		criteria.add(Restrictions.eq("accessLevel", accessLevel.getLevel()));
		criteria.add(Restrictions.eq("deleted", false));

		List<User> list = this.dao.findByCriteria(criteria, User.class);

		results.setList(list);
		results.setTotal((long) list.size());
		return results;
	}

	public void validatePassword(String password, User user) {
		if (password == null || !PASSWORD_POLICY_PATTERN.matcher(password).matches()) {
			throw new IllegalArgumentException(PASSWORD_POLICY_MESSAGE);
		}

		if (user != null && userPasswordUsedBS.isRecentlyUsedPassword(password, user)) {
			throw new IllegalArgumentException("A nova senha deve ser diferente das 5 senhas anteriormente usadas. Tente novamente.");
		}
	}

	public void notifyOnAccessLevelChange(CompanyUser companyUser, Long userId, User existent) {
		try {
			String url = domain.get().getBaseUrl() + "#/users/profile/info";
			if (companyUser.getNotificationSetting() == NotificationSetting.DEFAULT.getSetting()
					|| companyUser.getNotificationSetting() == NotificationSetting.RECEIVE_ALL_BY_EMAIL.getSetting()) {
				this.notificationBS.sendNotification(NotificationType.ACCESSLEVEL_CHANGED,
						this.notificationBS.getAccessLevelText(companyUser), null, existent.getId(), url);
				this.notificationBS.sendNotificationEmail(NotificationType.ACCESSLEVEL_CHANGED,
						this.notificationBS.getAccessLevelText(companyUser), "", existent, url);
			} else if (companyUser.getNotificationSetting() == NotificationSetting.DO_NOT_RECEIVE_EMAIL.getSetting()) {
				this.notificationBS.sendNotification(NotificationType.ACCESSLEVEL_CHANGED,
						this.notificationBS.getAccessLevelText(companyUser), null, existent.getId(), url);
			}
		} catch (Throwable e) {
			LOGGER.error("Erro no envio de notificações: ", e);
		}
	}
	
	public boolean isLoggedUserOrHasAccess(Long userId, AccessLevels accessLevel) {
		User user = this.exists(userId, User.class);

		if (user == null) {
			throw new IllegalArgumentException("User not found");
		}

		return user.getId().equals(userSession.getUser().getId())
				|| userSession.getAccessLevel() >= accessLevel.getLevel(); 
	}
	
	public boolean userIsLinkedToCurrentCompany(User user) {
		CompanyUser companyUser = retrieveCompanyUser(user, domain.get().getCompany());
		
		return companyUser != null;
	}

	@Transactional
	public User updateUserAccessLevel(EditAccessLevelDto dto, User loggedInUser, Company currentCompany) {
		User userToEdit = this.existsByUser(dto.userId());
		if (userToEdit == null) {
			throw new RuntimeException("Usuário com ID " + dto.userId() + " não encontrado.");
		}

		if (userToEdit.getAccessLevel() > loggedInUser.getAccessLevel()) {
			throw new SecurityException("Permissão negada para alterar este usuário.");
		}

		CompanyUser companyUser = this.retrieveCompanyUser(userToEdit, currentCompany);

		userToEdit.setAccessLevel(dto.accessLevel());

		if (companyUser != null) {
			companyUser.setAccessLevel(dto.accessLevel());
		}

		this.notifyOnAccessLevelChange(companyUser, dto.userId(), userToEdit);

		return userToEdit;
	}
}
