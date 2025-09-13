// PolicyRepository.java
package com.gatekeeper.repository;

import com.gatekeeper.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByActiveTrue();
    List<Policy> findByResourceAndActiveTrue(String resource);

    @Query("SELECT p FROM Policy p WHERE p.active = true ORDER BY p.priority DESC")
    List<Policy> findActivePoliciesByPriority();
}