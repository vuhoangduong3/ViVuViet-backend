package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import jakarta.persistence.Column;
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

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "trip_seats",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_trip_seats_company_trip_code",
                columnNames = {"company_id", "trip_id", "seat_code"}
        ),
        indexes = @Index(name = "ix_trip_seats_company_trip", columnList = "company_id, trip_id")
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripSeat extends BaseEntity {

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

    @Column(name = "seat_code", nullable = false, length = 20)
    String seat_code;

    @Column(name = "seat_class", nullable = false, length = 30)
    String seat_class;
}
