package org.tuvarna.smartdeliveryplatform.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.exception.MerchantNotFoundException;
import org.tuvarna.smartdeliveryplatform.exception.SystemOperationException;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.merchant.repository.MerchantRepository;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantCardResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantPageResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantProfileRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantServiceUTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private AddressService addressService;

    @Mock
    private UserService userService;

    @InjectMocks
    private MerchantService merchantService;

    @Test
    void createMerchantForUser_whenValidRequest_thenSaveActiveClosedMerchant() {
        User user = user("merchant@example.com", UserRole.CLIENT);
        Address address = address();
        MerchantRequest request = merchantRequest("merchant@example.com");

        when(addressService.addAddress(user, request.getAddress())).thenReturn(address);
        when(merchantRepository.existsMerchantBySlug(any())).thenReturn(false);

        merchantService.createMerchantForUser(user, request);

        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
        verify(merchantRepository, times(1)).save(merchantCaptor.capture());
        Merchant savedMerchant = merchantCaptor.getValue();

        assertEquals(user, savedMerchant.getUser());
        assertEquals(address, savedMerchant.getAddress());
        assertEquals("Test Merchant", savedMerchant.getName());
        assertEquals(MerchantType.RESTAURANT, savedMerchant.getType());
        assertEquals("https://img.test/merchant.png", savedMerchant.getImageUrl());
        assertTrue(savedMerchant.getIsActive());
        assertTrue(savedMerchant.getIsClosed());
        assertNotNull(savedMerchant.getCreatedAt());
        assertTrue(savedMerchant.getSlug().startsWith("test-merchant-sofia-"));
    }

    @Test
    void createMerchantForUser_whenGeneratedSlugAlreadyExists_thenThrowSystemOperationException() {
        User user = user("merchant@example.com", UserRole.CLIENT);
        Address address = address();
        MerchantRequest request = merchantRequest("merchant@example.com");

        when(addressService.addAddress(user, request.getAddress())).thenReturn(address);
        when(merchantRepository.existsMerchantBySlug(any())).thenReturn(true);

        assertThrows(SystemOperationException.class, () -> merchantService.createMerchantForUser(user, request));

        verify(merchantRepository, never()).save(any());
    }

    @Test
    void getMerchantByUserEmail_whenMerchantExists_thenReturnMerchant() {
        Merchant merchant = merchant("merchant@example.com");
        when(merchantRepository.getMerchantByUser_Email("merchant@example.com")).thenReturn(Optional.of(merchant));

        Merchant result = merchantService.getMerchantByUserEmail("merchant@example.com");

        assertEquals(merchant, result);
        verify(merchantRepository, times(1)).getMerchantByUser_Email("merchant@example.com");
    }

    @Test
    void getMerchantByUserEmail_whenMerchantDoesNotExist_thenThrowSystemOperationException() {
        when(merchantRepository.getMerchantByUser_Email("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(SystemOperationException.class, () -> merchantService.getMerchantByUserEmail("missing@example.com"));

        verify(merchantRepository, times(1)).getMerchantByUser_Email("missing@example.com");
    }

    @Test
    void toggleMerchantActiveStatus_whenDeactivatingMerchant_thenCloseMerchantAndDeactivateUser() {
        Merchant merchant = merchant("merchant@example.com");
        merchant.setIsActive(true);
        merchant.setIsClosed(false);
        when(merchantRepository.getMerchantByUser_Email("merchant@example.com")).thenReturn(Optional.of(merchant));

        merchantService.toggleMerchantActiveStatus("merchant@example.com");

        assertFalse(merchant.getIsActive());
        assertTrue(merchant.getIsClosed());
        assertEquals(UserStatus.INACTIVE, merchant.getUser().getStatus());
        verify(merchantRepository, times(1)).save(merchant);
    }

    @Test
    void setMerchantActiveStatus_whenSetInactive_thenCloseMerchant() {
        Merchant merchant = merchant("merchant@example.com");
        merchant.setIsActive(true);
        merchant.setIsClosed(false);
        when(merchantRepository.getMerchantByUser_Email("merchant@example.com")).thenReturn(Optional.of(merchant));

        merchantService.setMerchantActiveStatus("merchant@example.com", false);

        assertFalse(merchant.getIsActive());
        assertTrue(merchant.getIsClosed());
        verify(merchantRepository, times(1)).save(merchant);
    }

    @Test
    void toggleMerchantIsClosedStatus_whenCalled_thenToggleAndSaveMerchant() {
        Merchant merchant = merchant("merchant@example.com");
        merchant.setIsClosed(true);
        when(merchantRepository.getMerchantByUser_Email("merchant@example.com")).thenReturn(Optional.of(merchant));

        merchantService.toggleMerchantIsClosedStatus("merchant@example.com");

        assertFalse(merchant.getIsClosed());
        verify(merchantRepository, times(1)).save(merchant);
    }

    @Test
    void updateMerchantProfile_whenValidRequest_thenUpdateProfileAndSaveMerchant() {
        Merchant merchant = merchant("merchant@example.com");
        Address address = address();
        UUID addressId = UUID.randomUUID();
        MerchantProfileRequest request = MerchantProfileRequest.builder()
                .name("Updated Merchant")
                .description("Updated description")
                .addressId(addressId)
                .imageUrl("https://img.test/updated.png")
                .build();
        AuthenticationMetadata auth = AuthenticationMetadata.builder().email("merchant@example.com").build();

        when(merchantRepository.getMerchantByUser_Email("merchant@example.com")).thenReturn(Optional.of(merchant));
        when(addressService.getAddressByIdAndUser(addressId, merchant.getUser())).thenReturn(address);

        merchantService.updateMerchantProfile(auth, request);

        assertEquals("Updated Merchant", merchant.getName());
        assertEquals("Updated description", merchant.getDescription());
        assertEquals(address, merchant.getAddress());
        assertEquals("https://img.test/updated.png", merchant.getImageUrl());
        verify(merchantRepository, times(1)).save(merchant);
    }

    @Test
    void merchantIsClosedStatus_whenAuthenticatedUserIsMerchant_thenReturnMerchantClosedStatus() {
        User user = user("merchant@example.com", UserRole.MERCHANT);
        Merchant merchant = merchant("merchant@example.com");
        merchant.setIsClosed(true);
        AuthenticationMetadata auth = AuthenticationMetadata.builder().email("merchant@example.com").build();

        when(userService.getAuthenticatedUser(auth)).thenReturn(user);
        when(merchantRepository.getMerchantByUser_Email("merchant@example.com")).thenReturn(Optional.of(merchant));

        assertTrue(merchantService.merchantIsClosedStatus(auth));
    }

    @Test
    void merchantIsClosedStatus_whenAuthenticatedUserIsNotMerchant_thenReturnFalse() {
        User user = user("client@example.com", UserRole.CLIENT);
        AuthenticationMetadata auth = AuthenticationMetadata.builder().email("client@example.com").build();
        when(userService.getAuthenticatedUser(auth)).thenReturn(user);

        assertFalse(merchantService.merchantIsClosedStatus(auth));

        verify(merchantRepository, never()).getMerchantByUser_Email(any());
    }

    @Test
    void getMerchantPageBySlug_whenMerchantExists_thenReturnPageResponse() {
        Merchant merchant = merchant("merchant@example.com");
        when(merchantRepository.findBySlugAndIsActiveTrue("test-merchant")).thenReturn(Optional.of(merchant));

        MerchantPageResponse result = merchantService.getMerchantPageBySlug("test-merchant");

        assertEquals(merchant.getName(), result.getName());
        assertEquals(merchant.getSlug(), result.getSlug());
    }

    @Test
    void getMerchantPageBySlug_whenMerchantDoesNotExist_thenThrowMerchantNotFoundException() {
        when(merchantRepository.findBySlugAndIsActiveTrue("missing")).thenReturn(Optional.empty());

        assertThrows(MerchantNotFoundException.class, () -> merchantService.getMerchantPageBySlug("missing"));
    }

    @Test
    void getTopActiveRestaurants_whenMerchantsExist_thenReturnCards() {
        Merchant merchant = merchant("merchant@example.com");
        when(merchantRepository.findTop5ByIsActiveTrueAndTypeOrderByIsClosedAscCreatedAtDesc(MerchantType.RESTAURANT))
                .thenReturn(List.of(merchant));

        List<MerchantCardResponse> result = merchantService.getTopActiveRestaurants();

        assertEquals(1, result.size());
        assertEquals(merchant.getName(), result.getFirst().getName());
    }

    @Test
    void getMerchantResponse_whenMerchantExists_thenReturnMerchantResponse() {
        Merchant merchant = merchant("merchant@example.com");
        when(merchantRepository.getMerchantByUser_Email("merchant@example.com")).thenReturn(Optional.of(merchant));

        MerchantResponse result = merchantService.getMerchantResponse("merchant@example.com");

        assertEquals("merchant@example.com", result.getOwnerEmail());
        assertEquals(merchant.getName(), result.getName());
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

    private Merchant merchant(String email) {
        return Merchant.builder()
                .user(user(email, UserRole.MERCHANT))
                .name("Test Merchant")
                .description("Test description")
                .type(MerchantType.RESTAURANT)
                .address(address())
                .isActive(true)
                .isClosed(false)
                .createdAt(LocalDateTime.now())
                .imageUrl("https://img.test/merchant.png")
                .slug("test-merchant")
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

    private Address address() {
        return Address.builder()
                .id(UUID.randomUUID())
                .city("Sofia")
                .street("Test Street")
                .building("1")
                .isDefault(true)
                .build();
    }
}
