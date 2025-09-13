package com.gatekeeper.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String role;
    private long expiresIn;

    public LoginResponse(String token, String username, String role, long expiresIn) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.expiresIn = expiresIn;
    }
}