package org.tuvarna.smartdeliveryplatform.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tuvarna.smartdeliveryplatform.courier.service.CourierService;
import org.tuvarna.smartdeliveryplatform.exception.AdminOperationException;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.repository.UserRepository;
import org.tuvarna.smartdeliveryplatform.user.service.AdminService;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.UserResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceUTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MerchantService merchantService;

    @Mock
    private CourierService courierService;

    @InjectMocks
    private AdminService adminService;

    @Test
    void makeUserMerchant_whenValidUser_thenUpdateRoleAndCreateMerchant() {
        User user = user("merchant@example.com", UserRole.CLIENT);
        MerchantRequest request = merchantRequest("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.of(user));
        when(merchantService.getMerchantOptionalByUserEmail("merchant@example.com")).thenReturn(Optional.empty());

        adminService.makeUserMerchant(request);

        assertEquals(UserRole.MERCHANT, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userRepository, times(1)).save(user);
        verify(merchantService, times(1)).createMerchantForUser(user, request);
    }

    @Test
    void makeUserMerchant_whenUserIsCourier_thenThrowAdminOperationException() {
        User user = user("courier@example.com", UserRole.COURIER);
        MerchantRequest request = merchantRequest("courier@example.com");
        when(userRepository.findByEmail("courier@example.com")).thenReturn(Optional.of(user));

        assertThrows(AdminOperationException.class, () -> adminService.makeUserMerchant(request));

        verify(userRepository, never()).save(user);
        verify(merchantService, never()).createMerchantForUser(user, request);
    }

    @Test
    void makeUserMerchant_whenUserAlreadyHasMerchant_thenThrowAdminOperationException() {
        User user = user("merchant@example.com", UserRole.CLIENT);
        MerchantRequest request = merchantRequest("merchant@example.com");
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.of(user));
        when(merchantService.getMerchantOptionalByUserEmail("merchant@example.com")).thenReturn(Optional.of(new Merchant()));

        assertThrows(AdminOperationException.class, () -> adminService.makeUserMerchant(request));

        verify(userRepository, never()).save(user);
        verify(merchantService, never()).createMerchantForUser(user, request);
    }

    @Test
    void makeUserCourier_whenValidUser_thenUpdateRoleAndCreateCourier() {
        User user = user("courier@example.com", UserRole.CLIENT);
        when(userRepository.findByEmail("courier@example.com")).thenReturn(Optional.of(user));
        when(courierService.courierExistsForUserEmail("courier@example.com")).thenReturn(false);

        adminService.makeUserCourier("courier@example.com");

        assertEquals(UserRole.COURIER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userRepository, times(1)).save(user);
        verify(courierService, times(1)).createCourierForUser(user);
    }

    @Test
    void makeUserCourier_whenUserIsMerchant_thenThrowAdminOperationException() {
        User user = user("merchant@example.com", UserRole.MERCHANT);
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.of(user));

        assertThrows(AdminOperationException.class, () -> adminService.makeUserCourier("merchant@example.com"));

        verify(userRepository, never()).save(user);
        verify(courierService, never()).createCourierForUser(user);
    }

    @Test
    void makeUserAdmin_whenValidUser_thenUpdateRole() {
        User user = user("admin@example.com", UserRole.CLIENT);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(courierService.courierExistsForUserEmail("admin@example.com")).thenReturn(false);
        when(merchantService.getMerchantOptionalByUserEmail("admin@example.com")).thenReturn(Optional.empty());

        adminService.makeUserAdmin("admin@example.com");

        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void makeUserAdmin_whenUserIsCourier_thenThrowAdminOperationException() {
        User user = user("courier@example.com", UserRole.COURIER);
        when(userRepository.findByEmail("courier@example.com")).thenReturn(Optional.of(user));
        when(courierService.courierExistsForUserEmail("courier@example.com")).thenReturn(true);

        assertThrows(AdminOperationException.class, () -> adminService.makeUserAdmin("courier@example.com"));

        verify(userRepository, never()).save(user);
    }

    @Test
    void updateUserStatus_whenMerchantIsSetInactive_thenDeactivateMerchant() {
        User user = user("merchant@example.com", UserRole.MERCHANT);
        when(userRepository.findByEmail("merchant@example.com")).thenReturn(Optional.of(user));

        adminService.updateUserStatus("merchant@example.com", UserStatus.INACTIVE);

        assertEquals(UserStatus.INACTIVE, user.getStatus());
        verify(userRepository, times(1)).save(user);
        verify(merchantService, times(1)).setMerchantActiveStatus("merchant@example.com", false);
        verify(courierService, never()).setCourierActiveStatus("merchant@example.com", false);
    }

    @Test
    void updateUserStatus_whenCourierIsSetInactive_thenDeactivateCourier() {
        User user = user("courier@example.com", UserRole.COURIER);
        when(userRepository.findByEmail("courier@example.com")).thenReturn(Optional.of(user));

        adminService.updateUserStatus("courier@example.com", UserStatus.INACTIVE);

        assertEquals(UserStatus.INACTIVE, user.getStatus());
        verify(userRepository, times(1)).save(user);
        verify(courierService, times(1)).setCourierActiveStatus("courier@example.com", false);
        verify(merchantService, never()).setMerchantActiveStatus("courier@example.com", false);
    }

    @Test
    void demoteAdmin_whenValidAdmin_thenMakeClient() {
        User user = user("admin@example.com", UserRole.ADMIN);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        adminService.demoteAdmin("admin@example.com", "other-admin@example.com");

        assertEquals(UserRole.CLIENT, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void demoteAdmin_whenActingAdminDemotesSelf_thenThrowAdminOperationException() {
        User user = user("admin@example.com", UserRole.ADMIN);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        assertThrows(AdminOperationException.class,
                () -> adminService.demoteAdmin("admin@example.com", "admin@example.com"));

        verify(userRepository, never()).save(user);
    }

    @Test
    void getAdmins_whenAdminsExist_thenReturnAdminResponses() {
        User admin = user("admin@example.com", UserRole.ADMIN);
        when(userRepository.findAllByRole(UserRole.ADMIN)).thenReturn(List.of(admin));

        List<UserResponse> result = adminService.getAdmins();

        assertEquals(1, result.size());
        assertEquals("admin@example.com", result.getFirst().getEmail());
        assertEquals(UserRole.ADMIN, result.getFirst().getRole());
    }

    @Test
    void findUserResponseByEmail_whenUserExists_thenReturnUserResponse() {
        User user = user("client@example.com", UserRole.CLIENT);
        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(user));

        UserResponse result = adminService.findUserResponseByEmail("client@example.com");

        assertNotNull(result);
        assertEquals("client@example.com", result.getEmail());
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

    private MerchantRequest merchantRequest(String email) {
        return MerchantRequest.builder()
                .email(email)
                .name("Test Merchant")
                .description("Test description")
                .type(MerchantType.RESTAURANT)
                .imageUrl("https://img.test/merchant.png")
                .address(AddressRequest.builder()
                        .city("Sofia")
                        .street("Test Street")
                        .building("1")
                        .build())
                .build();
    }
}
