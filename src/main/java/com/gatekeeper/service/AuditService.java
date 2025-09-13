// Update: src/main/java/com/gatekeeper/service/AuditService.java
package com.gatekeeper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatekeeper.model.AccessDecision;
import com.gatekeeper.model.AccessRequest;
import com.gatekeeper.model.AuditLog;
import com.gatekeeper.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    public void logAccess(AccessRequest request, AccessDecision decision) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(request.getUserId());
            auditLog.setResource(request.getResource());
            auditLog.setAction(request.getAction());
            auditLog.setDecision(decision.getDecision());
            auditLog.setReason(decision.getReason());
            auditLog.setClientIp(request.getClientIp());
            auditLog.setTimestamp(request.getTimestamp());
            auditLog.setEvaluationTimeMs(decision.getEvaluationTimeMs());

            // Serialize request context
            try {
                auditLog.setRequestContext(objectMapper.writeValueAsString(request.getContext()));
            } catch (JsonProcessingException e) {
                log.warn("Error serializing request context: ", e);
                auditLog.setRequestContext("{}");
            }

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved for user: {} accessing resource: {}",
                    request.getUserId(), request.getResource());

        } catch (Exception e) {
            log.error("Error saving audit log: ", e);
        }
    }
}