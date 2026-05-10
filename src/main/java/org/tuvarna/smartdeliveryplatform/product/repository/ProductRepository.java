package org.tuvarna.smartdeliveryplatform.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tuvarna.smartdeliveryplatform.category.model.Category;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.product.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    List<Product> findAllByMerchantAndIsDeletedFalse(Merchant merchant);
    
    Optional<Product> findByIdAndMerchant(UUID id, Merchant merchant);

    boolean existsByCategoryAndIsDeletedFalse(Category category);
}