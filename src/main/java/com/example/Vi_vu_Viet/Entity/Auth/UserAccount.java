package com.example.Vi_vu_Viet.Entity.Auth;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_phone", columnNames = "phone"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAccount extends BaseEntity {

    String full_name;

    String phone;

    String email;

    String password_hash;

    @Enumerated(EnumType.STRING)
    UserAccountStatus account_status = UserAccountStatus.PENDING;

    LocalDateTime phone_verified_at;

}
