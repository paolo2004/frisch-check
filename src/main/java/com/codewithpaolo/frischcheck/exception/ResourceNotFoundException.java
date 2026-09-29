package com.codewithpaolo.frischcheck.exception;

/**
 * Angefragte Ressource existiert nicht -> HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " mit ID " + id + " wurde nicht gefunden");
    }
}
