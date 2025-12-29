package org.forpdi.core.storage.dto;

import jakarta.validation.constraints.NotBlank;

public record DownloadFileRequestDto(@NotBlank String file) {
}
