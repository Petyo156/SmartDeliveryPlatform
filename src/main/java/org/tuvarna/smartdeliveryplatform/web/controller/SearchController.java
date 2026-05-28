package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.tuvarna.smartdeliveryplatform.search.service.SearchService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.service.UserService;
import org.tuvarna.smartdeliveryplatform.web.dto.search.SearchResponse;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;
    private final UserService userService;

    public SearchController(SearchService searchService, UserService userService) {
        this.searchService = searchService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView searchAll(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                  @RequestParam(required = false) String q) {
        ModelAndView modelAndView = new ModelAndView("home/search-results");
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        SearchResponse searchResponse = searchService.searchAll(q);

        modelAndView.addObject("user", user);
        modelAndView.addObject("searchResponse", searchResponse);
        modelAndView.addObject("searchQuery", q);

        return modelAndView;
    }

    @GetMapping("/restaurants")
    public ModelAndView searchRestaurants(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                          @RequestParam(required = false) String q) {
        ModelAndView modelAndView = new ModelAndView("home/search-results-restaurants");
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        SearchResponse searchResponse = searchService.searchRestaurants(q);

        modelAndView.addObject("user", user);
        modelAndView.addObject("searchResponse", searchResponse);
        modelAndView.addObject("searchQuery", q);

        return modelAndView;
    }

    @GetMapping("/shops")
    public ModelAndView searchShops(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                    @RequestParam(required = false) String q) {
        ModelAndView modelAndView = new ModelAndView("home/search-results-shops");
        User user = userService.getAuthenticatedUser(authenticationMetadata);
        SearchResponse searchResponse = searchService.searchShops(q);

        modelAndView.addObject("user", user);
        modelAndView.addObject("searchResponse", searchResponse);
        modelAndView.addObject("searchQuery", q);

        return modelAndView;
    }
}

