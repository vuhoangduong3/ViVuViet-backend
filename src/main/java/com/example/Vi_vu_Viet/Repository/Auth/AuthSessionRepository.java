package com.example.Vi_vu_Viet.Repository.Auth;

import com.example.Vi_vu_Viet.Entity.Auth.AuthSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from AuthSession session where session.refresh_token_hash = :tokenHash")
    Optional<AuthSession> findByRefreshTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from AuthSession session join fetch session.user_id where session.id = :sessionId")
    Optional<AuthSession> findByIdWithUserForUpdate(@Param("sessionId") UUID sessionId);

    @Query("select session from AuthSession session join fetch session.user_id where session.id = :sessionId")
    Optional<AuthSession> findByIdWithUser(@Param("sessionId") UUID sessionId);
}
