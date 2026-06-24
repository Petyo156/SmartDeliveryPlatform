package org.tuvarna.smartdeliveryplatform.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.address.repository.AddressRepository;
import org.tuvarna.smartdeliveryplatform.cart.repository.CartRepository;
import org.tuvarna.smartdeliveryplatform.exception.UserWithEmailAlreadyExistsException;
import org.tuvarna.smartdeliveryplatform.exception.UserWithPhoneNumberAlreadyExistsException;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.repository.UserRepository;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.RegisterRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.UserRegisterRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class RegistrationITest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void registrationCreatesClientWithDefaultRoleAndRejectsDuplicateEmailOrPhone() {
        RegisterRequest request = registrationRequest(
                "registration.client@example.com",
                "0888123401",
                "Sofia",
                "Registration Street",
                "1"
        );

        userService.register(request);

        User registeredUser = userRepository.findByEmail("registration.client@example.com").orElseThrow();
        assertThat(registeredUser.getRole()).isEqualTo(UserRole.CLIENT);
        assertThat(registeredUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(cartRepository.findByUser_Email("registration.client@example.com")).isPresent();
        assertThat(addressRepository.findAllByUserOrderByIsDefaultDesc(registeredUser))
                .extracting(Address::getCity, Address::getStreet, Address::getBuilding)
                .containsExactly(tuple("Sofia", "Registration Street", "1"));

        assertThatExceptionOfType(UserWithEmailAlreadyExistsException.class)
                .isThrownBy(() -> userService.register(registrationRequest(
                        "registration.client@example.com",
                        "0888123402",
                        "Varna",
                        "Duplicate Email Street",
                        "2"
                )));

        assertThatExceptionOfType(UserWithPhoneNumberAlreadyExistsException.class)
                .isThrownBy(() -> userService.register(registrationRequest(
                        "registration.other@example.com",
                        "0888123401",
                        "Plovdiv",
                        "Duplicate Phone Street",
                        "3"
                )));
    }

    private RegisterRequest registrationRequest(String email, String phoneNumber, String city, String street, String building) {
        return RegisterRequest.builder()
                .userRegisterRequest(UserRegisterRequest.builder()
                        .email(email)
                        .password("TestPassword123")
                        .confirmPassword("TestPassword123")
                        .firstName("Flow")
                        .lastName("Client")
                        .phoneNumber(phoneNumber)
                        .build())
                .addressRequest(AddressRequest.builder()
                        .city(city)
                        .street(street)
                        .building(building)
                        .isDefault(true)
                        .build())
                .build();
    }
}
