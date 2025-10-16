package com.example.fullstackserver.services;

import com.google.zxing.BarcodeFormat;
//import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.stereotype.Service;

import com.example.fullstackserver.entity.User;
import com.example.fullstackserver.repository.UserRepository;
import com.example.fullstackserver.security.JwtUtil;
import com.example.fullstackserver.dto.RefreshTokenResponse;
//import com.example.fullstackserver.services.RefreshTokenService;
import com.example.fullstackserver.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import java.io.ByteArrayOutputStream;
//import java.io.IOException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    private final com.warrenstrange.googleauth.GoogleAuthenticator gAuth = 
            new com.warrenstrange.googleauth.GoogleAuthenticator();

    public String enable2FA(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new RuntimeException("2FA already enabled");
        }

        String secret = gAuth.createCredentials().getKey();
        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        String otpAuthURL = String.format(
                "otpauth://totp/Auth:%s?secret=%s&issuer=Auth",
                user.getEmail(), secret
        );

        return generateQRCode(otpAuthURL); 
    }

    public Object verify2FA(String email, int code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new RuntimeException("2FA not enabled for this user");
        }

        boolean isValid = gAuth.authorize(user.getTwoFactorSecret(), code);
        if (!isValid) {
            throw new RuntimeException("Invalid OTP");
        }

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new RefreshTokenResponse(accessToken, refreshToken.getToken(), "Bearer");
    }

    public String disable2FA(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new RuntimeException("2FA is not enabled for this user");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);

        return "2FA has been disabled successfully";
    }

    private String generateQRCode(String otpAuthURL) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            var bitMatrix = qrCodeWriter.encode(otpAuthURL, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            return Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}
