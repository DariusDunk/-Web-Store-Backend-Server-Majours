package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.CompactProductPagedListResponse;
import com.example.ecomerseapplication.DTOs.responses.DetailedProductResponse;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.Repositories.ProductRepository;
import com.example.ecomerseapplication.Specifications.ProductSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final CustomerCartService customerCartService;

    private final ReviewService reviewService;

    @Autowired
    public ProductService(ProductRepository productRepository, CustomerCartService customerCartService, ReviewService reviewService) {
        this.productRepository = productRepository;
        this.customerCartService = customerCartService;
        this.reviewService = reviewService;
    }


    public Page<CompactProductResponse> findAllProductsPage(PageRequest pageRequest) {
        return ProductDTOMapper.productPageToDtoPage(productRepository.findAllSortByRating(pageRequest));
    }

    public Page<CompactProductResponse> findAllByRatingResponsePage(PageRequest pageRequest) {
        return productRepository.findAllAsResponseSortByRating(pageRequest);
    }

    public Page<CompactProductResponse> getProductsLikeName(PageRequest pageRequest, String name) {

        Page<Product> productPage = productRepository.getByNameLike(name, pageRequest);

        return ProductDTOMapper.productPageToDtoPage(productPage);
    }

    public List<String> getNameSuggestions(String name) {
        return productRepository.getNameSuggestions(name);
    }

    public ResponseEntity<DetailedProductResponse> getByNameAndCode(String productCode, Customer customer) {
        Product product = productRepository.getByProductCode(productCode).orElse(null);
        if (product == null)
            return ResponseEntity.notFound().build();

        DetailedProductResponse detailedProductResponse = ProductDTOMapper.entityToDetailedResponse(product);

        if (customerCartService.cartExists(customer, product))
            detailedProductResponse.inCart = true;

        if (product.getFavouredBy().contains(customer))
            detailedProductResponse.inFavourites = true;

        if (reviewService.exists(product, customer))
            detailedProductResponse.reviewed = true;


        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(detailedProductResponse);
    }

    public Page<CompactProductResponse> getByManufacturer(Manufacturer manufacturer, Pageable pageable) {

        Page<Product> productPage = productRepository.getByManufacturerOrderByRatingDescIdAsc(manufacturer, pageable);

        return ProductDTOMapper.productPageToDtoPage(productPage);
    }


    public List<CompactProductResponse> getFeaturedProducts() {
        return productRepository.getProductsByRating()
                .stream()
                .map(ProductDTOMapper::entityToCompactResponse)
                .collect(Collectors.toList());
    }

    public Page<CompactProductResponse> getByCategory(ProductCategory productCategory, Pageable pageable) {
        return ProductDTOMapper
                .productPageToDtoPage(productRepository
                        .getByProductCategoryOrderByRatingDesc(productCategory, pageable));
    }

//    public Page<CompactProductResponse> getByCategoryFiltersManufacturerAndPriceRange(Set<CategoryAttribute> categoryAttributes,
//                                                                                      ProductCategory productCategory,
//                                                                                      int priceLowest,
//                                                                                      int priceHighest,
//                                                                                      Manufacturer manufacturer,
//                                                                                      Pageable pageable) {
//
//
//        Specification<Product> productSpec =
//                ProductSpecifications.equalsCategory(productCategory)
//                        .and(ProductSpecifications.priceBetween(priceLowest, priceHighest));
//
//        if (manufacturer != null) {
//            productSpec = productSpec.and(ProductSpecifications.equalsManufacturer(manufacturer));
//        }
//
//        if (!categoryAttributes.isEmpty()) {
//            productSpec = productSpec.and(ProductSpecifications.containsAttributes(categoryAttributes));
//        }
//
//        Page<Product> products = productRepository.findAll(productSpec,pageable);
//
//        return ProductDTOMapper.productPageToDtoPage(products);
//    }

    public Page<CompactProductResponse> getByCategoryFiltersManufacturerAndPriceRange(Set<CategoryAttribute> categoryAttributes,
                                                                                      ProductCategory productCategory,
                                                                                      int priceLowest,
                                                                                      int priceHighest,
                                                                                      List<Manufacturer> manufacturers,
                                                                                      List<Integer> ratings,
                                                                                      Pageable pageable) {


        Specification<Product> productSpec =
                ProductSpecifications.equalsCategory(productCategory)
                        .and(ProductSpecifications.priceBetween(priceLowest, priceHighest));

        if (!manufacturers.isEmpty()) {
            System.out.println("Manufacturers: ");
            for (Manufacturer manufacturer : manufacturers) {
                System.out.println(manufacturer.getManufacturerName());
            }
            productSpec = productSpec.and(ProductSpecifications.manufacturerIn(manufacturers));
        }

        if (!categoryAttributes.isEmpty()) {
            productSpec = productSpec.and(ProductSpecifications.containsAttributes(categoryAttributes));
        }

        if (ratings != null&& !ratings.isEmpty()) {
            System.out.println("Ratings: ");
            for (Integer rating : ratings) {
                System.out.println(rating);
            }
            productSpec = productSpec.and(ProductSpecifications.ratingIn(ratings));
        }

        Page<Product> products = productRepository.findAll(productSpec,pageable);

        return ProductDTOMapper.productPageToDtoPage(products);
    }

    public Product findByPCode(String code) {
        return productRepository.getByProductCode(code).orElse(null);
    }

    public void save(Product product) {
        productRepository.save(product);
    }

    public void saveAll(List<Product> updatedQuantProducts) {
        productRepository.saveAll(updatedQuantProducts);
    }
}
