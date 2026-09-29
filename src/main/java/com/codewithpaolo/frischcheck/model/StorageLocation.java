package com.codewithpaolo.frischcheck.model;

public enum StorageLocation {
    KUEHLSCHRANK("Kühlschrank"),
    KUEHLHAUS("Kühlhaus"),
    TIEFKUEHLER("Tiefkühler"),
    TROCKENLAGER("Trockenlager"),
    GETRAENKELAGER("Getränkelager"),
    BAR("Bar");

    private final String label;

    StorageLocation(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
