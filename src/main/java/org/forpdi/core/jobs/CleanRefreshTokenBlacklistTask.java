package org.forpdi.core.jobs;

import java.util.Date;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import org.forpdi.security.auth.RefreshTokenBlacklist;
import org.forpdi.system.jobsetup.JobsSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class CleanRefreshTokenBlacklistTask {

	private static final Logger LOG = LoggerFactory.getLogger(CleanRefreshTokenBlacklistTask.class);
	
	@Autowired
	private EntityManager entityManager;
	
	@Scheduled(cron = JobsSetup.CLEAN_REFRESH_TOKEN_BLACKLIST_CRON)
	@Transactional
	public void execute() {
		LOG.info("Cleaning expired refresh tokens in balcklist...");
		var expiredTokens = listExpiredTokensInBlacklist();
		for (RefreshTokenBlacklist rtb : expiredTokens) {
			entityManager.remove(rtb);
		}
	}
	
	private List<RefreshTokenBlacklist> listExpiredTokensInBlacklist() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<RefreshTokenBlacklist> cq = cb.createQuery(RefreshTokenBlacklist.class);
		Root<RefreshTokenBlacklist> root = cq.from(RefreshTokenBlacklist.class);
		
		cq.where(cb.lessThan(root.get("expiration"), new Date()));
		
		return entityManager.createQuery(cq).getResultList();
	}

}
