// AccessRequestDto.java
package com.gatekeeper.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AccessRequestDto {
    private String resource;
    private String action;
    private Map<String, Object> context;
}