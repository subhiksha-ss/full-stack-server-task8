package com.example.fullstackserver.dto;

import lombok.Setter;
import lombok.Getter;

@Getter
@Setter 

public class LoginRequest {

    private String email;
    private String password;
    
}
