package org.tuvarna.smartdeliveryplatform.web.dto.auth;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;

@Data
public class LoginRequest {
    @Size(min = 3, message = ValidationMessages.EMAIL_MIN_THREE_SYMBOLS)
    private String email;

    @Size(min = 3, message = ValidationMessages.PASSWORD_MIN_THREE_SYMBOLS)
    private String password;
}
