package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import com.example.Vi_vu_Viet.Util.Enums.TripSalesStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "trips",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_trips_company_trip_code",
                columnNames = {"company_id", "trip_code"}
        ),
        indexes = {
                @Index(name = "ix_trips_company_departure", columnList = "company_id, departure_at"),
                @Index(name = "ix_trips_company_sales_departure", columnList = "company_id, sales_status, departure_at")
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Trip extends BaseEntity {

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    BusCompany company_id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    Route route_id;

    String trip_code;

    LocalDateTime departure_at;

    LocalDateTime expected_arrival_at;

    LocalDateTime actual_departure_at;

    LocalDateTime actual_arrival_at;

    LocalDateTime booking_cutoff_at;

    @Enumerated(EnumType.STRING)
    TripStatus status;

    @Enumerated(EnumType.STRING)
    TripSalesStatus sales_status;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    UserAccount created_by;
}
