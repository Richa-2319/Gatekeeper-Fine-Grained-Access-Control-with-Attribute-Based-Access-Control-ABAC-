// AuditLog.java
package com.gatekeeper.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String resource;
    private String action;
    private String decision;
    private String reason;
    private String clientIp;
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String requestContext;

    private LocalDateTime timestamp = LocalDateTime.now();
    private long evaluationTimeMs;
}