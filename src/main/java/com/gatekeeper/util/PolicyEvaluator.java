// Update: src/main/java/com/gatekeeper/util/PolicyEvaluator.java
package com.gatekeeper.util;

import com.gatekeeper.model.AccessRequest;
import com.gatekeeper.model.Policy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class PolicyEvaluator {

    @Value("${gatekeeper.opa.enabled:false}")
    private boolean opaEnabled;

    @Value("${gatekeeper.opa.url:http://localhost:8181}")
    private String opaUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean evaluate(Policy policy, AccessRequest request) {
        if (opaEnabled) {
            return evaluateWithOPA(policy, request);
        } else {
            return evaluateWithCustomEngine(policy, request);
        }
    }

    private boolean evaluateWithOPA(Policy policy, AccessRequest request) {
        try {
            Map<String, Object> input = buildOPAInput(request);

            // Call OPA API
            String opaEndpoint = opaUrl + "/v1/data/gatekeeper/authz/allow";
            Map<String, Object> payload = Map.of("input", input);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(opaEndpoint, payload, Map.class);

            if (response != null && response.containsKey("result")) {
                return Boolean.TRUE.equals(response.get("result"));
            }

            return false;

        } catch (Exception e) {
            log.error("Error evaluating policy {} with OPA: ", policy.getName(), e);
            return false;
        }
    }

    private boolean evaluateWithCustomEngine(Policy policy, AccessRequest request) {
        try {
            String rule = policy.getRegoRule().toLowerCase();
            Map<String, Object> userAttrs = request.getUserAttributes();

            log.debug("Evaluating policy: {} with rule: {}", policy.getName(), rule);

            // Admin access - always allow
            if (rule.contains("admin") && "admin".equals(userAttrs.get("role"))) {
                log.debug("Admin access granted for policy: {}", policy.getName());
                return true;
            }

            // Business hours check
            if (rule.contains("business_hours")) {
                if (!isBusinessHours()) {
                    log.debug("Access denied - outside business hours for policy: {}", policy.getName());
                    return false;
                }
            }

            // Location-based rules
            if (rule.contains("office_location")) {
                String userLocation = (String) userAttrs.get("location");
                if (!"office".equals(userLocation)) {
                    log.debug("Access denied - not in office location for policy: {}", policy.getName());
                    return false;
                }
            }

            // Sensitive resource check
            if (rule.contains("sensitive") || request.getResource().contains("sensitive")) {
                String userRole = (String) userAttrs.get("role");
                if (!"admin".equals(userRole)) {
                    log.debug("Access denied - insufficient privileges for sensitive resource");
                    return false;
                }
            }

            // Department-based access
            if (rule.contains("department")) {
                String userDept = (String) userAttrs.get("department");
                String contextDept = (String) request.getContext().get("department");

                if (contextDept != null && !contextDept.equals(userDept)) {
                    log.debug("Access denied - department mismatch: user={}, required={}", userDept, contextDept);
                    return false;
                }
            }

            // Default allow for basic rules
            log.debug("Access granted for policy: {}", policy.getName());
            return true;

        } catch (Exception e) {
            log.error("Error evaluating policy {} with custom engine: ", policy.getName(), e);
            return false;
        }
    }

    private boolean isBusinessHours() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);
        return !now.isBefore(start) && !now.isAfter(end);
    }

    private Map<String, Object> buildOPAInput(AccessRequest request) {
        Map<String, Object> input = new HashMap<>();
        input.put("user", request.getUserAttributes());
        input.put("resource", request.getResource());
        input.put("action", request.getAction());
        input.put("timestamp", request.getTimestamp().toString());
        input.put("context", request.getContext());
        return input;
    }
}