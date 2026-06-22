package org.tuvarna.smartdeliveryplatform.web.dto.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.tuvarna.smartdeliveryplatform.web.validation.ValidRegistrationAddress;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidRegistrationAddress
public class RegisterRequest {
    @Valid
    @NotNull(message = "User details are required.")
    private UserRegisterRequest userRegisterRequest;
    private AddressRequest addressRequest;
}
