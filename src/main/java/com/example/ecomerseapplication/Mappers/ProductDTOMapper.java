package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.DTOs.serverDtos.CartItemDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.CompactProductDto;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.*;
import com.example.ecomerseapplication.Entities.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDTOMapper {

    public static CartSummaryResponse summaryItemsToResponse(List<CartSummaryItem> summaryItems) {

        long totalPrice = 0L;
        long totalQuantity = 0;

        for (CartSummaryItem summaryItem : summaryItems) {
            totalQuantity += summaryItem.getQuantity();

            Short discountPercent = summaryItem.getDiscountPercent();
            Short overrideDiscountPercentage = summaryItem.getOverrideDiscountPercentage();
            int originalPrice = summaryItem.getOriginalPriceStotinki();

            totalPrice += calculateDiscountPrice(
                    originalPrice,
                    discountPercent,
                    overrideDiscountPercentage);
        }

        return new CartSummaryResponse(totalPrice, totalQuantity);

    }

    public static List<CartItemResponse> cartItemtListToCartItemResponseList(List<CartItemDTO> cartItems) {
        return cartItems.stream().map(ProductDTOMapper::cartItemToCartResponse).toList();
    }

    public static CartItemResponse cartItemToCartResponse(CartItemDTO cartItemDTO) {

        CompactProductDto compactProductDto = cartItemDTO.compactProductDto();
        CompactProductResponse compactProductResponse = ProductDTOMapper.compactProductToResponse(compactProductDto);

        return new CartItemResponse(compactProductResponse,
                cartItemDTO.dateAdded(),
                cartItemDTO.quantity(),
                cartItemDTO.stockQuantity());
    }

    public static CompactProductResponse entityToCompactResponse(Product product) {

        CompactProductResponse compactProductResponse = new CompactProductResponse();
        int originalPrice = product.getOriginalPriceStotinki();
        SaleProduct saleProduct = product.getMainSaleProduct().orElse(null);

        compactProductResponse.productCode = product.getProductCode();
        compactProductResponse.name = product.getProductName();
        compactProductResponse.imageUrl = product.getMainImageUrl();
        compactProductResponse.rating = product.getRating();
        compactProductResponse.originalPriceStotinki = product.getOriginalPriceStotinki();
        compactProductResponse.salePriceStotinki = calculatePriceForDto(saleProduct, originalPrice);
        compactProductResponse.reviewCount = product.getReviews().size();
        compactProductResponse.isInStock = product.isInStock();

        return compactProductResponse;
    }

    public static int calculatePriceForDto(SaleProduct saleProduct, int originalPrice) {
        if (saleProduct != null && saleProduct.getIsMain()) {

            Sale sale = saleProduct.getSale();
            Instant now = Instant.now();
            Short saleDiscount = null;
            Short overrideDiscountPercentage = null;

            if (sale.getIsActive() && sale.getStartDate().isBefore(now) && sale.getEndDate().isAfter(now))
            {
                saleDiscount = sale.getDiscountPercent();
                overrideDiscountPercentage = saleProduct.getOverrideDiscountPercentage();
            }

            return calculateDiscountPrice(originalPrice,
                    saleDiscount,
                    overrideDiscountPercentage);
        } else {
            return originalPrice;
        }
    }

    public static int calculateDiscountPrice(int originalPrice, Short defaultDiscount, Short explicitDiscount) {

        if (defaultDiscount == null)
            return originalPrice;

        if (explicitDiscount != null && !explicitDiscount.equals(defaultDiscount)) {
            return (originalPrice * (100 - explicitDiscount) + 50) / 100;
        } else {
            return (originalPrice * (100 - defaultDiscount) + 50) / 100;
        }
    }

    public static Page<CompactProductResponse> productPageToDtoPage(Page<Product> productPage) {
        return new PageImpl<>(
                productPage.stream().map(ProductDTOMapper::entityToCompactResponse).collect(Collectors.toList()),
                productPage.getPageable(),
                productPage.getTotalElements());
    }

    public static Page<CompactProductResponse> compactProductPageToCompactResponsePage(Page<CompactProductDto> compactProductPage) {

        List<CompactProductDto> compactProductDTOs = compactProductPage.getContent();
        List<CompactProductResponse> compactProductResponses = compactProductDTOs
                .stream()
                .map(ProductDTOMapper::compactProductToResponse)
                .toList();

        return new PageImpl<>(compactProductResponses,
                compactProductPage.getPageable(),
                compactProductPage.getTotalElements());

    }

    private static CompactProductResponse compactProductToResponse(CompactProductDto productDto) {

        CompactProductResponse compactProductResponse = new CompactProductResponse();
        compactProductResponse.productCode = productDto.productCode();
        compactProductResponse.imageUrl = productDto.imageUrl();
        compactProductResponse.name = productDto.name();
        compactProductResponse.rating = productDto.rating();
        compactProductResponse.isInStock = productDto.isInStock();
        compactProductResponse.originalPriceStotinki = productDto.originalPriceStotinki();
        compactProductResponse.reviewCount = productDto.reviewCount();
        compactProductResponse.salePriceStotinki = calculateDiscountPrice(
                productDto.originalPriceStotinki(),
                    productDto.defaultSaleDiscount(),
                    productDto.explicitDiscount());

        return compactProductResponse;
    }


    public static DetailedProductResponse entityToDetailedResponse(Product product,
                                                                   List<AttributeOfProjection> attributeProjections,
                                                                   List<String> productImageNames) {

        DetailedProductResponse detailedProductResponse = new DetailedProductResponse();
        int originalPrice = product.getOriginalPriceStotinki();
        SaleProduct saleProduct = product.getMainSaleProduct().orElse(null);

        detailedProductResponse.productCode = product.getProductCode();
        detailedProductResponse.name = product.getProductName();
        detailedProductResponse.categoryName = product.getProductCategory().getCategoryName();
        detailedProductResponse.manufacturer = product.getManufacturer().getManufacturerName();
        detailedProductResponse.attributes = AttributeMapper.projectionListToAttributeOptionResponseList(attributeProjections);
        detailedProductResponse.productDescription = product.getProductDescription();
        detailedProductResponse.deliveryCost = product.getDeliveryCost();
        detailedProductResponse.model = product.getModel();
        detailedProductResponse.productImageURLs = processDetailedProductImages(product, productImageNames);
        detailedProductResponse.rating = product.getRating();
        detailedProductResponse.originalPriceStotinki = product.getOriginalPriceStotinki();
        detailedProductResponse.salePriceStotinki = calculatePriceForDto(saleProduct, originalPrice);
        detailedProductResponse.isInStock = product.isInStock();

        return detailedProductResponse;
    }

    private static List<String > processDetailedProductImages(Product product, List<String> productImageNames) {
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {

            List<String> productImageURLs = new ArrayList<>();

            productImageURLs.add(product.getMainImageUrl());
            productImageURLs.addAll(productImageNames);

             return productImageURLs;
        } else {
            return List.of();
        }
    }

    public static List<CompactProductResponse> compactSaleProjectionListToResponseList(List<CompactSaleProductProjection> productProjections) {

        return productProjections.stream().map(ProductDTOMapper::compactSaleProjectionToResponse).toList();
    }

    public static CompactProductResponse compactSaleProjectionToResponse(CompactSaleProductProjection productProjection) {
        CompactProductResponse response = new CompactProductResponse();
        response.productCode = productProjection.getProductCode();
        response.salePriceStotinki = productProjection.getDiscountedPriceStotinki();
        response.isInStock = productProjection.getIsInStock();
        response.name = productProjection.getName();
        response.imageUrl = productProjection.getImageUrl();
        response.rating = productProjection.getRating();
        response.originalPriceStotinki = productProjection.getOriginalPriceStotinki();
        response.reviewCount = productProjection.getReviewCount();
        return response;
    }

    public static List<CompactProductResponse> compactProjectionListToResponseList(List<CompactProductProjection> products) {
        return products.stream().map(ProductDTOMapper::compactProjectionToResponse).toList();
    }

    public static CompactProductResponse compactProjectionToResponse(CompactProductProjection productProjection) {
        CompactProductResponse response = new CompactProductResponse();
        int originalPrice = productProjection.getOriginalPriceStotinki();
        Short defaultDiscount = productProjection.getDefaultSaleDiscount();
        Short explicitDiscount = productProjection.getExplicitDiscount();

        response.productCode = productProjection.getProductCode();
        response.salePriceStotinki = calculateDiscountPrice(originalPrice, defaultDiscount, explicitDiscount);
        response.isInStock = productProjection.getIsInStock();
        response.name = productProjection.getName();
        response.imageUrl = productProjection.getImageUrl();
        response.rating = productProjection.getRating();
        response.originalPriceStotinki = originalPrice;
        response.reviewCount = productProjection.getReviewCount();
        return response;
    }

    public static ProductForCompactPurchaseHistoryResponse compactPurchaseProjToCompactPurchHistoryResp(CompactPurchaseProductProjection projection) {
        return new ProductForCompactPurchaseHistoryResponse(projection.getProductName(),
                projection.getProductCode(),
                projection.getImageUrl());
    }


//
//    public static List<ProductForCompactPurchaseHistoryResponse> compactPurchaseProjListToCompactPurchHistoryRespList(List<CompactPurchaseProductProjection> projections) {
//
//        return projections.stream().map(ProductDTOMapper::compactPurchaseProjToCompactPurchHistoryResp).toList();
//
//    }


    public static ProductForCompactPurchaseHistoryResponse entityToCompactPurchIhistoryResp(Product product) {

        return new ProductForCompactPurchaseHistoryResponse(product.getProductName(),
                product.getProductCode(),
                product.getMainImageUrl());
    }

    public static List<ProductForCompactPurchaseHistoryResponse> entityListToCompactPurchIhistoryRespList(List<Product> products) {
        return products.stream().map(ProductDTOMapper::entityToCompactPurchIhistoryResp).toList();
    }


    public static DetailedPurchaseProductResponse detailedPurchaseProdProjectionToResponse(ProductForDetailedPurchaseProjection projection) {

        return new DetailedPurchaseProductResponse(projection.getProductCode(),
                projection.getProductName(),
                projection.getSinglePrice(),
                projection.getRating(),
                projection.getReviewCount(),
                projection.getImageUrl(),
                projection.getQuantity());

    }

    public static List<DetailedPurchaseProductResponse> detailedPurchaseProdProjectionListToResponseList(List<ProductForDetailedPurchaseProjection> projections) {
        return projections.stream().map(ProductDTOMapper::detailedPurchaseProdProjectionToResponse).toList();
    }

    public static AdminProductResponse detailedProjectionToAdminResponse (DetailedProductProjection projection) {
        return new AdminProductResponse(
                projection.getId(),
                projection.getName(),
                projection.getCategoryId(),
                projection.getOriginalPriceStotinki(),
                projection.getProductCode(),
                projection.getQuantityInStock(),
                projection.getManufacturerId(),
                projection.getModel(),
                projection.getProductDescription(),
                projection.getCategoryName(),
                projection.getManufacturerName()
        );
    }

    public static List<AdminProductResponse> detailedProjectionListToAdminResponseList (List<DetailedProductProjection> projections) {
        return projections.stream().map(ProductDTOMapper::detailedProjectionToAdminResponse).toList();
    }

    public static PageResponse<AdminProductResponse> adminProductProjPageToResponsePage(Page<DetailedProductProjection> projections) {
        List<DetailedProductProjection> content = projections.getContent();
        List<AdminProductResponse> adminProductResponses = ProductDTOMapper.detailedProjectionListToAdminResponseList(content);

        Page<AdminProductResponse> page = new PageImpl<>(adminProductResponses, projections.getPageable(), projections.getTotalElements());
        return PageResponse.from(page);
    }

}
