package com.codewithpaolo.frischcheck.dto;

import com.codewithpaolo.frischcheck.model.ExpiryLevel;
import com.codewithpaolo.frischcheck.model.Product;
import com.codewithpaolo.frischcheck.model.ProductStatus;
import com.codewithpaolo.frischcheck.model.StorageLocation;
import com.codewithpaolo.frischcheck.model.Unit;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductResponse(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        StorageLocation storageLocation,
        BigDecimal quantity,
        Unit unit,
        LocalDate expiryDate,
        LocalDate receivedDate,
        ProductStatus status,
        long daysUntilExpiry,
        ExpiryLevel expiryLevel,
        String expiryText
) {

    public static ProductResponse from(Product product, long daysUntilExpiry, ExpiryLevel expiryLevel,
                                       String expiryText) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getStorageLocation(),
                product.getQuantity(),
                product.getUnit(),
                product.getExpiryDate(),
                product.getReceivedDate(),
                product.getStatus(),
                daysUntilExpiry,
                expiryLevel,
                expiryText
        );
    }
}
