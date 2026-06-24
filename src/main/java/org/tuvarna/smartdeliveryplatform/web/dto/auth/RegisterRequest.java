package org.tuvarna.smartdeliveryplatform.web.dto.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;
import org.tuvarna.smartdeliveryplatform.web.validation.ValidRegistrationAddress;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidRegistrationAddress
public class RegisterRequest {
    @Valid
    @NotNull(message = ValidationMessages.USER_DETAILS_REQUIRED)
    private UserRegisterRequest userRegisterRequest;
    private AddressRequest addressRequest;
}
