package com.calmlywriter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1_000_000) String content
) {
}
