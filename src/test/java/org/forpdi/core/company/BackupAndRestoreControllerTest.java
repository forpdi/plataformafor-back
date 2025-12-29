package org.forpdi.core.company;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.forpdi.core.common.Response;
import org.forpdi.security.auth.AuthData;
import org.forpdi.security.auth.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockSettings;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Suite de testes para {@link BackupAndRestoreController}.
 *
 * <p>Requisitos de build: JUnit 5 (≥5.8), Mockito (≥4), Spring Test & Spring Web.</p>
 */
class BackupAndRestoreControllerTest {

    private <T> T positiveMock(Class<T> clazz) {
        Answer<Object> ans = (InvocationOnMock inv) -> {
            Class<?> ret = inv.getMethod().getReturnType();
            if (ret == boolean.class || ret == Boolean.class)            return true;
            if (java.util.Collection.class.isAssignableFrom(ret))        return Collections.emptyList();
            return null;
        };
        MockSettings st = withSettings().defaultAnswer(ans);
        return mock(clazz, st);
    }

    /** {@link CompanyDomainContext} cujo <code>get()</code> devolve um {@link CompanyDomain} com empresa válida. */
    private CompanyDomainContext contextWithCompany() {
        CompanyDomainContext ctx  = mock(CompanyDomainContext.class);
        CompanyDomain        cDom = mock(CompanyDomain.class);
        when(ctx.get()).thenReturn(cDom);
        when(cDom.getCompany()).thenReturn(mock(Company.class));
        return ctx;
    }

    private UserSession session() {
        UserSession s  = mock(UserSession.class);
        AuthData    ad = mock(AuthData.class);
        when(s.getAuthData()).thenReturn(ad);
        when(ad.getId()).thenReturn(1L);
        return s;
    }


    @Test @DisplayName("validexport → BAD_REQUEST quando contexto de domínio é nulo")
    void validexport_domainNull() {
        BackupAndRestoreController ctrl = new BackupAndRestoreController();
        ReflectionTestUtils.setField(ctrl, "domain", null);
        ReflectionTestUtils.setField(ctrl, "companyBS", positiveMock(CompanyBS.class));
        ReflectionTestUtils.setField(ctrl, "dbbackup",  positiveMock(BackupAndRestoreHelper.class));

        ResponseEntity<?> resp = ctrl.validexport(session());

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertFalse(((Response<?>) resp.getBody()).isSuccess());
    }

    @Test @DisplayName("validexport → BAD_REQUEST quando empresa é nula")
    void validexport_companyNull() {
        BackupAndRestoreController ctrl = new BackupAndRestoreController();

        CompanyDomainContext ctx = mock(CompanyDomainContext.class);
        CompanyDomain        cd  = mock(CompanyDomain.class);
        when(ctx.get()).thenReturn(cd);
        when(cd.getCompany()).thenReturn(null);

        ReflectionTestUtils.setField(ctrl, "domain",    ctx);
        ReflectionTestUtils.setField(ctrl, "companyBS", positiveMock(CompanyBS.class));
        ReflectionTestUtils.setField(ctrl, "dbbackup",  positiveMock(BackupAndRestoreHelper.class));

        ResponseEntity<?> resp = ctrl.validexport(session());

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertFalse(((Response<?>) resp.getBody()).isSuccess());
    }

    @Test @DisplayName("validexport → BAD_REQUEST em exceção inesperada")
    void validexport_excecao() {
        BackupAndRestoreController ctrl = new BackupAndRestoreController();

        CompanyDomainContext ctx = mock(CompanyDomainContext.class);
        when(ctx.get()).thenThrow(new RuntimeException("boom"));

        ReflectionTestUtils.setField(ctrl, "domain",    ctx);
        ReflectionTestUtils.setField(ctrl, "companyBS", positiveMock(CompanyBS.class));
        ReflectionTestUtils.setField(ctrl, "dbbackup",  positiveMock(BackupAndRestoreHelper.class));

        ResponseEntity<?> resp = ctrl.validexport(session());

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertFalse(((Response<?>) resp.getBody()).isSuccess());
    }

    @Test @DisplayName("fbkupload → BAD_REQUEST com arquivo nulo")
    void fbkupload_arquivoNulo() {
        BackupAndRestoreController ctrl = new BackupAndRestoreController();
        ReflectionTestUtils.setField(ctrl, "companyBS", positiveMock(CompanyBS.class));
        ReflectionTestUtils.setField(ctrl, "dbbackup",  positiveMock(BackupAndRestoreHelper.class));

        ResponseEntity<?> resp = ctrl.fbkupload(null);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(((Response<?>) resp.getBody()).getMessage().contains("upload falhou"));
    }
}
