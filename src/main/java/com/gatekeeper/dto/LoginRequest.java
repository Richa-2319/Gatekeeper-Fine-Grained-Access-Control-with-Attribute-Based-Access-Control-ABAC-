// Create: src/main/java/com/gatekeeper/dto/LoginRequest.java
package com.gatekeeper.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}