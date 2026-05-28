package org.tuvarna.smartdeliveryplatform.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tuvarna.smartdeliveryplatform.category.model.Category;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.product.model.Product;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    List<Product> findAllByMerchantAndIsDeletedFalse(Merchant merchant);
    
    Optional<Product> findBySlugAndMerchant(String slug, Merchant merchant);

    boolean existsByCategoryAndIsDeletedFalse(Category category);

    @Query("""
    SELECT DISTINCT m
    FROM Product p
    JOIN p.merchant m
    WHERE p.isAvailable = true
      AND p.isDeleted = false
      AND m.isActive = true
      AND m.type = :type
      AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
    ORDER BY m.createdAt DESC
""")
    List<Merchant> findMerchantsByProductNameMatchAndType(
            @Param("query") String query,
            @Param("type") MerchantType type
    );

    @Query("""
    SELECT DISTINCT p.merchant
    FROM Product p
    WHERE p.isAvailable = true
      AND p.isDeleted = false
      AND p.merchant.isActive = true
      AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
    ORDER BY p.merchant.createdAt DESC
""")
    List<Merchant> findMerchantsByProductNameMatch(@Param("query") String query);
}