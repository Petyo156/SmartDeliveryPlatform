package org.tuvarna.smartdeliveryplatform.web.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantCardResponse;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchResponse {

    private List<MerchantCardResponse> merchantMatches;

    private List<MerchantCardResponse> productMatches;

    private Boolean hasQuery;

    private Boolean hasMerchantMatches;

    private Boolean hasProductMatches;

    private Boolean hasResults;
}

