
package org.forpdi.core.user;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.Projection;
import org.hibernate.criterion.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPasswordUsedBSTest {

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private HibernateDAO dao;

	@Mock
	Criteria criteria;

	@InjectMocks
	private UserPasswordUsedBS userPasswordUsedBS;

	@Test
	@DisplayName("Deve verificar se a senha foi recentemente utilizada")
	void testIsRecentlyUsedPassword() {
		String password = "password123";
		User user = new User();
		List<UserPasswordUsed> usedPasswords = new ArrayList<>();

		UserPasswordUsed userPasswordUsed = new UserPasswordUsed();
		userPasswordUsed.setPassword("password123");
		usedPasswords.add(userPasswordUsed);


		when(dao.newCriteria(UserPasswordUsed.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, UserPasswordUsed.class)).thenReturn(usedPasswords);
		when(passwordEncoder.matches(password, userPasswordUsed.getPassword())).thenReturn(true);

		boolean result = userPasswordUsedBS.isRecentlyUsedPassword(password, user);

		assertTrue(result, "A senha deve ter sido recentemente utilizada.");
	}

	@Test
	@DisplayName("Deve retornar false quando a senha não foi recentemente utilizada")
	void testIsNotRecentlyUsedPassword() {
		String password = "newPassword123";
		User user = new User();
		List<UserPasswordUsed> usedPasswords = new ArrayList<>();

		UserPasswordUsed userPasswordUsed = new UserPasswordUsed();
		userPasswordUsed.setPassword("oldPassword123");
		usedPasswords.add(userPasswordUsed);


		when(dao.newCriteria(UserPasswordUsed.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, UserPasswordUsed.class)).thenReturn(usedPasswords);
		when(passwordEncoder.matches(password, userPasswordUsed.getPassword())).thenReturn(false);

		boolean result = userPasswordUsedBS.isRecentlyUsedPassword(password, user);

		assertFalse(result, "A senha não deve ter sido recentemente utilizada.");
	}

	@Test
	@DisplayName("Deve persistir a senha utilizada quando o número de senhas utilizadas for menor que o limite")
	void testPersistUsedPasswordUnderLimit() {
		String password = "newPassword123";
		User user = new User();
		UserPasswordUsed userPasswordUsed = new UserPasswordUsed();

		List<UserPasswordUsed> usedPasswords = new ArrayList<>();
		usedPasswords.add(userPasswordUsed);

		when(dao.newCriteria(UserPasswordUsed.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn((long) usedPasswords.size());
		when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

		userPasswordUsedBS.persistUsedPassword(password, user);

		verify(dao).newCriteria(UserPasswordUsed.class);
		verify(criteria).add(any(Criterion.class));
		verify(criteria).setProjection(any(Projection.class));
		verify(criteria).uniqueResult();
		verify(passwordEncoder).encode(password);
		verify(dao, times(1)).persist(any(UserPasswordUsed.class));
	}

	@Test
	@DisplayName("Deve substituir a senha mais antiga quando o número de senhas utilizadas atingir o limite")
	void testPersistUsedPasswordOverLimit() {
		String password = "newPassword123";
		User user = new User();
		Long userNumberOfPasswords = 6L;
		UserPasswordUsed lastPasswordUsed = new UserPasswordUsed();
		lastPasswordUsed.setPassword("oldPassword123");
		lastPasswordUsed.setCreation(new Date());

		Criteria countingCriteria = mock(Criteria.class);
		Criteria oldestPasswordCriteria = mock(Criteria.class);

		when(dao.newCriteria(UserPasswordUsed.class))
			.thenReturn(countingCriteria)
			.thenReturn(oldestPasswordCriteria);

		when(countingCriteria.add(any(Criterion.class))).thenReturn(countingCriteria);
		when(countingCriteria.setProjection(any(Projection.class))).thenReturn(countingCriteria);
		when(countingCriteria.uniqueResult()).thenReturn(userNumberOfPasswords);

		when(oldestPasswordCriteria.add(any(Criterion.class))).thenReturn(oldestPasswordCriteria);
		when(oldestPasswordCriteria.setMaxResults(1)).thenReturn(oldestPasswordCriteria);
		when(oldestPasswordCriteria.addOrder(any(Order.class))).thenReturn(oldestPasswordCriteria);
		when(oldestPasswordCriteria.uniqueResult()).thenReturn(lastPasswordUsed);

		userPasswordUsedBS.persistUsedPassword(password, user);

		verify(dao, times(2)).newCriteria(any());
		verify(countingCriteria).add(any(Criterion.class));
		verify(countingCriteria).setProjection(any(Projection.class));
		verify(countingCriteria).uniqueResult();

		verify(oldestPasswordCriteria).add(any(Criterion.class));
		verify(oldestPasswordCriteria).setMaxResults(1);
		verify(oldestPasswordCriteria).addOrder(any(Order.class));
		verify(oldestPasswordCriteria).uniqueResult();
		verify(passwordEncoder).encode(password);
	}
}
