package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.product.service.ProductService;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductCategorySectionResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantPageResponse;

import java.util.List;

@Controller
@RequestMapping("/merchant")
public class MerchantController {
    private final MerchantService merchantService;
    private final ProductService productService;

    public MerchantController(MerchantService merchantService, ProductService productService) {
        this.merchantService = merchantService;
        this.productService = productService;
    }

    @GetMapping("/{slug}")
    public String getMerchantPage(@PathVariable String slug,
                                  @RequestParam(required = false) String category,
                                  Model model) {
        MerchantPageResponse merchant = merchantService.getMerchantPageBySlug(slug);
        List<ProductCategorySectionResponse> productSections = productService.getAvailableProductSectionsForMerchantSlug(slug, category);
        List<String> categoryNavigation = productService.getAvailableCategoryNamesForMerchantSlug(slug);

        model.addAttribute("merchant", merchant);
        model.addAttribute("productSections", productSections);
        model.addAttribute("categoryNavigation", categoryNavigation);
        model.addAttribute("selectedCategory", category);
        return "home/merchant-page";
    }
}
