package com.gatekeeper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatekeeper.model.AccessDecision;
import com.gatekeeper.model.AccessRequest;
import com.gatekeeper.model.Policy;
import com.gatekeeper.util.PolicyEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final PolicyService policyService;
    private final AuditService auditService;
    private final PolicyEvaluator policyEvaluator;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public AccessDecision authorize(AccessRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // Check cache first
            String cacheKey = generateCacheKey(request);
            AccessDecision cachedDecision = getCachedDecision(cacheKey);
            if (cachedDecision != null) {
                log.debug("Cache hit for access request: {}", cacheKey);
                return cachedDecision;
            }

            // Evaluate policies
            AccessDecision decision = evaluateAccess(request);
            decision.setEvaluationTimeMs(System.currentTimeMillis() - startTime);

            // Cache the decision
            cacheDecision(cacheKey, decision);

            // Audit the request
            auditService.logAccess(request, decision);

            return decision;

        } catch (Exception e) {
            log.error("Error during authorization: ", e);
            AccessDecision denyDecision = new AccessDecision();
            denyDecision.setAllowed(false);
            denyDecision.setDecision("DENY");
            denyDecision.setReason("Authorization service error: " + e.getMessage());
            denyDecision.setEvaluationTimeMs(System.currentTimeMillis() - startTime);

            auditService.logAccess(request, denyDecision);
            return denyDecision;
        }
    }

    private AccessDecision evaluateAccess(AccessRequest request) {
        List<Policy> applicablePolicies = policyService.getApplicablePolicies(
                request.getResource(), request.getAction());

        List<String> appliedPolicies = new ArrayList<>();
        boolean hasPermit = false;
        boolean hasDeny = false;
        String denyReason = null;

        // Evaluate each policy
        for (Policy policy : applicablePolicies) {
            try {
                boolean result = policyEvaluator.evaluate(policy, request);
                appliedPolicies.add(policy.getName());

                if (result) {
                    if (policy.getName().toLowerCase().contains("permit") ||
                            policy.getName().toLowerCase().contains("allow") ||
                            policy.getName().toLowerCase().contains("access")) {
                        hasPermit = true;
                    } else if (policy.getName().toLowerCase().contains("deny") ||
                            policy.getName().toLowerCase().contains("block")) {
                        hasDeny = true;
                        denyReason = "Denied by policy: " + policy.getName();
                        break; // Deny takes precedence
                    } else {
                        // Default behavior for policies that evaluate to true
                        hasPermit = true;
                    }
                }
            } catch (Exception e) {
                log.warn("Error evaluating policy {}: {}", policy.getName(), e.getMessage());
            }
        }

        AccessDecision decision = new AccessDecision();
        decision.setEvaluatedAt(LocalDateTime.now());

        if (hasDeny) {
            decision.setAllowed(false);
            decision.setDecision("DENY");
            decision.setReason(denyReason);
        } else if (hasPermit) {
            decision.setAllowed(true);
            decision.setDecision("PERMIT");
            decision.setReason("Access granted by applicable policies");
        } else {
            decision.setAllowed(false);
            decision.setDecision("DENY");
            decision.setReason("No applicable permit policies found");
        }

        decision.setAppliedPolicies(appliedPolicies);
        return decision;
    }

    private String generateCacheKey(AccessRequest request) {
        return String.format("access:%s:%s:%s:%s",
                request.getUserId(),
                request.getResource(),
                request.getAction(),
                Objects.hash(request.getContext(), request.getUserAttributes())
        );
    }

    private AccessDecision getCachedDecision(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.convertValue(cached, AccessDecision.class);
            }
        } catch (Exception e) {
            log.warn("Error reading from cache: ", e);
        }
        return null;
    }

    private void cacheDecision(String cacheKey, AccessDecision decision) {
        try {
            // Cache for 5 minutes
            redisTemplate.opsForValue().set(cacheKey, decision, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Error writing to cache: ", e);
        }
    }
}
