package com.example.fullstackserver.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.example.fullstackserver.services.RateLimitService;

import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.Map;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimitService rateLimitService;

    private static final Map<String, LimitConfig> ENDPOINT_LIMITS = Map.of(
        "/me/user", new LimitConfig(5, 60_000),         
        "/me/update", new LimitConfig(5, 60_000),        
        "/me/profile-image", new LimitConfig(5, 60_000)  
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        for (Map.Entry<String, LimitConfig> entry : ENDPOINT_LIMITS.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                String userKey = getUserKey(request);  
                String finalKey = userKey + ":" + entry.getKey();  

                LimitConfig config = entry.getValue();
                boolean allowed = rateLimitService.isAllowed(finalKey, config.limit, config.window);

                if (!allowed) {
                    response.setStatus(429);
                    response.getWriter().write("Rate limit exceeded for endpoint: " + entry.getKey() +
                                               " by user: " + userKey);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getUserKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName(); 
        }
        return request.getRemoteAddr();
    }

    private static class LimitConfig {
        int limit;
        long window;
        LimitConfig(int limit, long window) {
            this.limit = limit;
            this.window = window;
        }
    }
}
