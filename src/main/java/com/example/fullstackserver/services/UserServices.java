package com.example.fullstackserver.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
// import com.example.fullstackserver.services.CloudinaryService;
import com.example.fullstackserver.repository.UserRepository;
import com.example.fullstackserver.dto.RegisterRequest;
import com.example.fullstackserver.entity.User;


import java.util.List;

@Service
public class UserServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword())); 
        user.setDob(registerRequest.getDob());
        user.setGender(registerRequest.getGender());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setProvider("manual"); 
        userRepository.save(user);
    }

    // get all user
    public List<User> getAllUser(){
        return userRepository.findAll();       
    }

    // get user by using id

    public User getUserById(Long id){
        return userRepository.findById(id).orElseThrow(() 
            -> new RuntimeException("User not found")); 
    }

    // finding user by using email
    public User getUserByEmail(String Email){
        return userRepository.findByEmail(Email).orElseThrow(() 
            -> new RuntimeException("User not found"));

    }

    // update user by using id

    public User updateUser(Long id,User user){
        User existingUser = userRepository.findById(id).orElseThrow(
            () -> new RuntimeException("User not found")
        );
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
        existingUser.setDob(user.getDob());
        existingUser.setGender(user.getGender());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setRole(user.getRole());
        existingUser.setImage(user.getImage());
        return userRepository.save(existingUser);
    }


    // deletion of user using id

    public String deleteUser(Long id){
        User existingUser = userRepository.findById(id).orElseThrow(
            () -> new RuntimeException("User not found")
        );
        userRepository.delete(existingUser);
        return "User deleted successfully";
    }

    // update user role

    public User updateUserRole(Long id, User user){
    User existingUser = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));

    existingUser.setRole(user.getRole());  //upadte only role
    return userRepository.save(existingUser);
}

    

}