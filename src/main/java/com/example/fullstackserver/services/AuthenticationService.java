package com.example.fullstackserver.services;
import com.example.fullstackserver.entity.RefreshToken;
import com.example.fullstackserver.entity.User;         
import com.example.fullstackserver.dto.RefreshTokenResponse;
import com.example.fullstackserver.repository.UserRepository;
import com.example.fullstackserver.security.JwtUtil;
import com.example.fullstackserver.security.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public Object login(String email, String rawPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            if (unlockWhenTimeExpired(user)) {
                // unlocked, proceed
            } else {
                return "Your account is locked. Try again later.";
            }
        }

        // Password check
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            increaseFailedAttempts(user);
            return "Invalid credentials";
        }

        // Reset failed attempts on successful login
        resetFailedAttempts(user);

        // Check 2FA
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return "2FA enabled. Please verify OTP using /auth/2fa/verify";
        }

        // Generate tokens
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new RefreshTokenResponse(accessToken, refreshToken.getToken(), "Bearer");
    }

    private void increaseFailedAttempts(User user) {
        int newFailAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newFailAttempts);

        if (newFailAttempts >= SecurityConstants.MAX_FAILED_ATTEMPTS) {
            user.setAccountNonLocked(false);
            user.setLockTime(LocalDateTime.now());
        }

        userRepository.save(user);
    }

    private void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        userRepository.save(user);
    }

    private boolean unlockWhenTimeExpired(User user) {
        LocalDateTime lockTime = user.getLockTime();
        if (lockTime == null) return true;

        LocalDateTime now = LocalDateTime.now();
        if (lockTime.plusMinutes(SecurityConstants.LOCK_TIME_DURATION).isBefore(now)) {
            user.setAccountNonLocked(true);
            user.setFailedLoginAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
