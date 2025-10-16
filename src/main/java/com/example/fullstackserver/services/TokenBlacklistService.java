package com.example.fullstackserver.services;

import com.example.fullstackserver.entity.BlacklistedToken;
import com.example.fullstackserver.repository.BlacklistedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class TokenBlacklistService {

    @Autowired
    private BlacklistedTokenRepository repository;

    public void blacklistToken(String token, LocalDateTime expiry) {
        BlacklistedToken blacklisted = new BlacklistedToken();
        blacklisted.setToken(token);
        blacklisted.setExpiryDate(expiry);
        repository.save(blacklisted);
    }

    public boolean isTokenBlacklisted(String token) {
        return repository.findByToken(token).isPresent();
    }
}
