package org.forpdi.system.jobsetup;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLockRepository extends JpaRepository<JobLock, String> {
	Optional<JobLock> findByJobNameAndExpiryAtAfter(String jobName, LocalDateTime expiryAt);
}
