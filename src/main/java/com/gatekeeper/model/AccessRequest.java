// AccessRequest.java
package com.gatekeeper.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AccessRequest {
    private String userId;
    private String resource;
    private String action;
    private String clientIp;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Map<String, Object> context;
    private Map<String, Object> userAttributes;
    private Map<String, Object> resourceAttributes;
}
