package org.tuvarna.smartdeliveryplatform.web.dto.auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private UserRegisterRequest userRegisterRequest;
    private AddressRequest addressRequest;
}