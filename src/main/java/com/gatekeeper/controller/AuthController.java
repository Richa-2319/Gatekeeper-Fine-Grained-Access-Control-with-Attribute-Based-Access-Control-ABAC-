package com.gatekeeper.controller;

import com.gatekeeper.dto.AccessRequestDto;
import com.gatekeeper.dto.LoginRequest;
import com.gatekeeper.dto.LoginResponse;
import com.gatekeeper.model.AccessDecision;
import com.gatekeeper.model.AccessRequest;
import com.gatekeeper.model.User;
import com.gatekeeper.service.AuthorizationService;
import com.gatekeeper.service.UserService;
import com.gatekeeper.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthorizationService authorizationService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            User user = userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword())
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            // Create JWT claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole());
            claims.put("department", user.getDepartment());
            claims.put("location", user.getLocation());

            // Generate token
            String token = jwtUtil.generateToken(user.getUsername(), claims);

            // Return response
            LoginResponse response = new LoginResponse(
                    token,
                    user.getUsername(),
                    user.getRole(),
                    86400000L // 24 hours
            );

            log.info("User {} logged in successfully", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Login failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Login failed", "message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "username", createdUser.getUsername()
            ));
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Registration failed", "message", e.getMessage()));
        }
    }

    @PostMapping("/authorize")
    public ResponseEntity<AccessDecision> authorize(
            @RequestBody AccessRequestDto requestDto,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        try {
            // Build access request
            AccessRequest accessRequest = new AccessRequest();
            accessRequest.setUserId(authentication.getName());
            accessRequest.setResource(requestDto.getResource());
            accessRequest.setAction(requestDto.getAction());
            accessRequest.setClientIp(getClientIp(httpRequest));
            accessRequest.setContext(requestDto.getContext() != null ? requestDto.getContext() : new HashMap<>());

            // Get user attributes
            User user = userService.getUserByUsername(authentication.getName());
            Map<String, Object> userAttrs = new HashMap<>();
            userAttrs.put("role", user.getRole());
            userAttrs.put("department", user.getDepartment());
            userAttrs.put("location", user.getLocation());
            if (user.getAttributes() != null) {
                userAttrs.putAll(user.getAttributes());
            }
            accessRequest.setUserAttributes(userAttrs);

            // Evaluate authorization
            AccessDecision decision = authorizationService.authorize(accessRequest);

            return ResponseEntity.ok(decision);

        } catch (Exception e) {
            log.error("Error during authorization request: ", e);
            AccessDecision denyDecision = new AccessDecision();
            denyDecision.setAllowed(false);
            denyDecision.setDecision("DENY");
            denyDecision.setReason("Authorization request failed: " + e.getMessage());
            return ResponseEntity.ok(denyDecision);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
