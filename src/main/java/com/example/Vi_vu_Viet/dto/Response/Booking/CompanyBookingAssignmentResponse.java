package com.example.Vi_vu_Viet.dto.Response.Booking;

import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;

import java.util.UUID;

public record CompanyBookingAssignmentResponse(UUID driverId, String driverName, String driverPhone,
                                                UUID vehicleId, String plateNumber, String vehicleModel,
                                                Integer passengerCapacity, DriverAssignmentStatus status) { }
