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
import org.tuvarna.smartdeliveryplatform.exception.ExceptionMessages;
import org.tuvarna.smartdeliveryplatform.exception.SystemOperationException;
import org.tuvarna.smartdeliveryplatform.order.service.OrderPricingService;
import org.tuvarna.smartdeliveryplatform.product.model.Product;
import org.tuvarna.smartdeliveryplatform.product.repository.ProductRepository;
import org.tuvarna.smartdeliveryplatform.shared.enums.UserRole;
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
    private final OrderPricingService orderPricingService;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       OrderPricingService orderPricingService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderPricingService = orderPricingService;
    }

    public Cart initializeCartForUser(User user) {
        return Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();
    }

    @Transactional(readOnly = true)
    public CartResponse getCartResponse(User user) {
        Cart cart = getCartForUser(user);
        return toCartResponse(cart);
    }

    @Transactional
    public void addProductToCart(User user, AddCartItemRequest request) {
        int quantity = validateQuantity(request.getQuantity());
        Cart cart = getCartForUser(user);
        Product product = getProductBySlug(request.getProductSlug());

        validateProductDoesNotBelongToUserMerchant(user, product);
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

        validateProductDoesNotBelongToUserMerchant(user, product);
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

    @Transactional(readOnly = true)
    public List<CartItem> getValidatedCartItemsForCheckout(User user) {
        Cart cart = getCartForUser(user);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CartOperationException(ExceptionMessages.CART_IS_EMPTY);
        }

        List<CartItem> cartItems = cart.getItems();
        validateProductsCanBeOrdered(cartItems);
        return cartItems;
    }

    @Transactional
    public void clearCartItems(User user) {
        Cart cart = getCartForUser(user);
        clearCart(cart);
        cartRepository.saveAndFlush(cart);
        log.info("Cleared user's cart");
    }

    private Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new CartOperationException(ExceptionMessages.PRODUCT_WAS_NOT_FOUND));
    }

    private Cart getCartForUser(User user) {
        return cartRepository.findByUser_Email(user.getEmail())
                .orElseThrow(() -> new SystemOperationException(ExceptionMessages.CART_WAS_NOT_FOUND_FOR_USER));
    }

    private CartItem getCartItemForUser(String userEmail, UUID itemId) {
        return cartItemRepository.findByIdAndCart_User_Email(itemId, userEmail)
                .orElseThrow(() -> new CartOperationException(ExceptionMessages.CART_ITEM_WAS_NOT_FOUND));
    }

    private int validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new CartOperationException(ExceptionMessages.CART_QUANTITY_MUST_BE_AT_LEAST_ONE);
        }
        return quantity;
    }

    private void validateProductCanBeAdded(Product product) {
        if (!Boolean.TRUE.equals(product.getIsAvailable()) || Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new CartOperationException(ExceptionMessages.CART_PRODUCT_IS_NOT_AVAILABLE);
        }

        if (!Boolean.TRUE.equals(product.getMerchant().getIsActive())) {
            throw new CartOperationException(ExceptionMessages.MERCHANT_IS_NOT_AVAILABLE);
        }

        if (Boolean.TRUE.equals(product.getMerchant().getIsClosed())) {
            throw new CartOperationException(ExceptionMessages.MERCHANT_IS_CURRENTLY_CLOSED);
        }
    }

    private void validateProductsCanBeOrdered(List<CartItem> cartItems) {
        boolean unavailableProductExists = cartItems.stream()
                .map(CartItem::getProduct)
                .anyMatch(product -> !Boolean.TRUE.equals(product.getIsAvailable())
                        || Boolean.TRUE.equals(product.getIsDeleted()));

        if (unavailableProductExists) {
            throw new CartOperationException(ExceptionMessages.CART_CONTAINS_UNAVAILABLE_PRODUCT);
        }
    }

    private void validateSingleMerchantCart(Cart cart, Product product) {
        if (hasDifferentMerchant(cart, product)) {
            throw new CartMerchantConflictException(ExceptionMessages.CART_MERCHANT_CONFLICT);
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
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return initializeCartResponse(cart, null, null, List.of(), BigDecimal.ZERO);
        }

        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toCartItemResponse)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getLineSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Product firstProduct = cart.getItems().getFirst().getProduct();
        String merchantName = firstProduct.getMerchant().getName();
        Boolean merchantIsClosed = firstProduct.getMerchant().getIsClosed();
        return initializeCartResponse(cart, merchantName, merchantIsClosed, items, subtotal);
    }

    private CartResponse initializeCartResponse(Cart cart, String merchantName, Boolean merchantIsClosed, List<CartItemResponse> items, BigDecimal subtotal) {
        boolean checkoutAvailable = items.stream()
                .allMatch(item -> Boolean.TRUE.equals(item.getAvailableForCheckout()));
        BigDecimal deliveryFee = items.isEmpty() ? BigDecimal.ZERO : OrderPricingService.DEFAULT_DELIVERY_FEE;
        BigDecimal total = items.isEmpty() ? BigDecimal.ZERO : orderPricingService.calculateTotal(subtotal);

        return CartResponse.builder()
                .id(cart.getId())
                .merchantName(merchantName)
                .merchantIsClosed(merchantIsClosed)
                .items(items)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .total(total)
                .empty(items.isEmpty())
                .checkoutAvailable(checkoutAvailable)
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        BigDecimal unitPrice = item.getProduct().getPrice();
        BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        boolean availableForCheckout = productAvailableForCheckout(item.getProduct());
        String availabilityMessage = availableForCheckout ? null : "Currently unavailable";
        return initializeCartItemResponse(item, unitPrice, lineSubtotal, availableForCheckout, availabilityMessage);
    }

    private CartItem initializeCartItem(Cart cart, Product product, int quantity) {
        return CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();
    }

    private CartItemResponse initializeCartItemResponse(CartItem item, BigDecimal unitPrice, BigDecimal lineSubtotal, boolean availableForCheckout, String availabilityMessage) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productSlug(item.getProduct().getSlug())
                .productName(item.getProduct().getName())
                .productImageUrl(item.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .lineSubtotal(lineSubtotal)
                .availableForCheckout(availableForCheckout)
                .availabilityMessage(availabilityMessage)
                .build();
    }

    private void validateProductDoesNotBelongToUserMerchant(User user, Product product) {
        if (user.getRole() != UserRole.MERCHANT) {
            return;
        }

        if (product.getMerchant().getUser().getEmail().equals(user.getEmail())) {
            throw new CartOperationException(ExceptionMessages.CANNOT_ORDER_FROM_OWN_SHOP);
        }
    }

    private boolean productAvailableForCheckout(Product product) {
        return Boolean.TRUE.equals(product.getIsAvailable()) && !Boolean.TRUE.equals(product.getIsDeleted());
    }
}
