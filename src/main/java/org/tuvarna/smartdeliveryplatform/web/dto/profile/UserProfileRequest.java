package org.tuvarna.smartdeliveryplatform.web.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileRequest {

    @NotBlank(message = ValidationMessages.FIRST_NAME_REQUIRED)
    private String firstName;

    @NotBlank(message = ValidationMessages.LAST_NAME_REQUIRED)
    private String lastName;

    @NotBlank(message = ValidationMessages.PHONE_REQUIRED)
    @Pattern(regexp = "^$|\\d{10}", message = ValidationMessages.PHONE_EXACTLY_TEN_DIGITS)
    private String phoneNumber;
}
