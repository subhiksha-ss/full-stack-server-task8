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
import com.example.fullstackserver.entity.RefreshToken;
import com.example.fullstackserver.dto.LogoutRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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


    

    // login 
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

    if (!user.getPassword().equals(loginRequest.getPassword())) {
        return ResponseEntity.status(401).body("Invalid password");
    }

    // Generate access token
    String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

    // Generate refresh token
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

    // Return both tokens
    return ResponseEntity.ok(new RefreshTokenResponse(accessToken, refreshToken.getToken(),"Bearer"));
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
    user.setPassword(registerRequest.getPassword()); 
    user.setDob(registerRequest.getDob());
    user.setGender(registerRequest.getGender());
    user.setPhoneNumber(registerRequest.getPhoneNumber());
    user.setRole(Role.valueOf(registerRequest.getRole().toUpperCase()));

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

