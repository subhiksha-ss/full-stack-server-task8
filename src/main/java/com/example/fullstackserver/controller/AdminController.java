package com.example.fullstackserver.controller;

import com.example.fullstackserver.dto.RegisterResponse;
import com.example.fullstackserver.entity.User;
import com.example.fullstackserver.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.example.fullstackserver.entity.RefreshToken;
import com.example.fullstackserver.services.SessionService;
import java.util.List;


@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserServices userServices;
    
    @Autowired
    private SessionService sessionService;

    public AdminController(UserServices userServices) {
        this.userServices = userServices;
    }

    // getting all users
    @GetMapping("/get")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userServices.getAllUser());
    }



    // update user role
    @PutMapping("/updateRoleById/{id}")
    public ResponseEntity<?> roleUpdate(@PathVariable Long id,@RequestBody User user) {

        try{
            User UpdateUser = userServices.updateUserRole(id, user);
            return ResponseEntity.ok(new RegisterResponse(
                UpdateUser.getFirstName(),
                UpdateUser.getLastName(),
                UpdateUser.getEmail(),
                UpdateUser.getPassword(),
                UpdateUser.getDob(),
                UpdateUser.getGender(),
                UpdateUser.getPhoneNumber(),UpdateUser.getRole().name()));

        }
        catch(RuntimeException e){
            return ResponseEntity.ok(e.getMessage());

        }
    }

    // delete user by id
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        String message = userServices.deleteUser(id);
        return ResponseEntity.ok(message);
    }

     // List all sessions -> freshToken
    @GetMapping("/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RefreshToken>> listSessions() {
        return ResponseEntity.ok(sessionService.listAllSessions());
    }

    // Revoke session by ID
    @PostMapping("/sessions/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> revokeSession(@PathVariable Long id) {
        sessionService.revokeSession(id);
        return ResponseEntity.ok("Session revoked successfully");
}

}
