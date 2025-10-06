package com.example.fullstackserver.controller;

import com.example.fullstackserver.entity.User;
import com.example.fullstackserver.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.fullstackserver.services.ImageService;



@RestController
@RequestMapping("/me")
public class UserController {

    @Autowired
    private UserServices userServices;
    @Autowired
    private ImageService imageService;

    // get user profile
    @GetMapping("/user")
    public ResponseEntity<User> getMyProfile(Authentication authentication) {
        String email = authentication.getName(); 
        User user = userServices.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    // update user profile
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

    // Upload profile image
    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            User updatedUser = imageService.uploadProfileImageByEmail(email, file);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

    
