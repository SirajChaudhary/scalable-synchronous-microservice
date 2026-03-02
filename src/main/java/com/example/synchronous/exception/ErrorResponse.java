package com.example.synchronous.exception;

import java.time.LocalDateTime;
import java.util.Map;

/*
----------------------------------------------------------------------------
 Standard API Error Response
----------------------------------------------------------------------------
 - Ensures consistent error structure across the application.
----------------------------------------------------------------------------
*/
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> details
) {}