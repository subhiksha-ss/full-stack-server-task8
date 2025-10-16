package com.example.fullstackserver.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service 
public class RateLimitService {

    private final Map<String, List<Long>> requestLogs = new HashMap<>();

    public synchronized boolean isAllowed(String key, int limit, long timeWindowMillis) {
        long now = Instant.now().toEpochMilli();
        requestLogs.putIfAbsent(key, new ArrayList<>());

        
        List<Long> timestamps = requestLogs.get(key);
        timestamps.removeIf(ts -> ts < now - timeWindowMillis);

        if (timestamps.size() >= limit) {
            return false; 
        }

        timestamps.add(now);
        return true;
    }

}

