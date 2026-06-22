package org.tuvarna.smartdeliveryplatform.web.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.category.service.CategoryService;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.product.service.ProductService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.category.CategoryRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.category.CategoryResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductRequest;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductResponse;
import org.tuvarna.smartdeliveryplatform.web.util.FlashValidationAttributes;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final MerchantService merchantService;
    private final FlashValidationAttributes flashValidationAttributes;

    public ProductController(ProductService productService, CategoryService categoryService, 
                             MerchantService merchantService,
                             FlashValidationAttributes flashValidationAttributes) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.merchantService = merchantService;
        this.flashValidationAttributes = flashValidationAttributes;
    }

    @GetMapping
    public String getProductsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                  Model model) {
        return initializeProductsPage(authenticationMetadata, ProductRequest.builder().build(), null, model);
    }

    @GetMapping("/{slug}/edit")
    public String getEditProductPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                     @PathVariable String slug,
                                     Model model) {
        ProductRequest productRequest = productService.getProductRequestForEdit(authenticationMetadata.getUsername(), slug);
        return initializeProductsPage(authenticationMetadata, productRequest, slug, model);
    }

    private String initializeProductsPage(AuthenticationMetadata authenticationMetadata,
                                          ProductRequest productRequest,
                                          String editProductSlug,
                                          Model model) {
        MerchantResponse merchantResponse = merchantService.getMerchantResponse(authenticationMetadata.getUsername());
        List<ProductResponse> products = productService.getMerchantProductResponses(authenticationMetadata.getUsername());
        List<CategoryResponse> globalCategories = categoryService.getGlobalAvailableCategories(authenticationMetadata.getUsername());
        List<CategoryResponse> merchantCategories = categoryService.getMerchantAvailableCategories(authenticationMetadata.getUsername());

        model.addAttribute("merchantResponse", merchantResponse);
        model.addAttribute("products", products);
        model.addAttribute("globalCategories", globalCategories);
        model.addAttribute("merchantCategories", merchantCategories);
        flashValidationAttributes.addModelAttributeIfMissing(model, "productRequest", productRequest);
        model.addAttribute("categoryRequest", CategoryRequest.builder().build());
        model.addAttribute("editProductSlug", editProductSlug);

        return "merchant/products";
    }

    @PostMapping("/{slug}/edit")
    public String updateProduct(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                @PathVariable String slug,
                                @Valid @ModelAttribute("productRequest") ProductRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            flashValidationAttributes.addValidationFlashAttribute(redirectAttributes, "productRequest", request, bindingResult);
            return "redirect:/products/" + slug + "/edit";
        }

        productService.updateProduct(authenticationMetadata.getUsername(), slug, request);
        redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
        return "redirect:/products";
    }

    @PostMapping("/create")
    public String createProduct(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                @Valid @ModelAttribute("productRequest") ProductRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            flashValidationAttributes.addValidationFlashAttribute(redirectAttributes, "productRequest", request, bindingResult);
            return "redirect:/products";
        }

        productService.createProduct(authenticationMetadata.getUsername(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Product created successfully!");
        return "redirect:/products";
    }

    @PostMapping("/{slug}/delete")
    public String deleteProduct(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                @PathVariable String slug,
                                RedirectAttributes redirectAttributes) {
        productService.deleteProduct(slug, authenticationMetadata);
        redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
        return "redirect:/products";
    }

    @PostMapping("/{productSlug}/toggle")
    public String toggleAvailability(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                     @PathVariable String productSlug,
                                     RedirectAttributes redirectAttributes) {
        productService.toggleAvailability(productSlug, authenticationMetadata);
        redirectAttributes.addFlashAttribute("successMessage", "Product availability updated!");
        return "redirect:/products";
    }
}
