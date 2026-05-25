package org.tuvarna.smartdeliveryplatform.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.tuvarna.smartdeliveryplatform.category.service.CategoryService;
import org.tuvarna.smartdeliveryplatform.security.AuthenticationMetadata;

@Controller
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public String createCategory(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                 @RequestParam String categoryName,
                                 RedirectAttributes redirectAttributes) {
        categoryService.createMerchantCategory(authenticationMetadata, categoryName);
        redirectAttributes.addFlashAttribute("successMessage", "Category created successfully!");
        return "redirect:/products";
    }

    @PostMapping("/{categoryId}/delete")
    public String deleteCategory(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                 @PathVariable String categoryId,
                                 RedirectAttributes redirectAttributes) {
        categoryService.deleteMerchantCategory(authenticationMetadata, categoryId);
        redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
        return "redirect:/products";
    }
}
