package org.tuvarna.smartdeliveryplatform.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.config.demo.dto.DemoDataConstants;
import org.tuvarna.smartdeliveryplatform.courier.service.CourierService;
import org.tuvarna.smartdeliveryplatform.exception.AdminOperationException;
import org.tuvarna.smartdeliveryplatform.exception.ExceptionMessages;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.repository.UserRepository;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.UserResponse;

import java.util.List;

@Service
@Slf4j
public class AdminService {
    private final UserRepository userRepository;
    private final MerchantService merchantService;
    private final CourierService courierService;

    public AdminService(UserRepository userRepository,
                        MerchantService merchantService, CourierService courierService) {
        this.userRepository = userRepository;
        this.merchantService = merchantService;
        this.courierService = courierService;
    }

    public List<UserResponse> getAdmins() {
        return userRepository.findAllByRole(UserRole.ADMIN)
                .stream()
                .map(AdminService::initializeUserResponse)
                .toList();
    }

    @Transactional
    public void updateUserStatus(String email, UserStatus status) {
        User user = getExistingUserByEmail(email);
        user.setStatus(status);
        userRepository.save(user);
        updateMerchantStatusIfNeeded(user, status);
        updateCourierStatusIfNeeded(user, status);
        log.info("Updated status for user {} to {}", email, status);
    }

    @Transactional
    public void makeUserMerchant(MerchantRequest merchantRequest) {
        User user = getExistingUserByEmail(merchantRequest.getEmail());

        if (user.getRole() == UserRole.COURIER) {
            throw new AdminOperationException(ExceptionMessages.USER_ALREADY_COURIER_CANNOT_BECOME_MERCHANT);
        }

        if (user.getRole() == UserRole.ADMIN) {
            throw new AdminOperationException(ExceptionMessages.USER_ALREADY_ADMIN_CANNOT_BECOME_MERCHANT);
        }

        if (merchantService.getMerchantOptionalByUserEmail(merchantRequest.getEmail()).isPresent()) {
            throw new AdminOperationException(ExceptionMessages.USER_ALREADY_MERCHANT);
        }

        user.setRole(UserRole.MERCHANT);
        userRepository.save(user);
        merchantService.createMerchantForUser(user, merchantRequest);
        log.info("Made user {} a merchant", merchantRequest.getEmail());
    }

    @Transactional
    public void makeUserCourier(String email) {
        User user = getExistingUserByEmail(email);

        if (user.getRole() == UserRole.MERCHANT) {
            throw new AdminOperationException(ExceptionMessages.USER_ALREADY_MERCHANT_CANNOT_BECOME_COURIER);
        }

        if (user.getRole() == UserRole.ADMIN) {
            throw new AdminOperationException(ExceptionMessages.USER_ALREADY_ADMIN_CANNOT_BECOME_COURIER);
        }

        if (courierService.courierExistsForUserEmail(user.getEmail())) {
            throw new AdminOperationException(ExceptionMessages.USER_ALREADY_COURIER);
        }

        user.setRole(UserRole.COURIER);
        userRepository.save(user);
        courierService.createCourierForUser(user);
        log.info("Made user {} a courier", email);
    }

    @Transactional
    public void makeUserAdmin(String email) {
        User user = getExistingUserByEmail(email);

        if (courierService.courierExistsForUserEmail(user.getEmail())) {
            throw new AdminOperationException(ExceptionMessages.USER_COURIER_CANNOT_BE_ADMIN);
        }

        if (merchantService.getMerchantOptionalByUserEmail(user.getEmail()).isPresent()) {
            throw new AdminOperationException(ExceptionMessages.USER_MERCHANT_CANNOT_BE_ADMIN);
        }

        user.setRole(UserRole.ADMIN);
        userRepository.save(user);
        log.info("Made user {} an admin", email);
    }

    @Transactional
    public void demoteAdmin(String email) {
        User user = getExistingUserByEmail(email);

        if (user.getRole() != UserRole.ADMIN) {
            throw new AdminOperationException(ExceptionMessages.USER_NOT_ADMIN_CANNOT_BE_DEMOTED);
        }

        if(user.getEmail().equals("admin@smartdelivery.bg")) {
            throw new AdminOperationException(ExceptionMessages.MAIN_ADMIN_CANNOT_BE_DEMOTED);
        }

        user.setRole(UserRole.CLIENT);
        userRepository.save(user);
        log.info("Demoted admin {} to regular user", email);
    }

    public UserResponse getAdminByEmailAndRole(String email) {
        if (null == email || email.isBlank()) {
            return UserResponse.builder().build();
        }

        return userRepository.findByEmailAndRole(email, UserRole.ADMIN)
                .map(AdminService::initializeUserResponse)
                .orElseGet(() -> UserResponse.builder().build());
    }

    public UserResponse findUserResponseByEmail(String email) {
        if (null == email || email.isBlank()) {
            return UserResponse.builder().build();
        }

        return userRepository.findByEmail(email)
                .map(AdminService::initializeUserResponse)
                .orElseGet(() -> UserResponse.builder().build());
    }

    public boolean adminAlreadyExists() {
        return userRepository.findByEmail(DemoDataConstants.ADMIN_EMAIL).isPresent();
    }

    private void updateMerchantStatusIfNeeded(User user, UserStatus status) {
        if (user.getRole() != UserRole.MERCHANT) {
            return;
        }

        merchantService.setMerchantActiveStatus(user.getEmail(), status == UserStatus.ACTIVE);
    }

    private void updateCourierStatusIfNeeded(User user, UserStatus status) {
        if (user.getRole() != UserRole.COURIER) {
            return;
        }

        courierService.setCourierActiveStatus(user.getEmail(), status == UserStatus.ACTIVE);
    }

    private User getExistingUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AdminOperationException(ExceptionMessages.USER_WITH_EMAIL_DOES_NOT_EXIST));
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
