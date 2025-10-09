package com.example.fullstackserver.services;

import com.example.fullstackserver.entity.RefreshToken;
import com.example.fullstackserver.entity.User;
import com.example.fullstackserver.repository.RefreshTokenRepository;
import com.example.fullstackserver.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private final long accessTokenExpiry = 15 * 60 * 1000;       
    private final long refreshTokenExpiry = 7 * 24 * 60 * 60 * 1000; 
    
    // creating refresh token and savint it to database 
    public RefreshToken createRefreshToken(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    refreshTokenRepository.deleteByUserId(userId);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setToken(UUID.randomUUID().toString());
    refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiry));
    refreshToken.setRevoked(false);

    return refreshTokenRepository.save(refreshToken);
}


    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional    // ensure change are applied to the database
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token); 
            throw new RuntimeException("Refresh token was expired. Please login again.");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public long getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    public long getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }
}
