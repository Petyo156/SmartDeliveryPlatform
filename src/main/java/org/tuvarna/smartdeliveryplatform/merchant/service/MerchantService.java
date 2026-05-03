package org.tuvarna.smartdeliveryplatform.merchant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.merchant.repository.MerchantRepository;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantResponse;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class MerchantService {
    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public void createMerchantForUser(User user, MerchantRequest merchantRequest) {
        Merchant merchant = Merchant.builder()
                .user(user)
                .name(merchantRequest.getName())
                .description(merchantRequest.getDescription())
                .type(merchantRequest.getType())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        merchantRepository.save(merchant);
    }

    public MerchantResponse getMerchant(String searchEmail) {
        Optional<Merchant> merchantOptional = getMerchantByUserEmail(searchEmail);
        if(merchantOptional.isEmpty()) {
            return MerchantResponse.builder().build();
        }

        Merchant merchant = merchantOptional.get();
        return MerchantResponse.builder()
                .ownerEmail(searchEmail)
                .name(merchant.getName())
                .type(merchant.getType())
                .description(merchant.getDescription())
                .address(merchant.getAddress())
                .isActive(merchant.getIsActive())
                .createdAt(merchant.getCreatedAt())
                .build();
    }

    public Optional<Merchant> getMerchantByUserEmail(String email) {
        return merchantRepository.getMerchantByUser_Email(email);
    }

    public void toggleMerchantStatus(String email) {
        Merchant merchant = merchantRepository.getMerchantByUser_Email(email)
                .orElseThrow(() -> new IllegalStateException("Merchant with email '" + email + "' does not exist"));
        merchant.setIsActive(!merchant.getIsActive());
        merchantRepository.save(merchant);
        log.info("Toggled status for merchant {} to {}", email, merchant.getIsActive());
    }

}