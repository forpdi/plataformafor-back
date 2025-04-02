package org.forpdi.security;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.forpdi.core.CustomAccessDeniedHandler;
import org.forpdi.security.auth.AuthContextFilter;
import org.forpdi.security.auth.JwtService;
import org.forrisco.core.plan.PlanRiskController;
import org.forrisco.core.policy.PolicyController;
import org.forrisco.core.unit.UnitController;
import org.forrisco.risk.RiskController;
import org.forrisco.risk.incident.IncidentController;
import org.forrisco.risk.preventiveaction.PreventiveActionController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private static final List<UrlMatcher> PUBLIC_URLS = List.of(
			UrlMatcher.ant(HttpMethod.GET, "/api/test-db-connections/**"),
			UrlMatcher.ant(HttpMethod.GET, "/environment"),
			UrlMatcher.ant(HttpMethod.GET, "/api/company/logo"),
			UrlMatcher.ant(HttpMethod.POST, "/api/auth/login"),
			UrlMatcher.ant(HttpMethod.GET, "/api/auth/refresh-access-token"),
			UrlMatcher.ant(HttpMethod.POST, "/api/user/recover"),
			UrlMatcher.regex(HttpMethod.GET, "/api/user/reset/\\w+?"),
			UrlMatcher.regex(HttpMethod.POST, "/api/user/reset/\\w+?"),
			UrlMatcher.regex(HttpMethod.GET, "/api/user/register/\\w+?"),
			UrlMatcher.regex(HttpMethod.POST, "/api/user/register/\\w+?"),
			UrlMatcher.ant(HttpMethod.GET, "/actuator/**"),
			UrlMatcher.ant(HttpMethod.GET, "/v3/api-docs/**"),
			UrlMatcher.ant(HttpMethod.GET, "/swagger-ui/**")
	);
	
	private static final List<UrlMatcher> COMMUNITY_DASH_URLS = List.of(
			UrlMatcher.regex(HttpMethod.GET, PlanRiskController.PATH + "/\\d+?"),
			UrlMatcher.ant(HttpMethod.GET, PlanRiskController.PATH + "/list-to-select"),
			UrlMatcher.regex(HttpMethod.GET, PolicyController.PATH + "/risklevel/\\d+?"),
			UrlMatcher.ant(HttpMethod.GET, UnitController.PATH + "/list-to-select"),
			UrlMatcher.regex(HttpMethod.GET, UnitController.PATH + "/allByPlan\\?planId=\\d+?"),
			UrlMatcher.ant(HttpMethod.GET, RiskController.PATH + "/history"),
			UrlMatcher.regex(HttpMethod.GET, RiskController.PATH + "\\?planId=\\d+?"),
			UrlMatcher.regex(HttpMethod.GET, IncidentController.PATH + "/incidents\\?planId=\\d+?"),
			UrlMatcher.regex(HttpMethod.GET, PreventiveActionController.PATH + "/filteredActions\\?planId=\\d+?"),
			UrlMatcher.ant(HttpMethod.GET, "/api/dashboard/community/**"),
			UrlMatcher.ant(HttpMethod.GET, "/api/planmacro"),
			UrlMatcher.regex(HttpMethod.GET, "/api/plan\\?parentId=\\d+?"),
			UrlMatcher.regex(HttpMethod.GET, "/api/structure/levelsonsfilter\\?.*?")
			
	);

	
	
	@Autowired
	private JwtService jwtService;
	@Autowired
	private AuthenticationProvider authenticationProvider;
	@Autowired
	private RoleHierarchy roleHierarchy;
    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;
		
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		configureCorsAndCsrf(http);
		configureRequestsAuthorization(http);
		
		
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.authenticationProvider(authenticationProvider);
		http.addFilterBefore(new AuthContextFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
    	
        return http.build();
    }
	
	private void configureCorsAndCsrf(HttpSecurity http) throws Exception {
		http.cors()
			.and()
			.csrf()
			.disable();
	}
	
	private void configureRequestsAuthorization(HttpSecurity http) throws Exception {
		var registry =  http.authorizeRequests();

		final List<UrlMatcher> allPublicUrls = Stream
				.concat(PUBLIC_URLS.stream(), COMMUNITY_DASH_URLS.stream())
				.collect(Collectors.toList());
		
		allPublicUrls.forEach(methodAndPattern -> {
			if (methodAndPattern.useRegexPattern) {
				registry.regexMatchers(methodAndPattern.method, methodAndPattern.pattern)
						.permitAll();
			} else {
				registry.antMatchers(methodAndPattern.method, methodAndPattern.pattern)
						.permitAll();
			}
		});
		
		registry.expressionHandler(webExpressionHandler())
				.anyRequest()
				.authenticated()
				.and()
				.exceptionHandling().accessDeniedHandler(customAccessDeniedHandler);
	}

	private SecurityExpressionHandler<FilterInvocation> webExpressionHandler() {
		DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
		defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchy);

		return defaultWebSecurityExpressionHandler;
    }
	
	private static class UrlMatcher {
		public final HttpMethod method;
		public final String pattern;
		public final boolean useRegexPattern;

		private UrlMatcher(HttpMethod method, String pattern, boolean useRegexPattern) {
			this.method = method;
			this.pattern = pattern;
			this.useRegexPattern = useRegexPattern;
		}
		
		public static UrlMatcher ant(HttpMethod method, String pattern) {
			return new UrlMatcher(method, pattern, false);
		}
		
		public static UrlMatcher regex(HttpMethod method, String pattern) {
			return new UrlMatcher(method, pattern, true);
		}
	}
}
