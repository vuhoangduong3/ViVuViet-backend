package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "bus_companies",
        uniqueConstraints = @UniqueConstraint(name = "uk_bus_companies_slug", columnNames = "slug")
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BusCompany extends BaseEntity {

    String name;

    String slug;

    String hotline;

    String timezone;

    @Enumerated(EnumType.STRING)
    BusCompanyStatus status;
}
