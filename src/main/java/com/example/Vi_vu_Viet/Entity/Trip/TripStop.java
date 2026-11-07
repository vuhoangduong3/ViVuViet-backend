package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import jakarta.persistence.Entity;
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
        name = "trip_stops",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_trip_stops_company_trip_sequence",
                columnNames = {"company_id", "trip_id", "sequence_no"}
        ),
        indexes = {
                @Index(name = "ix_trip_stops_area_departure_trip", columnList = "area_id, planned_departure_at, trip_id")
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripStop extends BaseEntity {

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    BusCompany company_id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    Trip trip_id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    Stop stop_id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    Area area_id;

    Integer sequence_no;

    String name_snapshot;

    String address_snapshot;

    LocalDateTime planned_arrival_at;

    LocalDateTime planned_departure_at;

    Boolean pickup_allowed;

    Boolean dropoff_allowed;
}
