package org.tuvarna.smartdeliveryplatform.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.cart.model.Cart;
import org.tuvarna.smartdeliveryplatform.cart.service.CartService;
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
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.RegisterRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.UserRegisterRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceUTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AddressService addressService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private UserService userService;

    @Test
    void register_whenValidRequest_thenCreateClientWithCartAndAddress() {
        RegisterRequest request = registerRequest("client@example.com", "0888123456");
        Cart cart = new Cart();

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByPhoneNumber("0888123456")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartService.initializeCartForUser(any(User.class))).thenReturn(cart);

        userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(userCaptor.capture());
        List<User> savedUsers = userCaptor.getAllValues();
        User initiallySavedUser = savedUsers.getFirst();
        User finalSavedUser = savedUsers.getLast();

        assertEquals("client@example.com", initiallySavedUser.getEmail());
        assertEquals("encoded-password", initiallySavedUser.getPassword());
        assertEquals(UserRole.CLIENT, initiallySavedUser.getRole());
        assertEquals(UserStatus.ACTIVE, initiallySavedUser.getStatus());
        assertNotNull(initiallySavedUser.getCreatedAt());
        assertSame(cart, finalSavedUser.getCart());

        verify(cartService, times(1)).initializeCartForUser(initiallySavedUser);
        verify(addressService, times(1)).addAddressIfPresent(finalSavedUser, request.getAddressRequest());
    }

    @Test
    void register_whenEmailAlreadyExists_thenThrowUserWithEmailAlreadyExistsException() {
        RegisterRequest request = registerRequest("existing@example.com", "0888123456");
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(UserWithEmailAlreadyExistsException.class, () -> userService.register(request));

        verify(userRepository, never()).save(any());
        verify(cartService, never()).initializeCartForUser(any());
    }

    @Test
    void register_whenPhoneAlreadyExists_thenThrowUserWithPhoneNumberAlreadyExistsException() {
        RegisterRequest request = registerRequest("client@example.com", "0888123456");
        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByPhoneNumber("0888123456")).thenReturn(true);

        assertThrows(UserWithPhoneNumberAlreadyExistsException.class, () -> userService.register(request));

        verify(userRepository, never()).save(any());
        verify(cartService, never()).initializeCartForUser(any());
    }

    @Test
    void register_whenPasswordsDoNotMatch_thenThrowPasswordsDoNotMatchException() {
        RegisterRequest request = registerRequest("client@example.com", "0888123456");
        request.getUserRegisterRequest().setConfirmPassword("different-password");

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByPhoneNumber("0888123456")).thenReturn(false);

        assertThrows(PasswordsDoNotMatchException.class, () -> userService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenEmailIsBlank_thenThrowUserOperationException() {
        RegisterRequest request = registerRequest(" ", "0888123456");

        assertThrows(UserOperationException.class, () -> userService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserByEmail_whenUserExists_thenReturnUser() {
        User user = user("client@example.com", UserRole.CLIENT);
        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail("client@example.com");

        assertSame(user, result);
        verify(userRepository, times(1)).findByEmail("client@example.com");
    }

    @Test
    void getUserByEmail_whenUserDoesNotExist_thenThrowSystemOperationException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(SystemOperationException.class, () -> userService.getUserByEmail("missing@example.com"));

        verify(userRepository, times(1)).findByEmail("missing@example.com");
    }

    @Test
    void getAuthenticatedUser_whenAuthIsNull_thenReturnNull() {
        User result = userService.getAuthenticatedUser(null);

        assertNull(result);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void getAuthenticatedUser_whenAuthExists_thenReturnUserByEmail() {
        User user = user("client@example.com", UserRole.CLIENT);
        AuthenticationMetadata auth = AuthenticationMetadata.builder().email("client@example.com").build();
        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));

        User result = userService.getAuthenticatedUser(auth);

        assertSame(user, result);
        verify(userRepository, times(1)).findByEmail("client@example.com");
    }

    @Test
    void userCountMoreThanOne_whenRepositoryCountIsMoreThanOne_thenReturnTrue() {
        when(userRepository.count()).thenReturn(2L);

        assertTrue(userService.userCountMoreThanOne());
    }

    @Test
    void userCountMoreThanOne_whenRepositoryCountIsOne_thenReturnFalse() {
        when(userRepository.count()).thenReturn(1L);

        assertFalse(userService.userCountMoreThanOne());
    }

    @Test
    void checkIfPhoneNumberBelongsToAnotherUser_whenPhoneBelongsToAnotherUser_thenThrowException() {
        when(userRepository.existsByPhoneNumberAndEmailNot("0888123456", "client@example.com")).thenReturn(true);

        assertThrows(UserWithPhoneNumberAlreadyExistsException.class,
                () -> userService.checkIfPhoneNumberBelongsToAnotherUser("0888123456", "client@example.com"));
    }

    @Test
    void checkIfPhoneNumberBelongsToAnotherUser_whenPhoneIsBlank_thenDoNothing() {
        userService.checkIfPhoneNumberBelongsToAnotherUser(" ", "client@example.com");

        verify(userRepository, never()).existsByPhoneNumberAndEmailNot(any(), any());
    }

    @Test
    void saveUser_whenCalled_thenSaveUser() {
        User user = user("client@example.com", UserRole.CLIENT);

        userService.saveUser(user);

        verify(userRepository, times(1)).save(user);
    }

    private RegisterRequest registerRequest(String email, String phoneNumber) {
        return RegisterRequest.builder()
                .userRegisterRequest(UserRegisterRequest.builder()
                        .email(email)
                        .password("password123")
                        .confirmPassword("password123")
                        .firstName("Test")
                        .lastName("User")
                        .phoneNumber(phoneNumber)
                        .build())
                .addressRequest(AddressRequest.builder()
                        .city("Sofia")
                        .street("Test Street")
                        .building("1")
                        .isDefault(true)
                        .build())
                .build();
    }

    private User user(String email, UserRole role) {
        return User.builder()
                .email(email)
                .password("encoded-password")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("0888123456")
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
