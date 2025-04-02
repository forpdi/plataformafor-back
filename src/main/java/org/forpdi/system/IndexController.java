package org.forpdi.system;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.forpdi.core.common.AbstractController;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyThemeFactory;
import org.forpdi.core.properties.CoreMessages;
import org.forpdi.core.properties.SystemConfigs;
import org.forpdi.core.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class IndexController extends AbstractController {
	
	@Autowired
	private CompanyBS companyBS;
	@Autowired
	protected HttpServletResponse response;

	@GetMapping("/environment")
	public void envInfo() {
		StringBuilder body = new StringBuilder();
		CompanyDomain domain = this.companyBS.currentDomain();
		CoreMessages msg = new CoreMessages(CoreMessages.DEFAULT_LOCALE);
		
		body.append("EnvInfo={");
		body.append("'baseUrl': '").append(SystemConfigs.getBaseBrl()).append("'");
		body.append(",'themes': ").append(CompanyThemeFactory.getInstance().toJSON());
		if (domain == null) {
			body.append(",'company': null");
			body.append(",'themeCss': '")
				.append(CompanyThemeFactory.getDefaultTheme().getCSSFile())
				.append("'");
		} else {
			Map<String, String> messagesOverlays = this.companyBS.retrieveMessagesOverlay(domain.getCompany());
			msg.setOverlay(messagesOverlays);
			Company company = domain.getCompany();
			if (company.getLogoArchive() != null) {
				company.getLogoArchive().setCompany(null);
			}
			body.append(",'company': ").append(JsonUtil.toJson(domain.getCompany()));
			body.append(",'themeCss': '")
				.append(CompanyThemeFactory.getInstance().get(domain.getTheme()).getCSSFile())
				.append("'");
		}
		body.append(",'messages':").append(msg.getJSONMessages());
		body.append(",'buildVersion':\"").append(SystemConfigs.getBuildVersion());
		body.append("\"};");
		try {
			this.response.setCharacterEncoding("UTF-8");
			this.response.addHeader("Content-Type", "text/javascript");

			this.response.getWriter().print(body.toString());
		} catch (IOException ex) {
			LOGGER.error("Unexpected runtime error", ex);
		}
	}
}
