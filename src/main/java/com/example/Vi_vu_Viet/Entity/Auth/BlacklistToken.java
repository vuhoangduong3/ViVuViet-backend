package com.example.Vi_vu_Viet.Entity.Auth;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
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
        name = "blacklist_tokens",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_blacklist_tokens_token_hash",
                columnNames = "token_hash"
        ),
        indexes = @Index(name = "ix_blacklist_tokens_expires_at", columnList = "expires_at")
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlacklistToken extends BaseEntity {

    String token_hash;

    LocalDateTime expires_at;
}
