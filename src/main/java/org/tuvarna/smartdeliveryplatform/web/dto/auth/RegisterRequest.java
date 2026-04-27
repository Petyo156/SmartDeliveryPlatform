package org.tuvarna.smartdeliveryplatform.web.dto.auth;

import lombok.*;

@Data
public class RegisterRequest {
    private UserRegisterRequest userRegisterRequest;
    private AddressRequest addressRequest;
}