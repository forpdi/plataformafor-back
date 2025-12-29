package org.forpdi.core.user;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.LogicalDeletable;
import org.forpdi.core.common.SimpleIdentifiable;
import org.forpdi.security.authz.AccessLevels;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity(name = User.TABLE)
@Table(name = User.TABLE)
public class User implements UserDetails, SimpleIdentifiable, LogicalDeletable {
	public static final String TABLE = "fpdi_user";
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Long id;
	
	@Column(nullable=false)
	@SkipSerialization
	protected boolean deleted = false;

	@Column(nullable=false, unique=true, length=255)
	@SkipSerialization
	private String email;

	@Column(nullable=true, unique=true, length=255)
	@SkipSerialization
	private String cpf;

	@Column(nullable=true, unique=true, length=255)
	@SkipSerialization
	private String cellphone;

	@Column(nullable=true, length=255)
	@SkipSerialization
	private String password;
	
	@Column(nullable=false, length=255)
	private String name;

	@Column(nullable=true, length=255)
	@SkipSerialization
	private String phone;

	@Column(nullable=true, length=255)
	@SkipSerialization
	private String department;
	
	@Column(nullable=true, length=255)
	@SkipSerialization
	private String picture;

	@Temporal(TemporalType.DATE)
	@Column(nullable=true)
	@SkipSerialization
	private Date birthdate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	@SkipSerialization
	private Date creation = new Date();
	
	@Temporal(TemporalType.TIMESTAMP)
	@SkipSerialization
	private Date termsAcceptance;
	
	@Column(nullable=true, length=128, unique=true)
	@SkipSerialization
	private String inviteToken;

	@Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    public int getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(int tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public void incrementTokenVersion() {
        this.tokenVersion++;
    }

	@SkipSerialization
	private boolean active = false;
	
	private int accessLevel = AccessLevels.NONE.getLevel();
	
	@Override
	public boolean isDeleted() {
		return this.deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getCellphone() {
		return cellphone;
	}

	public void setCellphone(String cellphone) {
		this.cellphone = cellphone;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}
	
	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public Date getTermsAcceptance() {
		return termsAcceptance;
	}

	public void setTermsAcceptance(Date termsAcceptance) {
		this.termsAcceptance = termsAcceptance;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(int accessLevel) {
		this.accessLevel = accessLevel;
	}

	public String getInviteToken() {
		return inviteToken;
	}

	public void setInviteToken(String inviteToken) {
		this.inviteToken = inviteToken;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		AccessLevels level = AccessLevels.getByLevel(this.accessLevel);
		GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(level.asRoleName());
		return Arrays.asList(grantedAuthority);
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return Objects.equals(id, user.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
