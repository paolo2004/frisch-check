package com.codewithpaolo.frischcheck.dto;

import com.codewithpaolo.frischcheck.model.Category;

public record CategoryResponse(Long id, String name, int warningDays, int criticalDays) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(),
                category.getWarningDays(), category.getCriticalDays());
    }
}
