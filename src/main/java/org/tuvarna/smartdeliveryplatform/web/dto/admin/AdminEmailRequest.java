package org.tuvarna.smartdeliveryplatform.web.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEmailRequest {
    @NotBlank(message = ValidationMessages.EMAIL_REQUIRED)
    @Email(message = ValidationMessages.VALID_EMAIL_REQUIRED)
    private String email;
}
