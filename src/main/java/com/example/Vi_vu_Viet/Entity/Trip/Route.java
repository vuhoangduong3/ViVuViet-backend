package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import com.example.Vi_vu_Viet.Util.Enums.RouteStatus;
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

import java.math.BigDecimal;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "routes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_routes_company_code",
                columnNames = {"company_id", "code"}
        ),
        indexes = {
                @Index(name = "ix_routes_company_status", columnList = "company_id, status"),
                @Index(name = "ix_routes_company_origin_destination", columnList = "company_id, origin_area_id, destination_area_id")
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Route extends BaseEntity {

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    BusCompany company_id;

    String code;

    String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_area_id", nullable = false)
    Area origin_area_id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_area_id", nullable = false)
    Area destination_area_id;

    BigDecimal distance_km;

    Integer duration_minutes;

    Long base_fare_amount;

    @Enumerated(EnumType.STRING)
    RouteStatus status;
}
