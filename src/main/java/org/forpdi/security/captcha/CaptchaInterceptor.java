package org.forpdi.security.captcha;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.forpdi.core.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CaptchaInterceptor implements HandlerInterceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(CaptchaInterceptor.class);

	@Value("${google.recaptcha.disabled}")
	private Boolean captchaIsDisabled;
	@Value("${google.recaptcha.secret.v2}")
	private String secret;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof HandlerMethod) {
	        HandlerMethod handlerMethod = (HandlerMethod) handler;
	        Method method = handlerMethod.getMethod();
	        if (method.isAnnotationPresent(Captcha.class)) {
	        	if (!accessIsValid(request)) {
		            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		            response.getWriter().write("Não foi possível validar o reCAPTCHA, tente novamente.");
		            return false;
	        	}
	        }
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean accessIsValid(HttpServletRequest request) {
		if (captchaIsDisabled) {
			return true;
		}

		String captchaToken = request.getHeader("Captcha-Token");
		HttpURLConnection httpUrlConnection = null;
		InputStream inputStream = null;

		URL url;
		try {
			url = new URL("https://www.google.com/recaptcha/api/siteverify?secret=" + secret + "&response=" + captchaToken);
			httpUrlConnection = (HttpURLConnection) url.openConnection();
			httpUrlConnection.setRequestMethod("GET");
			httpUrlConnection.connect();
			if (httpUrlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				inputStream = httpUrlConnection.getInputStream();
				String resp = IOUtils.toString(inputStream);
				Map<String, Object> results = JsonUtil.fromJson(resp, Map.class);
				Boolean success = (Boolean) results.get("success");
				return success;
			}
		} catch (IOException e) {
			LOGGER.error("Error in captcha interceptor", e);
		} finally {
			if (httpUrlConnection != null) {
				httpUrlConnection.disconnect();				
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					LOGGER.error("Failed to close inputStream", e);
				}
			}
		}
		
		return false;
	}
	
}
