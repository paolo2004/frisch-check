package com.codewithpaolo.frischcheck.dto;

import com.codewithpaolo.frischcheck.model.StorageLocation;
import com.codewithpaolo.frischcheck.model.Unit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Eingabe zum Anlegen/Ändern eines Produkts über die REST-API.
 * receivedDate ist optional – ohne Angabe wird "heute" verwendet.
 */
public record ProductRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Long categoryId,
        @NotNull StorageLocation storageLocation,
        @NotNull @Positive BigDecimal quantity,
        @NotNull Unit unit,
        @NotNull LocalDate expiryDate,
        @PastOrPresent LocalDate receivedDate
) {
}
