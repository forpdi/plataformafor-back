package org.forpdi.dashboard.interceptor;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.security.auth.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CommunityDashboardInterceptor implements HandlerInterceptor {

	@Autowired
	private CompanyDomainContext domain;
	
	@Autowired
	private UserSession session;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof HandlerMethod) {
	        HandlerMethod handlerMethod = (HandlerMethod) handler;
	        Method method = handlerMethod.getMethod();
	        if (method.isAnnotationPresent(CommunityDashboard.class)) {
	    		Company company = domain.get().getCompany();
	    		boolean dashCommunityIsEnabled = company != null && company.isShowDashboard();
	    		if (!dashCommunityIsEnabled && !session.isLogged()) {
		            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		            response.getWriter().write("Você não tem permissão para acessar este recurso.");
		    		return false;
	    		}
	        }
		}
		
		return true;
	}

}
