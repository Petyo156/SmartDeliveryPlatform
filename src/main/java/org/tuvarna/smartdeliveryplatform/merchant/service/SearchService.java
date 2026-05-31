package org.tuvarna.smartdeliveryplatform.merchant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.merchant.repository.MerchantRepository;
import org.tuvarna.smartdeliveryplatform.product.repository.ProductRepository;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;
import org.tuvarna.smartdeliveryplatform.web.dto.merchant.MerchantCardResponse;
import org.tuvarna.smartdeliveryplatform.web.dto.search.SearchResponse;

import java.util.*;

@Service
@Slf4j
public class SearchService {

    private final MerchantRepository merchantRepository;
    private final ProductRepository productRepository;

    public SearchService(MerchantRepository merchantRepository, ProductRepository productRepository) {
        this.merchantRepository = merchantRepository;
        this.productRepository = productRepository;
    }

    public SearchResponse searchRestaurants(String query) {
        if (isInvalidQuery(query)) {
            return initializeSearchResponse(new ArrayList<>(), new ArrayList<>());
        }
        return search(query, MerchantType.RESTAURANT);
    }

    public SearchResponse searchShops(String query) {
        if (isInvalidQuery(query)) {
            return initializeSearchResponse(new ArrayList<>(), new ArrayList<>());
        }
        return search(query, MerchantType.SHOP);
    }

    public SearchResponse searchAll(String query) {
        if (isInvalidQuery(query)) {
            return initializeSearchResponse(new ArrayList<>(), new ArrayList<>());
        }
        return searchAllMerchants(query);
    }

    private SearchResponse search(String query, MerchantType type) {
        List<MerchantCardResponse> merchantMatches =
                merchantRepository.findMerchantsByNameLike(query, type)
                        .stream()
                        .map(this::toMerchantCardResponse)
                        .toList();

        List<MerchantCardResponse> productMatches =
                productRepository.findMerchantsByProductNameMatchAndType(query, type)
                        .stream()
                        .map(this::toMerchantCardResponse)
                        .toList();

        log.info("Search found {} merchant matches and {} product matches", merchantMatches.size(), productMatches.size());
        return initializeSearchResponse(merchantMatches, productMatches);
    }

    private SearchResponse searchAllMerchants(String query) {
        List<MerchantCardResponse> merchantMatches =
                merchantRepository.findMerchantsByNameLikeAllTypes(query)
                        .stream()
                        .map(this::toMerchantCardResponse)
                        .toList();

        List<MerchantCardResponse> productMatches =
                productRepository.findMerchantsByProductNameMatch(query)
                        .stream()
                        .map(this::toMerchantCardResponse)
                        .toList();

        log.info("Search found {} merchant matches and {} product matches", merchantMatches.size(), productMatches.size());
        return initializeSearchResponse(merchantMatches, productMatches);
    }

    private SearchResponse initializeSearchResponse(List<MerchantCardResponse> finalMerchantMatches, List<MerchantCardResponse> finalProductMatches) {
        return SearchResponse.builder()
                .merchantMatches(finalMerchantMatches)
                .productMatches(finalProductMatches)
                .build();
    }

    private MerchantCardResponse toMerchantCardResponse(Merchant merchant) {
        return MerchantCardResponse.builder()
                .slug(merchant.getSlug())
                .name(merchant.getName())
                .description(merchant.getDescription())
                .imageUrl(merchant.getImageUrl())
                .type(merchant.getType())
                .isClosed(merchant.getIsClosed())
                .build();
    }

    private boolean isInvalidQuery(String query) {
        return query == null || query.isBlank();
    }
}

