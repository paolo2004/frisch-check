package com.codewithpaolo.frischcheck.dto;

import com.codewithpaolo.frischcheck.model.StorageLocation;
import com.codewithpaolo.frischcheck.model.Unit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Formular-Objekt für die Thymeleaf-Oberfläche.
 * Eine Klasse mit Gettern/Settern statt Record, weil th:field über JavaBean-Properties bindet.
 */
public class ProductForm {

    @NotBlank(message = "Bitte einen Namen angeben")
    @Size(max = 100)
    private String name;

    @NotNull(message = "Bitte eine Kategorie wählen")
    private Long categoryId;

    @NotNull(message = "Bitte einen Lagerort wählen")
    private StorageLocation storageLocation;

    @NotNull(message = "Bitte eine Menge angeben")
    @Positive(message = "Menge muss größer als 0 sein")
    private BigDecimal quantity;

    @NotNull(message = "Bitte eine Einheit wählen")
    private Unit unit;

    @NotNull(message = "Bitte ein Ablaufdatum angeben")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDate;

    @PastOrPresent(message = "Eingangsdatum darf nicht in der Zukunft liegen")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate receivedDate;

    public ProductRequest toRequest() {
        return new ProductRequest(name, categoryId, storageLocation, quantity, unit, expiryDate, receivedDate);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public StorageLocation getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(StorageLocation storageLocation) {
        this.storageLocation = storageLocation;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }
}
