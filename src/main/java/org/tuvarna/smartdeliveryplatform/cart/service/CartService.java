package org.tuvarna.smartdeliveryplatform.cart.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.cart.model.Cart;
import org.tuvarna.smartdeliveryplatform.cart.repository.CartRepository;
import org.tuvarna.smartdeliveryplatform.user.model.User;

@Service
@Slf4j
public class CartService {
    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart initializeCartForUser(User user) {
        return Cart.builder()
                .user(user)
                .build();
    }
}