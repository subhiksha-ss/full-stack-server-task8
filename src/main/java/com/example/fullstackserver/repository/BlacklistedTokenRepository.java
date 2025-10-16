package com.example.fullstackserver.repository;

import com.example.fullstackserver.entity.BlacklistedToken;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    Optional<BlacklistedToken> findByToken(String token);
    void deleteByExpiryDateBefore(java.time.LocalDateTime now);
     
}
