package org.tuvarna.smartdeliveryplatform.address.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.address.repository.AddressRepository;
import org.tuvarna.smartdeliveryplatform.exception.AddressOperationException;
import org.tuvarna.smartdeliveryplatform.exception.ExceptionMessages;
import org.tuvarna.smartdeliveryplatform.exception.OrderOperationException;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantAddressUsageService;
import org.tuvarna.smartdeliveryplatform.shared.enums.CheckoutAddressMode;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantAddressResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.order.OrderPlacementRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.profile.UserAddressResponse;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

@Service
@Slf4j
public class AddressService {

    private static final int MAX_ADDRESSES_PER_USER = 5;

    private final AddressRepository addressRepository;
    private final MerchantAddressUsageService merchantAddressUsageService;

    public AddressService(AddressRepository addressRepository, MerchantAddressUsageService merchantAddressUsageService) {
        this.addressRepository = addressRepository;
        this.merchantAddressUsageService = merchantAddressUsageService;
    }

    @Transactional
    public Address addAddress(User user, AddressRequest addressRequest) {
        Address addressEntity = createAddress(user, addressRequest);
        log.info("Successfully added address for user: {}", user.getEmail());
        return addressEntity;
    }

    @Transactional
    public void addAddressIfPresent(User user, AddressRequest addressRequest) {
        if (isAddressRequestEmpty(addressRequest)) {
            return;
        }

        addAddress(user, addressRequest);
    }

