package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;
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
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "company_members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_company_members_company_user",
                columnNames = {"company_id", "user_id"}
        ),
        indexes = @Index(name = "ix_company_members_user_status", columnList = "user_id, status")
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    BusCompany company_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserAccount user_id;

    @Enumerated(EnumType.STRING)
    CompanyMemberStatus status;

    LocalDateTime joined_at;
}
