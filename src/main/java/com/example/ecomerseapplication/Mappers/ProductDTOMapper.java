package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.CompactProductPagedListResponse;
import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.DTOs.responses.DetailedProductResponse;
import com.example.ecomerseapplication.Entities.Product;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDTOMapper {

    public static CompactProductResponse entityToCompactResponse(Product product) {

        CompactProductResponse compactProductResponse = new CompactProductResponse();
        compactProductResponse.productCode = product.getProductCode();
        compactProductResponse.name = product.getProductName();
        compactProductResponse.imageUrl = product.getMainImageUrl();
        compactProductResponse.rating = product.getRating();
        compactProductResponse.originalPriceStotinki = product.getOriginalPriceStotinki();
        compactProductResponse.salePriceStotinki = product.getSalePriceStotinki();
        compactProductResponse.reviewCount = product.getReviews().size();

        return compactProductResponse;
    }

//    public static void addReviewsCountToCompactResponse(CompactProductResponse compactProductResponse, int reviewsCount) {
//        compactProductResponse.reviewCount = reviewsCount;
//    }

    public static Page<CompactProductResponse> productPageToDtoPage(Page<Product> productPage) {
        return new PageImpl<>(
                productPage.stream().map(ProductDTOMapper::entityToCompactResponse).collect(Collectors.toList()),
                productPage.getPageable(),
                productPage.getTotalElements());
    }

    public static DetailedProductResponse entityToDetailedResponse(Product product) {

        DetailedProductResponse detailedProductResponse = new DetailedProductResponse();

        detailedProductResponse.productCode = product.getProductCode();
        detailedProductResponse.name = product.getProductName();
        detailedProductResponse.categoryName = product.getProductCategory().getCategoryName();
        detailedProductResponse.manufacturer = product.getManufacturer().getManufacturerName();
        detailedProductResponse.attributes = product.getCategoryAttributeSet();
        detailedProductResponse.productDescription = product.getProductDescription();
        detailedProductResponse.deliveryCost = product.getDeliveryCost();
        detailedProductResponse.model = product.getModel();
        detailedProductResponse.productImages = product.getProductImages();
        detailedProductResponse.rating = product.getRating();
        detailedProductResponse.originalPriceStotinki = product.getOriginalPriceStotinki();
        detailedProductResponse.salePriceStotinki = product.getSalePriceStotinki();
        detailedProductResponse.reviews = product.getReviews()
                .stream()
                .map(ReviewEntToDTO::entityToResponse)
                .collect(Collectors.toList());

        return detailedProductResponse;
    }

    public static CompactProductPagedListResponse pagedListToDtoResponse(List<Product> productList,
                                                                         Pageable pageable) {
        CompactProductPagedListResponse productPagedListDto = new CompactProductPagedListResponse();

        List<CompactProductResponse> compactProductResponseList = productList
                .stream()
                .map(ProductDTOMapper::entityToCompactResponse)
                .toList();

        PagedListHolder<CompactProductResponse> productPage = new PagedListHolder<>(compactProductResponseList);

        if ((pageable.getPageNumber()) > productPage.getPageCount())
            productPage.setPage(0);
        else
            productPage.setPage(pageable.getPageNumber());

        productPage.setPageSize(pageable.getPageSize());

        productPagedListDto.content = productPage.getPageList();
        productPagedListDto.page.number = productPage.getPage();
        productPagedListDto.page.totalPages = productPage.getPageCount();
        productPagedListDto.page.size = productPage.getPageSize();
        productPagedListDto.page.totalElements = productPage.getNrOfElements();

        return productPagedListDto;
    }
}
