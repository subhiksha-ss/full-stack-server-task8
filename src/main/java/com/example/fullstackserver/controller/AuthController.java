package com.example.fullstackserver.controller;

import com.example.fullstackserver.entity.User;
import com.example.fullstackserver.entity.Role;
import com.example.fullstackserver.repository.UserRepository;
import com.example.fullstackserver.security.JwtUtil;
import com.example.fullstackserver.dto.ForgotPasswordRequest;
import com.example.fullstackserver.dto.LoginRequest;
import com.example.fullstackserver.dto.RegisterRequest;
import com.example.fullstackserver.dto.ResetPasswordRequest;
import com.example.fullstackserver.dto.RefreshTokenRequest;
import com.example.fullstackserver.dto.RefreshTokenResponse;
import com.example.fullstackserver.services.PasswordResetService;
import com.example.fullstackserver.services.RefreshTokenService;
import com.google.zxing.WriterException;
import com.example.fullstackserver.services.TwoFactorService;

import com.example.fullstackserver.entity.RefreshToken;
import com.example.fullstackserver.dto.LogoutRequest;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
     private TwoFactorService twoFactorService;

    @Autowired
    private PasswordEncoder passwordEncoder;



    //login
   @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
        return ResponseEntity.status(401).body("Invalid password");
    }

    if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
        return ResponseEntity.ok("2FA enabled. Please verify OTP using /auth/2fa/verify");
    }
    // login without 2fa enable
    String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId()); 

    return ResponseEntity.ok(new RefreshTokenResponse(accessToken, refreshToken.getToken(), "Bearer"));
}


    // QR code generation and 2FA enable
    @PostMapping("/2fa/enable/qrcode")
    public ResponseEntity<?> enable2FA(@RequestParam String email) throws WriterException, IOException {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
        return ResponseEntity.badRequest().body("2FA already enabled");
    }

    String secret = twoFactorService.generateSecretKey();
    user.setTwoFactorSecret(secret);
    user.setTwoFactorEnabled(true);
    userRepository.save(user);

    String otpAuthURL = String.format(
            "otpauth://totp/Auth:%s?secret=%s&issuer=Auth",
            user.getEmail(), secret
    );

    String qrBase64 = twoFactorService.generateQRCode(otpAuthURL);
    return ResponseEntity.ok(qrBase64);
}

// otp verification during login
    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verify2FA(@RequestParam String email, @RequestParam int code) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!user.getTwoFactorEnabled()) {
        return ResponseEntity.badRequest().body("2FA not enabled for this user");
    }

    boolean isValid = twoFactorService.verifyCode(user.getTwoFactorSecret(), code);
    if (!isValid) {
        return ResponseEntity.status(401).body("Invalid OTP");
    }

    String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
    return ResponseEntity.ok(new RefreshTokenResponse(accessToken, refreshToken.getToken(), "Bearer"));
}


  // disable 2fa
    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disable2FA(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return ResponseEntity.badRequest().body("2FA is not enabled for this user");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);

        return ResponseEntity.ok("2FA has been disabled successfully");
    }
   

    // register 
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {

    if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
        return ResponseEntity.badRequest().body("Email already registered");
    }

    User user = new User();
    user.setFirstName(registerRequest.getFirstName());
    user.setLastName(registerRequest.getLastName());
    user.setEmail(registerRequest.getEmail());
    user.setPassword(passwordEncoder.encode(registerRequest.getPassword())); 
    user.setDob(registerRequest.getDob());
    user.setGender(registerRequest.getGender());
    user.setPhoneNumber(registerRequest.getPhoneNumber());
    user.setRole(Role.valueOf(registerRequest.getRole().toUpperCase()));
    user.setProvider("manual");  // important for distinguishing from OAuth users

    userRepository.save(user);

    return ResponseEntity.ok("User registered successfully!");
}


    //refresh token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken, requestRefreshToken, "Bearer"));

    }
    
    // forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.createPasswordResetToken(request.getEmail());
        return ResponseEntity.ok("Password reset token sent to your email");
    }
    // reset password
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
    // The service will hash the new password internally
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successful");
}

    // logout
     @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest logoutRequest) {
    String refreshTokenStr = logoutRequest.getRefreshToken();


    RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
            .orElseThrow(() -> new RuntimeException("Refresh token not found"));


    refreshTokenService.deleteByUserId(refreshToken.getUser().getId());

    return ResponseEntity.ok("Logout successful. Refresh token deleted.");  
    }
}

