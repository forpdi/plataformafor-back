package org.forpdi.core.communication.dto;

import java.util.Date;

import org.forpdi.core.user.User;

public record CommunicationDto(
		Long id,
		String title,
		String message,
		Date validityBegin,
		Date validityEnd,
		User responsible,
		boolean showPopup) {
}
