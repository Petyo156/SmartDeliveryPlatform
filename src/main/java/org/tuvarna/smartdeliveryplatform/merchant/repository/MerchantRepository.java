package org.tuvarna.smartdeliveryplatform.merchant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Optional<Merchant> getMerchantByUser_Email(String searchEmail);
    Optional<Merchant> getMerchantByUser_Id(UUID id);
}