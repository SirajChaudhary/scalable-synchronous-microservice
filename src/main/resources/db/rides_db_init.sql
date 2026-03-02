-- ============================================================
-- PostgreSQL Database Initialization Script
-- Scalable Synchronous Ride Microservice
-- ============================================================

-- ------------------------------------------------------------
-- 1. Create Database (Run Separately If Needed)
-- ------------------------------------------------------------
-- CREATE DATABASE rides_db;
-- \c rides_db;

-- ------------------------------------------------------------
-- 2. Create Drivers Table
-- ------------------------------------------------------------
CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,

    -- Driver location (for future geo-based allocation)
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,

    rating DOUBLE PRECISION NOT NULL DEFAULT 0,
    available BOOLEAN NOT NULL DEFAULT true,

    -- Optimistic Locking Support
    version BIGINT DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index to optimize availability + rating queries
CREATE INDEX idx_drivers_available_rating
ON drivers (available, rating DESC);

-- Optional index for future geo-based queries
CREATE INDEX idx_drivers_location
ON drivers (latitude, longitude);

-- ------------------------------------------------------------
-- 3. Create Rides Table
-- ------------------------------------------------------------
CREATE TABLE rides (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    fare DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- Indexes to improve read performance
CREATE INDEX idx_rides_driver_id ON rides(driver_id);
CREATE INDEX idx_rides_user_id ON rides(user_id);
CREATE INDEX idx_rides_status ON rides(status);

-- ------------------------------------------------------------
-- 4. Insert Sample Driver Data
-- ------------------------------------------------------------
INSERT INTO drivers (name, latitude, longitude, rating, available)
VALUES
('Rahul', 18.5204, 73.8567, 4.5, true),
('Amit', 19.0760, 72.8777, 4.8, true),
('Suresh', 12.9716, 77.5946, 4.2, true),
('Vikram', 28.7041, 77.1025, 4.9, true),
('Arjun', 13.0827, 80.2707, 4.7, true);

-- ------------------------------------------------------------
-- Script Completed
-- ------------------------------------------------------------
-- Database: rides_db
-- Tables: drivers, rides
-- Includes location columns (latitude, longitude)
-- Optimistic locking enabled via version column
-- Ready for ddl-auto: validate mode
-- ------------------------------------------------------------