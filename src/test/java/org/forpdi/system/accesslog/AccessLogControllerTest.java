package org.forpdi.system.accesslog;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;

import org.forpdi.core.user.User;
import org.forpdi.security.auth.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AccessLogControllerTest {

    private MockMvc mvc;
    private AccessLogController controller;

    private AccessLogBS bs;
    private UserSession userSession;

    private static void inject(Object target, Class<?> ownerClass, String field, Object value) throws Exception {
        Field f = ownerClass.getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @BeforeEach
    void setup() throws Exception {
        controller = new AccessLogController();
        bs = mock(AccessLogBS.class);
        userSession = mock(UserSession.class);

        inject(controller, AccessLogController.class, "bs", bs);
        inject(controller, org.forpdi.core.common.AbstractController.class, "userSession", userSession);

        when(userSession.getUser()).thenReturn(mock(User.class));

        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/access-log/fpdi-access → 204 e chama bs.fpdiAccess(user)")
    void fpdiAccess_success204() throws Exception {
        mvc.perform(get("/api/access-log/fpdi-access"))
           .andExpect(status().isNoContent());

        verify(bs, times(1)).fpdiAccess(any(User.class));
    }

    @Test
    @DisplayName("GET /api/access-log/frisco-access → 204 e chama bs.friscoAccess(user)")
    void friscoAccess_success204() throws Exception {
        mvc.perform(get("/api/access-log/frisco-access"))
           .andExpect(status().isNoContent());

        verify(bs, times(1)).friscoAccess(any(User.class));
    }

    @Test
    @DisplayName("Exceção no fpdiAccess é engolida e resposta continua 204")
    void fpdiAccess_swallowExceptionStill204() throws Exception {
        doThrow(new RuntimeException("boom")).when(bs).fpdiAccess(any(User.class));

        mvc.perform(get("/api/access-log/fpdi-access"))
           .andExpect(status().isNoContent());

        verify(bs, times(1)).fpdiAccess(any(User.class));
    }

    @Test
    @DisplayName("Exceção no friscoAccess é engolida e resposta continua 204")
    void friscoAccess_swallowExceptionStill204() throws Exception {
        doThrow(new RuntimeException("boom")).when(bs).friscoAccess(any(User.class));

        mvc.perform(get("/api/access-log/frisco-access"))
           .andExpect(status().isNoContent());

        verify(bs, times(1)).friscoAccess(any(User.class));
    }
}
