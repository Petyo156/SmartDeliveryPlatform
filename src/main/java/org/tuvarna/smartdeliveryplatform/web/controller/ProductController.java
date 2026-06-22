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
import org.tuvarna.smartdeliveryplatform.web.dto.admin.MerchantResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.category.CategoryRequest;
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

    public ProductController(ProductService productService, CategoryService categoryService, 
                             MerchantService merchantService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.merchantService = merchantService;
    }

    @GetMapping
    public ModelAndView getProductsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        return initializeProductsPage(authenticationMetadata, ProductRequest.builder().build(), null);
    }

    @GetMapping("/{slug}/edit")
    public ModelAndView getEditProductPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                           @PathVariable String slug) {
        ProductRequest productRequest = productService.getProductRequestForEdit(authenticationMetadata.getUsername(), slug);
        return initializeProductsPage(authenticationMetadata, productRequest, slug);
    }

    private ModelAndView initializeProductsPage(AuthenticationMetadata authenticationMetadata,
                                                ProductRequest productRequest,
                                                String editProductSlug) {
        ModelAndView modelAndView = new ModelAndView("merchant/products");

        MerchantResponse merchantResponse = merchantService.getMerchantResponse(authenticationMetadata.getUsername());
        List<ProductResponse> products = productService.getMerchantProductResponses(authenticationMetadata.getUsername());
        List<CategoryResponse> globalCategories = categoryService.getGlobalAvailableCategories(authenticationMetadata.getUsername());
        List<CategoryResponse> merchantCategories = categoryService.getMerchantAvailableCategories(authenticationMetadata.getUsername());

        modelAndView.addObject("merchantResponse", merchantResponse);
        modelAndView.addObject("products", products);
        modelAndView.addObject("globalCategories", globalCategories);
        modelAndView.addObject("merchantCategories", merchantCategories);
        modelAndView.addObject("productRequest", productRequest);
        modelAndView.addObject("categoryRequest", CategoryRequest.builder().build());
        modelAndView.addObject("editProductSlug", editProductSlug);

        return modelAndView;
    }

    @PostMapping("/{slug}/edit")
    public ModelAndView updateProduct(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                      @PathVariable String slug,
                                      @Valid @ModelAttribute("productRequest") ProductRequest request,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return initializeProductsPage(authenticationMetadata, request, slug);
        }

        productService.updateProduct(authenticationMetadata.getUsername(), slug, request);
        redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
        return new ModelAndView("redirect:/products");
    }

    @PostMapping("/create")
    public ModelAndView createProduct(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                      @Valid @ModelAttribute("productRequest") ProductRequest request,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return initializeProductsPage(authenticationMetadata, request, null);
        }

        productService.createProduct(authenticationMetadata.getUsername(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Product created successfully!");
        return new ModelAndView("redirect:/products");
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
