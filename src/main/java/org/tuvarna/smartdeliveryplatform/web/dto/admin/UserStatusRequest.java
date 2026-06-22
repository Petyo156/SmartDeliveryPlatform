package org.tuvarna.smartdeliveryplatform.web.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusRequest {
    @NotBlank(message = "Email is required.")
    @Email(message = "Valid email is required.")
    private String email;

    @NotNull(message = "Status is required.")
    private UserStatus status;
}
