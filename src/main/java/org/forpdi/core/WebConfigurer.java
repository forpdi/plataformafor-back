package org.forpdi.core;

import java.util.List;

import org.forpdi.core.company.CompanyDomainContextInterceptor;
import org.forpdi.dashboard.interceptor.CommunityDashboardInterceptor;
import org.forpdi.security.captcha.CaptchaInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.google.gson.Gson;

@Configuration
public class WebConfigurer implements WebMvcConfigurer {

	@Autowired
	private CompanyDomainContextInterceptor companyDomainContextInterceptor;
	@Autowired
	private CommunityDashboardInterceptor communityDashboardInterceptor;
	@Autowired
	private CaptchaInterceptor captchaInterceptor;
	@Autowired
	private Gson gson;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(companyDomainContextInterceptor);
		registry.addInterceptor(communityDashboardInterceptor);
		registry.addInterceptor(captchaInterceptor);
	}
	
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
		gsonHttpMessageConverter.setGson(gson);
		converters.add(gsonHttpMessageConverter);
	}
}
