// AuditLogRepository.java
package com.gatekeeper.repository;

import com.gatekeeper.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserIdAndTimestampBetween(String userId, LocalDateTime start, LocalDateTime end);
    List<AuditLog> findByResourceAndTimestampBetween(String resource, LocalDateTime start, LocalDateTime end);
}