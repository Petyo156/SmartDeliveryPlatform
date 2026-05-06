package org.tuvarna.smartdeliveryplatform.address.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.address.repository.AddressRepository;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.web.dto.auth.AddressRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantAddressResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public void addAddress(User user, AddressRequest addressRequest) {
        log.info("Adding address for user: {}", user.getEmail());

        Address address = Address.builder()
                .user(user)
                .city(addressRequest.getCity())
                .street(addressRequest.getStreet())
                .building(addressRequest.getBuilding())
                .build();

        addressRepository.save(address);
        log.info("Successfully added address for user: {}", user.getEmail());
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

    public List<Address> getAllAddressesForUser(User user) {
        return addressRepository.findAllByUser(user);
    }

    public Optional<Address> findById(UUID uuid) {
        return addressRepository.findById(uuid);
    }

    private MerchantAddressResponse initializeMerchantAddressResponse(Address address) {
        return MerchantAddressResponse.builder()
                .id(address.getId())
                .city(address.getCity())
                .street(address.getStreet())
                .building(address.getBuilding())
                .build();
    }
}