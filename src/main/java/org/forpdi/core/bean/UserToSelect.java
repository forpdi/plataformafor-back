package org.forpdi.core.bean;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.forpdi.core.user.User;

public class UserToSelect implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
    private int accessLevel;
    private String name;
	
	public UserToSelect(long id, String name, int accessLevel) {
		this.id = id;
		this.name = name;
        this.accessLevel = accessLevel;
	}
	
    public static List<User> toUserList(List<UserToSelect> usersToSelect) {
        return usersToSelect.stream().map(userToSelect -> {
            User user = new User();
            user.setName(userToSelect.getName());
            user.setId(userToSelect.getId());
            user.setAccessLevel(userToSelect.getAccessLevel());
            return user;
        }).collect(Collectors.toList());
    }
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    
    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }
}
