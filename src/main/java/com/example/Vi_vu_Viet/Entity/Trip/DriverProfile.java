package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import com.example.Vi_vu_Viet.Util.Enums.DriverProfileStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "driver_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_driver_profiles_company_membership", columnNames = {"company_id", "membership_id"}),
        @UniqueConstraint(name = "uk_driver_profiles_company_license", columnNames = {"company_id", "license_number"})
})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DriverProfile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    BusCompany company_id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", nullable = false)
    CompanyMember membership_id;

    @Column(name = "employee_code", length = 50)
    String employee_code;

    @Column(name = "license_number", length = 50)
    String license_number;

    @Column(name = "license_class", length = 30)
    String license_class;

    @Column(name = "license_expires_on")
    LocalDate license_expires_on;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    DriverProfileStatus status = DriverProfileStatus.ACTIVE;
}
