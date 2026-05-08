package org.tuvarna.smartdeliveryplatform.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.web.dto.profile.UserProfileRequest;

@Service
@Slf4j
public class ProfileService {

    private final UserService userService;

    public ProfileService(UserService userService) {
        this.userService = userService;
    }

    public void updateUserProfile(String email, UserProfileRequest request) {
        User user = userService.getUserByEmail(email);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        userService.saveUser(user);
        log.info("Successfully updated profile for user: {}", email);
    }

    public UserProfileRequest initializeProfileRequest(User user) {
        return UserProfileRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}