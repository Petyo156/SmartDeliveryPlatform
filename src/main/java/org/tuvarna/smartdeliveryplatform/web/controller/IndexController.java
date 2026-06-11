package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
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

    @GetMapping()
    public ModelAndView getHomePage() {
        ModelAndView modelAndView = new ModelAndView("home/index");
        List<MerchantCardResponse> shopsMerchants = merchantService.getTopActiveShops();
        List<MerchantCardResponse> restaurantMerchants = merchantService.getTopActiveRestaurants();

        modelAndView.addObject("shopsMerchants", shopsMerchants);
        modelAndView.addObject("restaurantMerchants", restaurantMerchants);
        return modelAndView;
    }

    @GetMapping("/shops")
    public ModelAndView getShopsPage(@RequestParam(required = false) String category) {
        ModelAndView modelAndView = new ModelAndView("home/shops");
        List<MerchantCardResponse> merchantCardResponses = merchantService.getAllActiveShops(category);

        modelAndView.addObject("merchantCardResponses", merchantCardResponses);
        modelAndView.addObject("selectedCategory", category);
        modelAndView.addObject("categoryPill", categoryService.getGlobalShopCategories());
        return modelAndView;
    }

    @GetMapping("/restaurants")
    public ModelAndView getRestaurantsPage(@RequestParam(required = false) String category) {
        ModelAndView modelAndView = new ModelAndView("home/restaurants");
        List<MerchantCardResponse> merchantCardResponses = merchantService.getAllActiveRestaurants(category);

        modelAndView.addObject("merchantCardResponses", merchantCardResponses);
        modelAndView.addObject("selectedCategory", category);
        modelAndView.addObject("categoryPill", categoryService.getGlobalRestaurantCategories());
        return modelAndView;
    }
}
