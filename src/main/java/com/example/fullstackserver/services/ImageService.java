package com.example.fullstackserver.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.fullstackserver.entity.User;
import com.example.fullstackserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 500L * 1024 * 1024; // 500 MB

    public User uploadProfileImageByEmail(String email, MultipartFile file) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    return uploadProfileImage(user, file);
}

private User uploadProfileImage(User user, MultipartFile file) {
    try {
        if (file.isEmpty()) throw new RuntimeException("File is empty");
        if (file.getSize() > MAX_FILE_SIZE) throw new RuntimeException("File size exceeds 500 MB limit");
        if (!isValidFileType(file.getContentType())) throw new RuntimeException("Invalid file type");

        // when user already has a profile image it will automatically delete and restore this new image
        if (user.getImage() != null && !user.getImage().isEmpty()) {
            cloudinary.uploader().destroy(user.getImage(), ObjectUtils.emptyMap());
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "profile_images")
        );

        user.setImage((String) uploadResult.get("public_id"));

        return userRepository.save(user);

    } catch (IOException e) {
        throw new RuntimeException("Failed to upload profile image", e);
    }
}

    // validate file type uploaded 
    private boolean isValidFileType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("application/pdf")
        );
    }
}
