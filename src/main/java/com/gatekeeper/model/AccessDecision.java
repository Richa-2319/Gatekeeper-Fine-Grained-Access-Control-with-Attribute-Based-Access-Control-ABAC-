// Update: src/main/java/com/gatekeeper/model/AccessDecision.java
package com.gatekeeper.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessDecision {
    private boolean allowed;
    private String decision; // "PERMIT", "DENY"
    private String reason;
    private List<String> appliedPolicies = new ArrayList<>();
    private LocalDateTime evaluatedAt = LocalDateTime.now();
    private long evaluationTimeMs;

    // Constructor for quick deny decisions
    public AccessDecision(boolean allowed, String decision, String reason) {
        this.allowed = allowed;
        this.decision = decision;
        this.reason = reason;
        this.evaluatedAt = LocalDateTime.now();
        this.appliedPolicies = new ArrayList<>();
    }
}
