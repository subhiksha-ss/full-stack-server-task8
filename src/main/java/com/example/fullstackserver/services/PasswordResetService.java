package com.example.fullstackserver.services;

import com.example.fullstackserver.entity.PasswordResetToken;
import com.example.fullstackserver.entity.User;
import com.example.fullstackserver.repository.PasswordResetTokenRepository;
import com.example.fullstackserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Transactional
    public void createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete old token if exists
        tokenRepository.deleteByUser(user);

        // Generate token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600)) // 1 hour expiry
                .build();

        tokenRepository.save(resetToken);

        // Create reset link for frontend
        String frontendUrl = "http://localhost:3000/reset-password";
        String resetLink = frontendUrl + "?token=" + token;

       String html = "<p>Click the link below to reset your password:</p>"
        + "<a href=\"" + resetLink + "\">" + resetLink + "</a>"
        + "<p>This link expires in 1 hour.</p>";

        // Send email using EmailService
        emailService.sendHtmlEmail(
                user.getEmail(),
                "Password Reset Request",
                html
        );
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(newPassword); // no encoder if already configured elsewhere
        userRepository.save(user);

        tokenRepository.delete(resetToken); 
    }
}
