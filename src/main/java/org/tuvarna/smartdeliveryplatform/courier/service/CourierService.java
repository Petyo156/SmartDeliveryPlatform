package org.tuvarna.smartdeliveryplatform.courier.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.courier.model.Courier;
import org.tuvarna.smartdeliveryplatform.courier.repository.CourierRepository;
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
        Courier courier = Courier.builder()
                .user(user)
                .isAvailable(true)
                .currentLat(0.0)
                .currentLng(0.0)
                .build();
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

    public Optional<Courier> getCourierByUserEmail(String email) {
        return courierRepository.findCourierByUser_Email(email);
    }

    @Transactional
    public void toggleCourierStatus(String email) {
        Courier courier = courierRepository.findCourierByUser_Email(email)
                .orElseThrow(() -> new IllegalStateException("Courier with email '" + email + "' does not exist"));
        courier.setIsAvailable(!courier.getIsAvailable());
        courierRepository.save(courier);
        log.info("Toggled availability for courier {} to {}", email, courier.getIsAvailable());
    }

    private CourierResponse initializeCourierResponse(String searchEmail, Courier courier) {
        return CourierResponse.builder()
                .userEmail(searchEmail)
                .isAvailable(courier.getIsAvailable())
                .currentLng(courier.getCurrentLng())
                .currentLat(courier.getCurrentLat())
                .build();
    }
}