    @Transactional(readOnly = true)
    public List<MerchantAddressResponse> getAllAddressesForMerchant(User user) {
        return getAllAddressesForUser(user)
                .stream()
                .map(this::initializeMerchantAddressResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserAddressResponse> getAllAddressResponsesForUser(User user) {
        return getAllAddressesForUser(user)
                .stream()
                .map(this::initializeUserAddressResponse)
                .toList();
    }

    @Transactional
    public Address resolveCheckoutAddress(User user, OrderPlacementRequest request) {
        if (request.getAddressMode() == CheckoutAddressMode.NEW) {
            return createCheckoutAddress(user, request);
        }

        if (request.getAddressId() == null) {
            throw new OrderOperationException(ExceptionMessages.CHOOSE_SAVED_DELIVERY_ADDRESS);
        }

        return findAddressByIdAndUser(request.getAddressId(), user);
    }

    @Transactional
    public void updateAddress(User authenticatedUser, UUID addressId, AddressRequest addressRequest) {
        Address address = addressRepository.findAddressByIdAndUser(addressId, authenticatedUser)
                .orElseThrow(() -> new AddressOperationException(ExceptionMessages.ADDRESS_NOT_FOUND));

        address.setCity(addressRequest.getCity());
        address.setStreet(addressRequest.getStreet());
        address.setBuilding(addressRequest.getBuilding());

        addressRepository.save(address);

        log.info("Successfully updated address {} for user: {}", addressId, authenticatedUser.getEmail());
    }

    public UserAddressResponse getAddressResponse(UUID editAddressId, User user) {
        Address address = getAddressByIdAndUser(editAddressId, user);
        return initializeUserAddressResponse(address);
    }

    @Transactional
    public void deleteAddress(User user, UUID id) {
        Address address = findAddressByIdAndUser(id, user);
        validateAddressIsNotUsedByMerchantProfile(user, id);

        boolean deletedDefaultAddress = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);

        if (deletedDefaultAddress) {
            promoteDefaultAddressIfNeeded(user);
        }

        log.info("Successfully deleted address {} for user: {}", id, user.getEmail());
    }

    @Transactional
    public void setDefaultAddress(User user, UUID id) {
        Address defaultAddress = getAddressByIdAndUser(id, user);

        clearDefaultAddresses(user);
        defaultAddress.setIsDefault(true);
        addressRepository.save(defaultAddress);
        log.info("Successfully set default address {} for user: {}", id, user.getEmail());
    }

    @Transactional
    public Address createCheckoutAddress(User user, OrderPlacementRequest request) {
        if (!canAddMoreAddresses(user)) {
            throw new OrderOperationException(ExceptionMessages.CHECKOUT_ADDRESS_LIMIT_EXCEEDED);
        }

        if (!hasText(request.getCity())
                || !hasText(request.getStreet())
                || !hasText(request.getBuilding())) {
            throw new OrderOperationException(ExceptionMessages.DELIVERY_ADDRESS_FIELDS_REQUIRED);
        }

        AddressRequest addressRequest = initializeAddressRequest(request);
        return addAddress(user, addressRequest);
    }

    public boolean canAddMoreAddresses(User user) {
        return addressRepository.countByUser(user) < MAX_ADDRESSES_PER_USER;
    }

    public AddressRequest initializeAddressEditRequest(UserAddressResponse addressResponse) {
        return AddressRequest.builder()
                .city(addressResponse.getCity())
                .building(addressResponse.getBuilding())
                .street(addressResponse.getStreet())
                .isDefault(addressResponse.isDefault())
                .build();
    }

    private List<Address> getAllAddressesForUser(User user) {
        return addressRepository.findAllByUserOrderByIsDefaultDesc(user);
    }

    private Address createAddress(User user, AddressRequest addressRequest) {
        validateAddressRequest(addressRequest);

        if (!canAddMoreAddresses(user)) {
            throw new AddressOperationException(ExceptionMessages.MAX_ADDRESS_LIMIT_EXCEEDED);
        }

        boolean isFirstAddress = getAllAddressesForUser(user).isEmpty();
        boolean shouldBeDefault = isFirstAddress || addressRequest.isDefault();

        if (shouldBeDefault) {
            clearDefaultAddresses(user);
        }

        Address address = initializeAddressForUser(user, addressRequest, shouldBeDefault);

        return addressRepository.save(address);
    }

    private MerchantAddressResponse initializeMerchantAddressResponse(Address address) {
        return MerchantAddressResponse.builder()
                .id(address.getId())
                .city(address.getCity())
                .street(address.getStreet())
                .building(address.getBuilding())
                .build();
    }

    private UserAddressResponse initializeUserAddressResponse(Address address) {
        return UserAddressResponse.builder()
                .id(address.getId().toString())
                .city(address.getCity())
                .street(address.getStreet())
                .building(address.getBuilding())
                .isDefault(address.getIsDefault())
                .build();
    }

    private void promoteDefaultAddressIfNeeded(User user) {
        if (addressRepository.findByUserAndIsDefaultTrue(user).isPresent()) {
            return;
        }

        addressRepository.findFirstByUserOrderByIdAsc(user)
                .ifPresent(address -> {
                    address.setIsDefault(true);
                    addressRepository.save(address);
                });
    }

    private void clearDefaultAddresses(User user) {
        List<Address> defaultAddresses = addressRepository.findAllByUserAndIsDefaultTrue(user);
        defaultAddresses.forEach(address -> address.setIsDefault(false));
        addressRepository.saveAllAndFlush(defaultAddresses);
    }

    private Address findAddressByIdAndUser(UUID addressId, User user) {
        return addressRepository.findAddressByIdAndUser(addressId, user)
                .orElseThrow(() -> new OrderOperationException(ExceptionMessages.CHOOSE_SAVED_DELIVERY_ADDRESS));
    }

    private AddressRequest initializeAddressRequest(OrderPlacementRequest request) {
        return AddressRequest.builder()
                .city(request.getCity())
                .street(request.getStreet())
                .building(request.getBuilding())
                .build();
    }

    private void validateAddressRequest(AddressRequest request) {
        if (request == null || !hasText(request.getCity()) || !hasText(request.getStreet()) || !hasText(request.getBuilding())) {
            throw new AddressOperationException(ExceptionMessages.ADDRESS_FIELDS_REQUIRED_WHEN_ADDING);
        }
    }

    private void validateAddressIsNotUsedByMerchantProfile(User user, UUID addressId) {
        if (merchantAddressUsageService.isMerchantProfileAddress(user, addressId)) {
            throw new AddressOperationException(ExceptionMessages.CANNOT_DELETE_MERCHANT_PROFILE_ADDRESS);
        }
    }

    private boolean isAddressRequestEmpty(AddressRequest request) {
        return request == null
                || (!hasText(request.getCity())
                && !hasText(request.getStreet())
                && !hasText(request.getBuilding()));
    }

    private Address initializeAddressForUser(User user, AddressRequest addressRequest, boolean isDefault) {
        return Address.builder()
                .user(user)
                .city(addressRequest.getCity())
                .street(addressRequest.getStreet())
                .building(addressRequest.getBuilding())
                .isDefault(isDefault)
                .build();
    }

    public Address getAddressByIdAndUser(UUID addressId, User user) {
        return addressRepository.findAddressByIdAndUser(addressId, user)
                .orElseThrow(() -> new AddressOperationException(ExceptionMessages.ADDRESS_NOT_FOUND_WITH_ID.formatted(addressId)));
    }
}
