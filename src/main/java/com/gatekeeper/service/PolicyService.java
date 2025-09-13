// PolicyService.java
package com.gatekeeper.service;

import com.gatekeeper.model.Policy;
import com.gatekeeper.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Cacheable(value = "policies", key = "#resource + ':' + #action")
    public List<Policy> getApplicablePolicies(String resource, String action) {
        log.debug("Fetching applicable policies for resource: {} and action: {}", resource, action);

        // Get all active policies, prioritized
        List<Policy> allPolicies = policyRepository.findActivePoliciesByPriority();

        // Filter by resource and action (can be enhanced with pattern matching)
        return allPolicies.stream()
                .filter(policy -> isApplicable(policy, resource, action))
                .toList();
    }

    private boolean isApplicable(Policy policy, String resource, String action) {
        // Simple matching - can be enhanced with regex or wildcards
        return (policy.getResource() == null || policy.getResource().equals("*") || policy.getResource().equals(resource)) &&
                (policy.getAction() == null || policy.getAction().equals("*") || policy.getAction().equals(action));
    }

    public Policy createPolicy(Policy policy) {
        Policy savedPolicy = policyRepository.save(policy);

        // Publish policy update to Kafka
        publishPolicyUpdate("CREATE", savedPolicy);

        // Clear cache
        clearPolicyCache();

        return savedPolicy;
    }

    public Policy updatePolicy(Long id, Policy policyUpdate) {
        Optional<Policy> existing = policyRepository.findById(id);
        if (existing.isPresent()) {
            Policy policy = existing.get();
            policy.setName(policyUpdate.getName());
            policy.setRegoRule(policyUpdate.getRegoRule());
            policy.setDescription(policyUpdate.getDescription());
            policy.setResource(policyUpdate.getResource());
            policy.setAction(policyUpdate.getAction());
            policy.setActive(policyUpdate.isActive());
            policy.setPriority(policyUpdate.getPriority());

            Policy saved = policyRepository.save(policy);

            // Publish policy update
            publishPolicyUpdate("UPDATE", saved);

            // Clear cache
            clearPolicyCache();

            return saved;
        }
        throw new RuntimeException("Policy not found");
    }

    public void deletePolicy(Long id) {
        Optional<Policy> policy = policyRepository.findById(id);
        if (policy.isPresent()) {
            policyRepository.deleteById(id);

            // Publish policy update
            publishPolicyUpdate("DELETE", policy.get());

            // Clear cache
            clearPolicyCache();
        }
    }

    private void publishPolicyUpdate(String action, Policy policy) {
        try {
            String message = String.format("{\"action\":\"%s\",\"policyId\":%d,\"policyName\":\"%s\"}",
                    action, policy.getId(), policy.getName());
            kafkaTemplate.send("policy-updates", message);
            log.info("Published policy update: {} for policy: {}", action, policy.getName());
        } catch (Exception e) {
            log.error("Error publishing policy update: ", e);
        }
    }

    @CacheEvict(value = "policies", allEntries = true)
    public void clearPolicyCache() {
        log.info("Policy cache cleared");
    }

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    public Optional<Policy> getPolicy(Long id) {
        return policyRepository.findById(id);
    }
}