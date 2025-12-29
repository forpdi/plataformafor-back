package org.forpdi.system.accesslog;

import java.time.LocalDate;
import java.util.List;

import org.forpdi.system.accesslog.AccessLogHistory.AccessLogHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessLogHistoryRepository extends JpaRepository<AccessLogHistory, AccessLogHistoryId> {
	List<AccessLogHistory> findById_DateGreaterThanOrderById_DateAsc(LocalDate minDate);
	List<AccessLogHistory> findById_DateBetweenOrderById_DateAsc(LocalDate begin, LocalDate end);
}
