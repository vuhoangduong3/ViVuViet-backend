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

    public enum StopStatus {
        ACTIVE,
        INACTIVE
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
}
