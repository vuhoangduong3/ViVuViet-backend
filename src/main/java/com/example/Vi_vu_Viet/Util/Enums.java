package com.example.Vi_vu_Viet.Util;

public final class Enums {

    private Enums() {
    }

    public enum UserAccountStatus {
        ACTIVE,
        PENDING,
        SUSPENDED,
        CLOSED
    }

    public enum BusCompanyStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }

    public enum CompanyMemberStatus {
        INVITED,
        ACTIVE,
        SUSPENDED,
        LEFT
    }

    public enum CompanyMemberRoleType {
        COMPANY_ADMIN,
        DRIVER
    }

    public enum RouteStatus {
        ACTIVE,
        INACTIVE
    }

    public enum TripStatus {
        DRAFT,
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        TERMINATED
    }

    public enum TripSalesStatus {
        CLOSED,
        OPEN,
        PAUSED
    }

    public enum BookingStatus {
        HOLDING,
        CONFIRMED,
        CANCELLED,
        EXPIRED
    }

    public enum TicketStatus {
        HOLDING,
        CONFIRMED,
        CANCELLED,
        EXPIRED
    }

    public enum VehicleStatus {
        ACTIVE,
        MAINTENANCE,
        RETIRED
    }

    public enum DriverProfileStatus { ACTIVE, SUSPENDED, INACTIVE }
    public enum DriverAssignmentStatus { ASSIGNED, ACCEPTED, REJECTED, PREPARING, ARRIVED, COMPLETED, REPLACED }
    public enum PassengerTripStatus { WAITING_PICKUP, PICKED_UP, DROPPED_OFF, NO_SHOW }
    public enum IncidentType { VEHICLE_BREAKDOWN, ACCIDENT, TRAFFIC, PASSENGER, HEALTH, OTHER }
    public enum IncidentStatus { OPEN, IN_PROGRESS, RESOLVED, CLOSED }
}
