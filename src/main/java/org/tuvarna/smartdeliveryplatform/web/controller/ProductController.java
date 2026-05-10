package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.category.service.CategoryService;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.product.service.ProductService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.category.CategoryResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductResponse;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final MerchantService merchantService;
    private final UserService userService;

    public ProductController(ProductService productService, CategoryService categoryService, 
                             MerchantService merchantService, UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.merchantService = merchantService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getProductsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView("merchant/products");

        User user = userService.getUserByEmail(authenticationMetadata.getUsername());
        MerchantResponse merchantResponse = merchantService.getMerchantResponse(authenticationMetadata.getUsername());
        Boolean merchantIsClosed = merchantService.merchantIsClosedStatus(authenticationMetadata);
        List<ProductResponse> products = productService.getMerchantProductResponses(authenticationMetadata.getUsername());
        List<CategoryResponse> availableCategories = categoryService.getAvailableCategories(authenticationMetadata.getUsername());

        modelAndView.addObject("user", user);
        modelAndView.addObject("merchantResponse", merchantResponse);
        modelAndView.addObject("merchantIsClosed", merchantIsClosed);
        modelAndView.addObject("products", products);
        modelAndView.addObject("availableCategories", availableCategories);
        modelAndView.addObject("productRequest", ProductRequest.builder().build());
        modelAndView.addObject("categoryName", "");

        return modelAndView;
    }

    @PostMapping("/create")
    public String createProduct(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                @Valid @ModelAttribute ProductRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.productRequest", bindingResult);
            redirectAttributes.addFlashAttribute("productRequest", request);
            return "redirect:/products";
        }

        productService.createProduct(authenticationMetadata, request);
        redirectAttributes.addFlashAttribute("successMessage", "Product created successfully!");
        return "redirect:/products";
    }

    @PostMapping("/{productId}/delete")
    public String deleteProduct(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                @PathVariable String productId,
                                RedirectAttributes redirectAttributes) {
        productService.deleteProduct(productId, authenticationMetadata);
        redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
        return "redirect:/products";
    }

    @PostMapping("/{productId}/toggle")
    public String toggleAvailability(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                     @PathVariable String productId,
                                     RedirectAttributes redirectAttributes) {
        productService.toggleAvailability(productId, authenticationMetadata);
        redirectAttributes.addFlashAttribute("successMessage", "Product availability updated!");
        return "redirect:/products";
    }
}
