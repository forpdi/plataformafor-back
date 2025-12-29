package org.forpdi.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class SystemConfigs {
	
	private static String BASE_URL;
	private static String BUILD_VERSION;
	private static String FRONTEND_URL;
	private static String STORE_PDFS;
	
	private SystemConfigs() {}
	
	@Value("${sys.baseurl}")
	private void setBaseUrl(String value) {
		BASE_URL = value;
	}
	
	@Value("${build.version}")
	private void setBuildVersion(String value) {
		BUILD_VERSION = value;
	}

	@Value("${sys.frontendUrl}")
	private void setFrontendUrl(String value) {
		FRONTEND_URL = value;
	}

	@Value("${store.pdfs}")
	private void setStorePdfs(String value) {
		STORE_PDFS = value;
	}

	public static String getBaseBrl() {
		return BASE_URL;
	}

	public static String getBuildVersion() {
		return BUILD_VERSION;
	}
	
	public static String getFrontendUrl() {
		return FRONTEND_URL;
	}
	
	public static String getEmailLogoUrl() {
		return getFrontendUrl() + "/images/plataforma-for-logo.png"; 
	}
	
	public static String getStorePdfs() {
		return STORE_PDFS;
	}

}
