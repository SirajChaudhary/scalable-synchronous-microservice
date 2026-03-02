package com.example.synchronous.controller;

import com.example.synchronous.dto.DriverRequest;
import com.example.synchronous.dto.DriverResponse;
import com.example.synchronous.service.DriverService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Slf4j
public class DriverController {

    private final DriverService driverService;

    /*
    ----------------------------------------------------------------------------
    Create Driver API
    ----------------------------------------------------------------------------
    - Virtual Threads handle execution.
    - Validates request body.
    - Returns HTTP 201 CREATED.
    ----------------------------------------------------------------------------
    */
    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(
            @Valid @RequestBody DriverRequest request) {

        DriverResponse response = driverService.createDriver(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}