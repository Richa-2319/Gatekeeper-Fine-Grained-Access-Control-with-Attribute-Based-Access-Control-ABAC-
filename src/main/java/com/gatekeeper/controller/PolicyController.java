package com.gatekeeper.controller;

import com.gatekeeper.dto.PolicyDto;
import com.gatekeeper.model.Policy;
import com.gatekeeper.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Slf4j
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    public ResponseEntity<List<Policy>> getAllPolicies(Authentication authentication) {
        // For now, allow any authenticated user to view policies
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Policy> getPolicy(@PathVariable Long id) {
        return policyService.getPolicy(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createPolicy(@Valid @RequestBody PolicyDto policyDto,
                                          Authentication authentication) {
        try {
            // Check if user is admin
            if (!isAdmin(authentication)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Access denied", "message", "Admin role required"));
            }

            Policy policy = convertToEntity(policyDto);
            Policy created = policyService.createPolicy(policy);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Policy creation failed: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Policy creation failed", "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePolicy(@PathVariable Long id,
                                          @Valid @RequestBody PolicyDto policyDto,
                                          Authentication authentication) {
        try {
            if (!isAdmin(authentication)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Access denied", "message", "Admin role required"));
            }

            Policy policy = convertToEntity(policyDto);
            Policy updated = policyService.updatePolicy(id, policy);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Policy update failed: ", e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Policy update failed: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Policy update failed", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePolicy(@PathVariable Long id, Authentication authentication) {
        try {
            if (!isAdmin(authentication)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Access denied", "message", "Admin role required"));
            }

            policyService.deletePolicy(id);
            return ResponseEntity.ok(Map.of("message", "Policy deleted successfully"));
        } catch (Exception e) {
            log.error("Policy deletion failed: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Policy deletion failed", "message", e.getMessage()));
        }
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<?> clearCache(Authentication authentication) {
        try {
            if (!isAdmin(authentication)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Access denied", "message", "Admin role required"));
            }

            policyService.clearPolicyCache();
            return ResponseEntity.ok(Map.of("message", "Policy cache cleared successfully"));
        } catch (Exception e) {
            log.error("Cache clear failed: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cache clear failed", "message", e.getMessage()));
        }
    }

    // Helper method to convert DTO to Entity
    private Policy convertToEntity(PolicyDto dto) {
        Policy policy = new Policy();
        policy.setName(dto.getName());
        policy.setRegoRule(dto.getRegoRule());
        policy.setDescription(dto.getDescription());
        policy.setResource(dto.getResource() != null ? dto.getResource() : "*");
        policy.setAction(dto.getAction() != null ? dto.getAction() : "*");
        policy.setActive(dto.getActive() != null ? dto.getActive() : true);
        policy.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        return policy;
    }

    // Helper method to check if user is admin
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority ->
                        authority.equals("ROLE_admin") ||
                                authority.equals("admin") ||
                                authority.equals("ADMIN")
                );
    }
}
