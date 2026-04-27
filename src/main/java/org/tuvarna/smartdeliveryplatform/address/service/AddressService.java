package org.tuvarna.smartdeliveryplatform.address.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.address.repository.AddressRepository;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;

import java.util.UUID;

@Service
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public Address addAddress(User user, String city, String street, String building, Double lat, Double lng, Boolean isDefault) {
        log.info("Adding address for user: {}", user.getEmail());

        Address address = Address.builder()
                .user(user)
                .city(city)
                .street(street)
                .building(building)
                .lat(lat)
                .lng(lng)
                .isDefault(isDefault != null && isDefault)
                .build();

        if (address.getIsDefault()) {
            unsetPreviousDefaultAddress(user);
        }

        addressRepository.save(address);
        log.info("Successfully added address for user: {}", user.getEmail());

        return address;
    }

    public Address setDefaultAddress(User user, UUID addressId) {
        log.info("Setting default address for user: {}", user.getEmail());

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address with id " + addressId + " not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Address does not belong to the authenticated user");
        }

        unsetPreviousDefaultAddress(user);

        address.setIsDefault(address.getIsDefault());
        addressRepository.save(address);

        log.info("Successfully set default address for user: {}", user.getEmail());
        return address;
    }

    private void unsetPreviousDefaultAddress(User user) {
        addressRepository.findByUserAndIsDefaultTrue(user).ifPresent(existingDefault -> {
            existingDefault.setIsDefault(false);
            addressRepository.save(existingDefault);
            log.info("Unset previous default address for user: {}", user.getEmail());
        });
    }

    public Address initializeAddressForUser(User user, AddressRequest addressRequest) {
        return Address.builder()
                .user(user)
                .city(addressRequest.getCity())
                .street(addressRequest.getStreet())
                .building(addressRequest.getBuilding())
                .isDefault(true)
                .build();
    }
}