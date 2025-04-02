package org.forpdi.core.user;

import java.util.Date;
import java.util.List;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author Erick Alves
 */
@Service
public class UserPasswordUsedBS extends HibernateBusiness {

	public static final int PASSWORDS_SIZE = 5;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public boolean isRecentlyUsedPassword(String password, User user) {
		Criteria criteria = this.dao.newCriteria(UserPasswordUsed.class)
			.add(Restrictions.eq("user", user));

		List<UserPasswordUsed> list = dao.findByCriteria(criteria, UserPasswordUsed.class);
		for (UserPasswordUsed userPasswordUsed : list) {
			if (passwordEncoder.matches(password, userPasswordUsed.getPassword())) {
				return true;
			}
		}
		
		return false;
	}
	
	public void persistUsedPassword(String password, User user) {
		Criteria counting = this.dao.newCriteria(UserPasswordUsed.class)
				.add(Restrictions.eq("user", user))
				.setProjection(Projections.countDistinct("id"));
		Long numOfPasswordsUsed = (Long) counting.uniqueResult();
		UserPasswordUsed userPasswordUsed;
		if (numOfPasswordsUsed >= PASSWORDS_SIZE) {
			Criteria criteria = this.dao.newCriteria(UserPasswordUsed.class)
				.add(Restrictions.eq("user", user))
				.setMaxResults(1)
				.addOrder(Order.asc("creation"));
			userPasswordUsed = (UserPasswordUsed) criteria.uniqueResult();
		} else {
			userPasswordUsed = new UserPasswordUsed();
			userPasswordUsed.setUser(user);
		}
		userPasswordUsed.setPassword(passwordEncoder.encode(password));
		userPasswordUsed.setCreation(new Date());
		this.dao.persist(userPasswordUsed);
	}
	
}
