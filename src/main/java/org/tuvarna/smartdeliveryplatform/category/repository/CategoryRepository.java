package org.tuvarna.smartdeliveryplatform.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tuvarna.smartdeliveryplatform.category.model.Category;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    
    boolean existsByNameAndType(String name, MerchantType type);
    
    List<Category> findAllByIsGlobalTrueAndTypeAndIsDeletedFalse(MerchantType type);
    
    List<Category> findAllByMerchantAndIsDeletedFalse(Merchant merchant);
    
    boolean existsByNameIgnoreCaseAndMerchantAndIsDeletedFalse(String name, Merchant merchant);
}