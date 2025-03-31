package org.forpdi.core.user.dto;

import java.io.Serializable;
import java.util.Date;

import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyUser;
import org.forpdi.core.user.User;
import org.forpdi.core.utils.Util;

public final class UserDto implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String email;
	private String cpf;
	private String name;
	private String cellphone;
	private String phone;
	private String department;
	private Date creation;
	private Date termsAcceptance;
	private boolean active;
	private int accessLevel;
	private boolean blocked;
	private int notificationSettings;
	private Company company;
	private boolean deleted;
	
	public static UserDto from(CompanyUser companyUser, boolean censorCpf) {
		var dto = from(companyUser.getUser(), censorCpf);
		dto.blocked = companyUser.isBlocked();
		dto.notificationSettings = companyUser.getNotificationSetting();
		dto.company = companyUser.getCompany();
		
		return dto;
	}
	
	public static UserDto from(User user, boolean censorCpf) {
		var dto = new UserDto();
		dto.id = user.getId();
		dto.email = user.getEmail();
		dto.cpf = censorCpf ? Util.censorCPF(user.getCpf()) : user.getCpf();
		dto.name = user.getName();
		dto.cellphone = user.getCellphone();
		dto.phone = user.getPhone();
		dto.department = user.getDepartment();
		dto.creation = user.getCreation();
		dto.termsAcceptance = user.getTermsAcceptance();
		dto.active = user.isActive();
		dto.accessLevel = user.getAccessLevel();
		dto.deleted = user.isDeleted();
		
		return dto;
	}

	public long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getCpf() {
		return cpf;
	}

	public String getName() {
		return name;
	}

	public String getCellphone() {
		return cellphone;
	}

	public String getPhone() {
		return phone;
	}

	public String getDepartment() {
		return department;
	}

	public Date getCreation() {
		return creation;
	}

	public Date getTermsAcceptance() {
		return termsAcceptance;
	}

	public boolean isActive() {
		return active;
	}

	public int getAccessLevel() {
		return accessLevel;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public int getNotificationSettings() {
		return notificationSettings;
	}

	public Company getCompany() {
		return company;
	}

	public boolean isDeleted() {
		return deleted;
	}
}
