package org.tuvarna.smartdeliveryplatform.courier.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.courier.model.Courier;
import org.tuvarna.smartdeliveryplatform.courier.repository.CourierRepository;
import org.tuvarna.smartdeliveryplatform.exception.CourierOperationException;
import org.tuvarna.smartdeliveryplatform.exception.ExceptionMessages;
import org.tuvarna.smartdeliveryplatform.exception.SystemOperationException;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.CourierResponse;
import java.util.Optional;

@Service
@Slf4j
public class CourierService {
    private final CourierRepository courierRepository;

    public CourierService(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Transactional
    public void createCourierForUser(User user) {
        Courier courier = initializeCourier(user);
        courierRepository.save(courier);
    }

    public CourierResponse getCourierResponse(String searchEmail) {
        if(null == searchEmail || searchEmail.isBlank()) {
            return CourierResponse.builder().build();
        }

        return getCourierByUserEmail(searchEmail)
                .map(courier -> initializeCourierResponse(searchEmail, courier))
                .orElseGet(() -> CourierResponse.builder().build());
    }

    public boolean courierExistsForUserEmail(String email) {
        return courierRepository.findCourierByUser_Email(email).isPresent();
    }

    public boolean courierCountMoreThanZero() {
        return courierRepository.count() > 0;
    }

    @Transactional
    public void toggleCourierActiveStatus(String email) {
        Courier courier = getExistingCourierByUserEmail(email);
        courier.setIsActive(!courier.getIsActive());
        updateLinkedUserStatusFromCourierStatus(courier);
        if (!courier.getIsActive()) {
            courier.setIsAvailable(false);
        }
        courierRepository.save(courier);
        log.info("Toggled active status for courier {} to {}", email, courier.getIsActive());
    }

    @Transactional
    public void setCourierActiveStatus(String email, boolean isActive) {
        Courier courier = getRequiredCourierByUserEmail(email);
        courier.setIsActive(isActive);
        if (!isActive) {
            courier.setIsAvailable(false);
        }
        courierRepository.save(courier);
        log.info("Set active status for courier {} to {}", email, isActive);
    }

    @Transactional
    public void toggleCourierAvailability(String email) {
        Courier courier = getExistingCourierByUserEmail(email);
        courier.setIsAvailable(!courier.getIsAvailable());
        courierRepository.save(courier);
        log.info("Toggled availability for courier {} to {}", email, courier.getIsAvailable());
    }

    public boolean courierIsAvailable(AuthenticationMetadata authenticationMetadata) {
        if (authenticationMetadata == null || authenticationMetadata.getRole() != UserRole.COURIER) {
            return false;
        }

        return getCourierByUserEmail(authenticationMetadata.getUsername())
                .map(Courier::getIsAvailable)
                .orElse(false);
    }

    public boolean courierIsActive(String email) {
        return getCourierByUserEmail(email)
                .map(Courier::getIsActive)
                .orElse(false);
    }

    private CourierResponse initializeCourierResponse(String searchEmail, Courier courier) {
        return CourierResponse.builder()
                .userEmail(searchEmail)
                .isActive(courier.getIsActive())
                .isAvailable(courier.getIsAvailable())
                .currentLng(courier.getCurrentLng())
                .currentLat(courier.getCurrentLat())
                .build();
    }

    private Optional<Courier> getCourierByUserEmail(String email) {
        return courierRepository.findCourierByUser_Email(email);
    }

    private Courier getExistingCourierByUserEmail(String email) {
        return getCourierByUserEmail(email)
                .orElseThrow(() -> new CourierOperationException(ExceptionMessages.COURIER_WITH_EMAIL_DOES_NOT_EXIST.formatted(email)));
    }

    private Courier getRequiredCourierByUserEmail(String email) {
        return getCourierByUserEmail(email)
                .orElseThrow(() -> new SystemOperationException(ExceptionMessages.COURIER_WITH_EMAIL_DOES_NOT_EXIST.formatted(email)));
    }

    private void updateLinkedUserStatusFromCourierStatus(Courier courier) {
        UserStatus linkedUserStatus = courier.getIsActive() ? UserStatus.ACTIVE : UserStatus.INACTIVE;
        courier.getUser().setStatus(linkedUserStatus);
    }

    private Courier initializeCourier(User user) {
        return Courier.builder()
                .user(user)
                .isActive(true)
                .isAvailable(false)
                .currentLat(0.0)
                .currentLng(0.0)
                .build();
    }
}
