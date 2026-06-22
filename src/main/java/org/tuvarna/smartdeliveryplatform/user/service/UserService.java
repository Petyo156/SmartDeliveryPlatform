package org.tuvarna.smartdeliveryplatform.user.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.cart.model.Cart;
import org.tuvarna.smartdeliveryplatform.cart.service.CartService;
import org.tuvarna.smartdeliveryplatform.exception.ExceptionMessages;
import org.tuvarna.smartdeliveryplatform.exception.PasswordsDoNotMatchException;
import org.tuvarna.smartdeliveryplatform.exception.SystemOperationException;
import org.tuvarna.smartdeliveryplatform.exception.UserOperationException;
import org.tuvarna.smartdeliveryplatform.exception.UserWithEmailAlreadyExistsException;
import org.tuvarna.smartdeliveryplatform.exception.UserWithPhoneNumberAlreadyExistsException;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.repository.UserRepository;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.RegisterRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.UserRegisterRequest;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AddressService addressService;
    private final CartService cartService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AddressService addressService,
                       CartService cartService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.addressService = addressService;
        this.cartService = cartService;
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {
        UserRegisterRequest userRequest = registerRequest.getUserRegisterRequest();
        AddressRequest addressRequest = registerRequest.getAddressRequest();
        validateInput(userRequest.getEmail(), userRequest.getPassword());
        checkIfEmailAlreadyExists(userRequest.getEmail());
        checkIfPhoneNumberAlreadyExists(userRequest.getPhoneNumber());
        checkIfPasswordsMatch(userRequest.getPassword(), userRequest.getConfirmPassword());

        setupUser(userRequest, addressRequest);
        log.info("User registered successfully: {}", userRequest.getEmail());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()
                -> new SystemOperationException(ExceptionMessages.USER_WITH_EMAIL_DOES_NOT_EXIST));
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User getAuthenticatedUser(AuthenticationMetadata auth) {
        if (null == auth) {
            return null;
        }

        return getUserByEmail(auth.getEmail());
    }

    public boolean userCountMoreThanOne() {
        return userRepository.count() > 1;
    }

    public void checkIfPhoneNumberBelongsToAnotherUser(String phoneNumber, String email) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return;
        }

        if (userRepository.existsByPhoneNumberAndEmailNot(phoneNumber, email)) {
            throw new UserWithPhoneNumberAlreadyExistsException(ExceptionMessages.USER_WITH_PHONE_NUMBER_ALREADY_EXISTS);
        }
    }

    public User initializeUser(UserRegisterRequest userRegisterRequest) {
        return User.builder()
                .email(userRegisterRequest.getEmail())
                .password(passwordEncoder.encode(userRegisterRequest.getPassword()))
                .firstName(userRegisterRequest.getFirstName())
                .lastName(userRegisterRequest.getLastName())
                .phoneNumber(userRegisterRequest.getPhoneNumber())
                .role(UserRole.CLIENT)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void validateInput(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new UserOperationException(ExceptionMessages.EMAIL_MUST_NOT_BE_EMPTY);
        }
        if (password == null || password.isBlank()) {
            throw new UserOperationException(ExceptionMessages.PASSWORD_MUST_NOT_BE_EMPTY);
        }
    }

    private void checkIfPasswordsMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new PasswordsDoNotMatchException(ExceptionMessages.PASSWORDS_DO_NOT_MATCH);
        }
    }

    private void checkIfEmailAlreadyExists(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserWithEmailAlreadyExistsException(ExceptionMessages.USER_WITH_EMAIL_ALREADY_EXISTS);
        }
    }

    private void checkIfPhoneNumberAlreadyExists(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserWithPhoneNumberAlreadyExistsException(ExceptionMessages.USER_WITH_PHONE_NUMBER_ALREADY_EXISTS);
        }
    }

    private void setupUser(UserRegisterRequest userRegisterRequest, AddressRequest addressRequest) {
        User user = initializeUser(userRegisterRequest);
        user = userRepository.save(user);

        Cart cart = cartService.initializeCartForUser(user);
        user.setCart(cart);

        addressService.addAddressIfPresent(user, addressRequest);
        userRepository.save(user);
    }
}
