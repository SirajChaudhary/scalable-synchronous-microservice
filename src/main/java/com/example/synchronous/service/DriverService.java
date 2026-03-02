package com.example.synchronous.service;

import com.example.synchronous.dto.DriverRequest;
import com.example.synchronous.dto.DriverResponse;

public interface DriverService {

    DriverResponse createDriver(DriverRequest request);
}