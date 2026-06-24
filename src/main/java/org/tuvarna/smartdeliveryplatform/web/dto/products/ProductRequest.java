package org.tuvarna.smartdeliveryplatform.web.dto.products;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.shared.constants.ValidationMessages;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = ValidationMessages.PRODUCT_NAME_REQUIRED)
    private String name;

    private String description;

    @NotNull(message = ValidationMessages.PRICE_REQUIRED)
    @DecimalMin(value = "0.01", message = ValidationMessages.PRICE_MIN)
    private BigDecimal price;

    @NotNull(message = ValidationMessages.CATEGORY_REQUIRED)
    private UUID categoryId;

    private String imageUrl;
}
