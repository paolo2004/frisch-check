package com.codewithpaolo.frischcheck.exception;

/**
 * Anfrage ist formal gültig, verletzt aber eine fachliche Regel
 * (z. B. weggeworfenes Produkt erneut ändern, doppelter Kategoriename) -> HTTP 409.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
