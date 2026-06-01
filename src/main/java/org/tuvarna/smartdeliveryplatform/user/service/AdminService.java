package org.tuvarna.smartdeliveryplatform.user.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.config.demo.dto.DemoDataConstants;
import org.tuvarna.smartdeliveryplatform.courier.service.CourierService;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.repository.UserRepository;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.UserResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AdminService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final MerchantService merchantService;
    private final CourierService courierService;

    public AdminService(UserService userService, UserRepository userRepository,
                        MerchantService merchantService, CourierService courierService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.merchantService = merchantService;
        this.courierService = courierService;
    }

    public List<UserResponse> getAdmins() {
        List<UserResponse> userResponses = new ArrayList<>();
        List<User> admins = userRepository.findAllByRole(UserRole.ADMIN);
        for (User admin : admins) {
            UserResponse userResponse = initializeUserResponse(admin);
            userResponses.add(userResponse);
        }
        return userResponses;
    }

    public void updateUserStatus(String email, UserStatus status) {
        User user = userService.getUserByEmail(email);
        user.setStatus(status);
        userRepository.save(user);
        log.info("Updated status for user {} to {}", email, status);
    }

    @Transactional
    public void makeUserMerchant(MerchantRequest merchantRequest) {
        User user = userService.getUserByEmail(merchantRequest.getEmail());

        if (user.getRole() == UserRole.COURIER) {
            throw new IllegalStateException("User is already a courier and cannot become a merchant");
        }

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("User is already an admin and cannot become a merchant");
        }

        if (merchantService.getMerchantOptionalByUserEmail(merchantRequest.getEmail()).isPresent()) {
            throw new IllegalStateException("User is already a merchant");
        }

        user.setRole(UserRole.MERCHANT);
        userRepository.save(user);
        merchantService.createMerchantForUser(user, merchantRequest);
        log.info("Made user {} a merchant", merchantRequest.getEmail());
    }

    public void makeUserCourier(String email) {
        User user = userService.getUserByEmail(email);

        if (user.getRole() == UserRole.MERCHANT) {
            throw new IllegalStateException("User is already a merchant and cannot become a courier");
        }

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("User is already an admin and cannot become a courier");
        }

        if (courierService.getCourierByUserEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("User is already a courier");
        }

        user.setRole(UserRole.COURIER);
        userRepository.save(user);
        courierService.createCourierForUser(user);
        log.info("Made user {} a courier", email);
    }

    public void makeUserAdmin(String email) {
        User user = userService.getUserByEmail(email);

        if (courierService.getCourierByUserEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("User is a courier and cannot be an admin");
        }

        if (merchantService.getMerchantOptionalByUserEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("User is a merchant and cannot be an admin");
        }

        user.setRole(UserRole.ADMIN);
        userRepository.save(user);
        log.info("Made user {} an admin", email);
    }

    public void demoteAdmin(String email) {
        User user = userService.getUserByEmail(email);

        if (user.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("User is not an admin and cannot be demoted");
        }

        if(user.getEmail().equals("admin@smartdelivery.bg")) {
            throw new IllegalStateException("Main admin cannot be demoted");
        }

        user.setRole(UserRole.CLIENT);
        userRepository.save(user);
        log.info("Demoted admin {} to regular user", email);
    }

    public UserResponse getAdminByEmailAndRole(String email) {
        if (null == email || email.isBlank()) {
            return UserResponse.builder().build();
        }

        Optional<User> userOptional = userRepository.findByEmailAndRole(email, UserRole.ADMIN);
        if(userOptional.isEmpty()) {
            return UserResponse.builder().build();
        }

        User user = userOptional.get();
        return initializeUserResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        if (null == email || email.isBlank()) {
            return UserResponse.builder().build();
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isEmpty()) {
            return UserResponse.builder().build();
        }

        User user = userOptional.get();
        return initializeUserResponse(user);
    }

    public boolean adminAlreadyExists() {
        return userRepository.findByEmail(DemoDataConstants.ADMIN_EMAIL).isPresent();
    }

    private static UserResponse initializeUserResponse(User user) {
        return UserResponse.builder()
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
