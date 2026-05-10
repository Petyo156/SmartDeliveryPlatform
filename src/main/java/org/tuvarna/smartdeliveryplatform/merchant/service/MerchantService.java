package org.tuvarna.smartdeliveryplatform.merchant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.merchant.repository.MerchantRepository;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantProfileRequest;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class MerchantService {
    private final MerchantRepository merchantRepository;
    private final AddressService addressService;
    private final UserService userService;

    public MerchantService(MerchantRepository merchantRepository, AddressService addressService, UserService userService) {
        this.merchantRepository = merchantRepository;
        this.addressService = addressService;
        this.userService = userService;
    }

    public MerchantProfileRequest getMerchantProfileRequest(String searchEmail) {
        Merchant merchant = getMerchantByUserEmail(searchEmail);
        return initializeMerchantProfileRequest(merchant);
    }

    public MerchantResponse getMerchantResponse(String searchEmail) {
        if (null == searchEmail || searchEmail.isBlank()) {
            return MerchantResponse.builder().build();
        }

        Optional<Merchant> merchantOptional = getMerchantOptionalByUserEmail(searchEmail);
        if (merchantOptional.isEmpty()) {
            return MerchantResponse.builder().build();
        }

        Merchant merchant = merchantOptional.get();
        return initializeMerchantResponse(searchEmail, merchant);
    }

    public void toggleMerchantActiveStatus(String email) {
        Merchant merchant = getMerchantByUserEmail(email);
        merchant.setIsActive(!merchant.getIsActive());
        if (!merchant.getIsActive()) {
            merchant.setIsClosed(true);
        }
        merchantRepository.save(merchant);
        log.info("Toggled active status for merchant {} to {}", email, merchant.getIsActive());
    }

    public void toggleMerchantIsClosedStatus(String email) {
        Merchant merchant = getMerchantByUserEmail(email);
        merchant.setIsClosed(!merchant.getIsClosed());
        merchantRepository.save(merchant);
        log.info("Toggled is closed status for merchant {} to {}", email, merchant.getIsClosed());
    }

    public void updateMerchantProfile(AuthenticationMetadata authenticationMetadata, MerchantProfileRequest merchantProfileRequest) {
        Merchant merchant = getMerchantByUserEmail(authenticationMetadata.getUsername());
        Address address = addressService.findAddressById(merchantProfileRequest.getAddressId());

        merchant.setName(merchantProfileRequest.getName());
        merchant.setDescription(merchantProfileRequest.getDescription());
        merchant.setAddress(address);
        merchant.setIsClosed(merchantProfileRequest.getIsClosed());

        merchantRepository.save(merchant);
        log.info("Updated profile for merchant {}", merchant.getName());
    }

    public Boolean merchantIsClosedStatus(AuthenticationMetadata authenticationMetadata) {
        if (null == authenticationMetadata) {
            return false;
        }

        User user = userService.getAuthenticatedUser(authenticationMetadata);
        if (user.getRole() != UserRole.MERCHANT) {
            return false;
        }

        Merchant merchant = getMerchantByUserEmail(authenticationMetadata.getUsername());
        return merchant.getIsClosed();
    }

    public void createMerchantForUser(User user, MerchantRequest request) {
        Address address = addressService.addAddress(user,request.getAddress());
        Merchant merchant = initializeMerchant(user, request);
        merchant.setAddress(address);
        merchantRepository.save(merchant);
    }

    public Merchant getMerchantByUserEmail(String email) {
        Optional<Merchant> merchantByUserEmail = merchantRepository.getMerchantByUser_Email(email);
        if (merchantByUserEmail.isEmpty()) {
            throw new IllegalStateException("Merchant with this email doesnt exist");
        }
        return merchantByUserEmail.get();
    }

    public Optional<Merchant> getMerchantOptionalByUserEmail(String email) {
        return merchantRepository.getMerchantByUser_Email(email);
    }

    private Merchant initializeMerchant(User user, MerchantRequest merchantRequest) {
        return Merchant.builder()
                .user(user)
                .name(merchantRequest.getName())
                .description(merchantRequest.getDescription())
                .type(merchantRequest.getType())
                .isActive(true)
                .isClosed(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private MerchantResponse initializeMerchantResponse(String searchEmail, Merchant merchant) {
        return MerchantResponse.builder()
                .ownerEmail(searchEmail)
                .name(merchant.getName())
                .type(merchant.getType())
                .description(merchant.getDescription())
                .address(merchant.getAddress())
                .isActive(merchant.getIsActive())
                .isClosed(merchant.getIsClosed())
                .createdAt(merchant.getCreatedAt())
                .build();
    }

    private MerchantProfileRequest initializeMerchantProfileRequest(Merchant merchant) {
        return MerchantProfileRequest.builder()
                .name(merchant.getName())
                .description(merchant.getDescription())
                .addressId(merchant.getAddress().getId())
                .isClosed(merchant.getIsClosed())
                .build();
    }
}