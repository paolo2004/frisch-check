package com.codewithpaolo.frischcheck.exception;

/**
 * Versand über einen Benachrichtigungskanal ist fehlgeschlagen.
 */
public class NotificationException extends RuntimeException {

    public NotificationException(String message) {
        super(message);
    }
}
