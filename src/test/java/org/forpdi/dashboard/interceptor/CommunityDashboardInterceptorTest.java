package org.forpdi.dashboard.interceptor;

import jakarta.servlet.http.HttpServletResponse;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.security.auth.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommunityDashboardInterceptorTest {

    private CommunityDashboardInterceptor interceptor;
    private CompanyDomainContext domain;
    private UserSession session;

    @BeforeEach
    void setup() throws Exception {
        interceptor = new CommunityDashboardInterceptor();
        domain = mock(CompanyDomainContext.class, RETURNS_DEEP_STUBS);
        session = mock(UserSession.class);

        var fDomain = CommunityDashboardInterceptor.class.getDeclaredField("domain");
        fDomain.setAccessible(true);
        fDomain.set(interceptor, domain);

        var fSession = CommunityDashboardInterceptor.class.getDeclaredField("session");
        fSession.setAccessible(true);
        fSession.set(interceptor, session);
    }

    static class DummyController {
        @CommunityDashboard
        public void annotated() {
        }

        public void plain() {
        }
    }

    private HandlerMethod handlerFor(String methodName) throws Exception {
        Method m = DummyController.class.getMethod(methodName);
        return new HandlerMethod(new DummyController(), m);
    }

    @Test
    @DisplayName("Método não anotado → sempre true")
    void preHandle_plainMethod_returnsTrue() throws Exception {
        boolean result = interceptor.preHandle(null, mock(HttpServletResponse.class), handlerFor("plain"));
        assertTrue(result);
    }

    @Test
    @DisplayName("Método anotado + empresa com showDashboard=true → true")
    void preHandle_annotatedCompanyDashboardEnabled_returnsTrue() throws Exception {
        Company c = new Company();
        c.setShowDashboard(true);
        when(domain.get().getCompany()).thenReturn(c);

        boolean result = interceptor.preHandle(null, mock(HttpServletResponse.class), handlerFor("annotated"));
        assertTrue(result);
    }

    @Test
    @DisplayName("Método anotado + dashboard desabilitado + usuário logado → true")
    void preHandle_annotatedDashboardDisabledUserLogged_returnsTrue() throws Exception {
        Company c = new Company();
        c.setShowDashboard(false);
        when(domain.get().getCompany()).thenReturn(c);
        when(session.isLogged()).thenReturn(true);

        boolean result = interceptor.preHandle(null, mock(HttpServletResponse.class), handlerFor("annotated"));
        assertTrue(result);
    }

    @Test
    @DisplayName("Método anotado + dashboard desabilitado + usuário não logado → false e 401")
    void preHandle_annotatedDashboardDisabledUserNotLogged_returnsFalseAnd401() throws Exception {
        Company c = new Company();
        c.setShowDashboard(false);
        when(domain.get().getCompany()).thenReturn(c);
        when(session.isLogged()).thenReturn(false);

        StringWriter sw = new StringWriter();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        boolean result = interceptor.preHandle(null, resp, handlerFor("annotated"));
        assertFalse(result);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(sw.toString().contains("Você não tem permissão"));
    }
}
