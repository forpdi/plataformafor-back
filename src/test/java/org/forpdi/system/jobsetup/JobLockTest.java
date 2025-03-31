package org.forpdi.system.jobsetup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JobLockTest {

	@Test
	@DisplayName("Deve definir e obter o nome do job corretamente")
	void testSetAndGetJobName() {
		JobLock jobLock = new JobLock();
		String jobName = "TestJob";
		jobLock.setJobName(jobName);

		assertEquals(jobName, jobLock.getJobName(), "O nome do job deve ser configurado e recuperado corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter o horário de bloqueio corretamente")
	void testSetAndGetLockedAt() {
		JobLock jobLock = new JobLock();
		LocalDateTime lockedAt = LocalDateTime.now();
		jobLock.setLockedAt(lockedAt);

		assertEquals(lockedAt, jobLock.getLockedAt(), "O horário de bloqueio deve ser configurado e recuperado corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter o horário de expiração corretamente")
	void testSetAndGetExpiryAt() {
		JobLock jobLock = new JobLock();
		LocalDateTime expiryAt = LocalDateTime.now().plusHours(1);
		jobLock.setExpiryAt(expiryAt);

		assertEquals(expiryAt, jobLock.getExpiryAt(), "O horário de expiração deve ser configurado e recuperado corretamente");
	}

	@Test
	@DisplayName("Deve criar um objeto JobLock com construtor parametrizado")
	void testParametrizedConstructor() {
		String jobName = "TestJob";
		LocalDateTime lockedAt = LocalDateTime.now();
		LocalDateTime expiryAt = LocalDateTime.now().plusHours(1);

		JobLock jobLock = new JobLock(jobName, lockedAt, expiryAt);

		assertEquals(jobName, jobLock.getJobName(), "O nome do job deve ser configurado corretamente no construtor");
		assertEquals(lockedAt, jobLock.getLockedAt(), "O horário de bloqueio deve ser configurado corretamente no construtor");
		assertEquals(expiryAt, jobLock.getExpiryAt(), "O horário de expiração deve ser configurado corretamente no construtor");
	}

	@Test
	@DisplayName("Deve criar um objeto JobLock com construtor padrão")
	void testDefaultConstructor() {
		JobLock jobLock = new JobLock();

		assertNull(jobLock.getJobName(), "O nome do job deve ser nulo no construtor padrão");
		assertNull(jobLock.getLockedAt(), "O horário de bloqueio deve ser nulo no construtor padrão");
		assertNull(jobLock.getExpiryAt(), "O horário de expiração deve ser nulo no construtor padrão");
	}
}
