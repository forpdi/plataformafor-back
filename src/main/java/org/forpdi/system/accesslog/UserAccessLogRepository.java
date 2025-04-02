package org.forpdi.system.accesslog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccessLogRepository extends JpaRepository<UserAccessLog, Long> {
	UserAccessLog findByUserId(Long userId);
}
