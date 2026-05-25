package org.tuvarna.smartdeliveryplatform.category.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.category.model.Category;
import org.tuvarna.smartdeliveryplatform.product.repository.ProductRepository;

@Service
@Slf4j
public class ProductCategoryService {
    private final ProductRepository productRepository;

    public ProductCategoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public boolean categoryHasProducts(Category category) {
        return productRepository.existsByCategoryAndIsDeletedFalse(category);
    }
}
