package com.codewithpaolo.frischcheck.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 50) String name,
        @NotNull @PositiveOrZero @Max(365) Integer warningDays,
        @NotNull @PositiveOrZero @Max(365) Integer criticalDays
) {
}
