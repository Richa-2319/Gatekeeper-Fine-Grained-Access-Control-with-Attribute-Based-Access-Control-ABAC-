// Update: src/main/java/com/gatekeeper/dto/PolicyDto.java
package com.gatekeeper.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDto {

    @NotBlank(message = "Policy name is required")
    private String name;

    @NotBlank(message = "Rego rule is required")
    private String regoRule;

    private String description;

    private String resource = "*";

    private String action = "*";

    private Boolean active = true;

    @Min(value = 0, message = "Priority must be non-negative")
    @Max(value = 1000, message = "Priority must not exceed 1000")
    private Integer priority = 0;
}
