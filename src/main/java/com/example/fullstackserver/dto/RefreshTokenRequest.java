package com.example.fullstackserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenRequest {
    private String refreshToken;
    private String accessToken;
    private String TokenType = "Bearer";
    
}
