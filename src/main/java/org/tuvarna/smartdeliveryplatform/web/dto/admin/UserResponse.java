package org.tuvarna.smartdeliveryplatform.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private UserRole role;

    private UserStatus status;

    private LocalDateTime createdAt;
}
