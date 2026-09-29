package com.codewithpaolo.frischcheck.model;

/**
 * Dringlichkeit eines Produkts. Wird aus Ablaufdatum + Kategorie-Schwellen berechnet, nicht gespeichert.
 */
public enum ExpiryLevel {
    ABGELAUFEN("Abgelaufen"),
    KRITISCH("Kritisch"),
    WARNUNG("Warnung"),
    OK("OK");

    private final String label;

    ExpiryLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean needsAttention() {
        return this != OK;
    }
}
