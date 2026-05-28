package org.tuvarna.smartdeliveryplatform.merchant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Merchant> findTop3ByIsActiveTrueAndTypeOrderByIsClosedAscCreatedAtDesc(MerchantType type);

    boolean existsMerchantBySlug(String slug);

    @Query("""
        SELECT DISTINCT m
        FROM Merchant m
        JOIN m.products p
        JOIN p.category c
        WHERE m.isActive = true
        AND m.type = :type
        AND c.name = :category
        AND c.isGlobal = true
        AND c.isDeleted = false
        AND p.isAvailable = true
        AND p.isDeleted = false
    """)
    List<Merchant> findMerchantsByCategory(
            @Param("type") MerchantType type,
            @Param("category") String category
    );

    @Query("""
        SELECT m
        FROM Merchant m
        WHERE m.isActive = true
        AND m.type = :type
        AND LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY m.isClosed ASC, m.createdAt DESC
    """)
    List<Merchant> findMerchantsByNameLike(
            @Param("query") String query,
            @Param("type") MerchantType type
    );

    @Query("""
        SELECT m
        FROM Merchant m
        WHERE m.isActive = true
        AND LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY m.isClosed ASC, m.createdAt DESC
    """)
    List<Merchant> findMerchantsByNameLikeAllTypes(@Param("query") String query);

}