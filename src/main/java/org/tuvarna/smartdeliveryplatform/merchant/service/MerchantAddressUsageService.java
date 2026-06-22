package org.tuvarna.smartdeliveryplatform.merchant.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.merchant.repository.MerchantRepository;
import org.tuvarna.smartdeliveryplatform.user.model.User;

import java.util.UUID;

@Service
public class MerchantAddressUsageService {
    private final MerchantRepository merchantRepository;

    public MerchantAddressUsageService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Transactional(readOnly = true)
    public boolean isMerchantProfileAddress(User user, UUID addressId) {
        return merchantRepository.existsByAddress_IdAndUser(addressId, user);
    }
}
