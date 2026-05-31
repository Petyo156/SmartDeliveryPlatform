package org.tuvarna.smartdeliveryplatform.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tuvarna.smartdeliveryplatform.cart.model.Cart;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUser_Email(String email);
}