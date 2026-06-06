package org.tuvarna.smartdeliveryplatform.web.dto.admin;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSearchRequest {
    @Email(message = "Valid email is required.")
    private String searchEmail;
}
