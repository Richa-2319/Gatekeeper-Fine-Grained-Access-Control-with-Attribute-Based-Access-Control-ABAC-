// PolicyUpdateListener.java
package com.gatekeeper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyUpdateListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "policy-updates")
    public void handlePolicyUpdate(String message) {
        try {
            log.info("Received policy update: {}", message);

            @SuppressWarnings("unchecked")
            Map<String, Object> update = objectMapper.readValue(message, Map.class);

            String action = (String) update.get("action");
            String policyName = (String) update.get("policyName");

            // Clear relevant cache entries
            clearPolicyRelatedCache(policyName);

            log.info("Processed policy update: {} for policy: {}", action, policyName);

        } catch (Exception e) {
            log.error("Error processing policy update: ", e);
        }
    }

    private void clearPolicyRelatedCache(String policyName) {
        try {
            // Clear all access decision cache entries
            // In production, you might want to be more selective
            redisTemplate.delete(redisTemplate.keys("access:*"));

            // Clear policy cache
            redisTemplate.delete(redisTemplate.keys("policies::*"));

            log.info("Cleared cache for policy update: {}", policyName);

        } catch (Exception e) {
            log.warn("Error clearing cache: ", e);
        }
    }
}