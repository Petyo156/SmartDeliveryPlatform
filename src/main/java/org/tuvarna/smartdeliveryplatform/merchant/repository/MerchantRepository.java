package org.tuvarna.smartdeliveryplatform.merchant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Optional<Merchant> getMerchantByUser_Email(String searchEmail);
    List<Merchant> findAllByIsActiveTrueAndTypeOrderByIsClosedAscCreatedAtDesc(MerchantType type);
    boolean existsMerchantBySlug(String slug);
}