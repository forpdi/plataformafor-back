package org.forpdi.system.jobsetup;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobLockService {

	@Autowired
	private JobLockRepository repository;
		
	public boolean lockJob(Class<?> jobClass, LocalDateTime expiryAt) {
		if (!jobIsLocked(jobClass)) {
			JobLock jobLock = new JobLock(jobClass.getName(), LocalDateTime.now(), expiryAt);
			repository.save(jobLock);
			return true;
		}
		
		return false;
	}
	
	private boolean jobIsLocked(Class<?> jobClass) {
		Optional<JobLock> lock = repository.findByJobNameAndExpiryAtAfter(jobClass.getName(), LocalDateTime.now());
		return lock.isPresent();
	}
}
