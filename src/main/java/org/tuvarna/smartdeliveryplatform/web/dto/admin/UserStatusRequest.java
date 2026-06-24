package org.tuvarna.smartdeliveryplatform.web.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusRequest {
    @NotBlank(message = ValidationMessages.EMAIL_REQUIRED)
    @Email(message = ValidationMessages.VALID_EMAIL_REQUIRED)
    private String email;

    @NotNull(message = ValidationMessages.STATUS_REQUIRED)
    private UserStatus status;
}
