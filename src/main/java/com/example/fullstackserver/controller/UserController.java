package com.example.fullstackserver.controller;

import com.example.fullstackserver.entity.User;
import com.example.fullstackserver.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;



@RestController
@RequestMapping("/me")
public class UserController {

    @Autowired
    private UserServices userServices;

    @GetMapping("/user")
    public ResponseEntity<User> getMyProfile(Authentication authentication) {
        String email = authentication.getName(); 
        User user = userServices.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateMyProfile(Authentication authentication, @RequestBody User updatedUser) {
        String email = authentication.getName();
        User existingUser = userServices.getUserByEmail(email);

   
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setDob(updatedUser.getDob());
        existingUser.setGender(updatedUser.getGender());
        existingUser.setImage(updatedUser.getImage());

        User savedUser = userServices.updateUser(existingUser.getId(), existingUser);
        return ResponseEntity.ok(savedUser);
    }
}