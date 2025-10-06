
package com.example.fullstackserver.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dlmx1b0ie",
                "api_key", "226934757876616",
                "api_secret", "P9uWIV78oKi8wI9E7E-aSYUzfW8"
        ));
    }
}
