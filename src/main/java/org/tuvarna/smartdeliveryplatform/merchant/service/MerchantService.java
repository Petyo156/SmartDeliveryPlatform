package org.tuvarna.smartdeliveryplatform.merchant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.address.service.AddressService;
import org.tuvarna.smartdeliveryplatform.shared.constants.ErrorMessages;
import org.tuvarna.smartdeliveryplatform.exception.MerchantNotFoundException;
import org.tuvarna.smartdeliveryplatform.exception.MerchantOperationException;
import org.tuvarna.smartdeliveryplatform.exception.SystemOperationException;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.merchant.repository.MerchantRepository;
import org.tuvarna.smartdeliveryplatform.order.service.OrderService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserStatus;
import org.tuvarna.smartdeliveryplatform.shared.utils.SlugUtil;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantAddressResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantCardResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantPageResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantProfileRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class MerchantService {
    private final MerchantRepository merchantRepository;
    private final AddressService addressService;
    private final UserService userService;
    private final OrderService orderService;

    public MerchantService(MerchantRepository merchantRepository,
                           AddressService addressService,
                           UserService userService,
                           OrderService orderService) {
        this.merchantRepository = merchantRepository;
        this.addressService = addressService;
        this.userService = userService;
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public MerchantProfileRequest getMerchantProfileRequest(String searchEmail) {
        Merchant merchant = getMerchantByUserEmail(searchEmail);
        return initializeMerchantProfileRequest(merchant);
    }

    @Transactional(readOnly = true)
    public MerchantResponse getMerchantResponse(String searchEmail) {
        if (null == searchEmail || searchEmail.isBlank()) {
            return MerchantResponse.builder().build();
        }

        return getMerchantOptionalByUserEmail(searchEmail)
                .map(merchant -> initializeMerchantResponse(searchEmail, merchant))
                .orElseGet(() -> MerchantResponse.builder().build());
    }

    @Transactional
    public void toggleMerchantActiveStatus(String email) {
        Merchant merchant = getMerchantOptionalByUserEmail(email)
                .orElseThrow(() -> new MerchantOperationException(ErrorMessages.MERCHANT_WITH_EMAIL_DOES_NOT_EXIST));
        boolean newActiveStatus = !merchant.getIsActive();
        validateMerchantCanBeDeactivated(email, newActiveStatus);

        merchant.setIsActive(newActiveStatus);
        updateLinkedUserStatusFromMerchantStatus(merchant);
        if (!merchant.getIsActive()) {
            merchant.setIsClosed(true);
        }
        merchantRepository.save(merchant);
        log.info("Toggled active status for merchant {} to {}", email, merchant.getIsActive());
    }

    @Transactional
    public void setMerchantActiveStatus(String email, boolean isActive) {
        Merchant merchant = getMerchantByUserEmail(email);
        validateMerchantCanBeDeactivated(email, isActive);

        merchant.setIsActive(isActive);
        if (!isActive) {
            merchant.setIsClosed(true);
        }
        merchantRepository.save(merchant);
        log.info("Set active status for merchant {} to {}", email, isActive);
    }

    @Transactional
    public void toggleMerchantIsClosedStatus(String email) {
        Merchant merchant = getMerchantByUserEmail(email);
        merchant.setIsClosed(!merchant.getIsClosed());
        merchantRepository.save(merchant);
        log.info("Toggled is closed status for merchant {} to {}", email, merchant.getIsClosed());
    }

    @Transactional
    public void updateMerchantProfile(AuthenticationMetadata authenticationMetadata, MerchantProfileRequest merchantProfileRequest) {
        Merchant merchant = getMerchantByUserEmail(authenticationMetadata.getUsername());
        Address address = addressService.getAddressByIdAndUser(merchantProfileRequest.getAddressId(), merchant.getUser());

        merchant.setName(merchantProfileRequest.getName());
        merchant.setDescription(merchantProfileRequest.getDescription());
        merchant.setAddress(address);
        merchant.setImageUrl(merchantProfileRequest.getImageUrl());

        merchantRepository.save(merchant);
        log.info("Updated profile for merchant {}", merchant.getName());
    }

    public boolean merchantIsClosedStatus(AuthenticationMetadata authenticationMetadata) {
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        if (user == null || user.getRole() != UserRole.MERCHANT) {
            return false;
        }

        Merchant merchant = getMerchantByUserEmail(authenticationMetadata.getUsername());
        return merchant.getIsClosed();
    }

    public boolean merchantIsActive(String email) {
        return getMerchantOptionalByUserEmail(email)
                .map(Merchant::getIsActive)
                .orElse(false);
    }

    @Transactional
    public void createMerchantForUser(User user, MerchantRequest request) {
        Address address = addressService.addAddress(user,request.getAddress());
        Merchant merchant = initializeMerchant(user, request);
        merchant.setAddress(address);
        String slug = initializeSlugForMerchant(merchant, address.getCity());
        merchant.setSlug(slug);
        if(merchantRepository.existsMerchantBySlug(slug)) {
            throw new SystemOperationException(ErrorMessages.MERCHANT_SLUG_ALREADY_EXISTS);
        }
        merchantRepository.save(merchant);
    }

    public Merchant getMerchantByUserEmail(String email) {
        return merchantRepository.getMerchantByUser_Email(email)
                .orElseThrow(() -> new SystemOperationException(ErrorMessages.MERCHANT_WITH_EMAIL_DOES_NOT_EXIST));
    }

    public Optional<Merchant> getMerchantOptionalByUserEmail(String email) {
        return merchantRepository.getMerchantByUser_Email(email);
    }

    @Transactional(readOnly = true)
    public MerchantPageResponse getMerchantPageBySlug(String slug) {
        Merchant merchant = merchantRepository.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new MerchantNotFoundException(ErrorMessages.MERCHANT_WITH_SLUG_DOES_NOT_EXIST.formatted(slug)));

        return toMerchantPageResponse(merchant);
    }

    @Transactional(readOnly = true)
    public List<MerchantCardResponse> getTopActiveShops() {
        return getTopActiveMerchants(MerchantType.SHOP);
    }

    @Transactional(readOnly = true)
    public List<MerchantCardResponse> getTopActiveRestaurants() {
        return getTopActiveMerchants(MerchantType.RESTAURANT);
    }

    @Transactional(readOnly = true)
    public List<MerchantCardResponse> getAllActiveShops(String category) {
        return getAllActiveMerchants(MerchantType.SHOP, category);
    }

    @Transactional(readOnly = true)
    public List<MerchantCardResponse> getAllActiveRestaurants(String category) {
        return getAllActiveMerchants(MerchantType.RESTAURANT, category);
    }

    public boolean merchantCountMoreThanZero() {
        return merchantRepository.count() > 0;
    }

    private String initializeSlugForMerchant(Merchant merchant, String city) {
        String baseSlug = SlugUtil.normalize(merchant.getName());
        String citySlug = SlugUtil.normalize(city);
        return baseSlug + "-" + citySlug + "-" + UUID.randomUUID().toString().substring(0, 6);
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
                .imageUrl(merchantRequest.getImageUrl())
                .build();
    }

    private void updateLinkedUserStatusFromMerchantStatus(Merchant merchant) {
        UserStatus linkedUserStatus = merchant.getIsActive() ? UserStatus.ACTIVE : UserStatus.INACTIVE;
        merchant.getUser().setStatus(linkedUserStatus);
    }

    private void validateMerchantCanBeDeactivated(String email, boolean newActiveStatus) {
        if (!newActiveStatus && orderService.merchantHasActiveOrders(email)) {
            throw new MerchantOperationException(ErrorMessages.MERCHANT_CANNOT_BE_DEACTIVATED_ACTIVE_ORDERS);
        }
    }

    private MerchantResponse initializeMerchantResponse(String searchEmail, Merchant merchant) {
        return MerchantResponse.builder()
                .ownerEmail(searchEmail)
                .name(merchant.getName())
                .type(merchant.getType())
                .description(merchant.getDescription())
                .address(toMerchantAddressResponse(merchant.getAddress()))
                .isActive(merchant.getIsActive())
                .isClosed(merchant.getIsClosed())
                .createdAt(merchant.getCreatedAt())
                .imageUrl(merchant.getImageUrl())
                .build();
    }

    private MerchantProfileRequest initializeMerchantProfileRequest(Merchant merchant) {
        return MerchantProfileRequest.builder()
                .name(merchant.getName())
                .description(merchant.getDescription())
                .addressId(merchant.getAddress().getId())
                .isClosed(merchant.getIsClosed())
                .imageUrl(merchant.getImageUrl())
                .build();
    }

    private MerchantCardResponse toMerchantCardResponse(Merchant merchant) {
        return initializeMerchantCard(merchant);
    }

    private List<MerchantCardResponse> getTopActiveMerchants(MerchantType type) {
        return merchantRepository.findTop5ByIsActiveTrueAndTypeOrderByIsClosedAscCreatedAtDesc(type)
                .stream()
                .map(this::toMerchantCardResponse)
                .toList();
    }

    private List<MerchantCardResponse> getAllActiveMerchants(MerchantType type, String category) {
        List<Merchant> merchants = category == null || category.isBlank()
                ? merchantRepository.findAllByIsActiveTrueAndTypeOrderByIsClosedAscCreatedAtDesc(type)
                : merchantRepository.findMerchantsByCategory(type, category);

        return merchants.stream()
                .map(this::toMerchantCardResponse)
                .toList();
    }

    private MerchantCardResponse initializeMerchantCard(Merchant merchant) {
        return MerchantCardResponse.builder()
                .slug(merchant.getSlug())
                .name(merchant.getName())
                .description(merchant.getDescription())
                .imageUrl(merchant.getImageUrl())
                .type(merchant.getType())
                .isClosed(merchant.getIsClosed())
                .build();
    }

    private MerchantPageResponse toMerchantPageResponse(Merchant merchant) {
        return MerchantPageResponse.builder()
                .slug(merchant.getSlug())
                .name(merchant.getName())
                .description(merchant.getDescription())
                .imageUrl(merchant.getImageUrl())
                .type(merchant.getType())
                .isClosed(merchant.getIsClosed())
                .build();
    }

    private MerchantAddressResponse toMerchantAddressResponse(Address address) {
        return MerchantAddressResponse.builder()
                .id(address.getId())
                .city(address.getCity())
                .street(address.getStreet())
                .building(address.getBuilding())
                .build();
    }
}
