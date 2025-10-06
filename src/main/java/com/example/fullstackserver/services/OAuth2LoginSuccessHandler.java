package com.example.fullstackserver.services;

import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import com.example.fullstackserver.security.JwtUtil;
import com.example.fullstackserver.repository.UserRepository;
import com.example.fullstackserver.entity.User;
import com.example.fullstackserver.entity.RefreshToken;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        Long userId = (Long) oauthUser.getAttribute("userId"); 

        User user = userRepository.findById(userId)
                      .orElseThrow(() -> new RuntimeException("User not found after OAuth login"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        response.setContentType("application/json");
        response.getWriter().write(
            String.format("{\"accessToken\":\"%s\",\"refreshToken\":\"%s\"}", token, refreshToken.getToken())
        );
    }
}
