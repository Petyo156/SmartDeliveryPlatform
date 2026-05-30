package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.product.service.ProductService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.products.ProductCategorySectionResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantPageResponse;

import java.util.List;

@Controller
@RequestMapping("/merchant")
public class MerchantController {
    private final UserService userService;
    private final MerchantService merchantService;
    private final ProductService productService;

    public MerchantController(UserService userService, MerchantService merchantService, ProductService productService) {
        this.userService = userService;
        this.merchantService = merchantService;
        this.productService = productService;
    }

    @GetMapping("/{slug}")
    public ModelAndView getMerchantPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                        @PathVariable String slug,
                                        @RequestParam(required = false) String category) {
        ModelAndView modelAndView = new ModelAndView("home/merchant-page");
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        MerchantPageResponse merchant = merchantService.getMerchantPageBySlug(slug);
        List<ProductCategorySectionResponse> productSections = productService.getAvailableProductSectionsForMerchantSlug(slug, category);
        List<String> categoryNavigation = productService.getAvailableCategoryNamesForMerchantSlug(slug);

        modelAndView.addObject("user", user);
        modelAndView.addObject("merchant", merchant);
        modelAndView.addObject("productSections", productSections);
        modelAndView.addObject("categoryNavigation", categoryNavigation);
        modelAndView.addObject("selectedCategory", category);
        return modelAndView;
    }
}
