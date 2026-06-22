package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tuvarna.smartdeliveryplatform.merchant.service.MerchantService;
import org.tuvarna.smartdeliveryplatform.category.service.CategoryService;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantCardResponse;

import java.util.List;

@Controller
@RequestMapping("/")
public class IndexController {
    private final MerchantService merchantService;
    private final CategoryService categoryService;

    public IndexController(MerchantService merchantService, CategoryService categoryService) {
        this.merchantService = merchantService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String getHomePage(Model model) {
        List<MerchantCardResponse> shopsMerchants = merchantService.getTopActiveShops();
        List<MerchantCardResponse> restaurantMerchants = merchantService.getTopActiveRestaurants();

        model.addAttribute("shopsMerchants", shopsMerchants);
        model.addAttribute("restaurantMerchants", restaurantMerchants);
        return "home/index";
    }

    @GetMapping("/shops")
    public String getShopsPage(@RequestParam(required = false) String category, Model model) {
        List<MerchantCardResponse> merchantCardResponses = merchantService.getAllActiveShops(category);

        model.addAttribute("merchantCardResponses", merchantCardResponses);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categoryPill", categoryService.getGlobalShopCategories());
        return "home/shops";
    }

    @GetMapping("/restaurants")
    public String getRestaurantsPage(@RequestParam(required = false) String category, Model model) {
        List<MerchantCardResponse> merchantCardResponses = merchantService.getAllActiveRestaurants(category);

        model.addAttribute("merchantCardResponses", merchantCardResponses);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categoryPill", categoryService.getGlobalRestaurantCategories());
        return "home/restaurants";
    }
}
