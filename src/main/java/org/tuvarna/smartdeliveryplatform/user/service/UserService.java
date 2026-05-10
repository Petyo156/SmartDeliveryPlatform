package org.tuvarna.smartdeliveryplatform.user.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.cart.model.Cart;
import org.tuvarna.smartdeliveryplatform.cart.service.CartService;
import org.tuvarna.smartdeliveryplatform.exception.PasswordsDoNotMatchException;
import org.tuvarna.smartdeliveryplatform.exception.UserWithEmailAlreadyExistsException;
import org.tuvarna.smartdeliveryplatform.exception.UserWithEmailDoesntExistException;
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

        log.info("Registering user with email: {}", userRequest.getEmail());

        validateInput(userRequest.getEmail(), userRequest.getPassword());
        checkIfEmailAlreadyExists(userRequest.getEmail());
        checkIfPasswordsMatch(userRequest.getPassword(), userRequest.getConfirmPassword());

        setupUser(userRequest, addressRequest);
        log.info("User registered successfully: {}", userRequest.getEmail());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()
                -> new UserWithEmailDoesntExistException("User with email '" + email + "' does not exist"));
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

    public boolean userCountMoreThanZero() {
        return userRepository.count() > 0;
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
            throw new IllegalArgumentException("Email must not be empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
    }

    private void checkIfPasswordsMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new PasswordsDoNotMatchException("Passwords do not match");
        }
        log.info("Passwords match");
    }

    private void checkIfEmailAlreadyExists(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserWithEmailAlreadyExistsException("User with this email already exists");
        }
        log.info("Email is valid");
    }

    private void setupUser(UserRegisterRequest userReq, AddressRequest addressRequest) {
        User user = initializeUser(userReq);
        user = userRepository.save(user);

        Cart cart = cartService.initializeCartForUser(user);
        user.setCart(cart);

        addressService.addAddressIfPresent(user, addressRequest);
        userRepository.save(user);
    }
}
