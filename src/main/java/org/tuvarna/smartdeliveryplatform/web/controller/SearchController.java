package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.tuvarna.smartdeliveryplatform.merchant.service.SearchService;
import org.tuvarna.smartdeliveryplatform.web.dto.search.SearchResponse;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ModelAndView searchAll(@RequestParam(required = false) String q) {
        ModelAndView modelAndView = new ModelAndView("home/search-results");
        SearchResponse searchResponse = searchService.searchAll(q);

        modelAndView.addObject("searchResponse", searchResponse);
        modelAndView.addObject("searchQuery", q);

        return modelAndView;
    }

    @GetMapping("/restaurants")
    public ModelAndView searchRestaurants(@RequestParam(required = false) String q) {
        ModelAndView modelAndView = new ModelAndView("home/search-results-restaurants");
        SearchResponse searchResponse = searchService.searchRestaurants(q);

        modelAndView.addObject("searchResponse", searchResponse);
        modelAndView.addObject("searchQuery", q);

        return modelAndView;
    }

    @GetMapping("/shops")
    public ModelAndView searchShops(@RequestParam(required = false) String q) {
        ModelAndView modelAndView = new ModelAndView("home/search-results-shops");
        SearchResponse searchResponse = searchService.searchShops(q);

        modelAndView.addObject("searchResponse", searchResponse);
        modelAndView.addObject("searchQuery", q);

        return modelAndView;
    }
}

