package com.example.fullstackserver.controller;

import com.example.fullstackserver.entity.User;
//import com.example.fullstackserver.entity.Role;
//import com.example.fullstackserver.repository.UserRepository;
import com.example.fullstackserver.security.JwtUtil;
import com.example.fullstackserver.dto.ForgotPasswordRequest;
import com.example.fullstackserver.dto.LoginRequest;
import com.example.fullstackserver.dto.RegisterRequest;
import com.example.fullstackserver.dto.ResetPasswordRequest;
import com.example.fullstackserver.dto.RefreshTokenRequest;
import com.example.fullstackserver.dto.RefreshTokenResponse;
import com.example.fullstackserver.services.AuthenticationService;
import com.example.fullstackserver.services.PasswordResetService;
import com.example.fullstackserver.services.RefreshTokenService;
//import com.google.zxing.WriterException;
import com.example.fullstackserver.services.TwoFactorService;
import com.example.fullstackserver.services.UserServices;
import com.example.fullstackserver.entity.RefreshToken;
import com.example.fullstackserver.dto.LogoutRequest;
//import java.io.IOException;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserServices userServices;


    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
     private TwoFactorService twoFactorService;

    @Autowired
    private AuthenticationService authenticationService;
  

    // Login 
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Object response = authenticationService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        if (response instanceof String) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<?> enable2FA(@RequestParam String email) {
        return ResponseEntity.ok(twoFactorService.enable2FA(email));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verify2FA(@RequestParam String email, @RequestParam int code) {
        return ResponseEntity.ok(twoFactorService.verify2FA(email, code));
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disable2FA(@RequestParam String email) {
        return ResponseEntity.ok(twoFactorService.disable2FA(email));
    }
   
    // Register
   @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        userServices.registerUser(registerRequest);
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

