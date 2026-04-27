package org.tuvarna.smartdeliveryplatform.web.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
public class UserRegisterRequest {
    @Email(message = "Invalid email address.")
    @NotNull
    private String email;

    @Size(min = 3, message = "Password must be at least 3 symbols.")
    private String password;

    @Size(min = 3, message = "Password must be at least 3 symbols.")
    private String confirmPassword;

    @Size(min = 10, max = 10, message = "Password must be at least 3 symbols.")
    @NotNull
    private String phoneNumber;

    private String firstName;

    private String lastName;
}
