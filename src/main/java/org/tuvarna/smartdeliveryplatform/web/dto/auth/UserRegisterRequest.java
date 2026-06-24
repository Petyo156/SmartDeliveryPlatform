package org.tuvarna.smartdeliveryplatform.web.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterRequest {
    @NotBlank(message = ValidationMessages.EMAIL_REQUIRED_NO_PERIOD)
    @Email(message = ValidationMessages.VALID_EMAIL_REQUIRED_NO_PERIOD)
    private String email;

    @NotBlank(message = ValidationMessages.PASSWORD_REQUIRED)
    private String password;

    @NotBlank(message = ValidationMessages.CONFIRM_PASSWORD_REQUIRED)
    private String confirmPassword;

    @NotBlank(message = ValidationMessages.FIRST_NAME_REQUIRED)
    private String firstName;

    @NotBlank(message = ValidationMessages.LAST_NAME_REQUIRED)
    private String lastName;

    @NotBlank(message = ValidationMessages.PHONE_REQUIRED)
    @Pattern(regexp = "\\d{10}", message = ValidationMessages.PHONE_EXACTLY_TEN_DIGITS)
    private String phoneNumber;
}
