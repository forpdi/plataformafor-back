
package org.forpdi.system.jobsetup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobLockServiceTest {

	@InjectMocks
	private JobLockService jobLockService;

	@Mock
	private JobLockRepository jobLockRepository;

	@Test
	@DisplayName("Deve bloquear um trabalho com sucesso e retornar true.")
	public void testLockJobValidCase() {

		when(jobLockRepository.findByJobNameAndExpiryAtAfter(anyString(), any(LocalDateTime.class)))
			.thenReturn(Optional.empty());

		ReflectionTestUtils.setField(jobLockService, "repository", jobLockRepository);

		LocalDateTime expiryAt = LocalDateTime.now().plusHours(1);

		boolean result = jobLockService.lockJob(JobLock.class, expiryAt);

		assertTrue(result);
		verify(jobLockRepository).save(argThat(lock ->
			lock.getJobName().equals(JobLock.class.getName()) &&
				lock.getExpiryAt().equals(expiryAt)
		));
	}

	@Test
	@DisplayName("Solicitação de bloqueio para uma ocorrência já bloqueada. Deve retornar falso.")
	public void testLockJobWhenJobLockAlreadyIsLocked() {
		JobLock existentJobLocked = new JobLock();
		existentJobLocked.setLockedAt(LocalDateTime.now());

		LocalDateTime expiryAt = LocalDateTime.now().plusHours(1);
		when(jobLockRepository.findByJobNameAndExpiryAtAfter(anyString(), any(LocalDateTime.class)))
			.thenReturn(Optional.of(existentJobLocked));

		boolean result = jobLockService.lockJob(JobLock.class, expiryAt);

		assertFalse(result);
		verify(jobLockRepository).findByJobNameAndExpiryAtAfter(anyString(), any(LocalDateTime.class));
	}

	@Test
	@DisplayName("Ao solicitar o bloqueio para uma classe nula, deve retornar NullPointerException.")
	public void testLockJobWhenJobClassIsNullExceptionCase() {
		JobLockService service = new JobLockService();
		LocalDateTime expiryAt = LocalDateTime.now().plusHours(1);

		assertThrows(NullPointerException.class, () -> {
			service.lockJob(null, expiryAt);
		});
	}
}
