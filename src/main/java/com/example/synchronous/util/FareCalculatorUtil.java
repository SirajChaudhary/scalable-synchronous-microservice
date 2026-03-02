package com.example.synchronous.util;

/*
 Utility class
 Static Factory / Utility Pattern
*/
public final class FareCalculatorUtil {

    private FareCalculatorUtil() {}

    public static double calculateFare(Double lat, Double lng) {
        return 100 + (lat + lng) * 0.5;
    }
}