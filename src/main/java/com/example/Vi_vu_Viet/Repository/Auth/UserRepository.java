package com.example.Vi_vu_Viet.Repository.Auth;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, UUID> {

    @Query("select account from UserAccount account " +
            "where lower(account.email) = lower(:identifier) " +
            "or account.phone = :identifier")
    Optional<UserAccount> findByLoginIdentifier(@Param("identifier") String identifier);

    @Query("select (count(account) > 0) from UserAccount account where lower(account.email) = lower(:email)")
    boolean existsByEmail(@Param("email") String email);

    @Query("select (count(account) > 0) from UserAccount account where account.phone = :phone")
    boolean existsByPhone(@Param("phone") String phone);
}
