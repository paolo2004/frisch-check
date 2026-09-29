package com.codewithpaolo.frischcheck.model;

public enum Unit {
    STUECK("Stück"),
    KG("kg"),
    GRAMM("g"),
    LITER("l"),
    MILLILITER("ml"),
    PACKUNG("Packung"),
    FLASCHE("Flasche"),
    KISTE("Kiste");

    private final String label;

    Unit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
