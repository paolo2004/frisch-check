package com.codewithpaolo.frischcheck.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Produktkategorie mit eigenen Warnschwellen.
 * Beispiel Frischware: warningDays = 5, criticalDays = 2.
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /** Ab so vielen Tagen vor Ablauf gilt ein Produkt als WARNUNG. */
    @Column(nullable = false)
    private int warningDays;

    /** Ab so vielen Tagen vor Ablauf gilt ein Produkt als KRITISCH (muss &lt;= warningDays sein). */
    @Column(nullable = false)
    private int criticalDays;

    protected Category() {
        // für JPA
    }

    public Category(String name, int warningDays, int criticalDays) {
        this.name = name;
        this.warningDays = warningDays;
        this.criticalDays = criticalDays;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWarningDays() {
        return warningDays;
    }

    public void setWarningDays(int warningDays) {
        this.warningDays = warningDays;
    }

    public int getCriticalDays() {
        return criticalDays;
    }

    public void setCriticalDays(int criticalDays) {
        this.criticalDays = criticalDays;
    }
}
