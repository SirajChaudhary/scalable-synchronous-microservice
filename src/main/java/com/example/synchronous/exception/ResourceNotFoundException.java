package com.example.synchronous.exception;

/*
----------------------------------------------------------------------------
 ResourceNotFoundException
----------------------------------------------------------------------------
 - Thrown when a requested resource does not exist.
 - Used in the service layer.
 - Translated to HTTP 404 by GlobalExceptionHandler.
----------------------------------------------------------------------------
*/
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource,
                                     String field,
                                     Object value) {
        super(String.format("%s not found with %s : '%s'",
                resource, field, value));
    }
}