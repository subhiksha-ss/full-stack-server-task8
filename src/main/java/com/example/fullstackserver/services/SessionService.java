package com.example.fullstackserver.services;

import com.example.fullstackserver.entity.RefreshToken;
import com.example.fullstackserver.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final RefreshTokenRepository refreshTokenRepository;

    // List all sessions
    public List<RefreshToken> listAllSessions() {
        return refreshTokenRepository.findAll();
    }

    // Delete the session (revoke by deleting)
    public void revokeSession(Long sessionId) {
        if (!refreshTokenRepository.existsById(sessionId)) {
            throw new RuntimeException("Session not found");
        }
        refreshTokenRepository.deleteById(sessionId);
    }
}

