package org.tuvarna.smartdeliveryplatform.web.dto.admin;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSearchRequest {
    @Email(message = ValidationMessages.VALID_EMAIL_REQUIRED)
    private String searchEmail;
}
