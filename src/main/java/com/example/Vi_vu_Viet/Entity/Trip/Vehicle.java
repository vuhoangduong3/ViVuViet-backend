package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import com.example.Vi_vu_Viet.Util.Enums.VehicleStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "vehicles", uniqueConstraints = @UniqueConstraint(
        name = "uk_vehicles_company_plate", columnNames = {"company_id", "plate_normalized"}))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vehicle extends BaseEntity {

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    BusCompany company_id;

    @Column(name = "plate_normalized", nullable = false, length = 20)
    String plate_normalized;

    @Column(name = "vehicle_model", nullable = false, length = 120)
    String vehicle_model;

    @Column(name = "passenger_capacity", nullable = false)
    Integer passenger_capacity;

    @Column(name = "inspection_expires_on")
    LocalDate inspection_expires_on;

    @Column(length = 120)
    String vehicle_type;
    Integer manufacturing_year;
    @Column(length = 80)
    String fuel_type;
    @Column(length = 80)
    String color;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_layout_id")
    SeatLayout seat_layout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    VehicleStatus status;
}
