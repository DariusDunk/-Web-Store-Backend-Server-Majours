package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.AttributeOptionResponse;
import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.DTOs.responses.DetailedProductResponse;
import com.example.ecomerseapplication.Entities.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductDTOMapper {

    public static CompactProductResponse entityToCompactResponse(Product product) {

        CompactProductResponse compactProductResponse = new CompactProductResponse();
        compactProductResponse.productCode = product.getProductCode();
        compactProductResponse.name = product.getProductName();
        compactProductResponse.imageUrl = product.getMainImageUrl();
        compactProductResponse.rating = product.getRating();
        compactProductResponse.originalPriceStotinki = product.getOriginalPriceStotinki();

        int originalPrice = product.getOriginalPriceStotinki();
        SaleProduct saleProduct = product.getSingleSaleProduct().orElse(null);

        if (saleProduct != null) {

            Sale sale = saleProduct.getSale();

            compactProductResponse.salePriceStotinki = calculateDiscountPrice(originalPrice,
                    sale.getDiscountPercent(),
                    saleProduct.getOverrideDiscountPercentage());
        } else {
            compactProductResponse.salePriceStotinki = product.getOriginalPriceStotinki();
        }

//        compactProductResponse.salePriceStotinki = product.getSalePriceStotinki();


        compactProductResponse.reviewCount = product.getReviews().size();
        compactProductResponse.isInStock = product.isInStock();

        return compactProductResponse;
    }
    //todo izvikai tova v ostanalite maperi za produkt i napravi mapper ot query DTO-tata kym response DTO-tata
    private static int calculateDiscountPrice(int originalPrice, Short defaultDiscount, Short explicitDiscount) {

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

    public static DetailedProductResponse entityToDetailedResponse(Product product, List<String[]> attributeNameMUnitPairs) {

        DetailedProductResponse detailedProductResponse = new DetailedProductResponse();

        detailedProductResponse.productCode = product.getProductCode();
        detailedProductResponse.name = product.getProductName();
        detailedProductResponse.categoryName = product.getProductCategory().getCategoryName();
        detailedProductResponse.manufacturer = product.getManufacturer().getManufacturerName();
        detailedProductResponse.attributes = formAttributeOptionResponses(product, attributeNameMUnitPairs);
        detailedProductResponse.productDescription = product.getProductDescription();
        detailedProductResponse.deliveryCost = product.getDeliveryCost();
        detailedProductResponse.model = product.getModel();
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            detailedProductResponse.productImageURLs = product
                    .getProductImages()
                    .stream()
                    .map((ProductImage::getImageFileName))
                    .toList();
        } else {
            detailedProductResponse.productImageURLs = List.of();
        }

        detailedProductResponse.rating = product.getRating();
        detailedProductResponse.originalPriceStotinki = product.getOriginalPriceStotinki();

        int originalPrice = product.getOriginalPriceStotinki();
        SaleProduct saleProduct = product.getSingleSaleProduct().orElse(null);

        if (saleProduct != null) {

            Sale sale = saleProduct.getSale();

            detailedProductResponse.salePriceStotinki = calculateDiscountPrice(originalPrice,
                    sale.getDiscountPercent(),
                    saleProduct.getOverrideDiscountPercentage());
        } else {
            detailedProductResponse.salePriceStotinki = product.getOriginalPriceStotinki();
        }

//        detailedProductResponse.salePriceStotinki = product.getSalePriceStotinki();
        detailedProductResponse.isInStock = product.isInStock();

        return detailedProductResponse;
    }

    private static Set<AttributeOptionResponse> formAttributeOptionResponses(Product product, List<String[]> attributeNameMUnitPairs) {
        Set<CategoryAttribute> categoryAttributes = product.getCategoryAttributeSet();

        Set<AttributeOptionResponse> attributeOptionResponses = new HashSet<>();

        if (categoryAttributes.size() == attributeNameMUnitPairs.size()) {
            for (CategoryAttribute categoryAttribute : categoryAttributes) {
                for (String[] pair : attributeNameMUnitPairs) {
                    if (pair[0].equals(categoryAttribute
                            .getAttributeName()
                            .getAttributeName())) {

                        attributeOptionResponses.add(new AttributeOptionResponse(pair[0],
                                categoryAttribute.getAttributeOption(),
                                pair[1]));
                    }
                }
            }
        }
        return attributeOptionResponses;
    }

}
