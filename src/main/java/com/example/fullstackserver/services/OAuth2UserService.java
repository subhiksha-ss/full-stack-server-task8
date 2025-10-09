package com.example.fullstackserver.services;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.RequiredArgsConstructor;
import com.example.fullstackserver.repository.UserRepository;
import com.example.fullstackserver.entity.User;
import com.example.fullstackserver.entity.Role;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String provider = request.getClientRegistration().getRegistrationId(); // used to get provider google/github
        final String email;

        if ("google".equals(provider)) {
            email = oAuth2User.getAttribute("email");
        } else if ("github".equals(provider)) {
            String githubEmail = oAuth2User.getAttribute("email");
            if (githubEmail == null) {
                githubEmail = oAuth2User.getAttribute("login") + "@github.com";
            }
            email = githubEmail;
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
    User newUser = new User();
    newUser.setEmail(email);
    newUser.setProvider(provider);  
    newUser.setRole(Role.USER);
    return userRepository.save(newUser);
});


if (user.getProvider() != null && !user.getProvider().equals(provider) && !"manual".equals(user.getProvider())) {
    throw new OAuth2AuthenticationException(
        "Please use your " + user.getProvider() + " account to login."
    );
}



        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("userId", user.getId());


        String nameAttributeKey = "name"; 
        if ("github".equals(provider)) {
            nameAttributeKey = "login"; 
        }

        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            nameAttributeKey
        );
    }
}
