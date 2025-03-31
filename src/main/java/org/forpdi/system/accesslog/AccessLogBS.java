package org.forpdi.system.accesslog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.user.User;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.accesslog.AccessLogHistory.AccessLogHistoryId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessLogBS {
	
	@Autowired
	private UserAccessLogRepository userAccessLogRepository;
	@Autowired
	private AccessLogHistoryRepository accessLogHistoryRepository;
	@Autowired
	private CompanyDomainContext domain;
	
	public void fpdiAccess(User user) {
		if (ignorableAccess(user)) {
			return;
		}
		
		UserAccessLog userAccessLog = findOrCreateUserAccessLog(user);
		LocalDateTime fpdiLastAccess = userAccessLog.getFpdiLastAccess();
		updateHistory(fpdiLastAccess, history -> history.incrementFpdiAccess());

		userAccessLog.setFpdiLastAccess(LocalDateTime.now());
		userAccessLogRepository.save(userAccessLog);
	}
	
	public void friscoAccess(User user) {
		if (ignorableAccess(user)) {
			return;
		}

		UserAccessLog userAccessLog = findOrCreateUserAccessLog(user);
		LocalDateTime friscoLastAccess = userAccessLog.getFriscoLastAccess();
		updateHistory(friscoLastAccess, history -> history.incrementFriscoAccess());
		
		userAccessLog.setFriscoLastAccess(LocalDateTime.now());
		userAccessLogRepository.save(userAccessLog);
	}

	public List<AccessLogHistory> listAccessLogHistory() {
		return accessLogHistoryRepository.findAll();
	}

	public List<AccessLogHistory> listAccessLogHistoryByPeriod(LocalDate begin, LocalDate end) {
		return accessLogHistoryRepository.findById_DateBetweenOrderById_DateAsc(begin, end);
	}

	private boolean ignorableAccess(User user) {
		return user.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel();
	}

	private void updateHistory(LocalDateTime lastAccess, Consumer<AccessLogHistory> onUpdateHistory) {
		LocalDate today = LocalDate.now();
		
		if (lastAccess == null || lastAccess.toLocalDate().isBefore(today)) {
			AccessLogHistory history = findOrCreateAccessLogHistory();
			onUpdateHistory.accept(history);
			accessLogHistoryRepository.save(history);
		}

	}
	
	private UserAccessLog findOrCreateUserAccessLog(User user) {
		UserAccessLog userAccessLog = userAccessLogRepository.findByUserId(user.getId());
		if (userAccessLog == null) {
			userAccessLog = new UserAccessLog();
			userAccessLog.setUserId(user.getId());
		}
		return userAccessLog;
	}
	
	private AccessLogHistory findOrCreateAccessLogHistory() {
		long companyId = domain.get().getCompany().getId();
		LocalDate today = LocalDate.now();
		AccessLogHistoryId id = new AccessLogHistoryId(companyId, today);
		AccessLogHistory history = accessLogHistoryRepository.findById(id).orElse(null);
		if (history == null) {
			history = new AccessLogHistory(companyId, today, 0, 0);
		}
		return history;
	}
}