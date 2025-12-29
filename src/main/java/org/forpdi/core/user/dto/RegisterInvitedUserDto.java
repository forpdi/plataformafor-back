package org.forpdi.core.user.dto;

import org.forpdi.core.user.User;

public record RegisterInvitedUserDto(User user, String birthdate, Boolean termsAccepted) {

}
