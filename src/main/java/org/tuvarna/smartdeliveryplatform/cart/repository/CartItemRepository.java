package org.tuvarna.smartdeliveryplatform.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tuvarna.smartdeliveryplatform.cart.model.Cart;
import org.tuvarna.smartdeliveryplatform.cart.model.CartItem;
import org.tuvarna.smartdeliveryplatform.product.model.Product;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    Optional<CartItem> findByIdAndCart_User_Email(UUID id, String email);
}
