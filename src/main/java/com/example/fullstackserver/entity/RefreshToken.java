package com.example.fullstackserver.entity;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne // one to one relation which means each user will have a separate token 
    @JoinColumn(name = "user_id", referencedColumnName = "id") // used to refer table name from user table ID 
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    
}
