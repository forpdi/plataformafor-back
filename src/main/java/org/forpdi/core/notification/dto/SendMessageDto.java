package org.forpdi.core.notification.dto;

public record SendMessageDto(String subject, String message, Long userId) {

}
