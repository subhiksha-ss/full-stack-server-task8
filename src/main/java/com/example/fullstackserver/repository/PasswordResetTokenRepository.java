package com.example.fullstackserver.repository;

import java.util.Optional;

import com.example.fullstackserver.entity.PasswordResetToken;
import com.example.fullstackserver.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
    
}
