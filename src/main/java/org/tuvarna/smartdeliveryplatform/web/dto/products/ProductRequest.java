package org.tuvarna.smartdeliveryplatform.web.dto.products;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name is required.")
    private String name;

    private String description;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01.")
    private BigDecimal price;

    @NotNull(message = "Category is required.")
    private UUID categoryId;

    private String imageUrl;
}
