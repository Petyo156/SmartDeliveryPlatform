package org.tuvarna.smartdeliveryplatform.web.dto.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DemoUserRequest {
    private String email;

    private String firstName;

    private String lastName;
}
