package com.codewithpaolo.frischcheck.dto;

import com.codewithpaolo.frischcheck.model.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull ProductStatus status) {
}
