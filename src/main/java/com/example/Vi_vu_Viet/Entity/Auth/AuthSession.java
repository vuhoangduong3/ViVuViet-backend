package com.example.Vi_vu_Viet.Entity.Auth;

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
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(
        name = "auth_sessions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_auth_sessions_refresh_token_hash",
                columnNames = "refresh_token_hash"
        ),
        indexes = {
                @Index(name = "ix_auth_sessions_user_expires", columnList = "user_id, expires_at"),
                @Index(name = "ix_auth_sessions_expires", columnList = "expires_at")
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserAccount user_id;

    String refresh_token_hash;

    LocalDateTime expires_at;

    LocalDateTime revoked_at;

    String device_info;
}
