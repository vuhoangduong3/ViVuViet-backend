package com.example.Vi_vu_Viet.Repository.Auth;

import com.example.Vi_vu_Viet.Entity.Auth.BlacklistToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface BlacklistTokenRepository extends JpaRepository<BlacklistToken, UUID> {

    @Query("select (count(token) > 0) from BlacklistToken token where token.token_hash = :tokenHash")
    boolean isBlacklisted(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("delete from BlacklistToken token where token.expires_at <= :currentTime")
    int deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);
}
