package org.tuvarna.smartdeliveryplatform.cart.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tuvarna.smartdeliveryplatform.cart.model.Cart;
import org.tuvarna.smartdeliveryplatform.cart.model.CartItem;
import org.tuvarna.smartdeliveryplatform.cart.repository.CartItemRepository;
import org.tuvarna.smartdeliveryplatform.cart.repository.CartRepository;
import org.tuvarna.smartdeliveryplatform.exception.CartMerchantConflictException;
import org.tuvarna.smartdeliveryplatform.exception.CartOperationException;
import org.tuvarna.smartdeliveryplatform.product.model.Product;
import org.tuvarna.smartdeliveryplatform.product.repository.ProductRepository;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.web.dto.cart.AddCartItemRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.cart.CartItemResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.cart.CartResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.cart.UpdateCartItemQuantityRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public Cart initializeCartForUser(User user) {
        return Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();
    }

    @Transactional
    public CartResponse getCartResponse(User user) {
        Cart cart = getCartForUser(user);
        return toCartResponse(cart);
    }

    @Transactional
    public void addProductToCart(User user, AddCartItemRequest request) {
        int quantity = validateQuantity(request.getQuantity());
        Cart cart = getCartForUser(user);
        Product product = getProductBySlug(request.getProductSlug());

        validateSingleMerchantCart(cart, product);
        validateProductCanBeAdded(product);

        Optional<CartItem> cartItemOptional = cartItemRepository.findByCartAndProduct(cart, product);
        if(cartItemOptional.isEmpty()) {
            addItemToCart(cart, product, quantity);
        } else {
            increaseQuantity(cartItemOptional.get(), quantity);
        }

        log.info("Added product {} to cart for {}", product.getName(), user.getEmail());
    }

    @Transactional
    public void clearCartAndAddProduct(User user, AddCartItemRequest request) {
        int quantity = validateQuantity(request.getQuantity());

        Cart cart = getCartForUser(user);
        Product product = getProductBySlug(request.getProductSlug());

        validateProductCanBeAdded(product);
        clearCart(cart);
        addItemToCart(cart, product, quantity);
        log.info("Cleared cart and added product {} for {}", product.getName(), user.getEmail());
    }

    @Transactional
    public void updateItemQuantity(User user, UUID itemId, UpdateCartItemQuantityRequest request) {
        int quantity = validateQuantity(request.getQuantity());
        CartItem cartItem = getCartItemForUser(user.getEmail(), itemId);

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        log.info("Updated cart item {} quantity to {}", itemId, quantity);
    }

    @Transactional
    public void removeItem(User user, UUID itemId) {
        CartItem cartItem = getCartItemForUser(user.getEmail(), itemId);

        cartItemRepository.delete(cartItem);
        log.info("Removed cart item {}", itemId);
    }

    private Product getProductBySlug(String slug) {
        Optional<Product> productOptional = productRepository.findBySlug(slug);
        if (productOptional.isEmpty()) {
            throw new CartOperationException("Product was not found.");
        }
        return productOptional.get();
    }

    private Cart getCartForUser(User user) {
        Optional<Cart> cartOptional = cartRepository.findByUser_Email(user.getEmail());
        if (cartOptional.isEmpty()) {
            throw new CartOperationException("Cart was not found for user.");
        }
        return cartOptional.get();
    }

    private CartItem getCartItemForUser(String userEmail, UUID itemId) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findByIdAndCart_User_Email(itemId, userEmail);
        if (cartItemOptional.isEmpty()) {
            throw new CartOperationException("Cart item was not found.");
        }
        return cartItemOptional.get();
    }

    private int validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new CartOperationException("Quantity must be at least 1.");
        }
        return quantity;
    }

    private void validateProductCanBeAdded(Product product) {
        if (!Boolean.TRUE.equals(product.getIsAvailable()) || Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new CartOperationException("This product is not available.");
        }

        if (!Boolean.TRUE.equals(product.getMerchant().getIsActive())) {
            throw new CartOperationException("This merchant is not available.");
        }

        if (Boolean.TRUE.equals(product.getMerchant().getIsClosed())) {
            throw new CartOperationException("This merchant is currently closed.");
        }
    }

    private void validateSingleMerchantCart(Cart cart, Product product) {
        if (hasDifferentMerchant(cart, product)) {
            throw new CartMerchantConflictException("Your cart contains items from another merchant. You can only order from one merchant at a time.");
        }
    }

    private boolean hasDifferentMerchant(Cart cart, Product product) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return false;
        }

        String cartMerchantSlug = cart.getItems().getFirst().getProduct().getMerchant().getSlug();
        return !cartMerchantSlug.equals(product.getMerchant().getSlug());
    }

    private void increaseQuantity(CartItem cartItem, int quantity) {
        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItemRepository.save(cartItem);
    }

    private void addItemToCart(Cart cart, Product product, int quantity) {
        CartItem cartItem = initializeCartItem(cart, product, quantity);

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        cart.getItems().add(cartItem);
        cartItemRepository.save(cartItem);
    }

    private void clearCart(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return;
        }

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
    }

    private CartResponse toCartResponse(Cart cart) {
        if(cart.getItems() == null || cart.getItems().isEmpty()) {
            return initializeCartResponse(cart, null, null, List.of(), BigDecimal.ZERO);
        }

        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toCartItemResponse)
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getLineSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Product firstProduct = cart.getItems().getFirst().getProduct();
        String merchantName = firstProduct.getMerchant().getName();
        Boolean merchantIsClosed = firstProduct.getMerchant().getIsClosed();
        return initializeCartResponse(cart, merchantName, merchantIsClosed, items, total);
    }

    private CartResponse initializeCartResponse(Cart cart, String merchantName, Boolean merchantIsClosed, List<CartItemResponse> items, BigDecimal total) {
        return CartResponse.builder()
                .id(cart.getId())
                .merchantName(merchantName)
                .merchantIsClosed(merchantIsClosed)
                .items(items)
                .total(total)
                .empty(items.isEmpty())
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        BigDecimal unitPrice = item.getProduct().getPrice();
        BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        return initializeCartItemResponse(item, unitPrice, lineSubtotal);
    }

    private CartItem initializeCartItem(Cart cart, Product product, int quantity) {
        return CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();
    }

    private CartItemResponse initializeCartItemResponse(CartItem item, BigDecimal unitPrice, BigDecimal lineSubtotal) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productSlug(item.getProduct().getSlug())
                .productName(item.getProduct().getName())
                .productImageUrl(item.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .lineSubtotal(lineSubtotal)
                .build();
    }
}
