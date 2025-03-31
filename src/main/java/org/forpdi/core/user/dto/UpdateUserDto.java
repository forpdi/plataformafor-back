package org.forpdi.core.user.dto;

import org.forpdi.core.user.User;

public record UpdateUserDto(User user, String currentPassword, String newPassword) {

}
