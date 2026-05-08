package org.tuvarna.smartdeliveryplatform.address.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.address.repository.AddressRepository;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantAddressResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.profile.UserAddressResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public Address addAddress(User user, AddressRequest addressRequest) {
        if (addressRepository.countByUser(user) >= MAX_ADDRESSES_PER_USER) {
            throw new IllegalStateException("Maximum address limit of addresses exceeded");
        }

        Address address = initializeAddressForUser(user, addressRequest);
        Address addressEntity = addressRepository.save(address);
        log.info("Successfully added address for user: {}", user.getEmail());
        return addressEntity;
    }

    public List<MerchantAddressResponse> getAllAddressesForMerchant(User user) {
        List<Address> addresses = getAllAddressesForUser(user);
        List<MerchantAddressResponse> merchantAddressResponses = new ArrayList<>();
        for(Address address : addresses)
        {
            MerchantAddressResponse merchantAddressResponse = initializeMerchantAddressResponse(address);
            merchantAddressResponses.add(merchantAddressResponse);
        }
        return merchantAddressResponses;
    }

    public List<UserAddressResponse> getAllAddressResponsesForUser(User user) {
        List<Address> addresses = getAllAddressesForUser(user);
        List<UserAddressResponse> userAddressResponses = new ArrayList<>();
        for(Address address : addresses)
        {
            UserAddressResponse userAddressResponse = initializeUserAddressResponse(address);
            userAddressResponses.add(userAddressResponse);
        }
        return userAddressResponses;
    }

    public Optional<Address> findById(UUID uuid) {
        return addressRepository.findById(uuid);
    }

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

    public List<Address> getAllAddressesForUser(User user) {
        return addressRepository.findAllByUser(user);
    }

    public boolean canAddMoreAddresses(User user) {
        return addressRepository.countByUser(user) < MAX_ADDRESSES_PER_USER;
    }

    public UserAddressResponse getAddressResponse(String editAddressId, User user) {
        Address address = getAddressByIdAndUser(editAddressId, user);
        return initializeUserAddressResponse(address);
    }

    public void deleteAddress(User user, String id) {
        UUID addressId = UUID.fromString(id);
        Address address = addressRepository.findAddressByIdAndUser(addressId, user)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with id: " + id));

        addressRepository.delete(address);
        log.info("Successfully deleted address {} for user: {}", id, user.getEmail());
    }

    public void addAddressIfPresent(User user, AddressRequest request) {
        if (request == null) {
            return;
        }
        boolean hasCity = hasText(request.getCity());
        boolean hasStreet = hasText(request.getStreet());

        if (!hasCity || !hasStreet) {
            throw new IllegalArgumentException("City and street are required when adding address.");
        }

        Address address = initializeAddressForUser(user, request);
        addressRepository.save(address);
    }

    public AddressRequest initializeAddressEditRequest(UserAddressResponse addressResponse)
    {
        return AddressRequest.builder()
                .city(addressResponse.getCity())
                .building(addressResponse.getBuilding())
                .street(addressResponse.getStreet())
                .isDefault(addressResponse.isDefault())
                .build();
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

    private Address initializeAddressForUser(User user, AddressRequest addressRequest) {
        return Address.builder()
                .user(user)
                .city(addressRequest.getCity())
                .street(addressRequest.getStreet())
                .building(addressRequest.getBuilding())
                .isDefault(false)
                .build();
    }

    private Address getAddressByIdAndUser(String editAddressId, User user) {
        UUID uuid = UUID.fromString(editAddressId);
        Optional<Address> addressOptional = addressRepository.findAddressByIdAndUser(uuid, user);
        if (addressOptional.isEmpty()) {
            throw new IllegalArgumentException("Address not found with id: " + editAddressId);
        }
        return addressOptional.get();
    }
}