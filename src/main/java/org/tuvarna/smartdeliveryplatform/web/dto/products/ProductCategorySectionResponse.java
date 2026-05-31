package org.tuvarna.smartdeliveryplatform.web.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategorySectionResponse {
    private String categoryName;

    private List<ProductResponse> products;
}
