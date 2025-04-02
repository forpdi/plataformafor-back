package org.forpdi.core.user.dto;

public record RegisterUserDto(String name, String email, String password, Integer accessLevel) {

}
