package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tuvarna.smartdeliveryplatform.category.service.CategoryService;
import org.tuvarna.smartdeliveryplatform.merchant.service.SearchService;
import org.tuvarna.smartdeliveryplatform.web.dto.search.SearchResponse;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;
    private final CategoryService categoryService;

    public SearchController(SearchService searchService, CategoryService categoryService) {
        this.searchService = searchService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String searchAll(@RequestParam(required = false) String q, Model model) {
        SearchResponse searchResponse = searchService.searchAll(q);

        model.addAttribute("searchResponse", searchResponse);
        model.addAttribute("searchQuery", q);

        return "home/search-results";
    }

    @GetMapping("/restaurants")
    public String searchRestaurants(@RequestParam(required = false) String q, Model model) {
        SearchResponse searchResponse = searchService.searchRestaurants(q);

        model.addAttribute("searchResponse", searchResponse);
        model.addAttribute("searchQuery", q);
        model.addAttribute("selectedCategory", null);
        model.addAttribute("categoryPill", categoryService.getGlobalRestaurantCategories());

        return "home/search-results-restaurants";
    }

    @GetMapping("/shops")
    public String searchShops(@RequestParam(required = false) String q, Model model) {
        SearchResponse searchResponse = searchService.searchShops(q);

        model.addAttribute("searchResponse", searchResponse);
        model.addAttribute("searchQuery", q);
        model.addAttribute("selectedCategory", null);
        model.addAttribute("categoryPill", categoryService.getGlobalShopCategories());

        return "home/search-results-shops";
    }
}
