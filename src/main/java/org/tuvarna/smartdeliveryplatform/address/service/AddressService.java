package org.tuvarna.smartdeliveryplatform.address.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.address.repository.AddressRepository;
import org.tuvarna.smartdeliveryplatform.exception.OrderOperationException;
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

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
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

    public Address findAddressById(UUID uuid) {
        return addressRepository.findById(uuid)
                .orElseThrow(() -> new IllegalStateException("Address with this id does not exist"));
    }

    @Transactional
    public Address resolveCheckoutAddress(User user, OrderPlacementRequest request) {
        if (request.getAddressMode() == CheckoutAddressMode.NEW) {
            return createCheckoutAddress(user, request);
        }

        if (request.getAddressId() == null) {
            throw new OrderOperationException("Choose one of your saved delivery addresses.");
        }

        return findAddressByIdAndUser(request.getAddressId(), user);
    }

    @Transactional
    public void updateAddress(User authenticatedUser, String addressId, AddressRequest addressRequest) {
        UUID uuid = UUID.fromString(addressId);

        Address address = addressRepository.findAddressByIdAndUser(uuid, authenticatedUser)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        address.setCity(addressRequest.getCity());
        address.setStreet(addressRequest.getStreet());
        address.setBuilding(addressRequest.getBuilding());

        addressRepository.save(address);

        log.info("Successfully updated address {} for user: {}", addressId, authenticatedUser.getEmail());
    }

    public UserAddressResponse getAddressResponse(String editAddressId, User user) {
        Address address = getAddressByIdAndUser(UUID.fromString(editAddressId), user);
        return initializeUserAddressResponse(address);
    }

    @Transactional
    public void deleteAddress(User user, String id) {
        UUID addressId = UUID.fromString(id);
        Address address = findAddressByIdAndUser(addressId, user);

        boolean deletedDefaultAddress = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);

        if (deletedDefaultAddress) {
            promoteDefaultAddressIfNeeded(user);
        }

        log.info("Successfully deleted address {} for user: {}", id, user.getEmail());
    }

    @Transactional
    public void setDefaultAddress(User user, String id) {
        UUID addressId = UUID.fromString(id);
        Address defaultAddress = getAddressByIdAndUser(addressId, user);

        clearDefaultAddresses(user);
        defaultAddress.setIsDefault(true);
        addressRepository.save(defaultAddress);
        log.info("Successfully set default address {} for user: {}", id, user.getEmail());
    }

    @Transactional
    public Address createCheckoutAddress(User user, OrderPlacementRequest request) {
        if (!canAddMoreAddresses(user)) {
            throw new OrderOperationException("You reached the maximum address limit. Delete an address before adding a new one.");
        }

        if (!hasText(request.getCity())
                || !hasText(request.getStreet())
                || !hasText(request.getBuilding())) {
            throw new OrderOperationException("City, street and building are required for delivery address.");
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
            throw new IllegalStateException("Maximum address limit of addresses exceeded");
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
                .orElseThrow(() -> new OrderOperationException("Choose one of your saved delivery addresses."));
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
            throw new IllegalArgumentException("City, street and building are required when adding address.");
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

    private Address getAddressByIdAndUser(UUID addressId, User user) {
        return addressRepository.findAddressByIdAndUser(addressId, user)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with id: " + addressId.toString()));
    }
}
