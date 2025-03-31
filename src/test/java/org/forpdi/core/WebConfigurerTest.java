
package org.forpdi.core;

import com.google.gson.Gson;
import org.forpdi.core.company.CompanyDomainContextInterceptor;
import org.forpdi.dashboard.interceptor.CommunityDashboardInterceptor;
import org.forpdi.security.captcha.CaptchaInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class WebConfigurerTest {
	@InjectMocks
	WebConfigurer webConfigurer;
	@Mock
	CompanyDomainContextInterceptor companyDomainContextInterceptor;
	@Mock
	CommunityDashboardInterceptor communityDashboardInterceptor;
	@Mock
	CaptchaInterceptor captchaInterceptor;
	@Mock
	Gson gson;

	@Test
	@DisplayName("Testa a adição de interceptors.")
	void testInterceptorsAddedInCorrectOrder() {
		InterceptorRegistry registry = mock(InterceptorRegistry.class);

		webConfigurer.addInterceptors(registry);

		InOrder inOrder = inOrder(registry);
		inOrder.verify(registry).addInterceptor(companyDomainContextInterceptor);
		inOrder.verify(registry).addInterceptor(communityDashboardInterceptor);
		inOrder.verify(registry).addInterceptor(captchaInterceptor);
		inOrder.verifyNoMoreInteractions();
	}


	@Test
	@DisplayName("Testa se GsonHttpMessageConverter está configurado e o adiciona para converters list")
	public void testMessageConvertersConfiguration() {
		List<HttpMessageConverter<?>> converters = new ArrayList<>();

		webConfigurer.configureMessageConverters(converters);

		assertEquals(1, converters.size());
		assertInstanceOf(GsonHttpMessageConverter.class, converters.get(0));
		GsonHttpMessageConverter gsonConverter = (GsonHttpMessageConverter) converters.get(0);
		assertEquals(gson, gsonConverter.getGson());
	}
}
