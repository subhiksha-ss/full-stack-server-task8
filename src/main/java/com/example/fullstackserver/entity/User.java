package com.example.fullstackserver.entity;

import jakarta.persistence.Column;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;


import jakarta.persistence.EnumType;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    private String email;
    private String password;
    private LocalDate dob;
    private String gender;
    private String phoneNumber;

    @Enumerated(EnumType.STRING) 
    private Role role;

    private String image;
    @Column(nullable = false)
    private String provider = "manual";

    private String twoFactorSecret;
    private Boolean twoFactorEnabled = false; 

    @Column(nullable = false)
    private boolean accountNonLocked = true;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    private LocalDateTime lockTime;
   
}


